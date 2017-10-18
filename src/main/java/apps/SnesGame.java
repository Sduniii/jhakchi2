package apps;

import Properties.Resources;
import apps.header.SfromHeader1;
import apps.header.SfromHeader2;
import apps.header.SnesRomHeader;
import apps.wrapper.CachedGameInfo;
import apps.wrapper.ParameterWrapper;
import apps.wrapper.SnesRomHeaderWrapper;
import config.ConfigIni;
import enums.ConsoleType;
import gui.controls.CustomButtonType;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import lombok.Getter;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import tools.Debug;
import tools.FileTool;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SnesGame extends MiniApplication implements ICloverAutofill {


    public SnesGame(Path path) throws IOException {
        super(path);
    }

    public SnesGame(Path path, Boolean ignoreEmptyConfig) throws IOException {
        super(path, ignoreEmptyConfig);
    }

    @Override
    public Boolean tryAutofill(long crc32) {
        return null;
    }

    public enum SnesRomType {
        loRom(0x14), hiRom(0x15), no(0x00);
        @Getter
        private int type;

        SnesRomType(int type) {
            this.type = type;
        }

        public static SnesRomType get(int id) {
            SnesRomType[] types = SnesRomType.values();
            for (SnesRomType type1 : types) {
                if (type1.type == id)
                    return type1;
            }
            return no;
        }
    }

    static final String DEFAULT_CANOEA_RGS = "--volume 100 -rollback-snapshot-period 600";
    static Boolean need3rdPartyEmulator;
    static List<Byte> sfxTypes = new ArrayList<Byte>() {
        {
            add((byte) 0x13);
            add((byte) 0x14);
            add((byte) 0x15);
            add((byte) 0x1a);
        }
    };
    static List<Byte> dsp1Types = new ArrayList<Byte>() {
        {
            add((byte) 0x03);
            add((byte) 0x05);
        }
    };
    static List<Byte> sA1Types = new ArrayList<Byte>() {
        {
            add((byte) 0x34);
            add((byte) 0x35);
        }
    };
    // Known presets
    static Map<String, Integer> knownPresets = new HashMap<String, Integer>() {{
        put("SUPER MARIOWORLD", 0x1011);
        put("F-ZERO", 0x1018);
        //{ "THE LEGEND OF ZELDA", 0x101D }, // Removed to use hacks and translations
        put("SUPER MARIO KART", 0x10BD);
        put("Super Metroid", 0x1040);
        put("EARTH BOUND", 0x1070);
        put("Kirby's Dream Course", 0x1058);
        put("DONKEY KONG COUNTRY", 0x1077);
        put("KIRBY SUPER DELUXE", 0x109F);
        put("Super Punch-Out!!", 0x10A9);
        put("MEGAMAN X", 0x1109);
        put("SUPER GHOULS'N GHOST", 0x1003);
        put("Street Fighter2 Turb", 0x1065);
        put("SUPER MARIO RPG", 0x109E);
        put("Secret of MANA", 0x10B0);
        put("FINAL FANTASY 3", 0x10DC);
        put("SUPER CASTLEVANIA 4", 0x1030);
        put("CONTRA3 THE ALIEN WA", 0x1036);
        put("STAR FOX", 0x1242);
        put("YOSHI'S ISLAND", 0x123D);
        put("STARFOX2", 0x123C);
        //{ "ZELDANODENSETSU", 0x101F }, // Removed to use hacks and translations
        put("SHVC FIREEMBLEM", 0x102B);
        put("SUPER DONKEY KONG", 0x1023);
        //{ "Super Street Fighter", 0x1056 }, // Invalid
        put("ROCKMAN X", 0x110A);
        put("CHOHMAKAIMURA", 0x1004);
        put("SeikenDensetsu 2", 0x10B2);
        put("FINAL FANTASY 6", 0x10DD);
        put("CONTRA SPIRITS", 0x1037);
        put("ganbare goemon", 0x1048);
        put("SUPER FORMATION SOCC", 0x1240);
        put("YOSSY'S ISLAND", 0x1243);
        put("FINAL FIGHT", 0x100E);
        put("DIDDY'S KONG QUEST", 0x105D);
        //{ "KIRBY'S DREAM LAND 3", 0x10A2 }, // Reported as problematic, using ID from Mario RPG
        put("BREATH OF FIRE 2", 0x1068);
        put("FINAL FIGHT 2", 0x10E1);
        put("MEGAMAN X2", 0x1117);
        put("FINAL FIGHT 3", 0x10E3);
        put("GENGHIS KHAN 2", 0x10C4);
        put("CASTLEVANIA DRACULA", 0x1131);
        put("STREET FIGHTER ALPHA", 0x10DF);
        put("MEGAMAN 7", 0x113A);
        put("MEGAMAN X3", 0x113D);
        put("Breath of Fire", 0x1144);
    }};
    // Known LoRom games
    static List<String> gamesLoRom = new ArrayList<String>() {
    };
    // Known HiRom games
    static List<String> gamesHiRom = new ArrayList<String>() {
    };
    static List<String> problemGames = new ArrayList<String>() {{
        add("ActRaiser-2 USA"); // ActRaiser2.smc
        add("ALIEN vs. PREDATOR"); // Alien vs. Predator (U).smc
        add("ASTERIX"); // Asterix (E) [!].smc
        add("BATMAN--REVENGE JOKER"); // Batman - Revenge of the Joker (U).smc
        add("???????S???????????"); // Bishoujo Senshi Sailor Moon S - Jougai Rantou! Shuyaku Soudatsusen (J).smc
        add("CHAMPIONSHIP POOL"); // Championship Pool (U).smc
        add("ClayFighter 2"); // Clay Fighter 2 - Judgment Clay (U) [!].smc
        add("CLOCK TOWER SFX"); // Clock Tower (J).smc
        add("COOL WORLD"); // Cool World (U) [!].smc
        add("CRYSTAL BEANS"); // Crystal Beans From Dungeon Explorer (J).smc
        add("CYBER KNIGHT 2"); // Cyber Knight II - Chikyuu Teikoku no Yabou (J).smc
        add("ASCII DARK LAW"); // Dark Law - Meaning of Death (J).smc
        add("DIRT TRAX FX"); // Dirt Trax FX (U) [!].smc
        add("DBZ HYPER DIMENSION"); // Dragon Ball Z - Hyper Dimension (F).smc
        add("DRAGON BALL Z HD"); // Dragon Ball Z - Hyper Dimension (J) [!].smc
        add("DRAGONBALL Z 2"); // Dragon Ball Z - La Legende Saien (F).smc
        add("SFX DRAGONBALLZ2"); // Dragon Ball Z - Super Butouden (F).smc
        add("SFX SUPERBUTOUDEN2"); // Dragon Ball Z - Super Butouden 2 (J) (V1.0).smc
        add("DUNGEON MASTER"); // Dungeon Master (U).smc
        add("EARTHWORM JIM 2"); // Earthworm Jim 2 (U) [!].smc
        add("F1 WORLD CHAMP EDTION"); // F1 World Championship Edition (E).smc
        add("FACEBALL 2000"); // Faceball 2000 (U) [!].smc
        add("THE FIREMEN     PAL"); // Firemen, The (E).smc
        add("HAMMERIN' HARRY (JPN)"); // Ganbare Daiku no Gensan (J) [!].smc
        add("HARMELUNNOBAIOLINHIKI"); // Hamelin no Violin Hiki (J).smc
        add("HOME ALONE"); // Home Alone (U).smc
        add("HUMAN GRANDPRIX"); // Human Grand Prix (J).smc
        add("HUMAN GRANDPRIX 3"); // Human Grand Prix III - F1 Triple Battle (J).smc
        add("ILLUSION OF GAIA USA"); // Illusion of Gaia (U) [!].smc
        add("ILLUSION OF TIME ENG"); // Illusion of Time (E) [!].smc
        add("JumpinDerby"); // Jumpin' Derby (J).smc
        add("KRUSTYS SUPERFUNHOUSE"); // Krusty's Super Fun House (U) (V1.1).smc
        add("Mario's Time Machine"); // Mario's Time Machine (U) [!].smc
        add("MARKOS MAGIC FOOTBALL"); // Marko's Magic Football (E).smc
        add("POWER RANGERS FIGHT"); // Mighty Morphin Power Rangers - The Fighting Edition (U).smc
        add("MOMOTETSU HAPPY"); // Momotarou Dentetsu Happy (J) [!].smc
        add("NHL HOCKEY 1998"); // NHL '98 (U).smc
        add("RENDERING RANGER R2"); // Rendering Ranger R2 (J).smc
        add("ROBOTREK 1 USA"); // Robotrek (U) [!].smc
        add("ROCK N' ROLL RACING"); // Rock N' Roll Racing (U) [!].smc
        add("ROMANCING SAGA3"); // Romancing SaGa 3 (J) (V1.1).smc
        add("SD??????GX"); // SD Gundam GX (J) [!].smc
        add("SECRET OF EVERMORE"); // Secret of Evermore (U) [!].smc
        add("Secret of MANA"); // Secret of Mana (U) [!].smc
        add("SIM CITY 2000"); // Sim City 2000 (U).smc
        add("SMASH TENNIS"); // Smash Tennis (E) [!].smc
        add("Star Ocean"); // Star Ocean (J) [!].smc
        add("STREET FIGHTER ALPHA2"); // Street Fighter Alpha 2 (U) [!].smc
        add("SUPER BASES LOADED 2"); // Super Bases Loaded 2 (U).smc
        add("PANIC BOMBER WORLD"); // Super Bomberman - Panic Bomber W (J).smc
        add("TALES OF PHANTASIA"); // Tales of Phantasia (J) [!].smc
        add("TERRANIGMA P"); // Terranigma (E) [!].smc
        add("TOP GEAR 3000"); // Top Gear 3000 (U) [!].smc
        add("UNIRACERS"); // Uniracers (U) [!].smc
        add("WARIO'S WOODS"); // Wario's Woods (U) [!].smc
        add("WORLD CLASS RUGBY"); // World Class Rugby (E) [!].smc
        add("WORLD CUP STRIKER"); // World Cup Striker (E) (M3) [!].smc
        add("WORLD MASTERS GOLF"); // World Masters Golf (E).smc
        add("WWF SUPER WRESTLEMANI"); // WWF Super WrestleMania (U) [!].smc
        add("WRESTLEMANIA"); // WWF WrestleMania - The Arcade Game (U) [!].smc
    }};
    private static Map<Long, CachedGameInfo> gameInfoCache = null;

    public static Boolean patch(ParameterWrapper wrapper) throws Exception {
        String ext = FileTool.getExtension(wrapper.getInputFileName().getFileName().toString()).toLowerCase();
        if ((ext.equals(".smc")) && (wrapper.getRawRomData().length % 1024) != 0) {
            Debug.WriteLine("Removing SMC header");
            byte[] stripped = new byte[wrapper.getRawRomData().length - 512];
            System.arraycopy(wrapper.getRawRomData(), 512, stripped, 0, stripped.length);
            wrapper.setRawRomData(stripped);
            wrapper.setCrc32(crc32(wrapper.getRawRomData()));
        }
        findPatch(wrapper);
        if (wrapper.getInputFileName().toString().contains("(E)") || wrapper.getInputFileName().toString().contains("(J)"))
            wrapper.setCover(Resources.blank_snes_eu_jp);
        if (ConfigIni.consoleType == ConsoleType.SNES || ConfigIni.consoleType == ConsoleType.SuperFamicom) {
            wrapper.setApplication("/bin/clover-canoe-shvc-wr -rom");
            wrapper.setArgs(DEFAULT_CANOEA_RGS);
            if (!ext.equals(".sfrom")) // Need to patch for canoe
            {
                Debug.WriteLine("Trying to convert " + wrapper.getInputFileName().toString());
                Boolean problemGame = makeSfrom(new SnesRomHeaderWrapper(wrapper));
                wrapper.setOutputFileName(Paths.get(FileTool.getNameWithoutExtension(wrapper.getInputFileName().toString()) + ".sfrom"));
                // Using 3rd party emulator for this ROM
                if (problemGame && (need3rdPartyEmulator == null || !need3rdPartyEmulator)) {
                    if (need3rdPartyEmulator == null) {
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, String.format(Resources.Need3rdPartyEmulator, wrapper.getInputFileName().getFileName().toString()), ButtonType.CANCEL, CustomButtonType.IGNORE);
                        alert.showAndWait();
                        if (alert.getResult() == ButtonType.CANCEL)
                            need3rdPartyEmulator = true;
                        if (alert.getResult() == CustomButtonType.IGNORE)
                            problemGame = false;
                    } else problemGame = false;
                }
                if (problemGame) {
                    wrapper.setApplication("/bin/snes");
                    wrapper.setArgs("");
                }
            }
        } else {
            wrapper.setApplication("/bin/snes");
        }

        return true;
    }

    public static SnesRomHeader getCorrectHeader(SnesRomHeaderWrapper wrapper) throws Exception {
        SnesRomHeader romHeaderLoRom = SnesRomHeader.read(wrapper.getRawRomData(), 0x7FC0);
        SnesRomHeader romHeaderHiRom = SnesRomHeader.read(wrapper.getRawRomData(), 0xFFC0);
        String titleLo = romHeaderLoRom.getTitle();
        String titleHi = romHeaderHiRom.getTitle();

        // Boring LoRom/HiRom detection...
        if (((romHeaderLoRom.getChecksum() ^ 0xFFFF) == romHeaderLoRom.getChecksumComplement()) &&
                ((romHeaderHiRom.getChecksum() ^ 0xFFFF) != romHeaderHiRom.getChecksumComplement()))
            wrapper.setRomType(SnesRomType.loRom);
        else if (((romHeaderLoRom.getChecksum() ^ 0xFFFF) != romHeaderLoRom.getChecksumComplement()) &&
                ((romHeaderHiRom.getChecksum() ^ 0xFFFF) == romHeaderHiRom.getChecksumComplement()))
            wrapper.setRomType(SnesRomType.hiRom);
        else if (titleLo.length() != 0 && titleHi.length() == 0)
            wrapper.setRomType(SnesRomType.loRom);
        else if (titleLo.length() == 0 && titleHi.length() != 0)
            wrapper.setRomType(SnesRomType.hiRom);
        else if ((titleLo.equals(titleHi)) && ((romHeaderLoRom.getRomMakeup() & 1) == 0))
            wrapper.setRomType(SnesRomType.loRom);
        else if ((titleLo.equals(titleHi)) && ((romHeaderHiRom.getRomMakeup() & 1) == 1))
            wrapper.setRomType(SnesRomType.hiRom);
        else if (gamesLoRom.contains(titleLo))
            wrapper.setRomType(SnesRomType.loRom);
        else if (gamesHiRom.contains(titleHi))
            wrapper.setRomType(SnesRomType.hiRom);
        else {
            Boolean loRom = true;
            Boolean hiRom = true;
            for (char c : titleLo.toCharArray())
                if (c < 31 || c > 127) loRom = false;
            for (char c : titleHi.toCharArray())
                if (c < 31 || c > 127) hiRom = false;
            if (loRom && !hiRom)
                wrapper.setRomType(SnesRomType.loRom);
            else if (!loRom && hiRom)
                wrapper.setRomType(SnesRomType.hiRom);
            else {
                Debug.WriteLine("Can't detect ROM type");
                throw new Exception("can't detect ROM type, seems like ROM is corrupted");
            }
        }

        SnesRomHeader romHeader;
        if (wrapper.getRomType() == SnesRomType.loRom) {
            romHeader = romHeaderLoRom;
            wrapper.setGameTitle(titleLo);
        } else {
            romHeader = romHeaderHiRom;
            wrapper.setGameTitle(titleHi);
        }
        return romHeader;
    }

    private static Boolean makeSfrom(SnesRomHeaderWrapper wrapper) throws Exception {

        SnesRomHeader romHeader = getCorrectHeader(wrapper);

        if (wrapper.getRomType() == SnesRomType.loRom)
            wrapper.changeRawRomData(0x7FD9, (byte) 0x01); // Force NTSC
        else
            wrapper.changeRawRomData(0xFFD9, (byte) 0x01); // Force NTSC

        Debug.WriteLine("Game title: " + wrapper.getGameTitle());
        Debug.WriteLine("ROM type: " + wrapper.getRomType());
        Integer presetId = 0; // 0x1011;
        Integer chip = 0;
        if (sfxTypes.contains(romHeader.getRomType())) // Super FX chip
        {
            Debug.WriteLine("Super FX chip detected");
            chip = 0x0C;
        }
        if (knownPresets.get(wrapper.getGameTitle()) == null) // Known codes
        {
            if (dsp1Types.contains(romHeader.getRomType())) {
                Debug.WriteLine("DSP-1 chip detected");
                presetId = 0x10BD; // ID from Mario Kard, DSP1
            }
            if (sA1Types.contains(romHeader.getRomType())) {
                Debug.WriteLine("SA1 chip detected");
                presetId = 0x109C; // ID from Super Mario RPG, SA1
            }
        } else {
            Debug.WriteLine("We have preset for this game");
        }
        Debug.WriteLine(String.format("PresetID: 0x%02X%02X, extra byte: %02X", presetId & 0xFF, (presetId >> 8) & 0xFF, chip));

        SfromHeader1 sfromHeader1 = new SfromHeader1(wrapper.getRawRomData().length);
        SfromHeader2 sfromHeader2 = new SfromHeader2(wrapper.getRawRomData().length, presetId, wrapper.getRomType(), chip);
        byte[] sfromHeader1Raw = sfromHeader1.getBytes();
        byte[] sfromHeader2Raw = sfromHeader2.getBytes();
        byte[] result = new byte[sfromHeader1Raw.length + wrapper.getRawRomData().length + sfromHeader2Raw.length];
        System.arraycopy(sfromHeader1Raw, 0, result, 0, sfromHeader1Raw.length);
        System.arraycopy(wrapper.getRawRomData(), 0, result, sfromHeader1Raw.length, wrapper.getRawRomData().length);
        System.arraycopy(sfromHeader2Raw, 0, result, sfromHeader1Raw.length + wrapper.getRawRomData().length, sfromHeader2Raw.length);

        if (romHeader.getSramSize() > 0)
            wrapper.setSaveCount((byte) 3);
        else
            wrapper.setSaveCount((byte) 0);

        boolean problemGame = problemGames.contains(wrapper.getGameTitle());

        wrapper.setRawRomData(result);
        return problemGame;
    }
