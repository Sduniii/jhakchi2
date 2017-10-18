package gui.controller;

import clovershell.ClovershellConnection;
import clovershell.ConnectedListener;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;

import java.net.URL;
import java.util.ResourceBundle;

public class OverviewController implements Initializable, ConnectedListener {

    @FXML
    Circle connectedCircle;

    public void initialize(URL location, ResourceBundle resources) {
        ClovershellConnection clovershell = new ClovershellConnection();
        clovershell.addListener(this);
        Platform.runLater(() -> clovershell.setEnabled(true));
    }


    @Override
    public void onConnected(boolean b) {
        if (b)
            connectedCircle.setFill(new RadialGradient(-25.71, 0.4047619047619049, 0, 0, 1.0, true, CycleMethod.NO_CYCLE, new Stop(0, Color.WHITE), new Stop(0.18888888888888888, Color.WHITE), new Stop(0.8025925925925926, Color.GREEN), new Stop(1.0, Color.GREEN)));
        else
            connectedCircle.setFill(new RadialGradient(-25.71, 0.4047619047619049, 0, 0, 1.0, true, CycleMethod.NO_CYCLE, new Stop(0, Color.WHITE), new Stop(0.18888888888888888, Color.WHITE), new Stop(0.8025925925925926, Color.RED), new Stop(1.0, Color.RED)));
    }
}
