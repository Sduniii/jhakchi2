package clovershell;

import java.nio.ByteBuffer;

public interface DataReceivedListener {
    void dataReceived(ByteBuffer buffer);
}
