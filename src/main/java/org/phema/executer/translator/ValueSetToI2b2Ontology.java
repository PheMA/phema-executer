package org.phema.executer.translator;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigObject;
import org.phema.executer.exception.PhemaUserException;
import org.phema.executer.hqmf.v2.DataCriteria;
import org.phema.executer.hqmf.v2.Range;
import org.phema.executer.hqmf.v2.TemporalReference;
import org.phema.executer.hqmf.v2.Value;
import org.phema.executer.i2b2.OntologyService;
import org.phema.executer.i2b2.ProjectManagementService;
import org.phema.executer.models.i2b2.Concept;
import org.phema.executer.models.i2b2.I2b2OverrideRule;
import org.phema.executer.models.i2b2.I2b2TerminologyRule;
import org.phema.executer.models.i2b2.I2b2ValueSetRule;
import org.phema.executer.util.ConfigHelper;
import org.phema.executer.valueSets.models.Member;
import org.phema.executer.valueSets.models.ValueSet;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Luke Rasmussen on 12/22/17.
 */
public class ValueSetToI2b2Ontology {
    private static final String DEFAULT_DELIMITER = ":";

    private ArrayList<I2b2TerminologyRule> terminologyRules;
    private ArrayList<I2b2ValueSetRule> valueSetRules;
    private ArrayList<I2b2OverrideRule> overrideRules;
    private OntologyService ontService;

    public class TranslationResult {
        public ArrayList<Member> UnmappedMembers;
        public HashMap<Member, List<Concept>> MappedMembers;
        public HashMap<Member, List<Concept>> FilteredOutMembers;
        public ArrayList<Concept> DistinctMappedConcepts;

        public TranslationResult() {
            UnmappedMembers = new ArrayList<>();
            MappedMembers = new HashMap<>();
            FilteredOutMembers = new HashMap<>();
            DistinctMappedConcepts = new ArrayList<>();
        }
    }

    private class BasecodeRuleMatch {
        public String basecode;
        public I2b2TerminologyRule rule;

        public BasecodeRuleMatch(String basecode, I2b2TerminologyRule rule) {
            this.basecode = basecode;
            this.rule = rule;
        }
    }

    /**
     * Initialize the value set translator
     * @param config
     * @throws Exception
     */
    public void initialize(Config config, OntologyService ontService) throws Exception {
        this.ontService = ontService;
        initializeTerminologyRules(config);
        initializeValueSetRules(config);
        initializeOverrideRules(config);
    }

    public TranslationResult translate(ValueSet valueSet) throws PhemaUserException {
        if (ontService == null) {
            throw new PhemaUserException("No active i2b2 instance was provided.  Please make sure that your username and password work correctly with the configured i2b2 instance.");
        }

        ArrayList<Member> members = valueSet.getMembers();
        ArrayList<Concept> concepts = new ArrayList<>();
        TranslationResult result = new TranslationResult();
        for (Member member : members) {
            BasecodeRuleMatch basecodeRule = translateValueSetMemberToBasecode(member);
            ArrayList<ArrayList<Concept>> filterResults = filterFoundConcepts(basecodeRule);
            ArrayList<Concept> foundConcepts = filterResults.get(0);
            ArrayList<Concept> filteredOutConcepts = filterResults.get(1);
            if (foundConcepts.isEmpty()) {
                result.UnmappedMembers.add(member);
            }
            else {
                result.MappedMembers.put(member, foundConcepts);
                concepts.addAll(foundConcepts);
            }

            // We separately track any concepts that were filtered out so that we can
            // report to the user if their filter is perhaps too restrictive.  Note
            // that this is only set if a filter is defined.
            if (!filteredOutConcepts.isEmpty()) {
                result.FilteredOutMembers.put(member, filteredOutConcepts);
            }
        }

        result.DistinctMappedConcepts = distinctConceptList(concepts);
        return result;
    }

