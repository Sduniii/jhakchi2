package clovershell;

import lombok.Getter;
import lombok.Setter;
import tools.Debug;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.Charset;

public class ShellConnection {
    public final ClovershellConnection connection;
    @Getter
    @Setter
    private Socket socket;
    @Getter
    @Setter
    private int id;
    @Getter
    @Setter
    private Thread shellConnectionThread;

    public ShellConnection(ClovershellConnection connection, Socket socket) {
        this.connection = connection;
        this.socket = socket;
        id = -1;
        try {
            socket.getOutputStream().write(new byte[]{(byte) 0xFF, (byte) 0xFD, (byte) 0x03}); // Do Suppress Go Ahead
            socket.getOutputStream().write(new byte[]{(byte) 0xFF, (byte) 0xFB, (byte) 0x03}); // Will Suppress Go Ahead
            socket.getOutputStream().write(new byte[]{(byte) 0xFF, (byte) 0xFB, (byte) 0x01}); // Will Echo
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Getter
    private Runnable shellConnectionLoop = () -> {
        final ClovershellConnection connection = (this).connection;
        try {
            byte[] buff = new byte[1024];
            while (socket.isConnected()) {
                int l = socket.getInputStream().read(buff);
                if (l > 0) {
                    int start = 0;
                    int pos = 0;
                    do {
                        if ((pos + 1 < l) && (buff[pos] == '\r') && (buff[pos + 1] == '\n')) // New line?
                        {
                            // Hey, dot not send \r\n! I'll cut it to \n
                            buff[pos] = (byte) '\n';
                            connection.writeUsb(ClovershellConnection.ClovershellCommand.CMD_SHELL_IN, (byte) id, buff, start, pos - start + 1);
                            pos += 2;
                            start = pos;
                        } else if ((pos + 1 < l) && (buff[pos] == 0xFF)) // Telnet command?
                        {
                            if (buff[pos + 1] == 0xFF) // Or just 0xFF...
                            {
                                connection.writeUsb(ClovershellConnection.ClovershellCommand.CMD_SHELL_IN, (byte) id, buff, start, pos - start + 1);
                                pos += 2;
                                start = pos;
                            } else if (pos + 2 < l) {
                                if (pos - start > 0)
                                    connection.writeUsb(ClovershellConnection.ClovershellCommand.CMD_SHELL_IN, (byte) id, buff, start, pos - start);
                                byte cmd = buff[pos + 1]; // Telnet command code
                                byte opt = buff[pos + 2]; // Telnet option code
                                Debug.WriteLine(String.format("Telnet command: CMD=%dX2 ARG=%dX2", cmd, opt));
                                pos += 3;
                                start = pos;
                            }
                        } else pos++; // No, moving to next character
                        if ((pos == l) && (l - start > 0)) // End of packet
                        {
                            connection.writeUsb(ClovershellConnection.ClovershellCommand.CMD_SHELL_IN, (byte) id, buff, start, l - start);
                        }
                    } while (pos < l);
                } else
                    break;
            }
        } catch (Exception ex) {
            Debug.WriteLine(ex.getMessage() + ex.getStackTrace());
            if (socket.isConnected())
                try {
                    socket.getOutputStream().write(("Error: " + ex.getMessage()).getBytes(Charset.forName("ASCII")));
                } catch (IOException e) {
                    e.printStackTrace();
                }
        } catch (ClovershellException e) {
            e.printStackTrace();
        } finally {
            shellConnectionThread = null;
        }
        Debug.WriteLine(String.format("Shell client %d disconnected", id));
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        connection.setSchellConnection(id, null);
    };

    public void Dispose() {
        if (shellConnectionThread != null)
            try {
                shellConnectionThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        if (socket != null)
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        socket = null;
        if (id > 0)
            connection.setSchellConnection(id, null);
    }


    public void send(byte[] data, int pos, int len) {
        try {
            socket.getOutputStream().write(data,pos,len);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

