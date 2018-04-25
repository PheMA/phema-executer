package org.phema.executer.translator;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigOrigin;
import org.phema.executer.exception.PhemaUserException;
import org.phema.executer.hqmf.IDocument;
import org.phema.executer.hqmf.v2.*;
import org.phema.executer.i2b2.CRCService;
import org.phema.executer.i2b2.I2B2ExecutionConfiguration;
import org.phema.executer.i2b2.OntologyService;
import org.phema.executer.i2b2.ProjectManagementService;
import org.phema.executer.interfaces.IValueSetRepository;
import org.phema.executer.models.DescriptiveResult;
import org.phema.executer.models.i2b2.Concept;
import org.phema.executer.models.i2b2.QueryMaster;
import org.phema.executer.models.i2b2.TemporalDefinition;
import org.phema.executer.models.i2b2.TemporalEvent;
import org.phema.executer.util.HttpHelper;
import org.phema.executer.valueSets.FileValueSetRepository;
import org.phema.executer.valueSets.models.ValueSet;

import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Luke Rasmussen on 12/7/17.
 */
public class HqmfToI2b2 {
    private HashMap<DataCriteria, QueryMaster> criteriaQueryMap = null;

    public void translate(IDocument document, Config config) throws PhemaUserException {
        // Reset any pre-existing variables
        criteriaQueryMap = null;

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
        } catch (PhemaUserException pue) {
            throw pue;  // Re-throw the user-facing exception as-is.
        } catch (Exception e) {
            // For all other types of exceptions, wrap it in a more user-friendly container.
            throw new PhemaUserException("There was an error when trying to load the configuration details for your value set repositories.  Please double-check that you have specified all of the necessary configuration information, and that the file is formatted correctly.", e);
        }

        // Now get the mapping configuration for the value sets.
        ValueSetToI2b2Ontology valueSetTranslator = new ValueSetToI2b2Ontology();
        try {
            valueSetTranslator.initialize(config, ontService);
        } catch (PhemaUserException pue) {
            throw pue;  // Re-throw the user-facing exception as-is.
        } catch (Exception e) {
            // For all other types of exceptions, wrap it in a more user-friendly container.
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

        CRCService crcService = new CRCService(pmService, configuration, new HttpHelper());

        // Create query definitions for all of the underlying source data criteria.  Once saved, these will be combined
        // into a larger, final query.
        try {
            criteriaQueryMap = defineDataCriteriaQueries(hqmfDocument, hqmfDocument.getDataCriteria(), valueSetConceptMap, crcService);
        } catch (PhemaUserException pue) {
            throw pue;  // Re-throw the user-facing exception as-is.
        } catch (Exception e) {
            // For all other types of exceptions, wrap it in a more user-friendly container.
            throw new PhemaUserException("There was an unexpected error when trying to build the i2b2 query.", e);
        }

        // Now go through and combine these into more complex queries
        List<PopulationCriteria> populationCriteria = hqmfDocument.getPopulationCriteria()
                .stream()
                .filter(x -> x.getType().equals(PopulationCriteria.IPP))
                .collect(Collectors.toList());
        if (populationCriteria.size() != 1) {
            throw new PhemaUserException("Currently the PhEMA Executer is only able to process HQMF documents with a single Initial Patient Population defined");
        }

        PopulationCriteria ipp = populationCriteria.get(0);
        if (ipp.getPreconditions().size() != 1) {
            throw new PhemaUserException("Currently the PhEMA Executer assumes there is only one top-most condition for a population.");
        }

        Precondition precondition = ipp.getPreconditions().get(0);
        QueryMaster masterQuery = null;
        try {
            masterQuery = buildQueryFromPreconditions(crcService, hqmfDocument, precondition, true);
        } catch (PhemaUserException pue) {
            throw pue;  // Re-throw the user-facing exception as-is.
        } catch (Exception e) {
            // For all other types of exceptions, wrap it in a more user-friendly container.
            throw new PhemaUserException("We encountered an error when trying to run your phenotype in i2b2.  Please see the PhEMA Executer logs for more details.  If insufficient details exist, you may need to work with your i2b2 administrator to see if there are reported failures from within i2b2.", e);
        }


        //ArrayList<DataCriteria> dataCriteria = hqmfDocument.getDataCriteria();
//        // Age has special handling
//        DataCriteria[] birthdateCriteria = dataCriteria.stream().filter(x -> x.getDefinition().equals("patient_characteristic_birthdate")).toArray(DataCriteria[]::new);
//        if (birthdateCriteria != null && birthdateCriteria.length > 0) {
//            for (DataCriteria birthdateCriterion : birthdateCriteria) {
//                System.out.println(birthdateCriterion);
//            }
//        }
    }

