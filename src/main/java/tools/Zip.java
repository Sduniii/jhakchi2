package tools;

import net.sf.sevenzipjbinding.*;
import net.sf.sevenzipjbinding.impl.OutItemFactory;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import net.sf.sevenzipjbinding.impl.RandomAccessFileOutStream;
import net.sf.sevenzipjbinding.util.ByteArrayStream;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Zip {

    /**
     * The callback provides information about archive items.
     */
    private static final class MyCreateCallback implements IOutCreateCallback<IOutItemAllFormats> {

        private Item[] items;

        public MyCreateCallback(Item[] items) {
            this.items = items;
        }

        public void setOperationResult(boolean operationResultOk)
                throws SevenZipException {
            // Track each operation result here
        }

        public void setTotal(long total) throws SevenZipException {
            // Track operation progress here
        }

        public void setCompleted(long complete) throws SevenZipException {
            // Track operation progress here
        }

        public IOutItemAllFormats getItemInformation(int index, OutItemFactory<IOutItemAllFormats> outItemFactory) {
            IOutItemAllFormats item = outItemFactory.createOutItem();

            if (items[index].getContent() == null) {
                // Directory
                item.setPropertyIsDir(true);
            } else {
                // File
                item.setDataSize((long) items[index].getContent().length);
            }

            item.setPropertyPath(items[index].getPath());

            return item;
        }

        public ISequentialInStream getStream(int i) throws SevenZipException {
            if (items[i].getContent() == null) {
                return null;
            }
            return new ByteArrayStream(items[i].getContent(), true);
        }
    }

    private static class MyExtractCallback implements IArchiveExtractCallback {
        private final IInArchive inArchive;
        private final Path extractPath;


        public MyExtractCallback(IInArchive inArchive, Path extractPath) {
            this.inArchive = inArchive;
            this.extractPath = extractPath;
        }

        @Override
        public ISequentialOutStream getStream(final int index, ExtractAskMode extractAskMode) throws SevenZipException {
            return data -> {
                String filePath = inArchive.getStringProperty(index, PropID.PATH);
                try {
                    Path path = Paths.get(extractPath.toString(), filePath);
                    if(!Files.exists(extractPath))
                        Files.createDirectory(extractPath);


                    if (!Files.exists(path.getParent())) {
                        Files.createDirectory(path.getParent());
                    }

                    if (Files.isDirectory(path)) {
                        Files.createDirectory(path);
                    } else {
                        if (!Files.exists(path)) {
                            Files.write(path, data);
                        } else {
                            if (Arrays.hashCode(data) != Arrays.hashCode(Files.readAllBytes(path))) {
                                Files.write(path, data);
                            }
                        }
                    }
                } catch (IOException e) {
                    System.err.println("IOException while extracting "
                            + filePath + ": " + e);
                }
                return data.length;
            };
        }

        @Override
        public void prepareOperation(ExtractAskMode extractAskMode)
                throws SevenZipException {
        }

        @Override
        public void setOperationResult(ExtractOperationResult
                                               extractOperationResult) throws SevenZipException {
        }

        @Override
        public void setCompleted(long completeValue) throws SevenZipException {
        }

        @Override
        public void setTotal(long total) throws SevenZipException {
        }
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            Debug.WriteLine("Usage: java CompressGeneric "
                    + "<archive> <archive-format> <count-of-files>");
            for (ArchiveFormat af : ArchiveFormat.values()) {
                if (af.isOutArchiveSupported()) {
                    Debug.WriteLine("Supported formats: " + af.getMethodName());
                }
            }
            return;
        }

        List<Item> items = Arrays.asList(new Zip().createTest());

        //compress(items, Paths.get(args[0]), 5);

        Path[] path = decompress(Paths.get(args[0]), Paths.get("decomp"));
        Debug.WriteLine(Arrays.toString(path));
    }

    public static void compress(Path file, Path archiveFile, int level) {
        try {
            Item item = new Item(file.toString(), Files.readAllBytes(file));
            List<Item> items = new ArrayList<>();
            items.add(item);
            compress(items, archiveFile, level);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void compress(Path[] files, Path archiveFile, int level) {
        try {
            List<Item> items = new ArrayList<>();
            for (Path file : files) {
                Item item = new Item(file.toString(), Files.readAllBytes(file));
                items.add(item);
            }
            compress(items, archiveFile, level);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void compress(List<Item> items, Path archiveFile, int level) {

        Item[] aItems = new Item[items.size()];
        aItems = items.toArray(aItems);

        boolean success = false;
        RandomAccessFile raf = null;
        IOutCreateArchive<IOutItemAllFormats> outArchive = null;
        ArchiveFormat archiveFormat = getFormat(archiveFile);
        if (archiveFormat == null) archiveFormat = ArchiveFormat.SEVEN_ZIP;
        try {
            raf = new RandomAccessFile(archiveFile.toFile(), "rw");

            // Open out-archive object
            outArchive = SevenZip.openOutArchive(archiveFormat);

            // Configure archive
            if (outArchive instanceof IOutFeatureSetLevel) {
                ((IOutFeatureSetLevel) outArchive).setLevel(level);
            }
            if (outArchive instanceof IOutFeatureSetMultithreading) {
                ((IOutFeatureSetMultithreading) outArchive).setThreadCount(2);
            }

            // Create archive
            outArchive.createArchive(new RandomAccessFileOutStream(raf),
                    aItems.length, new MyCreateCallback(aItems));

            success = true;
        } catch (SevenZipException e) {
            System.err.println("7z-Error occurs:");
            // Get more information using extended method
            e.printStackTraceExtended();
        } catch (Exception e) {
            System.err.println("Error occurs: " + e);
        } finally {
            if (outArchive != null) {
                try {
                    outArchive.close();
                } catch (IOException e) {
                    System.err.println("Error closing archive: " + e);
                    success = false;
                }
            }
            if (raf != null) {
                try {
                    raf.close();
                } catch (IOException e) {
                    System.err.println("Error closing file: " + e);
                    success = false;
                }
            }
        }
        if (success) {
            Debug.WriteLine(archiveFormat.getMethodName()
                    + " archive with " + items.size() + " item(s) created");
        }
    }

    public static Path[] decompress(Path file) {
        return decompress(file, file.getParent());
    }

    public static Path[] decompress(Path file, Path extractPath) {
        RandomAccessFile randomAccessFile = null;
        IInArchive inArchive = null;
        List<Path> itemsToExtract = new ArrayList<>();
        try {
            randomAccessFile = new RandomAccessFile(file.toFile(), "r");
            inArchive = SevenZip.openInArchive(null, // autodetect archive type
                    new RandomAccessFileInStream(randomAccessFile));

            for (int i = 0; i < inArchive.getNumberOfItems(); i++) {
                if (!(Boolean) inArchive.getProperty(i, PropID.IS_FOLDER)) {
                    itemsToExtract.add(Paths.get((String) inArchive.getProperty(i, PropID.PATH)));
                }
            }
            inArchive.extract(null, false, new MyExtractCallback(inArchive, extractPath));

        } catch (Exception e) {
            Debug.WriteLine("Error occurs: " + e);
            Path[] items = new Path[itemsToExtract.size()];
            return itemsToExtract.toArray(items);
        } finally {
            try {
                if (inArchive != null) {
                    inArchive.close();

                }
                if (randomAccessFile != null) {
                    randomAccessFile.close();
                }
            }catch (IOException e){
                Debug.WriteLine("Error at closing: " + e);
            }
        }
        Path[] items = new Path[itemsToExtract.size()];
        return itemsToExtract.toArray(items);
    }

    public Item[] createTest() {

        //     <root>
        //     |
        //     +- info.txt
        //     +- random-100-bytes.dump
        //     +- dir1
        //     |  +- file-in-a-directory1.txt
        //     +- dir2
        //        +- file-in-a-directory2.txt

        Item[] items = new Item[5];

        items[0] = new Item("info.txt", "This is the info");

        byte[] content = new byte[100];
        new Random().nextBytes(content);
        items[1] = new Item("random-100-bytes.dump", content);

        // dir1 doesn't have separate archive item
        items[2] = new Item("dir1" + File.separator + "file1.txt",
                "This file located in a directory 'dir'");

        // dir2 does have separate archive item
        items[3] = new Item("dir2" + File.separator, (byte[]) null);
        items[4] = new Item("dir2" + File.separator + "file2.txt",
                "This file located in a directory 'dir'");
        return items;
    }

    static class Item {
        private String path;
        private byte[] content;

        Item(String path, String content) {
            this(path, content.getBytes());
        }

        Item(String path, byte[] content) {
            this.path = path;
            this.content = content;
        }

        String getPath() {
            return path;
        }

        byte[] getContent() {
            return content;
        }
    }

    private static ArchiveFormat getFormat(Path archiveFile) {
        ArchiveFormat f = null;
        String format = FileTool.getExtension(archiveFile.getFileName().toString());
        try {
            f = ArchiveFormat.valueOf(format);
        } catch (Exception e) {

        }
        if (f != null) return f;

        for (ArchiveFormat af : ArchiveFormat.values()) {
            if (af.getMethodName().equalsIgnoreCase(format))
                return af;
        }
        return null;
    }

}
