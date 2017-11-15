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
    private Object value;
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
    private boolean specificOccurrence;
    private ArrayList<String> comments;
    private boolean isDerivedSpecificOccurrenceVariable;
    private String definition;
    private boolean variable;
    private String codeListId;

    private String localVariableName;
    private Node entry;
    private XPath xPath;
    private HashMap<String, DataCriteria> dataCriteriaReferences;
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
    public DataCriteria(Node entry, HashMap<String, DataCriteria> dataCriteriaReferences, HashMap<String, String> occurrencesMap) throws Exception {
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
        if (results == null) {
            this.sourceDataCriteria = null;
            this.sourceDataCriteriaRoot = null;
            this.sourceDataCriteriaExtension = null;
            this.specificOccurrence = false;
            this.specificOccurrenceConst = null;
        }
        else {
            this.sourceDataCriteria = results.get("sourceDataCriteria");
            this.sourceDataCriteriaRoot = results.get("sourceDataCriteriaRoot");
            this.sourceDataCriteriaExtension = results.get("sourceDataCriteriaExtension");
            this.specificOccurrence = (results.get("specificOccurrence") != null && results.get("specificOccurrence").equals("true"));
            this.specificOccurrenceConst = results.get("specificOccurrenceConst");
        }

        DataCriteriaTypeAndDefinitionExtraction.extractDefinitionFromTemplateOrType(this);
        DataCriteriaPostProcessing.postProcessing(this);
    }

    // Handles elments that can be extracted directly from the xml. Utilises the "BaseExtractions" class.
    private void basicSetup() throws Exception {
        Element element = (Element)this.entry;
        XPath xPath = XmlHelpers.createXPath(this.entry.getOwnerDocument());
        this.status = XmlHelpers.getAttributeValue(element, xPath, "./*/cda:statusCode/@code", "");
        this.id = String.format("%s_%s",
                XmlHelpers.getAttributeValue(element, xPath, "./*/cda:id/@extension", ""),
                XmlHelpers.getAttributeValue(element, xPath, "./*/cda:id/@root", ""));

        this.comments = new ArrayList<String>();
        NodeList commentNodes = (NodeList)xPath.evaluate(String.format("./%s/cda:text/cda:xml/cda:qdmUserComments/cda:item/text()", CRITERIA_GLOB),
                element, XPathConstants.NODESET);
        for (int index = 0; index < commentNodes.getLength(); index++) {
            Node comment = commentNodes.item(index);
            this.comments.add(comment.getTextContent());
        }
        this.codeListXPath = "./*/cda:code";
        this.valueXPath = "./*/cda:value";
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
            String encodedName = XmlHelpers.getAttributeValue(this.entry, this.xPath, "./cda:localVariableName/@value", "");
            if (encodedName != null && encodedName.length() > 0) {
                encodedName = extractDescriptionForVariable(encodedName);
                if (encodedName != null && encodedName.length() > 0) {
                    return encodedName;
                }
            }
        }
        else {
            String value = XmlHelpers.getAttributeValue(this.entry, this.xPath, String.format("./%s/cda:text/@value", CRITERIA_GLOB), "");
            if (value.length() > 0) {
                return value;
            }

            value = XmlHelpers.getAttributeValue(this.entry, this.xPath, String.format("./%s/cda:title/@value", CRITERIA_GLOB), "");
            if (value.length() > 0) {
                return value;
            }
        }

        return XmlHelpers.getAttributeValue(this.entry, this.xPath, String.format("./%s/cda:id/@extension", CRITERIA_GLOB), "");
    }


    // In the original HDS library, these are defined in an inner class, but they are just static
    // methods so we are keeping them in the DataCriteria class.

    //  Given an entry, and whether or not it's negated, extract out the proper field values for the data criteria.
    private static HashMap<String, Object> extractFieldValues(Node entry, boolean negation) throws Exception {
        HashMap<String, Object> fields = new HashMap<String, Object>();
        XPath xPath = XmlHelpers.createXPath(entry.getOwnerDocument());
        // extract most fields which use the same structure
        NodeList fieldNodes = (NodeList)xPath.evaluate("cda:outboundRelationship[*/cda:code]", entry, XPathConstants.NODESET);
        for (int index = 0; index < fieldNodes.getLength(); index++) {
            Node field = fieldNodes.item(index);
            String code = XmlHelpers.getAttributeValue((Element)field, xPath, "./*/cda:code/@code", "");
            String codeId = VALUE_FIELDS.get(code);
            // No need to run if there is no code id
            if ((negation && codeId.equals("REASON")) || codeId == null) {
                continue;
            }
            Object value = parseValue(field, xPath, "./*/cda:value");
            if (value == null) {
                value = parseValue(field, xPath, "./*/cda:effectiveTime");
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
    public static Object parseValue(Node node, XPath xPath, String xpathLocation) throws Exception {
        Node valueDef = (Node)xPath.evaluate(xpathLocation, node, XPathConstants.NODE);
        if (valueDef != null) {
            if ("ANY.NONNULL" == (String)xPath.evaluate("@flavorId", valueDef, XPathConstants.STRING)) {
                return new AnyValue();
            }
            Node valueTypeDef = (Node)xPath.evaluate("@type", valueDef, XPathConstants.NODE);
            if (valueTypeDef != null) {
                return handleValueType(valueTypeDef, valueDef);
            }
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOriginalId() {
        return originalId;
    }

    public void setOriginalId(String originalId) {
        this.originalId = originalId;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getEffectiveTime() {
        return effectiveTime;
    }

    public void setEffectiveTime(String effectiveTime) {
        this.effectiveTime = effectiveTime;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getDerivationOperator() {
        return derivationOperator;
    }

    public void setDerivationOperator(String derivationOperator) {
        this.derivationOperator = derivationOperator;
    }

    public boolean isNegation() {
        return negation;
    }

    public void setNegation(boolean negation) {
        this.negation = negation;
    }

    public String getNegationCodeListId() {
        return negationCodeListId;
    }

    public void setNegationCodeListId(String negationCodeListId) {
        this.negationCodeListId = negationCodeListId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public HashMap<String, Object> getFieldValues() {
        return fieldValues;
    }

    public void setFieldValues(HashMap<String, Object> fieldValues) {
        this.fieldValues = fieldValues;
    }

    public String getSourceDataCriteria() {
        return sourceDataCriteria;
    }

    public void setSourceDataCriteria(String sourceDataCriteria) {
        this.sourceDataCriteria = sourceDataCriteria;
    }

    public String getSourceDataCriteriaRoot() {
        return sourceDataCriteriaRoot;
    }

    public void setSourceDataCriteriaRoot(String sourceDataCriteriaRoot) {
        this.sourceDataCriteriaRoot = sourceDataCriteriaRoot;
    }

    public String getSpecificOccurrenceConst() {
        return specificOccurrenceConst;
    }

    public void setSpecificOccurrenceConst(String specificOccurrenceConst) {
        this.specificOccurrenceConst = specificOccurrenceConst;
    }

    public String getSourceDataCriteriaExtension() {
        return sourceDataCriteriaExtension;
    }

    public void setSourceDataCriteriaExtension(String sourceDataCriteriaExtension) {
        this.sourceDataCriteriaExtension = sourceDataCriteriaExtension;
    }

    public boolean isSpecificOccurrence() {
        return specificOccurrence;
    }

    public void setSpecificOccurrence(boolean specificOccurrence) {
        this.specificOccurrence = specificOccurrence;
    }

    public ArrayList<String> getComments() {
        return comments;
    }

    public void setComments(ArrayList<String> comments) {
        this.comments = comments;
    }

    public boolean isDerivedSpecificOccurrenceVariable() {
        return isDerivedSpecificOccurrenceVariable;
    }

    public void setDerivedSpecificOccurrenceVariable(boolean derivedSpecificOccurrenceVariable) {
        isDerivedSpecificOccurrenceVariable = derivedSpecificOccurrenceVariable;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public boolean isVariable() {
        return variable;
    }

    public void setVariable(boolean variable) {
        this.variable = variable;
    }

    public String getLocalVariableName() {
        return localVariableName;
    }

    public void setLocalVariableName(String localVariableName) {
        this.localVariableName = localVariableName;
    }

    public Node getEntry() {
        return entry;
    }

    public void setEntry(Node entry) {
        this.entry = entry;
    }

    public XPath getxPath() {
        return xPath;
    }

    public void setxPath(XPath xPath) {
        this.xPath = xPath;
    }

    public HashMap<String, DataCriteria> getDataCriteriaReferences() {
        return dataCriteriaReferences;
    }

    public void setDataCriteriaReferences(HashMap<String, DataCriteria> dataCriteriaReferences) {
        this.dataCriteriaReferences = dataCriteriaReferences;
    }

    public HashMap<String, String> getOccurrencesMap() {
        return occurrencesMap;
    }

    public void setOccurrencesMap(HashMap<String, String> occurrencesMap) {
        this.occurrencesMap = occurrencesMap;
    }

    public ArrayList<String> getTemplateIds() {
        return templateIds;
    }

    public void setTemplateIds(ArrayList<String> templateIds) {
        this.templateIds = templateIds;
    }

    public ArrayList<TemporalReference> getTemporalReferences() {
        return temporalReferences;
    }

    public void setTemporalReferences(ArrayList<TemporalReference> temporalReferences) {
        this.temporalReferences = temporalReferences;
    }

    public ArrayList<String> getChildrenCriteria() {
        return childrenCriteria;
    }

    public void setChildrenCriteria(ArrayList<String> childrenCriteria) {
        this.childrenCriteria = childrenCriteria;
    }

    public ArrayList<SubsetOperator> getSubsetOperators() {
        return subsetOperators;
    }

    public void setSubsetOperators(ArrayList<SubsetOperator> subsetOperators) {
        this.subsetOperators = subsetOperators;
    }

    public String getCodeListXPath() {
        return codeListXPath;
    }

    public void setCodeListXPath(String codeListXPath) {
        this.codeListXPath = codeListXPath;
    }

    public String getValueXPath() {
        return valueXPath;
    }

    public void setValueXPath(String valueXPath) {
        this.valueXPath = valueXPath;
    }

    public static Pattern getVariableNamePattern() {
        return variableNamePattern;
    }

    public static void setVariableNamePattern(Pattern variableNamePattern) {
        DataCriteria.variableNamePattern = variableNamePattern;
    }

    public static Pattern getQdmVariablePattern() {
        return qdmVariablePattern;
    }

    public static void setQdmVariablePattern(Pattern qdmVariablePattern) {
        DataCriteria.qdmVariablePattern = qdmVariablePattern;
    }

    public static Pattern getLocalVariablePattern() {
        return localVariablePattern;
    }

    public static void setLocalVariablePattern(Pattern localVariablePattern) {
        DataCriteria.localVariablePattern = localVariablePattern;
    }

    public String getCodeListId() {
        if (codeListId == null || codeListId.length() == 0) {
            try {
                return XmlHelpers.getAttributeValue((Element)entry, xPath, String.format("%s/@valueSet", codeListXPath), "");
            } catch (XPathExpressionException e) {
                e.printStackTrace();
                return codeListId;
            }
        }
        return codeListId;
    }
    public void setCodeListId(String codeListId) {
        this.codeListId = codeListId;
    }
}
