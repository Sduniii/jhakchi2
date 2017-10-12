package apps;

import java.io.IOException;
import java.nio.file.Path;

public class GenesisGame extends MiniApplication{
    public GenesisGame(Path path) throws IOException {
        super(path);
    }

    public GenesisGame(Path name, boolean ignoreEmptyConfig) throws IOException {
        super(name, ignoreEmptyConfig);
    }

    @Override
    public String getGoogleSuffix() {
        return "genesis";
    }
}