    public ArrayList<Concept> translateAgeRequirement(DataCriteria criterion) throws PhemaUserException {
        ArrayList<Concept> ageConcepts = new ArrayList<>();

        if (!Objects.equals(criterion.getDefinition(), "patient_characteristic_birthdate")) {
            throw new PhemaUserException("A non-patient age criteria was treated as an age requirement.  This may be an internal error within the PhEMA Executer, or may be caused by an error in your HQMF file.");
        }

        TemporalReference temporalReference = criterion.getTemporalReferences().get(0);
        Range range = temporalReference.getRange();
        Value lowRange = null;
        Value highRange = null;
        if (range.getLow() != null && range.getLow().getClass().equals(Value.class)) {
            lowRange = (Value)range.getLow();
        }
        if (range.getHigh() != null && range.getHigh().getClass().equals(Value.class)) {
            highRange = (Value)range.getHigh();
        }

        // TODO: Handle more than just years
        if (lowRange != null && !lowRange.getUnit().equals("a")) {
            throw new PhemaUserException("Currently the PhEMA Executer only supports ages in years.");
        }
        if (highRange != null && !highRange.getUnit().equals("a")) {
            throw new PhemaUserException("Currently the PhEMA Executer only supports ages in years.");
        }

        int lowerBound = (lowRange != null ? Integer.parseInt(lowRange.getValue()) : 0);
        int upperBound = (highRange != null ? Integer.parseInt(highRange.getValue()) : 89);  // Going for HIPAA compliance here by default

        // Do some quick sanity checks on the data
        if (lowerBound > upperBound) {
            throw new PhemaUserException(String.format("There is an error within your HQMF document.  We expect the lower bound (%d) to be less than or equal to the upper bound (%d) for an age requirement, but this is not the case.  Please contact the original phenotype author that generated the HQMF document.",
                    lowerBound, upperBound));
        }

        for (int counter = lowerBound; counter <= upperBound; counter++) {
            Member temp = new Member();
            temp.setCodeSystem("Age");
            temp.setCode(Integer.toString(counter));
            BasecodeRuleMatch basecodeRule = translateValueSetMemberToBasecode(temp);
            ArrayList<ArrayList<Concept>> filterResults = filterFoundConcepts(basecodeRule);
            ArrayList<Concept> foundConcepts = filterResults.get(0);
            ArrayList<Concept> filteredOutConcepts = filterResults.get(1);
            ageConcepts.addAll(foundConcepts);
        }

        return distinctConceptList(ageConcepts);
    }

    private ArrayList<Concept> distinctConceptList(ArrayList<Concept> concepts) {
        ArrayList<Concept> distinctConcepts = new ArrayList<>();
        if (concepts == null) {
            return distinctConcepts;
        }

        HashSet<String> baseCodes = new HashSet<>();
        for (Concept concept : concepts) {
            if (baseCodes.contains(concept.getBaseCode())) {
                continue;
            }

            distinctConcepts.add(concept);
            baseCodes.add(concept.getBaseCode());
        }

        return distinctConcepts;
    }

    private ArrayList<ArrayList<Concept>> filterFoundConcepts(BasecodeRuleMatch basecodeRule) throws PhemaUserException {
        //String basecode = basecodeRule.basecode;
        ArrayList<Concept> concepts = new ArrayList<>();
        ArrayList<Concept> filteredOutConcepts = new ArrayList<>();
        ArrayList<Concept> foundConcepts = this.ontService.getCodeInfo(basecodeRule.basecode);
        if (foundConcepts != null && foundConcepts.size() > 0) {
            // If we have a mapped rule that includes a setting for the ontology path that we should restrict to, make
            // sure each of the entries we have found is in that path (and remove the ones that aren't)
            if (basecodeRule.rule != null && basecodeRule.rule.getRestrictToOntologyPath() != null) {
                String restrictToPath = basecodeRule.rule.getRestrictToOntologyPath();
                concepts.addAll(foundConcepts.stream()
                        .filter(x -> x.getKey().contains(restrictToPath))
                        .collect(Collectors.toList()));
                filteredOutConcepts.addAll(foundConcepts.stream()
                        .filter(x -> !x.getKey().contains(restrictToPath))
                        .collect(Collectors.toList()));
            }
            else {
                concepts.addAll(foundConcepts);
            }
        }

        return new ArrayList<ArrayList<Concept>>() {{ add(concepts); add(filteredOutConcepts); }};
    }

