package org.phema.executer.translator;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigOrigin;
import org.phema.executer.exception.PhemaUserException;
import org.phema.executer.hqmf.IDocument;
import org.phema.executer.hqmf.v2.DataCriteria;
import org.phema.executer.hqmf.v2.Document;
import org.phema.executer.i2b2.I2B2ExecutionConfiguration;
import org.phema.executer.i2b2.OntologyService;
import org.phema.executer.i2b2.ProjectManagementService;
import org.phema.executer.interfaces.IValueSetRepository;
import org.phema.executer.models.DescriptiveResult;
import org.phema.executer.models.i2b2.Concept;
import org.phema.executer.util.HttpHelper;
import org.phema.executer.valueSets.FileValueSetRepository;
import org.phema.executer.valueSets.models.ValueSet;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Luke Rasmussen on 12/7/17.
 */
public class HqmfToI2b2 {
    public static void translate(IDocument document, Config config) throws PhemaUserException {
        if (document == null) {
            System.out.println("The document is null - exiting");
            return;
        }

        if (config == null) {
            System.out.println("The configuration is null - exiting");
        }

        if (!(document instanceof Document)) {
            System.out.println("The document is not HQMF v2, and so is unsupported");
            return;
        }

        Document hqmfDocument = (Document)document;

        // Build the i2b2-specific configuration information from our config object
        I2B2ExecutionConfiguration configuration = new I2B2ExecutionConfiguration();
        DescriptiveResult result = configuration.loadFromConfiguration(config);
        if (!result.isSuccess()) {
            throw new PhemaUserException(result);
        }

        // Create an instance of the project management service, and ensure that we are
        // properly authenticated
        ProjectManagementService pmService = new ProjectManagementService(configuration, new HttpHelper());
        result = pmService.login();
        if (!result.isSuccess()) {
            throw new PhemaUserException(result);
        }

        OntologyService ontService = new OntologyService(pmService, configuration, new HttpHelper());

        // Build the full list of value set repositories that we are configured to use
        // for this phenotype.
        ArrayList<IValueSetRepository> valueSetRepositories = null;
        try {
            valueSetRepositories = processValueSets(config);
        } catch (Exception e) {
            throw new PhemaUserException("There was an error when trying to load the configuration details for your value set repositories.  Please double-check that you have specified all of the necessary configuration information, and that the file is formatted correctly.", e);
        }

        // Now get the mapping configuration for the value sets.
        ValueSetToI2b2Ontology valueSetTranslator = new ValueSetToI2b2Ontology();
        try {
            valueSetTranslator.initialize(config, ontService);
        } catch (Exception e) {
            throw new PhemaUserException("There was an error when trying to set up the rules to map from a value set to i2b2 concepts.  Please double-check that you have correctly specified the mapping rules.", e);
        }

        // Get the list of value sets from the document and find those that exist in the repository.  Make a note
        // of those we can't find.
        ArrayList<String> valueSetOids = document.getAllValueSetOids();
        ArrayList<String> unmappedValueSets = new ArrayList<>();
        ArrayList<ValueSet> valueSets = new ArrayList<>(valueSetOids.size());
        for (String oid : valueSetOids) {
            ValueSet valueSet = findValueSet(oid, valueSetRepositories);
            if (valueSet != null) {
                valueSets.add(valueSet);
            }
            else {
                unmappedValueSets.add(oid);
            }
        }

        // Translate the value sets into the i2b2 ontology.  What we end up with is a mapping between a value set and
        // a list of i2b2 Concepts.
        HashMap<ValueSet, ArrayList<Concept>> valueSetConceptMap = new HashMap<>();
        for (ValueSet valueSet : valueSets) {
            valueSetConceptMap.put(valueSet, valueSetTranslator.translate(valueSet));
        }

        ArrayList<DataCriteria> dataCriteria = hqmfDocument.getDataCriteria();
//        // Age has special handling
//        DataCriteria[] birthdateCriteria = dataCriteria.stream().filter(x -> x.getDefinition().equals("patient_characteristic_birthdate")).toArray(DataCriteria[]::new);
//        if (birthdateCriteria != null && birthdateCriteria.length > 0) {
//            for (DataCriteria birthdateCriterion : birthdateCriteria) {
//                System.out.println(birthdateCriterion);
//            }
//        }
    }

    /**
     * Local helper function to find a value set from a list of value set repositories.  It assumes that the list of
     * repositories is in order of preference, so it stops searching after the first is found.
     * @param oid
     * @param repositories
     * @return The ValueSet definition, or null if no value set was found.
     */
    private static ValueSet findValueSet(String oid, List<IValueSetRepository> repositories) {
        for (IValueSetRepository repository : repositories) {
            ValueSet valueSet = repository.getByOID(oid);
            if (valueSet != null) {
                return valueSet;
            }
        }

        return null;
    }

    /**
     * Creates a FileValueSetRepository for a given configuration object
     * @param config ConfigObject containing all information needed to create the FileValueSetRepository
     * @return FileValueSetRepository created from the ConfigObject
     * @throws Exception
     */
    private static FileValueSetRepository createFileValueSetRepository(ConfigObject config) throws Exception {
        if (!config.containsKey("format")) {
            throw new Exception("A value set definition must contain a 'format' field of 'CSV' or 'VSAC'");
        }

        if (!config.containsKey("path")) {
            throw new Exception("A value set definition must contain a 'path' field");
        }

        ConfigOrigin origin = config.origin();
        URL configFilePath = origin.url();
        File configFile = new File(configFilePath.getFile());
        String baseDirectory = configFile.getParent();
        FileValueSetRepository repository = new FileValueSetRepository();
        repository.initialize(new HashMap<String, String>(){{ put(FileValueSetRepository.Parameters.FilePath,
                (new File(baseDirectory, config.get("path").unwrapped().toString())).getAbsolutePath()); }});
        return repository;
    }

    /**
     * Create all configured value sets for the phenotype definition, given a configuration object
     * @param config The Config object that contains all of the value set definitions
     * @return ArrayList&lt;IValueSetRepository&gt; containing all configured value sets
     * @throws Exception
     */
    private static ArrayList<IValueSetRepository> processValueSets(Config config) throws Exception {
        ArrayList<IValueSetRepository> valueSetRepositories = new ArrayList<>();

        List<? extends ConfigObject> valueSetObjects = config.getObjectList("execution.valueSets");
        if (valueSetObjects == null || valueSetObjects.size() == 0) {
            throw new Exception("You must specify at least one value set for the phenotype to execute.");
        }

        for (ConfigObject valueSetObject : valueSetObjects) {
            if (!valueSetObject.containsKey("type")) {
                throw new Exception("A value set definition must contain a 'type' field of 'File' or 'CTS2'");
            }

            String valueSetType = valueSetObject.get("type").unwrapped().toString();
            switch (valueSetType) {
                case "File":
                    IValueSetRepository repository = createFileValueSetRepository(valueSetObject);
                    valueSetRepositories.add(repository);
                    break;
                default:
                    throw new Exception(String.format("A value set type of '%s' is not currently supported.", valueSetType));
            }
        }

        return valueSetRepositories;
    }
}