    private QueryMaster buildQueryFromPreconditions(CRCService crcService, Document hqmfDocument, Precondition parentCondition, boolean returnResults) throws Exception {
        ArrayList<QueryMaster> queryItems = new ArrayList<>();
        ArrayList<Precondition> preconditions = parentCondition.getPreconditions();
        for (Precondition precondition : preconditions) {
            if (precondition.hasPreconditions()) {
                QueryMaster query = buildQueryFromPreconditions(crcService, hqmfDocument, precondition, false);
                queryItems.add(query);
            }
            else if (precondition.getReference() != null){
                // If it doesn't have more preconditions, it's an element that we need to query for.  Get
                // the query entry from our cache.
                Optional<Map.Entry<DataCriteria, QueryMaster>> searchResult = criteriaQueryMap.entrySet()
                        .stream()
                        .filter(x -> Objects.equals(x.getKey().getOriginalId(), precondition.getReference().getId())
                        || Objects.equals(x.getKey().getId(), precondition.getReference().getId()))
                        .findFirst();
                if (!searchResult.isPresent()) {
                    continue;
                }

                Map.Entry<DataCriteria, QueryMaster> entry = searchResult.get();
                queryItems.add(entry.getValue());
                // The way that we are structuring temporal queries - we will find it filed away under it's data criteria
                // entry (not the source entry).  We will do a separate check for each element to find that, if it exists.
                // It's admittedly some overhead, given that the minority of the time we will find a hit.  We should look
                // at refactoring this to have a better (custom) data structure for the criteriaQueryMap value that holds on
                // to these relationships.
                String sourceId = entry.getKey().getId();
                Optional<Map.Entry<DataCriteria, QueryMaster>> temporalSearchResult = criteriaQueryMap.entrySet()
                        .stream()
                        .filter(x -> Objects.equals(x.getKey().getSourceDataCriteria(), sourceId) && !x.getKey().getId().equals(sourceId))
                        .findFirst();
                if (!temporalSearchResult.isPresent()) {
                    continue;
                }

                queryItems.add(temporalSearchResult.get().getValue());
            }
        }

        // Remove all null items, as a safeguard for future processing.
        queryItems.removeAll(Collections.singleton(null));

        // Once we get a definition that is just QueryMaster entries, we need to create a new query
        // master based off of that.
        if (queryItems.size() > 0) {
            // TODO: Counts (e.g., >=2 instances)
            // TODO: Exclusion (NOT)
            QueryMaster query = crcService.runQueryInstance(parentCondition.getId(), crcService.createQueryPanelXmlString(
                    1, requireAll(parentCondition.getConjunction()), parentCondition.isNegation(), 1, "SAMEINSTANCENUM", queryItems), returnResults);
            crcService.pollForQueryCompletion(query);
            return query;
        }

        return null;
    }

