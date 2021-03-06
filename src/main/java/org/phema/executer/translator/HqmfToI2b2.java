package org.phema.executer.translator;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigOrigin;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.phema.executer.DebugLogger;
import org.phema.executer.configuration.ExecutionConfiguration;
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
import org.phema.executer.models.ExecutionModeType;
import org.phema.executer.models.i2b2.*;
import org.phema.executer.util.HttpHelper;
import org.phema.executer.valueSets.FileValueSetRepository;
import org.phema.executer.valueSets.models.Member;
import org.phema.executer.valueSets.models.ValueSet;

import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Luke Rasmussen on 12/7/17.
 */
public class HqmfToI2b2 extends Observable {
    private HashMap<DataCriteria, QueryMaster> criteriaQueryMap = null;
    private List<QueryMaster> queries = null;
    private DebugLogger debugLogger = null;

    public Observer getLogger() {
        return logger;
    }

    public void setLogger(Observer logger) {
        this.logger = logger;
    }

    private Observer logger = null;

    public boolean execute(File configFile) {
        try {
            if (logger != null) {
                deleteObservers();
                addObserver(getLogger());
            }

            if (!configFile.exists()) {
                updateProgress("The configuration file you specified could not be found.  Please make sure that you have the correct path to the file.");
                return false;
            }

            updateActionStart("Loading the configuration settings for this phenotype");
            Config config = ConfigFactory.parseFile(configFile);
            updateActionEnd("Configuration settings have been loaded");

            updateActionStart("Finding HQMF file path");
            String hqmfFilePath = ExecutionConfiguration.getFilePathRelativeToConfigFile(config.getObject("execution"), "phenotypeDefinition");
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
            pue.printStackTrace();
            return false;
        }
        catch (Exception exc) {
            updateProgress("ERROR - There was an unhandled error during execution.  Please report this to the PhEMA team.");
            exc.printStackTrace();
            return false;
        }

        return true;
    }

    public void close() {
        if (debugLogger != null) {
            debugLogger.close();
            debugLogger = null;
        }
    }

