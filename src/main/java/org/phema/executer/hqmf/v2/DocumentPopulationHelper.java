package org.phema.executer.hqmf.v2;

import org.phema.executer.util.XmlHelpers;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Luke Rasmussen on 9/29/17.
 */
public class DocumentPopulationHelper {
    private Node entry;
    private Node docNode;
    private XPath xPath;
    private Document document;
    private IdGenerator idGenerator;
    private ArrayList<String> referenceIds;
    private ArrayList<Population> populations;
    private ArrayList<PopulationCriteria> populationCriteria;
    private ArrayList<Population> stratifications;
    private HashMap<String, String> idsByHqmfId;
    private HashMap<String, Integer> populationCounters;

    private static final HashMap<String, String> basePopulations = new HashMap<String, String>() {{
        put(PopulationCriteria.IPP , "initialPopulationCriteria");
        put(PopulationCriteria.DENOM , "denominatorCriteria");
        put(PopulationCriteria.NUMER , "numeratorCriteria");
        put(PopulationCriteria.NUMEX , "numeratorExclusionCriteria");
        put(PopulationCriteria.DENEXCEP , "denominatorExceptionCriteria");
        put(PopulationCriteria.DENEX , "denominatorExclusionCriteria");
        put(PopulationCriteria.MSRPOPL , "measurePopulationCriteria");
        put(PopulationCriteria.MSRPOPLEX , "measurePopulationExclusionCriteria");
    }};

    public DocumentPopulationHelper(Node entry, Node doc, Document document, IdGenerator idGenerator, ArrayList referenceIds) throws XPathExpressionException {
        this.entry = entry;
        this.docNode = doc;
        this.xPath = XmlHelpers.createXPath(doc.getOwnerDocument());
        removePopulationPreconditions(this.docNode);
        this.document = document;
        this.idGenerator = idGenerator;
        this.referenceIds = referenceIds;
        this.populations = new ArrayList<>();
        this.populationCriteria = new ArrayList<>();
        this.stratifications = new ArrayList();
        this.idsByHqmfId = new HashMap<>();
        this.populationCounters = new HashMap<>();
    }

    // If a precondition references a population, remove it
    private void removePopulationPreconditions(Node doc) throws XPathExpressionException {
        // population sections
        NodeList popIds = (NodeList)xPath.evaluate("//cda:populationCriteriaSection/cda:component[@typeCode='COMP']/*/cda:id", doc, XPathConstants.NODESET);
        // find the population entries and get their ids
        for (int index = 0; index < popIds.getLength(); index++) {
            Node popId = popIds.item(index);
            Node node = (Node)xPath.evaluate(String.format("//cda:precondition[./cda:criteriaReference/cda:id[@extension='%s' and @root='%s']]",
                    popId.getAttributes().getNamedItem("extension"), popId.getAttributes().getNamedItem("root")), doc, XPathConstants.NODE);
            if (node != null) {
                node.getParentNode().removeChild(node);
            }
        }
    }

    // Returns the population descriptions and criteria found in this document
    public Object[] extractPopulationsAndCriteria() throws Exception {
        boolean hasObservation = extractObservations();
        NodeList documentPopulations = (NodeList)xPath.evaluate("cda:QualityMeasureDocument/cda:component/cda:populationCriteriaSection", this.docNode.getOwnerDocument(), XPathConstants.NODESET);
        // Sort the populations based on the id/extension, since the populations may be out of order; there doesn't seem to
        // be any other way that order is indicated in the HQMF
        Collections.sort(XmlHelpers.convertNodeListToArray(documentPopulations), new Comparator<Node>() {
            @Override
            public int compare(Node left, Node right) {
                if (left == null && right == null) { return 0; }
                else if (left == null) { return -1; }
                else if (right == null) { return 1; }

                String leftValue = "";
                String rightValue = "";
                try {
                    leftValue = XmlHelpers.getAttributeValue(left, xPath,"cda:id/@extension", "");
                    rightValue = XmlHelpers.getAttributeValue(right, xPath,"cda:id/@extension", "");
                } catch (XPathExpressionException e) {
                    e.printStackTrace();
                }

                return leftValue.compareTo(rightValue);
            }
        });

        int numberOfPopulations = documentPopulations.getLength();
        for (int index = 0; index < numberOfPopulations; index++) {
            Node populationNode = documentPopulations.item(index);
            Population population = new Population();
            handleBasePopulations(populationNode, population);
            String idDef = XmlHelpers.getAttributeValue(populationNode, xPath, "cda:id/@extension", null);
            population.setId((idDef == null) ? String.format("Population %d", index) : idDef);
            String titleDef = XmlHelpers.getAttributeValue(populationNode, xPath, "cda:title/@value", null);
            population.setTitle((titleDef == null) ? String.format("Population %d", index) : titleDef);
            if (hasObservation) {
                population.setObservation("OBSERV");
            }
            this.populations.add(population);

            handleStratifications(populationNode, numberOfPopulations, population, idDef, index);
        }

        // Push in the stratification populations after the unstratified populations
        this.populations.addAll(stratifications);
        Object[] results = new Object[2];
        results[0] = this.populations;
        results[1] = this.populationCriteria;
        return results;
    }

    // Builds populations based an a predfined set of expected populations
    private void handleBasePopulations(Node populationDef, Population population) throws Exception {
        for (String criteriaId : basePopulations.keySet()) {
            String criteriaElementName = basePopulations.get(criteriaId);
            Node criteriaDef = (Node)xPath.evaluate(String.format("cda:component[cda:%s]", criteriaElementName), populationDef, XPathConstants.NODE);
            if (criteriaDef != null) {
                buildPopulationCriteria(criteriaDef, criteriaId, population);
            }
        }
    }

