package org.phema.executer.translator;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigOrigin;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.phema.executer.exception.PhemaUserException;
import org.phema.executer.hqmf.IDocument;
import org.phema.executer.hqmf.Parser;
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
public class HqmfToI2b2 extends Observable {
    private HashMap<DataCriteria, QueryMaster> criteriaQueryMap = null;

    public boolean execute(File configFile) {
        try {
            updateActionStart("Loading the configuration settings for this phenotype");
            Config config = ConfigFactory.parseFile(configFile);
            updateActionEnd("Configuration settings have been loaded");

            updateActionStart("Finding HQMF file path");
            String hqmfFilePath = getFilePathRelativeToConfigFile(config.getObject("execution"), "phenotypeDefinition");
            updateActionDetails(String.format("Using HQMF file located at %s", hqmfFilePath));
            File hqmfFile = new File(hqmfFilePath);
            updateActionEnd("Found HQMF file path");

            updateActionStart("Parsing the HQMF document");
            String hqmf = FileUtils.readFileToString(hqmfFile);
            Parser parser = new Parser();
            IDocument document = parser.parse(hqmf);
            updateActionEnd("The HQMF document has been parsed and is ready to process");
            translate(document, config);
        }
        catch (PhemaUserException pue) {
            updateProgress("ERROR - There was an error during execution");
            updateProgress(pue.getMessage());
            return false;
        }
        catch (Exception exc) {
            updateProgress("ERROR - There was an unhandled error during execution.  Please report this to the PhEMA team.");
            return false;
        }

        return true;
    }

