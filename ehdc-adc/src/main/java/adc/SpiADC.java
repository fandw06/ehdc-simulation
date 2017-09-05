package adc;
import com.pi4j.io.spi.SpiDevice;

import java.io.IOException;

/**
 * AD7490 operation
 */
public class SpiADC {

    /**
     * 16 channels for AD7490.
     */
    public static final short ADC_CHANNEL_COUNT = 16;
    /**
     * 0: don't write; 1: write
     */
    private static final byte WRITE = (byte)(1 << 7);
    /**
     * Combined with Shadow, default 0
     */
    private static final byte SEQ = (1 << 6);
    /**
     * Default 1, normal mode
     */
    private static final byte PM1 = (1 << 1);
    /**
     * Default 1, normal mode
     */
    private static final byte PM0 = 1;
    /**
     * Combined with SEQ, default 0
     */
    private static final byte SHADOW = (byte)(1 << 7);
    /**
     * 0: tri-state; 1: weak
     */
    private static final byte WEAK = (1 << 6);
    /**
     * 0: 0~2ref; 1:0~ref
     */
    private static final byte RANGE = (1 << 5);
    /**
     * 0: complimentary; 1: straight coding
     */
    private static final byte CODING = (1 << 4);

    private static final double Vref = 2.5;

    private static final int FULL_SCALE = (1 << 12) - 1;

    public byte getControlLow() {
        return controlLow;
    }

    public byte getControlHigh() {
        return controlHigh;
    }

    /**
     * Four power modes
     */
    public enum PowerMode {
        NORMAL,
        FULL_SHUTDOWN,
        AUTO_SHUTDOWN,
        STANDBY
    }

    /**
     * Four sequence modes
     */
    public enum SequenceMode {
        NONE,
        SHADOW,
        NON_INTERRUPT,
        CONTINUOUS
    }

    /**
     * 0~2Vr or 0~Vr
     */
    public enum Range {
        FULL,
        HALF,
    }

    /**
     * Two's complement coding or straight binary
     */
    public enum Coding {
        COMPLEMENT,
        STRAIGHT
    }

    /**
     * Tri state or weak
     */
    public enum State {
        TRI,
        WEAK
    }
    /**
     * Low control byte
     */
    private byte controlLow;

    /**
     * High control byte
     */
    private byte controlHigh;

    private final SpiDevice spi;

    /**
     * Create a new AD7490 device with a given spi device
     * @param spi Spi device for AD7490
     */
    public SpiADC(SpiDevice spi) {
        controlHigh = 0;
        controlLow = 0;
        this.spi = spi;
    }

    public void setPowerMode(PowerMode pm) {
        controlHigh = (byte)(controlHigh & 0b11111100);
        switch(pm) {
            case NORMAL:
                controlHigh = (byte)(controlHigh | PM0 | PM1);
                break;
            case FULL_SHUTDOWN:
                controlHigh = (byte)(controlHigh | PM1);
                break;
            case AUTO_SHUTDOWN:
                controlHigh = (byte)(controlHigh | PM0);
                break;
            case STANDBY:
                break;
            default:
                return;
        }
    }

    public void setSequenceMode(SequenceMode sm) {
        controlHigh = (byte)(controlHigh & 0b10111111);
        controlLow = (byte)(controlLow & 0b01111111);
        switch(sm) {
            case NONE:
                break;
            case SHADOW:
                controlLow = (byte)(controlLow | SHADOW);
                break;
            case NON_INTERRUPT:
                controlHigh = (byte)(controlHigh | SEQ);
                break;
            case CONTINUOUS:
                controlLow = (byte)(controlLow | SHADOW);
                controlHigh = (byte)(controlHigh | SEQ);
                break;
            default:
                return;
        }
    }

    public void setRange(Range r) {
        controlLow = (byte)(controlLow & 0b11011111);
        switch(r) {
            case FULL:
                break;
            case HALF:
                controlLow = (byte)(controlLow | RANGE);
                break;
            default:
                return;
        }
    }

    public void setCoding(Coding c) {
        controlLow = (byte)(controlLow & 0b11101111);
        switch(c) {
            case COMPLEMENT:
                break;
            case STRAIGHT:
                controlLow = (byte)(controlLow | CODING);
                break;
            default:
                return;
        }
    }

    public void setState(State s) {
        controlLow = (byte)(controlLow & 0b10111111);
        switch(s) {
            case TRI:
                break;
            case WEAK:
                controlLow = (byte)(controlLow | WEAK);
                break;
            default:
                return;
        }
    }

    public void setDefault() {
        this.writeEnable();
        this.setCoding(Coding.STRAIGHT);
        this.setPowerMode(PowerMode.NORMAL);
        this.setRange(Range.FULL);
        this.setSequenceMode(SequenceMode.NONE);
        this.setState(State.TRI);
    }

