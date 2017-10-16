package clovershell;

import org.usb4java.DeviceHandle;
import tools.UsbDevices;

public class EndpointWriter {

    private byte endp;
    private DeviceHandle handle;
    private int timeout;

    public EndpointWriter(DeviceHandle handle, byte endp, int timeout) {
        this.handle = handle;
        this.endp = endp;
        this.timeout = timeout;
    }

    public int write(byte[] data) {
        return UsbDevices.write(handle, data, endp, timeout);
    }
}
