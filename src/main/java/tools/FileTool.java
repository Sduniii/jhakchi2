package tools;

public class FileTool {

    public static String getExtension(String file){
        return file.substring(file.lastIndexOf('.'));
    }

    public static String getNameWithoutExtension(String file){
        if(!file.contains(".")) return file;
        return file.substring(0,file.lastIndexOf('.'));
    }
}
