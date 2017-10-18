package apps;

import Properties.Resources;
import apps.wrapper.CachedGameInfo;
import apps.wrapper.ParameterWrapper;
import config.ConfigIni;
import enums.ConsoleType;
import gamegenie.GameGenieFormatException;
import gamegenie.GameGenieNotFoundException;
import gamegenie.GameGeniePatcherNes;
import gui.controls.CustomButtonType;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import lombok.Getter;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import tools.ArrayTool;
import tools.Debug;
import tools.FileTool;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class NesGame extends MiniApplication implements ICloverAutofill, ISupportGameGenie {

    public static final char PREFIX = 'H';
    private static Boolean ignoreMapper;
    public final static String DEFAULT_ARGS = "--guest-overscan-dimensions 0,0,9,3 --initial-fadein-durations 10,2 --volume 75 --enable-armet";
    private static Map<Long, CachedGameInfo> gameInfoCache = null;
    private static byte[] supportedMappers = new byte[]{0, 1, 2, 3, 4, 5, 7, 9, 10, 86, 87, -72};

    public NesGame(Path path) throws IOException {
        super(path);
    }

    public NesGame(Path path, Boolean ignoreEmptyConfig) throws IOException {
        super(path, ignoreEmptyConfig);
    }

    @Override
    public String getGoogleSuffix() {
        return "(nes | famicom)";
    }

    public static Boolean patch(ParameterWrapper wrapper) throws Exception {
        Boolean patched = findPatch(wrapper);
        NesFile nesFile;
        try {
            nesFile = new NesFile(wrapper.getRawRomData());
        } catch (Exception e) {
            wrapper.setApplication("/bin/nes");
            return true;
        }
        wrapper.setCrc32(nesFile.CRC32());
        if (!patched) {
            if (findPatch(wrapper)) {
                nesFile = new NesFile(wrapper.getRawRomData());
            }
        }
        nesFile.correctRom();
        if (ConfigIni.consoleType == ConsoleType.NES || ConfigIni.consoleType == ConsoleType.Famicom) {
            wrapper.setApplication("/bin/clover-kachikachi-wr");
            wrapper.setArgs(DEFAULT_ARGS);
        } else {
            wrapper.setApplication("/bin/nes");
        }
        if (!ArrayTool.contains(supportedMappers, nesFile.mapper) &&
                (ConfigIni.consoleType == ConsoleType.NES || ConfigIni.consoleType == ConsoleType.Famicom) &&
                (ignoreMapper == null || !ignoreMapper)) {
            if (ignoreMapper == null) { //always False
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, String.format(Resources.MapperNotSupported, wrapper.getInputFileName().getFileName().toString(), nesFile.mapper), ButtonType.CANCEL, CustomButtonType.IGNORE);
                alert.showAndWait();
                if (alert.getResult() == ButtonType.CANCEL)
                    ignoreMapper = true;
                if (alert.getResult() == CustomButtonType.IGNORE)
                    return false;
            }
            return false;
        }

        if ((nesFile.mirroring == NesFile.MirroringType.FourScreenVram) &&
                (ConfigIni.consoleType == ConsoleType.NES || ConfigIni.consoleType == ConsoleType.Famicom) &&
                (ignoreMapper == null || !ignoreMapper)) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, String.format(Resources.FourScreenNotSupported, wrapper.getInputFileName().getFileName().toString()), ButtonType.CANCEL, ButtonType.NO);
            alert.showAndWait();
            if (alert.getResult() == ButtonType.CANCEL)
                ignoreMapper = true;
            if (alert.getResult() == ButtonType.NO)
                return false;
        }

        wrapper.setRawRomData(nesFile.getRaw());
        if (wrapper.getInputFileName().toString().contains("(J)"))
            wrapper.setCover(Resources.blank_jp);

        if (nesFile.battery)
            wrapper.setSaveCount((byte) 3);
        return true;
    }

    public static Runnable loadCache = () -> {
        try {
            Path xmlDataBasePath = Paths.get(".", "data", "nescarts.xml");
            Debug.WriteLine("Loading " + xmlDataBasePath);
            Debug.WriteLine(xmlDataBasePath.toFile().exists());

            if (Files.exists(xmlDataBasePath)) {
                SAXParserFactory factory = SAXParserFactory.newInstance();
                SAXParser saxParser = null;

                saxParser = factory.newSAXParser();
                UserHandler userhandler = new UserHandler();
                saxParser.parse(xmlDataBasePath.toFile(), userhandler);

                gameInfoCache = userhandler.getGameInfoCache();
//            gameInfoCache.forEach((aLong, cachedGameInfo) -> Debug.WriteLine("["+Long.toHexString(aLong)+"]\n"
//                    + cachedGameInfo.getName() + "\n"
//                    + cachedGameInfo.getPublisher() + "\n"
//                    + cachedGameInfo.getReleaseDate() + "\n"
//                    + cachedGameInfo.getRegion() + "\n"));

            }
            Debug.WriteLine(String.format("NES XML loading done, %d roms total", gameInfoCache.size()));
        } catch (SAXException | ParserConfigurationException | IOException e) {
            e.printStackTrace();
        }
    };

    @Override
    public Boolean tryAutofill(long crc32) {
        CachedGameInfo gameInfo;
        if (gameInfoCache != null && (gameInfo = gameInfoCache.get(crc32)) != null) {
            this.setName(gameInfo.getName().replace("_", " ").replace("  ", " ").trim());
            this.setPlayers(gameInfo.getPlayers());
            if (this.getPlayers() > 1) this.setSimultaneous(true);
            this.setReleaseDate(gameInfo.getReleaseDate());
            if (this.getReleaseDate().length() == 4) this.setReleaseDate(this.getReleaseDate() + "-01");
            if (this.getReleaseDate().length() == 7) this.setReleaseDate(this.getReleaseDate() + "-01");
            this.setPublisher(gameInfo.getPublisher().toUpperCase());
            return true;
        }
        return false;
    }

    @Override
    public void applyGameGenie() throws Exception, GameGenieFormatException, GameGenieNotFoundException {
        if (getGameGenie() != null && !getGameGenie().equals("")) {
            String[] sar = getGameGenie().split("[,\\s\\t;]");
            List<String> tmpCode = new ArrayList<>(Arrays.asList(sar));
            tmpCode.removeAll(Arrays.asList("", null));
            String[] codes = new String[tmpCode.size()];
            codes = tmpCode.toArray(codes);
            //var codes = getGameGenie().split("(,)*(\\s)*(;)*(\\t)*");
            List<Path> nesFiles = Files.list(this.getGamePath()).filter(path -> FileTool.getExtension(path.getFileName().toString()).equals(".nes")).collect(Collectors.toList());
            for (Path f : nesFiles) {
                NesFile nesFile = new NesFile(f);
                for (String code : codes) {
                    nesFile.prg = GameGeniePatcherNes.Patch(nesFile.prg, code.trim());
                }
                nesFile.save(f);
            }
        }
    }

    private static class UserHandler extends DefaultHandler {

        @Getter
        private Map<Long, CachedGameInfo> gameInfoCache;

        private CachedGameInfo cachedGameInfo;
        private String crc;

        public UserHandler() {
            this.gameInfoCache = new HashMap<>();
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (qName.equals("game")) {
                //Debug.WriteLine("Start: " + qName);
                cachedGameInfo = new CachedGameInfo();
                String value = attributes.getValue("name");
                cachedGameInfo.setName(value == null ? "" : value);
                value = attributes.getValue("players");
                cachedGameInfo.setPlayers(value == null ? 1 : Byte.parseByte(value));
                value = attributes.getValue("date");
                cachedGameInfo.setReleaseDate(value == null ? "" : value);
                value = attributes.getValue("publisher");
                cachedGameInfo.setPublisher(value == null ? "" : value);
                value = attributes.getValue("region");
                cachedGameInfo.setRegion(value == null ? "" : value);
            }
            if (qName.equals("cartridge")) {
                crc = attributes.getValue("crc");
            }

        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (qName.equals("game")) {
                //Debug.WriteLine("End: " +qName);
                if (gameInfoCache != null) {
                    gameInfoCache.put(Long.parseLong(crc, 16), cachedGameInfo);
                    cachedGameInfo = null;
                    crc = null;
                }
            }
        }
    }


}
