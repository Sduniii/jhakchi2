package gui.controller;

import apps.NesGame;
import apps.SnesGame;
import clovershell.ClovershellConnection;
import clovershell.ClovershellException;
import clovershell.ConnectedListener;
import config.ConfigIni;
import enums.ConsoleType;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import org.apache.commons.lang3.ArrayUtils;
import tools.Debug;
import tools.FileTool;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class OverviewController implements Initializable, ConnectedListener {

    @FXML
    Circle connectedCircle;
    @FXML
    RadioMenuItem menuItemConsoleTypeSNES;
    @FXML
    RadioMenuItem menuItemConsoleTypeSuperFamicon;

    private final int defaultMaxGameSize = 300;
    public static List<String> internalMods;
    public static ClovershellConnection clovershell;
    public static Boolean downloadCover;
    private static ConsoleType lastConsoleType = ConsoleType.Unknown;

    public void initialize(URL location, ResourceBundle resources) {
        clovershell = new ClovershellConnection();
        clovershell.setAutoreconnect(true);
        clovershell.addOnConnectedListener(this);
        Platform.runLater(() -> clovershell.setEnabled(true));
        syncConsoleType();
        try {
            internalMods = Files.list(Paths.get(".","mods/hmods")).filter(path -> Files.isRegularFile(path)).map(path -> FileTool.getNameWithoutExtension(path.getFileName().toString())).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        Debug.WriteLine(ArrayUtils.toString(internalMods.toArray()));
        new Thread(NesGame.loadCache).start();
        new Thread(SnesGame.loadCahe).start();
    }

    private void syncConsoleType() {
        if(lastConsoleType == ConfigIni.consoleType) return;
        menuItemConsoleTypeSNES.selectedProperty().setValue(ConfigIni.consoleType == ConsoleType.SNES);
        menuItemConsoleTypeSuperFamicon.selectedProperty().setValue(ConfigIni.consoleType == ConsoleType.SuperFamicom);

        lastConsoleType = ConfigIni.consoleType;

    }


    @Override
    public void onConnected(boolean b) {
        if (b) {
            connectedCircle.setFill(new RadialGradient(-25.71, 0.4047619047619049, 0, 0, 1.0, true, CycleMethod.NO_CYCLE, new Stop(0, Color.WHITE), new Stop(0.18888888888888888, Color.WHITE), new Stop(0.8025925925925926, Color.GREEN), new Stop(1.0, Color.GREEN)));
            try {
                String customFirmware = clovershell.executeSimple("[ -d /var/lib/hakchi/firmware/ ] && [ -f /var/lib/hakchi/firmware/*.hsqs ] && echo YES || echo NO",4000,false);
                Debug.WriteLine(customFirmware);
            } catch (ClovershellException | IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }else
            connectedCircle.setFill(new RadialGradient(-25.71, 0.4047619047619049, 0, 0, 1.0, true, CycleMethod.NO_CYCLE, new Stop(0, Color.WHITE), new Stop(0.18888888888888888, Color.WHITE), new Stop(0.8025925925925926, Color.RED), new Stop(1.0, Color.RED)));
    }
}