//
//    public SfromHeader1 ReadSfromHeader1() {
//        foreach(var f in Directory.getFiles(GamePath, "*.sfrom"))
//        {
//            var sfrom = File.readAllBytes(f);
//            var sfromHeader1 = SfromHeader1.read(sfrom, 0);
//            return sfromHeader1;
//        }
//        throw new Exception(".sfrom file not found");
//    }
//
//    public SfromHeader2 ReadSfromHeader2() {
//        foreach(var f in Directory.getFiles(GamePath, "*.sfrom"))
//        {
//            var sfrom = File.readAllBytes(f);
//            var sfromHeader1 = SfromHeader1.read(sfrom, 0);
//            var sfromHeader2 = SfromHeader2.read(sfrom, (int) sfromHeader1.header2);
//            return sfromHeader2;
//        }
//        throw new Exception(".sfrom file not found");
//    }
//
//    public void WriteSfromHeader1(SfromHeader1 sfromHeader1) {
//        foreach(var f in Directory.getFiles(GamePath, "*.sfrom"))
//        {
//            var sfrom = File.readAllBytes(f);
//            var data = sfromHeader1.getBytes();
//            Array.copy(data, 0, sfrom, 0, data.length);
//            File.writeAllBytes(f, sfrom);
//            return;
//        }
//        throw new Exception(".sfrom file not found");
//    }
//
//    public void WriteSfromHeader2(SfromHeader2 sfromHeader2) {
//        foreach(var f in Directory.getFiles(GamePath, "*.sfrom"))
//        {
//            var sfrom = File.readAllBytes(f);
//            var sfromHeader1 = SfromHeader1.read(sfrom, 0);
//            var data = sfromHeader2.getBytes();
//            Array.copy(data, 0, sfrom, (int) sfromHeader1.header2, data.length);
//            File.writeAllBytes(f, sfrom);
//            return;
//        }
//        throw new Exception(".sfrom file not found");
//    }
//
//
    public static Runnable loadCahe = ()-> {
//        try {
//            try {
//                Path xmlDataBasePath = Paths.get(".", "data", "snescarts.xml");
//                Debug.WriteLine("Loading " + xmlDataBasePath);
//                Debug.WriteLine(xmlDataBasePath.toFile().exists());
//
//                if (Files.exists(xmlDataBasePath)) {
//                    SAXParserFactory factory = SAXParserFactory.newInstance();
//                    SAXParser saxParser = null;
//
//                    saxParser = factory.newSAXParser();
//                    UserHandler userhandler = new UserHandler();
//                    saxParser.parse(xmlDataBasePath.toFile(), userhandler);
//
//                    gameInfoCache = userhandler.getGameInfoCache();
////            gameInfoCache.forEach((aLong, cachedGameInfo) -> Debug.WriteLine("["+Long.toHexString(aLong)+"]\n"
////                    + cachedGameInfo.getName() + "\n"
////                    + cachedGameInfo.getPublisher() + "\n"
////                    + cachedGameInfo.getReleaseDate() + "\n"
////                    + cachedGameInfo.getRegion() + "\n"));
//
//                }
//                Debug.WriteLine(String.format("NES XML loading done, %d roms total", gameInfoCache.size()));
//            } catch (SAXException | ParserConfigurationException | IOException e) {
//                e.printStackTrace();
//            }
//            Path xmlDataBasePath = Paths.get(Paths.get(".", "data").toString(), "snescarts.xml");
//            Debug.WriteLine("Loading " + xmlDataBasePath);
//
//            if (Files.exists(xmlDataBasePath)) {
//                var xpath = new XPathDocument(xmlDataBasePath);
//                var navigator = xpath.createNavigator();
//                var iterator = navigator.select("/Data");
//                gameInfoCache = new Dictionary<Long, CachedGameInfo>();
//                while (iterator.moveNext()) {
//                    XPathNavigator game = iterator.current;
//                    var cartridges = game.select("Game");
//                    while (cartridges.moveNext()) {
//                        var cartridge = cartridges.current;
//                        Long crc = 0;
//                        var info = new CachedGameInfo();
//
//                        try {
//                            var v = cartridge.select("name");
//                            if (v.moveNext() && !String.isNullOrEmpty(v.current.value))
//                                info.name = v.current.value;
//                            v = cartridge.select("players");
//                            if (v.moveNext() && !String.isNullOrEmpty(v.current.value))
//                                info.players = byte.parse(v.current.value);
//                            v = cartridge.select("simultaneous");
//                            if (v.moveNext() && !String.isNullOrEmpty(v.current.value))
//                                info.simultaneous = byte.parse(v.current.value) != 0;
//                            v = cartridge.select("crc");
//                            if (v.moveNext() && !String.isNullOrEmpty(v.current.value))
//                                crc = Convert.toUInt32(v.current.value, 16);
//                            v = cartridge.select("date");
//                            if (v.moveNext() && !String.isNullOrEmpty(v.current.value))
//                                info.releaseDate = v.current.value;
//                            v = cartridge.select("publisher");
//                            if (v.moveNext() && !String.isNullOrEmpty(v.current.value))
//                                info.publisher = v.current.value;
//                            v = cartridge.select("region");
//                            if (v.moveNext() && !String.isNullOrEmpty(v.current.value))
//                                info.region = v.current.value;
//                            v = cartridge.select("cover");
//                            if (v.moveNext() && !String.isNullOrEmpty(v.current.value))
//                                info.coverUrl = v.current.value;
//                        } catch
//                        {
//                            Debug.WriteLine($"Invalid XML record for game: {cartridge.outerXml}");
//                        }
//
//                        gameInfoCache[crc] = info;
//                    } ;
//                }
//            }
//            Debug.WriteLine(String.format("SNES XML loading done, {0} roms total", gameInfoCache.count));
//        } catch (Exception ex) {
//            Debug.WriteLine(ex.message + ex.stackTrace);
//        }
    };
