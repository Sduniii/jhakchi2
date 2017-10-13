package apps;

import java.io.IOException;
import java.nio.file.Path;

public class NesUGame extends MiniApplication{

    public NesUGame(Path path) throws IOException {
        super(path);
    }

    public NesUGame(Path path, boolean ignoreEmptyConfig) throws IOException {
        super(path, ignoreEmptyConfig);
    }

    @Override
    public String getGoogleSuffix() {
        return "(nes | famicom)";
    }
}
