package org.phema.executer.hqmf.v2;

import org.phema.executer.util.XmlHelpers;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Luke Rasmussen on 8/30/17.
 */
public class DataCriteriaBaseExtractions {
    private Node entry;
    private XPath xPath;

    private static final HashMap<String, String> CONJUNCTION_CODE_TO_DERIVATION_OP = new HashMap<String, String>()
    {{
      put("OR", "UNION");
      put("AND", "XPRODUCT");
    }};

    public DataCriteriaBaseExtractions(Node entry) {
        this.entry = entry;
        this.xPath = XmlHelpers.createXPath(entry.getOwnerDocument());
    }

    public ArrayList<String> extractTemplateIds() throws XPathExpressionException {
        ArrayList<String> templateIds = new ArrayList<String>();
        NodeList items = (NodeList)xPath.evaluate("./*/cda:templateId/cda:item", this.entry, XPathConstants.NODESET);
        for (int index = 0; index < items.getLength(); index++) {
            Node item = items.item(index);
            templateIds.add(item.getAttributes().getNamedItem("root").getNodeValue());
        }
        return templateIds;
    }

    // Extracts the derivation operator to be used by the data criteria, and fails out if it finds more than one (should
    // not be valid)
    public String extractDerivationOperator() throws Exception {
        NodeList codes = (NodeList)this.xPath.evaluate("./*/cda:outboundRelationship[@typeCode='COMP']/cda:conjunctionCode[@code]", this.entry, XPathConstants.NODESET);
        if (codes != null) {
            String dOp = null;
            for (int index = 0; index < codes.getLength(); index++) {
                Node code = codes.item(index);
                String codeValue = code.getAttributes().getNamedItem("code").getNodeValue();
                if (dOp != null && dOp != CONJUNCTION_CODE_TO_DERIVATION_OP.get(codeValue)) {
                    throw new Exception("More than one derivation operator in data criteria");
                }
                dOp = CONJUNCTION_CODE_TO_DERIVATION_OP.get(codeValue);
            }
            return dOp;
        }

        return "";
    }

    // Extract the local variable name (held in the value of the localVariableName element)
    public String extractLocalVariableName() throws XPathExpressionException {
        return XmlHelpers.getAttributeValue((Element)this.entry, this.xPath, "./localVariableName/@value", "");
    }

    public ArrayList<TemporalReference> extractTemporalReferences() throws XPathExpressionException {
        ArrayList<TemporalReference> references = new ArrayList<TemporalReference>();
        NodeList referenceNodes = (NodeList)this.xPath.evaluate("./*/cda:temporallyRelatedInformation", this.entry, XPathConstants.NODESET);
        for (int index = 0; index < referenceNodes.getLength(); index++) {
            Node referenceNode = referenceNodes.item(index);
            references.add(new TemporalReference(referenceNode));
        }
        return references;
    }

    // Generate a list of child criteria
    public ArrayList<String> extractChildCriteria() throws XPathExpressionException {
        ArrayList<String> criteria = new ArrayList<String>();
        NodeList nodes = (NodeList)this.xPath.evaluate("./*/cda:outboundRelationship[@typeCode='COMP']/cda:criteriaReference/cda:id", this.entry, XPathConstants.NODESET);
        for (int index = 0; index < nodes.getLength(); index++) {
            Reference ref = new Reference(nodes.item(index));
            if (ref != null && ref.getId() != null) {
                criteria.add(ref.getId());
            }
        }

        return criteria;
    }

    // Filters all the subset operators to only include the ones of type 'UNION' and 'XPRODUCT'
    public ArrayList<SubsetOperator> extractSubsetOperators() throws XPathExpressionException {
        ArrayList<SubsetOperator> filteredList = new ArrayList<SubsetOperator>();
        ArrayList<SubsetOperator> list = allSubsetOperators();
        for (int index = 0; index < list.size(); index++) {
            SubsetOperator operator = list.get(index);
            if (operator.getType() != "UNION" && operator.getType() != "XPRODUCT") {
                filteredList.add(operator);
            }
        }
        return filteredList;
    }

    // Extracts all subset operators contained in the entry xml
    public ArrayList<SubsetOperator> allSubsetOperators() throws XPathExpressionException {
        ArrayList<SubsetOperator> operators = new ArrayList<SubsetOperator>();
        NodeList operatorNodes = (NodeList)xPath.evaluate("./*/cda:excerpt", this.entry, XPathConstants.NODESET);
        for (int index = 0; index < operatorNodes.getLength(); index++) {
            operators.add(new SubsetOperator(operatorNodes.item(index)));
        }
        return operators;
    }

    // Extract the negation
    public boolean extractNegation() throws XPathExpressionException {
        String negationValue = XmlHelpers.getAttributeValue(this.entry, this.xPath, "./*/@actionNegationInd", "false");
        return (negationValue.toLowerCase().equals("true"));
    }

    // Extract the negation code list ID (if appropriate)
    public String extractNegationCodeListId(boolean negation) throws XPathExpressionException {
        if (negation) {
            Node code = (Node)xPath.evaluate("./*/cda:outboundRelationship/*/cda:code[@code=\"410666004\"]/../cda:value/@valueSet", this.entry, XPathConstants.NODE);
            if (code != null) {
                return code.getNodeValue();
            }
        }

        return "";
    }
}