    // Generate the stratifications of populations, if any exist
    private void handleStratifications(Node populationDef, int numberOfPopulations, Population population, String idDef, int populationIndex) throws Exception {
        // handle stratifications (EP137, EP155)
        String stratifierXpath = "cda:component/cda:stratifierCriteria[not(cda:component/cda:measureAttribute/cda:code[@code  = 'SDE'])]/..";
        NodeList criteriaDefs = (NodeList)xPath.evaluate(stratifierXpath, populationDef, XPathConstants.NODESET);
        for (int criteriaDefIndex = 0; criteriaDefIndex < criteriaDefs.getLength(); criteriaDefIndex++) {
            Node criteriaDef = criteriaDefs.item(criteriaDefIndex);
            // Skip this Stratification if any precondition doesn't contain any preconditions
            PopulationCriteria tempCriteria = new PopulationCriteria(criteriaDef, this.document, this.idGenerator);
            if (tempCriteria != null && tempCriteria.getPreconditions() != null) {
                if (tempCriteria.getPreconditions().stream().allMatch(prcn -> prcn.getPreconditions().size() > 0)) {
                    continue;
                }
            }

            int index = numberOfPopulations + (((populationIndex - 1) * ((NodeList)xPath.evaluate("./*/cda:precondition", criteriaDef, XPathConstants.NODESET)).getLength())) + criteriaDefIndex;
            String criteriaId = PopulationCriteria.STRAT;
            Population stratifiedPopulation = new Population(population);
            String stratificationValue = XmlHelpers.getAttributeValue(criteriaDef, this.xPath, "./*/cda:id/@root", "");
            if (stratificationValue.length() == 0) {
                stratificationValue = String.format("%s-%d", criteriaId, criteriaDefIndex);
            }
            stratifiedPopulation.setAdditionalKey("stratification", stratificationValue);
            buildPopulationCriteria(criteriaDef, criteriaId, stratifiedPopulation);

            stratifiedPopulation.setId((idDef.length() > 0) ? String.format("%s - Stratification %d", idDef, (criteriaDefIndex + 1))
                    : String.format("Population%d", index));
            String titleDef = XmlHelpers.getAttributeValue(populationDef, xPath,"cda:title/@value", "");
            stratifiedPopulation.setTitle((titleDef.length() > 0) ? String.format("%s - Stratification %d", titleDef, (criteriaDefIndex + 1))
                    : String.format("Population%d", index));
            this.stratifications.add(stratifiedPopulation);
        }
    }

    // Extracts the measure observations, will return true if one exists
    private boolean extractObservations() throws Exception {
        boolean hasObservations = false;
        // look for observation data in separate section but create a population for it if it exists
        Node observationSection = (Node)xPath.evaluate("/cda:QualityMeasureDocument/cda:component/cda:measureObservationSection",
                this.entry, XPathConstants.NODE);
        if (observationSection == null) {
            return hasObservations;
        }

        NodeList definitionNodes = (NodeList)xPath.evaluate("cda:definition",
                observationSection, XPathConstants.NODESET);
        for (int index = 0; index < definitionNodes.getLength(); index++) {
            Node criteriaDef = definitionNodes.item(index);
            String criteriaId = "OBSERV";
            PopulationCriteria criteria = new PopulationCriteria(criteriaDef, this.document, this.idGenerator);
            criteria.setType("OBSERV");
            // This section constructs a human readable id.  The first IPP will be IPP, the second will be IPP_1, etc.
            // This allows the populations to be more readable.  The alternative would be to have the hqmf ids in the
            // populations, which would work, but is difficult to read the populations.
            if (this.idsByHqmfId.containsKey(criteria.getHqmfId())) {
                criteria.createHumanReadableId(this.idsByHqmfId.get(criteria.getHqmfId()));
            }
            else {
                criteria.createHumanReadableId(populationIdWithCounter(criteriaId));
                this.idsByHqmfId.put(criteria.getHqmfId(), criteria.getId());
            }

            this.populationCriteria.add(criteria);
            hasObservations = true;
        }

        return hasObservations;
    }

    // Method to generate the criteria defining a population
    private void buildPopulationCriteria(Node criteriaDef, String criteriaId, Population population) throws Exception {
        PopulationCriteria criteria = new PopulationCriteria(criteriaDef, this.document, this.idGenerator);

        // check to see if we have an identical population criteria.
        // this can happen since the hqmf 2.0 will export a DENOM, NUMER, etc for each population, even if identical.
        // if we have identical, just re-use it rather than creating DENOM_1, NUMER_1, etc.
        List<PopulationCriteria> identical = this.populationCriteria.stream()
                .filter(pc -> pc.getHqmfId().equals(criteria.getHqmfId()))
                .collect(Collectors.toList());
        if (identical == null || identical.size() == 0) {
            // this section constructs a human readable id.  The first IPP will be IPP, the second will be IPP_1, etc.
            // This allows the populations to be more readable.  The alternative would be to have the hqmf ids in the
            // populations, which would work, but is difficult to read the populations.

            this.populationCriteria.add(criteria);
            population.setAdditionalKey(criteriaId, criteria.getId());
        }
        else {
            population.setAdditionalKey(criteriaId, identical.get(0).getId());
        }
    }

    // Returns a unique id for a given population (increments the id if already present)
    private String populationIdWithCounter(String criteriaId) {
        if (this.populationCounters.containsKey(criteriaId)) {
            this.populationCounters.put(criteriaId, this.populationCounters.get(criteriaId) + 1);
            return String.format("%s_%d", criteriaId, this.populationCounters.get(criteriaId));
        }
        else {
            this.populationCounters.put(criteriaId, 0);
            return criteriaId;
        }
    }
}
