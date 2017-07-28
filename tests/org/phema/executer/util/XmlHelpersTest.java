package org.phema.executer.util;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import javax.xml.transform.TransformerException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by Luke Rasmussen on 7/25/17.
 */
class XmlHelpersTest {
    @Test
    void loadXMLFromString() throws Exception {
        Document result = XmlHelpers.LoadXMLFromString("<test>Test document</test>");
        assertNotNull(result);
        assertEquals("Test document", result.getDocumentElement().getTextContent());
    }

    @Test
    void loadXMLFromString_Invalid() {
        assertThrows(Exception.class, () -> { XmlHelpers.LoadXMLFromString("well, this is certainly not XML!"); });
    }

    @Test
    void documentToString() throws Exception {
        String xml = "<test>Test document</test>";
        Document document = XmlHelpers.LoadXMLFromString(xml);
        String resultXml = XmlHelpers.DocumentToString(document);
        assertEquals(xml, resultXml);
    }

    @Test
    void documentToString_NullDocument() throws TransformerException {
        assertEquals("", XmlHelpers.DocumentToString(null));
    }

}