package org.phema.executer.hqmf.v2;

import org.phema.executer.hqmf.models.*;
import org.phema.executer.hqmf.models.Coded;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.phema.executer.UniversalNamespaceCache;
import org.phema.executer.hqmf.IDocument;
import org.phema.executer.util.XmlHelpers;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.util.*;

/**
 * Created by Luke Rasmussen on 8/19/17.
 */
public class Document implements IDocument {
    private IdGenerator idGenerator = new IdGenerator();
    private org.w3c.dom.Document document = null;
    private org.w3c.dom.Document entry = null;
    private XPath documentXPath = null;
    private String id = "";
    private String hqmfSetId = "";
    private String hqmfVersionNumber = "";
    private String cmsId = "";
    private ArrayList<Attribute> attributes = null;
    private ArrayList<DataCriteria> sourceDataCriteria = null;
    private ArrayList<DataCriteria> dataCriteria = null;
    private ArrayList referenceIds = null;
    private HashMap<String, DataCriteria> dataCriteriaReferences;
    private HashMap<String, String> occurrencesMap;
    private ArrayList<Population> populations;
    private ArrayList<PopulationCriteria> populationCriteria;


    /**
     * Create a new HQMF2::Document instance by parsing the given HQMF contents
     * @param hqmfContents String containing the HQMF contents to be parsed
     * @param useDefaultMeasurePeriod
     * @throws Exception
     */
    public Document(String hqmfContents, boolean useDefaultMeasurePeriod) throws Exception {
        setupDefaultValues(hqmfContents, useDefaultMeasurePeriod);
        extractCriteria();

        // Extract the population criteria and population collections
        DocumentPopulationHelper popHelper = new DocumentPopulationHelper((Node)this.entry.getDocumentElement(), (Node)this.document.getDocumentElement(), this, this.idGenerator, this.referenceIds);
        Object[] results = popHelper.extractPopulationsAndCriteria();
        this.populations = (ArrayList<Population>)results[0];
        this.populationCriteria = (ArrayList<PopulationCriteria>)results[1];

        // Remove any data criteria from the main data criteria list that already has an equivalent member
        //  and no references to it. The goal of this is to remove any data criteria that should not
        //  be purely a source.
        this.dataCriteria.removeIf(dc -> criteriaCoveredByCriteria(dc));
    }

    /**
     * Get all of the unique value set OIDs used by data criteria elements in this HQMF document
     * @return Unique list of value set OIDs that are in this HQMF document
     */
    public ArrayList<String> getAllValueSetOids() {
        HashSet<String> valueSetOids = new HashSet<>();
        if (sourceDataCriteria == null || sourceDataCriteria.size() == 0) {
            return new ArrayList<String>(valueSetOids);
        }

        for (DataCriteria criterion : this.sourceDataCriteria) {
            String oid = criterion.getCodeListId();
            if (oid != null && oid.length() > 0 && !valueSetOids.contains(oid)) {
                valueSetOids.add(oid);
            }
        }
        return new ArrayList<String>(valueSetOids);
    }

    // Checks if one data criteria is covered by another (has all the appropriate elements of)
    private boolean criteriaCoveredByCriteria(DataCriteria dc) {
        // This original Ruby code doesn't appear to do anything (it should return the result to nothing, instead of
        // modifying the reference_ids collection).  Assuming this does nothing, we're skipping implementing it here.
        //@reference_ids.uniq

        ArrayList<String> baseCriteriaDefs = new ArrayList<String>() {{
            add("patient_characteristic_ethnicity");
            add("patient_characteristic_gender");
            add("patient_characteristic_payer");
            add("patient_characteristic_race");
        }};

        boolean toReject = true;
        // don't reject if anything refers directly to this criteria
        toReject = toReject && (this.referenceIds.indexOf(dc.getId()) == -1);
        // don't reject if it is a "base" criteria (no references but must exist)
        toReject = toReject && !(baseCriteriaDefs.indexOf(dc.getDefinition()) >= 0);
        // keep referral occurrence
        toReject = toReject && ((dc.getSpecificOccurrenceConst() == null) || dc.getCodeListId().equals("2.16.840.1.113883.3.464.1003.101.12.1046"));
        // don't reject unless there is a similar element
        toReject = toReject && this.dataCriteria.stream().anyMatch(x -> (x != dc) // Don't check against itself
                && (dc.getCodeListId().equals(x.getCodeListId())) // Ensure code list ids are the same
                && detectCriteriaCoveredByCriteria(dc, x));
        return toReject;
    }

