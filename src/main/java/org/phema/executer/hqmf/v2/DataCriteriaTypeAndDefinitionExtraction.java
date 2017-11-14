package org.phema.executer.hqmf.v2;

import org.json.simple.parser.ParseException;
import org.phema.executer.hqmf.models.HqmfTemplateHelper;
import org.phema.executer.hqmf.models.Template;
import org.phema.executer.util.XmlHelpers;
import org.w3c.dom.Node;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import java.util.ArrayList;

import static org.phema.executer.hqmf.models.DataCriteria.*;

/**
 * Created by Luke Rasmussen on 9/21/17.
 *
 * Extracts the type, and modifies the data criteria, based on the template id or definition
 */
public class DataCriteriaTypeAndDefinitionExtraction {
    private static final String VARIABLE_TEMPLATE = "0.1.2.3.4.5.6.7.8.9.1";
    private static final String SATISFIES_ANY_TEMPLATE = "2.16.840.1.113883.10.20.28.3.108";
    private static final String SATISFIES_ALL_TEMPLATE = "2.16.840.1.113883.10.20.28.3.109";

    public static Object extractDefinitionFromTemplateOrType(DataCriteria criteria) throws ParseException, XPathExpressionException {
        // Try to determine what kind of data criteria we are dealing with
        // First we look for a template id and if we find one just use the definition
        // status and negation associated with that
        // If no template id or not one we recognize then try to determine type from
        // the definition element
        boolean found = extractDefinitionFromTemplateId(criteria);
        if (!found) {
            extractDefinitionFromType(criteria);
        }

        return null;
    }

    // Given a template id, derive (if available) the definition for the template.
    // The definitions are stored in hqmf-model/data_criteria.json.
    public static boolean extractDefinitionFromTemplateId(DataCriteria criteria) throws ParseException {
        boolean found = false;
        ArrayList<String> templateIds = criteria.getTemplateIds();
        for (String templateId : templateIds) {
            Template template = HqmfTemplateHelper.definitionForTemplateId(templateId, "r2");
            if (template != null) {
                criteria.setDefinition(template.getDefinition());
                criteria.setStatus((template.getStatus().length() > 0) ? template.getStatus() : null);
                found = found || true;
            }
            else if (!found) {
                found = handleKnownTemplateId(criteria, templateId);
            }
        }

        return found;
    }

    // Given a template id, modify the variables inside the data criteria to reflect the template
    public static boolean handleKnownTemplateId(DataCriteria criteria, String templateId) {
        switch (templateId) {
            case VARIABLE_TEMPLATE:
                if (criteria.getDerivationOperator().equals(XPRODUCT)) {
                    criteria.setDerivationOperator(INTERSECT);
                }
                if (criteria.getDefinition().length() == 0) {
                    criteria.setDefinition("derived");
                }
                criteria.setVariable(true);
                criteria.setNegation(false);
                break;
            case SATISFIES_ANY_TEMPLATE:
                criteria.setDefinition(SATISFIES_ANY);
                criteria.setNegation(false);
                break;
            case SATISFIES_ALL_TEMPLATE:
                criteria.setDefinition(SATISFIES_ALL);
                criteria.setDerivationOperator(INTERSECT);
                criteria.setNegation(false);
                break;
            default:
                return false;
        }

        return true;
    }

    // Extract the definition (sometimes status, sometimes other elements) of the data criteria based on the type
    public static void extractDefinitionFromType(DataCriteria criteria) throws XPathExpressionException {
        // If we have a specific occurrence of a variable, pull attributes from the reference.
        // IDEA set this up to be called from dc_specific_and_source_extract, the number of
        // fields changed by handle_specific_variable_ref may pose an issue.
        if (criteria.isVariable() && criteria.isSpecificOccurrence()) {
            extractInformationForSpecificVariable(criteria);
        }
    }

    // Extracts information from a reference for a specific variable
    public static void extractInformationForSpecificVariable(DataCriteria criteria) throws XPathExpressionException {
        XPath xPath = criteria.getxPath();
        Node reference = (Node)xPath.evaluate("./*/outboundRelationship/criteriaReference", criteria.getEntry(), XPathConstants.NODE);
        if (reference == null) {
            return;
        }

        String refId = Utilities.stripTokens(
                String.format("%s_%s",
                        XmlHelpers.getAttributeValue(reference, xPath, "id/@extension", ""),
                        XmlHelpers.getAttributeValue(reference, xPath, "id/@root", "")));
        if (refId == null || refId.length() == 0) {
            return;
        }

        DataCriteria referenceCriteria = criteria.getDataCriteriaReferences().get(refId);
        // if the reference is derived, pull from the original variable
        if (referenceCriteria != null && referenceCriteria.getDefinition().equals("derived")) {
            referenceCriteria = criteria.getDataCriteriaReferences().get(String.format("GROUP_%s", refId));
        }
        if (referenceCriteria == null) {
            return;
        }

        handleSpecificVariableRef(criteria, referenceCriteria);
    }

    // Apply additional information to a specific occurrence's elements from the criteria it references.
    public static void handleSpecificVariableRef(DataCriteria criteria, DataCriteria referenceCriteria) {
        // If there are no referenced children, then it's a variable representing
        // a single data criteria, so just reference it
        if (referenceCriteria.getChildrenCriteria() == null || referenceCriteria.getChildrenCriteria().size() == 0) {
            // @children_criteria = [reference_criteria.id]
            criteria.setChildrenCriteria(new ArrayList<String>() {{ add(referenceCriteria.getId()); }});
        }
        // otherwise pull all the data criteria info from the reference
        else {
            criteria.setFieldValues(referenceCriteria.getFieldValues());
            criteria.setTemporalReferences(referenceCriteria.getTemporalReferences());
            criteria.setSubsetOperators(referenceCriteria.getSubsetOperators());
            criteria.setDerivationOperator(referenceCriteria.getDerivationOperator());
            criteria.setDefinition(referenceCriteria.getDefinition());
            criteria.setDescription(referenceCriteria.getDescription());
            criteria.setStatus(referenceCriteria.getStatus());
            criteria.setChildrenCriteria(referenceCriteria.getChildrenCriteria());
        }
    }
}
