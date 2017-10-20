package clovershell;

import lombok.Getter;
import lombok.Setter;
import org.usb4java.DeviceHandle;
import usb.lowapi.UsbDevices;

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

    public synchronized int write(byte[] buffer, int pos, int length) {
        byte[] bytes = new byte[length];
        System.arraycopy(buffer,pos,bytes,0,length);
        return write(bytes);
    }
}
