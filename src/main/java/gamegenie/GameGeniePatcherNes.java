package gamegenie;

import java.util.HashMap;
import java.util.Map;

public class GameGeniePatcherNes {
    public static byte[] Patch(byte[] data, String code) throws GameGenieFormatException, GameGenieNotFoundException {
        assert code != null;
        code = code.toUpperCase().trim();
        if (code.equals("")) return data;

        byte[] result = data.clone();

        String binaryCode = code;
        for (char l : letterValues.keySet())
            binaryCode = binaryCode.replace(String.valueOf(l), letterValues.get(l));

        byte value, compare;
        long address;

        if (binaryCode.length() == 24) {
            if (binaryCode.charAt(8) != '0') throw new GameGenieFormatException(code);

            try {
                value = Byte.parseByte(new String(new char[]{binaryCode.charAt(0), binaryCode.charAt(5), binaryCode.charAt(6), binaryCode.charAt(7), binaryCode.charAt(20), binaryCode.charAt(1), binaryCode.charAt(2), binaryCode.charAt(3)}), 2);
                address = Long.parseLong(new String(new char[]{binaryCode.charAt(13), binaryCode.charAt(14), binaryCode.charAt(15), binaryCode.charAt(16), binaryCode.charAt(21), binaryCode.charAt(22), binaryCode.charAt(23), binaryCode.charAt(4), binaryCode.charAt(9), binaryCode.charAt(10), binaryCode.charAt(11), binaryCode.charAt(12), binaryCode.charAt(17), binaryCode.charAt(18), binaryCode.charAt(19)}), 2);
            }catch (Exception e){
                throw new GameGenieFormatException(code);
            }

            if (result.length <= 0x8000) {
                result[Math.toIntExact(address) % result.length] = value;
            } else {
                int pos = Math.toIntExact(address) % 0x2000;
                while (pos < result.length) {
                    result[pos] = value;
                    pos += 0x2000;
                }
            }
        } else if (binaryCode.length() == 32) {
            if (binaryCode.charAt(8) != '1') throw new GameGenieFormatException(code);

            try {
                value = Byte.parseByte(new String(new char[]{binaryCode.charAt(0), binaryCode.charAt(5), binaryCode.charAt(6), binaryCode.charAt(7), binaryCode.charAt(28), binaryCode.charAt(1), binaryCode.charAt(2), binaryCode.charAt(3)}), 2);
                address = Long.parseLong(new String(new char[]{binaryCode.charAt(13), binaryCode.charAt(14), binaryCode.charAt(15), binaryCode.charAt(16), binaryCode.charAt(21), binaryCode.charAt(22), binaryCode.charAt(23), binaryCode.charAt(4), binaryCode.charAt(9), binaryCode.charAt(10), binaryCode.charAt(11), binaryCode.charAt(12), binaryCode.charAt(17), binaryCode.charAt(18), binaryCode.charAt(19)}), 2);
                compare = Byte.parseByte(new String(new char[]{binaryCode.charAt(24), binaryCode.charAt(29), binaryCode.charAt(30), binaryCode.charAt(31), binaryCode.charAt(20), binaryCode.charAt(25), binaryCode.charAt(26), binaryCode.charAt(27)}), 2);
            }catch (Exception e) {
                throw new GameGenieFormatException(code);
            }


            boolean replaced = false;
            int pos = Math.toIntExact(address) % 0x2000;
            while (pos < result.length) {
                if (result[pos] == compare) {
                    result[pos] = value;
                    replaced = true;
                }
                pos += 0x2000;
            }
            if (!replaced) throw new GameGenieNotFoundException(code);
        } else throw new GameGenieFormatException(code);

        return result;
    }

    static Map<Character, String> letterValues = new HashMap<Character, String>() {{
        put('A', "0000");
        put('P', "0001");
        put('Z', "0010");
        put('L', "0011");
        put('G', "0100");
        put('I', "0101");
        put('T', "0110");
        put('Y', "0111");
        put('E', "1000");
        put('O', "1001");
        put('X', "1010");
        put('U', "1011");
        put('K', "1100");
        put('S', "1101");
        put('V', "1110");
        put('N', "1111");
    }};
}
