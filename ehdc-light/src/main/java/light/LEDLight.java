package light;
import com.pi4j.io.spi.SpiDevice;

/**
 * Created by Dawei on 9/5/2017.
 */
public class LEDLight extends LightSource{

    /**
     * LED light device is bound to SPI1.
     */
    private final SpiDevice spi;

    public LEDLight(SpiDevice spi) {
        this.spi = spi;
    }

    /**
     * TODO:
     * Set the gain of current for LED.
     * @param gain
     */
    public void setGain(double gain) {
        /**
         * Code will be like:
         * byte command[] = {$data};
         * spi.write(command)
         *
         */
    }

    /**
     * TODO:
     * Set resistance value.
     * @param res
     */
    public void setResistance(double res) {

    }

    /**
     * TODO:
     * @param i
     */
    @Override
    public void setIlluminance(double i) {

    }
}
