package org.phema.executer.hqmf.v2;

import org.phema.executer.hqmf.models.AnyValue;
import org.phema.executer.hqmf.v2.Value;
import org.phema.executer.util.XmlHelpers;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

/**
 * Created by Luke Rasmussen on 8/29/17.
 *
 * Represents a HQMF physical quantity which can have low and high bounds
 */
public class Range {
    private Object low;
    private Object high;
    private Object width;
    private String type;
    private Node entry;

    public Range(Node entry, String type) throws XPathExpressionException {
        this.type = type;
        this.entry = entry;
        if (this.entry == null) {
            return;
        }

        XPath xPath = XmlHelpers.createXPath(entry.getOwnerDocument());
        if (this.type == null) {
            this.type = XmlHelpers.getAttributeValue((Element) this.entry, xPath, "./@xsi:type", "");
        }
        String defaultName = defaultElementName();
        this.low = optionalValue(xPath, defaultName + "/low", defaultBoundsType());
        this.high = optionalValue(xPath, defaultName + "/high", defaultBoundsType());
        //      # Unset low bound to resolve verbose value bounds descriptions
        //TODO: @low = nil if (@high.try(:value) && @high.value.to_i > 0) && (@low.try(:value) && @low.value.try(:to_i) == 0)
        this.width = optionalValue(xPath, defaultName + "/width", "PQ");
    }

    public Object getLow() {
        return low;
    }

    public void setLow(Object low) {
        this.low = low;
    }

    public Object getHigh() {
        return high;
    }

    public void setHigh(Object high) {
        this.high = high;
    }

    public Object getWidth() {
        return width;
    }

    public void setWidth(Object width) {
        this.width = width;
    }

    // Either derives a value from a specific path or generates a new value (or returns nil if none found)
    private Object optionalValue(XPath xPath, String xpathLocation, String type) throws XPathExpressionException {
        Node valueDef = (Node)xPath.evaluate(xpathLocation, this.entry, XPathConstants.NODE);
        if (valueDef == null) {
            return "";
        }
        if (valueDef.getAttributes().getNamedItem("flavorId").getNodeValue().equals("ANY.NONNULL")) {
            return new AnyValue();
        }

        Value createdValue = new Value(valueDef, type);
        // Return nil if no value was parsed
        if (createdValue.getValue().length() != 0) {
            return createdValue;
        }
        return null;
    }

    // Defines how the time based element should be described
    private String defaultElementName() {
        switch (this.type) {
            case "IVL_PQ":
                return ".";
            case "IVL_TS":
                return "phase";
            default:
                return "uncertainRange";
        }
    }

    // Sets up the default bound type as either time based or a physical quantity
    private String defaultBoundsType() {
        switch (this.type) {
            case "IVL_TS":
                return "TS";
            default:
                return "PQ";
        }
    }
}
