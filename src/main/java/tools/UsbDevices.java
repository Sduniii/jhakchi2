package tools;

import org.usb4java.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

public class UsbDevices {

    private static Boolean isConnected = false;
    private static Context context;

    public static void openConnection(){
        if(!isConnected){
            context = new Context();
            int result = LibUsb.init(context);
            if (result != LibUsb.SUCCESS) throw new LibUsbException("Unable to initialize libusb.", result);
            isConnected = true;
        }
    }

    public static void closeConnection(){
        if(isConnected){
            LibUsb.exit(context);
            isConnected = false;
        }
    }

    public static Device findDevice(short vendorId, short productId) {
        if(!isConnected) return null;
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

    public static int write(DeviceHandle handle, byte[] data, byte outEndpoint, int timeout)
    {
        if(isConnected) {
            ByteBuffer buffer = BufferUtils.allocateByteBuffer(data.length);
            buffer.put(data);
            IntBuffer transferred = BufferUtils.allocateIntBuffer();
            int result = LibUsb.bulkTransfer(handle, outEndpoint, buffer,
                    transferred, timeout);
            if (result != LibUsb.SUCCESS) {
                Debug.WriteLine("Unable to send data " + result);
                return -1;
            }
            int r =  transferred.get();
            System.out.println(r + " bytes sent to device");
            return r;
        }
        return -1;
    }


    public static ByteBuffer read(DeviceHandle handle, int size, byte inEndpoint, int timeout)
    {
        if(isConnected) {
            ByteBuffer buffer = BufferUtils.allocateByteBuffer(size).order(ByteOrder.LITTLE_ENDIAN);
            IntBuffer transferred = BufferUtils.allocateIntBuffer();
            int result = LibUsb.bulkTransfer(handle, inEndpoint, buffer,
                    transferred, timeout);
            if (result != LibUsb.SUCCESS) {
                Debug.WriteLine("Unable to read data " + result);
                return null;
            }
            System.out.println(transferred.get() + " bytes read from device");
            return buffer;
        }
        return null;
    }

    public static  void listDevices() {
        if(!isConnected) return;
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
}