    // Check if one data criteria contains the others information by checking that one has everything the other has
    // (or more)
    private boolean detectCriteriaCoveredByCriteria(DataCriteria dataCriteria, DataCriteria checkCriteria) {
        boolean baseChecks = true;

        // Check whether basic features are the same
        baseChecks = baseChecks && Objects.equals(dataCriteria.getDefinition(), checkCriteria.getDefinition()); // same definition
        baseChecks = baseChecks && Objects.equals(dataCriteria.getStatus(), checkCriteria.getStatus());  // same status
        // same children
        baseChecks = baseChecks && (String.join(",", dataCriteria.getChildrenCriteria().stream().sorted().toArray(String[]::new)).equals(
            String.join(",", checkCriteria.getChildrenCriteria().stream().sorted().toArray(String[]::new))));
        // Ensure it doesn't contain basic elements that should not be removed
        baseChecks = baseChecks && !dataCriteria.isVariable(); // Ensure it's not a variable
        baseChecks = baseChecks && (dataCriteria.getDerivationOperator() == null); // Ensure it doesn't have a derivation operator
        baseChecks = baseChecks && (dataCriteria.getSubsetOperators().size() == 0); // Ensure it doesn't have a subset operator
        // Ensure it doesn't have Temporal References
        baseChecks = baseChecks && (dataCriteria.getTemporalReferences() == null || dataCriteria.getTemporalReferences().size() == 0);

        return baseChecks && complexCoverage(dataCriteria, checkCriteria);
    }

    // Check elements that do not already exist; else, if they do, check if those elements are the same
    // in a different, potentially matching, data criteria
    private boolean complexCoverage(DataCriteria dataCriteria, DataCriteria checkCriteria) {
        boolean sameValue = dataCriteria.getValue() == null ||
                (dataCriteria.getValue().equals(checkCriteria.getValue()));
        boolean sameFieldValues = sameFieldValuesCheck(dataCriteria, checkCriteria);
        boolean sameNegationValues = dataCriteria.getNegationCodeListId() == null ||
                dataCriteria.getNegationCodeListId().equals(checkCriteria.getNegationCodeListId());
        return sameValue && sameNegationValues && sameFieldValues;
    }

    private boolean sameFieldValuesCheck(DataCriteria dataCriteria, DataCriteria checkCriteria) {
        boolean empty = dataCriteria.getFieldValues() == null || dataCriteria.getFieldValues().size() == 0;
        // Ignore STATUS (and ORDINAL for CMS172v5)
        // The meaning of status has changed over time. Laboratory test and procedure now use status differently.
        // This change is causing superficial discrepencies between the simplexml and hqmf regarding STATUS.
        Object[] dcFiltered = dataCriteria.getFieldValues().entrySet().stream()
                .filter(x -> !x.getKey().equals("STATUS") && !x.getKey().equals("ORDINAL")).toArray(Object[]::new);
        Object[] ccFiltered = checkCriteria.getFieldValues().entrySet().stream()
                .filter(x -> !x.getKey().equals("STATUS") && !x.getKey().equals("ORDINAL")).toArray(Object[]::new);
        String left = (dcFiltered == null || dcFiltered.length == 0) ? null : dcFiltered.toString();
        String right = (ccFiltered == null || ccFiltered.length == 0) ? null : ccFiltered.toString();
        return empty || (left.equals(right));
    }

    private void extractCriteria() throws Exception {
        NodeList extractedCriteria = (NodeList)documentXPath.evaluate("cda:QualityMeasureDocument/cda:component/cda:dataCriteriaSection/cda:entry", document, XPathConstants.NODESET);
        // Extract the source data criteria from data criteria
        Object[] result = SourceDataCriteriaHelper.getSourceDataCriteriaList(extractedCriteria, dataCriteriaReferences, occurrencesMap);
        this.sourceDataCriteria = (ArrayList<DataCriteria>)result[0];
        HashMap<String, String> collapsedSourceDataCriteriaMap = (HashMap<String, String>)result[1];
        for (int index = 0; index < extractedCriteria.getLength(); index++) {
            Node criterionNode = extractedCriteria.item(index);
            DataCriteria criterion = new DataCriteria(criterionNode, this.dataCriteriaReferences, this.occurrencesMap);
            dataCriteria.add(criterion);
        }
    }

