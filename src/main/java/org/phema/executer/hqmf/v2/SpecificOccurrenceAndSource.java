package org.phema.executer.hqmf.v2;

import org.phema.executer.util.XmlHelpers;
import org.w3c.dom.Node;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Luke Rasmussen on 9/13/17.
 *
 * Handles various tasks that the Data Criteria needs performed to obtain and
 # modify specific occurrences
 */
public class SpecificOccurrenceAndSource {
    private Node entry;
    private XPath xPath;
    private String id;
    private String localVariableName;
    private boolean isVariable;
    private HashMap<String, DataCriteria> dataCriteriaReferences;
    private HashMap<String, String> occurrencesMap;

    public SpecificOccurrenceAndSource(Node entry, String id, String localVariableName, HashMap<String, DataCriteria> dataCriteriaReferences, HashMap<String, String> occurrencesMap) {
        this.entry = entry;
        this.id = id;
        this.localVariableName = localVariableName;
        this.isVariable = DataCriteria.extractVariable(this.localVariableName, this.id);
        this.occurrencesMap = occurrencesMap;
        this.dataCriteriaReferences = dataCriteriaReferences;

        if (this.entry != null) {
            this.xPath = XmlHelpers.createXPath(this.entry.getOwnerDocument());
        }
    }

    // Retrieve the specific occurrence and source data criteria information (or just source if there is no specific)
    public HashMap<String, String> extractSpecificOccurrencesAndSourceDataCriteria() throws Exception {
        Node specificDef = (Node)xPath.evaluate("./*/cda:outboundRelationship[@typeCode=\"OCCR\"]", this.entry, XPathConstants.NODE);
        Node sourceDef = (Node)xPath.evaluate("./*/cda:outboundRelationship[cda:subsetCode/@code=\"SOURCE\"]", this.entry, XPathConstants.NODE);
        if (specificDef != null) {
            String sourceDataCriteriaExtension = XmlHelpers.getAttributeValue(specificDef, this.xPath, "./cda:criteriaReference/cda:id/@extension", "");
            String sourceDataCriteriaRoot = XmlHelpers.getAttributeValue(specificDef, this.xPath, "./cda:criteriaReference/cda:id/@root", "");
            Object occurrenceCriteria = this.dataCriteriaReferences.get(Utilities.stripTokens(String.format("%s_%s", sourceDataCriteriaExtension, sourceDataCriteriaRoot)));
            if (occurrenceCriteria == null) {
                return null;
            }
            String specificOccurrenceConst = XmlHelpers.getAttributeValue(specificDef, this.xPath, "./cda:localVariableName/@controlInformationRoot", "");
            String specificOccurrence = XmlHelpers.getAttributeValue(specificDef, this.xPath, "./cda:localVariableName/@controlInformationExtension", "");

            // FIXME: Remove debug statements after cleaning up occurrence handling
            // build regex for extracting alpha-index of specific occurrences
            String occurrenceIdentifier = obtainOccurrenceIdentifier(Utilities.stripTokens(this.id),
                Utilities.stripTokens(this.localVariableName),
                Utilities.stripTokens(sourceDataCriteriaExtension),
                this.isVariable);

            return handleSpecificAndSource(occurrenceIdentifier, sourceDataCriteriaExtension, sourceDataCriteriaRoot,
                    specificOccurrenceConst, specificOccurrence);
        }
        else if (sourceDef != null) {
            String extension = XmlHelpers.getAttributeValue(sourceDef, this.xPath, "./cda:criteriaReference/cda:id/@extension", "");
            String root = XmlHelpers.getAttributeValue(sourceDef, this.xPath, "./cda:criteriaReference/cda:id/@root", "");
            // Return the source data criteria itself, the rest will be blank
            return new HashMap<String, String>() {{
                put("sourceDataCriteria", String.format("%s_%s_source", extension, root));
                put("sourceDataCriteriaRoot", root);
                put("sourceDataCriteriaExtension", extension);
                put("specificOccurrence", null);
                put("specificOccurrenceConst", null);
            }};
        }

        return null;
    }

