package org.phema.executer;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.commons.io.FileUtils;
import org.phema.executer.cts2.ValueSetRepository;
import org.phema.executer.hqmf.IDocument;
import org.phema.executer.hqmf.Parser;
import org.phema.executer.util.HttpHelper;
import org.phema.executer.valueSets.models.ValueSet;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Luke Rasmussen on 1/23/18.
 */
public class ExtractValueSets {
    public static void main(String[] args) {
        try {
            ValueSetRepository repository = new ValueSetRepository(new HttpHelper(true));
            HashMap<String, String> parameters = new HashMap<String, String>();
            parameters.put(ValueSetRepository.Parameters.BaseUri, "http://172.16.51.130:8080/value-sets/");
            repository.initialize(parameters);

            File xmlFile = new File("tests/resources/phenotype-packages/phema-bph/phema-bph-use-case.xml");
            File configFile = new File("tests/resources/phenotype-packages/phema-bph/phema-bph-use-case.conf");
            Config config = ConfigFactory.parseFile(configFile);
            String hqmf = FileUtils.readFileToString(xmlFile);
            Parser parser = new Parser();
            IDocument document = parser.parse(hqmf);
            System.out.println(document);

            ArrayList<String> valueSetOids = document.getAllValueSetOids();
            ArrayList<ValueSet> valueSets = new ArrayList<>(valueSetOids.size());
            for (String oid : valueSetOids) {
                ValueSet valueSet = repository.getByOID(oid);
                if (valueSet != null) {
                    valueSets.add(valueSet);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
