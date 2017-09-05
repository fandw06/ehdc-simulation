package main;

import adc.SpiADC;
import com.pi4j.io.spi.SpiChannel;
import com.pi4j.io.spi.SpiDevice;
import com.pi4j.io.spi.SpiFactory;
import com.pi4j.io.gpio.*;
import load.Load;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;
import storage.Storage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Collector {

    private SpiDevice spi;
    private SpiADC adc;
    // 20Hz
    private static final int INTERVAL = 49;

    // InfluxData settings
    private static String serverIP = "128.143.24.101";
    private String dbName;
    private InfluxDB influxDB;
    private boolean influxEnabled;

    // Console output
    private boolean consoleOutput;
    // Storage settings
    private Storage storage;
    private String sessionName;

    private boolean running;
    private static final int THR = 5000;
    private long startTime = -1;

    public Collector() {
        try {
            // Initialize a spi device.
            spi = SpiFactory.getInstance(SpiChannel.CS0,
                    SpiDevice.DEFAULT_SPI_SPEED, // default spi speed 1 MHz
                    SpiDevice.DEFAULT_SPI_MODE); // default spi mode 0
            // Initialize a spiadc device.
            adc = new SpiADC(spi);
            adc.setDefault();
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
        running = false;
    }

    public void run() {
        final GpioController gpio = GpioFactory.getInstance();
        final GpioPinDigitalInput input = gpio.provisionDigitalInputPin(RaspiPin.GPIO_00, PinPullResistance.PULL_DOWN);
        final GpioPinDigitalOutput led = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_04, PinState.LOW);
        led.setShutdownOptions(true, PinState.LOW);

        for (int i = 0; i< 5; i++) {
            led.high();
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            led.low();
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Press button to start running.
        int counter = 0;
        while (!running) {
            while (input.isHigh()) {
                counter++;
                if (counter > THR)
                    running = true;
            }
        }
        if (consoleOutput)
            System.out.println("Data collecting is started.");

        // Check if the button is pressed to stop data collecting.
        new Thread() {
            @Override
            public void run() {
                int c = 0;
                while (running) {
                    while (input.isHigh()) {
                        c++;
                        if (c > THR)
                            running = false;
                    }
                }
            }
        }.start();

        // Blink LED to indicate the program is running.
        new Thread() {
            @Override
            public void run() {
                while (running) {
                    led.high();
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    led.low();
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();

        if (consoleOutput) {
            System.out.println("--------------------------------------Data Collector--------------------------------------");
            for (int i = 0; i < 16; i++)
                System.out.print(String.format(" | %04d ", i));

            System.out.println();
        }
        while(running) {
            try {
                int ts = 0;
                if (startTime == -1)
                    startTime = System.currentTimeMillis();
                else
                    ts = (int)(System.currentTimeMillis() - startTime);
                short raw[] = adc.readAllRaw();
                double value[] = adc.calibrate(raw);
                storage.add(ts, raw);
                if (isInfluxEnabled())
                    writeInfluxDB(value);
                if (consoleOutput) {
                    for (int i = 0; i < 16; i++)
                        System.out.print(String.format(" | %1.3f", value[i]));
                    System.out.print("\r");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(INTERVAL);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            running = input.isLow();
        }
        storage.writeRemains();
        if (consoleOutput)
            System.out.println("\nData collecting is stopped.");
        System.exit(0);
    }

    public void setServer(String serverIP) {
        this.serverIP = serverIP;
        influxDB = InfluxDBFactory.connect("http://" + serverIP +":8086", "root", "root");
    }

    public boolean isInfluxEnabled() {
        return influxEnabled;
    }

    public void enableInflux() {
        this.influxEnabled = true;
    }

    public void disableInflux() {
        this.influxEnabled = false;
    }

    public void enableConsoleOutput() {
        this.consoleOutput = true;
    }

    public void disableConsoleOutput() {
        this.consoleOutput = false;
    }

    public void writeInfluxDB(final double data[]) {
        new Thread(new Runnable() {

            public void run() {
                Map<String, Object> fields = new HashMap<String, Object>();
                for (int i = 0; i< data.length; i++) {
                    fields.put("ch" + i, data[i]);
                }
                Point point = Point.measurement("ad")
                        .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                        .fields(fields)
                        .build();
                if (isInfluxEnabled())
                    influxDB.write("collector", "autogen", point);
            }
        }).start();

    }

    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
    }

    public void setStorage(Storage storage) {
        this.storage = storage;
    }
}
