package org.phema.executer.hqmf.v2;

import org.apache.commons.lang.StringUtils;
import org.phema.executer.util.XmlHelpers;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import java.util.ArrayList;

/**
 * Created by Luke Rasmussen on 11/15/17.
 */
public class PopulationCriteria {
    public static final String IPP = "IPP";
    public static final String DENOM = "DENOM";
    public static final String NUMER = "NUMER";
    public static final String NUMEX = "NUMEX";
    public static final String DENEXCEP = "DENEXCEP";
    public static final String DENEX = "DENEX";
    public static final String MSRPOPL = "MSRPOPL";
    public static final String OBSERV = "OBSERV";
    public static final String MSRPOPLEX = "MSRPOPLEX";

    public static final String STRAT = "STRAT";

    public static final ArrayList<String> ALL_POPULATION_CODES = new ArrayList<String>() {{
            add(STRAT);
            add(IPP);
            add(DENOM);
            add(DENEX);
            add(NUMER);
            add(NUMEX);
            add(DENEXCEP);
            add(MSRPOPL);
            add(OBSERV);
            add(MSRPOPLEX);
        }};

    private String id;
    private String type;
    private Document document;
    private XPath xPath;
    private Node entry;
    private String comments;
    private String aggregator;
    private String hqmfId;
    private String title;
    private IdGenerator idGenerator;
    private ArrayList<Precondition> preconditions;

    public PopulationCriteria(Node entry, Document doc, IdGenerator idGenerator) throws Exception {
        this.idGenerator = idGenerator;
        this.document = doc;
        this.entry = entry;
        this.xPath = XmlHelpers.createXPath(entry.getOwnerDocument());
        setupDerivedEntryElements(idGenerator);
        // modify type to meet current expected population names
        if (this.type.equals("IPOP") || this.type.equals("IPPOP")) {
            this.type = "IPP";
        }
        if (this.comments != null && this.comments.length() == 0) {
            this.comments = null;
        }
        // MEAN is handled in current code. Changed since it should have the same effect
        if (this.aggregator != null && this.aggregator.equals("AVERAGE")) {
            this.aggregator = "MEAN";
        }
        // The id extension is not required, if it's not provided use the code
        if (this.hqmfId == null || this.hqmfId.length() == 0) {
            this.hqmfId = this.type;
        }
        handleType(idGenerator);
    }

    // Handles how the code should deal with the type definition (aggregate vs non-aggregate)
    private void handleType(IdGenerator idGenerator) throws Exception {
        if (!this.type.equals("AGGREGATE")) {
            // Generate the precondition for this population
            if (this.preconditions.size() > 1 ||
                    (this.preconditions.size() == 1 && !StringUtils.equals(preconditions.get(0).getConjunction(), conjunctionCode()))) {
                Precondition newPrecondition = new Precondition(Integer.toString(idGenerator.nextId()), conjunctionCode(), this.preconditions);
                this.preconditions = new ArrayList<Precondition>(){{ add(newPrecondition); }};
            }
        }
        else {
            // Extract the data criteria this population references
            DataCriteria dc = handleObservationCriteria();
            this.preconditions = new ArrayList<Precondition>() {{
                add(new Precondition(Integer.toString(idGenerator.nextId()), null, null, false, new Reference(dc.getId())));
            }};
        }
    }

    // extracts out any measure observation definitions, creating from them the proper criteria to generate a precondition
    private DataCriteria handleObservationCriteria() throws Exception {
        Node exp = (Node)this.xPath.evaluate("./cda:measureObservationDefinition/cda:value/cda:expression/@value", this.entry, XPathConstants.NODE);
        // Measure Observations criteria rely on computed expressions. If it doesn't have one,
        //  then it is likely formatted improperly.
        if (exp == null) {
            throw new Exception("Measure Observations criteria is missing computed expression(s)");
        }

        String[] parts = exp.getNodeValue().split("-");
        DataCriteria dc = parsePartsToDataCriteria(parts);
        this.document.addDataCriteria(dc);
        // Update reference_ids with any newly referenced data criteria
        ArrayList<String> children = dc.getChildrenCriteria();
        if (children != null) {
            for (String child : children) {
                this.document.addReferenceId(child);
            }
        }

        return dc;
    }

