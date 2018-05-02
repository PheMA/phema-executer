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
                System.out.println("PhEMA Executer - Usage");
                System.out.println("  java -jar phema-executer-lib.jar path_to_config");
                System.out.println("    path_to_config - The path to the .conf file which specifies the phenotype to run");
                System.out.println();
                return;
            }

            File configFile = new File(args[0]);

            ConsoleProgressObserver consoleLogger = new ConsoleProgressObserver();

            long startTime = System.nanoTime();
            org.phema.executer.translator.HqmfToI2b2 translator = new org.phema.executer.translator.HqmfToI2b2();
            translator.addObserver(consoleLogger);
            translator.execute(configFile);
            long endTime = System.nanoTime();
            System.out.println("Elapsed execution time in seconds: " + (endTime - startTime) / 1000000000.0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
