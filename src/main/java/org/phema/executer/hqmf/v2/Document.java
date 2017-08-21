package org.phema.executer.hqmf.v2;

import org.phema.executer.hqmf.models.Attribute;
import org.phema.executer.hqmf.models.Coded;
import org.phema.executer.hqmf.models.Identifier;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.phema.executer.UniversalNamespaceCache;
import org.phema.executer.hqmf.IDocument;
import org.phema.executer.util.XmlHelpers;
import org.w3c.dom.Node;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
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

    private void extractCriteria() {
    }

    private String getAttributeValue(Element element, String xpath, String defaultValue) throws XPathExpressionException {
        String value = (String)documentXPath.evaluate(xpath, element, XPathConstants.STRING);
        if (value.length() == 0) {
            return defaultValue;
        }
        return value;
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
        
    }

    private void buildMeasureAttributes() throws XPathExpressionException {
        NodeList attributes = (NodeList)documentXPath.evaluate("subjectOf/measureAttribute", document.getDocumentElement(), XPathConstants.NODESET);
        if (attributes == null || attributes.getLength() == 0) {
            return;
        }

        for (int index = 0; index < attributes.getLength(); index++) {
            Node attributeNode = attributes.item(index);
            Attribute attribute = readAttribute(attributeNode);
        }
    }

    private void setupDefaultValues(String hqmfContents, boolean useDefaultMeasurePeriod) throws Exception {
        idGenerator = new IdGenerator();
        document = XmlHelpers.LoadXMLFromString(hqmfContents);
        documentXPath = XPathFactory.newInstance().newXPath();
        //XPath xPath = XPathFactory.newInstance().newXPath();
        NamespaceContext context = new UniversalNamespaceCache(document, true);
        documentXPath.setNamespaceContext(context);
        id = getAttributeValueFromDocument("id/@extension", "id/@root");
        hqmfSetId = getAttributeValueFromDocument("setId/@extension", "setId/@root");
        hqmfVersionNumber = getAttributeValueFromDocument("versionNumber/@value");
        buildMeasureAttributes();
    }

}
