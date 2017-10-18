package clovershell;

import lombok.Getter;
import lombok.Setter;
import tools.Debug;
import tools.PositionInputStream;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Calendar;

public class ExecConnection {
    private final ClovershellConnection connection;
    @Setter
    @Getter
    private final String command;
    @Setter
    @Getter
    private PositionInputStream stdin;
    @Setter
    @Getter
    private int stdinPipeSize;
    @Setter
    @Getter
    private int stdinQueue;
    @Setter
    @Getter
    private OutputStream stdout;
    @Setter
    @Getter
    private ByteBuffer stderr;
    @Setter
    @Getter
    private int id;
    @Setter
    @Getter
    private boolean finished;
    @Getter
    @Setter
    private int result;
    @Setter
    @Getter
    private boolean stdinFinished;
    @Setter
    @Getter
    private boolean stdoutFinished;
    @Setter
    @Getter
    private boolean stderrFinished;
    @Setter
    @Getter
    private Thread stdinThread;
    @Setter
    @Getter
    private Calendar LastDataTime;
    @Setter
    @Getter
    private Runnable stdinLoop = () -> {
        try {
            final ClovershellConnection connection = (this).connection;
            if (stdin == null || (this).connection == null) return;
            stdin.reset();
            byte[] buffer = new byte[8 * 1024];
            int l;
            while (connection.isOnline()) {
                l = stdin.read(buffer, 0, buffer.length);
                if (l > 0)
                    connection.writeUsb(ClovershellConnection.ClovershellCommand.CMD_EXEC_STDIN, (byte) id, buffer, 0, l);
                else
                    break;
                LastDataTime = Calendar.getInstance();
                if (stdinQueue > 32 * 1024 && connection.isOnline()) {
                    Debug.WriteLine(String.format("queue: %d / %d, %dMB / %dMB (%d%)",
                            stdinQueue, stdinPipeSize, stdin.getPosition() / 1024 / 1024, stdin.available() / 1024 / 1024, 100 * stdin.getPosition() / stdin.available()));
                    while (stdinQueue > 16 * 1024) {
                        Thread.sleep(50);
                        connection.writeUsb(ClovershellConnection.ClovershellCommand.CMD_EXEC_STDIN_FLOW_STAT_REQ, (byte) id, null, 0, -1);
                    }
                }
            }
            connection.writeUsb(ClovershellConnection.ClovershellCommand.CMD_EXEC_STDIN, (byte) id, null, 0, -1); // eof
            if (stdinQueue > 0 && connection.isOnline()) {
                Thread.sleep(50);
                connection.writeUsb(ClovershellConnection.ClovershellCommand.CMD_EXEC_STDIN_FLOW_STAT_REQ, (byte) id, null, 0, -1);
            }
            stdinFinished = true;
        } catch (ClovershellException ex) {
            Debug.WriteLine("stdin error: " + ex.getMessage() + ex.getStackTrace());
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        } finally {
            stdinThread = null;
        }
    };


    public ExecConnection(ClovershellConnection connection, String command, PositionInputStream stdin, OutputStream stdout, ByteBuffer stderr) {
        this.connection = connection;
        this.command = command;
        id = -1;
        stdinPipeSize = 0;
        stdinQueue = 0;
        this.stdin = stdin;
        this.stdout = stdout;
        this.stderr = stderr;
        finished = false;
        stdinFinished = false;
        stdoutFinished = false;
        stderrFinished = false;
        LastDataTime = Calendar.getInstance();
    }



    public void Dispose() throws InterruptedException {
        if (stdinThread != null)
            stdinThread.join();
    }
}
