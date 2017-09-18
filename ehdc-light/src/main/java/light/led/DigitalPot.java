package light.led;

import com.pi4j.io.spi.SpiDevice;

import java.io.IOException;

/**
 * Created by Dawei on 9/18/2017.
 * The code for digital potentiometer MCP 4261.
 */
public class DigitalPot {

    private SpiDevice spi;

    DigitalPot(SpiDevice spi) {
        this.spi = spi;
    }

    public void setResistance(byte v) {
        try {
            spi.write(new byte[]{v});
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Send raw command for testing.
     * @param cmd
     * @return response.
     */
    public byte[] sendCommand(byte cmd[]) {
        byte recv[] = new byte[cmd.length];
        try {
            recv = spi.write(cmd);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return recv;
    }
}
