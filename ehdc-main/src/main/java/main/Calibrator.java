package main;
import adc.SpiADC;
import com.pi4j.io.spi.SpiChannel;
import com.pi4j.io.spi.SpiDevice;
import com.pi4j.io.spi.SpiFactory;
import com.pi4j.io.gpio.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import light.LEDLight;
import load.Load;

/**
 * Calibrator for LED lighting source rig.
 *
 * Created by Dawei on 9/5/2017.
 */
public class Calibrator {

    private SpiDevice spiLED;
    private SpiDevice spiADC;
    private double gainList[];
    LEDLight led;
    private SpiADC adc;

    private final static int DELAY = 30;
    private final static int CH_LED = 11;
    private final static String PATH = "/home/pi/opt/calibrate/";

    public Calibrator() {
        gainList = new double[]{1.0, 2.0, 3.0, 4.0};
        try {
            // Initialize a spi device for ADC.
            spiADC = SpiFactory.getInstance(SpiChannel.CS0,
                    SpiDevice.DEFAULT_SPI_SPEED, // default spi speed 1 MHz
                    SpiDevice.DEFAULT_SPI_MODE); // default spi mode 0
            // Initialize a spi device for LED rig.
            spiLED = SpiFactory.getInstance(SpiChannel.CS1,
                    SpiDevice.DEFAULT_SPI_SPEED, // default spi speed 1 MHz
                    SpiDevice.DEFAULT_SPI_MODE); // default spi mode 0
            adc = new SpiADC(spiADC);
            led = new LEDLight(spiLED);
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

    public void run() {
        for (double g : gainList) {
            led.setGain(g);
            double data[] = new double[65536];
            for (int i = 0; i < 65536; i++) {
                led.setResistance(i);
                // Keep the state for some time to let the sensor response.
                try {
                    Thread.sleep(DELAY);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    double val = adc.readChannel(CH_LED);
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
