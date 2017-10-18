package clovershell;

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;
import lombok.Getter;
import lombok.Setter;
import org.usb4java.*;
import tools.Debug;
import tools.Lists;
import tools.PositionInputStream;
import tools.UsbDevices;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

public class ClovershellConnection implements DataReceivedListener {

    final int vid = 0x1f3a;
    final int pid = 0xefe8;
    Device usbDevice;
    Thread mainThread, shellListenerThread;
    @Getter
    boolean online = false;
    short schellPort = 1023;
    Queue<ShellConnection> pendingShellConnections = new PriorityQueue<>();
    List<ExecConnection> pendingExecConnections = new ArrayList<>();
    private ShellConnection[] shellConnections = new ShellConnection[256];
    private ExecConnection[] execConnections = new ExecConnection[256];
    @Getter
    boolean enabled = false;
    @Setter
    boolean autoreconnect = false;
    byte[] lastPingResponse = null;
    Calendar lastAliveTime;
    EndpointReader epReader;
    EndpointWriter epWriter;

    public void setSchellConnection(int id, ShellConnection s) {
        shellConnections[id] = s;
    }

    private List<ConnectedListener> listeners = new ArrayList<>();

    public void addOnConnectedListener(ConnectedListener l) {
        listeners.add(l);
    }


    Runnable mainThreadLoop = new Runnable() {
        @Override
        public void run() {
            UsbDevices.openConnection();
            try {
                while (enabled) {
                    online = false;
                    Debug.WriteLine("Waiting for ClovershellConnection");
                    while (enabled) {
                        int inEndp = -1;
                        int outEndp = -1;
                        int inMax = 0;
                        int outMax = 0;
                        Device device = UsbDevices.findDevice((short) vid, (short) pid);
                        if (device == null) break;
                        DeviceHandle handle = new DeviceHandle();
                        try {
                            int fresult = LibUsb.open(device, handle);
                            if (fresult != LibUsb.SUCCESS) {
                                Debug.WriteLine("Unable to open USB device " + fresult);
                                break;
                            }
                            try {

                                final DeviceDescriptor descriptor = new DeviceDescriptor();
                                int result = LibUsb.getDeviceDescriptor(device, descriptor);
                                if (result < 0) {
                                    Debug.WriteLine("Unable to read device descriptor " + result);
                                    break;
                                }

                                for (byte i = 0; i < descriptor.bNumConfigurations(); i += 1) {
                                    final ConfigDescriptor cdescriptor = new ConfigDescriptor();
                                    final int cresult = LibUsb.getConfigDescriptor(device, i, cdescriptor);
                                    if (result < 0) {
                                        Debug.WriteLine("Unable to read config descriptor " + result);
                                        break;
                                    }
                                    try {
                                        for (byte j = 0; j < cdescriptor.bNumInterfaces(); j++) {

                                            final int iresult = LibUsb.claimInterface(handle, j);
                                            if (iresult != LibUsb.SUCCESS) {
                                                Debug.WriteLine("Unable to claim interface " + result);
                                                break;
                                            }
                                            try {
                                                for (Interface iface : cdescriptor.iface()) {
                                                    for (InterfaceDescriptor ifd : iface.altsetting()) {
                                                        for (EndpointDescriptor epd : ifd.endpoint()) {
                                                            if ((epd.bEndpointAddress() & (byte) 0x80) != 0) {
                                                                inEndp = epd.bEndpointAddress();
                                                                inMax = epd.wMaxPacketSize();
                                                            } else {
                                                                outEndp = epd.bEndpointAddress();
                                                                outMax = epd.wMaxPacketSize();
                                                            }
                                                        }
                                                    }
                                                }
                                            } finally {
                                                result = LibUsb.releaseInterface(handle, j);
                                                if (result != LibUsb.SUCCESS)
                                                    Debug.WriteLine("Unable to release interface " + result);
                                            }
                                        }
                                    } finally {
                                        // Ensure that the config descriptor is freed
                                        LibUsb.freeConfigDescriptor(cdescriptor);
                                    }
                                }

                                if (inEndp != (byte) 0x81 || outEndp != (byte) 0x01)
                                    break;
                                setEpReader(new EndpointReader((byte) inEndp, 65536, handle, 1000));
                                epWriter = new EndpointWriter(handle, (byte) outEndp, 1000);
                                while (epReader.read() != null) ;
                                epReader.start();
                                lastAliveTime = Calendar.getInstance();
                                Debug.WriteLine("ClovershellConnection connected");
                                // Kill all other sessions and drop all output
                                killAll();
                                online = true;
                                onConnected();
                                int p = 0;

                                while ((p = ping()) >= 0) {
                                    Thread.sleep(1000);
                                    if ((idleTime() >= 1000) && (p < 0)) {
                                        Debug.WriteLine("no answer from device");
                                        break;
                                    }
                                }
                                break;
                            } catch (Exception ex) {
                                Debug.WriteLine(ex.getMessage() + ex.getStackTrace());
                                break;
                            } catch (ClovershellException e) {
                                e.printStackTrace();
                                break;
                            }
                        } finally {
                            LibUsb.close(handle);
                        }
                    }
                    if (online) Debug.WriteLine("ClovershellConnection disconnected");
                    online = false;
                    onDisconnected();
                    if (epReader != null)
                        epReader.dispose();
                    epReader = null;
                    epWriter = null;
                    if (!autoreconnect) enabled = false;
                    Thread.sleep(1000);

                }
                return;

            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                UsbDevices.closeConnection();
            }

        }
    };

