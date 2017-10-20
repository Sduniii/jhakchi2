package usb.lowapi;

import lombok.Getter;
import lombok.Setter;

public class USBReadWrapper {
    @Getter
    @Setter
    private byte[] buffer;
    @Getter
    @Setter
    private int transferred;
    @Getter
    @Setter
    private int resultCode;

}
