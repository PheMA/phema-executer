package org.phema.executer.i2b2;

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

    public OntologyService(ProjectManagementService pmService, I2B2ExecutionConfiguration configuration, IHttpHelper httpHelper) {
        super(configuration, httpHelper);
        this.pmService = pmService;
    }

    public ArrayList<Concept> getCodeInfo(String code) throws PhemaUserException {
        loadRequest("i2b2_getCodeInfo");
        message = message.replace("{{domain}}", configuration.getI2b2Domain());
        message = message.replace("{{username}}", configuration.getI2b2Login());
        message = message.replace("{{token}}", pmService.getAuthenticationToken());
        message = message.replace("{{project}}", configuration.getI2b2Project());
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
            //return new DescriptiveResult(false, "We were unable to attempt to log in to your i2b2 instance.  Please make sure that you have entered the correct Project Management URL, and that i2b2 is up and running.");
        }
        XPath xPath = XPathFactory.newInstance().newXPath();
        NamespaceContext context = new UniversalNamespaceCache(i2b2Result, true, "");
        xPath.setNamespaceContext(context);

        Element documentElement = i2b2Result.getDocumentElement();
        HashMap<String, Concept> concepts = new HashMap<>();
        NodeList conceptNodes = null;
        try {
            conceptNodes = (NodeList)xPath.evaluate("//message_body/ns6:concepts/concept", documentElement, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            throw new PhemaUserException("There was an unexpected error when trying to find the list of i2b2 concepts", e);
        }

        for (int index = 0; index < conceptNodes.getLength(); index++) {
            Element conceptElement = (Element)conceptNodes.item(index);
            String key = XmlHelpers.getChildContent(conceptElement, "key", "");
            if (!concepts.containsKey(key)) {
                concepts.put(key, new Concept(key,
                        XmlHelpers.getChildContent(conceptElement, "name", ""),
                        XmlHelpers.getChildContent(conceptElement, "basecode", ""),
                        XmlHelpers.getChildContentAsInt(conceptElement, "level"),
                        XmlHelpers.getChildContent(conceptElement, "tooltip", ""),
                        XmlHelpers.getChildContent(conceptElement, "synonym_cd", "false"),
                        XmlHelpers.getChildContent(conceptElement, "visualattributes", "")));
            }
        }

        return new ArrayList<>(concepts.values());
    }

    @Override
    public ProjectManagementService getProjectManagementService() {
        return pmService;
    }

}
