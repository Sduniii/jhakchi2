package apps;

import javafx.scene.image.Image;
import lombok.Getter;
import lombok.Setter;

import java.nio.file.Path;

public class PatchParameterWrapper {
    @Setter @Getter
    private Path inputFileName,outputFileName;
    @Setter @Getter
    private String application, args;
    @Setter @Getter
    private byte[] rawRomData;
    @Setter @Getter
    private char prefix;
    @Setter @Getter
    private Image cover;
    @Setter @Getter
    private long crc32;
    @Getter @Setter
    private byte saveCount;
    @Getter @Setter
    private Path patch;

    public PatchParameterWrapper(){
        this.inputFileName = null;
        this.application = null;
        this.outputFileName = null;
        this.args = null;
        this.rawRomData = null;
        this.prefix = 0;
        this.cover = null;
        this.crc32 = 0;
        this.saveCount = 0;
        this.patch = null;
    }

    public PatchParameterWrapper(Path inputFileName, String application, Path outputFileName, String args, byte[] rawRomData, char prefix, Image cover, long crc32, byte saveCount) {
        this.inputFileName = inputFileName;
        this.application = application;
        this.outputFileName = outputFileName;
        this.args = args;
        this.rawRomData = rawRomData;
        this.prefix = prefix;
        this.cover = cover;
        this.crc32 = crc32;
        this.saveCount = saveCount;
        this.patch = null;
    }
}
