package clovershell;

import lombok.Getter;
import lombok.Setter;
import org.usb4java.DeviceHandle;
import tools.UsbDevices;

public class EndpointWriter {

    private byte endp;
    private DeviceHandle handle;
    private int timeout;
    @Getter
    @Setter
    boolean locked;

    public EndpointWriter(DeviceHandle handle, byte endp, int timeout) {
        this.handle = handle;
        this.endp = endp;
        this.timeout = timeout;
    }

    public synchronized int write(byte[] data) {
        int w = UsbDevices.write(handle, data, endp, timeout);
        return w;
    }
}
