package org.phema.executer;

import org.apache.commons.io.FileUtils;
import org.phema.executer.hqmf.IDocument;
import org.phema.executer.hqmf.Parser;
import java.io.File;

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
        File xmlFile = new File("tests/resources/xmlfiles/phema-bph-use-case.xml");
//        File xmlFile = new File("/Users/lvr491/Documents/HL7Standards/HQMF/HQMFr2_1 2/EH/CMS73v3_R2.xml");
        String hqmf = null;
        try {
            hqmf = FileUtils.readFileToString(xmlFile);
            Parser parser = new Parser();
            IDocument document = parser.parse(hqmf);
            System.out.println(document);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
