package apps;

import Properties.Resources;
import config.ConfigIni;
import enums.ConsoleType;
import gui.controls.CustomButtonType;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import tools.Debug;
import tools.FileTool;
import tools.IpsPatcher;
import tools.Zip;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import static apps.AppTypeCollection.*;

public class MiniApplication implements IMenuElement {


    final static String DEFAULT_APP = "/bin/path-to-your-app";
    final String defaultReleaseDate = "1900-01-01";
    final String defaultPublisher = "UNKNOWN";
    final static char DEFAULT_PREFIX = 'Z';
    public static Image defaultCover = Resources.blank_app;
    public static Stage parentForm;
    public static Boolean needPatch;
    protected boolean hasUnsavedChanges = true;

    public static String gamesDirectory() {
        switch (ConfigIni.consoleType) {
            default:
            case NES:
            case Famicom:
                return Paths.get("games").toUri().getPath();
            case SNES:
            case SuperFamicom:
                return Paths.get("games_snes").toUri().getPath();
        }

    }

    public static String gamesCloverPath() {
        switch (ConfigIni.consoleType) {
            default:
            case NES:
            case Famicom:
                return "/usr/share/games/nes/kachikachi";
            case SNES:
            case SuperFamicom:
                return "/usr/share/games";
        }
    }

    @Getter
    private String code;
    @Getter
    private String googleSuffix;

    public final String gameGenieFileName = "gamegenie.txt";
    @Getter
    @Setter
    private Path gameGeniePath;
    @Getter
    private String gameGenie = "";

    public void setGameGenie(String value) {
        if (!gameGenie.equals(value)) hasUnsavedChanges = true;
        gameGenie = value;
    }


