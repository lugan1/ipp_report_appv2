package fragment.techtown.org.report_appv2.Util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class HexUtil {

    private static final char[] DIGITS_LOWER = {'0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    private static final char[] DIGITS_UPPER = {'0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    public static char[] encodeHex(byte[] data) {
        return encodeHex(data, true);
    }

    public static char[] encodeHex(byte[] data, boolean toLowerCase) {
        return encodeHex(data, toLowerCase ? DIGITS_LOWER : DIGITS_UPPER);
    }

    protected static char[] encodeHex(byte[] data, char[] toDigits) {
        if (data == null)
            return null;
        int l = data.length;
        char[] out = new char[l << 1];
        for (int i = 0, j = 0; i < l; i++) {
            out[j++] = toDigits[(0xF0 & data[i]) >>> 4];
            out[j++] = toDigits[0x0F & data[i]];
        }
        return out;
    }


    public static String encodeHexStr(byte[] data) {
        return encodeHexStr(data, true);
    }

    public static String encodeHexStr(byte[] data, boolean toLowerCase) {
        return encodeHexStr(data, toLowerCase ? DIGITS_LOWER : DIGITS_UPPER);
    }


    protected static String encodeHexStr(byte[] data, char[] toDigits) {
        return new String(encodeHex(data, toDigits));
    }

    public static String formatHexString(byte[] data) {
        return formatHexString(data, false);
    }

    public static String formatHexString(byte[] data, boolean addSpace) {
        if (data == null || data.length < 1)
            return null;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < data.length; i++) {
            String hex = Integer.toHexString(data[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex);
            if (addSpace)
                sb.append(" ");
        }
        return sb.toString().trim();
    }

    public static byte[] decodeHex(char[] data) {

        int len = data.length;

        if ((len & 0x01) != 0) {
            throw new RuntimeException("Odd number of characters.");
        }

        byte[] out = new byte[len >> 1];

        // two characters form the hex value.
        for (int i = 0, j = 0; j < len; i++) {
            int f = toDigit(data[j], j) << 4;
            j++;
            f = f | toDigit(data[j], j);
            j++;
            out[i] = (byte) (f & 0xFF);
        }

        return out;
    }


    protected static int toDigit(char ch, int index) {
        int digit = Character.digit(ch, 16);
        if (digit == -1) {
            throw new RuntimeException("Illegal hexadecimal character " + ch
                    + " at index " + index);
        }
        return digit;
    }


    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    public static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    public static String extractData(byte[] data, int position) {
        return HexUtil.formatHexString(new byte[]{data[position]});
    }

    public static String HexToDec(String hex) {
        long v = Long.parseLong(hex, 16);
        return String.valueOf(v);
    }

    //////little//////

    /**
     * byte 배열을 short 형으로 변환한다.
     **/

    public static final short byteToShort(byte[] buffer) {
        return byteToShort(buffer, 0);
    }

    public static final short byteToShort(byte[] buffer, int offset) {
        return (short) ((buffer[offset + 1] & 0xff) << 8 | (buffer[offset] & 0xff));
    }


//////////////////////////////////////////////////////////////////////////////////////////

    public static final int getInt(byte[] src, int offset, int length) {
        if ((src == null) || (length < 1) || (src.length < (length + offset))) {
            return -1;
        }

        int value = 0;

        for (int i = 0; i < length; i++) {
            value |= (src[offset++] & 0xFF);

            if (i < (length - 1)) {
                value <<= 8;
            }
        }
        return value;
    }

    public static String getHexString(byte[] src) {
        if (src == null || src.length < 1) {
            return null;
        }
        StringBuffer sb = new StringBuffer(src.length * 2);
        String hexNumber = null;
        for (int x = 0; x < src.length; x++) {
            hexNumber = "0" + Integer.toHexString(0xff & src[x]);
            sb.append(hexNumber.substring(hexNumber.length() - 2));
        }

        return sb.toString();
    }


    public static final int swap16(int value) {
        int word = (value & 0xFFFF);
        word = ((word << 8) | (word >> 8));

        return (word & 0xFFFF);
    }

    public static final long swap32(long value) {
        long word = 0;

        word |= (value & 0x000000FF) << 24;
        word |= (value & 0xFF000000) >> 24;
        word |= (value & 0x0000FF00) << 8;
        word |= (value & 0x00FF0000) >> 8;

        return (word & 0xFFFFFFFF);
    }

    public int one_chanel_To_Dec(byte[] input2Byte) {
////////////////////////////////////////////////

        byte[] one = {input2Byte[0]};
        byte[] two = {input2Byte[1]};
        //2Byte를 1Byte씩 반으로 쪼갬

        String strMost = toBinary(one);
        String strLast = toBinary(two);
        //1Byte씩 Binary String으로 컨버트


        String reviseMost = strMost.substring(4, 8);
        //0x0f 원칙에 따라 제일 뒤쪽 4bit를 걸러냄

        String reviseLast = "0000" + strLast;
        //제일 뒤쪽 1byte (Binary)에다가 0000 붙임

        String concat_BinaryString = reviseLast.concat(reviseMost);
        //수정한 Binary들을 하나로 붙임.

        //Log.v("BTST","strMost : "+reviseMost+"    strLast : "+reviseLast + " concat_BinaryString : "+concat_BinaryString);

        int digitNumber = 1;
        int sum = 0;
        String HexString = "";
        for (int i = 0; i < concat_BinaryString.length(); i++) {
            if (digitNumber == 1)
                sum += Integer.parseInt(concat_BinaryString.charAt(i) + "") * 8;
            else if (digitNumber == 2)
                sum += Integer.parseInt(concat_BinaryString.charAt(i) + "") * 4;
            else if (digitNumber == 3)
                sum += Integer.parseInt(concat_BinaryString.charAt(i) + "") * 2;
            else if (digitNumber == 4 || i < concat_BinaryString.length() + 1) {
                sum += Integer.parseInt(concat_BinaryString.charAt(i) + "") * 1;
                digitNumber = 0;
                if (sum < 10)
                    HexString = HexString + sum;
                else if (sum == 10)
                    HexString = HexString + "A";
                else if (sum == 11)
                    HexString = HexString + "B";
                else if (sum == 12)
                    HexString = HexString + "C";
                else if (sum == 13)
                    HexString = HexString + "D";
                else if (sum == 14)
                    HexString = HexString + "E";
                else if (sum == 15)
                    HexString = HexString + "F";
                sum = 0;
            }
            digitNumber++;
        }
        HexString = "0x" + HexString;
        //합친 Binary들을 Hex스트링으로 변환

        int Decimal = Integer.decode(HexString);
        //Hex스트링을 10진수 Int로 변환
        return Decimal;
    }


    public int EMGbyte_to_Int(byte[] inputByte) {
        int IntData = ((inputByte[1]+256 << 4)) | ((inputByte[0] & 0x0f));

        if (IntData >= 4096){
            IntData = IntData-4096;
        }

        //int IntData = ((inputByte[1] << 4)&0xFFF) | ((inputByte[0] & 0x0f));

        return IntData;
    }






    public static byte[] inToByteArray(final int integer){
        ByteBuffer buffer = ByteBuffer.allocate(Integer.SIZE/8);
        buffer.putInt(integer);
        buffer.order(ByteOrder.BIG_ENDIAN);
        return buffer.array();
    }

    String toBinary( byte[] bytes )
    {
        StringBuilder sb = new StringBuilder(bytes.length * Byte.SIZE);
        for( int i = 0; i < Byte.SIZE * bytes.length; i++ )
            sb.append((bytes[i / Byte.SIZE] << i % Byte.SIZE & 0x80) == 0 ? '0' : '1');
        return sb.toString();
    }

    byte[] fromBinary( String s )
    {
        int sLen = s.length();
        byte[] toReturn = new byte[(sLen + Byte.SIZE - 1) / Byte.SIZE];
        char c;
        for( int i = 0; i < sLen; i++ )
            if( (c = s.charAt(i)) == '1' )
                toReturn[i / Byte.SIZE] = (byte) (toReturn[i / Byte.SIZE] | (0x80 >>> (i % Byte.SIZE)));
            else if ( c != '0' )
                throw new IllegalArgumentException();
        return toReturn;
    }
}






