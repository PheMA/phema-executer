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
import java.util.HashMap;

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

    private static final HashMap<String, String> DEMOGRAPHIC_TRANSLATION = new HashMap<String, String>() {{
        put ("21112-8", "patient_characteristic_birthdate");
        put ("424144002", "patient_characteristic_age");
        put ("263495000", "patient_characteristic_gender");
        put ("102902016", "patient_characteristic_languages");
        put ("125680007", "patient_characteristic_marital_status");
        put ("103579009", "patient_characteristic_race");
    }};

    public static Object extractDefinitionFromTemplateOrType(DataCriteria criteria) throws Exception {
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
    public static void extractDefinitionFromType(DataCriteria criteria) throws Exception {
        // If we have a specific occurrence of a variable, pull attributes from the reference.
        // IDEA set this up to be called from dc_specific_and_source_extract, the number of
        // fields changed by handle_specific_variable_ref may pose an issue.
        if (criteria.isVariable() && criteria.isSpecificOccurrence()) {
            extractInformationForSpecificVariable(criteria);
        }

        Node criteriaNode = criteria.getEntry();
        XPath criteriaXPath = criteria.getxPath();
        Node grouperNode = (Node)criteriaXPath.evaluate("./cda:grouperCriteria", criteriaNode, XPathConstants.NODE);
        if (grouperNode != null) {
            String definition = criteria.getDefinition();
            if (definition == null || definition.length() == 0) {
                criteria.setDefinition("derived");
                return;
            }
        }

        // See if we can find a match for the entry definition value and status.
        String entryType = XmlHelpers.getAttributeValue(criteriaNode, criteriaXPath, "./*/cda:definition/*/cda:id/@extension", null);
        handleEntryType(criteria, criteriaNode, criteriaXPath, entryType);
    }

    // Generate the definition and/or status from the entry type in most cases.
    // If the entry type is nil, and the value is a specific occurrence, more parsing may be necessary.
    private static void handleEntryType(DataCriteria criteria, Node entry, XPath xPath, String entryType) throws Exception {
        // settings is required to trigger exceptions, which set the definition
        try {
            DataCriteria.getSettingsForDefinition(entryType, criteria.getStatus());
            criteria.setDefinition(entryType);
        }
        catch (Exception exc) {
            // if no exact match then try a string match just using entry definition value
            if (entryType == null) {
                definitionForNilEntry(criteria, entry, xPath);
            }
            else if (entryType.equals("Medication") || entryType.equals("Medications")) {
                criteria.setDefinition("medication");
                if (criteria.getStatus() == null || criteria.getStatus().length() == 0) {
                    criteria.setStatus("active");
                }
            }
            else if (entryType.equals("RX")) {
                criteria.setDefinition("medication");
                if (criteria.getStatus() == null || criteria.getStatus().length() == 0) {
                    criteria.setStatus("dispensed");
                }
            }
            else {
                criteria.setDefinition(extractDefinitionFromEntryType(criteria, entry, xPath, entryType));
            }
        }
    }

    // If there is no entry type, extract the entry type from what it references, and extract additional information for
    // specific occurrences. If there are no outbound references, print an error and mark it as variable.
    private static void definitionForNilEntry(DataCriteria criteria, Node entry, XPath xPath) throws XPathExpressionException {
        Node reference = (Node)xPath.evaluate("./*/cda:outboundRelationship/cda:criteriaReference", entry, XPathConstants.NODE);
        String refId = null;
        if (reference != null) {
            refId = String.format("%s_%s",
                    XmlHelpers.getAttributeValue(reference, xPath, "cda:id/@extension", ""),
                    XmlHelpers.getAttributeValue(reference, xPath, "cda:id/@root", ""));
        }

        if (refId != null) {
            refId = Utilities.stripTokens(refId);
            DataCriteria referenceCriteria = criteria.getDataCriteriaReferences().getOrDefault(refId, null);
            if (referenceCriteria != null) {
                // we only want to copy the reference criteria definition, status, and code_list_id if this is this is not a grouping criteria (i.e., there are no children)
                if (criteria.getChildrenCriteria() == null || criteria.getChildrenCriteria().size() == 0) {
                    criteria.setDefinition(referenceCriteria.getDefinition());
                    criteria.setStatus(referenceCriteria.getStatus());
                    if (criteria.isSpecificOccurrence()) {
                        criteria.setTitle(referenceCriteria.getTitle());
                        criteria.setDescription(referenceCriteria.getDescription());
                        criteria.setCodeListId(referenceCriteria.getCodeListId());
                    }
                }
                else {
                    // if this is a grouping data criteria (has children) mark it as derived and only pull title and description from the reference criteria
                    criteria.setDefinition("derived");
                    if (criteria.isSpecificOccurrence()) {
                        criteria.setTitle(referenceCriteria.getTitle());
                        criteria.setDescription(referenceCriteria.getDescription());
                    }
                }
            }
            else {
                criteria.setDefinition("variable");
            }
        }
    }

    // Given an entry type (which describes the criteria's purpose) return the appropriate defintion
    private static String extractDefinitionFromEntryType(DataCriteria criteria, Node entry, XPath xPath, String entryType) throws Exception {
        if (entryType.equals("Problem") || entryType.equals("Problems")) {
            return "diagnosis";
        }
        else if (entryType.equals("Encounter") || entryType.equals("Encounters")) {
            return "encounter";
        }
        else if (entryType.equals("LabResults") || entryType.equals("Results")) {
            return "laboratory_test";
        }
        else if (entryType.equals("Procedure") || entryType.equals("Procedures")) {
            return "procedure";
        }
        else if (entryType.equals("Demographics")) {
            return definitionForDemographic(criteria, entry, xPath);
        }
        else if (entryType.equals("Derived")) {
            return "derived";
        }

        throw new Exception(String.format("Unknown data criteria template identifier [%s]", entryType));
    }

    // Return the definition for a known subset of patient characteristics
    private static String definitionForDemographic(DataCriteria criteria, Node entry, XPath xPath) throws Exception {
        String demographicType = XmlHelpers.getAttributeValue(entry, xPath, "./cda:observationCriteria/cda:code/@code", "");
        if (DEMOGRAPHIC_TRANSLATION.containsKey(demographicType)) {
            return DEMOGRAPHIC_TRANSLATION.get("demographicType");
        }

        throw new Exception(String.format("Unknown demographic identifier [%s]", demographicType));
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
                        XmlHelpers.getAttributeValue(reference, xPath, "cda:id/@extension", ""),
                        XmlHelpers.getAttributeValue(reference, xPath, "cda:id/@root", "")));
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
