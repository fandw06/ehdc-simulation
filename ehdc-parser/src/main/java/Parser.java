import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import java.io.*;

/**
 * Created by Dawei on 11/17/2016.
 */
public class Parser {

    public static final String inputDir = "input/";
    public static final String outputDir = "calibrated/";

    public static void generateCSVFromDirectory(String sessionName) {
        File session = new File(inputDir + sessionName);
        File calibrated = new File(outputDir + sessionName + ".csv");
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(calibrated);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        File[] files = session.listFiles();
        for (File file : files) {
            try {
                DataInputStream reader = new DataInputStream(new FileInputStream(file));
                while (reader.available() > 0) {
                    int ts = reader.readInt();
                    pw.printf(ts + ",");
                    for (int i = 0; i< 16; i++) {
                        Short curr = reader.readShort();
                        double v = curr* 5.0/4095.0;
                        pw.printf(String.format("%1.5f", v) + ",");
                    }
                    pw.println();
                }
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        pw.close();
    }

    public static void generateCSVFromTarGz(String tarGz) {

        try {
            TarArchiveInputStream tarInput = new TarArchiveInputStream(
                    new GzipCompressorInputStream(
                            new FileInputStream(inputDir + tarGz)));
            TarArchiveEntry currentEntry = tarInput.getNextTarEntry();
            String dirName = currentEntry.getName();
            File output = new File(outputDir + dirName.substring(0, dirName.length() - 1) + ".csv");
            PrintWriter pw = new PrintWriter(output);
            System.out.println(output.getAbsolutePath());
            File dir = new File(outputDir + dirName);
            dir.mkdir();
            System.out.println(dir.getName());
            currentEntry = tarInput.getNextTarEntry();
            while (currentEntry != null) {
                byte buffer[] = new byte[2048];
                FileOutputStream fos = new FileOutputStream(outputDir + currentEntry.getName());
                int n;
                while (-1 != (n = tarInput.read(buffer, 0, 2048)))
                    fos.write(buffer, 0, n);
                fos.flush();
                fos.close();
                currentEntry = tarInput.getNextTarEntry();
            }
            File[] files = dir.listFiles();
            for (File file : files) {
                try {
                    DataInputStream reader = new DataInputStream(new FileInputStream(file));
                    while (reader.available() > 0) {
                        int ts = reader.readInt();
                        pw.printf(ts + ",");
                        for (int i = 0; i< 16; i++) {
                            Short curr = reader.readShort();
                            double v = curr* 5.0/4095.0;
                            pw.printf(String.format("%1.5f", v) + ",");
                        }
                        pw.println();
                    }
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String name = "0215.tar.gz";
        Parser.generateCSVFromTarGz(name);
        name = "0216.tar.gz";
        Parser.generateCSVFromTarGz(name);
    }
}
