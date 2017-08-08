package org.phema.executer;

import com.javapda.hqmf.QualityMeasureDocument;
import com.javapda.hqmf.QualityMeasureDocumentFactory;
import org.phema.executer.cts2.ValueSetRepository;
import org.phema.executer.cts2.models.ValueSet;
import org.phema.executer.i2b2.I2b2Configuration;
import org.phema.executer.util.HttpHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Luke Rasmussen on 7/19/17.
 */
public class CLITest {
    public static void main(String[] args) {
//        I2b2Configuration configuration = null;
//        try {
//            configuration = new I2b2Configuration(new URI("http://172.16.51.215:9090/i2b2/services/PMService/"), "demo", "demouser", "Demo", "i2b2demo",
//                    ExecutionReturnType.COUNTS, ExecutionMode.OPTIMIZED,
//                    "test", "test");
//            ProjectManagementService service = new ProjectManagementService(configuration);
//            service.login();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

//        I2b2Configuration configuration = null;
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

        File file = new File("tests/resources/xmlfiles/PhEMA-T2DM-Simple.xml");
        QualityMeasureDocument document = QualityMeasureDocumentFactory.create(file);
        ArrayList<ValueSet> valueSets = document.getAllElementValueSets();
    }
}