//    private static class UserHandler extends DefaultHandler {
//
//        @Getter
//        private Map<Long, CachedGameInfo> gameInfoCache;
//
//        private CachedGameInfo cachedGameInfo;
//        private String crc;
//
//        public UserHandler() {
//            this.gameInfoCache = new HashMap<>();
//        }
//
//        @Override
//        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
//            if (qName.equals("game")) {
//                //Debug.WriteLine("Start: " + qName);
//                cachedGameInfo = new CachedGameInfo();
//                String value = attributes.getValue("name");
//                cachedGameInfo.setName(value == null ? "" : value);
//                value = attributes.getValue("players");
//                cachedGameInfo.setPlayers(value == null ? 1 : Byte.parseByte(value));
//                value = attributes.getValue("date");
//                cachedGameInfo.setReleaseDate(value == null ? "" : value);
//                value = attributes.getValue("publisher");
//                cachedGameInfo.setPublisher(value == null ? "" : value);
//                value = attributes.getValue("region");
//                cachedGameInfo.setRegion(value == null ? "" : value);
//            }
//            if (qName.equals("cartridge")) {
//                crc = attributes.getValue("crc");
//            }
//
//        }
//
//        @Override
//        public void endElement(String uri, String localName, String qName) throws SAXException {
//            if (qName.equals("game")) {
//                //Debug.WriteLine("End: " +qName);
//                if (gameInfoCache != null) {
//                    gameInfoCache.put(Long.parseLong(crc, 16), cachedGameInfo);
//                    cachedGameInfo = null;
//                    crc = null;
//                }
//            }
//        }
//    }
//
//    public Boolean TryAutofill(Long crc32) {
//        CachedGameInfo gameinfo;
//        if (gameInfoCache != null && gameInfoCache.tryGetValue(crc32, out gameinfo)) {
//            if (!String.isNullOrEmpty(gameinfo.name))
//                Name = gameinfo.name;
//            Players = gameinfo.players;
//            Simultaneous = gameinfo.simultaneous;
//            if (!String.isNullOrEmpty(gameinfo.releaseDate))
//                ReleaseDate = gameinfo.releaseDate;
//            if (ReleaseDate.length == 4) ReleaseDate += "-01";
//            if (ReleaseDate.length == 7) ReleaseDate += "-01";
//            if (!String.isNullOrEmpty(gameinfo.publisher))
//                Publisher = gameinfo.publisher.toUpper();
//
//                /*
//                if (!String.isNullOrEmpty(gameinfo.coverUrl))
//                {
//                    if (NeedAutoDownloadCover != true)
//                    {
//                        if (NeedAutoDownloadCover != false)
//                        {
//                            var r = WorkerForm.messageBoxFromThread(ParentForm,
//                                String.format(Resources.downloadCoverQ, Name),
//                                Resources.cover,
//                                MessageBoxButtons.abortRetryIgnore,
//                                MessageBoxIcon.question,
//                                MessageBoxDefaultButton.button2, true);
//                            if (r == DialogResult.abort)
//                                NeedAutoDownloadCover = true;
//                            if (r == DialogResult.ignore)
//                                return true;
//                        }
//                        else return true;
//                    }
//
//                    try
//                    {
//                        var cover = ImageGooglerForm.downloadImage(gameinfo.coverUrl);
//                        Image = cover;
//                    }
//                    catch (Exception ex)
//                    {
//                        Debug.WriteLine(ex.message + ex.stackTrace);
//                    }
//                }
//                */
//            return true;
//        }
//        return false;
//    }
//
//    public void ApplyGameGenie() {
//        if (!String.isNullOrEmpty(GameGenie)) {
//            var codes = GameGenie.split(new char[]{',', '\t', ' ', ';'}, StringSplitOptions.removeEmptyEntries);
//            var nesFiles = Directory.getFiles(this.gamePath, "*.*", SearchOption.topDirectoryOnly);
//            foreach(var f in nesFiles)
//            {
//                byte[] data;
//                var ext = Path.getExtension(f).ToLower();
//                int offset;
//                if (ext == ".sfrom") {
//                    data = File.readAllBytes(f);
//                    offset = 48;
//                } else if (ext == ".sfc" || ext == ".smc") {
//                    data = File.readAllBytes(f);
//                    if ((data.length % 1024) != 0)
//                        offset = 512;
//                    else
//                        offset = 0;
//                } else continue;
//
//                var rawData = new byte[data.length - offset];
//                Array.copy(data, offset, rawData, 0, rawData.length);
//
//                foreach(var code in codes)
//                rawData = GameGeniePatcherSnes.patch(rawData, code);
//
//                Array.copy(rawData, 0, data, offset, rawData.length);
//                File.writeAllBytes(f, data);
//            }
//        }
//    }


    @Override
    public String getGoogleSuffix() {
        return "(snes | super nintendo)";
    }

}