    public void translate(IDocument document, Config config) throws PhemaUserException {
        // Reset any pre-existing variables
        criteriaQueryMap = null;
        queries = new ArrayList<QueryMaster>();

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

        if (!configuration.getExecutionEngineName().equalsIgnoreCase("i2b2")) {
            throw new PhemaUserException("Currently the PhEMA Executer only works with i2b2.  Your configuration file has specified 'engine' as '" + configuration.getExecutionEngineName() + "'");
        }

        boolean debugMode = (configuration.getMode() == ExecutionModeType.DEBUG);
        if (debugMode) {
            updateProgress(String.format("DEBUG mode is enabled - additional logging will be done to the '%s' directory in the same location as your configuration file.",
                    DebugLogger.LOGGING_DIRECTORY));
            try {
                debugLogger = new DebugLogger();
                debugLogger.initialize(configuration.getConfigBaseDirectory());
            }
            catch (PhemaUserException pue) {
                throw pue;
            }
            catch (Exception exc) {
                throw new PhemaUserException("An error was detected when trying to enable debug logging.  Please review the output log for more information.");
            }
        }

        boolean trustAllSsl = configuration.isTrustAllSsl();
        HttpHelper httpHelper = new HttpHelper(trustAllSsl);
        if (trustAllSsl) {
            updateProgress("Implicitly trusting all certificates used in SSL connections");
        } else {
            updateProgress("Using Java certificate store for SSL trust resolution.  If you receive SSL connection errors, you may need to import additional certificates into the Java certificate store.");
        }

        // Create an instance of the project management service, and ensure that we are
        // properly authenticated
        updateActionStart("Logging into the i2b2 Project Manager");
        ProjectManagementService pmService = new ProjectManagementService(configuration, httpHelper, debugLogger);
        result = pmService.login();
        if (!result.isSuccess()) {
            throw new PhemaUserException(result);
        }
        updateActionEnd("Successfully connected to i2b2");

        OntologyService ontService = new OntologyService(pmService, configuration, httpHelper, debugLogger);

        // Build the full list of value set repositories that we are configured to use
        // for this phenotype.
        updateActionStart("Initializing value set repositories");
        ArrayList<IValueSetRepository> valueSetRepositories = configuration.getValueSetRepositories();
        updateActionEnd("Value set repositories are now loaded");

        // Make sure at least one is loaded
        if (valueSetRepositories == null || valueSetRepositories.size() == 0) {
            throw new PhemaUserException("No value set repositories were loaded.  At least one repository must be configured for your phenotype.");
        }

        // Now get the mapping configuration for the value sets.
        updateActionStart("Initializing the mapping logic between value sets and the i2b2 ontology");
        ValueSetToI2b2Ontology valueSetTranslator = new ValueSetToI2b2Ontology(debugLogger);
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

        updateActionStart("Gathering information about your i2b2 ontology setup");
        ArrayList<Concept> ontologyCategories = ontService.getCategories();
        updateActionDetails("The following ontology categories were found:", 1);
        for (Concept category : ontologyCategories) {
            updateActionDetails(String.format("%s - %s", category.getName(), category.getKey()), 2);
        }
        updateActionEnd("Completed information gathering about your i2b2 ontology");

        // Translate the value sets into the i2b2 ontology.  What we end up with is a mapping between a value set and
        // a list of i2b2 Concepts.
        updateActionStart("Mapping value set codes to i2b2 ontology");
        HashMap<ValueSet, ArrayList<Concept>> valueSetConceptMap = new HashMap<>();
        for (ValueSet valueSet : valueSets) {
            int codeCount = valueSet.getMembers().size();
            updateActionDetails(String.format("Mapping %d codes in %s (OID: %s)",
                    codeCount, valueSet.getName(), valueSet.getOid()));
            ValueSetToI2b2Ontology.TranslationResult translationResult = valueSetTranslator.translate(valueSet);
            int mappedCount = translationResult.DistinctMappedConcepts.size();
            if (mappedCount == 0) {
                updateActionDetails("** WARNING ** : No mappings found for this value set - the phenotype results may not be accurate", 2);
            }
            else {
                updateActionDetails(String.format("Mapped %d i2b2 ontology terms to %d value set codes",
                        mappedCount, codeCount), 2);
            }

            if (translationResult.UnmappedMembers.size() > 0) {
                updateActionDetails("The following value set codes could not be mapped in your i2b2 instance:", 2);
                for (Member member : translationResult.UnmappedMembers) {
                    updateActionDetails(String.format("%s (%s : %s)", member.getDescription(), member.getCodeSystem(), member.getCode()), 3);
                }

                if (translationResult.FilteredOutMembers.size() > 0) {
                    updateActionDetails(String.format("There were %d concepts filtered out because of the ontology path filter", translationResult.FilteredOutMembers.size()), 2);
                    if (translationResult.MappedMembers.size() == 0) {
                        updateActionDetails("** HINT ** : The restrictToOntologyPath setting in your config may have caused all of the i2b2 concepts to get filtered out.  Please review and make sure it is correct.", 2);
                    }
                }

                updateActionDetails("");
            }
            valueSetConceptMap.put(valueSet, translationResult.DistinctMappedConcepts);
        }
        updateActionEnd("Finished mapping value set codes to i2b2 ontology");

        CRCService crcService = new CRCService(pmService, configuration, httpHelper, debugLogger);
        crcService.addObserver(getLogger());

        // Create query definitions for all of the underlying source data criteria.  Once saved, these will be combined
        // into a larger, final query.
        try {
            updateActionStart("Creating data criteria queries");
            criteriaQueryMap = defineDataCriteriaQueries(hqmfDocument, hqmfDocument.getDataCriteria(), valueSetConceptMap, crcService, valueSetTranslator, 1, configuration);
            queries.addAll(criteriaQueryMap.values());
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
            masterQuery = buildQueryFromPreconditions(crcService, hqmfDocument, precondition, true, 1, configuration.getQueryPrefix());
            updateActionDetails("Running the master query - we will poll for this query to complete");
            DescriptiveResult masterQueryResult = crcService.pollForQueryCompletion(masterQuery);
            if (!masterQueryResult.isSuccess()) {
                updateActionDetails(String.format("Polling for query completion failed - %s", String.join("\r\n", masterQueryResult.getDescriptions())));
            }
            updateActionEnd("Completed creation of the master query definition");
        } catch (PhemaUserException pue) {
            throw pue;  // Re-throw the user-facing exception as-is.
        } catch (Exception e) {
            // For all other types of exceptions, wrap it in a more user-friendly container.
            throw new PhemaUserException("We encountered an error when trying to run your phenotype in i2b2.  Please see the PhEMA Executer logs for more details.  If insufficient details exist, you may need to work with your i2b2 administrator to see if there are reported failures from within i2b2.", e);
        }

        if (masterQuery != null) {
            updateActionDetails("");
            updateActionDetails("************************************");
            updateActionDetails("");
            updateActionDetails(String.format("Total patients: %d", masterQuery.getCount()));
            updateActionDetails("");
            updateActionDetails("Query Breakdown:");
            updateActionDetails("------------------------------------");
            int maxLength = 0;
            List<QueryMaster> sortedMasterQueryList = queries.stream().sorted().distinct().collect(Collectors.toList());
            for (QueryMaster entry : sortedMasterQueryList) {
                maxLength = Math.max(maxLength, entry.getName().length());
            }
            maxLength = Math.max(maxLength, masterQuery.getName().length());
            for (QueryMaster entry : sortedMasterQueryList) {
                updateActionDetails(String.format(String.format(String.format("  %%-%ds   %%d", maxLength), entry.getName(), entry.getCount())));
            }
            updateActionDetails(String.format(String.format(String.format("  %%-%ds   %%d", maxLength), masterQuery.getName(), masterQuery.getCount())));
        }
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

    private QueryMaster buildQueryFromPreconditions(CRCService crcService, Document hqmfDocument, Precondition parentCondition,
                                                    boolean returnResults, int nestedLevel, String queryPrefix) throws Exception {
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
                QueryMaster query = buildQueryFromPreconditions(crcService, hqmfDocument, precondition, false, (nestedLevel + 2), queryPrefix);
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
            queries.addAll(queryItems);
            boolean isMasterQuery = (nestedLevel == 1);
            String queryName = String.format("%s - %s", queryPrefix, (isMasterQuery ? "Master Query" : parentCondition.getId()));
            QueryMaster query = crcService.runQueryInstance(queryName, crcService.createQueryPanelXmlString(
                    1, requireAll(parentCondition.getConjunction()), parentCondition.isNegation(), 1, "ANY", queryItems), returnResults);
            return query;
        }

        return null;
    }

