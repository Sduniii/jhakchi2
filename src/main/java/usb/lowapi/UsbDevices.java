package usb.lowapi;

import org.usb4java.*;
import tools.Debug;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

public class UsbDevices {

    private static Boolean isConnected = false;
    private static Context context;

    public static void openConnection() {
        if (!isConnected) {
            context = new Context();
            int result = LibUsb.init(context);
            if (result != LibUsb.SUCCESS) throw new LibUsbException("Unable to initialize libusb.", result);
            isConnected = true;
        }
    }

    public static void closeConnection() {
        if (isConnected) {
            LibUsb.exit(context);
            isConnected = false;
        }
    }

    public static Device findDevice(short vendorId, short productId) {
        if (!isConnected) return null;
        // Read the USB device list
        DeviceList list = new DeviceList();
        int result = LibUsb.getDeviceList(context, list);
        if (result < 0) throw new LibUsbException("Unable to get device list", result);

        try {
            // Iterate over all devices and scan for the right one
            for (Device device : list) {
                DeviceDescriptor descriptor = new DeviceDescriptor();
                result = LibUsb.getDeviceDescriptor(device, descriptor);
                if (result != LibUsb.SUCCESS) throw new LibUsbException("Unable to read device descriptor", result);
                //System.out.println(descriptor.idProduct());
                if (descriptor.idVendor() == vendorId && descriptor.idProduct() == productId) return device;
            }
        } finally {
            // Ensure the allocated device list is freed
            LibUsb.freeDeviceList(list, true);
        }

        // Device not found
        return null;
    }

    public static int write(DeviceHandle handle, byte[] data, byte outEndpoint, int timeout) {
        if (isConnected) {
            ByteBuffer buffer = BufferUtils.allocateByteBuffer(data.length);
            buffer.put(data);
            IntBuffer transferred = BufferUtils.allocateIntBuffer();
            int result = LibUsb.bulkTransfer(handle, outEndpoint, buffer,
                    transferred, timeout);
            if (result != LibUsb.SUCCESS) {
                Debug.WriteLine("Unable to send data " + result);
                return -1;
            }
            int r = transferred.get();
            System.out.println(r + " bytes sent to device");
            return r;
        }
        return -1;
    }

    public static USBReadWrapper read(DeviceHandle handle, int size, byte inEndpoint, int timeout) {
        USBReadWrapper wrapper = new USBReadWrapper();
        wrapper.setResultCode(-1);
        if (isConnected) {
            ByteBuffer buffer = BufferUtils.allocateByteBuffer(size).order(ByteOrder.LITTLE_ENDIAN);
            IntBuffer transferred = BufferUtils.allocateIntBuffer();
            int result = LibUsb.bulkTransfer(handle, inEndpoint, buffer, transferred, timeout);
            wrapper.setResultCode(result);
            if (result != LibUsb.SUCCESS) {
                Debug.WriteLine("Unable t -1o read data " + result);
                return wrapper;
            }
            byte[] buff = new byte[buffer.slice().remaining()];
            buffer.get(buff);
            int len = 0;
            for (int i = 0; i < buff.length; i++) {
                if (buff.length >= i + 3 && buff[i + 1] == 0 && buff[i + 2] == 0)
                    break;
                len++;
            }
            byte[] r = new byte[len];
            System.arraycopy(buff, 0, r, 0, len);
            wrapper.setBuffer(r);
            wrapper.setTransferred(len);
            return wrapper;
        }
        return wrapper;
    }

    public static void listDevices() {
        if (!isConnected) return;
        // Read the USB device list
        DeviceList list = new DeviceList();
        int result = LibUsb.getDeviceList(context, list);
        if (result < 0) throw new LibUsbException("Unable to get device list", result);

        try {
            // Iterate over all devices and scan for the right one
            for (Device device : list) {
                DeviceDescriptor descriptor = new DeviceDescriptor();
                result = LibUsb.getDeviceDescriptor(device, descriptor);
                if (result != LibUsb.SUCCESS) throw new LibUsbException("Unable to read device descriptor", result);
                System.out.println(descriptor.toString());
            }
        } finally {
            // Ensure the allocated device list is freed
            LibUsb.freeDeviceList(list, true);
        }
    }

    /**
     * @param device
     * @param handle
     * @return int[] with 0:inEndp, 1:outEndp, 2:inMax, 3:outMax
     */
    public static byte[] checkDevice(Device device, DeviceHandle handle) {

        byte[] res = new byte[]{-1, -1, 0, 0};
        final DeviceDescriptor descriptor = new DeviceDescriptor();
        int result = LibUsb.getDeviceDescriptor(device, descriptor);
        if (result < 0) {
            Debug.WriteLine("Unable to read device descriptor " + result);
            return res;
        }

        for (byte i = 0; i < descriptor.bNumConfigurations(); i += 1) {
            final ConfigDescriptor cdescriptor = new ConfigDescriptor();
            final int cresult = LibUsb.getConfigDescriptor(device, i, cdescriptor);
            if (cresult < 0) {
                Debug.WriteLine("Unable to read config descriptor " + result);
                return res;
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
                                        res[0] = epd.bEndpointAddress();
                                        res[2] = (byte) epd.wMaxPacketSize();
                                    } else {
                                        res[1] = epd.bEndpointAddress();
                                        res[3] = (byte) epd.wMaxPacketSize();
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
        return res;
    }
}
