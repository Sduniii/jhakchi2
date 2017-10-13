package apps.wrapper;

import apps.SnesGame;
import lombok.Getter;
import lombok.Setter;

public class SnesRomHeaderWrapper {
    @Getter
    @Setter
    private SnesGame.SnesRomType romType = SnesGame.SnesRomType.no;
    @Getter
    @Setter
    private String gameTitle = "";
    private ParameterWrapper parameterWrapper;

    public SnesRomHeaderWrapper(ParameterWrapper wrapper) {
        parameterWrapper = wrapper;
    }

    public byte[] getRawRomData() {
        return parameterWrapper.getRawRomData();
    }

    public boolean changeRawRomData(int address, byte value) {
        if (getRawRomData() != null && getRawRomData().length >= address && address >= 0) {
            parameterWrapper.changeRawRomData(address, value);
            return true;
        }
        return false;
    }

    public void setSaveCount(byte saveCount) {
        parameterWrapper.setSaveCount(saveCount);
    }

    public void setRawRomData(byte[] rawData) {
        parameterWrapper.setRawRomData(rawData);
    }
}