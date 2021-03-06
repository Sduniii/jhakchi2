package fel;

import lombok.Getter;
import lombok.Setter;

public class AWUSBRequest {
    public enum RequestType {
        AW_USB_READ(0x11), AW_USB_WRITE(0x12);

        @Getter
        private int type;

        RequestType(int type) {
            this.type = type;
        }

        public static RequestType get(int b) {
            for (RequestType type : RequestType.values()) {
                if (type.type == b) {
                    return type;
                }
            }
            return null;
        }
    }

    @Getter @Setter
    private int tag = 0;
    @Getter @Setter
    private int len;
    @Getter @Setter
    private RequestType cmd;
    @Getter @Setter
    private byte cmdlen = 0x0C;

    public AWUSBRequest() {
    }

    public AWUSBRequest(byte[] data) throws FelParseException {
        if (data[0] != 'A' || data[1] != 'W' || data[2] != 'U' || data[3] != 'C')
            throw new FelParseException();
        tag = (int) (data[4] | (data[5] * 0x100) | (data[6] * 0x10000) | (data[7] * 0x1000000));
        len = (int) (data[8] | (data[9] * 0x100) | (data[10] * 0x10000) | (data[11] * 0x1000000));
        cmdlen = data[15];
        cmd = RequestType.get(data[16]);
    }

    public byte[] getData() {
        byte[] data = new byte[32];
        data[0] = (byte) 'A';
        data[1] = (byte) 'W';
        data[2] = (byte) 'U';
        data[3] = (byte) 'C';
        data[4] = (byte) (tag & 0xFF); // tag
        data[5] = (byte) ((tag >> 8) & 0xFF); // tag
        data[6] = (byte) ((tag >> 16) & 0xFF); // tag
        data[7] = (byte) ((tag >> 24) & 0xFF); // tag
        data[8] = (byte) (len & 0xFF); // len
        data[9] = (byte) ((len >> 8) & 0xFF); // len
        data[10] = (byte) ((len >> 16) & 0xFF); // len
        data[11] = (byte) ((len >> 24) & 0xFF); // len
        data[12] = data[13] = 0; // reserved1
        data[14] = 0; // reserved2
        data[15] = cmdlen; // cmd_len
        data[16] = (byte) cmd.getType();
        data[17] = 0; // reserved3

        data[18] = (byte) (len & 0xFF); // len
        data[19] = (byte) ((len >> 8) & 0xFF); // len
        data[20] = (byte) ((len >> 16) & 0xFF); // len
        data[21] = (byte) ((len >> 24) & 0xFF); // len

        return data;

    }
}
