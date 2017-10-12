package apps;

import java.io.IOException;
import java.nio.file.Path;

public class Atari2600Game extends MiniApplication {
    public Atari2600Game(Path path) throws IOException {
        super(path);
    }
    public Atari2600Game(Path path, boolean b) throws IOException {
        super(path,b);
    }

    @Override
    public String getGoogleSuffix() {
        return "atari 2600";
    }
}
