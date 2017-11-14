package org.phema.executer.hqmf.v2;

import org.phema.executer.util.XmlHelpers;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import java.util.HashMap;

/**
 * Created by Luke Rasmussen on 8/30/17.
 */
public class TemporalReference {
    private String type;
    private Reference reference;
    private Range range;
    private Node entry;

    public TemporalReference(Node entry) throws XPathExpressionException {
        this.entry = entry;
        XPath xPath = XmlHelpers.createXPath(entry.getOwnerDocument());
        String typeAttrValue = XmlHelpers.getAttributeValue((Element)this.entry, xPath, "./@typeCode", "");
        if (UPDATED_TYPES.containsKey(typeAttrValue)) {
            this.type = UPDATED_TYPES.get(typeAttrValue);
        }
        else {
            this.type = typeAttrValue;
        }
        this.reference = new Reference((Node)xPath.evaluate("./*/id", this.entry, XPathConstants.NODE));
        Node rangeDef = (Node)xPath.evaluate("./temporalInformation/delta", this.entry, XPathConstants.NODE);
        if (rangeDef != null) {
            this.range = new Range(rangeDef, "IVL_PQ");
        }
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Reference getReference() {
        return reference;
    }

    public void setReference(Reference reference) {
        this.reference = reference;
    }

    public Range getRange() {
        return range;
    }

    public void setRange(Range range) {
        this.range = range;
    }
    
    // Use updated mappings to HDS temporal reference types (as used in SimpleXML Parser)
    // https://github.com/projecttacoma/simplexml_parser/blob/fa0f589d98059b88d77dc3cb465b62184df31671/lib/model/types.rb#L167
    public static final HashMap<String, String> UPDATED_TYPES = new HashMap<String, String>(){{
        put("EAOCW", "EACW");
        put("EAEORECW", "EACW");
        put("EAOCWSO", "EACWS");
        put("EASORECWS", "EACWS");
        put("EBOCW", "EBCW");
        put("EBEORECW", "EBCW");
        put("EBOCWSO", "EBCWS");
        put("EBSORECWS", "EBCWS");
        put("ECWSO", "ECWS");
        put("SAOCWEO", "SACWE");
        put("SAEORSCWE", "SACWE");
        put("SAOCW", "SACW");
        put("SASORSCW", "SACW");
        put("SBOCWEO", "SBCWE");
        put("SBEORSCWE", "SBCWE");
        put("SBOCW", "SBCW");
        put("SBSORSCW", "SBCW");
        put("SCWEO", "SCWE");
        put("OVERLAPS", "OVERLAP");
    }};
}