    // generates the value given in an expression based on the number of criteria it references.
    private DataCriteria parsePartsToDataCriteria(String[] parts) throws Exception {
        switch(parts.length) {
            case 1:
                // If there is only one part, it is a reference to an existing data criteria's value
                return this.document.findCriteriaByLocalVariableName(parts[0].trim().split("\\.")[0]);
            case 2:
                // If there are two parts, there is a computation performed, specifically time difference, on the two criteria
                ArrayList<String> children = new ArrayList<String>();
                for (String part : parts) {
                    children.add(this.document.findCriteriaByLocalVariableName(part.trim().split("\\.")[0]).getId());
                }
                String id = String.format("GROUP_TIMEDIFF_%s", Integer.toString(this.idGenerator.nextId()));
                DataCriteria newCriteria = new DataCriteria();
                newCriteria.setId(id);
                newCriteria.setTitle(id);
                newCriteria.setSubsetOperators(new ArrayList<SubsetOperator>() {{ add(new SubsetOperator("DATETIMEDIFF", null)); }});
                newCriteria.setChildrenCriteria(children);
                newCriteria.setDerivationOperator(DataCriteria.XPRODUCT);
                newCriteria.setType("derived");
                newCriteria.setDefinition("derived");
                newCriteria.setNegation(false);
                newCriteria.setSourceDataCriteria(id);
                return newCriteria;
            default:
                // If there are neither one or 2 parts, the code should fail
                throw new Exception(String.format("No defined extraction method to handle %d parts", parts.length));
        }
    }


    // Get the conjunction code, ALL_TRUE or AT_LEAST_ONE_TRUE
    private String conjunctionCode() throws Exception {
        switch (this.type) {
            case IPP:
            case DENOM:
            case NUMER:
            case MSRPOPL:
            case STRAT:
                return Precondition.ALL_TRUE;
            case DENEXCEP:
            case DENEX:
            case MSRPOPLEX:
            case NUMEX:
                return Precondition.AT_LEAST_ONE_TRUE;
        }

        throw new Exception(String.format("Unknown population type [%s]", this.type));
    }

    // Handles extracting elements from the entry
    private void setupDerivedEntryElements(IdGenerator idGenerator) throws XPathExpressionException {
        this.hqmfId = XmlHelpers.getAttributeValue(this.entry, this.xPath, "./*/cda:id/@root", "");
        if (this.hqmfId.length() == 0) {
            this.hqmfId = XmlHelpers.getAttributeValue(this.entry, this.xPath, "./*/cda:typeId/@extension", "");
        }
        this.title = XmlHelpers.getAttributeValue(this.entry, this.xPath, "./*/cda:code/cda:displayName/@value", "");
        this.type = XmlHelpers.getAttributeValue(this.entry, this.xPath, "./*/cda:code/@code", "");
        this.comments = XmlHelpers.getNodeText(this.entry, this.xPath, "./*/cda:text/cda:xml/cda:qdmUserComments/cda:item/text()", "");

        handlePreconditions(idGenerator);

        // If there are no measure observations, or there is a title, then there are no aggregations to extract
        String obsTest = XmlHelpers.getAttributeValue(this.entry, this.xPath, "./cda:measureObservationDefinition/@classCode", "");
        if (this.title.length() == 0 && obsTest.equals("OBS")) {
            this.title = XmlHelpers.getAttributeValue(this.entry, this.xPath, "../cda:code/cda:displayName/@value", "");
            this.aggregator = XmlHelpers.getAttributeValue(this.entry, this.xPath, "./cda:measureObservationDefinition/cda:methodCode/cda:item/@code", "");
        }
    }

    // specifically handles extracting the preconditions for the population criteria
    private void handlePreconditions(IdGenerator idGenerator) throws XPathExpressionException {
        // Nest multiple preconditions under a single root precondition
        this.preconditions = new ArrayList<Precondition>();
        NodeList preconditionNodes = (NodeList)this.xPath.evaluate("./*/cda:precondition[not(@nullFlavor)]", this.entry, XPathConstants.NODESET);
        for (int index = 0; index < preconditionNodes.getLength(); index++) {
            Precondition precondition = Precondition.parse(preconditionNodes.item(index), this.document, idGenerator);
            if (precondition == null || (precondition.getReference() == null && !precondition.hasPreconditions())) {
                // Do nothing
            }
            else {
                preconditions.add(precondition);
            }
        }
    }

    public void createHumanReadableId(String id) {
        setId(id);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public Node getEntry() {
        return entry;
    }

    public void setEntry(Node entry) {
        this.entry = entry;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getAggregator() {
        return aggregator;
    }

    public void setAggregator(String aggregator) {
        this.aggregator = aggregator;
    }

    public String getHqmfId() {
        return hqmfId;
    }

    public void setHqmfId(String hqmfId) {
        this.hqmfId = hqmfId;
    }

    public IdGenerator getIdGenerator() {
        return idGenerator;
    }

    public void setIdGenerator(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    public ArrayList<Precondition> getPreconditions() {
        return preconditions;
    }

    public void setPreconditions(ArrayList<Precondition> preconditions) {
        this.preconditions = preconditions;
    }
}
