package apps;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;

class NesGameTest {
    @Test
    void loadCache() {
        try {
            NesGame.loadCache();
        } catch (Exception e) {
            e.printStackTrace();
            assert false;
        }
    }

    @Test
    void test(){
        try {
            new NesGame(Paths.get("./roms/nes/zelda.nes"), false);
        } catch (IOException e) {
            e.printStackTrace();
            Assertions.fail("Error");
        }
    }

}