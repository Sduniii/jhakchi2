package apps;

import java.io.IOException;
import java.nio.file.Path;

public class GbaGame extends MiniApplication{
    public GbaGame(Path path) throws IOException {
        super(path);
    }

    public GbaGame(Path path, boolean ignoreEmptyConfig) throws IOException {
        super(path, ignoreEmptyConfig);
    }

    @Override
    public String getGoogleSuffix() {
        return "gba";
    }
}
