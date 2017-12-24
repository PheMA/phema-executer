package org.phema.executer.i2b2;

import org.phema.executer.UniversalNamespaceCache;
import org.phema.executer.interfaces.IHttpHelper;
import org.phema.executer.models.DescriptiveResult;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Luke Rasmussen on 7/17/17.
 */

public class ProjectManagementService extends I2b2ServiceBase {
    private String authenticationToken;

    public ProjectManagementService(I2B2ExecutionConfiguration configuration, IHttpHelper httpHelper) {
        super(configuration, httpHelper);
    }

    public DescriptiveResult login() {
        // If we are going to attempt a login, we want to ensure we have cleared out any
        // previous token (if one exists), so that it doesn't remain cached in the event
        // of an authentication error.
        setAuthenticationToken("");

        loadRequest("i2b2_login");
        message = message.replace("{{domain}}", configuration.getI2b2Domain());
        message = message.replace("{{username}}", configuration.getI2b2Login());
        message = message.replace("{{password}}", configuration.getI2b2Password());
        Document document = null;
        try {
            document = getMessage();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Document i2b2Result = null;
        try {
            i2b2Result = httpHelper.postXml(new URI(configuration.getI2b2ProjectManagementUrl().toString() + "getServices"), document);
        } catch (Exception e) {
            return new DescriptiveResult(false, "We were unable to attempt to log in to your i2b2 instance.  Please make sure that you have entered the correct Project Management URL, and that i2b2 is up and running.");
        }
        XPath xPath = XPathFactory.newInstance().newXPath();
        NamespaceContext context = new UniversalNamespaceCache(i2b2Result, true, "cda");
        xPath.setNamespaceContext(context);
        Node status = null;
        Node token = null;
        try {
            status = (Node)xPath.evaluate("//response_header/result_status/status", i2b2Result.getDocumentElement(), XPathConstants.NODE);
            token = (Node)xPath.evaluate("//message_body/configure/user/password", i2b2Result.getDocumentElement(), XPathConstants.NODE);
        } catch (XPathExpressionException e) {
            return new DescriptiveResult(false, "There was an unexpected error when trying to get the result of your login attempt against i2b2.");
        }

        String tokenValue = "";
        if (status != null && token != null
                && status.getAttributes().getNamedItem("type").getTextContent().equals("DONE")
                && token.getAttributes().getNamedItem("is_token").getTextContent().equals("true")) {
            tokenValue = token.getTextContent();
        }
        else {
            return new DescriptiveResult(false, "Failed to authenticate against i2b2");
        }

        Node project = null;
        try {
            project = (Node)xPath.evaluate("//message_body/configure/user/project[@id='" + configuration.getI2b2Project() + "']",
                    i2b2Result.getDocumentElement(), XPathConstants.NODE);
        } catch (XPathExpressionException e) {
            return new DescriptiveResult(false, "There was an unexpected error when trying to get the list of projects your i2b2 user has access to.");
        }

        if (project == null) {
            NodeList projects = null;
            try {
                projects = (NodeList)xPath.evaluate("//message_body/configure/user/project",
                        i2b2Result.getDocumentElement(), XPathConstants.NODESET);
            } catch (XPathExpressionException e) {
                return new DescriptiveResult(false, "There was an unexpected error when trying to get the list of projects your i2b2 user has access to.");
            }

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

            return new DescriptiveResult(false, errorBuilder.toString());
        }

        setAuthenticationToken(tokenValue);
        return new DescriptiveResult(true);
    }

    public String getAuthenticationToken() {
        return authenticationToken;
    }

    public void setAuthenticationToken(String authenticationToken) {
        this.authenticationToken = authenticationToken;
    }
}
