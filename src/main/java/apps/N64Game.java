package apps;

import java.io.IOException;
import java.nio.file.Path;

public class N64Game extends MiniApplication {
    public N64Game(Path path) throws IOException {
        super(path);
    }

    public N64Game(Path path, boolean ignoreEmptyConfig) throws IOException {
        super(path, ignoreEmptyConfig);
    }

    @Override
    public String getGoogleSuffix() {
        return "nintendo 64";
    }
}