    public void translate(IDocument document, Config config) throws PhemaUserException {
        // Reset any pre-existing variables
        criteriaQueryMap = null;

        if (document == null) {
            updateProgress("The document is null - exiting");
            return;
        }

        if (config == null) {
            updateProgress("The configuration is null - exiting");
        }

        if (!(document instanceof Document)) {
            updateProgress("The document is not HQMF v2, and so is unsupported");
            return;
        }

        Document hqmfDocument = (Document)document;

        // Build the i2b2-specific configuration information from our config object
        updateActionStart("Loading the specific i2b2 configuration information");
        I2B2ExecutionConfiguration configuration = new I2B2ExecutionConfiguration();
        DescriptiveResult result = configuration.loadFromConfiguration(config);
        if (!result.isSuccess()) {
            throw new PhemaUserException(result);
        }
        updateActionEnd("i2b2 configuration details have been loaded.");

        // Create an instance of the project management service, and ensure that we are
        // properly authenticated
        updateActionStart("Logging into the i2b2 Project Manager");
        ProjectManagementService pmService = new ProjectManagementService(configuration, new HttpHelper());
        result = pmService.login();
        if (!result.isSuccess()) {
            throw new PhemaUserException(result);
        }
        updateActionEnd("Successfully connected to i2b2");

        OntologyService ontService = new OntologyService(pmService, configuration, new HttpHelper());

        // Build the full list of value set repositories that we are configured to use
        // for this phenotype.
        updateActionStart("Initializing value set repositories");
        ArrayList<IValueSetRepository> valueSetRepositories = null;
        try {
            valueSetRepositories = processValueSets(config);
        } catch (PhemaUserException pue) {
            throw pue;  // Re-throw the user-facing exception as-is.
        } catch (Exception e) {
            // For all other types of exceptions, wrap it in a more user-friendly container.
            throw new PhemaUserException("There was an error when trying to load the configuration details for your value set repositories.  Please double-check that you have specified all of the necessary configuration information, and that the file is formatted correctly.", e);
        }
        updateActionEnd("Value set repositories are now loaded");

        // Make sure at least one is loaded
        if (valueSetRepositories == null || valueSetRepositories.size() == 0) {
            throw new PhemaUserException("No value set repositories were loaded.  At least one repository must be configured for your phenotype.");
        }

        // Now get the mapping configuration for the value sets.
        updateActionStart("Initializing the mapping logic between value sets and the i2b2 ontology");
        ValueSetToI2b2Ontology valueSetTranslator = new ValueSetToI2b2Ontology();
        try {
            valueSetTranslator.initialize(config, ontService);
        } catch (PhemaUserException pue) {
            throw pue;  // Re-throw the user-facing exception as-is.
        } catch (Exception e) {
            // For all other types of exceptions, wrap it in a more user-friendly container.
            throw new PhemaUserException("There was an error when trying to set up the rules to map from a value set to i2b2 concepts.  Please double-check that you have correctly specified the mapping rules.", e);
        }
        updateActionEnd("Completed initializing the mapping rules");

        // Get the list of value sets from the document and find those that exist in the repository.  Make a note
        // of those we can't find.
        ArrayList<String> valueSetOids = document.getAllValueSetOids();
        ArrayList<String> unmappedValueSets = new ArrayList<>();
        ArrayList<ValueSet> valueSets = new ArrayList<>(valueSetOids.size());
        updateActionStart("Loading rules to map value sets to the i2b2 ontology");
        int valueSetCount = valueSetOids.size();
        for (int index = 0; index < valueSetCount; index++) {
            String oid = valueSetOids.get(index);
            updateActionDetails(String.format("Mapping value set %d of %d (OID: %s)", (index + 1), valueSetCount, oid));
            ValueSet valueSet = findValueSet(oid, valueSetRepositories);
            if (valueSet != null) {
                valueSets.add(valueSet);
                updateActionDetails(String.format("  Found value set %s", oid));
            }
            else {
                unmappedValueSets.add(oid);
                updateActionDetails(String.format("  Could not find value set %s", oid));
            }
        }
        updateActionEnd(String.format("Completed loading mapping rules for %d of %d value sets", valueSets.size(), valueSetCount));

        // Translate the value sets into the i2b2 ontology.  What we end up with is a mapping between a value set and
        // a list of i2b2 Concepts.
        updateActionStart("Mapping value set codes to i2b2 ontology");
        HashMap<ValueSet, ArrayList<Concept>> valueSetConceptMap = new HashMap<>();
        for (ValueSet valueSet : valueSets) {
            int codeCount = valueSet.getMembers().size();
            updateActionDetails(String.format("Mapping %d codes in %s (OID: %s)",
                    codeCount, valueSet.getName(), valueSet.getOid()));
            ArrayList<Concept> mappedConcepts = valueSetTranslator.translate(valueSet);
            int mappedCount = mappedConcepts.size();
            updateActionDetails(String.format("  Mapped %d i2b2 ontology terms to %d value set codes",
                    mappedCount, codeCount));
            if (mappedCount == 0) {
                updateActionDetails("  *WARNING*: No mappings found for this value set");
            }
            valueSetConceptMap.put(valueSet, mappedConcepts);
        }
        updateActionEnd("Finished mapping value set codes to i2b2 ontology");

        CRCService crcService = new CRCService(pmService, configuration, new HttpHelper());

        // Create query definitions for all of the underlying source data criteria.  Once saved, these will be combined
        // into a larger, final query.
        try {
            updateActionStart("Creating data criteria queries");
            criteriaQueryMap = defineDataCriteriaQueries(hqmfDocument, hqmfDocument.getDataCriteria(), valueSetConceptMap, crcService, 1);
            updateActionEnd("Finished creating data criteria queries");
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
            updateActionStart("Starting to build the master query definition");
            masterQuery = buildQueryFromPreconditions(crcService, hqmfDocument, precondition, true, 1);
            updateActionEnd("Completed creation of the master query definition");
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

    private void updateActionStart(String details) {
        updateProgress(String.format("%s ...", details));
    }

    private void updateActionEnd(String details) {
        updateProgress(String.format("... %s", details));
    }

    private void updateActionDetails(String details) {
        updateActionDetails(details, 1);
    }

    private void updateActionDetails(String details, int indentLevel) {
        updateProgress(String.format("%s%s", StringUtils.repeat(" ", (indentLevel * 2)), details));
    }

    /**
     * Trigger an update to all Observers that progress has been made.
     * @param progress String describing the progress/message that will be sent to the Observer
     */
    private void updateProgress(String progress) {
        this.setChanged();
        this.notifyObservers(progress);
    }

    private QueryMaster buildQueryFromPreconditions(CRCService crcService, Document hqmfDocument, Precondition parentCondition, boolean returnResults, int nestedLevel) throws Exception {
        updateActionDetails(String.format("Defining query for parent precondition: %s", parentCondition.getId()), nestedLevel);
        ArrayList<QueryMaster> queryItems = new ArrayList<>();
        ArrayList<Precondition> preconditions = parentCondition.getPreconditions();
        //for (Precondition precondition : preconditions) {
        int preconditionsCount = preconditions.size();
        for (int index = 0; index < preconditionsCount; index++) {
            updateActionDetails(String.format("  Processing precondition %d of %d", (index + 1), preconditionsCount), nestedLevel);
            Precondition precondition = preconditions.get(index);
            if (precondition.hasPreconditions()) {
                updateActionDetails("  Handling nested preconditions of this precondition", nestedLevel);
                QueryMaster query = buildQueryFromPreconditions(crcService, hqmfDocument, precondition, false, (nestedLevel + 2));
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

    private HashMap<DataCriteria, QueryMaster> defineDataCriteriaQueries(
            Document hqmfDocument, ArrayList<DataCriteria> dataCriteria, HashMap<ValueSet, ArrayList<Concept>> valueSetConceptMap,
            CRCService crcService, int nestedLevel) throws Exception {
        updateActionDetails(String.format("Processing %d data criteria", dataCriteria.size()), nestedLevel);
        HashMap<DataCriteria, QueryMaster> criteriaQueryMap = new HashMap<>();
        for (DataCriteria criterion : dataCriteria) {
            if (Objects.equals(criterion.getDefinition(), "patient_characteristic_birthdate")) {
                continue;
            }

            // If we have temporal relationships in the query, we need to handle building them a little differently.
            if (criterion.getTemporalReferences() != null && criterion.getTemporalReferences().size() > 0) {
                updateActionDetails("Creating criteria with a temporal relationship", nestedLevel);
                // TODO: Handle >1 temporal relationship
                if (criterion.getTemporalReferences().size() > 1) {
                    throw new PhemaUserException("Currently the PhEMA Executer is only able to handle a single temporal relationship between data criteria.");
                }

                // The criterion that we are on is the anchor event to be used by i2b2.  We need to define this
                // criterion without the temporal relationship (just the basic data query).
                updateActionDetails(String.format("Locating anchor event for temporal relationship: %s", criterion.getSourceDataCriteria()), nestedLevel);
                DataCriteria sourceCriterion = hqmfDocument.getSourceDataCriteriaById(criterion.getSourceDataCriteria());
                if (sourceCriterion == null) {
                    throw new PhemaUserException(String.format("There appears to be an issue with this HQMF document.  We expect every data criteria to have a matching source data criteria, however a source data criteria is not found for the identifier %s.  If the HQMF document was generated by the PhEMA tool, please report this issue to the PhEMA development team for assistance.", criterion.getId()));
                }

                // Now get the associated item and build a basic data criteria for it.
                TemporalReference temporalReference = criterion.getTemporalReferences().get(0);
                String linkedItemId = temporalReference.getReference().getId();
                updateActionDetails(String.format("Locating linked event for temporal relationship: %s", linkedItemId), nestedLevel);
                DataCriteria linkedCriterion = hqmfDocument.getDataCriteriaById(linkedItemId);
                if (linkedCriterion == null) {
                    throw new PhemaUserException(String.format("There appears to be an issue with this HQMF document.  We expect every data criteria to have a matching source data criteria, however a source data criteria is not found for the identifier %s.  If the HQMF document was generated by the PhEMA tool, please report this issue to the PhEMA development team for assistance.", linkedItemId));
                }

                // Send our basic data criterion back to this method to get the actual data query references built.  Then
                // we will build the temporal relationship part of the query.
                HashMap<DataCriteria, QueryMaster> temporalCriteriaQueryMap = new HashMap<>();
                temporalCriteriaQueryMap.putAll(defineDataCriteriaQueries(hqmfDocument,
                        new ArrayList<DataCriteria>() {{ add(sourceCriterion); add(linkedCriterion); }},
                        valueSetConceptMap, crcService, (nestedLevel + 1)));

                // Now build the temporal query structure used by i2b2.
                String panel = crcService.createTemporalQueryXmlString(temporalCriteriaQueryMap.get(sourceCriterion),
                        temporalCriteriaQueryMap.get(linkedCriterion),
                        buildI2B2TemporalDefinition(temporalReference));
                updateActionDetails("Creating the temporal query in i2b2...", nestedLevel);
                QueryMaster temporalQuery = crcService.runQueryInstance("Temporal Query", panel, false);
                crcService.pollForQueryCompletion(temporalQuery);
                criteriaQueryMap.putAll(temporalCriteriaQueryMap);
                criteriaQueryMap.put(criterion, temporalQuery);
                updateActionDetails("...Created the temporal query in i2b2", nestedLevel);
            }
            else {
                updateActionDetails("Locating the i2b2 ontology terms mapped to the value set for this criterion", nestedLevel);
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
                updateActionDetails(String.format("Creating the criterion in i2b2...", criterion.getId()), nestedLevel);
                QueryMaster dataCriteriaQuery = crcService.runQueryInstance(criterion.getId(), panel, false);
                crcService.pollForQueryCompletion(dataCriteriaQuery);
                criteriaQueryMap.put(criterion, dataCriteriaQuery);
                updateActionDetails("...Saved the criterion definition in i2b2");
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

        String path = config.get("path").unwrapped().toString();
        updateActionStart(String.format("Creating a value set repository from the file '%s'", path));

//        ConfigOrigin origin = config.origin();
//        URL configFilePath = origin.url();
//        File configFile = new File(configFilePath.getFile());
//        String baseDirectory = configFile.getParent();
//
//        updateActionDetails(String.format("Looking for the value sets file in the directory '%s'", baseDirectory));
//        String fullFilePath = (new File(baseDirectory, path)).getAbsolutePath();
        String fullFilePath = getFilePathRelativeToConfigFile(config, "path");
        updateActionDetails(String.format("Repository path set to '%s'", fullFilePath));

        FileValueSetRepository repository = new FileValueSetRepository();
        repository.initialize(new HashMap<String, String>(){{ put(FileValueSetRepository.Parameters.FilePath,
                fullFilePath); }});
        updateActionEnd("Created the value set file repository");
        return repository;
    }

    private String getFilePathRelativeToConfigFile(ConfigObject config, String fileNameKey) {
        ConfigOrigin origin = config.origin();
        URL configFilePath = origin.url();
        File configFile = new File(configFilePath.getFile());
        String baseDirectory = configFile.getParent();

        String path = config.get(fileNameKey).unwrapped().toString();
        String fullFilePath = (new File(baseDirectory, path)).getAbsolutePath();
        return fullFilePath;
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
            throw new PhemaUserException("You must specify at least one value set for the phenotype to execute.");
        }

        for (ConfigObject valueSetObject : valueSetObjects) {
            if (!valueSetObject.containsKey("type")) {
                throw new PhemaUserException("A value set definition must contain a 'type' field of 'File' or 'CTS2'");
            }

            String valueSetType = valueSetObject.get("type").unwrapped().toString();
            switch (valueSetType) {
                case "File":
                    IValueSetRepository repository = createFileValueSetRepository(valueSetObject);
                    valueSetRepositories.add(repository);
                    break;
                default:
                    throw new PhemaUserException(String.format("A value set type of '%s' is not currently supported.", valueSetType));
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
