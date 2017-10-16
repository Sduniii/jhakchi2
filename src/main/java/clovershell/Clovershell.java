package clovershell;

import lombok.Getter;
import org.usb4java.*;
import tools.Debug;
import tools.UsbDevices;

import java.nio.ByteBuffer;
import java.util.*;

public class Clovershell implements DataReceivedListener {

    final int vid = 0x1f3a;
    final int pid = 0xefe8;
    Device usbDevice;
    Thread mainThread, shellListenerThread;
    boolean online = false;
    short schellPort = 1023;
    Queue<ShellConnection> pendingShellConnections = new PriorityQueue<ShellConnection>();
    List<ExecConnection> pendingExecConnections = new ArrayList<ExecConnection>();
    private ShellConnection[] shellConnections = new ShellConnection[256];
    private ExecConnection[] execConnections = new ExecConnection[256];
    @Getter
    boolean enabled = false;
    boolean autoreconnect = true;
    byte[] lastPingResponse = null;
    Date lastAliveTime;
    EndpointReader epReader;
    EndpointWriter epWriter;

    private List<ConnectedListener> listeners = new ArrayList<>();

    public void addListener(ConnectedListener l){
        listeners.add(l);
    }


    Runnable mainThreadLoop = new Runnable() {
        @Override
        public void run() {
            UsbDevices.openConnection();
            try {
                while (enabled) {
                    online = false;
                    Debug.WriteLine("Waiting for Clovershell");
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
                            Debug.WriteLine(fresult);
                            if (fresult != LibUsb.SUCCESS)
                                Debug.WriteLine("Unable to open USB device " + fresult);
                            try {
                                // Use device handle here

                                final DeviceDescriptor descriptor = new DeviceDescriptor();
                                int result = LibUsb.getDeviceDescriptor(device, descriptor);
                                if (result < 0) {
                                    Debug.WriteLine("Unable to read device descriptor " + result);
                                }

                                for (byte i = 0; i < descriptor.bNumConfigurations(); i += 1) {
                                    final ConfigDescriptor cdescriptor = new ConfigDescriptor();
                                    final int cresult = LibUsb.getConfigDescriptor(device, i, cdescriptor);
                                    if (result < 0) {
                                        Debug.WriteLine("Unable to read config descriptor " + result);
                                    }
                                    try {
                                        for (byte j = 0; j < cdescriptor.bNumInterfaces(); j++) {

                                            final int iresult = LibUsb.claimInterface(handle, j);
                                            if (iresult != LibUsb.SUCCESS)
                                                Debug.WriteLine("Unable to claim interface " + result);
                                            try {
                                                for (Interface iface : cdescriptor.iface()) {
                                                    for (InterfaceDescriptor ifd : iface.altsetting()) {
                                                        for (EndpointDescriptor epd : ifd.endpoint()) {
                                                            if ((epd.bEndpointAddress() & (byte)0x80) != 0) {
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
                                Debug.WriteLine(inEndp);
                                if (inEndp != (byte)0x81 || outEndp != (byte)0x01)
                                    break;
                                epReader = new EndpointReader((byte) inEndp, 65536, handle, 1000);
                                epWriter = new EndpointWriter(handle, (byte) outEndp, 1000);
                                while (epReader.read() != null) ;
                                epReader.start();
                                lastAliveTime = Calendar.getInstance().getTime();
                                Debug.WriteLine("Clovershell connected");
                                // Kill all other sessions and drop all output
                                killAll();
                                onConnected();
//                                while (device) {
//                                    Thread.Sleep(100);
//                                    if ((IdleTime.TotalSeconds >= 10) && (Ping() < 0))
//                                        throw new ClovershellException("no answer from device");
//                                }
                                break;
                            } catch (ClovershellException ex) {
                                Debug.WriteLine(ex.getMessage() + ex.getStackTrace());
                                break;
                            }
                        } finally {
                            LibUsb.close(handle);
                        }
                    }
                    if (online) Debug.WriteLine("Clovershell disconnected");
                    online = false;
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

    private void onConnected() {
        for(ConnectedListener l :listeners){
            l.onConnected(true);
        }
    }

    private void killAll() throws ClovershellException {
        int tLen;
        byte[] buff = new byte[4];
        buff[0] = (byte) ClovershellCommand.CMD_SHELL_KILL_ALL.getType();
        buff[1] = 0;
        buff[2] = 0;
        buff[3] = 0;
        tLen = epWriter.write(buff);
        if (tLen != buff.length)
            throw new ClovershellException("kill all shell: write error!");
        buff[0] = (byte) ClovershellCommand.CMD_EXEC_KILL_ALL.getType();
        buff[1] = 0;
        buff[2] = 0;
        buff[3] = 0;
        tLen = epWriter.write(buff);
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

    private enum ClovershellCommand {
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
        int count = buffer.limit();
        Debug.WriteLine(buffer.get(0));
    }
}
