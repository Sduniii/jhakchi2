package tools;

import org.usb4java.*;

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
