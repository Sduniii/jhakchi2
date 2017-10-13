package apps;

import java.io.IOException;
import java.nio.file.Path;

public class Sega32XGame extends MiniApplication{

    public Sega32XGame(Path path) throws IOException {
        super(path);
    }

    public Sega32XGame(Path path, boolean ignoreEmptyConfig) throws IOException {
        super(path, ignoreEmptyConfig);
    }

    @Override
    public String getGoogleSuffix() {
        return "sega 32X";
    }
}