    private HashMap<DataCriteria, QueryMaster> defineDataCriteriaQueries(Document hqmfDocument, ArrayList<DataCriteria> dataCriteria, HashMap<ValueSet, ArrayList<Concept>> valueSetConceptMap, CRCService crcService) throws Exception {
        HashMap<DataCriteria, QueryMaster> criteriaQueryMap = new HashMap<>();
        for (DataCriteria criterion : dataCriteria) {
            if (Objects.equals(criterion.getDefinition(), "patient_characteristic_birthdate")) {
                continue;
            }

            // If we have temporal relationships in the query, we need to handle building them a little differently.
            if (criterion.getTemporalReferences() != null && criterion.getTemporalReferences().size() > 0) {
                // TODO: Handle >1 temporal relationship
                if (criterion.getTemporalReferences().size() > 1) {
                    throw new PhemaUserException("Currently the PhEMA Executer is only able to handle a single temporal relationship between data criteria.");
                }

                // The criterion that we are on is the anchor event to be used by i2b2.  We need to define this
                // criterion without the temporal relationship (just the basic data query).
                DataCriteria sourceCriterion = hqmfDocument.getSourceDataCriteriaById(criterion.getSourceDataCriteria());
                if (sourceCriterion == null) {
                    throw new PhemaUserException(String.format("There appears to be an issue with this HQMF document.  We expect every data criteria to have a matching source data criteria, however a source data criteria is not found for the identifier %s.  If the HQMF document was generated by the PhEMA tool, please report this issue to the PhEMA development team for assistance.", criterion.getId()));
                }

                // Now get the associated item and build a basic data criteria for it.
                TemporalReference temporalReference = criterion.getTemporalReferences().get(0);
                String linkedItemId = temporalReference.getReference().getId();
                DataCriteria linkedCriterion = hqmfDocument.getDataCriteriaById(linkedItemId);
                if (linkedCriterion == null) {
                    throw new PhemaUserException(String.format("There appears to be an issue with this HQMF document.  We expect every data criteria to have a matching source data criteria, however a source data criteria is not found for the identifier %s.  If the HQMF document was generated by the PhEMA tool, please report this issue to the PhEMA development team for assistance.", linkedItemId));
                }

                // Send our basic data criterion back to this method to get the actual data query references built.  Then
                // we will build the temporal relationship part of the query.
                HashMap<DataCriteria, QueryMaster> temporalCriteriaQueryMap = new HashMap<>();
                temporalCriteriaQueryMap.putAll(defineDataCriteriaQueries(hqmfDocument,
                        new ArrayList<DataCriteria>() {{ add(sourceCriterion); add(linkedCriterion); }},
                        valueSetConceptMap, crcService));

                // Now build the temporal query structure used by i2b2.
                String panel = crcService.createTemporalQueryXmlString(temporalCriteriaQueryMap.get(sourceCriterion),
                        temporalCriteriaQueryMap.get(linkedCriterion),
                        buildI2B2TemporalDefinition(temporalReference));
                QueryMaster temporalQuery = crcService.runQueryInstance("Temporal Query", panel, false);
                crcService.pollForQueryCompletion(temporalQuery);
                criteriaQueryMap.putAll(temporalCriteriaQueryMap);
                criteriaQueryMap.put(criterion, temporalQuery);
            }
            else {
                String valueSetOid = criterion.getCodeListId();
                Optional<Map.Entry<ValueSet, ArrayList<Concept>>> conceptsResult = valueSetConceptMap.entrySet().stream()
                        .filter(x -> x.getKey().getOid().equals(valueSetOid))
                        .findFirst();
                if (!conceptsResult.isPresent() || conceptsResult.get().getValue() == null) {
                    throw new PhemaUserException(
                            String.format("We were unable to find a value set definition (the codes that belong in the value set) for the value set with OID %s.  We are unable to keep executing the phenotype definition.",
                                    valueSetOid));
                }

                ArrayList<Concept> concepts = conceptsResult.get().getValue();
                String panel = crcService.createConceptPanelXmlString(1, false, false, 1, concepts);
                QueryMaster dataCriteriaQuery = crcService.runQueryInstance(criterion.getId(), panel, false);
                crcService.pollForQueryCompletion(dataCriteriaQuery);
                criteriaQueryMap.put(criterion, dataCriteriaQuery);
            }
        }

        return criteriaQueryMap;
    }

