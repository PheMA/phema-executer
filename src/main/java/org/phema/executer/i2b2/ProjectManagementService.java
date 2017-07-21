package org.phema.executer.i2b2;

import org.phema.executer.UniversalNamespaceCache;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.net.URI;

/**
 * Created by Luke Rasmussen on 7/17/17.
 */

public class ProjectManagementService extends I2b2ServiceBase {
    public ProjectManagementService(I2b2Configuration configuration) {
        super(configuration);
    }

    public String login() throws Exception {
        loadRequest("i2b2_login");
        message = message.replace("{{domain}}", configuration.getI2b2Domain());
        message = message.replace("{{username}}", configuration.getI2b2Login());
        message = message.replace("{{password}}", configuration.getI2b2Password());
        Document document = getMessage();
//        StringBuilder builder = new StringBuilder();
//        builder.append(
//                "        <pm:get_user_configuration>\n" +
//                "            <project>undefined</project>\n" +
//                "        </pm:get_user_configuration>");
//
//        loadRequest("i2b2_login");
//        message = message.replace("<message_body/>", builder.toString());
//        Document document = getMessage();
//        //Evaluate XPath against Document itself
//        XPath xPath = XPathFactory.newInstance().newXPath();
//        Node domainNode = (Node)xPath.evaluate("//message_header/security/domain",
//                document.getDocumentElement(), XPathConstants.NODE);
//        domainNode.setTextContent(configuration.getI2b2Domain());
//        Node usernameNode = (Node)xPath.evaluate("//message_header/security/username",
//                document.getDocumentElement(), XPathConstants.NODE);
//        usernameNode.setTextContent(configuration.getI2b2Login());
//        Node passwordNode = (Node)xPath.evaluate("//message_header/security/password",
//                document.getDocumentElement(), XPathConstants.NODE);
//        passwordNode.setTextContent(configuration.getI2b2Password());
        Document result = postMessage(new URI(configuration.getI2b2ProjectManagementUrl().toString() + "getServices"), document);
        XPath xPath = XPathFactory.newInstance().newXPath();
        NamespaceContext context = new UniversalNamespaceCache(result, true);
        xPath.setNamespaceContext(context);
        Node status = (Node)xPath.evaluate("//response_header/result_status/status", result.getDocumentElement(), XPathConstants.NODE);
        Node token = (Node)xPath.evaluate("//message_body/configure/user/password", result.getDocumentElement(), XPathConstants.NODE);
        if (status != null && token != null
                && status.getAttributes().getNamedItem("type").getTextContent().equals("DONE")
                && token.getAttributes().getNamedItem("is_token").getTextContent().equals("true")) {
            return token.getTextContent();
        }
        else {
            throw new Exception("Failed to authenticate against i2b2");
        }
    }
}
