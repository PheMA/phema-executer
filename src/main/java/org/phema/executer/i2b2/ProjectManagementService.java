package org.phema.executer.i2b2;

import org.phema.executer.UniversalNamespaceCache;
import org.phema.executer.interfaces.IHttpHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Luke Rasmussen on 7/17/17.
 */

public class ProjectManagementService extends I2b2ServiceBase {
    public ProjectManagementService(I2B2ExecutionConfiguration configuration, IHttpHelper httpHelper) {
        super(configuration, httpHelper);
    }

    public String login() throws Exception {
        loadRequest("i2b2_login");
        message = message.replace("{{domain}}", configuration.getI2b2Domain());
        message = message.replace("{{username}}", configuration.getI2b2Login());
        message = message.replace("{{password}}", configuration.getI2b2Password());
        Document document = getMessage();

        Document result = httpHelper.PostXml(new URI(configuration.getI2b2ProjectManagementUrl().toString() + "getServices"), document);
        XPath xPath = XPathFactory.newInstance().newXPath();
        NamespaceContext context = new UniversalNamespaceCache(result, true);
        xPath.setNamespaceContext(context);
        Node status = (Node)xPath.evaluate("//response_header/result_status/status", result.getDocumentElement(), XPathConstants.NODE);
        Node token = (Node)xPath.evaluate("//message_body/configure/user/password", result.getDocumentElement(), XPathConstants.NODE);
        String tokenValue = "";
        if (status != null && token != null
                && status.getAttributes().getNamedItem("type").getTextContent().equals("DONE")
                && token.getAttributes().getNamedItem("is_token").getTextContent().equals("true")) {
            tokenValue = token.getTextContent();
        }
        else {
            throw new Exception("Failed to authenticate against i2b2");
        }

        Node project = (Node)xPath.evaluate("//message_body/configure/user/project[@id='" + configuration.getI2b2Project() + "']",
                result.getDocumentElement(), XPathConstants.NODE);
        if (project == null) {
            NodeList projects = (NodeList)xPath.evaluate("//message_body/configure/user/project",
                    result.getDocumentElement(), XPathConstants.NODESET);
            StringBuilder errorBuilder = new StringBuilder();
            errorBuilder.append("You don't have access to the project '");
            errorBuilder.append(configuration.getI2b2Project());
            errorBuilder.append("'");
            if (projects != null && projects.getLength() > 0) {
                List<String> projectList = new ArrayList<>();
                for (int index = 0; index < projects.getLength(); index++) {
                    projectList.add(projects.item(index).getAttributes().getNamedItem("id").getNodeValue());
                }
                errorBuilder.append("\r\nYou do have access to the following project(s): ");
                errorBuilder.append(String.join(", ", projectList));
            }
            throw new Exception(errorBuilder.toString());
        }

        return tokenValue;
    }
}
