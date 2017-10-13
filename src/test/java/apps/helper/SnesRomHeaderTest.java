package apps.helper;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class SnesRomHeaderTest {

    @Test
    void createTest() throws Exception {
        byte[] rawData = Files.readAllBytes(Paths.get("./roms/snes/ActRaiser2.smc"));
        byte[] headerbytes = new byte[32];
        System.arraycopy(rawData, 0xffc0, headerbytes, 0, 32);

        SnesRomHeader header = SnesRomHeader.read(rawData, 0x7fc0);
        SnesRomHeader header2 = SnesRomHeader.read(rawData, 0xffc0);

        assertNotEquals("ActRaiser-2 USA", header.getTitle());
        assertEquals("ActRaiser-2 USA", header2.getTitle());
        assertArrayEquals(headerbytes, header2.getBytes());

        rawData = Files.readAllBytes(Paths.get("./roms/snes/HarvestMoon.smc"));
        headerbytes = new byte[32];
        System.arraycopy(rawData, 0x7fc0, headerbytes, 0, 32);

        header = SnesRomHeader.read(rawData, 0x7fc0);
        header2 = SnesRomHeader.read(rawData, 0xffc0);

        assertNotEquals("HARVEST MOON", header2.getTitle());
        assertEquals("HARVEST MOON", header.getTitle());
        assertArrayEquals(headerbytes, header.getBytes());
    }

}