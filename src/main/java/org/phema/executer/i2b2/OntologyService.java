package org.phema.executer.i2b2;

import org.phema.executer.DebugLogger;
import org.phema.executer.UniversalNamespaceCache;
import org.phema.executer.exception.PhemaUserException;
import org.phema.executer.interfaces.IHttpHelper;
import org.phema.executer.models.DescriptiveResult;
import org.phema.executer.models.i2b2.Concept;
import org.phema.executer.util.XmlHelpers;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Luke Rasmussen on 1/3/18.
 */
public class OntologyService extends I2b2ServiceBase {
    private ProjectManagementService pmService = null;

    public OntologyService(ProjectManagementService pmService, I2B2ExecutionConfiguration configuration, IHttpHelper httpHelper, DebugLogger debugLogger) {
        super(configuration, httpHelper, debugLogger);
        this.pmService = pmService;
    }

    public ArrayList<Concept> getCodeInfo(String code) throws PhemaUserException {
        debugMessage(String.format("Searching for i2b2 code '%s'", code));
        prepareRequest("i2b2_getCodeInfo");
        message = message.replace("{{code}}", code);

        Document document = null;
        try {
            document = getMessage();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Document i2b2Result = null;
        try {
            i2b2Result = httpHelper.postXml(new URI(getProjectManagementService().getCellUrl("ONT") + "getCodeInfo"), document);
        } catch (Exception e) {
            throw new PhemaUserException("There was an error when trying to search for an i2b2 ontology code", e);
        }
        XPath xPath = XPathFactory.newInstance().newXPath();
        NamespaceContext context = new UniversalNamespaceCache(i2b2Result, true, "");
        xPath.setNamespaceContext(context);

        Element documentElement = i2b2Result.getDocumentElement();
        return convertConceptXmlToObjects(xPath, documentElement);
    }

    public ArrayList<Concept> getCategories() throws PhemaUserException {
        debugMessage(String.format("Retrieving all i2b2 categories"));
        prepareRequest("i2b2_getCategories");
        Document document = null;
        try {
            document = getMessage();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Document i2b2Result = null;
        try {
            i2b2Result = httpHelper.postXml(new URI(getProjectManagementService().getCellUrl("ONT") + "getCategories"), document);
        } catch (Exception e) {
            throw new PhemaUserException("There was an error when trying to get the list of i2b2 ontology categories", e);
        }
        XPath xPath = XPathFactory.newInstance().newXPath();
        NamespaceContext context = new UniversalNamespaceCache(i2b2Result, true, "");
        xPath.setNamespaceContext(context);

        Element documentElement = i2b2Result.getDocumentElement();
        return convertConceptXmlToObjects(xPath, documentElement);
    }

    private void prepareRequest(String messageName) {
        loadRequest(messageName);
        message = message.replace("{{domain}}", configuration.getI2b2Domain());
        message = message.replace("{{username}}", configuration.getI2b2Login());
        message = message.replace("{{token}}", pmService.getAuthenticationToken());
        message = message.replace("{{project}}", configuration.getI2b2Project());
    }

    private ArrayList<Concept> convertConceptXmlToObjects(XPath xPath, Element documentElement) throws PhemaUserException {
        HashMap<String, Concept> concepts = new HashMap<>();
        NodeList conceptNodes = null;
        try {
            conceptNodes = (NodeList) xPath.evaluate("//message_body/ns6:concepts/concept", documentElement, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            throw new PhemaUserException("There was an unexpected error when trying to find the list of i2b2 concepts", e);
        }

        debugMessage("List of i2b2 results");
        debugData("\"key\",\"name\",\"basecode\",\"level\",\"tooltip\",\"synonym_cd\",\"visualattributes\",\"phema_excluded\"");
        for (int index = 0; index < conceptNodes.getLength(); index++) {
            Element conceptElement = (Element) conceptNodes.item(index);
            String key = XmlHelpers.getChildContent(conceptElement, "key", "");
            if (!concepts.containsKey(key)) {
                Concept concept = createConceptFromXml(conceptElement);
                concepts.put(key, concept);
                debugLogConcept(concept, false);
            }
            else if (debugLogger != null) {
                // If logging is enabled, write out this concept entry with a flag to show that it's being excluded by PhEMA.
                Concept concept = createConceptFromXml(conceptElement);
                debugLogConcept(concept, true);
            }
        }
        debugData("");

        return new ArrayList<>(concepts.values());
    }

    private Concept createConceptFromXml(Element conceptElement) throws PhemaUserException {
        Concept concept = new Concept(XmlHelpers.getChildContent(conceptElement, "key", ""),
                XmlHelpers.getChildContent(conceptElement, "name", ""),
                XmlHelpers.getChildContent(conceptElement, "basecode", ""),
                XmlHelpers.getChildContentAsInt(conceptElement, "level"),
                XmlHelpers.getChildContent(conceptElement, "tooltip", ""),
                XmlHelpers.getChildContent(conceptElement, "synonym_cd", "false"),
                XmlHelpers.getChildContent(conceptElement, "visualattributes", ""));
        return concept;
    }

    private void debugLogConcept(Concept concept, boolean isExcluded) {
        debugData(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"",
                concept.getKey(), concept.getName(), concept.getBaseCode(), concept.getHierarchyLevel(),
                concept.getTooltip(), concept.isSynonym(), concept.getVisualAttributes(), isExcluded));
    }

    @Override
    public ProjectManagementService getProjectManagementService() {
        return pmService;
    }

}
