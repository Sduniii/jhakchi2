package fel;

import lombok.Getter;
import lombok.Setter;

public class AWFELStatusResponse {
    @Getter @Setter
    private short mark = (short) 0xFFFF;
    @Getter @Setter
    private short tag = 0;
    @Getter @Setter
    private byte state;

    public AWFELStatusResponse() {
    }

    public AWFELStatusResponse(byte[] data) {
        mark = (short) (data[0] | (data[1] * 0x100));
        tag = (short) (data[2] | (data[3] * 0x100));
        state = data[4];
    }

    public byte[] getData() {

        byte[] data = new byte[8];
        data[0] = (byte) (mark & 0xFF); // mark
        data[1] = (byte) ((mark >> 8) & 0xFF); // mark
        data[2] = (byte) (tag & 0xFF); // tag
        data[3] = (byte) ((tag >> 8) & 0xFF); // tag
        data[4] = state;
        return data;

    }
}
