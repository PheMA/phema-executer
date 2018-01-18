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
//        I2B2ExecutionConfiguration configuration = null;
//        try {
//            configuration = new I2B2ExecutionConfiguration(new URI("http://172.16.51.215:9090/i2b2/services/PMService/"), "demo", "demouser", "Demo", "i2b2demo",
//                    ExecutionReturnType.COUNTS, ExecutionMode.OPTIMIZED,
//                    "test", "test");
//            ProjectManagementService service = new ProjectManagementService(configuration);
//            service.login();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

//        I2B2ExecutionConfiguration configuration = null;
//        try {
//            ValueSetRepository repository = new ValueSetRepository(new HttpHelper());
//            HashMap<String, String> parameters = new HashMap<String, String>();
//            parameters.put(ValueSetRepository.Parameters.BaseUri, "http://172.16.51.130:8080/value-sets/");
//            repository.Initialize(parameters);
//            ArrayList<ValueSet> valueSets = repository.Search("test");
//            System.out.println(valueSets.size());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

//        File file = new File("tests/resources/xmlfiles/PhEMA-T2DM-Simple.xml");
//        QualityMeasureDocument document = QualityMeasureDocumentFactory.create(file);
//        ArrayList<ValueSet> valueSets = document.getAllElementValueSets();
//        File xmlFile = new File("tests/resources/xmlfiles/PhEMA-T2DM-Simple.xml");
//        File xmlFile = new File("tests/resources/xmlfiles/CMS_146_HQMF_R2.xml");
//        File xmlFile = new File("/Users/lvr491/Development/MSPCTRA/H3/EP_CMS138v4_NQF0028_PREV_Tobacco/CMS138v4.xml");
//        File xmlFile = new File("/Users/lvr491/Documents/HL7Standards/HQMF/HQMFr2_1 2/EH/CMS73v3_R2.xml");
        try {
            File xmlFile = new File("tests/resources/phenotype-packages/phema-simple/phema-simple.xml");
            //File valueSets = new File("tests/resources/phenotype-packages/phema-bph/phema-bph-use-case.csv");
            //CSVParser csvParser = CSVParser.parse(valueSets, Charset.defaultCharset(), CSVFormat.DEFAULT);
            File configFile = new File("tests/resources/phenotype-packages/phema-simple/phema-simple.conf");
            String hqmf = null;

            Config config = ConfigFactory.parseFile(configFile);

            hqmf = FileUtils.readFileToString(xmlFile);
            Parser parser = new Parser();
            IDocument document = parser.parse(hqmf);
            System.out.println(document);

            org.phema.executer.translator.HqmfToI2b2.translate(document, config);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
