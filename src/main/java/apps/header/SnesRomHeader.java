package apps.header;

import lombok.AccessLevel;
import lombok.Setter;
import org.apache.commons.lang3.ArrayUtils;
import tools.ArrayTool;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

        /*
        romMakeup
        $20	0010 0000	LoROM	1048576 bytes / 1 MB
        $21	0010 0001	HiROM	2097152 bytes / 2 MB
        $30	0011 0000	LoROM + FastROM	1572864 bytes / 1.5 MB
        $31	0011 0001	HiROM + FastROM	3145728 bytes / 3 MB
        $32	0011 0010	ExLoROM	6291456 bytes / 6 MB
        $35	0011 0101	ExHiROM	6291456 bytes / 6 MB#


ROM Type      :  1 Byte

  Hex   ROM Type
  ---------------------
   00   ROM
   01   ROM/RAM
   02   ROM/SRAM
   03   ROM/DSP1
   04   ROM/DSP1/RAM
   05   ROM/DSP1/SRAM
   06   FX

   * SRAM = Save Ram
   * DSP1 = Nintendo's 1st generation of DSP (Math coprocessor)
   * FX   = RISC based math coprocessor
            Only a couple of games support the FX Chip, Star Fox
            is the most well known one.


ROM Size      : 1 BYTE

  Hex   Size
  --------------
   08    2 Mbit
   09    4 Mbit
   0A    8 Mbit
   0B   16 Mbit
   0C   32 Mbit

  * As of this documentation 32MBit ROMs are the largest that
    Nintendo currently uses.  Rumors of a 40+ kart are around,
    but cannot be verified.

  * 8MBit ROMs are the most common in the entire library of
    SNES karts

  * ROMs are always multiples 2, thus 2MBit ROMs are the smallest
    Space Invaders (c) Taito is a 2MBit ROM (Japan only)

  * Easy way to calc rom size without a lookup table
  *
      0x400 << ROM_SIZE bytes //Check with romemakeup possible

      1 << (ROM_SIZE - 7) MBits

      ie.   8Mbit ROMs = 0Ah = 10d
            1 << (0A-7) = 8 Mbit

SRAM Size      : 1 BYTE

  Hex   Size
  --------------
   00   No SRAM
   01   16 Kbit
   02   32 Kbit
   03   64 Kbit

    * 64Kbit is the largest SRAM size that Nintendo currently uses.
    * 256Kbit is standard for most copiers.

    * Easy way to calc SRAM Size without a lookup table

        1 << (3+SRAM_BYTE) Kbits

        ie. 16Kbit = 01
            1 << (3+1) = 16


COUNTRY CODE  : 1 BYTE

  Hex    Country                 Video Mode
  ------------------------------------------
   00    Japan                   (NTSC)
   01    USA                     (NTSC)
   02    Europe, Oceania, Asia    (PAL)
   03    Sweden                   (PAL)
   04    Finland                  (PAL)
   05    Denmark                  (PAL)
   06    France                   (PAL)
   07    Holland                  (PAL)
   08    Spain                    (PAL)
   09    Germany, Austria, Switz  (PAL)
   10    Italy                    (PAL)
   11    Hong Kong, China         (PAL)
   12    Indonesia                (PAL)
   13    Korea                    (PAL)

  * Country Codes are from SU.INI, could someone verify these?

LICENSE       : 1 BYTE
     0 <Invalid License Code>
     1 Nintendo
     5 Zamuse
     8 Capcom
     9 HOT B
    10 Jaleco
    11 STORM (Sales Curve) (1)
    15 Mebio Software
    18 Gremlin Graphics
    21 COBRA Team
    22 Human/Field
    24 Hudson Soft
    26 Yanoman
    28 Tecmo (1)
    30 Forum
    31 Park Place Productions / VIRGIN
    33 Tokai Engeneering (SUNSOFT?)
    34 POW
    35 Loriciel / Micro World
    38 Enix
    40 Kemco (1)
    41 Seta Co.,Ltd.
    45 Visit Co.,Ltd.
    53 HECT
    61 Loriciel
    64 Seika Corp.
    65 UBI Soft
    71 Spectrum Holobyte
    73 Irem
    75 Raya Systems/Sculptured Software
    76 Renovation Pruducts
    77 Malibu Games (T*HQ Inc.) / Black Pearl
    79 U.S. Gold
    80 Absolute Entertainment
    81 Acclaim
    82 Activision
    83 American Sammy
    84 GameTek
    85 Hi Tech
    86 LJN Toys
    90 Mindscape
    93 Technos Japan Corp. (Tradewest)
    95 American Softworks Corp.
    96 Titus
    97 Virgin Games
    98 Maxis
   103 Ocean
   105 Electronic Arts
   107 Laser Beam
   110 Elite
   111 Electro Brain
   112 Infogrames
   113 Interplay
   114 LucasArts
   115 Sculptured Soft
   117 STORM (Sales Curve) (2)
   120 THQ Software
   121 Accolade Inc.
   122 Triffix Entertainment
   124 Microprose
   127 Kemco (2)
   130 Namcot/Namco Ltd. (1)
   132 Koei/Koei! (second license?)
   134 Tokuma Shoten Intermedia
   136 DATAM-Polystar
   139 Bullet-Proof Software
   140 Vic Tokai
   143 I'Max
   145 CHUN Soft
   146 Video System Co., Ltd.
   147 BEC
   151 Kaneco
   153 Pack in Video
   154 Nichibutsu
   155 TECMO (2)
   156 Imagineer Co.
   160 Wolf Team
   164 Konami
   165 K.Amusement
   167 Takara
   169 Technos Jap. ????
   170 JVC
   172 Toei Animation
   173 Toho
   175 Namcot/Namco Ltd. (2)
   177 ASCII Co. Activison
   178 BanDai America
   180 Enix
   182 Halken
   186 Culture Brain
   187 Sunsoft
   188 Toshiba EMI/System Vision
   189 Sony (Japan) / Imagesoft
   191 Sammy
   192 Taito
   194 Kemco (3) ????
   195 Square
   196 NHK
   197 Data East
   198 Tonkin House
   200 KOEI
   202 Konami USA
   205 Meldac/KAZe
   206 PONY CANYON
   207 Sotsu Agency
   209 Sofel
   210 Quest Corp.
   211 Sigma
   214 Naxat
   216 Capcom Co., Ltd. (2)
   217 Banpresto
   219 Hiro
   221 NCS
   222 Human Entertainment
   223 Ringler Studios
   224 K.K. DCE / Jaleco
   226 Sotsu Agency
   228 T&ESoft
   229 EPOCH Co.,Ltd.
   231 Athena
   232 Asmik
   233 Natsume
   234 King/A Wave
   235 Atlus
   236 Sony Music
   238 Psygnosis / igs
   243 Beam Software
   244 Tec Magik
   255 Hudson Soft

  * License Codes are from SU.INI, could someone verify these?

  * I believe the # of licenses is low.  Is it possible that
    License and Country codes are used in conjuction to produce
    that many more licenses?

VERSION - 1 byte

  * The Version is interpeted this way.
    1.?? - (thanks to yoshi for the correction)

CHECKSUM COMPLEMENT - 2 bytes the complement of the checksum :>

  The bits are reversed of the CHECKSUM

CHECKSUM            - 2 bytes Checksum of the bin

  * Anyone know how the checksum is calculated for the ROM?

NMI/VBL Vector     - 2 bytes                            - OFFSET 0x81FA  (lowrom)
                                                          OFFSET 0x101FA (hirom)

RESET Vector       - 2 bytes where to start our code at - OFFSET 0x81FC  (lowrom)
                                                        - OFFSET 0x101FA (hirom)
  * 0x8000 is common for Low Roms

         */

