package tools;

import java.lang.reflect.Array;

public class ArrayTool {

    public static void copy(byte[] source, int startIndex, byte[] dest, int destStartIndex, int length){
        int index = destStartIndex;
        for(int i = startIndex; i < length; i++){
            dest[index] = source[i];
            index++;
        }
    }

    public static boolean contains(byte[] source, byte w){
        for(byte b : source){
            if(b == w){
                return true;
            }
        }
        return false;
    }
}
