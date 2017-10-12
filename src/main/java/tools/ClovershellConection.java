package tools;

import org.usb4java.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

public class ClovershellConection {
    private final int vid = 0x1F3A;
    private final int pid = 0xEFE8;
    private boolean online = false;
    private boolean enabled = false;
    private Device device;
}
