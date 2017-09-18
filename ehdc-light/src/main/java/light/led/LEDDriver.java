package light.led;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import java.io.IOException;

/**
 * Created by Dawei on 9/18/2017.
 * The code for LED driver TLC 59108.
 *
 */
public class LEDDriver {

    private I2CBus bus;
    private I2CDevice i2c;
    private static final byte SLAVE_ADDRESS = (byte)0b1000000;
    private static final byte READ = 0b01;
    private static final byte WRITE = 0b00;
    private static final byte RESET = (byte)0b10010110;

    // Register map
    // The first 3 bits are incremental control, default to 000 (single write)
    private static final byte MODE1 = 0x00;
    private static final byte MODE2 = 0x01;
    private static final byte GRPPWM = 0x0A;
    private static final byte LEDOUT0 = 0x0C;
    private static final byte LEDOUT1 = 0x0D;
    private static final byte ALLCALL = 0x11;
    private static final byte IREF = 0x12;
    private static final byte EFLAG = 0x13;

    LEDDriver(I2CBus bus) {
        this.bus = bus;
        try {
            i2c = bus.getDevice(SLAVE_ADDRESS);
            System.out.println("Connected to I2C device TLC 59108 (LED driver) successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
        setState();
    }

    /**
     * Set the state of 8 LEDs, see page 25.
     * Now set LEDs controlled by both individual and group registers.
     */
    public void setState() {
        try {
            byte command[] = {LEDOUT0, (byte)0xff};
            i2c.write(command);
            command = new byte[]{LEDOUT1, (byte)0xff};
            i2c.write(command);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Set 256 level brightness of all LEDs by writing to GRPPWM.
     * @param b
     */
    public void setBrightness(byte b) {
        try {
            byte command[] = {GRPPWM, b};
            i2c.write(command);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Set output gain.
     * @param g
     */
    public void setGain(byte g) {
        try {
            byte command[] = {IREF, g};
            i2c.write(command);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
