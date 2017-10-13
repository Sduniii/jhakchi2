package apps.wrapper;

import lombok.Getter;
import lombok.Setter;

public class CachedGameInfo {
    @Getter
    @Setter
    private String name;
    @Getter
    @Setter
    private byte players;
    @Getter
    @Setter
    private String releaseDate;
    @Getter
    @Setter
    private String publisher;
    @Getter
    @Setter
    private String region;
    @Getter
    @Setter
    private String coverUrl;
}