    private HashMap<DataCriteria, QueryMaster> defineDataCriteriaQueries(
            Document hqmfDocument, ArrayList<DataCriteria> dataCriteria, HashMap<ValueSet, ArrayList<Concept>> valueSetConceptMap,
            CRCService crcService, ValueSetToI2b2Ontology valueSetTranslator, int nestedLevel, I2B2ExecutionConfiguration configuration) throws Exception {
        updateActionDetails(String.format("Processing %d data criteria", dataCriteria.size()), nestedLevel);
        HashMap<DataCriteria, QueryMaster> criteriaQueryMap = new HashMap<>();
        for (DataCriteria criterion : dataCriteria) {
            // If we have temporal relationships in the query, we need to handle building them a little differently.
            if (criterion.getTemporalReferences() != null && criterion.getTemporalReferences().size() > 0) {
                updateActionDetails("Creating criteria with a temporal relationship", nestedLevel);
                // TODO: Handle >1 temporal relationship
                if (criterion.getTemporalReferences().size() > 1) {
                    throw new PhemaUserException("Currently the PhEMA Executer is only able to handle a single temporal relationship between data criteria.");
                }

                // Now get the associated item and build a basic data criteria for it.
                TemporalReference temporalReference = criterion.getTemporalReferences().get(0);
                HashMap<DataCriteria, QueryMaster> temporalCriteriaQueryMap = new HashMap<>();

                // Patient birthdate, when related to something else, can be expressed in i2b2 as a
                // temporal query.  We will relate it in the same visit to the other event.
                if (Objects.equals(criterion.getDefinition(), "patient_characteristic_birthdate")) {
                    updateActionDetails("Performing some special processing for patient age");
                    ArrayList<Concept> concepts = valueSetTranslator.translateAgeRequirement(criterion);

                    String linkedItemId = temporalReference.getReference().getId();
                    updateActionDetails(String.format("Locating linked event for temporal relationship: %s", linkedItemId), nestedLevel);
                    DataCriteria linkedCriterion = hqmfDocument.getDataCriteriaById(linkedItemId);
                    if (linkedCriterion == null) {
                        throw new PhemaUserException(String.format("There appears to be an issue with this HQMF document.  We expect every data criteria to have a matching source data criteria, however a source data criteria is not found for the identifier %s.  If the HQMF document was generated by the PhEMA tool, please report this issue to the PhEMA development team for assistance.", linkedItemId));
                    }

                    String agePanelString = crcService.createConceptPanelXmlString(1, false, false, 1, "SAMEVISIT", concepts);
                    temporalCriteriaQueryMap.putAll(defineDataCriteriaQueries(hqmfDocument,
                            new ArrayList<DataCriteria>() {{ add(linkedCriterion); }},
                            valueSetConceptMap, crcService, valueSetTranslator, (nestedLevel + 1), configuration));
                    String relatedPanelString = crcService.createQueryPanelXmlString(2, false, false, 1, "SAMEVISIT",
                            new ArrayList<QueryMaster>() {{ add(temporalCriteriaQueryMap.get(linkedCriterion)); }});

                    updateActionDetails("Creating the age temporal query in i2b2...", nestedLevel);
                    QueryMaster temporalQuery = crcService.runQueryInstance(String.format("%s - Age Temporal Query", configuration.getQueryPrefix()), agePanelString + "\n" + relatedPanelString, "SAMEVISIT", false);
                    if (configuration.isWaitForEachQueryPart()) {
                        DescriptiveResult result = crcService.pollForQueryCompletion(temporalQuery);
                        if (!result.isSuccess()) {
                            updateActionDetails(String.format("Polling for query completion failed - %s", String.join("\r\n", result.getDescriptions())));
                        }
                    }
                    criteriaQueryMap.putAll(temporalCriteriaQueryMap);
                    criteriaQueryMap.put(criterion, temporalQuery);
                    updateActionDetails("...Created the age temporal query in i2b2", nestedLevel);
                }
                else {
                    // The criterion that we are on is the anchor event to be used by i2b2.  We need to define this
                    // criterion without the temporal relationship (just the basic data query).
                    updateActionDetails(String.format("Locating anchor event for temporal relationship: %s", criterion.getSourceDataCriteria()), nestedLevel);
                    DataCriteria sourceCriterion = hqmfDocument.getSourceDataCriteriaById(criterion.getSourceDataCriteria());
                    if (sourceCriterion == null) {
                        throw new PhemaUserException(String.format("There appears to be an issue with this HQMF document.  We expect every data criteria to have a matching source data criteria, however a source data criteria is not found for the identifier %s.  If the HQMF document was generated by the PhEMA tool, please report this issue to the PhEMA development team for assistance.", criterion.getId()));
                    }

                    String linkedItemId = temporalReference.getReference().getId();
                    updateActionDetails(String.format("Locating linked event for temporal relationship: %s", linkedItemId), nestedLevel);
                    DataCriteria linkedCriterion = hqmfDocument.getDataCriteriaById(linkedItemId);
                    if (linkedCriterion == null) {
                        throw new PhemaUserException(String.format("There appears to be an issue with this HQMF document.  We expect every data criteria to have a matching source data criteria, however a source data criteria is not found for the identifier %s.  If the HQMF document was generated by the PhEMA tool, please report this issue to the PhEMA development team for assistance.", linkedItemId));
                    }

                    // Send our basic data criterion back to this method to get the actual data query references built.  Then
                    // we will build the temporal relationship part of the query.
                    temporalCriteriaQueryMap.putAll(defineDataCriteriaQueries(hqmfDocument,
                            new ArrayList<DataCriteria>() {{ add(sourceCriterion); add(linkedCriterion); }},
                            valueSetConceptMap, crcService, valueSetTranslator, (nestedLevel + 1), configuration));

                    // Now build the temporal query structure used by i2b2.
                    String panel = crcService.createTemporalQueryXmlString(temporalCriteriaQueryMap.get(sourceCriterion),
                            temporalCriteriaQueryMap.get(linkedCriterion),
                            buildI2B2TemporalDefinition(temporalReference));
                    updateActionDetails("Creating the temporal query in i2b2...", nestedLevel);
                    QueryMaster temporalQuery = crcService.runQueryInstance(String.format("%s - Temporal Query", configuration.getQueryPrefix()), panel, false);
                    if (configuration.isWaitForEachQueryPart()) {
                        DescriptiveResult result = crcService.pollForQueryCompletion(temporalQuery);
                        if (!result.isSuccess()) {
                            updateActionDetails(String.format("Polling for query completion failed - %s", String.join("\r\n", result.getDescriptions())));
                        }
                    }
                    criteriaQueryMap.putAll(temporalCriteriaQueryMap);
                    criteriaQueryMap.put(criterion, temporalQuery);
                    updateActionDetails("...Created the temporal query in i2b2", nestedLevel);
                }
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

                // Look for any subset operators, which includes COUNT.  If applicable, specify the lower bound.
                int itemOccurrence = 1;   // Default to 1
                if (criterion.getSubsetOperators() != null && criterion.getSubsetOperators().size() > 0) {
                    Optional<SubsetOperator> operatorResult = criterion.getSubsetOperators().stream()
                            .filter(x -> x.getType().equals("COUNT"))
                            .findFirst();
                    if (operatorResult.isPresent()) {
                        SubsetOperator countOperator = operatorResult.get();
                        Range range = (Range)countOperator.getValue();
                        String rangeHigh = range.safeGetHighAsString();
                        String rangeLow = range.safeGetLowAsString();
                        if (rangeHigh != null
                                && !rangeHigh.equalsIgnoreCase("PINF")
                                && !rangeHigh.equals("")) {
                            throw new PhemaUserException(String.format("i2b2 only supports specifying a minimum count - the criterion '%s - %s' specifies an upper bound, which cannot be supported",
                                    criterion.getId(), criterion.getDescription()));
                        }

                        if (rangeLow == null || rangeLow.equals("")) {
                            updateActionDetails("The i2b2 criteria has a COUNT operator, with no lower or upper bound specified.  Assuming a default count of >=1.", nestedLevel);
                        }

                        itemOccurrence = Integer.parseInt(rangeLow);
                    }
                }

                ArrayList<Concept> concepts = conceptsResult.get().getValue();
                String panel = crcService.createConceptPanelXmlString(1, false, false, itemOccurrence, "ANY", concepts);
                updateActionDetails(String.format("Creating the criterion in i2b2...", criterion.getId()), nestedLevel);
                String queryName = String.format("%s - %s", configuration.getQueryPrefix(), criterion.getId());
                QueryMaster dataCriteriaQuery = crcService.runQueryInstance(queryName, panel, false);
                if (configuration.isWaitForEachQueryPart()) {
                    DescriptiveResult result = crcService.pollForQueryCompletion(dataCriteriaQuery);
                    if (!result.isSuccess()) {
                        updateActionDetails(String.format("Polling for query completion failed - %s", String.join("\r\n", result.getDescriptions())));
                    }
                }
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
     * Build a TemporalDefinition used within an i2b2 query, given an HQMF TemporalReference
     * @param reference
     * @return
     * @throws PhemaUserException
     */
    private TemporalDefinition buildI2B2TemporalDefinition(TemporalReference reference) throws PhemaUserException {
        ArrayList<TemporalRelationship> relationships = new ArrayList<TemporalRelationship>();
        if (reference.getType().equalsIgnoreCase("CONCURRENT") || reference.getType().equalsIgnoreCase("DURING")) {
            relationships.add(new TemporalRelationship(new TemporalEvent("Event 1", "STARTDATE", "ANY"),
                            new TemporalEvent("Event 2", "STARTDATE", "ANY"), "LESSEQUAL", "EQUAL", "0", "DAY"));
            relationships.add(new TemporalRelationship(new TemporalEvent("Event 1", "ENDDATE", "ANY"),
                            new TemporalEvent("Event 2", "ENDDATE", "ANY"), "LESSEQUAL", "EQUAL", "0", "DAY"));
        }
        else {
            TemporalRelationship relationship = new TemporalRelationship(new TemporalEvent("Event 1", "", "ANY"),
                    new TemporalEvent("Event 2", "", "ANY"), "", "", "", "");

            setTemporalRelationshipTiming(reference.getType(), relationship);

            Range range = reference.getRange();
            if (range != null) {
                Object lowBoundObj = reference.getRange().getLow();
                Value lowBound = (lowBoundObj instanceof Value ? (Value) lowBoundObj : null);
                Object highBoundObj = reference.getRange().getHigh();
                Value highBound = (highBoundObj instanceof Value ? (Value) highBoundObj : null);

                setTemporalRelationshipOperator(lowBound, highBound, relationship);
                setTemporalRelationshipValue(lowBound, highBound, relationship);
            }
            relationships.add(relationship);
        }

        return new TemporalDefinition(relationships);
    }

    /**
     * Given a low and high bound value (either or both of which may be null), set the correct temporal bound within
     * our i2b2 TemporalDefinition object.  This includes both the value and the units.
     * @param lowBound
     * @param highBound
     * @param relationship The TemporalRelationship that will be updated with the correct bound
     * @throws PhemaUserException
     */
    private void setTemporalRelationshipValue(Value lowBound, Value highBound, TemporalRelationship relationship) throws PhemaUserException {
        // TODO: Support both bounds.  Cheating right now with one.
        Value bound = (lowBound == null ? highBound : lowBound);

        if (bound.getUnit().equals("h")) {
            relationship.setUnits("HOUR");
        }
        else if (bound.getUnit().equals("d")) {
            relationship.setUnits("DAY");
        }
        else if (bound.getUnit().equals("a")) {
            relationship.setUnits("YEAR");
        }
        else if (bound.getUnit().equals("mo")) {
            relationship.setUnits("MONTH");
        }
        else if (bound.getUnit().equals("min")) {
            relationship.setUnits("MINUTE");
        }
        else {
            throw new PhemaUserException(String.format("The PhEMA Executer does not support temporal conditions with a unit of %s", bound.getUnit()));
        }

        relationship.setValue(bound.getValue());
    }

    /**
     * Given a low and high bound value (either or both of which may be null), set the correct temporal definition
     * operator used by the i2b2 TemporalDefinition object.
     * @param lowBound
     * @param highBound
     * @param relationship
     * @throws PhemaUserException
     */
    private void setTemporalRelationshipOperator(Value lowBound, Value highBound, TemporalRelationship relationship) throws PhemaUserException {
        if (lowBound != null) {
            relationship.setSpanOperator(lowBound.isForceInclusive() ? "GREATEREQUAL" : "GREATER");
        }

        if (highBound != null) {
            relationship.setSpanOperator(highBound.isForceInclusive() ? "LESSEQUAL" : "LESS");
        }

        if (lowBound != null && highBound != null) {
            // TODO - we need to support low and high bounded ranges
            throw new PhemaUserException("Currently the PhEMA Executer does not support low and high bound temporal ranges.");
        }
        else if (lowBound == null && highBound == null) {
            throw new PhemaUserException("The PhEMA Executer was unable to convert the temporal operator into an i2b2 query.");
        }
    }

    /**
     * Convert a type of HQMF temporal relationship into the timing type used by i2b2 between two events.
     * @param temporalType
     * @param relationship
     * @throws PhemaUserException
     */
    private void setTemporalRelationshipTiming(String temporalType, TemporalRelationship relationship) throws PhemaUserException {
        if (temporalType.equals("CONCURRENT")) {
        }
        else if (temporalType.equals("DURING")) {
        }
        else if (temporalType.equals("EAE")) {
            relationship.getEvent1().setTiming("ENDDATE");
            relationship.getEvent2().setTiming("ENDDATE");
            relationship.setEventOperator("GREATER");
        }
        else if (temporalType.equals("EAS")) {
            relationship.getEvent1().setTiming("ENDDATE");
            relationship.getEvent2().setTiming("STARTDATE");
            relationship.setEventOperator("GREATER");
        }
        else if (temporalType.equals("EBE")) {
            relationship.getEvent1().setTiming("ENDDATE");
            relationship.getEvent2().setTiming("ENDDATE");
            relationship.setEventOperator("LESS");
        }
        else if (temporalType.equals("EBS")) {
            relationship.getEvent1().setTiming("ENDDATE");
            relationship.getEvent2().setTiming("STARTDATE");
            relationship.setEventOperator("LESS");
        }
        else if (temporalType.equals("ECW")) {
        }
        else if (temporalType.equals("ECWS")) {
            relationship.getEvent1().setTiming("ENDDATE");
            relationship.getEvent2().setTiming("STARTDATE");
            relationship.setEventOperator("EQUAL");
        }
        else if (temporalType.equals("EDU")) {
        }
        else if (temporalType.equals("OVERLAP")) {
        }
        else if (temporalType.equals("SAE")) {
            relationship.getEvent1().setTiming("STARTDATE");
            relationship.getEvent2().setTiming("ENDDATE");
            relationship.setEventOperator("GREATER");
        }
        else if (temporalType.equals("SBE")) {
            relationship.getEvent1().setTiming("STARTDATE");
            relationship.getEvent2().setTiming("ENDDATE");
            relationship.setEventOperator("LESS");
        }
        else if (temporalType.equals("SBS")) {
            relationship.getEvent1().setTiming("STARTDATE");
            relationship.getEvent2().setTiming("STARTDATE");
            relationship.setEventOperator("LESS");
        }
        else if (temporalType.equals("SCW")) {
        }
        else if (temporalType.equals("SCWE")) {
            relationship.getEvent1().setTiming("STARTDATE");
            relationship.getEvent2().setTiming("ENDDATE");
            relationship.setEventOperator("EQUAL");
        }
        else if (temporalType.equals("SDU")) {
        }

        // If we don't handle a temporal operator in the above if-else tree, the event timing will be blank.  We will do
        // a single check here for that, and throw an exception if either event timing is not set.
        if (relationship.getEvent1().getTiming().equals("") || relationship.getEvent2().getTiming().equals("")) {
            throw new PhemaUserException(
                    String.format("At this time, the %s temporal operator is not supported for i2b2 queries by PhEMA.", temporalType));
        }
    }

    /**
     * Utility function to translate the precondition requirement into a boolean
     * @param conjunction
     * @return
     * @throws PhemaUserException
     */
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