    /**
     * Local helper function to find a value set from a list of value set repositories.  It assumes that the list of
     * repositories is in order of preference, so it stops searching after the first is found.
     * @param oid
     * @param repositories
     * @return The ValueSet definition, or null if no value set was found.
     */
    private ValueSet findValueSet(String oid, List<IValueSetRepository> repositories) {
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
    private FileValueSetRepository createFileValueSetRepository(ConfigObject config) throws Exception {
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
    private ArrayList<IValueSetRepository> processValueSets(Config config) throws Exception {
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

    private TemporalDefinition buildI2B2TemporalDefinition(TemporalReference reference) throws PhemaUserException {
        TemporalDefinition definition = new TemporalDefinition(new TemporalEvent("Event 1", "", "ANY"),
                new TemporalEvent("Event 2", "", "ANY"), "", "", "");

        Object lowBoundObj = reference.getRange().getLow();
        Value lowBound = (lowBoundObj instanceof Value ? (Value)lowBoundObj : null);
        Object highBoundObj = reference.getRange().getHigh();
        Value highBound = (highBoundObj instanceof Value ? (Value)highBoundObj : null);

        setTemporalDefinitionTiming(reference.getType(), definition);
        setTemporalDefinitionOperator(lowBound, highBound, definition);
        setTemporalDefinitionValue(lowBound, highBound, definition);

        return definition;
    }

    private void setTemporalDefinitionValue(Value lowBound, Value highBound, TemporalDefinition definition) throws PhemaUserException {
        // TODO: Support both bounds.  Cheating right now with one.
        Value bound = (lowBound == null ? highBound : lowBound);

        if (bound.getUnit().equals("h")) {
            definition.setUnits("HOUR");
        }
        else if (bound.getUnit().equals("d")) {
            definition.setUnits("DAY");
        }
        else if (bound.getUnit().equals("a")) {
            definition.setUnits("YEAR");
        }
        else if (bound.getUnit().equals("mo")) {
            definition.setUnits("MONTH");
        }
        else if (bound.getUnit().equals("min")) {
            definition.setUnits("MINUTE");
        }
        else {
            throw new PhemaUserException(String.format("The PhEMA Executer does not support temporal conditions with a unit of %s", bound.getUnit()));
        }

        definition.setValue(bound.getValue());
    }

    private void setTemporalDefinitionOperator(Value lowBound, Value highBound, TemporalDefinition definition) throws PhemaUserException {
        if (lowBound != null) {
            if (lowBound.isForceInclusive()) {
                definition.setOperator("GREATEREQUAL");
            }
            else {
                definition.setOperator("GREATER");
            }
        }

        if (highBound != null) {
            if (highBound.isForceInclusive()) {
                definition.setOperator("LESSEQUAL");
            }
            else {
                definition.setOperator("LESS");
            }
        }

        if (lowBound != null && highBound != null) {
            // TODO - we need to support low and high bounded ranges
            throw new PhemaUserException("Currently the PhEMA Executer does not support low and high bound temporal ranges.");
        }
        else if (lowBound == null && highBound == null) {
            throw new PhemaUserException("The PhEMA Executer was unable to convert the temporal operator into an i2b2 query.");
        }
    }

    private void setTemporalDefinitionTiming(String temporalType, TemporalDefinition definition) throws PhemaUserException {
        if (temporalType.equals("CONCURRENT")) {
        }
        else if (temporalType.equals("DURING")) {
        }
        else if (temporalType.equals("EAE")) {
            definition.getEvent1().setTiming("ENDDATE");
            definition.getEvent2().setTiming("ENDDATE");
        }
        else if (temporalType.equals("EAS")) {
            definition.getEvent1().setTiming("ENDDATE");
            definition.getEvent2().setTiming("STARTDATE");
        }
        else if (temporalType.equals("EBE")) {
            definition.getEvent1().setTiming("ENDDATE");
            definition.getEvent2().setTiming("ENDDATE");
        }
        else if (temporalType.equals("EBS")) {
            definition.getEvent1().setTiming("ENDDATE");
            definition.getEvent2().setTiming("STARTDATE");
        }
        else if (temporalType.equals("ECW")) {
        }
        else if (temporalType.equals("ECWS")) {
            definition.getEvent1().setTiming("ENDDATE");
            definition.getEvent2().setTiming("STARTDATE");
        }
        else if (temporalType.equals("EDU")) {
        }
        else if (temporalType.equals("OVERLAP")) {
        }
        else if (temporalType.equals("SAE")) {
            definition.getEvent1().setTiming("STARTDATE");
            definition.getEvent2().setTiming("ENDDATE");
        }
        else if (temporalType.equals("SBE")) {
            definition.getEvent1().setTiming("STARTDATE");
            definition.getEvent2().setTiming("ENDDATE");
        }
        else if (temporalType.equals("SBS")) {
            definition.getEvent1().setTiming("STARTDATE");
            definition.getEvent2().setTiming("STARTDATE");
        }
        else if (temporalType.equals("SCW")) {
        }
        else if (temporalType.equals("SCWE")) {
            definition.getEvent1().setTiming("STARTDATE");
            definition.getEvent2().setTiming("ENDDATE");
        }
        else if (temporalType.equals("SDU")) {
        }

        // If we don't handle a temporal operator in the above if-else tree, the event timing will be blank.  We will do
        // a single check here for that, and throw an exception if either event timing is not set.
        if (definition.getEvent1().getTiming().equals("") || definition.getEvent2().getTiming().equals("")) {
            throw new PhemaUserException(
                    String.format("At this time, the %s temporal operator is not supported for i2b2 queries by PhEMA.", temporalType));
        }
    }

    private boolean requireAll(String conjunction) throws PhemaUserException {
        switch (conjunction) {
            case Precondition.ALL_TRUE:
                return true;
            case Precondition.AT_LEAST_ONE_TRUE:
                return false;
            default:
                throw new PhemaUserException(String.format("The PhEMA Executer is not currently capable of handling a boolean conjunction of '%s'", conjunction));
        }
    }

}
