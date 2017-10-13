package apps.helper;


import lombok.AccessLevel;
import lombok.Setter;
import tools.ArrayTool;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class SfromHeader1 implements Serializable {
    @Setter(AccessLevel.PRIVATE)
    private byte[] unknown1_0x00000100, fileSize, unknown2_0x00000030, romEnd, footerStart, header2, fileSize2, unknown3_0x00000000, flags;
    @Setter(AccessLevel.PRIVATE)
    private byte[] vcGameID;
    @Setter(AccessLevel.PRIVATE)
    private byte[] unknown4_0x00000000;


    public SfromHeader1(int romSize) {

        unknown1_0x00000100 = ByteBuffer.allocate(4).putInt(0x00000100).array();
        fileSize = ByteBuffer.allocate(4).putInt((48 + romSize + 38)).array();
        unknown2_0x00000030 = ByteBuffer.allocate(4).putInt(0x00000030).array();
        romEnd = ByteBuffer.allocate(4).putInt((48 + romSize)).array();
        footerStart = fileSize;
        header2 = romEnd;
        fileSize2 = fileSize;
        unknown3_0x00000000 = ByteBuffer.allocate(4).putInt(0).array();
        flags = ByteBuffer.allocate(4).putInt(ByteBuffer.wrap(fileSize).getInt() - 11).array();
        vcGameID = new byte[8];
        byte[] VCGameID_s = ("WUP-XXXX").getBytes(StandardCharsets.US_ASCII);
        System.arraycopy(VCGameID_s, 0, vcGameID, 0, VCGameID_s.length);
        unknown4_0x00000000 = ByteBuffer.allocate(4).putInt(0).array();
    }

    public SfromHeader1() {
        unknown1_0x00000100 = ByteBuffer.allocate(4).putInt(0x00000100).array();
        fileSize = ByteBuffer.allocate(4).putInt(0).array();
        unknown2_0x00000030 = ByteBuffer.allocate(4).putInt(0x00000030).array();
        romEnd = ByteBuffer.allocate(4).putInt(0).array();
        footerStart = fileSize;
        header2 = romEnd;
        fileSize2 = fileSize;
        unknown3_0x00000000 = ByteBuffer.allocate(4).putInt(0).array();
        flags = ByteBuffer.allocate(4).putInt(ByteBuffer.wrap(fileSize).getInt() - 11).array();
        vcGameID = new byte[8];
        byte[] VCGameID_s = ("WUP-XXXX").getBytes(StandardCharsets.US_ASCII);
        System.arraycopy(VCGameID_s, 0, vcGameID, 0, VCGameID_s.length);
        unknown4_0x00000000 = ByteBuffer.allocate(4).putInt(0).array();
    }

    public byte[] getBytes() {
        return ArrayTool.joinByteArray(unknown1_0x00000100, fileSize, unknown2_0x00000030, romEnd, footerStart, header2, fileSize2, unknown3_0x00000000, flags, vcGameID, unknown4_0x00000000);
    }

    public static SfromHeader1 read(byte[] buffer, int pos) throws InvocationTargetException, IllegalAccessException {
        SfromHeader1 sfromHeader1 = new SfromHeader1();

        System.arraycopy(buffer, pos, sfromHeader1.unknown1_0x00000100, 0, 4);
        System.arraycopy(buffer, pos + 4, sfromHeader1.fileSize, 0, 4);
        System.arraycopy(buffer, pos + 8, sfromHeader1.unknown2_0x00000030, 0, 4);
        System.arraycopy(buffer, pos + 12, sfromHeader1.romEnd, 0, 4);
        System.arraycopy(buffer, pos + 16, sfromHeader1.footerStart, 0, 4);
        System.arraycopy(buffer, pos + 20, sfromHeader1.header2, 0, 4);
        System.arraycopy(buffer, pos + 24, sfromHeader1.fileSize2, 0, 4);
        System.arraycopy(buffer, pos + 28, sfromHeader1.unknown3_0x00000000, 0, 4);
        System.arraycopy(buffer, pos + 32, sfromHeader1.flags, 0, 4);
        System.arraycopy(buffer, pos + 36, sfromHeader1.vcGameID, 0, 8);
        System.arraycopy(buffer, pos + 44, sfromHeader1.unknown4_0x00000000, 0, 4);

        return sfromHeader1;
    }

}