    /**
     * @param member
     * @return
     */
    private BasecodeRuleMatch translateValueSetMemberToBasecode(Member member) {
        // Default is codesystem + : + code
        String terminology = member.getCodeSystem();
        String delimiter = DEFAULT_DELIMITER;
        String term = member.getCode();

        // First apply any terminology rules that apply to this member.
        I2b2TerminologyRule rule = null;
        if (this.terminologyRules != null) {
            Optional<I2b2TerminologyRule> ruleResult = this.terminologyRules.stream().filter(x -> x.getSourceTerminologyName().equals(member.getCodeSystem())).findFirst();
            if (ruleResult.isPresent()) {
                // Check to see if the appropriate parts of the rule are defined.  If not, use the defaults.
                rule = ruleResult.get();
                String ruleTerminology = rule.getDestinationTerminologyPrefix();
                if (ruleTerminology != null) {
                    terminology = ruleTerminology;
                }

                String ruleDelimiter = rule.getDestinationTerminologyDelimiter();
                if (ruleDelimiter != null) {
                    delimiter = ruleDelimiter;
                }
            }
        }

        return new BasecodeRuleMatch(String.format("%s%s%s", terminology, delimiter, term), rule);
    }

    private void initializeTerminologyRules(Config config) throws Exception {
        this.terminologyRules = new ArrayList<>();
        if (!config.hasPath("execution.i2b2.valueSetMapping.terminologyRules")) {
            return;
        }

        List<? extends ConfigObject> ruleList = config.getObjectList("execution.i2b2.valueSetMapping.terminologyRules");
        if (ruleList == null || ruleList.size() == 0) {
            return;
        }

        for (ConfigObject ruleObject : ruleList) {
            if (!ruleObject.containsKey("sourceTerminologyName")) {
                throw new Exception("You must specify the sourceTerminologyName within a terminologyRule");
            }

            String sourceTerminologyName = ruleObject.get("sourceTerminologyName").unwrapped().toString();
            String destinationTerminologyPrefix = ConfigHelper.getStringValue(ruleObject, "destinationTerminology.prefix", sourceTerminologyName);
            String destinationTerminologyDelimiter = ConfigHelper.getStringValue(ruleObject, "destinationTerminology.delimiter", DEFAULT_DELIMITER);
            String destinationCodeMatch = ConfigHelper.getStringValue(ruleObject, "destinationTerminology.codeReplace.match", null);
            String destinationCodeReplace = ConfigHelper.getStringValue(ruleObject, "destinationTerminology.codeReplace.replaceWith", null);
            String restrictToOntologyPath = ConfigHelper.getStringValue(ruleObject, "destinationTerminology.restrictToOntologyPath", null);
            terminologyRules.add(new I2b2TerminologyRule(sourceTerminologyName, destinationTerminologyPrefix, destinationTerminologyDelimiter,
                    destinationCodeMatch, destinationCodeReplace, restrictToOntologyPath));
        }
    }

    private void initializeValueSetRules(Config config) throws Exception {
        this.valueSetRules = new ArrayList<>();
        if (!config.hasPath("execution.i2b2.valueSetMapping.valueSetRules")) {
            return;
        }

        List<? extends ConfigObject> ruleList = config.getObjectList("execution.i2b2.valueSetMapping.valueSetRules");
        if (ruleList == null || ruleList.size() == 0) {
            return;
        }

        for (ConfigObject ruleObject : ruleList) {
            if (!ruleObject.containsKey("valueSetOid")) {
                throw new Exception("You must specify the valueSetOid within a valueSetRule");
            }

            //TODO: Finish implementation
        }
    }

    private void initializeOverrideRules(Config config) throws Exception {
        this.overrideRules = new ArrayList<>();
        if (!config.hasPath("execution.i2b2.valueSetMapping.overrideRules")) {
            return;
        }

        List<? extends ConfigObject> ruleList = config.getObjectList("execution.i2b2.valueSetMapping.overrideRules");
        if (ruleList == null || ruleList.size() == 0) {
            return;
        }

        for (ConfigObject ruleObject : ruleList) {
            if (!ruleObject.containsKey("fileName")) {
                throw new Exception("You must specify the fileName within a overrideRule");
            }

            //TODO: Finish implementation
        }
    }
}
