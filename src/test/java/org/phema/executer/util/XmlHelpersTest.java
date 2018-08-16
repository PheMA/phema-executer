package org.phema.executer.util;

import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.w3c.dom.Document;

import javax.xml.transform.TransformerException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by Luke Rasmussen on 7/25/17.
 */
@RunWith(JUnitPlatform.class)
public class XmlHelpersTest {
    @Test
    void loadXMLFromString() throws Exception {
        Document result = XmlHelpers.loadXMLFromString("<test>Test document</test>");
        assertNotNull(result);
        assertEquals("Test document", result.getDocumentElement().getTextContent());
    }

    @Test
    void loadXMLFromString_Invalid() {
        assertThrows(Exception.class, () -> { XmlHelpers.loadXMLFromString("well, this is certainly not XML!"); });
    }

    @Test
    void documentToString() throws Exception {
        String xml = "<test>Test document</test>";
        Document document = XmlHelpers.loadXMLFromString(xml);
        String resultXml = XmlHelpers.documentToString(document);
        assertEquals(xml, resultXml);
    }

    @Test
    void documentToString_NullDocument() throws TransformerException {
        assertEquals("", XmlHelpers.documentToString(null));
    }

}