    public void writeEnable() {
        controlHigh = (byte)(controlHigh | WRITE);
    }

    public void writeDisable() {
        controlHigh = (byte)(controlHigh & 0b01111111);
    }

    public void resetControlWord() {
        controlLow = 0;
        controlHigh = 0;
    }

    public double readChannel(int channel) throws IOException, ConversionException{
        spi.write(new byte[]{(byte)(controlHigh | (channel << 2)) , controlLow});
        byte[] result = spi.write(new byte[]{(byte)(controlHigh | (channel << 2)) , controlLow});
     //   System.out.println(Util.tpBinary(result[0]));
     //   System.out.println(Util.tpBinary(result[1]));
        int ch = (result[0]>>>4) & 0xf;
        int vv = (result[0] & 0b00001111)*256 + result[1];
        if (result[1] < 0)
            vv += 256;
        if (ch != channel)
            throw new ConversionException("Returned channel number is incorrect." +
                                    String.format(" Target chanel is %d, but returned %d", channel, ch));
        else {
            if ((controlLow & 0b00100000) == 0)
                vv *= 2;
            return (double)(vv) * Vref / 4095.0;
        }
    }

    public double[] readAll() throws IOException, ConversionException{
        double result[] = new double[ADC_CHANNEL_COUNT];

        spi.write(new byte[]{controlHigh, controlLow});
        byte[] res = spi.write(new byte[]{(byte)(controlHigh | (1 << 2)) , controlLow});
        int ch = (res[0]>>>4) & 0xf;
        int vv = (res[0] & 0b00001111)*256 + res[1];
        if (res[1] < 0)
            vv += 256;
        if (ch != 0)
            throw new ConversionException("Returned channel number is incorrect." +
                    String.format(" Target chanel is %d, but returned %d", 0, ch));
        else {
            if ((controlLow & 0b00100000) == 0)
                vv *= 2;
            result[0] = (double)(vv) * Vref / 4095.0;
        }
        for (int i = 1; i < ADC_CHANNEL_COUNT; i++) {
            res = spi.write(new byte[]{(byte)(controlHigh | ((i+1) << 2)) , controlLow});
            ch = (res[0]>>>4) & 0xf;
            vv = (res[0] & 0b00001111)*256 + res[1];
            if (res[1] < 0)
                vv += 256;
            if (ch != i)
                throw new ConversionException("Returned channel number is incorrect." +
                        String.format(" Target chanel is %d, but returned %d", i, ch));
            else {
                if ((controlLow & 0b00100000) == 0)
                    vv *= 2;
                result[i] = (double)(vv) * Vref / 4095.0;
            }
        }
        return result;
    }

    public short[] readAllRaw() throws IOException, ConversionException{
        short result[] = new short[ADC_CHANNEL_COUNT];

        spi.write(new byte[]{controlHigh, controlLow});
        byte[] res = spi.write(new byte[]{(byte)(controlHigh | (1 << 2)) , controlLow});
        short ch = (short)((res[0]>>>4) & 0xf);
        short vv = (short)((res[0] & 0b00001111)*256 + res[1]);
        if (res[1] < 0)
            vv += 256;
        if (ch != 0)
            throw new ConversionException("Returned channel number is incorrect." +
                    String.format(" Target chanel is %d, but returned %d", 0, ch));
        result[0] = vv;

        for (int i = 1; i < ADC_CHANNEL_COUNT; i++) {
            res = spi.write(new byte[]{(byte)(controlHigh | ((i+1) << 2)) , controlLow});
            ch = (short)((res[0]>>>4) & 0xf);
            vv = (short)((res[0] & 0b00001111)*256 + res[1]);
            if (res[1] < 0)
                vv += 256;
            if (ch != i)
                throw new ConversionException("Returned channel number is incorrect." +
                        String.format(" Target chanel is %d, but returned %d", i, ch));

            result[i] = vv;

        }
        return result;
    }

    public double[] readChannels(int channels[]) throws IOException, ConversionException{
        double result[] = new double[channels.length];
        for (int i = 0; i < channels.length; i++)
            result[channels[i]] = readChannel(channels[i]);
        return result;
    }

    public double getVref() {
        if ((controlLow & 0b00100000) == 0)
            return 2* Vref;
        return Vref;
    }

    public int getFllScale() {
        return FULL_SCALE;
    }

    public double[] calibrate(short[] raw) {
        double result[] = new double[raw.length];
        double reference = getVref();
        for (int i = 0; i<raw.length; i++)
            result[i] = reference * raw[i] / FULL_SCALE;

        return result;
    }
}
