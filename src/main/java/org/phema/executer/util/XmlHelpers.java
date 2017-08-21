package org.phema.executer.util;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * Created by Luke Rasmussen on 7/25/17.
 */
public class XmlHelpers {
    public static Document LoadXMLFromString(String xml) throws Exception
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
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

    public static String DocumentToString(Document document) throws TransformerException {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(document), new StreamResult(writer));
        String output = writer.getBuffer().toString().replaceAll("\n|\r", "");
        return output;
    }
}
