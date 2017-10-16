package gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipNativeInitializationException;
import tools.Debug;

import java.io.IOException;

public class Main extends Application {

    public static void main(String[] args) {
        try {
            SevenZip.initSevenZipFromPlatformJAR();
            Debug.WriteLine("7-Zip-JBinding library was initialized");
            launch(args);
        } catch (SevenZipNativeInitializationException e) {
            e.printStackTrace();
            Platform.exit();
        }
    }

    @Override
    public void start(Stage primaryStage) {
        Pane root = null;
        try{
            root = FXMLLoader.load(getClass().getResource("/fxml/overview.fxml"));
        }catch (IOException e){
            Debug.log(e);
        }
        if(root != null){
            Scene scene = new Scene(root);
            primaryStage.setTitle("Jhakchi");
            primaryStage.setOnCloseRequest(e -> {
                Platform.exit();
                System.exit(0);
            });
            primaryStage.setScene(scene);
            primaryStage.show();
        }
    }
}
