package clovershell;

import org.usb4java.DeviceHandle;
import tools.UsbDevices;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class EndpointReader {


    private List<DataReceivedListener> listener = new ArrayList<>();
    private byte endpoint;
    private int size;
    private DeviceHandle handle;
    private int timeout;
    private Thread t;

    public EndpointReader(byte endpoint, int size, DeviceHandle handle, int timeout){
        this.endpoint = endpoint;
        this.size = size;
        this.handle = handle;
        this.timeout = timeout;
    }

    public void addListener(DataReceivedListener listener){
        this.listener.add(listener);
    }

    public void fireEvent(ByteBuffer buffer){
        for (DataReceivedListener dataReceivedListener : listener) {
            dataReceivedListener.dataReceived(buffer);
        }
    }

    public ByteBuffer read(){
        return UsbDevices.read(handle,size,endpoint,timeout);
    }

    public void start(){
        t = new Thread(runner);
        t.setPriority(7);
        t.start();

    }

    public void dispose() throws InterruptedException {
        if(t!=null && t.isAlive()){
            t.join();
        }
    }

    private Runnable runner = () -> {
        while(true) {
            ByteBuffer bf = read();
            if (bf != null) {
                fireEvent(bf);
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };
}
