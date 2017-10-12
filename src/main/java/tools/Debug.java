package tools;

import lombok.Getter;

public class Debug {
    @Getter
    private static Boolean isEnabled = true;

    public static void log(Exception e) {
        if (isEnabled) {
            e.printStackTrace();
        }
    }

    public static void WriteLine(Object s){
        if(isEnabled) System.out.println(s);
    }
    public static void WriteLine(){
        if(isEnabled) System.out.println();
    }
}
