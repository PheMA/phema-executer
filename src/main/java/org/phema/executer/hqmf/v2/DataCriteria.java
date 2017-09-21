package org.phema.executer.hqmf.v2;

import org.apache.commons.lang.StringUtils;
import org.phema.executer.hqmf.models.AnyValue;
import org.phema.executer.util.XmlHelpers;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Luke Rasmussen on 8/25/17.
 */
public class DataCriteria extends org.phema.executer.hqmf.models.DataCriteria {
    private String id;
    private String originalId;
    private String property;
    private String type;
    private String status;
    private String value;
    private String effectiveTime;
    private String section;
    private String derivationOperator;
    private boolean negation;
    private String negationCodeListId;
    private String description;
    private HashMap<String, Object> fieldValues;
    private String sourceDataCriteria;
    private String sourceDataCriteriaRoot;
    private String specificOccurrenceConst;
    private String sourceDataCriteriaExtension;
    private String specificOccurrence;
    private ArrayList<String> comments;
    private boolean isDerivedSpecificOccurrenceVariable;
    private String definition;
    private boolean variable;
    private String localVariableName;
    private Node entry;
    private XPath xPath;
    private HashMap<String, Object> dataCriteriaReferences;
    private HashMap<String, String> occurrencesMap;
    private ArrayList<String> templateIds;
    private ArrayList<TemporalReference> temporalReferences;
    private ArrayList<String> childrenCriteria;
    private ArrayList<SubsetOperator> subsetOperators;

    private String codeListXPath;
    private String valueXPath;

    private static Pattern variableNamePattern = Pattern.compile(".*qdm_var_");
    private static Pattern qdmVariablePattern = Pattern.compile("^qdm_var_");
    private static Pattern localVariablePattern = Pattern.compile("^localVar_");

    private static final String CRITERIA_GLOB = "*[substring(name(),string-length(name())-7) = 'Criteria']";

    // Create a new instance based on the supplied HQMF entry
    public DataCriteria(Node entry, HashMap<String, Object> dataCriteriaReferences, HashMap<String, String> occurrencesMap) throws Exception {
        this.entry = entry;
        if (entry != null) {
            this.xPath = XmlHelpers.createXPath(this.entry.getOwnerDocument());
        }
        this.dataCriteriaReferences = dataCriteriaReferences;
        this.occurrencesMap = occurrencesMap;
        basicSetup();
        this.variable = this.extractVariable(localVariableName, id);
        this.fieldValues = this.extractFieldValues(entry, negation);
        this.description = extractDescription();

        SpecificOccurrenceAndSource obtainSpecificAndSource = new SpecificOccurrenceAndSource(this.entry, this.id, this.localVariableName, this.dataCriteriaReferences, this.occurrencesMap);
        HashMap<String, String>  results = obtainSpecificAndSource.extractSpecificOccurrencesAndSourceDataCriteria();
        this.sourceDataCriteria = results.get("sourceDataCriteria");
        this.sourceDataCriteriaRoot = results.get("sourceDataCriteriaRoot");
        this.sourceDataCriteriaExtension = results.get("sourceDataCriteriaExtension");
        this.specificOccurrence = results.get("specificOccurrence");
        this.specificOccurrenceConst = results.get("specificOccurrenceConst");

        DataCriteriaTypeAndDefinitionExtraction.extractDefinitionFromTemplateOrType(this.templateIds);
        postProcessing();
    }

    // Handles elments that can be extracted directly from the xml. Utilises the "BaseExtractions" class.
    private void basicSetup() throws Exception {
        Element element = (Element)this.entry;
        XPath xPath = XmlHelpers.createXPath(this.entry.getOwnerDocument());
        this.status = XmlHelpers.getAttributeValue(element, xPath, "./*/statusCode/@code", "");
        this.id = String.format("%s_%s",
                XmlHelpers.getAttributeValue(element, xPath, "./*/id/@extension", ""),
                XmlHelpers.getAttributeValue(element, xPath, "./*/id/@root", ""));

        this.comments = new ArrayList<String>();
        NodeList commentNodes = (NodeList)xPath.evaluate(String.format("./%s/text/xml/qdmUserComments/item/text()", CRITERIA_GLOB),
                element, XPathConstants.NODESET);
        for (int index = 0; index < commentNodes.getLength(); index++) {
            Node comment = commentNodes.item(index);
            this.comments.add(comment.getTextContent());
        }
        this.codeListXPath = "./*/code";
        this.valueXPath = "./*/value";
        this.isDerivedSpecificOccurrenceVariable = false;

        DataCriteriaBaseExtractions simpleExtractions = new DataCriteriaBaseExtractions(this.entry);
        this.templateIds = simpleExtractions.extractTemplateIds();
        this.localVariableName = simpleExtractions.extractLocalVariableName();
        this.temporalReferences = simpleExtractions.extractTemporalReferences();
        this.derivationOperator = simpleExtractions.extractDerivationOperator();
        this.childrenCriteria = simpleExtractions.extractChildCriteria();
        this.subsetOperators = simpleExtractions.extractSubsetOperators();
        this.negation = simpleExtractions.extractNegation();
        this.negationCodeListId = simpleExtractions.extractNegationCodeListId(this.negation);
    }

