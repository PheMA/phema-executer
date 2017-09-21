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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;

/**
 * Created by Luke Rasmussen on 8/19/17.
 */
public class Document implements IDocument {
    private IdGenerator idGenerator = new IdGenerator();
    private org.w3c.dom.Document document = null;
    private XPath documentXPath = null;
    private String id = "";
    private String hqmfSetId = "";
    private String hqmfVersionNumber = "";
    private String cmsId = "";
    private ArrayList<Attribute> attributes = null;
    private ArrayList<DataCriteria> dataCriteria = null;
    private ArrayList sourceDataCriteria = null;
    private ArrayList referenceIds = null;

    public static final Map<String,String> NAMESPACES;
    static{
        Hashtable<String,String> tmp =
                new Hashtable<String,String>();
        tmp.put("cda", "urn:hl7-org:v3");
        tmp.put("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        tmp.put("qdm", "urn:hhs-qdm:hqmf-r2-extensions:v1");
        NAMESPACES = Collections.unmodifiableMap(tmp);
    }

    public Document(String hqmfContents, boolean useDefaultMeasurePeriod) throws Exception {
        setupDefaultValues(hqmfContents, useDefaultMeasurePeriod);
        extractCriteria();
    }

    private void extractCriteria() throws Exception {
        NodeList extractedCriteria = (NodeList)documentXPath.evaluate("QualityMeasureDocument/component/dataCriteriaSection/entry", document, XPathConstants.NODESET);
        for (int index = 0; index < extractedCriteria.getLength(); index++) {
            Node criterionNode = extractedCriteria.item(index);
            DataCriteria criterion = new DataCriteria(criterionNode, null, null);
            dataCriteria.add(criterion);
        }
    }

    private String getAttributeValue(Element element, String xpath, String defaultValue) throws XPathExpressionException {
        return XmlHelpers.getAttributeValue(element, documentXPath, xpath, defaultValue);
//        String value = (String)documentXPath.evaluate(xpath, element, XPathConstants.STRING);
//        if (value.length() == 0) {
//            return defaultValue;
//        }
//        return value;
    }

    private String getAttributeValue(Element element, String xpath) throws XPathExpressionException {
        return getAttributeValue(element, xpath, "");
    }

    private String getAttributeValueFromDocument(String xpath) throws XPathExpressionException {
        return getAttributeValue(document.getDocumentElement(), xpath);
//        String value = (String)xPath.evaluate(mainXpath, document.getDocumentElement(), XPathConstants.STRING);
//        if (value.length() == 0) {
//            return "";
//        }
//        return value;
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

        String id = getAttributeValue((Element)attributeNode, "./id/@root");
        String code = getAttributeValue((Element)attributeNode, "./code/@code");
        String name = getAttributeValue((Element)attributeNode, "./code/displayName/@value");
        String value = getAttributeValue((Element)attributeNode, "./value/@value");

        Node idNode = (Node)documentXPath.evaluate("./id", attributeNode, XPathConstants.NODE);
        Identifier identifierObject = null;
        if (idNode != null) {
            identifierObject = new Identifier(getAttributeValue((Element)idNode, "./id/@xsi:type"), id,
                    getAttributeValue((Element)idNode, "./id/@extension"));
        }

        Node codeNode = (Node)documentXPath.evaluate("./code", attributeNode, XPathConstants.NODE);
        Coded codeObject = null;
        if (codeNode != null) {
            codeObject = handleAttributeCode((Element)codeNode, code, name);

            // Mapping for null values to align with 1.0 parsing
            code = (code.length() == 0) ? getAttributeValue((Element)codeNode, "./code/@nullFlavor") : code;
            name = (name.length() == 0) ? getAttributeValue((Element)codeNode, "./code/originalText/@value") : name;
        }

        Node valueNode = (Node)documentXPath.evaluate("./value", attributeNode, XPathConstants.NODE);
        Object valueObject = null;
        if (valueNode != null) {
            valueObject = handleAttributeValue((Element)valueNode, value);
        }

        // Handle the CMS identifier
        if (name.indexOf("eMeasure Identifier") > -1) {
            cmsId = String.format("CMS%sv%s", value, hqmfVersionNumber);
        }

        return new Attribute(id, code, value, null, name, identifierObject, codeObject, null);
    }

    private Coded handleAttributeCode(Element attribute, String code, String name) throws XPathExpressionException {
        String nullFlavor = getAttributeValue(attribute, "./code/@nullFlavor");
        String originalText = getAttributeValue(attribute, "./code/originalText/@value");
        Coded codeObject = new Coded(getAttributeValue(attribute, "./code/@xsi:type", "CD"),
                getAttributeValue(attribute, "./code/@codeSystem"),
                code,
                getAttributeValue(attribute, "./code/@valueSet"),
                name,
                nullFlavor,
                originalText);
        return codeObject;
    }

    private Object handleAttributeValue(Element attribute, String value) throws XPathExpressionException {
        String type = getAttributeValue(attribute, "./value/@xsi:type");
        switch (type) {
            case "II" :
                if (value == null) {
                    value = getAttributeValue(attribute, "./value/@extension");
                }
                return new Identifier(type,
                        getAttributeValue(attribute, "./value/@root"),
                        getAttributeValue(attribute, "./value/@extension"));
            case "ED":
                return new ED(type, value,
                        getAttributeValue(attribute, "./value/@mediaType"));
            case "CD":
                return new Coded("CD",
                        getAttributeValue(attribute, "./value/@codeSystem"),
                        getAttributeValue(attribute, "./value/@code"),
                        getAttributeValue(attribute, "./value/@valueSet"),
                        getAttributeValue(attribute, "./value/displayName/@value"),
                        null, null);
            default:
                return (value.length() > 0) ? new GenericValueContainer(type, value) :
                        new AnyValue(type);
        }
    }

    private void buildMeasureAttributes() throws XPathExpressionException {
        NodeList attributeNodes = (NodeList)documentXPath.evaluate("subjectOf/measureAttribute", document.getDocumentElement(), XPathConstants.NODESET);
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
//        documentXPath = XPathFactory.newInstance().newXPath();
//        //XPath xPath = XPathFactory.newInstance().newXPath();
//        NamespaceContext context = new UniversalNamespaceCache(document, true);
//        documentXPath.setNamespaceContext(context);
        documentXPath = XmlHelpers.createXPath(document);
        id = getAttributeValueFromDocument("id/@extension", "id/@root");
        hqmfSetId = getAttributeValueFromDocument("setId/@extension", "setId/@root");
        hqmfVersionNumber = getAttributeValueFromDocument("versionNumber/@value");

        // In the HDS Ruby library, there is code around this spot to load measure period.
        // We are purposely ignoring this, because we don't want to use the encoded measure
        // period within the measure (we'll let people define it later).

        buildMeasureAttributes();

        // TODO - find types for criteria
        dataCriteria = new ArrayList();
        sourceDataCriteria = new ArrayList();

        // TODO
//        @data_criteria_references = {}
//        @occurrences_map = {}

        // Used to keep track of referenced data criteria ids
        referenceIds = new ArrayList();
    }

}
