package org.phema.executer.util;

import org.phema.executer.UniversalNamespaceCache;
import org.phema.executer.exception.PhemaUserException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
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

    /**
     * From http://www.java2s.com/Code/Java/XML/Getchildfromanelementbyname.htm
     * @param parent
     * @param name
     * @param defaultValue
     * @return
     */
    public static String getChildContent(Element parent, String name, String defaultValue) {
        Element child = getChild(parent, name);
        if (child == null) {
            return defaultValue;
        } else {
            String content = (String) getContent(child);
            return (content != null) ? content : defaultValue;
        }
    }

    /**
     * Retrieve an element's content and convert it to an int.  If the value does not exist, or it is not an
     * integer, we will throw an exception.
     * @param parent The Element to search for the value
     * @param name The name of the child node within parent
     * @return An integer representation of the value contained in the node.
     */
    public static int getChildContentAsInt(Element parent, String name) throws PhemaUserException {
        String value = getChildContent(parent, name, "");
        if (value.length() == 0) {
            throw new PhemaUserException("There was an unexpected error when trying to get an integer value - the value does not exist or is empty.");
        }

        if (!tryParseInt(value)) {
            throw new PhemaUserException("There was an unexpected error when trying to get an integer level - the value is not numeric and cannot be converted to an integer.");
        }

        return Integer.parseInt(value);
    }

    /**
     * Retrieve an element's content and convert it to a long.  If the value does not exist, or it is not a
     * long, we will throw an exception.
     * @param parent The Element to search for the value
     * @param name The name of the child node within parent
     * @return A long representation of the value contained in the node.
     */
    public static long getChildContentAsLong(Element parent, String name) throws PhemaUserException {
        String value = getChildContent(parent, name, "");
        if (value.length() == 0) {
            throw new PhemaUserException("There was an unexpected error when trying to get a long value - the value does not exist or is empty.");
        }

        if (!tryParseLong(value)) {
            throw new PhemaUserException("There was an unexpected error when trying to get a long value - the value is not numeric and cannot be converted to a long.");
        }

        return Long.parseLong(value);
    }

    /**
     * From https://stackoverflow.com/a/8392032/5670646
     * @param value
     * @return
     */
    private static boolean tryParseInt(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * From https://stackoverflow.com/a/8392032/5670646
     * @param value
     * @return
     */
    private static boolean tryParseLong(String value) {
        try {
            Long.parseLong(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * From http://www.java2s.com/Code/Java/XML/Getchildfromanelementbyname.htm
     * @param element
     * @return
     */
    private static Object getContent(Element element) {
        NodeList nl = element.getChildNodes();
        StringBuffer content = new StringBuffer();
        for (int i = 0; i < nl.getLength(); i++) {
            Node node = nl.item(i);
            switch (node.getNodeType()) {
                case Node.ELEMENT_NODE:
                    return node;
                case Node.CDATA_SECTION_NODE:
                case Node.TEXT_NODE:
                    content.append(node.getNodeValue());
                    break;
            }
        }
        return content.toString().trim();
    }

    /**
     * From http://www.java2s.com/Code/Java/XML/Getchildfromanelementbyname.htm
     * @param parent
     * @param name
     * @return
     */
    private static Element getChild(Element parent, String name) {
        for (Node child = parent.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child instanceof Element && name.equals(child.getNodeName())) {
                return (Element) child;
            }
        }
        return null;
    }

    public static void saveToFile(Document document, String fileName) throws Exception{
        TransformerFactory tranFactory = TransformerFactory.newInstance();
        Transformer aTransformer = tranFactory.newTransformer();
        Source src = new DOMSource(document);
        Result dest = new StreamResult(new File(fileName));
        aTransformer.transform(src, dest);
    }

//    public static String getAttributeValue(Element element, String xpath) throws XPathExpressionException {
//        return getAttributeValue(element, xpath, "");
//    }
}
