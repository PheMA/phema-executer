package org.phema.executer.hqmf.v2;

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

    private String getAttributeValueFromDocument(XPath xPath, String mainXpath) throws XPathExpressionException {
        String value = (String)xPath.evaluate(mainXpath, document.getDocumentElement(), XPathConstants.STRING);
        if (value.length() == 0) {
            return "";
        }
        return value;
    }

    private String getAttributeValueFromDocument(XPath xPath, String mainXpath, String alternateXpath) throws XPathExpressionException {
        String value = getAttributeValueFromDocument(xPath, mainXpath);
        if (value.length() == 0) {
            value = getAttributeValueFromDocument(xPath, alternateXpath);
        }
        return value;
//        String value = (String)xPath.evaluate(mainXpath, document.getDocumentElement(), XPathConstants.STRING);
//        if (value.length() == 0) {
//            value = (String)xPath.evaluate(alternateXpath, document.getDocumentElement(), XPathConstants.STRING);
//            if (value.length() == 0) {
//                return "";
//            }
//            else {
//                return value;
//            }
//        }
//        else {
//            return value;
//        }
    }

    private void buildMeasureAttributes(XPath xPath) {
        NodeList attributes = (NodeList)xPath.evaluate("subjectOf/measureAttribute", document.getDocumentElement(), XPathConstants.NODESET);
        if (attributes == null || attributes.getLength() == 0) {
            return;
        }

        for (int index = 0; index < attributes.getLength(); index++) {
            Node attribute = attributes.item(index);
        }
    }

    private void setupDefaultValues(String hqmfContents, boolean useDefaultMeasurePeriod) throws Exception {
        idGenerator = new IdGenerator();
        document = XmlHelpers.LoadXMLFromString(hqmfContents);
        //document = XmlHelpers.addNamespaceToDocument(document, "cda", "urn:hl7-org:v3");

        XPath xPath = XPathFactory.newInstance().newXPath();
        NamespaceContext context = new UniversalNamespaceCache(document, true);
        xPath.setNamespaceContext(context);
        id = getAttributeValueFromDocument(xPath,"id/@extension", "id/@root");
        hqmfSetId = getAttributeValueFromDocument(xPath, "setId/@extension", "setId/@root");
        hqmfVersionNumber = getAttributeValueFromDocument(xPath, "versionNumber/@value");
        buildMeasureAttributes(xPath);
    }

}
