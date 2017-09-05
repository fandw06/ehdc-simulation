package adc;

/**
 * Convert a byte/int value to a 8-bit binary string with leading zeros.
 */
public class Util {

    private Util(){};
    public static String tpBinary(int v) {
        return String.format("%8s", Integer.toBinaryString(v & 0xff)).replace(" ", "0");
    }

    public static String tpBinary(byte v) {
        return String.format("%8s", Integer.toBinaryString(v & 0xff)).replace(" ", "0");
    }
}
