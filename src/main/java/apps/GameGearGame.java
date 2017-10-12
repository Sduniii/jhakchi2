package apps;

import java.io.IOException;
import java.nio.file.Path;

public class GameGearGame extends MiniApplication{
    public GameGearGame(Path path) throws IOException {
        super(path);
    }

    public GameGearGame(Path path, boolean ignoreEmptyConfig) throws IOException {
        super(path, ignoreEmptyConfig);
    }

    @Override
    public String getGoogleSuffix() {
        return "game gear";
    }
}
