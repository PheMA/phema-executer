package org.phema.executer.valueSets;

import org.junit.jupiter.api.Test;
import org.phema.executer.interfaces.IValueSetRepository;
import org.phema.executer.valueSets.models.ValueSet;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by Luke Rasmussen on 12/12/17.
 */
public class FileValueSetRepositoryTest {
    @Test
    void initialize() throws Exception {
        FileValueSetRepository valueSets = new FileValueSetRepository();
        valueSets.initialize(new HashMap<String,String>() {{ put(FileValueSetRepository.Parameters.FilePath, "tests/resources/phenotype-packages/phema-bph/phema-bph-use-case.csv"); }});
        assertEquals(1, valueSets.getValueSets().size());
    }

    @Test
    void getByOID_NoResult() throws Exception {
        FileValueSetRepository valueSets = new FileValueSetRepository();
        valueSets.initialize(new HashMap<String,String>() {{ put(FileValueSetRepository.Parameters.FilePath, "tests/resources/phenotype-packages/phema-bph/phema-bph-use-case.csv"); }});
        ValueSet valueSet = valueSets.getByOID("1.2.3.4.5");
        assertNull(valueSet);
    }

    @Test
    void getByOID_Result() throws Exception {
        FileValueSetRepository valueSets = new FileValueSetRepository();
        valueSets.initialize(new HashMap<String,String>() {{ put(FileValueSetRepository.Parameters.FilePath, "tests/resources/phenotype-packages/phema-bph/phema-bph-use-case.csv"); }});
        ValueSet valueSet = valueSets.getByOID("2.16.840.1.113762.1.4.1034.65");
        assertEquals("2.16.840.1.113762.1.4.1034.65", valueSet.getOid());
    }
}