    private String getAttributeValue(Element element, String xpath, String defaultValue) throws XPathExpressionException {
        return XmlHelpers.getAttributeValue(element, documentXPath, xpath, defaultValue);
    }

    private String getAttributeValue(Element element, String xpath) throws XPathExpressionException {
        return getAttributeValue(element, xpath, "");
    }

    private String getAttributeValueFromDocument(String xpath) throws XPathExpressionException {
        return getAttributeValue(document.getDocumentElement(), xpath);
    }

    private String getAttributeValueFromDocument(String mainXpath, String alternateXpath) throws XPathExpressionException {
        String value = getAttributeValueFromDocument(mainXpath);
        if (value.length() == 0) {
            value = getAttributeValueFromDocument(alternateXpath);
        }
        return value;
    }

    // Adds data criteria to the Document's criteria list
    // needed so data criteria can be added to a document from other objects
    public void addDataCriteria(DataCriteria dataCriteria) {
        if (this.dataCriteria == null) {
            this.dataCriteria = new ArrayList<DataCriteria>();
        }

        this.dataCriteria.add(dataCriteria);
    }

    // Adds id of a data criteria to the list of reference ids
    public void addReferenceId(String id) {
        if (this.referenceIds == null) {
            this.referenceIds = new ArrayList<String>();
        }

        this.referenceIds.add(id);
    }

    private Attribute readAttribute(Node attributeNode) throws XPathExpressionException {
        if (attributeNode == null) {
            return null;
        }

        String id = getAttributeValue((Element)attributeNode, "./cda:id/@root");
        String code = getAttributeValue((Element)attributeNode, "./cda:code/@code");
        String name = getAttributeValue((Element)attributeNode, "./cda:code/cda:displayName/@value");
        String value = getAttributeValue((Element)attributeNode, "./cda:value/@value");

        Node idNode = (Node)documentXPath.evaluate("./cda:id", attributeNode, XPathConstants.NODE);
        Identifier identifierObject = null;
        if (idNode != null) {
            identifierObject = new Identifier(getAttributeValue((Element)idNode, "./cda:id/@type"), id,
                    getAttributeValue((Element)idNode, "./cda:id/@extension"));
        }

        Node codeNode = (Node)documentXPath.evaluate("./cda:code", attributeNode, XPathConstants.NODE);
        Coded codeObject = null;
        if (codeNode != null) {
            codeObject = handleAttributeCode((Element)codeNode, code, name);

            // Mapping for null values to align with 1.0 parsing
            code = (code.length() == 0) ? getAttributeValue((Element)codeNode, "./cda:code/@nullFlavor") : code;
            name = (name.length() == 0) ? getAttributeValue((Element)codeNode, "./cda:code/cda:originalText/@value") : name;
        }

        Node valueNode = (Node)documentXPath.evaluate("./cda:value", attributeNode, XPathConstants.NODE);
        Object valueObject = null;
        if (valueNode != null) {
            valueObject = handleAttributeValue((Element)attributeNode, value);
        }

        // Handle the CMS identifier
        if (name.indexOf("eMeasure Identifier") > -1) {
            cmsId = String.format("CMS%sv%s", value, hqmfVersionNumber);
        }

        return new Attribute(id, code, value, null, name, identifierObject, codeObject, valueObject);
    }

    private Coded handleAttributeCode(Element attribute, String code, String name) throws XPathExpressionException {
        String nullFlavor = getAttributeValue(attribute, "./cda:code/@nullFlavor");
        String originalText = getAttributeValue(attribute, "./cda:code/cda:originalText/@value");
        Coded codeObject = new Coded(getAttributeValue(attribute, "./cda:code/@type", "CD"),
                getAttributeValue(attribute, "./cda:code/@codeSystem"),
                code,
                getAttributeValue(attribute, "./cda:code/@valueSet"),
                name,
                nullFlavor,
                originalText);
        return codeObject;
    }

