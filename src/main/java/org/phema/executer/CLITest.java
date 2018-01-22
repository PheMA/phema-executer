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
            File xmlFile = new File("tests/resources/phenotype-packages/phema-simple/phema-simple.xml");
            File configFile = new File("tests/resources/phenotype-packages/phema-simple/phema-simple.conf");
            Config config = ConfigFactory.parseFile(configFile);
            String hqmf = FileUtils.readFileToString(xmlFile);
            Parser parser = new Parser();
            IDocument document = parser.parse(hqmf);
            System.out.println(document);

            org.phema.executer.translator.HqmfToI2b2 translator = new org.phema.executer.translator.HqmfToI2b2();
            translator.translate(document, config);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