    private void onDisconnected() {
        for (ConnectedListener l : listeners) {
            l.onConnected(false);
        }
    }

    private int ping() throws ClovershellException, InterruptedException {
        if (!online) return -1;
        Random rand = new Random();
        byte[] data = new byte[4];
        rand.nextBytes(data);
        lastPingResponse = null;
        Calendar start = Calendar.getInstance();
        writeUsb(ClovershellCommand.CMD_PING, (byte) 0, data, 0, -1);
        int t = 100;
        while ((lastPingResponse == null || !Arrays.equals(lastPingResponse, data)) && (t > 0)) {
            Thread.sleep(10);
            t--;
        }
        if (t <= 0) return -1;
        return (int) (Calendar.getInstance().getTimeInMillis() - start.getTimeInMillis());

    }

    private void onConnected() {
        for (ConnectedListener l : listeners) {
            l.onConnected(true);
        }
    }

    private long idleTime() {
        return Calendar.getInstance().getTimeInMillis() - lastAliveTime.getTimeInMillis();
    }

    private void killAll() throws ClovershellException {
        int tLen;
        byte[] buff = new byte[4];
        buff[0] = (byte) ClovershellCommand.CMD_SHELL_KILL_ALL.getType();
        buff[1] = 0;
        buff[2] = 0;
        buff[3] = 0;
        while(epWriter.isLocked()) ;
        epWriter.setLocked(true);
        tLen = epWriter.write(buff);
        epWriter.setLocked(false);
        if (tLen != buff.length)
            throw new ClovershellException("kill all shell: write error!");
        buff[0] = (byte) ClovershellCommand.CMD_EXEC_KILL_ALL.getType();
        buff[1] = 0;
        buff[2] = 0;
        buff[3] = 0;
        while(epWriter.isLocked()) ;
        epWriter.setLocked(true);
        tLen = epWriter.write(buff);
        epWriter.setLocked(false);
        if (tLen != buff.length)
            throw new ClovershellException("kill all exec: write error!");
    }

    public void setEnabled(boolean value) {
        if (enabled == value) return;
        enabled = value;
        if (value) {
            mainThread = new Thread(mainThreadLoop);
            mainThread.start();
            Debug.WriteLine("Thread startet!");
        }
    }

    void writeUsb(ClovershellCommand cmd, byte arg, byte[] data, int pos, int l) throws ClovershellException, InterruptedException {
        if (!online) throw new ClovershellException("NES Mini is offline");
        if (epWriter == null) return;

        int len = (l >= 0) ? l : ((data != null) ? (data.length - pos) : 0);

        if(cmd != ClovershellCommand.CMD_PING ) Debug.WriteLine(String.format("->[CLV] cmd=%s, arg=%X2, len=%d, data=%s", cmd, arg, len, data != null ? Arrays.toString(data) : ""));

        byte[] buff = new byte[len + 4];
        buff[0] = (byte) cmd.getType();
        buff[1] = arg;
        buff[2] = (byte) (len & 0xFF);
        buff[3] = (byte) ((len >> 8) & 0xFF);
        if (data != null)
            System.arraycopy(data, pos, buff, 4, len);
        int tLen = 0;
        pos = 0;
        len += 4;
        int repeats = 0;
        while (pos < len) {
            byte[] tb = new byte[len];
            System.arraycopy(buff, pos, tb, 0, len);

            while(epWriter.isLocked()) Thread.sleep(100);
            epWriter.setLocked(true);
            tLen = epWriter.write(tb);
            epWriter.setLocked(false);

            //Debug.WriteLine("->[CLV] " + Arrays.toString(tb));
            pos += tLen;
            len -= tLen;
            if (tLen == -1) {
                if (repeats >= 10) break;
                Debug.WriteLine("write error: " + tLen);
                repeats++;
                Thread.sleep(100);
            }
        }
        if (len > 0)
            throw new ClovershellException("write error");

    }

