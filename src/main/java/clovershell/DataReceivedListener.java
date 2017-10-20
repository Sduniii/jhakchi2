package clovershell;

import java.nio.ByteBuffer;

public interface DataReceivedListener {
    void dataReceived(byte[] buffer);
}
