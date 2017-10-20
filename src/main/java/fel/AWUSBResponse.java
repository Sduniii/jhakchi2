package fel;

import lombok.Getter;
import lombok.Setter;

public class AWUSBResponse {

    @Getter @Setter
    private int tag;
    @Getter @Setter
    private int residue;
    @Getter @Setter
    private byte cswStatus;

    public AWUSBResponse() {
    }

    public AWUSBResponse(byte[] data) throws FelParseException {
        if (data[0] != 'A' || data[1] != 'W' || data[2] != 'U' || data[3] != 'S')
            throw new FelParseException();
        tag = (data[4] | (data[5] * 0x100) | (data[6] * 0x10000) | (data[7] * 0x1000000));
        residue = (data[8] | (data[9] * 0x100) | (data[10] * 0x10000) | (data[11] * 0x1000000));
        cswStatus = data[12];
    }

    public byte[] getData()
    {
        byte[] data = new byte[13];
        data[0] = (byte) 'A';
        data[1] = (byte) 'W';
        data[2] = (byte) 'U';
        data[3] = (byte) 'S';
        data[4] = (byte) (tag & 0xFF); // tag
        data[5] = (byte) ((tag >> 8) & 0xFF); // tag
        data[6] = (byte) ((tag >> 16) & 0xFF); // tag
        data[7] = (byte) ((tag >> 24) & 0xFF); // tag
        data[8] = (byte) (residue & 0xFF); // residue
        data[9] = (byte) ((residue >> 8) & 0xFF); // residue
        data[10] = (byte) ((residue >> 16) & 0xFF); // residue
        data[11] = (byte) ((residue >> 24) & 0xFF); // residue
        data[12] = cswStatus; // csw_status
        return data;
    }


}
