package apps;

import gamegenie.GameGenieFormatException;
import gamegenie.GameGenieNotFoundException;

public interface ISupportGameGenie {
    void applyGameGenie() throws Exception, GameGenieFormatException, GameGenieNotFoundException;
}
