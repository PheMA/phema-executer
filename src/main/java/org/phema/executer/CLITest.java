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
public class CLITest {
    public static void main(String[] args) {
        try {
            File xmlFile = new File("tests/resources/phenotype-packages/phema-bph/phema-bph-use-case.xml");
            File configFile = new File("tests/resources/phenotype-packages/phema-bph/phema-bph-use-case.conf");

            ConsoleProgressObserver consoleLogger = new ConsoleProgressObserver();

            long startTime = System.nanoTime();
            org.phema.executer.translator.HqmfToI2b2 translator = new org.phema.executer.translator.HqmfToI2b2();
            translator.addObserver(consoleLogger);
            translator.execute(configFile, xmlFile);
            long endTime = System.nanoTime();
            System.out.println("Elapsed execution time in seconds: " + (endTime - startTime) / 1000000000.0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
