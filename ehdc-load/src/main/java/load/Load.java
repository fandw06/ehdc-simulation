/**
 * Created by Dawei on 12/7/2016.
 */
package load;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

import java.io.IOException;
import java.util.Arrays;

/**
 * A class to adjust the load resistance.
 * Fitting curve:
 * R = 0.3817*x + 0.1269
 * x = (R - 0.1269)/0.3817
 *
 */
public class Load {

    private static byte ADDRESS_HIGH = 0b0101000;
    private static byte ADDRESS_LOW = 0b0101011;
    private static byte ADDRESS_NC = 0b0101010;
    private static byte DEFAULT_ADDRESS = ADDRESS_HIGH;

    private I2CBus bus;
    private I2CDevice ad5143;

    public Load() {
        try {
            bus = I2CFactory.getInstance(I2CBus.BUS_1);
            System.out.println("Connected to I2C bus successfully.");
            ad5143 = bus.getDevice(DEFAULT_ADDRESS);
            System.out.println("Connected to I2C device AD5143 successfully.");
        } catch (I2CFactory.UnsupportedBusNumberException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Set register value for a specific channel.
     * Using command 1
     * @param ch channel
     * @param data register
     * @throws IOException
     */
    public void setValue(int ch, byte data) throws IOException {
        byte cmd[] = new byte[2];
        cmd[0] = (byte)(0x10 + ch);
        cmd[1] = data;
        ad5143.write(cmd, 0, 2);
    }

    /**
     * Set the load value, unit is kOhm, ranging from around 0 ~ 100.
     *
     * @return
     */
    public void setLoad(int ch, double value) throws IOException {
        byte v = (byte)((value - 0.1269)/0.3817);
        setValue(ch, v);
    }

    private void readData() throws IOException {
        System.out.println("Read data...");
        for (int i = 0; i< 4; i++) {
            byte res[] = new byte[1];
            int b = ad5143.read(DEFAULT_ADDRESS, res, 0, 1);
            System.out.println("Data: " + Arrays.toString(res));
        }
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]) {
        Load load = new Load();
        try {
            int ch = Integer.parseInt(args[0]);
            double arg = Double.parseDouble(args[1]);
            load.setLoad(ch, arg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
