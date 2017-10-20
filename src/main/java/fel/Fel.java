package fel;

import clovershell.EndpointReader;
import clovershell.EndpointWriter;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.ArrayUtils;
import org.usb4java.Device;
import org.usb4java.DeviceHandle;
import org.usb4java.LibUsb;
import tools.Debug;
import usb.lowapi.USBReadWrapper;
import usb.lowapi.UsbDevices;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Fel {
    @Getter
    @Setter
    private byte[] fes1Bin;
    @Getter
    private byte[] uBootBin;

    public enum CurrentAction {RunningCommand, ReadingMemory, WritingMemory}

    private Device device;
    private EndpointReader epReader;
    private EndpointWriter epWriter;
    private final int readTimeout = 1000;
    private final int writeTimeout = 1000;
    private final int maxBulkSize = 0x10000;
    private short vid, pid;
    private boolean dramInitDone = false;

    private int cmdOffset = -1;
    private final int fes1_base_m = 0x2000;
    private final int dram_base = 0x40000000;
    private final int uboot_base_m = dram_base + 0x7000000;
    private final int uboot_base_f = 0x100000;
    private final int sector_size = 0x20000;
    private final int uboot_maxsize_f = (sector_size * 0x10);
    private final int kernel_base_f = sector_size * 0x30;
    private final int kernel_max_size = sector_size * 0x20;
    private final int transfer_base_m = dram_base + 0x7400000;
    private final int transfer_max_size = sector_size * 0x100;
    private final String fastboot = "efex_test";

    public void setuBootBin(byte[] value) {
        uBootBin = value;
        String prefix = "bootcmd=";
        for (int i = 0; i < uBootBin.length - prefix.length(); i++) {
            if (new String(uBootBin, i, prefix.length()).equals(prefix)) {
                cmdOffset = i + prefix.length();
                break;
            }
        }
    }

    public static boolean deviceExists(short vid, short pid) throws InterruptedException {
        Fel fel = new Fel();
        try {
            if (fel.open(vid, pid)) {
                Debug.WriteLine("Device detection successful");
                return true;
            } else {
                return false;
            }
        } finally {
            fel.close();
        }
    }

    public boolean open(short vid, short pid) {
        try {
            UsbDevices.openConnection();
            this.vid = vid;
            this.pid = pid;
            close();
            //Debug.WriteLine("Trying to open device...");
            Device devices = UsbDevices.findDevice(vid, pid);

            if (device == null) {
                Debug.WriteLine("Device with such VID and PID not found");

                return false;
            }

            DeviceHandle handle = new DeviceHandle();
            int fresult = LibUsb.open(device, handle);
            if (fresult != LibUsb.SUCCESS) {
                Debug.WriteLine("Unable to open USB device " + fresult);
                return false;
            }

            byte inEndp = -1;
            byte outEndp = -1;
            byte inMax = 0;
            byte outMax = 0;
            byte[] res = UsbDevices.checkDevice(device, handle);
            if (inEndp != (byte) 0x82 || outEndp != (byte) 0x01) {
                Debug.WriteLine("Uncorrect FEL device/mode");
                return false;
            }

            epReader = new EndpointReader(inEndp, 65536, handle, readTimeout);
            epWriter = new EndpointWriter(handle, outEndp, writeTimeout);

            Debug.WriteLine("Trying to verify device");
            if (verifyDevice().getBoard() != 0x00166700) {
                Debug.WriteLine("Invalid board ID: " + verifyDevice().getBoard());
                return false;
            }
            return true;
        } catch (Exception ex) {
            Debug.WriteLine("Error: " + ex.getMessage() + ex.getStackTrace());
            return false;
        }
    }

    public void close() throws InterruptedException {
        device = null;
        if (epReader != null)
            epReader.dispose();
        epReader = null;

        epWriter = null;
    }

    private AWFELVerifyDeviceResponse verifyDevice() throws Exception {
        felRequest(AWFELStandardRequest.RequestType.FEL_VERIFY_DEVICE);
        byte[] resp;
        resp = felRead(32);
        AWFELStatusResponse status = new AWFELStatusResponse(felRead(8));
        return new AWFELVerifyDeviceResponse(resp);

    }

    private byte[] felRead(int length) throws Exception {
        AWUSBRequest req = new AWUSBRequest();
        req.setCmd(AWUSBRequest.RequestType.AW_USB_READ);
        req.setLen(length);
        writeToUsb(req.getData());

        byte[] result = readFromUSB(length);
        AWUSBResponse resp = new AWUSBResponse(readFromUSB(13));
        if (resp.getCswStatus() != 0) throw new FelException("FEL write error");
        return result;
    }


    private void felWrite(byte[] buffer) throws Exception {
        AWUSBRequest req = new AWUSBRequest();
        req.setCmd(AWUSBRequest.RequestType.AW_USB_WRITE);
        req.setLen(buffer.length);
        writeToUsb(req.getData());
        writeToUsb(buffer);
        AWUSBResponse resp = new AWUSBResponse(readFromUSB(13));
        if (resp.getCswStatus() != 0) throw new FelException("FEL write error");
    }

    private void felRequest(AWFELStandardRequest.RequestType command) throws Exception {
        AWFELStandardRequest req = new AWFELStandardRequest();
        req.setCmd(command);
        felWrite(req.getData());
    }

    private void felRequest(AWFELStandardRequest.RequestType command, int address, int length) throws Exception {
        AWFELMessage req = new AWFELMessage();
        req.setCmd(command);
        req.setAddress(address);
        req.setLen(length);
        felWrite(req.getData());
    }

    private void writeToUsb(byte[] buffer) throws Exception {
        Debug.WriteLine(String.format("-> %d bytes", buffer.length));
        int pos = 0;
        int l;
        while (pos < buffer.length) {
            l = epWriter.write(buffer, pos, buffer.length - pos);
            if (l > 0) {
                pos += l;
            } else throw new Exception("Can't write to USB");
        }
    }

    private byte[] readFromUSB(int length) throws Exception {
        byte[] result = new byte[length];
        int pos = 0;
        while (pos < length) {
            USBReadWrapper wrapper = readFromUSB(pos, length - pos);
            for (int i = pos; i < pos + wrapper.getTransferred(); i++) {
                result[i] = wrapper.getBuffer()[i - pos];
            }
            pos += wrapper.getTransferred();
        }
        return result;
    }

    private USBReadWrapper readFromUSB(int offset, int length) throws Exception {
        USBReadWrapper result = epReader.read(offset, length);
        if (result.getTransferred() != LibUsb.SUCCESS)
            throw new Exception("USB read error: " + result);
        Debug.WriteLine(String.format("<- %d bytes", length));
        return result;
    }

    public void writeMemory(int address, byte[] buffer) throws Exception {
        writeMemory(address,buffer,null);
    }
    public void writeMemory(int address, byte[] buffer, FelCallback callback) throws Exception {
        if (address >= dram_base)
            initDram();

        int length = buffer.length;
        if (length != (length & ~((int) 3))) {
            length = (length + 3) & ~((int) 3);
            byte[] newBuffer = new byte[length];
            System.arraycopy(buffer, 0, newBuffer, 0, buffer.length);
            buffer = newBuffer;
        }

        int pos = 0;
        while (pos < buffer.length) {
            if(callback != null) callback.onFelProgress(CurrentAction.WritingMemory, null);
            byte[] buf = new byte[Math.min(buffer.length - pos, maxBulkSize)];
            System.arraycopy(buffer, pos, buf, 0, buf.length);
            felRequest(AWFELStandardRequest.RequestType.FEL_DOWNLOAD, (int) (address + pos), buf.length);
            felWrite(buf);
            AWFELStatusResponse status = new AWFELStatusResponse(felRead(8));
            if (status.getState() != 0) throw new FelException("FEL write error");
            pos += buf.length;
        }
    }
    private byte[] readMemory(int address, int length) throws Exception {
          return readMemory(address,length,null);
    }

    private byte[] readMemory(int address, int length, FelCallback callback) throws Exception {
        if (address >= dram_base)
            initDram();

        length = (length + 3) & ~((int) 3);

        List<Byte> result = new ArrayList<>();
        while (length > 0) {
            if(callback != null) callback.onFelProgress(CurrentAction.ReadingMemory, null);
            int l = Math.min(length, maxBulkSize);
            felRequest(AWFELStandardRequest.RequestType.FEL_UPLOAD, address, l);
            byte[] r = felRead(l);
            for (byte b : r)
                result.add(b);
            AWFELStatusResponse status = new AWFELStatusResponse(felRead(8));
            if (status.getState() != 0) throw new FelException("FEL read error");
            length -= l;
            address += l;
        }
        Byte[] bytes = result.toArray(new Byte[result.size()]);
        return ArrayUtils.toPrimitive(bytes);
    }

    public boolean initDram() throws Exception {
        return initDram(false);
    }

    public boolean initDram(boolean force) throws Exception {
        if (dramInitDone && !force) return true;
        if (dramInitDone) return true;
            final int testSize = 0x80;
        if (fes1Bin == null || fes1Bin.length < testSize)
            throw new FelException("Can't init DRAM, incorrect Fes1 binary");
        byte[] buf = readMemory(fes1_base_m + fes1Bin.length - testSize, testSize);
        byte[] buf2 = new byte[testSize];
        System.arraycopy(fes1Bin, fes1Bin.length - buf.length, buf2, 0, testSize);
        if (Arrays.equals(buf,buf2)) {
            return dramInitDone = true;
        }
        writeMemory(fes1_base_m, fes1Bin);
        exec(fes1_base_m);
        Thread.sleep(2000);
        return dramInitDone = true;
    }

    public byte[] readFlash(int address, int length) throws Exception {
        return readFlash(address,length,null);
    }

    public byte[] readFlash(int address, int length, FelCallback callback) throws Exception {
        List<Byte> result = new ArrayList<>();
        String command;
        if ((address % sector_size) != 0)
            throw new FelException(String.format("Invalid flash address : 0x%dX8", address));
        if ((length % sector_size) != 0)
            throw new FelException(String.format("Invalid flash length: 0x%dX8", length));
        while (length > 0) {
            int reqLen = Math.min(length, transfer_max_size);
            command = String.format("sunxi_flash phy_read %dx %dx %dx;%s", transfer_base_m, address / sector_size, (int) Math.floor((double) reqLen / (double) sector_size), fastboot);
            runUbootCmd(command, false, callback);
            byte[] buf = readMemory(transfer_base_m + address % sector_size, reqLen, callback);
            for(byte b:buf)
            result.add(b);
            address += buf.length;
            length -= buf.length;
        }
        Byte[] bytes = result.toArray(new Byte[result.size()]);
        return ArrayUtils.toPrimitive(bytes);
    }

    public void writeFlash(int address, byte[] buffer) throws Exception {
        writeFlash(address,buffer,null);
    }

    public void writeFlash(int address, byte[] buffer, FelCallback callback) throws Exception {
        int length = buffer.length;
        int pos = 0;
        if ((address % sector_size) != 0)
            throw new FelException(String.format("Invalid flash address : 0x%dX8", address));
        if ((length % sector_size) != 0)
            throw new FelException(String.format("Invalid flash length: 0x%dX8", length));
        while (length > 0) {
            int wrLength = Math.min(length, transfer_max_size / 8);
            byte[] newBuf = new byte[wrLength];
            System.arraycopy(buffer, pos, newBuf, 0, wrLength);
            writeMemory(transfer_base_m, newBuf, callback);
            String command = String.format("sunxi_flash phy_write %dx %dx %dx;%s", transfer_base_m, address / sector_size, (int) Math.floor((double) wrLength / (double) sector_size), fastboot);
            runUbootCmd(command, false, callback);
            pos += wrLength;
            address += wrLength;
            length -= wrLength;
        }
    }

    public void exec(int address) throws Exception {
        felRequest(AWFELStandardRequest.RequestType.FEL_RUN, address, 0);
        AWFELStatusResponse status = new AWFELStatusResponse(felRead(8));
        if (status.getState() != 0) throw new FelException("FEL run error");
    }

    public void runUbootCmd(String command) throws Exception {
        runUbootCmd(command,false,null);
    }

    public void runUbootCmd(String command, boolean noreturn, FelCallback callback) throws Exception {
        if(callback != null) callback.onFelProgress(CurrentAction.RunningCommand, command);
        if (cmdOffset < 0) throw new Exception("Invalid Unoot binary, command variable not found");
            final int testSize = 0x20;
        if (uBootBin == null || uBootBin.length < testSize)
            throw new FelException("Can't init Uboot, incorrect Uboot binary");
        byte[] buf =readMemory(uboot_base_m, testSize);
        byte[] buf2 = new byte[testSize];
        System.arraycopy(uBootBin, 0, buf2, 0, testSize);
        if (!Arrays.equals(buf,buf2))
            writeMemory(uboot_base_m, uBootBin);
       byte[] cmdBuff = (command + "\0").getBytes(Charset.forName("ASCII"));
        writeMemory(uboot_base_m + cmdOffset, cmdBuff);
        exec(uboot_base_m);
        if (noreturn) return;
        close();
        for (int i = 0; i < 10; i++) {
            Thread.sleep(500);
            if(callback != null)callback.onFelProgress(CurrentAction.RunningCommand, command);
        }
        int errorCount = 0;
        while (true) {
            if (!open(vid, pid)) {
                errorCount++;
                if (errorCount >= 10) {
                    close();
                    throw new Exception("No answer from device");
                }
                Thread.sleep(2000);
            } else break;
        }
    }

    public void Dispose() throws InterruptedException {
        close();
    }

    private class FelCallback {
        public void onFelProgress(CurrentAction action, String command) {
        }
    }
}
