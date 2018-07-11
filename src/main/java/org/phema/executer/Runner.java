package org.phema.executer;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.io.FileUtils;
import org.phema.executer.hqmf.IDocument;
import org.phema.executer.hqmf.Parser;
import org.phema.executer.interfaces.IValueSetRepository;
import org.phema.executer.valueSets.FileValueSetRepository;

import java.io.File;
import java.nio.charset.Charset;
import java.util.HashMap;

/**
 * Created by Luke Rasmussen on 7/19/17.
 */
public class Runner {
    public static void main(String[] args) {
        try {
            if (args == null || args.length == 0) {
                printUsage();
                return;
            }

            File configFile = new File(args[0]);
            if(configFile.isDirectory()) {
                printUsage();
                return;
            }

            ConsoleProgressObserver consoleLogger = new ConsoleProgressObserver();

            long startTime = System.nanoTime();
            org.phema.executer.translator.HqmfToI2b2 translator = new org.phema.executer.translator.HqmfToI2b2();
            translator.setLogger(consoleLogger);
            translator.execute(configFile);
            long endTime = System.nanoTime();
            System.out.printf("Elapsed execution time in seconds: %.2f\r\n", ((endTime - startTime) / 1000000000.0));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void printUsage() {
        Version version = new Version();
        System.out.println(String.format("PhEMA Executer v%s - Usage", version.toString()));
        System.out.println();
        System.out.println("java -jar phema-executer-lib.jar [config_file]");
        System.out.println();
        System.out.println("Options:");
        System.out.println("config_file - The .conf file (including the relative or absolute path)\r\n  which specifies the phenotype to run");
        System.out.println();
        System.out.println();
        System.out.println("Example: java -jar phema-executer-lib.jar ./test/test-phenotype.conf");
        System.out.println();

    }
}
