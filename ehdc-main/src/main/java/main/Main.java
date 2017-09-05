package main;

import org.apache.commons.cli.*;
import storage.Storage;

import java.util.Arrays;

/**
 * Created by Dawei on 11/16/2016.
 */
public class Main {

    public static void main(String[] args) throws ParseException {

        Collector c = new Collector();
        Options option = new Options();
        option.addOption("s", "session", true, "assign a session name");
        option.addOption("h", "help", false, "commandline help");
        option.addOption("i", "influx", false, "enable influxdata");
        option.addOption("c", "console output", false, "enable console output");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(option, args);
        if (cmd.hasOption("s")) {
            String name = cmd.getOptionValue("s");
            c.setStorage(new Storage(name));
        }
        else
            c.setStorage(new Storage(String.valueOf(System.currentTimeMillis())));
        if (cmd.hasOption("h"))
            new HelpFormatter().printHelp("option", option);
        if (cmd.hasOption("i"))
            c.enableInflux();
        if (cmd.hasOption("c"))
            c.enableConsoleOutput();
        c.run();
    }
}
