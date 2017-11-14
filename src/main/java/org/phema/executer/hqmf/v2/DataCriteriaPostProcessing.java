package org.phema.executer.hqmf.v2;

import org.phema.executer.util.XmlHelpers;
import org.w3c.dom.Node;

import javax.xml.crypto.Data;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import java.util.ArrayList;

/**
 * Created by Luke Rasmussen on 9/21/17.
 *
 * Processing on data criteria after the initial extractions have taken place
 */
public class DataCriteriaPostProcessing {
    // Handles settings values after (most) values have been setup
    public static void postProcessing(DataCriteria criteria) throws Exception {
        extractCodeListPathAndResultValue(criteria);

        // Prefix ids that start with numerical values, and strip tokens from others
        criteria.setId(Utilities.stripTokens(criteria.getId()));
        ArrayList<String> childrenCriteria = criteria.getChildrenCriteria();
        if (childrenCriteria != null && childrenCriteria.size() > 0) {
            ArrayList<String> correctedChildrenCriteria = new ArrayList<String>();
            for (String child : childrenCriteria) {
                correctedChildrenCriteria.add(Utilities.stripTokens(child));
            }
            criteria.setChildrenCriteria(correctedChildrenCriteria);
        }

        // # append "_source" to the criteria since all the source criteria are separated from the non-source with the "_source" identifier
        // "_source" is added to the SDC ids so that we are not duplicating ids between source and non source data criteria lists
        // the derived source data criteria maintain their original ids since they are duplicated in the data criteria and source data criteria lists from the simple xml
        if (criteria.getDefinition() != null) {
            if (!criteria.getDefinition().equals("derived")
                    && !criteria.getDefinition().equals("satisfies_all")
                    && !criteria.getDefinition().equals("satisfies_any")) {
                criteria.setSourceDataCriteria(String.format("%s_source", criteria.getId()));
            }
        }
        if (criteria.getSourceDataCriteria() != null) {
            criteria.setSourceDataCriteria(Utilities.stripTokens(criteria.getSourceDataCriteria()));
        }
        if (criteria.getSpecificOccurrenceConst() != null) {
            criteria.setSpecificOccurrenceConst(Utilities.stripTokens(criteria.getSpecificOccurrenceConst()));
        }
        changeXProductToIntersection(criteria);
        handleDerivedSpecificOccurrences(criteria);
    }

    // Extract the code_list_xpath and the criteria's value from either the location related to the specific occurrence,
    // or from any of the template ids (if multiple exist)
    public static void extractCodeListPathAndResultValue(DataCriteria criteria) throws Exception {
        ArrayList<String> templateIds = criteria.getTemplateIds();
        if ((templateIds == null || templateIds.size() == 0) && criteria.isSpecificOccurrence()) {
            String template = XmlHelpers.getAttributeValue(criteria.getEntry(), criteria.getxPath(),
                    String.format("//id[@root='%s' and @extension='%s']/../templateId/item/@root",
                            criteria.getSourceDataCriteriaRoot(), criteria.getSourceDataCriteriaExtension()),
                    "");
            if (template.length() > 0) {
                ValueSetMapEntry mapping = ValueSetHelper.getMappingForTemplate(template);
                handleMappingTemplate(criteria, mapping);
            }
        }
        else {
            for (String template : templateIds) {
                ValueSetMapEntry mapping = ValueSetHelper.getMappingForTemplate(template);
                handleMappingTemplate(criteria, mapping);
                if (mapping != null) {
                    break;
                }
            }
        }
    }

    // Set the value and code_list_xpath using the template mapping held in the ValueSetHelper class
    private static void handleMappingTemplate(DataCriteria criteria, ValueSetMapEntry mapping) throws Exception {
        if (mapping != null) {
            if (mapping.getValuesetPath() != null && mapping.getValuesetPath().length() > 0) {
                criteria.setCodeListXPath(mapping.getValuesetPath());
            }
            if (mapping.getResultPath() != null && mapping.getResultPath().length() > 0) {
                criteria.setValue(DataCriteria.parseValue(criteria.getEntry(), criteria.getxPath(), mapping.getResultPath()));
            }
        }
    }

    // Changes XPRODUCT data criteria that has an associated tempalte(s) to an INTERSETION criteria.
    // UNION is used for all other cases.
    private static void changeXProductToIntersection(DataCriteria criteria) {
        // Need to handle grouper criteria that do not have template ids -- these will be union of and intersection criteria
        if (criteria.getTemplateIds() == null || criteria.getTemplateIds().size() == 0) {
            return;
        }

        // Change the XPRODUCT to an INTERSECT otherwise leave it as a UNION
        if (criteria.getDerivationOperator() != null && criteria.getDerivationOperator().equals(DataCriteria.XPRODUCT)) {
            criteria.setDerivationOperator(DataCriteria.INTERSECT);
        }
        if (criteria.getDescription() == null || criteria.getDescription().length() == 0) {
            criteria.setDescription((criteria.getDerivationOperator().equals(DataCriteria.INTERSECT) ? "Intersect" : "Union"));
        }
    }

    // Apply some elements from the reference_criteria to the derived specific occurrence
    private static void handleDerivedSpecificOccurrences(DataCriteria criteria) {
        if (criteria.getDefinition() == null || !criteria.getDefinition().equals("derived")) {
            return;
        }

        // remove "_source" from source data critera. It gets added in in SpecificOccurrenceAndSource but
        // when it gets added we have not yet determined the definition of the data criteria so we cannot
        // skip adding it.  Determining the definition before SpecificOccurrenceAndSource processes doesn't
        // work because we need to know if it is a specific occurrence to be able to figure out the definition
        if (criteria.getSourceDataCriteria() != null && criteria.getSourceDataCriteria().length() > 0) {
            criteria.setSourceDataCriteria(criteria.getSourceDataCriteria().replaceAll("_source", ""));
        }

        // Adds a child if none exists (specifically the source criteria)
        ArrayList<String> childrenCriteria = criteria.getChildrenCriteria();
        if (childrenCriteria == null || childrenCriteria.size() == 0) {
            childrenCriteria = new ArrayList<String>();
            childrenCriteria.add(criteria.getSourceDataCriteria());
            criteria.setChildrenCriteria(childrenCriteria);
        }

        if (childrenCriteria.size() != 1
                || (criteria.getSourceDataCriteria().length() > 0 && !childrenCriteria.get(0).equals(criteria.getSourceDataCriteria()))) {
            return;
        }

        // if child.first is nil, it will be caught in the second statement
        if (!criteria.getDataCriteriaReferences().containsKey(childrenCriteria.get(0))) {
            return;
        }

        DataCriteria referenceCriteria = criteria.getDataCriteriaReferences().get(childrenCriteria.get(0));
        if (referenceCriteria == null) {
            return;
        }
        // easier to track than all testing all features of these cases
        criteria.setDerivedSpecificOccurrenceVariable(true);
        if (criteria.getSubsetOperators() == null || criteria.getSubsetOperators().size() == 0) {
            criteria.setSubsetOperators(referenceCriteria.getSubsetOperators());
        }
        if (criteria.getDerivationOperator() == null || criteria.getDerivationOperator().length() == 0) {
            criteria.setDerivationOperator(referenceCriteria.getDerivationOperator());
        }
        criteria.setDescription(referenceCriteria.getDescription());
        criteria.setVariable(referenceCriteria.isVariable());
    }
}
