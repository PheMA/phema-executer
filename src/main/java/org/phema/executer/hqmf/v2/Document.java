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
    private ArrayList<DataCriteria> dataCriteria = null;
    private ArrayList<DataCriteria> sourceDataCriteria = null;
    private ArrayList referenceIds = null;
    private HashMap<String, DataCriteria> dataCriteriaReferences;
    private HashMap<String, String> occurrencesMap;

//    public static final Map<String,String> NAMESPACES;
//    static{
//        Hashtable<String,String> tmp =
//                new Hashtable<String,String>();
//        tmp.put("cda", "urn:hl7-org:v3");
//        tmp.put("xsi", "http://www.w3.org/2001/XMLSchema-instance");
//        tmp.put("qdm", "urn:hhs-qdm:hqmf-r2-extensions:v1");
//        NAMESPACES = Collections.unmodifiableMap(tmp);
//    }

    public Document(String hqmfContents, boolean useDefaultMeasurePeriod) throws Exception {
        setupDefaultValues(hqmfContents, useDefaultMeasurePeriod);
        extractCriteria();

        // Extract the population criteria and population collections
        //TODO Finish implementing this
        DocumentPopulationHelper popHelper = new DocumentPopulationHelper((Node)this.entry.getDocumentElement(), (Node)this.document.getDocumentElement(), this, this.idGenerator, this.referenceIds);
        Object[] results = popHelper.extractPopulationsAndCriteria();
        /*
                pop_helper = HQMF2::DocumentPopulationHelper.new(@entry, @doc, self, @id_generator, @reference_ids)
        @populations, @population_criteria = pop_helper.extract_populations_and_criteria

      # Remove any data criteria from the main data criteria list that already has an equivalent member
      #  and no references to it. The goal of this is to remove any data criteria that should not
      #  be purely a source.
        @data_criteria.reject! do |dc|
                criteria_covered_by_criteria?(dc)
                end*/
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
//        documentXPath = XPathFactory.newInstance().newXPath();
//        //XPath xPath = XPathFactory.newInstance().newXPath();
//        NamespaceContext context = new UniversalNamespaceCache(document, true);
//        documentXPath.setNamespaceContext(context);
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

}
