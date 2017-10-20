package gui.controller;

import Properties.Resources;
import fel.Fel;
import fel.FelException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.Setter;
import lombok.experimental.var;
import usb.lowapi.UsbDevices;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class WorkerController {

    @FXML
    Label label;
    @FXML
    ProgressBar progressBar;

    private Fel fel;
    private String mod = "mod_hakchi";
    @Setter
    private List<String> hmodsInstall;
    private Path tempDirectory = Paths.get(".", "temp");
    private Path fes1Path = Paths.get(Paths.get(".","data").toString(),"fes1.bin");
    private Path ubootPath = Paths.get(Paths.get(".","data").toString(),"ubbot.bin");

    final short vid = (short) 0x1F3A;
    final short pid = (short) 0xEFE8;
    private Path kernelDumpPath = Paths.get(tempDirectory.toString(),"kernel.img");

    public void flashKernel(Stage parent) throws Exception {
        int progress = 0;
        int maxProgress = 115 + (mod == null || mod.equals("") ? 0 : 110) +
                ((hmodsInstall != null && hmodsInstall.size() > 0) ? 150 : 0);
        Path tempKernelPath = Paths.get(tempDirectory.toString(), "kernel.img");
        List<String> hmods = hmodsInstall;
        hmodsInstall = null;
        waitForFelFromThread();
        progress += 5;
        progressBar.setProgress(progress);

        if (Files.exists(tempDirectory))
            Files.delete(tempDirectory);
        Files.createDirectory(tempDirectory);

        byte[] kernel;
        if (mod != null && !mod.equals("")) {
//            if (!DoKernelDump(tempKernelPath, maxProgress, progress))
//                return;
//            progress += 80;
//            kernel = CreatePatchedKernel(tempKernelPath);
//            progress += 5;
//            SetProgress(progress, maxProgress);
        } else
            kernel = Files.readAllBytes(kernelDumpPath);
        var size = calcKernelSize(kernel);
        if (size > kernel.Length || size > Fel.kernel_max_size)
            throw new Exception(Resources.InvalidKernelSize + " " + size);

        size = (size + Fel.sector_size - 1) / Fel.sector_size;
        size = size * Fel.sector_size;
        if (kernel.Length != size) {
            var newK = new byte[size];
            Array.Copy(kernel, newK, kernel.Length);
            kernel = newK;
        }

        fel.WriteFlash(Fel.kernel_base_f, kernel,
                delegate(Fel.CurrentAction action, string command)
        {
            switch (action) {
                case Fel.CurrentAction.RunningCommand:
                    SetStatus(Resources.ExecutingCommand + " " + command);
                    break;
                case Fel.CurrentAction.WritingMemory:
                    SetStatus(Resources.UploadingKernel);
                    break;
            }
            progress++;
            SetProgress(progress, maxProgress);
        }
            );
        var r = fel.ReadFlash((UInt32) Fel.kernel_base_f, (UInt32) kernel.Length,
                delegate(Fel.CurrentAction action, string command)
        {
            switch (action) {
                case Fel.CurrentAction.RunningCommand:
                    SetStatus(Resources.ExecutingCommand + " " + command);
                    break;
                case Fel.CurrentAction.ReadingMemory:
                    SetStatus(Resources.Verifying);
                    break;
            }
            progress++;
            SetProgress(progress, maxProgress);
        }
            );
        if (!kernel.SequenceEqual(r))
            throw new Exception(Resources.VerifyFailed);

        hmodsInstall = hmods;
        if (hmodsInstall != null && hmodsInstall.Count() > 0) {
            Memboot(maxProgress, progress); // Lets install some mods
        } else {
            var shutdownCommand = "shutdown";
            SetStatus(Resources.ExecutingCommand + " " + shutdownCommand);
            fel.RunUbootCmd(shutdownCommand, true);
            if (Directory.Exists(tempDirectory))
                Directory.Delete(tempDirectory, true);

            SetStatus(Resources.Done);
            SetProgress(maxProgress, maxProgress);
        }
    }

    private boolean waitForFelFromThread() throws Exception {
        label.setText("Wait");
        if (fel != null)
            fel.close();
        Runnable runnable = () -> {
            try {
                while(!Fel.deviceExists(vid,pid)) Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        };
        Thread t = new Thread(runnable);
        t.start();
        while (t.isAlive()) Thread.sleep(1000);
        if (!Files.exists(fes1Path)) throw new FileNotFoundException(fes1Path + " not found");
        if (!Files.exists(ubootPath)) throw new FileNotFoundException(ubootPath + " not found");
        fel.setFes1Bin(Files.readAllBytes(fes1Path));
        fel.setuBootBin(Files.readAllBytes(ubootPath));
        if (!fel.open(vid, pid))
            throw new FelException("Can't open device");
        fel.initDram(true);
        return true;
    }

    private boolean WaitForDevice(short vid, short pid, Stage owner) throws InterruptedException, IOException {
        if (Fel.deviceExists(vid, pid)) return true;
        Pane root = FXMLLoader.load(getClass().getResource("/fxml/waitFes.fxml"));
        Scene scene = new Scene(root);
        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(owner);
        stage.setScene(scene);
        stage.showAndWait();
        return true;
    }
}
