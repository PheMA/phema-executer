package org.phema.executer.valueSets;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.phema.executer.interfaces.IValueSetRepository;
import org.phema.executer.valueSets.models.Member;
import org.phema.executer.valueSets.models.ValueSet;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

/**
 * Created by Luke Rasmussen on 12/12/17.
 */
public class FileValueSetRepository implements IValueSetRepository {
    private File valueSetFile = null;
    private ArrayList<ValueSet> valueSets = new ArrayList<>();

    public static class Parameters {
        public static String FilePath = "FilePath";
    }

    @Override
    public void initialize(HashMap<String, String> parameters) throws Exception {
        valueSetFile = new File(parameters.get(Parameters.FilePath));
        CSVParser parser = CSVParser.parse(valueSetFile, Charset.defaultCharset(), CSVFormat.DEFAULT.withHeader().withIgnoreSurroundingSpaces(true));
        ValueSet currentValueSet = new ValueSet();
        for (CSVRecord csvRecord : parser) {
            if (!currentValueSet.getOid().equals(csvRecord.get("value_set_oid"))) {
                currentValueSet = new ValueSet(csvRecord.get("value_set_oid"), csvRecord.get("value_set_name"));
                valueSets.add(currentValueSet);
            }
            currentValueSet.addMember(new Member(
                    csvRecord.get("code"),
                    csvRecord.get("description"),
                    csvRecord.get("code_system"),
                    csvRecord.get("code_system_version"),
                    csvRecord.get("code_system_oid")));
        }
    }

    @Override
    public ArrayList<ValueSet> search(String searchTerm) {
        return null;
    }

    @Override
    public ValueSet getByOID(String oid) {
        Optional<ValueSet> result = valueSets.stream().filter(x -> x.getOid().equals(oid)).findFirst();
        if (result.isPresent()) {
            return result.get();
        }

        return null;
    }

    public ArrayList<ValueSet> getValueSets() {
        return valueSets;
    }
}