    private String extractDescription() throws XPathExpressionException {
        if (this.variable) {
            String encodedName = XmlHelpers.getAttributeValue(this.entry, this.xPath, "./localVariableName/@value", "");
            if (encodedName != null && encodedName.length() > 0) {
                encodedName = extractDescriptionForVariable(encodedName);
                if (encodedName != null && encodedName.length() > 0) {
                    return encodedName;
                }
            }
        }
        else {
            String value = XmlHelpers.getAttributeValue(this.entry, this.xPath, String.format("./%s/text/@value", CRITERIA_GLOB), "");
            if (value.length() > 0) {
                return value;
            }

            value = XmlHelpers.getAttributeValue(this.entry, this.xPath, String.format("./%s/title/@value", CRITERIA_GLOB), "");
            if (value.length() > 0) {
                return value;
            }
        }

        return XmlHelpers.getAttributeValue(this.entry, this.xPath, String.format("./%s/id/@extension", CRITERIA_GLOB), "");
    }


    // In the original HDS library, these are defined in an inner class, but they are just static
    // methods so we are keeping them in the DataCriteria class.

    //  Given an entry, and whether or not it's negated, extract out the proper field values for the data criteria.
    private static HashMap<String, Object> extractFieldValues(Node entry, boolean negation) throws Exception {
        HashMap<String, Object> fields = new HashMap<String, Object>();
        XPath xPath = XmlHelpers.createXPath(entry.getOwnerDocument());
        // extract most fields which use the same structure
        NodeList fieldNodes = (NodeList)xPath.evaluate("outboundRelationship[*/code]", entry, XPathConstants.NODESET);
        for (int index = 0; index < fieldNodes.getLength(); index++) {
            Node field = fieldNodes.item(index);
            String code = XmlHelpers.getAttributeValue((Element)field, xPath, "./*/code/@code", "");
            String codeId = VALUE_FIELDS.get(code);
            // No need to run if there is no code id
            if ((negation && codeId.equals("REASON")) || codeId == null) {
                continue;
            }
            Object value = parseValue(field, xPath, "./*/value");
            if (value == null) {
                value = parseValue(field, xPath, "./*/effectiveTime");
            }
            fields.put(codeId, value);
        }
        return fields;
    }

    // Determine if this instance is a qdm variable
    public static boolean extractVariable(String localVariableName, String id) {
        if (!StringUtils.isEmpty(localVariableName)) {
            Matcher matcher = variableNamePattern.matcher(localVariableName);
            return matcher.matches();
        }
        else if (!StringUtils.isEmpty(id)) {
            Matcher matcher = variableNamePattern.matcher(id);
            return matcher.matches();
        }
        return false;
    }

    // Parses the value for a given xpath
    private static Object parseValue(Node node, XPath xPath, String xpathLocation) throws Exception {
        Node valueDef = (Node)xPath.evaluate(xpathLocation, node, XPathConstants.NODE);
        if (valueDef != null) {
            if ("ANY.NONNULL" == (String)xPath.evaluate("@flavorId", valueDef, XPathConstants.STRING)) {
                return new AnyValue();
            }
            Node valueTypeDef = (Node)xPath.evaluate("@xsi:type", valueDef, XPathConstants.NODE);
            return handleValueType(valueTypeDef, valueDef);
        }
        return null;
    }

    // Derives the type associated with a specific value
    private static Object handleValueType(Node valueTypeDef, Node valueDef) throws Exception {
        String valueType = valueTypeDef.getNodeValue();
        switch (valueType) {
            case "PQ":
                return new Value(valueDef, "PQ", true);
            case "TS":
                return new Value(valueDef);
            case "IVL_PQ":
            case "IVL_INT":
                return new Range(valueDef, null);
            case "CD":
                return new Coded(valueDef);
            case "ANY":
            case "IVL_TS":
                // FIXME: (10/26/2015) IVL_TS should be able to handle other values, not just AnyValue
                return new AnyValue();
            default:
                throw new Exception("Unknown value type " + valueType);
        }
    }

    // Use the new MAT feature to extract the human generated (or computer generated) variable names from the xml.
    private static String extractDescriptionForVariable(String encodedName) {
        Matcher matcher = qdmVariablePattern.matcher(encodedName);
        if (matcher != null && matcher.matches()) {
            // Strip out initial qdm_var_ string, trailing _*, and possible occurrence reference
            encodedName = encodedName.replaceAll("^qdm_var_|", "");
            encodedName = encodedName.replaceAll("Occurrence[A-Z]of", "");
            // This code needs to handle measures created before the MAT added variable name hints; for those, don't strip
            // the final identifier
            Pattern hintMatcher = Pattern.compile("(SATISFIES ALL|SATISFIES ANY|UNION|INTERSECTION)");
            matcher = qdmVariablePattern.matcher(encodedName);
            if (matcher != null && matcher.matches()) {
                encodedName = encodedName.replaceAll("_[^_]+$", "");
            }
            return encodedName;
        }

        matcher = localVariablePattern.matcher(encodedName);
        if (matcher != null && matcher.matches()) {
            encodedName = encodedName.replaceAll("^localVar_", "");
            return encodedName;
        }

        return "";
    }
}