public class SnesRomHeader {


    private static final short headerSize = 32;

    @Setter(AccessLevel.PRIVATE)
    private byte[] title;

    public byte getRomMakeup() {
        return romMakeup[0];
    }

    public byte getRomType() {
        return romType[0];
    }

    public byte getRomSize() {
        return romSize[0];
    }

    public byte getSramSize() {
        return sramSize[0];
    }

    public byte getCountry() {
        return country[0];
    }

    public byte getLicense() {
        return license[0];
    }

    public byte getVersion() {
        return version[0];
    }

    public int getChecksumComplement() {
        return ByteBuffer.wrap(checksumComplement).getShort();
    }

    public int getChecksum() {
        return ByteBuffer.wrap(checksum).getShort();
    }

    @Setter(AccessLevel.PRIVATE)
    private byte[] romMakeup, romType, romSize, sramSize, country, license, version,checksumComplement,checksum;

    public static SnesRomHeader read(byte[] rawRomData, int pos) {
        SnesRomHeader romheader = new SnesRomHeader();

        byte[] result = new byte[headerSize];
        System.arraycopy(rawRomData, pos, result, 0, headerSize);
        romheader.setTitle(new byte[]{
                result[0], result[1], result[2], result[3],
                result[4], result[5], result[6], result[7],
                result[8], result[9], result[10], result[11],
                result[12], result[13], result[14], result[15],
                result[16], result[17], result[18], result[19],
                result[20]
        });
        romheader.setRomMakeup(new byte[]{result[21]});
        romheader.setRomType(new byte[]{result[22]});
        romheader.setRomSize(new byte[]{result[23]});
        romheader.setSramSize(new byte[]{result[24]});
        romheader.setCountry(new byte[]{result[25]});
        romheader.setLicense(new byte[]{result[26]});
        romheader.setVersion(new byte[]{result[27]});
        romheader.setChecksumComplement(new byte[]{result[28],result[29]});
        romheader.setChecksum(new byte[]{result[30],result[31]});
        return romheader;
    }

    private SnesRomHeader(){
        title = new byte[0];
        romMakeup = new byte[0];
        romType = new byte[0];
        romSize = new byte[0];
        sramSize = new byte[0];
        country = new byte[0];
        license = new byte[0];
        version = new byte[0];
        checksumComplement = new byte[0];
        checksum = new byte[0];
    }


    public String getTitle() throws Exception {
        if(title.length == 0) throw new Exception("No Title Array: Use SnesRomHeader.read() to create a Header");
        List<Byte> data = new ArrayList<>();
        for(byte b : title){
            data.add(b);
        }
        if(data.contains((byte)0))
            return "";
        if(data.contains((byte)0xff))
            return "";
        if(data.get(0) == 0x20)
            return "";
        while (data.size() > 0 && data.get(data.size()-1) == (byte)0x20)
            data.remove(data.size()-1);

        Byte[] bytes = data.toArray(new Byte[data.size()]);
        return new String(ArrayUtils.toPrimitive(bytes));
    }

    public byte[] getBytes(){
        return ArrayTool.joinByteArray(title,romMakeup,romType,romSize,sramSize,country,license,version,checksumComplement,checksum);
    }


}
