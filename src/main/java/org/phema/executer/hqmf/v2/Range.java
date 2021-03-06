package org.phema.executer.hqmf.v2;

import org.phema.executer.hqmf.models.AnyValue;
import org.phema.executer.hqmf.v2.Value;
import org.phema.executer.util.ConversionHelpers;
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
            this.type = XmlHelpers.getAttributeValue((Element) this.entry, xPath, "./@type", "");
        }
        String defaultName = defaultElementName();
        this.low = optionalValue(xPath, defaultName + "/cda:low", defaultBoundsType());
        this.high = optionalValue(xPath, defaultName + "/cda:high", defaultBoundsType());
        // Unset low bound to resolve verbose value bounds descriptions
        if (this.high != null && this.high instanceof Value && this.low != null && this.low instanceof Value) {
            Integer highValue = ConversionHelpers.tryIntParse(((Value) this.high).getValue());
            Integer lowValue = ConversionHelpers.tryIntParse(((Value)this.low).getValue());
            if (highValue != null && highValue > 0 && lowValue != null && lowValue == 0) {
                this.low = null;
            }
        }
        this.width = optionalValue(xPath, defaultName + "/cda:width", "PQ");
    }

    public Range(String type, Value low, Value high, Value width) {
        this.type = type;
        this.low = low;
        this.high = high;
        this.width = width;
    }

    public Range(String type, Object low, Object high, Object width) {
        this.type = type;
        this.low = low;
        this.high = high;
        this.width = width;
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

    public String safeGetHighAsString() {
        return safeGetObjectAsValueString(this.high);
    }

    public String safeGetLowAsString() {
        return safeGetObjectAsValueString(this.low);
    }

    /**
     * Helper function that converts an object to a string representation, if it is a Value or a String
     * @param obj the object to convert
     * @return String value contained in the object, or null if we are unable to convert it
     */
    private String safeGetObjectAsValueString(Object obj) {
        if (obj == null) {
            return null;
        }

        Value asValue = (obj instanceof Value ? (Value)obj : null);
        if (asValue == null) {
            // It's not a value - is it a String?
            String asString = (obj instanceof String ? (String)obj : null);
            if (asString == null) {
                // If not, we don't know what to do with it
                return null;
            }

            // It's a String - return that
            return asString;
        }

        // It's a Value - return the contained value as a string.
        return asValue.getValue();
    }

    // Either derives a value from a specific path or generates a new value (or returns nil if none found)
    private Object optionalValue(XPath xPath, String xpathLocation, String type) throws XPathExpressionException {
        Node valueDef = (Node)xPath.evaluate(xpathLocation, this.entry, XPathConstants.NODE);
        if (valueDef == null) {
            return "";
        }

        Node flavorNode = valueDef.getAttributes().getNamedItem("flavorId");
        if (flavorNode != null && flavorNode.getNodeValue().equals("ANY.NONNULL")) {
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
