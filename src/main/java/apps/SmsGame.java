package apps;

import java.io.IOException;
import java.nio.file.Path;

public class SmsGame extends MiniApplication{

    public SmsGame(Path path) throws IOException {
        super(path);
    }

    public SmsGame(Path path, boolean ignoreEmptyConfig) throws IOException {
        super(path, ignoreEmptyConfig);
    }

    @Override
    public String getGoogleSuffix() {
        return "(sms | sega master system)";
    }
}