    @Getter
    private final Path gamePath, configPath, iconPath, smallIconPath;

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String value) {
        if (!name.equals(value)) hasUnsavedChanges = true;
        name = value;
    }

    @Getter
    protected String command;

    public void setCommand(String value) {
        if (!command.equals(value)) hasUnsavedChanges = true;
        command = value;
    }

    @Getter
    protected byte players;

    public void setPlayers(byte value) {
        if (players != value) hasUnsavedChanges = true;
        players = value;
    }

    @Getter
    protected boolean simultaneous;

    public void setSimultaneous(boolean value) {
        if (simultaneous != value) hasUnsavedChanges = true;
        simultaneous = value;
    }

    @Getter
    protected String releaseDate;

    public void setReleaseDate(String value) {
        if (!releaseDate.equals(value)) hasUnsavedChanges = true;
        releaseDate = value;
    }

    @Getter
    protected String publisher;

    public void setPublisher(String value) {
        if (!publisher.equals(value)) hasUnsavedChanges = true;
        publisher = value;
    }

    @Getter
    protected byte saveCount;

    public void setSaveCount(byte value) {
        if (saveCount != value) hasUnsavedChanges = true;
        saveCount = value;
    }



    protected MiniApplication() {
        gamePath = null;
        configPath = null;
        players = 1;
        simultaneous = false;
        releaseDate = defaultReleaseDate;
        publisher = defaultPublisher;
        command = "";
        saveCount = 0;
        iconPath = null;
        smallIconPath = null;
    }

    protected MiniApplication(Path path) throws IOException {
        this(path, false);
    }

    protected MiniApplication(Path path, boolean ignoreEmptyConfig) throws IOException {
        this.gamePath = path;
        code = FileTool.getNameWithoutExtension(path.getFileName().toString());
        name = code;
        configPath = Paths.get(path.toString(), code + ".desktop");
        iconPath = Paths.get(path.toString(), code + ".png");
        smallIconPath = Paths.get(path.toString(), code + "_small.png");
        players = 1;
        simultaneous = false;
        releaseDate = defaultReleaseDate;
        publisher = defaultPublisher;
        command = "";

        if (!Files.exists(configPath)) {
            if (ignoreEmptyConfig) return;
            throw new IOException("Invalid application directory: " + path);
        }
        List<String> configLines = Files.readAllLines(configPath);
        for (String line : configLines) {
            int pos = line.indexOf('=');
            if (pos <= 0) continue;
            String param = line.substring(0, pos).trim().toLowerCase();
            String value = line.substring(pos + 1).trim();
            switch (param) {
                case "exec":
                    command = value;
                    break;
                case "name":
                    name = value;
                    break;
                case "players":
                    players = Byte.parseByte(value);
                    break;
                case "simultaneous":
                    simultaneous = !value.equals("0");
                    break;
                case "releasedate":
                    releaseDate = value;
                    break;
                case "sortrawpublisher":
                    publisher = value;
                    break;
                case "savecount":
                    saveCount = Byte.parseByte(value);
                    break;
            }
        }
        gameGeniePath = Paths.get(path.toString(), gameGenieFileName);
        if (Files.exists(gameGeniePath))
            gameGenie = new String(Files.readAllBytes(gameGeniePath), StandardCharsets.UTF_8);

        hasUnsavedChanges = false;

    }

    public static MiniApplication fromDirectory(Path path) throws IOException {
        return fromDirectory(path, false);
    }

    public static MiniApplication fromDirectory(Path path, boolean ignoreEmptyConfig) throws IOException {
        try {
            if (!Files.isDirectory(path)) throw new IOException("Invalid app folder");

            List<Path> files = Files.list(path).collect(Collectors.toList());
            if (files.size() == 0) throw new IOException("Invalid app folder");

            List<String> config = Files.readAllLines(files.get(0));
            for (String line : config) {
                if (line.startsWith("Exec=")) {
                    String command = line.substring(5);
                    AppInfo app = GetAppByExec(command);
                    if (app != null) {
                        Class<?> theClass = app.clazz;
                        Constructor<?> constructor = theClass.getDeclaredConstructor(Path.class, boolean.class);
                        return (MiniApplication) constructor.newInstance(path, ignoreEmptyConfig);
                    }
                    break;
                }
            }
        } catch (IOException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return new MiniApplication(path, ignoreEmptyConfig);

    }

    public static MiniApplication importRom(Path inputFileName) {
        return importRom(inputFileName, null, null);
    }

    public static MiniApplication importRom(Path inputFileName, Path originalFileName) {
        return importRom(inputFileName, originalFileName, null);
    }

    public static MiniApplication importRom(Path inputFileName, byte[] rawRomData) {
        return importRom(inputFileName, null, rawRomData);
    }

    public static MiniApplication importRom(Path inputFileName, Path originalFileName, byte[] rawRomData) {
        try {
            String extension = FileTool.getExtension(inputFileName.toString()).toLowerCase();

            if (extension.equals(".desktop"))
                return ImportApp(inputFileName);

            if (rawRomData == null) // Maybe it's already extracted data?
                rawRomData = Files.readAllBytes(inputFileName); // If not, reading file

            if (originalFileName == null) // Original file name from archive
                originalFileName = inputFileName;

            char prefix = DEFAULT_PREFIX;
            String application = extension.length() > 2 ? ("/bin/" + extension.substring(1)) : DEFAULT_APP;
            String args = null;
            Image cover = defaultCover;
            byte saveCount = 0;
            long crc32 = crc32(rawRomData);
            Path outputFileName = Paths.get(inputFileName.toString().replaceAll("[^A-Za-z0-9()!\\[\\]\\.\\-]", "_").trim());

            // Trying to determine file type
            AppInfo appinfo = GetAppByExtension(extension);
            boolean patched = false;
            if (appinfo != null) {
                if (appinfo.defaultApps.length > 0)
                    application = appinfo.defaultApps[0];
                prefix = appinfo.prefix;
                cover = appinfo.defaultCover;
                Class<?> clazz = appinfo.clazz;
                Method patch = clazz.getDeclaredMethod("patch", PatchParameterWrapper.class);
                if (patch != null) {
                    PatchParameterWrapper wrapper = new PatchParameterWrapper(inputFileName, application, outputFileName, args, rawRomData, prefix, cover, crc32, saveCount);
                    boolean result = (boolean) patch.invoke(null, wrapper);
                    if (!result) return null;
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
            }

            if (!patched) {
                PatchParameterWrapper wrapper = new PatchParameterWrapper();
                wrapper.setRawRomData(rawRomData);
                wrapper.setInputFileName(inputFileName);
                wrapper.setCrc32(crc32);
                findPatch(wrapper);
            }

            String code = generateCode(crc32, prefix);
            Path gamePath = Paths.get(gamesDirectory(), code);
            Path romPath = Paths.get(gamePath.toString(), outputFileName.toString());
            if (Files.exists(gamePath)) {
                List<Path> files = Files.walk(gamePath).filter(Files::isRegularFile).collect(Collectors.toList());
                for (Path p : files) {
                    Files.delete(p);
                }

            }
            Files.createDirectory(gamePath);
            Files.write(romPath, rawRomData);
            MiniApplication game = new MiniApplication(gamePath, true);
            game.name = FileTool.getNameWithoutExtension(inputFileName.getFileName().toString());
            game.name = game.name.replaceAll(" ?(.*?)", "").trim();
            game.name = game.name.replaceAll(" ?[.*?]", "").trim();
            game.name = game.name.replace("_", " ").replace("  ", " ").trim();
            game.command = application + " " + gamesCloverPath() + "/" + code + "/" + outputFileName;
            if (args != null && !args.equals(""))
                game.command += " " + args;
            game.findCover(inputFileName, cover, crc32);
            game.saveCount = saveCount;
            game.save();

            MiniApplication app = MiniApplication.fromDirectory(gamePath);
            if (app instanceof ICloverAutofill)
                ((ICloverAutofill) app).tryAutofill(crc32);

            if (ConfigIni.compress)
                app.compress();

            return app;
        } catch (IOException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static MiniApplication ImportApp(Path path) throws IOException {

        if (!Files.exists(path)) // Archives are not allowed
            throw new IOException("Invalid app folder");

        String code = FileTool.getNameWithoutExtension(path.getFileName().toString()).toUpperCase();
        Path targetDir = Paths.get(gamesDirectory(), code);
        directoryCopy(path.getParent(), targetDir, true);
        return fromDirectory(targetDir);
    }

    private boolean save() throws IOException {
        if (!hasUnsavedChanges) return false;
        Debug.WriteLine(String.format("Saving application \"%s\" as %s", name, code));
        name = name == null || name.equals("") ? code : name;
        name = name.replaceAll("'(\\d)", "`1"); // Apostrophe + any number in game name crashes whole system. What. The. Fuck?

        byte[] toWrite = ("[Desktop Entry]\n" +
                "Type=Application\n" +
                "Exec=" + command + "\n" +
                "Path=/var/lib/clover/profiles/0/" + code + "\n" +
                "Name=" + name + "\n" +
                "Icon=" + gamesCloverPath() + "/" + code + "/" + code + ".png\n\n" +
                "[X-CLOVER Game]\n" +
                "Code=" + code + "\n" +
                "TestID=777\n" +
                "ID=0\n" +
                "Players=" + players + "\n" +
                "Simultaneous=" + (simultaneous ? 1 : 0) + "\n" +
                "ReleaseDate=" + (releaseDate == null || releaseDate.equals("") ? defaultReleaseDate : releaseDate) + "\n" +
                "SaveCount=" + saveCount + "\n" +
                "SortRawTitle=" + name.toLowerCase() + "\n" +
                "SortRawPublisher=" + (publisher == null || publisher.equals("") ? defaultPublisher : publisher).toUpperCase() + "\n" +
                "Copyright=jhakchi2 ©2017 Sduniii\n").getBytes();
        Files.write(configPath, toWrite);

        if (gameGenie != null || !gameGenie.equals(""))
            Files.write(gameGeniePath, gameGenie.getBytes());
        else if (Files.exists(gameGeniePath))
            Files.delete(gameGeniePath);

        hasUnsavedChanges = false;
        return true;
    }

    public void setImage(Image img) {
        setTheImage(img);
    }

    public Image getImage() {
        if (Files.exists(iconPath)) return new Image(iconPath.toString());
        else return null;
    }

    private void setTheImage(Image img) {
        setTheImage(img, false);
    }

    private void setTheImage(Image image, boolean eightBitCompression) {
        ImageView outImage = new ImageView(image);
        ImageView outImageSmall = new ImageView(image);

        int maxX = 204;
        int maxY = 204;

        if (ConfigIni.consoleType == ConsoleType.SNES || ConfigIni.consoleType == ConsoleType.SuperFamicom) {
            maxX = 228;
            maxY = 204;
        }
        if (image.getWidth() / image.getHeight() > (double) maxX / (double) maxY) {
            outImage.setFitWidth(maxX);
            outImage.setFitHeight((int) ((double) maxX * image.getHeight() / image.getWidth()));
        } else {
            outImage.setFitWidth((int) (maxY * image.getWidth() / image.getHeight()));
            outImage.setFitHeight(maxY);
        }

        int maxXsmall = 40;
        int maxYsmall = 40;
        if (image.getWidth() / image.getHeight() > (double) maxXsmall / (double) maxYsmall) {
            outImageSmall.setFitWidth(maxXsmall);
            outImageSmall.setFitHeight((int) ((double) maxXsmall * image.getHeight() / image.getWidth()));
        } else {
            outImageSmall.setFitWidth((int) (maxYsmall * image.getWidth() / image.getHeight()));
            outImageSmall.setFitHeight(maxYsmall);
        }

        try {
            BufferedImage buff = SwingFXUtils.fromFXImage(outImage.snapshot(null, null), null);
            ImageIO.write(buff, ".png", iconPath.toFile());
            buff = SwingFXUtils.fromFXImage(outImageSmall.snapshot(null, null), null);
            ImageIO.write(buff, ".png", smallIconPath.toFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void findCover(Path inputFileName, Image defaultCover) throws IOException {
        findCover(inputFileName, defaultCover, 0);
    }

    private boolean findCover(Path inputFileName, Image defaultCover, long crc32) throws IOException {
        // Trying to find cover file
        Image cover = null;
        Path artDirectory = Paths.get(".", "art");
        if (!Files.exists(artDirectory)) Files.createDirectory(artDirectory);
        if (inputFileName == null || inputFileName.toString().equals("")) {
            if (crc32 != 0) {
                List<Path> covers = Files.walk(artDirectory).filter(path -> path.toString().matches(String.format("%08X*.*", crc32))).collect(Collectors.toList());
                if (covers.size() > 0)
                    cover = new Image(covers.get(0).toString());
            }
            assert inputFileName != null;
            Path imagePath = Paths.get(artDirectory.toString(), FileTool.getNameWithoutExtension(inputFileName.getFileName().toString()) + ".png");
            if (Files.exists(imagePath))
                cover = new Image(imagePath.toString());
            imagePath = Paths.get(artDirectory.toString(), FileTool.getNameWithoutExtension(inputFileName.getFileName().toString()) + ".jpg");
            if (Files.exists(imagePath))
                cover = new Image(imagePath.toString());
            ;
            imagePath = Paths.get(inputFileName.getParent().toString(), FileTool.getNameWithoutExtension(inputFileName.getFileName().toString()) + ".png");
            if (Files.exists(imagePath))
                cover = new Image(imagePath.toString());
            imagePath = Paths.get(inputFileName.getParent().toString(), FileTool.getNameWithoutExtension(inputFileName.getFileName().toString()) + ".jpg");
            if (Files.exists(imagePath))
                cover = new Image(imagePath.toString());
        }
        if (cover == null) {
            setTheImage(defaultCover);
            return false;
        }
        setTheImage(cover);
        return true;
    }

    protected static boolean findPatch(PatchParameterWrapper wrapper) throws IOException {
        Path patch = null;
        Path patchesDirectory = Paths.get(".", "patches");
        if (!Files.exists(patchesDirectory)) Files.createDirectory(patchesDirectory);
        if (wrapper.getInputFileName() != null && !wrapper.getInputFileName().toString().equals("")) {
            if (wrapper.getCrc32() != 0) {
                List<Path> patches = Files.walk(patchesDirectory).filter(path1 -> path1.toString().matches(String.format("%08X*.*", wrapper.getCrc32()))).collect(Collectors.toList());
                if (patches.size() > 0)
                    patch = patches.get(0);
            }
            Path patchesPath = Paths.get(patchesDirectory.toString(), FileTool.getNameWithoutExtension(wrapper.getInputFileName().getFileName().toString()) + ".ips");
            if (Files.exists(patchesPath))
                patch = patchesPath;
            patchesPath = Paths.get(wrapper.getInputFileName().getParent().toString(), FileTool.getNameWithoutExtension(wrapper.getInputFileName().getFileName().toString()) + ".ips");
            if (Files.exists(patchesPath))
                patch = patchesPath;
        }

        if (patch != null && !patch.toString().equals("")) {
            if (!needPatch) {
                return false;
            } else {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, String.format(Resources.PatchQ, wrapper.getInputFileName().getFileName().toString()), ButtonType.CANCEL, CustomButtonType.IGNORE);
                alert.showAndWait();
                if (alert.getResult() == ButtonType.CANCEL)
                    needPatch = true;
                if (alert.getResult() == CustomButtonType.IGNORE)
                    return false;
            }
            wrapper.setPatch(patch);
            IpsPatcher.patch(wrapper);
            return true;
        }
        return false;
    }

    private static String generateCode(long crc32, char prefix) {
        return String.format("CLV-%c-%c%c%c%c%c",
                prefix,
                (char) ('A' + (crc32 % 26)),
                (char) ('A' + (crc32 >> 5) % 26),
                (char) ('A' + ((crc32 >> 10) % 26)),
                (char) ('A' + ((crc32 >> 15) % 26)),
                (char) ('A' + ((crc32 >> 20) % 26)));
    }

    public MiniApplication copyTo(Path path) throws IOException {
        Path targetDir = Paths.get(path.toString(), code);
        directoryCopy(gamePath, targetDir, true);
        return fromDirectory(targetDir);
    }

    private static long directoryCopy(Path sourceDirName, Path targetDir, boolean copySubDirs) throws IOException {
        long size = 0;
        if (!Files.exists(sourceDirName))
            throw new IOException("Source directory does not exist or could not be found: "
                    + sourceDirName);
        List<Path> dirs = Files.walk(sourceDirName).filter(Files::isDirectory).collect(Collectors.toList());
        if (!Files.exists(targetDir))
            Files.createDirectory(targetDir);
        List<Path> files = Files.list(sourceDirName).filter(Files::isRegularFile).collect(Collectors.toList());
        for (Path file : files) {
            Path tempPath = Paths.get(targetDir.toString(), file.toFile().getName());
            size += Files.copy(tempPath, new FileOutputStream(file.toFile()));
        }
        if (copySubDirs) {
            for (Path subdir : dirs) {
                Path tempPath = Paths.get(targetDir.toString(), subdir.toFile().getPath());
                size += directoryCopy(subdir, tempPath, true);
            }
        }
        return size;
    }

    public long size() {
        return size(null);
    }

    public long size(Path path) {
        try {
            if (path == null)
                path = gamePath;
            long size = 0;
            if (!Files.exists(path)) return 0;

            List<Path> paths = Files.walk(path).collect(Collectors.toList());
            for (Path p : paths) {
                size += p.toFile().length();
            }
            return size;
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }

    protected static long crc32(byte[] data) {
        Checksum checksum = new CRC32();
        checksum.update(data, 0, data.length);
        return checksum.getValue();
    }

    private Path[] compressPossible() throws IOException {
        if (!Files.exists(gamePath)) return new Path[0];
        List<Path> result = new ArrayList<>();
        String exec = command.replaceAll("['\\/\\\"]", " ") + " ";
        List<Path> files = Files.list(gamePath).filter(Files::isRegularFile).collect(Collectors.toList());
        for (Path file : files) {
            if (FileTool.getExtension(file.getFileName().toString()).toLowerCase().equals(".7z"))
                continue;
            if (FileTool.getExtension(file.getFileName().toString()).toLowerCase().equals(".zip"))
                continue;
            if (exec.contains(" " + file.getFileName().toString() + " "))
                result.add(file);
        }
        Path[] paths = new Path[result.size()];
        return result.toArray(paths);
    }

    private Path[] decompressPossible() throws IOException {
        if (!Files.exists(gamePath)) return new Path[0];
        List<Path> result = new ArrayList<>();
        String exec = command.replaceAll("['\\/\\\"]", " ") + " ";
        List<Path> files = Files.list(gamePath).filter(path -> path.toString().matches(".7z")).collect(Collectors.toList());
        for (Path file : files) {
            if (exec.contains(" " + file.getFileName() + " "))
                result.add(file);
        }
        Path[] path = new Path[result.size()];
        return result.toArray(path);
    }

    private void compress() throws IOException {
        for (Path filename : compressPossible()) {
            Path archName = Paths.get(filename + ".7z");
            Zip.compress(filename, archName, 5);
            Files.delete(filename);
            command = command.replace(filename.getFileName().toString(), archName.getFileName().toString());
        }
    }

    private void decompress() throws IOException {
        for (Path filename : decompressPossible()) {
            Path[] files = Zip.decompress(filename, gamePath);
            for (Path f : files) {
                command = command.replace(filename.getFileName().toString(), f.getFileName().toString());
            }
            Files.delete(filename);
        }
    }

    @Override
    public String toString() {
        return name;
    }
}

