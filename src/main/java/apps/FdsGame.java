package apps;

import config.ConfigIni;
import enums.ConsoleType;
import tools.ArrayTool;

import java.io.IOException;
import java.nio.file.Path;

public class FdsGame extends MiniApplication {

    protected final static String DEFAULT_ARGS = "--guest-overscan-dimensions 0,0,9,3 --initial-fadein-durations 10,2 --volume 75 --enable-armet --fds-auto-disk-side-switch-on-keypress";
    protected final static char PREFIX = 'D';

    public FdsGame(Path path) throws IOException {
        super(path);
    }

    public FdsGame(Path path, boolean ignoreEmptyConfig) throws IOException {
        super(path, ignoreEmptyConfig);
    }

    public static boolean patch(PatchParameterWrapper wrapper) throws IOException {
        findPatch(wrapper);
        if(new String(wrapper.getRawRomData(),0,3).equals("FDS")){
            byte[] fdsDataNoHeader = new byte[wrapper.getRawRomData().length - 0x10];
            ArrayTool.copy(wrapper.getRawRomData(),0x10,fdsDataNoHeader,0,fdsDataNoHeader.length);
            wrapper.setRawRomData(fdsDataNoHeader);
            wrapper.setCrc32(crc32(wrapper.getRawRomData()));
            findPatch(wrapper);
        }
        if(ConfigIni.consoleType == ConsoleType.NES || ConfigIni.consoleType == ConsoleType.Famicom)
            wrapper.setApplication("/bin/clover-kachikachi-wr");
        else
            wrapper.setApplication("/bin/nes");
        wrapper.setArgs(DEFAULT_ARGS);
        return true;
    }

    @Override
    public String getGoogleSuffix() {
        return "(fds | nes | famicom)";
    }

}
