package apps;

import java.io.IOException;
import java.nio.file.Path;

public class GbcGame extends MiniApplication{
    public GbcGame(Path path) throws IOException {
        super(path);
    }

    public GbcGame(Path path, boolean ignoreEmptyConfig) throws IOException {
        super(path, ignoreEmptyConfig);
    }

    @Override
    public String getGoogleSuffix() {
        return "(gameboy | game boy)";
    }
}