package org.phema.executer.hqmf.v2;

import java.util.ArrayList;

/**
 * Created by Luke Rasmussen on 9/21/17.
 *
 * Extracts the type, and modifies the data criteria, based on the template id or definition
 */
public class DataCriteriaTypeAndDefinitionExtraction {
    private static final String VARIABLE_TEMPLATE = "0.1.2.3.4.5.6.7.8.9.1";
    private static final String SATISFIES_ANY_TEMPLATE = "2.16.840.1.113883.10.20.28.3.108";
    private static final String SATISFIES_ALL_TEMPLATE = "2.16.840.1.113883.10.20.28.3.109";

    public static Object extractDefinitionFromTemplateOrType(ArrayList<String> templateIds) {
        // Try to determine what kind of data criteria we are dealing with
        // First we look for a template id and if we find one just use the definition
        // status and negation associated with that
        // If no template id or not one we recognize then try to determine type from
        // the definition element
        boolean found = extractDefinitionFromTemplateId();
        if (!found) {
            extractDefinitionFromType();
        }
    }

    // Given a template id, derive (if available) the definition for the template.
    // The definitions are stored in hqmf-model/data_criteria.json.
    public static boolean extractDefinitionFromTemplateId(ArrayList<String> templateIds) {
        boolean found = false;
        for (String templateId : templateIds) {
            Object defs = DataCriteria.definitionForTemplateId(templateId, "r2");
            if (defs != null) {
                this.definition = defs.get("definition");
                this.status = (defs.get("status").length() > 0) ? defs.get("status") : null;
                found = found || true;
            }
            else {
                found = found || handleKnownTemplateId(templateId);
            }
        }
    }

    /*

        # Given a template id, modify the variables inside this data criteria to reflect the template
    def handle_known_template_id(template_id)
      case template_id
      when VARIABLE_TEMPLATE
        @derivation_operator = HQMF::DataCriteria::INTERSECT if @derivation_operator == HQMF::DataCriteria::XPRODUCT
        @definition ||= 'derived'
        @variable = true
        @negation = false
      when SATISFIES_ANY_TEMPLATE
        @definition = HQMF::DataCriteria::SATISFIES_ANY
        @negation = false
      when SATISFIES_ALL_TEMPLATE
        @definition = HQMF::DataCriteria::SATISFIES_ALL
        @derivation_operator = HQMF::DataCriteria::INTERSECT
        @negation = false
      else
        return false
      end
      true
    end
     */
}
