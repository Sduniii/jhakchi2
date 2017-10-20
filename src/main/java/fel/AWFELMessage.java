package fel;

import lombok.Getter;
import lombok.Setter;

public class AWFELMessage {
    @Getter
    @Setter
    private AWFELStandardRequest.RequestType cmd;
    @Getter
    @Setter
    private short tag;
    @Getter
    @Setter
    private int address;
    @Getter
    @Setter
    private int len;
    @Getter
    @Setter
    private int flags;

    public AWFELMessage() {
    }

    public AWFELMessage(byte[] data) {
        cmd = AWFELStandardRequest.RequestType.get(data[0] | (data[1] * 0x100));
        tag = (short) (data[2] | (data[3] * 0x100));
        address = data[4] | (data[5] * 0x100) | (data[6] * 0x10000) | (data[7] * 0x1000000);
        len = data[8] | (data[9] * 0x100) | (data[10] * 0x10000) | (data[11] * 0x1000000);
        flags = data[12] | (data[13] * 0x100) | (data[14] * 0x10000) | (data[15] * 0x1000000);
    }

    public byte[] getData() {

        byte[] data = new byte[16];
        data[0] = (byte) ((short) cmd.getType() & 0xFF); // mark
        data[1] = (byte) (((short) cmd.getType() >> 8) & 0xFF); // mark
        data[2] = (byte) (tag & 0xFF); // tag
        data[3] = (byte) ((tag >> 8) & 0xFF); // tag
        data[4] = (byte) (address & 0xFF); // address
        data[5] = (byte) ((address >> 8) & 0xFF); // address
        data[6] = (byte) ((address >> 16) & 0xFF); // address
        data[7] = (byte) ((address >> 24) & 0xFF); // address
        data[8] = (byte) (len & 0xFF); // len
        data[9] = (byte) ((len >> 8) & 0xFF); // len
        data[10] = (byte) ((len >> 16) & 0xFF); // len
        data[11] = (byte) ((len >> 24) & 0xFF); // len
        data[12] = (byte) (flags & 0xFF); // flags
        data[13] = (byte) ((flags >> 8) & 0xFF); // flags
        data[14] = (byte) ((flags >> 16) & 0xFF); // flags
        data[15] = (byte) ((flags >> 24) & 0xFF); // flags

        return data;

    }
}
