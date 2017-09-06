package main;

import org.apache.commons.cli.*;

/**
 * Created by Dawei on 11/16/2016.
 */
public class Main {

    public static void main(String[] args) throws ParseException {

        Options option = new Options();
        option.addOption("h", "help", false, "commandline help");
        option.addOption("s", "simulate", false, "simulate light intensity change");
        option.addOption("c", "calibrate", false, "calibrate LED based rig");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(option, args);
        if (cmd.hasOption("h")) {
            new HelpFormatter().printHelp("option", option);
            System.exit(0);
        }
        if (cmd.hasOption("s")) {
            String fileInput = cmd.getOptionValue("s");
            Simulator s = new Simulator(fileInput);
            s.run();
        }
        else if (cmd.hasOption("c")) {
            Calibrator c = new Calibrator();
            c.run();
        }
        else {
            System.out.println("Invalid option, see -h for help.");
            System.exit(0);
        }
    }
}