    public void setEpReader(EndpointReader epReader) {
        this.epReader = epReader;
        if (epReader != null) {
            epReader.addListener(this);
        }
    }


    protected enum ClovershellCommand {
        CMD_PING(0),
        CMD_PONG(1),
        CMD_SHELL_NEW_REQ(2),
        CMD_SHELL_NEW_RESP(3),
        CMD_SHELL_IN(4),
        CMD_SHELL_OUT(5),
        CMD_SHELL_CLOSED(6),
        CMD_SHELL_KILL(7),
        CMD_SHELL_KILL_ALL(8),
        CMD_EXEC_NEW_REQ(9),
        CMD_EXEC_NEW_RESP(10),
        CMD_EXEC_PID(11),
        CMD_EXEC_STDIN(12),
        CMD_EXEC_STDOUT(13),
        CMD_EXEC_STDERR(14),
        CMD_EXEC_RESULT(15),
        CMD_EXEC_KILL(16),
        CMD_EXEC_KILL_ALL(17),
        CMD_EXEC_STDIN_FLOW_STAT(18),
        CMD_EXEC_STDIN_FLOW_STAT_REQ(19);

        @Getter
        private int type;

        ClovershellCommand(int type) {
            this.type = type;
        }

        public static ClovershellCommand get(int id) {
            ClovershellCommand[] types = ClovershellCommand.values();
            for (ClovershellCommand type1 : types) {
                if (type1.type == id)
                    return type1;
            }
            return CMD_PING;
        }
    }

    @Override
    public void dataReceived(ByteBuffer buffer) {
        int pos = 0;
        Debug.WriteLine(buffer.slice().remaining());
        byte[] buff = new byte[buffer.slice().remaining()];
        buffer.get(buff);
        int count = buff.length;

        while (count > 1) {
            ClovershellCommand cmd = ClovershellCommand.get(buff[pos]);
            byte arg = buff[pos + 1];
            int len = buff[pos + 2] | (buff[pos + 3] * 0x100);
            if(len == 0) break;
            if(cmd != ClovershellCommand.CMD_PING && cmd != ClovershellCommand.CMD_PONG) Debug.WriteLine(count);
            proceedPacket(cmd, arg, buff, pos + 4, len);
            count -= len + 4;
            pos += len + 4;
        }
    }

    private void proceedPacket(ClovershellCommand cmd, byte arg, byte[] data, int pos, int len) {
        if (len < 0)
            len = data.length;
        byte[] tb = new byte[len];
        System.arraycopy(data, pos, tb, 0, len);
        if(cmd != ClovershellCommand.CMD_PING && cmd != ClovershellCommand.CMD_PONG) Debug.WriteLine(String.format("<-[CLV] cmd=%s, arg=%dX2, len=%d, data=%s", cmd, arg, len, Arrays.toString(tb)));

        lastAliveTime = Calendar.getInstance();
        switch (cmd) {
            case CMD_PONG:
                lastPingResponse = new byte[len];
                System.arraycopy(data, pos, lastPingResponse, 0, len);
                break;
            case CMD_SHELL_NEW_RESP:
                acceptShellConnection(arg);
                break;
            case CMD_SHELL_OUT:
                shellOut(arg, data, pos, len);
                break;
            case CMD_SHELL_CLOSED:
                shellClosed(arg);
                break;
            case CMD_EXEC_NEW_RESP:
                Debug.WriteLine(new String(data, pos, len, Charset.forName("UTF-8")));
                newExecConnection(arg, new String(data, pos, len, Charset.forName("UTF-8")));
                break;
            case CMD_EXEC_STDOUT:
                execOut(arg, data, pos, len);
                break;
            case CMD_EXEC_STDERR:
                execErr(arg, data, pos, len);
                break;
            case CMD_EXEC_RESULT:
                execResult(arg, data, pos, len);
                break;
            case CMD_EXEC_STDIN_FLOW_STAT:
                execStdinStat(arg, data, pos, len);
                break;
        }
    }

    private void acceptShellConnection(byte arg) {
        ShellConnection connection = pendingShellConnections.poll();
        if (connection == null) return;
        connection.setId(arg);
        shellConnections[connection.getId()] = connection;
        //Debug.WriteLine(string.Format("Shell started, id={0}", connection.id));
        connection.setShellConnectionThread(new Thread(connection.getShellConnectionLoop()));
        connection.getShellConnectionThread().start();
    }