    private Object handleAttributeValue(Element attribute, String value) throws XPathExpressionException {
        String type = getAttributeValue(attribute, "./cda:value/@type");
        switch (type) {
            case "II" :
                if (value == null) {
                    value = getAttributeValue(attribute, "./cda:value/@extension");
                }
                return new Identifier(type,
                        getAttributeValue(attribute, "./cda:value/@root"),
                        getAttributeValue(attribute, "./cda:value/@extension"));
            case "ED":
                return new ED(type, value,
                        getAttributeValue(attribute, "./cda:value/@mediaType"));
            case "CD":
                return new Coded("CD",
                        getAttributeValue(attribute, "./cda:value/@codeSystem"),
                        getAttributeValue(attribute, "./cda:value/@code"),
                        getAttributeValue(attribute, "./cda:value/@valueSet"),
                        getAttributeValue(attribute, "./cda:value/cda:displayName/@value"),
                        null, null);
            default:
                return (value.length() > 0) ? new GenericValueContainer(type, value) :
                        new AnyValue(type);
        }
    }

    private void buildMeasureAttributes() throws XPathExpressionException {
        NodeList attributeNodes = (NodeList)documentXPath.evaluate("cda:subjectOf/cda:measureAttribute", document.getDocumentElement(), XPathConstants.NODESET);
        if (attributeNodes == null || attributeNodes.getLength() == 0) {
            return;
        }

        attributes = new ArrayList<Attribute>();
        for (int index = 0; index < attributeNodes.getLength(); index++) {
            Node attributeNode = attributeNodes.item(index);
            Attribute attribute = readAttribute(attributeNode);
            attributes.add(attribute);
        }
    }

    private void setupDefaultValues(String hqmfContents, boolean useDefaultMeasurePeriod) throws Exception {
        idGenerator = new IdGenerator();
        document = XmlHelpers.loadXMLFromString(hqmfContents);
        entry = document;
        documentXPath = XmlHelpers.createXPath(document);
        id = getAttributeValueFromDocument("cda:id/@extension", "cda:id/@root");
        hqmfSetId = getAttributeValueFromDocument("cda:setId/@extension", "cda:setId/@root");
        hqmfVersionNumber = getAttributeValueFromDocument("cda:versionNumber/@value");

        // In the HDS Ruby library, there is code around this spot to load measure period.
        // We are purposely ignoring this, because we don't want to use the encoded measure
        // period within the measure (we'll let people define it later).

        buildMeasureAttributes();

        dataCriteria = new ArrayList<>();
        sourceDataCriteria = new ArrayList<>();

        dataCriteriaReferences = new HashMap<>();
        occurrencesMap = new HashMap<>();

        // Used to keep track of referenced data criteria ids
        referenceIds = new ArrayList();
    }

    // Finds a data criteria by it's local variable name
    public DataCriteria findCriteriaByLocalVariableName(String localVariableName) {
        if (this.dataCriteria == null) {
            return null;
        }

        DataCriteria criteria = this.dataCriteria.stream()
                .filter(x -> x.getLocalVariableName().equals(localVariableName))
                .findFirst()
                .orElse(null);
        return criteria;
    }


    public String getHqmfSetId() {
        return hqmfSetId;
    }

    public void setHqmfSetId(String hqmfSetId) {
        this.hqmfSetId = hqmfSetId;
    }

    public String getHqmfVersionNumber() {
        return hqmfVersionNumber;
    }

    public void setHqmfVersionNumber(String hqmfVersionNumber) {
        this.hqmfVersionNumber = hqmfVersionNumber;
    }

    public String getCmsId() {
        return cmsId;
    }

    public void setCmsId(String cmsId) {
        this.cmsId = cmsId;
    }

    public ArrayList<Attribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(ArrayList<Attribute> attributes) {
        this.attributes = attributes;
    }

    public ArrayList<DataCriteria> getDataCriteria() {
        return dataCriteria;
    }

    public void setDataCriteria(ArrayList<DataCriteria> dataCriteria) {
        this.dataCriteria = dataCriteria;
    }

    public ArrayList<DataCriteria> getSourceDataCriteria() {
        return sourceDataCriteria;
    }

    public void setSourceDataCriteria(ArrayList<DataCriteria> sourceDataCriteria) {
        this.sourceDataCriteria = sourceDataCriteria;
    }

    public ArrayList getReferenceIds() {
        return referenceIds;
    }

    public void setReferenceIds(ArrayList referenceIds) {
        this.referenceIds = referenceIds;
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ArrayList<Population> getPopulations() {
        return populations;
    }

    public void setPopulations(ArrayList<Population> populations) {
        this.populations = populations;
    }

    public ArrayList<PopulationCriteria> getPopulationCriteria() {
        return populationCriteria;
    }

    public void setPopulationCriteria(ArrayList<PopulationCriteria> populationCriteria) {
        this.populationCriteria = populationCriteria;
    }
}
