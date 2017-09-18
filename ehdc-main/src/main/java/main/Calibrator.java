package main;
import adc.SpiADC;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.spi.SpiChannel;
import com.pi4j.io.spi.SpiDevice;
import com.pi4j.io.spi.SpiFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Scanner;

import light.led.LEDLight;
import load.Load;

/**
 * Calibrator for LED lighting source rig.
 *
 * Created by Dawei on 9/5/2017.
 */
public class Calibrator {


    private LEDLight led;
    private SpiADC adc;

    private byte gainList[];
    private final static int DELAY = 1000;
    private final static int CH_LED = 11;
    private final static String PATH = "/home/pi/opt/calibrate/";

    public Calibrator() {
        gainList = new byte[]{1};
        try {
            // Initialize a spi device for ADC.
            SpiDevice spiADC = SpiFactory.getInstance(SpiChannel.CS0,
                    SpiDevice.DEFAULT_SPI_SPEED, // default spi speed 1 MHz
                    SpiDevice.DEFAULT_SPI_MODE); // default spi mode 0
            adc = new SpiADC(spiADC);
            adc.setDefault();

            // Initialize a spi device for LED rig,
            // including an SPI device for digital pot and an I2C bus for driver.
            SpiDevice spiPot = SpiFactory.getInstance(SpiChannel.CS1,
                    SpiDevice.DEFAULT_SPI_SPEED, // default spi speed 1 MHz
                    SpiDevice.DEFAULT_SPI_MODE); // default spi mode 0
            I2CBus i2cBus = null;
            try {
                i2cBus = I2CFactory.getInstance(I2CBus.BUS_1);
                System.out.println("Connected to I2C bus successfully.");
            } catch (I2CFactory.UnsupportedBusNumberException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            led = new LEDLight(spiPot, i2cBus);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Set default load resistance
        Load load = new Load();
        try {
            load.setValue(0, (byte)0x01);
            load.setValue(1, (byte)0x01);
            System.out.println("Load value is set to be 100KOhm");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Automatically calibrate by looping gains and resistance, and write results in separate
     * csv files.
     */
    public void run() {
        for (byte g : gainList) {
            led.setGain(g);
            double data[] = new double[256];
            for (int i = 0; i <= 0xff ; i++) {
                led.setResistance((byte)i);
                // Keep the state for some time to let the sensor response.
                try {
                    Thread.sleep(DELAY);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    double val = adc.readChannel(CH_LED);
                    System.out.println(val);
                    data[i] = val;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            writeToCSV(data, "Gain_" + g + ".csv");
            System.out.println("Simulation for gain = " + g + " is completed!");
        }
        System.out.println("All simulations complete!");
    }

    public void test() {
        System.out.println("Testing...");

        System.out.println("Tested");
    }

    public void testLoad() {
        Scanner reader = new Scanner(System.in);  // Reading from System.in

        for (int i = 0; i <= 255; i++) {
        //    byte b1 = reader.nextByte();
        //    byte b2 = reader.nextByte();
        //    byte b3 = reader.nextByte();
        //    byte[] r = led.sendCommand(new byte[]{0, (byte)0xcc});
        //    byte[] r = led.sendCommand(new byte[]{0, (byte)i});
        //    System.out.println("Raw: " + Arrays.toString(byte2int(r)));
            //System.out.println("Shifted: " + Arrays.toString(byte2int(shiftLeft(r, 7))));
            /*
            r = led.sendCommand(new byte[]{2});
            System.out.println(Arrays.toString(byte2int(shiftLeft(r, 7))));
            r = led.sendCommand(new byte[]{3});
            System.out.println(Arrays.toString(byte2int(shiftLeft(r, 7))));
            */
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            try {
                double val = adc.readChannel(CH_LED);
                System.out.println(val);
            } catch (IOException e) {
                e.printStackTrace();
            }
            /*
            byte recv[] = led.sendCommand(v);
            System.out.println(Arrays.toString(recv));
            recv = led.sendCommand(v);
            System.out.println(Arrays.toString(recv));

            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            try {
                double val = adc.readChannel(CH_LED);
                System.out.println(val);
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            */
        }
    }

    public int[] byte2int(byte b[]) {
        int i[] = new int[b.length];
        for(int j = 0; j < b.length; j++) {
            if (b[j] < 0)
                i[j] = b[j] + 256;
            else
                i[j] = b[j];
        }
        return i;
    }

    public byte[] shiftLeft(byte b[], int n) {
        byte[] s = new byte[b.length];
        for (int i = 0; i< b.length; i++) {
            int t = b[i];
            if (t < 0)
                t += 256;
            int ts = t<<n | t >>> (8-n);
            s[i] = (byte)(ts & 0xff);
        }
        return s;
    }

    private void writeToCSV(double data[], String name) {
        File file = new File(PATH + name);
        try {
            PrintWriter pw = new PrintWriter(file);
            for (int i = 0; i < data.length; i++) {
                pw.print(String.format("%1.5f", data[i]));
                pw.print(",\n");
            }
            pw.flush();
            pw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
