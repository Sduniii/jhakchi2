package apps;

import java.io.IOException;
import java.nio.file.Path;

public class GbGame extends MiniApplication {
    public GbGame(Path path) throws IOException {
        super(path);
    }

    public GbGame(Path path, boolean ignoreEmptyConfig) throws IOException {
        super(path, ignoreEmptyConfig);
    }

    @Override
    public String getGoogleSuffix() {
        return "(gameboy | game boy)";
    }
}
