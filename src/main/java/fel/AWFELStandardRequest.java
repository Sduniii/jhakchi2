package fel;

import lombok.Getter;
import lombok.Setter;

public class AWFELStandardRequest {

    public enum RequestType {
        FEL_VERIFY_DEVICE(0x1), // (Read length 32 => AWFELVerifyDeviceResponse)
        FEL_SWITCH_ROLE(0x2),
        FEL_IS_READY(0x3),// (Read length 8)
        FEL_GET_CMD_SET_VER(0x4),
        FEL_DISCONNECT(0x10),
        FEL_DOWNLOAD(0x101), // (Write data to the device)
        FEL_RUN(0x102), // (Execute code)
        FEL_UPLOAD(0x103); // (Read data from the device)
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
    private RequestType cmd;
    @Getter @Setter
    private short tag;

    public AWFELStandardRequest() {
    }

    public AWFELStandardRequest(byte[] data) {
        cmd = RequestType.get(data[0] | (data[1] * 0x100));
        tag = (short) (data[2] | (data[3] * 0x100));
    }

    public byte[] getData() {

        byte[] data = new byte[16];
        data[0] = (byte) ((short) cmd.getType() & 0xFF); // mark
        data[1] = (byte) (((short) cmd.getType() >> 8) & 0xFF); // mark
        data[2] = (byte) (tag & 0xFF); // tag
        data[3] = (byte) ((tag >> 8) & 0xFF); // tag
        return data;

    }

}
