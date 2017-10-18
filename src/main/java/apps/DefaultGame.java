package apps;

import lombok.Getter;
import lombok.Setter;

public class DefaultGame implements IMenuElement {

    @Getter
    @Setter
    private String code,name;
    @Getter
    @Setter
    private int size;

}
