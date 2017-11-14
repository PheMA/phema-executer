package org.phema.executer.hqmf.v2;

import org.phema.executer.hqmf.models.AnyValue;
import org.phema.executer.util.XmlHelpers;
import org.w3c.dom.Node;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Luke Rasmussen on 9/1/17.
 *
 * Represents a subset of a specific group (the first in the group, the sum of the group, etc.)
 */
public class SubsetOperator {
    private String type;
    private Object value;
    private Node entry;

    public static final String[] ORDER_SUBSETS = new String[] { "FIRST", "SECOND", "THIRD", "FOURTH", "FIFTH" };
    public static final String[] LAST_SUBSETS = new String[] { "LAST", "RECENT" };
    public static final String[] TIME_SUBSETS = new String[] { "DATEDIFF", "TIMEDIFF" };
    public static final HashMap<String, String> QDM_TYPE_MAP = new HashMap<String, String>() {{
        put("QDM_LAST:", "RECENT");
        put("QDM_SUM:SUM", "COUNT");
    }};

    public SubsetOperator(Node entry) throws XPathExpressionException {
        this.entry = entry;

        XPath xPath = XmlHelpers.createXPath(this.entry.getOwnerDocument());
        String sequenceNumber = XmlHelpers.getAttributeValue(this.entry, xPath, "./sequenceNumber/@value", "");
        //FIXME: this is supposed to be prefixed with qdm (qdm:subsetCode) but we don't have qdm namespace available
        String qdmSubsetCode = XmlHelpers.getAttributeValue(this.entry, xPath, "./subsetCode/@code", "");
        String subsetCode = XmlHelpers.getAttributeValue(this.entry, xPath, "./subsetCode/@code", "");
        if (sequenceNumber.length() > 0) {
            this.type = ORDER_SUBSETS[Integer.parseInt(sequenceNumber) - 1];
        }
        else {
            this.type = translateType(subsetCode, qdmSubsetCode);
        }

        Node valueDef = handleValueDefinition(xPath);
        if (valueDef != null && this.value == null) {
            this.value = new Range(valueDef, "IVL_PQ");
        }
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    // Return the value definition (what to calculate it on) associated with this subset.
    // Other values, such as type and value, may be modified depending on this value.
    private Node handleValueDefinition(XPath xPath) throws XPathExpressionException {
        Node valueDef = (Node)xPath.evaluate("./*/repeatNumber", this.entry, XPathConstants.NODE);
        if (valueDef == null) {
            // TODO: HQMF needs better differentiation between SUM & COUNT...
            // currently using presence of repeatNumber...
            if (this.type.equals("COUNT")) {
                this.type = "SUM";
            }
            valueDef = (Node)xPath.evaluate("./*/value", this.entry, XPathConstants.NODE);
        }

        // TODO: Resolve extracting values embedded in criteria within outboundRel's
        if (this.type.equals("SUM")) {
            valueDef = (Node)xPath.evaluate("./*/*/*/value", this.entry, XPathConstants.NODE);
        }

        if (valueDef != null) {
            String valueType = XmlHelpers.getAttributeValue(valueDef, xPath, "./@type", "");
            if (valueType.equals("ANY")) {
                this.value = new AnyValue();
            }
        }

        return valueDef;
    }

    // Take a qdm type code to map it to a subset operator, or failing at finding that, return the given subset code.
    private String translateType(String subsetCode, String qdmSubsetCode) {
        String combined = String.format("%s:%s", qdmSubsetCode, subsetCode);
        if (QDM_TYPE_MAP.containsKey(combined)) {
            return QDM_TYPE_MAP.get(combined);
        }
        else {
            return subsetCode;
        }
    }
}
