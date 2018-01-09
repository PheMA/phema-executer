package org.phema.executer.translator;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigObject;
import org.phema.executer.exception.PhemaUserException;
import org.phema.executer.i2b2.OntologyService;
import org.phema.executer.i2b2.ProjectManagementService;
import org.phema.executer.models.i2b2.Concept;
import org.phema.executer.models.i2b2.I2b2OverrideRule;
import org.phema.executer.models.i2b2.I2b2TerminologyRule;
import org.phema.executer.models.i2b2.I2b2ValueSetRule;
import org.phema.executer.util.ConfigHelper;
import org.phema.executer.valueSets.models.Member;
import org.phema.executer.valueSets.models.ValueSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by Luke Rasmussen on 12/22/17.
 */
public class ValueSetToI2b2Ontology {
    private static final String DEFAULT_DELIMITER = ":";

    private ArrayList<I2b2TerminologyRule> terminologyRules;
    private ArrayList<I2b2ValueSetRule> valueSetRules;
    private ArrayList<I2b2OverrideRule> overrideRules;
    private OntologyService ontService;

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

    public ArrayList<Concept> translate(ValueSet valueSet) throws PhemaUserException {
        if (ontService == null) {
            throw new PhemaUserException("No active i2b2 instance was provided.  Please make sure that your username and password work correctly with the configured i2b2 instance.");
        }

        ArrayList<Member> members = valueSet.getMembers();
        ArrayList<Concept> concepts = new ArrayList<>();
        for (Member member : members) {
            String basecode = translateValueSetMemberToBasecode(member);
            concepts.addAll(this.ontService.getCodeInfo(basecode));
        }

        return concepts;
    }

    private String translateValueSetMemberToBasecode(Member member) {
        // Default is codesystem + : + code
        String terminology = member.getCodeSystem();
        String delimiter = DEFAULT_DELIMITER;
        String term = member.getCode();

        // First apply any terminology rules that apply to this member.
        if (this.terminologyRules != null) {
            Optional<I2b2TerminologyRule> ruleResult = this.terminologyRules.stream().filter(x -> x.getSourceTerminologyName().equals(member.getCodeSystem())).findFirst();
            if (ruleResult.isPresent()) {
                // Check to see if the appropriate parts of the rule are defined.  If not, use the defaults.
                I2b2TerminologyRule rule = ruleResult.get();
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

        return String.format("%s%s%s", terminology, delimiter, term);
    }

    private void initializeTerminologyRules(Config config) throws Exception {
        this.terminologyRules = new ArrayList<>();
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
            terminologyRules.add(new I2b2TerminologyRule(sourceTerminologyName, destinationTerminologyPrefix, destinationTerminologyDelimiter,
                    destinationCodeMatch, destinationCodeReplace));
        }
    }

    private void initializeValueSetRules(Config config) throws Exception {
        this.valueSetRules = new ArrayList<>();
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
        this.valueSetRules = new ArrayList<>();
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
