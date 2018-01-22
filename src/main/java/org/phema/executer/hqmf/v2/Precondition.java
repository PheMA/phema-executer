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
 * Created by Luke Rasmussen on 11/15/17.
 *
 * Represents the logic that defines grouping of criteria and actions done on it.
 */
public class Precondition {
    public static final String AT_LEAST_ONE_TRUE = "atLeastOneTrue";
    public static final String AT_LEAST_ONE_FALSE = "atLeastOneFalse";
    public static final String ALL_TRUE = "allTrue";
    public static final String ALL_FALSE = "allFalse";
    public static HashMap<String, String> NEGATIONS = new HashMap<String,String>() {{
        put(AT_LEAST_ONE_TRUE, ALL_FALSE);
        put(ALL_FALSE, AT_LEAST_ONE_TRUE);
        put(ALL_TRUE, AT_LEAST_ONE_FALSE);
        put(AT_LEAST_ONE_FALSE, ALL_TRUE);
    }};
    public static HashMap<String, String> INVERSIONS = new HashMap<String,String>() {{
        put(AT_LEAST_ONE_TRUE, ALL_TRUE);
        put(ALL_FALSE, AT_LEAST_ONE_FALSE);
        put(ALL_TRUE, AT_LEAST_ONE_TRUE);
        put(AT_LEAST_ONE_FALSE, ALL_FALSE);
    }};

    private ArrayList<Precondition> preconditions;
    private Reference reference;
    private String conjunction;
    private String id;
    private boolean negation;
    private Document document;

    public static Precondition parse(Node entry, Document doc, IdGenerator idGenerator) throws XPathExpressionException {
        XPath xPath = XmlHelpers.createXPath(entry.getOwnerDocument());
        Node aggregation = (Node)xPath.evaluate("./cda:allTrue | ./cda:atLeastOneTrue | ./cda:allFalse | ./cda:atLeastOneFalse",
                entry, XPathConstants.NODE);
        Node referenceDef = (Node)xPath.evaluate("./*/cda:id", entry, XPathConstants.NODE);
        if (referenceDef == null) {
            referenceDef = (Node)xPath.evaluate("./cda:join/cda:templateId/cda:item", entry, XPathConstants.NODE);
        }
        Reference reference = null;
        if (referenceDef != null) {
            reference = new Reference(referenceDef);
        }

        // Unless there is an aggregator, no further actions are necessary.
        if (aggregation == null) {
            return new Precondition(Integer.toString(idGenerator.nextId()), null, new ArrayList<Precondition>(), false, reference);
        }

        NodeList preconditionEntries = (NodeList)xPath.evaluate("./*/cda:precondition", entry, XPathConstants.NODESET);
        ArrayList<Precondition> preconditions = new ArrayList<>();
        for (int index = 0; index < preconditionEntries.getLength(); index++) {
            Precondition precondition = Precondition.parse(preconditionEntries.item(index), doc, idGenerator);
            // There are cases where a precondition may contain no references or preconditions, and should be ignored.
            if (precondition.getReference() != null || precondition.hasPreconditions()) {
                preconditions.add(precondition);
            }
        }

        return handleAggregation(idGenerator, reference, preconditions, aggregation);
    }

    // "False" aggregators exist, and require special handling, so this manages that and returns the
    // proper precondition.
    private static Precondition handleAggregation(IdGenerator idGenerator, Reference reference, ArrayList<Precondition> preconditions, Node aggregation) {
        boolean negation = false;
        String conjunction = aggregation.getNodeName();

        // # DeMorgan's law is used to handle negated case: e.g. to find if all are false, negate the "at least one true check.
        switch (conjunction) {
            case "allFalse":
                negation = true;
                conjunction = "atLeastOneTrue";
                break;
            case "atLeastOneFalse":
                negation = true;
                conjunction = "allTrue";
        }

        // Return the proper precondition given if a negation exists
        if (negation) {
            // Wrap the negation in a separate precondition which this will reference
            Precondition preconditionWrapper = new Precondition(Integer.toString(idGenerator.nextId()), conjunction, preconditions, true, reference);
            return new Precondition(Integer.toString(idGenerator.nextId()), conjunction, new ArrayList<Precondition>(){{ add(preconditionWrapper); }});
        }
        else {
            return new Precondition(Integer.toString(idGenerator.nextId()), conjunction, preconditions, false, reference);
        }
    }

    public Precondition(String id, String conjunction, ArrayList<Precondition> preconditions) {
        initialize(id, conjunction, preconditions, false, null);
    }

    public Precondition(String id, String conjunction, ArrayList<Precondition> preconditions, boolean negation, Reference reference) {
        initialize(id, conjunction, preconditions, negation, reference);
    }

    private void initialize(String id, String conjunction, ArrayList<Precondition> preconditions, boolean negation, Reference reference) {
        this.preconditions = (preconditions == null ? new ArrayList<Precondition>() : preconditions);
        this.conjunction = conjunction;
        this.reference = reference;
        this.negation = negation;
        this.id = id;
    }

    public boolean hasPreconditions() {
        return (this.preconditions != null && this.preconditions.size() > 0);
    }

    public ArrayList<Precondition> getPreconditions() {
        return preconditions;
    }

    public void setPreconditions(ArrayList<Precondition> preconditions) {
        this.preconditions = preconditions;
    }

    public Reference getReference() {
        return reference;
    }

    public void setReference(Reference reference) {
        this.reference = reference;
    }

    public String getConjunction() {
        return conjunction;
    }

    public void setConjunction(String conjunction) {
        this.conjunction = conjunction;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isNegation() {
        return negation;
    }

    public void setNegation(boolean negation) {
        this.negation = negation;
    }
}
