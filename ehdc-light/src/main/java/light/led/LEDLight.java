package light.led;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.spi.SpiDevice;
import light.LightSource;

import java.io.IOException;

/**
 * Created by Dawei on 9/5/2017.
 *
 * The LED light intensity is controlled using an LED driver and a digital potentiometer.
 * Digital pot: MCP 4261
 *  - Connect to SPI1.
 * LED driver: TLC 59108
 *  - Connect to I2C.
 *
 *
 *  Deprecated versions:
 *  - MSP controller.
 *      Weired issues interfering with SPI ADC.
 */
public class LEDLight extends LightSource {

    /**
     * Digital pot is bound to SPI1.
     */
    private final LEDDriver LEDDriver;
    private final DigitalPot pot;

    /**
     * Initialize a LED device given assigned SPI and I2C bus.
     * @param spi Assigned spi device
     * @param i2c Assigned i2c bus
     */
    public LEDLight(SpiDevice spi, I2CBus i2c) {
        this.LEDDriver = new LEDDriver(i2c);
        this.pot = new DigitalPot(spi);
    }

    /**
     * Set the gain of current for LED.
     * @param gain
     */
    public void setGain(byte gain) {
        LEDDriver.setGain(gain);
    }

    /**
     * Set resistance value.
     * There are two resistors, and currently only use the second one and keep the first as 0.
     * @param res
     */
    public void setResistance(byte res) {
        pot.setResistance(res);
    }

    /**
     * TODO:
     *  This will be done after calibration.
     * @param i
     */
    @Override
    public void setIlluminance(double i) {

    }
}
