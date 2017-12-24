package org.phema.executer.translator;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigObject;
import org.phema.executer.models.i2b2.I2b2OverrideRule;
import org.phema.executer.models.i2b2.I2b2TerminologyRule;
import org.phema.executer.models.i2b2.I2b2ValueSetRule;
import org.phema.executer.util.ConfigHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Luke Rasmussen on 12/22/17.
 */
public class ValueSetToI2b2Ontology {
    private static final String DEFAULT_DELIMITER = ":";

    private ArrayList<I2b2TerminologyRule> terminologyRules;
    private ArrayList<I2b2ValueSetRule> valueSetRules;
    private ArrayList<I2b2OverrideRule> overrideRules;

    /**
     * Initialize the value set translator
     * @param config
     * @throws Exception
     */
    public void initialize(Config config) throws Exception {
        initializeTerminologyRules(config);
        initializeValueSetRules(config);
        initializeOverrideRules(config);
    }

    private void initializeTerminologyRules(Config config) throws Exception {
        this.terminologyRules = new ArrayList<>();
        List<? extends ConfigObject> ruleList = config.getObjectList("i2b2.valueSetMapping.terminologyRule");
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
            String destinationCodeMatch = ConfigHelper.getStringValue(ruleObject, "destinationTerminology.codeReplace.match", "");
            String destinationCodeReplace = ConfigHelper.getStringValue(ruleObject, "destinationTerminology.codeReplace.replaceWith", "");
            terminologyRules.add(new I2b2TerminologyRule(sourceTerminologyName, destinationTerminologyPrefix, destinationTerminologyDelimiter,
                    destinationCodeMatch, destinationCodeReplace));
        }
    }

    private void initializeValueSetRules(Config config) throws Exception {
        this.valueSetRules = new ArrayList<>();
        List<? extends ConfigObject> ruleList = config.getObjectList("i2b2.valueSetMapping.valueSetRule");
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
        List<? extends ConfigObject> ruleList = config.getObjectList("i2b2.valueSetMapping.overrideRule");
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
