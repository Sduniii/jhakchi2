package apps.header;

import apps.SnesGame;
import tools.ArrayTool;

import java.nio.ByteBuffer;

public class SfromHeader2 {

    private byte[] fps;

    private byte[] romSize, pcmSize, footerSize;
    private byte[] presetID;
    private byte[] mostly2;
    private byte[] volume;
    private byte[] romType;
    private byte[] chip, unknown1_0x00000000, unknown2_0x00000100, unknown3_0x00000100, unknown4_0x00000000;

    public SfromHeader2(int romSize, int presetId, SnesGame.SnesRomType romType, int chip) {
        fps = new byte[]{(byte) 60};
        this.romSize = ByteBuffer.allocate(4).putInt(romSize).array();
        pcmSize = ByteBuffer.allocate(4).putInt(0).array();
        footerSize = ByteBuffer.allocate(4).putInt(0).array();
        presetID = ByteBuffer.allocate(2).putShort((short) presetId).array();
        mostly2 = new byte[]{(byte) 2};
        volume = new byte[]{(byte) 100};
        this.romType = new byte[(byte) romType.getType()];
        this.chip = ByteBuffer.allocate(4).putInt(chip).array();
        unknown1_0x00000000 = ByteBuffer.allocate(4).putInt(0x00000000).array();
        unknown2_0x00000100 = ByteBuffer.allocate(4).putInt(0x00000100).array();
        unknown3_0x00000100 = ByteBuffer.allocate(4).putInt(0x00000100).array();
        unknown4_0x00000000 = ByteBuffer.allocate(4).putInt(0x00000000).array();
    }

    private SfromHeader2() {
        fps = new byte[1];
        romSize = ByteBuffer.allocate(4).putInt(0).array();
        pcmSize = ByteBuffer.allocate(4).putInt(0).array();
        footerSize = ByteBuffer.allocate(4).putInt(0).array();
        presetID = ByteBuffer.allocate(2).putShort((short) 0).array();
        mostly2 = new byte[1];
        volume = new byte[1];
        romType = new byte[1];
        chip = ByteBuffer.allocate(4).putInt(0).array();
        unknown1_0x00000000 = ByteBuffer.allocate(4).putInt(0x00000000).array();
        unknown2_0x00000100 = ByteBuffer.allocate(4).putInt(0x00000100).array();
        unknown3_0x00000100 = ByteBuffer.allocate(4).putInt(0x00000100).array();
        unknown4_0x00000000 = ByteBuffer.allocate(4).putInt(0x00000000).array();
    }

    public byte[] getBytes() {
        return ArrayTool.joinByteArray(fps, romSize, pcmSize, footerSize, presetID, mostly2, volume, romType, chip, unknown1_0x00000000, unknown2_0x00000100, unknown3_0x00000100, unknown4_0x00000000);
    }

    public static SfromHeader2 read(byte[] buffer, int pos) {
        SfromHeader2 sfromHeader2 = new SfromHeader2();

        System.arraycopy(buffer, pos, sfromHeader2.fps, 0, 1);
        System.arraycopy(buffer, pos + 1, sfromHeader2.romSize, 0, 4);
        System.arraycopy(buffer, pos + 5, sfromHeader2.pcmSize, 0, 4);
        System.arraycopy(buffer, pos + 9, sfromHeader2.footerSize, 0, 4);
        System.arraycopy(buffer, pos + 13, sfromHeader2.presetID, 0, 2);
        System.arraycopy(buffer, pos + 15, sfromHeader2.mostly2, 0, 1);
        System.arraycopy(buffer, pos + 16, sfromHeader2.volume, 0, 1);
        System.arraycopy(buffer, pos + 17, sfromHeader2.romType, 0, 1);
        System.arraycopy(buffer, pos + 18, sfromHeader2.chip, 0, 4);
        System.arraycopy(buffer, pos + 22, sfromHeader2.unknown1_0x00000000, 0, 4);
        System.arraycopy(buffer, pos + 26, sfromHeader2.unknown2_0x00000100, 0, 4);
        System.arraycopy(buffer, pos + 30, sfromHeader2.unknown3_0x00000100, 0, 4);
        System.arraycopy(buffer, pos + 34, sfromHeader2.unknown4_0x00000000, 0, 4);

        return sfromHeader2;
    }

}