    private void newExecConnection(byte arg, String command) {

        ExecConnection connection = Lists.getLast(pendingExecConnections.stream().filter(execConnection -> execConnection.getCommand().toLowerCase().equals(command.toLowerCase())).collect(Collectors.toList()));
        pendingExecConnections.remove(connection);
        //Debug.WriteLine("Executing: " + command);
        connection.setId(arg);
        execConnections[arg] = connection;
        if (connection.getStdin() != null) {
            connection.setStdinThread(new Thread(connection.getStdinLoop()));
            connection.getStdinThread().start();
        }
    }

    void execOut(byte arg, byte[] data, int pos, int len) {
        ExecConnection c = execConnections[arg];
        if (c == null) return;
        if (c.getStdout() != null)
            try {
                c.getStdout().write(data, pos, len);
            } catch (IOException e) {
                e.printStackTrace();
            }
        c.setLastDataTime(Calendar.getInstance());
        Debug.WriteLine("stdout: " + new String(data, pos, len, Charset.forName("UTF-8")));
        if (len == 0)
            c.setStdoutFinished(true);
    }

    void execErr(byte arg, byte[] data, int pos, int len) {
        ExecConnection c = execConnections[arg];
        if (c == null) return;
        if (c.getStderr() != null)
            c.getStderr().put(data, pos, len);

        //Debug.WriteLine("stderr: " + Encoding.UTF8.GetString(data, pos, len));
        c.setLastDataTime(Calendar.getInstance());
        if (len == 0)
            c.setStderrFinished(true);
    }

    void execResult(byte arg, byte[] data, int pos, int len) {
        ExecConnection c = execConnections[arg];
        if (c == null) return;
        c.setResult(data[pos]);
        Debug.WriteLine(String.format("%s # exit code: %d", c.getCommand(), c.getResult()));
        c.setFinished(true);
    }

    void execStdinStat(byte arg, byte[] data, int pos, int len) {
        ExecConnection c = execConnections[arg];
        if (c == null) return;
        c.setStdinQueue(data[pos] | data[pos + 1] * 0x100 | data[pos + 2] * 0x10000 | data[pos + 3] * 0x1000000);
        c.setStdinPipeSize(data[pos + 4] | data[pos + 5] * 0x100 | data[pos + 6] * 0x10000 | data[pos + 7] * 0x1000000);
    }


    void shellOut(byte id, byte[] data, int pos, int len) {
        if (shellConnections[id] == null) return;
        shellConnections[id].send(data, pos, len);
    }

    void shellClosed(byte id) {
        if (shellConnections[id] == null) return;
        shellConnections[id].Dispose();
        shellConnections[id] = null;
    }

    public String executeSimple(String command, int timeout, boolean throwOnNonZero) throws ClovershellException, IOException, InterruptedException {
        ByteOutputStream stdOut = new ByteOutputStream();
        execute(command, null, stdOut, null, timeout, throwOnNonZero);
        byte[] buff = stdOut.getBytes();
        return new String(buff, Charset.forName("UTF-8")).trim();
    }

    public int execute(String command, PositionInputStream stdin, OutputStream stdout, ByteBuffer stderr, int timeout, boolean throwOnNonZero) throws ClovershellException, InterruptedException, IOException {
        if (!online) throw new ClovershellException("NES Mini is offline");
        if (throwOnNonZero && stderr == null)
            stderr = ByteBuffer.allocate(255);
        ExecConnection c = new ExecConnection(this, command, stdin, stdout, stderr);
        try {
            pendingExecConnections.add(c);
            writeUsb(ClovershellCommand.CMD_EXEC_NEW_REQ, (byte) 0, command.getBytes(Charset.forName("UTF-8")), 0, -1);
            int t = 0;
            while (c.getId() < 0) {
                Thread.sleep(50);
                t++;
                if (t >= 50)
                    throw new ClovershellException("exec request timeout");
            }
            while (!c.isFinished()) {
                Thread.sleep(50);
                if (!online)
                    throw new ClovershellException("device goes offline");
                if (!c.isFinished() && timeout > 0 && Calendar.getInstance().getTimeInMillis() - c.getLastDataTime().getTimeInMillis() > timeout)
                    throw new ClovershellException("clovershell read timeout");
            }
            if (throwOnNonZero && c.getResult() != 0) {
                String errText = "";
                stderr.reset();
                byte[] b = new byte[stderr.remaining()];
                stderr.get(b);
                errText = ": " + new String(b);
                throw new ClovershellException(String.format("shell command \"%s\" returned exit code %d%s", command, c.getResult(), errText));
            }
            return c.getResult();
        } finally {
            if (c.getId() >= 0)
                execConnections[c.getId()] = null;
        }

    }
}
