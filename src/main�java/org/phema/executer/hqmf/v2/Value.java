package org.phema.executer.hqmf.v2;

import org.phema.executer.util.XmlHelpers;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;

/**
 * Created by Luke Rasmussen on 8/29/17.
 *
 * Represents a bound within a HQMF pauseQuantity, has a value, a unit and an
 * inclusive/exclusive indicator
 */
public class Value {
    private String type;
    private String unit;
    private String value;
    private Node entry;
    private boolean forceInclusive;

    public Value(Node entry, String defaultType, boolean forceInclusive) throws XPathExpressionException {
        initialize(entry, defaultType, forceInclusive);
    }

    public Value(Node entry) throws XPathExpressionException {
        initialize(entry, "PQ", false);
    }

    public Value(Node entry, String defaultType) throws XPathExpressionException {
        initialize(entry, defaultType, false);
    }

    private void initialize(Node entry, String defaultType, boolean forceInclusive) throws XPathExpressionException {
        this.entry = entry;
        XPath xPath = XmlHelpers.createXPath(entry.getOwnerDocument());
        Element entryElement = (Element)entry;
        this.type = XmlHelpers.getAttributeValue(entryElement, xPath, "./@type", "");
        if (this.type.length() == 0) {
            this.type = defaultType;
        }
        this.unit = XmlHelpers.getAttributeValue(entryElement, xPath, "./@unit", "");
        this.value = XmlHelpers.getAttributeValue(entryElement, xPath, "./@value", "");
        this.forceInclusive = forceInclusive;

        // FIXME: Remove below when lengthOfStayQuantity unit is fixed
        if (this.unit.equals("days")) {
            this.unit = "d";
        }
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
