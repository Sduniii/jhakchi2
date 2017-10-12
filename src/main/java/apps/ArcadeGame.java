package apps;

import java.io.IOException;
import java.nio.file.Path;

public class ArcadeGame extends MiniApplication{

    public ArcadeGame(Path path) throws IOException {
        super(path);
    }

    public ArcadeGame(Path path, boolean ignoreEmptyConfig) throws IOException {
        super(path, ignoreEmptyConfig);
    }

    @Override
    public String getGoogleSuffix() {
        return "arcade";
    }
}
