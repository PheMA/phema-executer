package org.phema.executer.hqmf.v2;

import org.phema.executer.util.XmlHelpers;
import org.w3c.dom.Node;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;

/**
 * Created by Luke Rasmussen on 8/30/17.
 */
public class Reference {
    private Node entry;

    public String getId() {
        return id;
    }

    private String id;

    public Reference(Node entry) throws XPathExpressionException {
        this.entry = entry;
        XPath xPath = XmlHelpers.createXPath(entry.getOwnerDocument());
        String extension = XmlHelpers.getAttributeValue(this.entry, xPath, "./@extension", "");
        String root = XmlHelpers.getAttributeValue(this.entry, xPath, "./@root", "");
        this.id = Utilities.stripTokens(String.format("%s_%s", extension, root));
        // Handle MeasurePeriod references for calculation code
        if (this.id.toLowerCase().startsWith("measureperiod")) {
            this.id = "MeasurePeriod";
        }
    }
}