    // Handle setting the specific and source instance variables with a given occurrence identifier
    private HashMap<String, String> handleSpecificAndSource(String occurrenceIdentifier, String sourceDataCriteriaExtension, String sourceDataCriteriaRoot,
                                   String specificOccurrenceConst, String specificOccurrence) throws Exception {
        String sourceDataCriteria = String.format("%s_%s_source", sourceDataCriteriaExtension, sourceDataCriteriaRoot);
        if (occurrenceIdentifier.length() > 0) {
            // If it doesn't exist, add extracted occurrence to the map
            if (!this.occurrencesMap.containsKey(Utilities.stripTokens(sourceDataCriteria))) {
                this.occurrencesMap.put(Utilities.stripTokens(sourceDataCriteria), occurrenceIdentifier);
            }
            if (specificOccurrence.length() == 0) {
                specificOccurrence = occurrenceIdentifier;
            }
            specificOccurrenceConst = sourceDataCriteria.toUpperCase();

        }
        else {
            // create variable occurrences that do not already exist
            if (this.isVariable) {
                if (!this.occurrencesMap.containsKey(Utilities.stripTokens(sourceDataCriteria))) {
                    this.occurrencesMap.put(Utilities.stripTokens(sourceDataCriteria), occurrenceIdentifier);
                }
            }

            if (!this.occurrencesMap.containsKey(Utilities.stripTokens(sourceDataCriteria))) {
                throw new Exception(String.format("Could not find occurrence mapping for %s, %s", sourceDataCriteria, sourceDataCriteriaRoot));
            }

            if (specificOccurrence == null || specificOccurrence.length() == 0) {
                specificOccurrence = this.occurrencesMap.get(Utilities.stripTokens(sourceDataCriteria));
            }
        }

        if (specificOccurrence.length() == 0) {
            specificOccurrence = "A";
        }
        if (specificOccurrenceConst.length() == 0) {
            specificOccurrenceConst = sourceDataCriteria.toUpperCase();
        }

        String occurrenceVale = specificOccurrence;
        String occurrenceConst = specificOccurrenceConst;
        return new HashMap<String, String>() {{
            put("sourceDataCriteria", sourceDataCriteria);
            put("sourceDataCriteriaRoot", sourceDataCriteriaRoot);
            put("sourceDataCriteriaExtension", sourceDataCriteriaExtension);
            put("specificOccurrence", occurrenceVale);
            put("specificOccurrenceConst", occurrenceConst);
        }};
    }


    // Using the id, source data criteria id, and local variable name (and whether or not it's a variable),
    // extract the occurrence identifiter (if one exists).
    private String obtainOccurrenceIdentifier(String strippedId, String strippedLvn, String strippedSdc, boolean isVariable) {
        if (isVariable || strippedSdc.contains("qdm_var")) {
            String occurrenceLvnRegex = "occ[A-Z]of_qdm_var";
            String occurrenceIdRegex = "occ[A-Z]of_qdm_var";
            int occIndex = 3;
            return handleOccurrenceVar(strippedId, strippedLvn, strippedSdc, occurrenceIdRegex, occurrenceLvnRegex, occIndex);
        }
        else {
            String occurrenceLvnRegex = "Occurrence[A-Z]of";
            String occurrenceIdRegex = "Occurrence[A-Z]_";
            int occIndex = 10;
            String occurrenceIdentifier = handleOccurrenceVar(strippedId, strippedLvn, strippedSdc,
                    String.format("%s%s", occurrenceIdRegex, strippedSdc),
                    String.format("%s%s", occurrenceLvnRegex, strippedSdc), occIndex);
            if (occurrenceIdentifier != null && occurrenceIdentifier.length() > 0) {
                return occurrenceIdentifier;
            }

            if (strippedSdc.matches(String.format("/(^%s| ^%sqdm_var_| ^%s)| ^%sqdm_var_/)",
                    occurrenceIdRegex, occurrenceIdRegex, occurrenceLvnRegex, occurrenceLvnRegex))) {
                return String.valueOf(strippedSdc.charAt(occIndex));
            }
        }

        return "";
    }

    // If the occurrence is a variable, extract the occurrence identifier (if present)
    private String handleOccurrenceVar(String strippedId, String strippedLvn, String strippedSdc, String occurrenceIdCompare, String occurrenceLvnCompare, int occIndex) {
        // TODO: Handle specific occurrences of variables that don't self-reference?
        if (strippedId.matches("^" + occurrenceIdCompare)) {
            return String.valueOf(strippedId.charAt(occIndex));
        }
        else if (strippedLvn.matches("^" + occurrenceLvnCompare)) {
            return String.valueOf(strippedLvn.charAt(occIndex));
        }
        else if (strippedSdc.matches("^" + occurrenceIdCompare)) {
            return String.valueOf(strippedSdc.charAt(occIndex));
        }

        return "";
    }
}



