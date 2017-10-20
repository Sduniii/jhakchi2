package fel;

import lombok.Getter;
import lombok.experimental.var;

public class AWFELVerifyDeviceResponse {
    @Getter
    private int board;
    private int fw;
    private short mode;
    private byte dataFlag;
    private byte dataLength;
    private int dataStartAddress;

    private AWFELVerifyDeviceResponse() {
    }

    AWFELVerifyDeviceResponse(byte[] data) throws FelParseException {
        if (data[0] != 'A' || data[1] != 'W' || data[2] != 'U' || data[3] != 'S' || data[4] != 'B' || data[5] != 'F' || data[6] != 'E' || data[7] != 'X')
            throw new FelParseException();
        board = (data[8] | (data[9] * 0x100) | (data[10] * 0x10000) | (data[11] * 0x1000000));
        fw = (data[12] | (data[13] * 0x100) | (data[14] * 0x10000) | (data[15] * 0x1000000));
        mode = (short) (data[16] | (data[17] * 0x100));
        dataFlag = data[18];
        dataLength = data[19];
        dataStartAddress = (int) (data[20] | (data[21] * 0x100) | (data[22] * 0x10000) | (data[23] * 0x1000000));
    }

    public byte[] getData() {
        byte[] data = new byte[32];
        data[0] = (byte) 'A';
        data[1] = (byte) 'W';
        data[2] = (byte) 'U';
        data[3] = (byte) 'S';
        data[4] = (byte) 'B';
        data[5] = (byte) 'F';
        data[6] = (byte) 'E';
        data[7] = (byte) 'X';
        data[8] = (byte) (board & 0xFF); // board
        data[9] = (byte) ((board >> 8) & 0xFF); // board
        data[10] = (byte) ((board >> 16) & 0xFF); // board
        data[11] = (byte) ((board >> 24) & 0xFF); // board
        data[12] = (byte) (fw & 0xFF); // fw
        data[13] = (byte) ((fw >> 8) & 0xFF); // fw
        data[14] = (byte) ((fw >> 16) & 0xFF); // fw
        data[15] = (byte) ((fw >> 24) & 0xFF); // fw
        data[16] = (byte) (mode & 0xFF); // mode
        data[17] = (byte) ((mode >> 8) & 0xFF); // mode
        data[18] = dataFlag;
        data[19] = dataLength;
        data[20] = (byte) (dataStartAddress & 0xFF); // data_start_address
        data[21] = (byte) ((dataStartAddress >> 8) & 0xFF); // data_start_address
        data[22] = (byte) ((dataStartAddress >> 16) & 0xFF); // data_start_address
        data[23] = (byte) ((dataStartAddress >> 24) & 0xFF); // data_start_address
        return data;
    }

}
