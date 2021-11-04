import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class Helper {
    public static byte[] concat(byte[] first, byte[] second) {
        byte[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    public static int readHeader(byte[] bytes) throws IOException {
        System.out.println(Arrays.toString(bytes));
        byte[] size = new byte[4];
        System.arraycopy(bytes, 8, size, 0, 4);
        return byteArrayBigToInt(size);
    }

    public static int readMessageSizeHeader(byte[] data) throws IOException {
        byte[] size = new byte[2];
        System.arraycopy(data, 2, size, 0, 2);
        return byteArraySmallToInt(size);
    }

    public static final int byteArraySmallToInt(byte[] bytes) {
        return (bytes[0] & 0xFF) << 8 | (bytes[1] & 0xFF);
    }

    public static final int byteArrayBigToInt(byte[] bytes) {
        return bytes[0] << 24 | (bytes[1] & 0xFF) << 16 | (bytes[2] & 0xFF) << 8 | (bytes[3] & 0xFF);
    }

    public static byte[] createHeader(String message, boolean startTimeCode, boolean startName, boolean finish, boolean file, int size) throws UnsupportedEncodingException {
        byte[] array = new byte[12];
        array[0] = (byte) (1);
        array[1] = (byte) (1);
        int messageSize = message.getBytes().length;
        array[2] = (byte) (messageSize / (int) Math.pow(2, 8));
        array[3] = (byte) (messageSize % (int) Math.pow(2, 8));
        array[4] = (byte) ((booleanToInt(startTimeCode) << 7) + (booleanToInt(startName) << 6)
                + (booleanToInt(finish) << 5) + (booleanToInt(file) << 4));
        byte[] hashcode = intToByteArray(message.hashCode());
        for (int i = 5; i < 8; i++) {
            array[i] = hashcode[i - 5];
        }
        byte[] sizeBytes = intToByteArrayBig(size);
        for (int i = 8; i < 12; i++) {
            array[i] = sizeBytes[i - 8];
        }
        return array;
    }

    public static final byte[] intToByteArray(int value) {
        return new byte[]{
                (byte) (value >>> 16),
                (byte) (value >>> 8),
                (byte) value};
    }

    public static final byte[] intToByteArrayBig(int value) {
        return new byte[]{
                (byte) (value >>> 24),
                (byte) (value >>> 16),
                (byte) (value >>> 8),
                (byte) value};
    }

    public static int booleanToInt(boolean b) {
        return b ? 1 : 0;
    }

}
