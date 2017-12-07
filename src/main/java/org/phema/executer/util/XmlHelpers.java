package org.phema.executer.util;

import org.phema.executer.UniversalNamespaceCache;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;

/**
 * Created by Luke Rasmussen on 7/25/17.
 */
public class XmlHelpers {
    public static Document loadXMLFromString(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(xml));
        return builder.parse(is);
    }

    // Solution from https://stackoverflow.com/a/11146971/5670646
//    public static Document addNamespaceToDocument(Document document, String prefix, String namespace) {
//        Element rootElement = document.getDocumentElement();
//        rootElement.setAttributeNS(namespace, )
//        // Upgrade the DOM level 1 to level 2 with the correct namespace
//        Element originalDocumentElement = document.getDocumentElement();
//        Element newDocumentElement = document.createElementNS(namespace, originalDocumentElement.getNodeName());
//        // Set the desired namespace and prefix
//        newDocumentElement.setPrefix(prefix);
//        // Copy all children
//        NodeList list = originalDocumentElement.getChildNodes();
//        while(list.getLength() != 0) {
//            newDocumentElement.appendChild(list.item(0));
//        }
//        // Replace the original element
//        document.replaceChild(newDocumentElement, originalDocumentElement);
//        return document;
//    }

    public static String documentToString(Document document) throws TransformerException {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(document), new StreamResult(writer));
        String output = writer.getBuffer().toString().replaceAll("\n|\r", "");
        return output;
    }

    public static XPath createXPath(Document document) {
        XPath documentXPath = XPathFactory.newInstance().newXPath();
        NamespaceContext context = new UniversalNamespaceCache(document, true, "cda");
        documentXPath.setNamespaceContext(context);
        return documentXPath;
    }

    public static String getAttributeValue(Element element, XPath documentXPath, String xpath, String defaultValue) throws XPathExpressionException {
        String value = (String)documentXPath.compile(xpath).evaluate(element, XPathConstants.STRING);
        if (value.length() == 0) {
            return defaultValue;
        }
        return value;
    }

    public static String getAttributeValue(Node node, XPath documentXPath, String xpath, String defaultValue) throws XPathExpressionException {
        return getAttributeValue((Element)node, documentXPath, xpath, defaultValue);
    }

    public static String getNodeText(Node node, XPath documentXPath, String xpath, String defaultValue) throws XPathExpressionException {
        Node textNode = ((Node)documentXPath.evaluate(xpath, node, XPathConstants.NODE));
        if (textNode == null) {
            return defaultValue;
        }

        return textNode.getTextContent();
    }

    // Derived from http://www.java2s.com/Code/Java/XML/ConvertNodeListToNodeArray.htm
    public static ArrayList<Node> convertNodeListToArray(NodeList list) {
        int length = list.getLength();
        ArrayList<Node> array = new ArrayList<Node>(length);
        for (int index = 0; index < length; index++) {
            array.add(list.item(index));
        }

        return array;
    }

//    public static String getAttributeValue(Element element, String xpath) throws XPathExpressionException {
//        return getAttributeValue(element, xpath, "");
//    }
}
