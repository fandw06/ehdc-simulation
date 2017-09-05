package storage;

import java.io.*;
import java.util.Arrays;

/**
 * Created by Dawei on 11/16/2016.
 */
public class Storage {

    /**
     * 16 ADC channels
     */
    private static final int CHANNELS = 16;

    /**
     * Buffer of 1K data array
     */
    private static final int ROWS = 1024;

    /**
     * Buffer size of data, default is 36KB
     */
    private static final int BUFFER_SIZE = ROWS * (CHANNELS + 2);

    /**
     * Number of data rows written
     */
    private int writtenSize;

    /**
     * Buffered data
     */
    private short data[] = new short[BUFFER_SIZE];
    private File dir;
    private int numOfFiles;
    private static String ROOT_PATH = "/home/pi/opt/";

    public Storage(String s) {
        this.writtenSize = 0;
        dir = new File(ROOT_PATH + s);
        if (!dir.mkdir()) {
            System.err.printf("Cannot create a directory!");
            System.exit(-1);
        }
        numOfFiles = 0;
    }

    public void add(int ts, short d[]) {

        if (d.length != CHANNELS) {
            System.err.println("Incorrect data array size. Cannot add.");
            return;
        }
        int writtenShorts = (CHANNELS + 2) * writtenSize;
        data[writtenShorts] = (short)(ts >> 16);
        data[writtenShorts + 1] = (short)(ts & 0xffff);
        for (int i = 0; i<CHANNELS; i++)
            data[writtenShorts + i + 2] = d[i];
        writtenSize++;
        if (writtenSize == ROWS) {
            int size = writtenSize * (CHANNELS + 2);
            writtenSize = 0;
            writeToBin(Arrays.copyOf(this.data, size));
        }
    }

    @Deprecated
    public void writeToCSV() {
        File file = new File(dir + "/" + String.format("%03d.csv", numOfFiles++));
        try {
            PrintWriter pw = new PrintWriter(file);

            for (int i = 0; i < writtenSize/CHANNELS; i++) {
                for (int j = 0; j< CHANNELS; j++) {
                    pw.print(String.format("%1.5f", data[i*CHANNELS + j]));
                    pw.print(", ");
                }
                pw.print("\n");
            }
            writtenSize = 0;
            pw.flush();
            pw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Write buffer into a new file.
     * @param d buffered data
     */
    private void writeToBin(final short d[]) {

        new Thread() {
            @Override
            public void run() {
                final File file = new File(dir + "/" + String.format("%03d.dat", numOfFiles++));
                BufferedOutputStream bo;
                try {
                    bo = new BufferedOutputStream(new FileOutputStream(file));
                    for (Short s : d) {
                        byte b[] = new byte[2];
                        b[1] = (byte)(s & 0xff);
                        b[0] = (byte)((s >> 8) & 0xff);
                        bo.write(b);
                    }
                    bo.flush();
                    bo.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public void writeRemains() {
        int size = writtenSize * (CHANNELS + 2);
        writeToBin(Arrays.copyOf(this.data, size));
    }

}
