package org.phema.executer.hqmf.v2;

import org.phema.executer.util.XmlHelpers;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Luke Rasmussen on 9/29/17.
 */
public class DocumentPopulationHelper {
    private Node entry;
    private Node docNode;
    private Document document;
    private IdGenerator idGenerator;
    private ArrayList referenceIds;
    private ArrayList populations;
    private ArrayList populationCriteria;
    private ArrayList stratifications;
    private HashMap<String, Object> idsByHqmfId;
    private HashMap<String, Object> populationCounters;

    public DocumentPopulationHelper(Node entry, Node doc, Document document, IdGenerator idGenerator, ArrayList referenceIds) throws XPathExpressionException {
        this.entry = entry;
        this.docNode = doc;
        removePopulationPreconditions(this.docNode);
        this.document = document;
        this.idGenerator = idGenerator;
        this.referenceIds = referenceIds;
        this.populations = new ArrayList();
        this.populationCriteria = new ArrayList();
        this.stratifications = new ArrayList();
        this.idsByHqmfId = new HashMap<>();
        this.populationCounters = new HashMap<>();
    }

    // If a precondition references a population, remove it
    private void removePopulationPreconditions(Node doc) throws XPathExpressionException {
        XPath xPath = XmlHelpers.createXPath(doc.getOwnerDocument());
        // population sections
        NodeList popIds = (NodeList)xPath.evaluate("//populationCriteriaSection/component[@typeCode='COMP']/*/id", doc, XPathConstants.NODESET);
        // find the population entries and get their ids
        for (int index = 0; index < popIds.getLength(); index++) {
            Node popId = popIds.item(index);
            Node node = (Node)xPath.evaluate(String.format("//precondition[./criteriaReference/id[@extension='%s' and @root='%s']]",
                    popId.getAttributes().getNamedItem("extension"), popId.getAttributes().getNamedItem("root")), doc, XPathConstants.NODE);
            if (node != null) {
                node.getParentNode().removeChild(node);
            }
        }
    }
}
