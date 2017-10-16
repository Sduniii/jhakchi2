package gui.controller;

import apps.AppTypeCollection;
import apps.MiniApplication;
import apps.wrapper.ParameterWrapper;
import clovershell.Clovershell;
import clovershell.ConnectedListener;
import clovershell.DataReceivedListener;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import tools.Debug;
import tools.UsbDevices;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.ResourceBundle;

public class OverviewController implements Initializable, ConnectedListener {

    @FXML
    Circle connectedCircle;

    public void initialize(URL location, ResourceBundle resources) {
        Clovershell clovershell = new Clovershell();
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
