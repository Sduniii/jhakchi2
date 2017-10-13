package apps.header;

import apps.wrapper.ParameterWrapper;
import apps.SnesGame;
import config.ConfigIni;
import enums.ConsoleType;
import fx.JavaFXThreadingRule;
import org.junit.Rule;
import org.junit.Test;


import java.nio.file.Files;
import java.nio.file.Paths;

public class SfromHeader1Test {

    public SfromHeader1Test(){

    }
    @Rule
    public JavaFXThreadingRule jfxRule = new JavaFXThreadingRule();

    @Test
    public void test() throws Exception {


        ConfigIni.consoleType = ConsoleType.SNES;

        SfromHeader1 snes = new SfromHeader1();

        ParameterWrapper wrapper = new ParameterWrapper();
        wrapper.setInputFileName(Paths.get("./roms/snes/ActRaiser2.smc"));
        wrapper.setRawRomData(Files.readAllBytes(Paths.get("./roms/snes/ActRaiser2.smc")));
        snes = SfromHeader1.read(wrapper.getRawRomData(),0);
        if(SnesGame.patch(wrapper)) Files.write(wrapper.getOutputFileName(),wrapper.getRawRomData());
    }

}