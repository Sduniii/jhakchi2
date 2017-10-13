package gui.controller;

import apps.AppTypeCollection;
import apps.MiniApplication;
import apps.ParameterWrapper;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
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

public class OverviewController implements Initializable {
    public void initialize(URL location, ResourceBundle resources) {
        UsbDevices.openConnection();
        Debug.WriteLine(UsbDevices.findDevice((short)0x1f3a,(short)0xefe8));
        UsbDevices.closeConnection();

        AppTypeCollection.AppInfo app = AppTypeCollection.ApplicationTypes[2];
        try {
            Class<?> theClass = app.clazz;
            Constructor<?> constructor = theClass.getDeclaredConstructor(Path.class,boolean.class);
            MiniApplication m = (MiniApplication) constructor.newInstance(Paths.get("hahaah.gzt"), true);

            Method patch = theClass.getDeclaredMethod("patch", ParameterWrapper.class);
            Path inputFileName = Paths.get("file.bla");
            byte[] rawRomData = {0,0,0,0,0,0,0,0,0,0};
            char prefix = 'Z';
            String application = "/bin/nes";
            Path outputFileName = Paths.get("file.bla.bla");
            String args = "";
            Image cover = null;
            byte saveCount = 0;
            long crc32 = Integer.toUnsignedLong(0xaadfff);
            boolean patched = false;
            if (patch != null) {
                ParameterWrapper wrapper =  new ParameterWrapper(inputFileName, application, outputFileName, args, rawRomData, prefix, cover, crc32, saveCount);
                boolean result = (boolean) patch.invoke(null,wrapper);
                if (!result) return;
                rawRomData = wrapper.getRawRomData();
                prefix = wrapper.getPrefix();
                application = wrapper.getApplication();
                outputFileName = wrapper.getOutputFileName();
                args = wrapper.getArgs();
                cover = wrapper.getCover();
                saveCount = wrapper.getSaveCount();
                crc32 = wrapper.getCrc32();
                patched = true;
            }
            Debug.WriteLine("rawromdata: " + Arrays.toString(rawRomData));
            Debug.WriteLine("prefix: " + prefix);
            Debug.WriteLine("app: " + application);
            Debug.WriteLine("out: " + outputFileName);
            Debug.WriteLine("args: " + args);
            Debug.WriteLine("cover: " + cover);
            Debug.WriteLine("savecount: " + saveCount);
            Debug.WriteLine("crc32: " + crc32);
            Debug.WriteLine("patched: " + patched);

            Debug.WriteLine();
            Debug.WriteLine("'5gggg".replaceAll("'(\\d)","`$1"));
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }


}
