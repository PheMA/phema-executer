package org.phema.executer.i2b2;

import org.apache.commons.lang.StringEscapeUtils;
import org.phema.executer.UniversalNamespaceCache;
import org.phema.executer.exception.PhemaUserException;
import org.phema.executer.interfaces.IHttpHelper;
import org.phema.executer.models.i2b2.Concept;
import org.phema.executer.models.i2b2.QueryMaster;
import org.phema.executer.util.XmlHelpers;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.net.URI;
import java.util.ArrayList;

/**
 * Created by Luke Rasmussen on 1/9/18.
 */
public class CRCService extends I2b2ServiceBase {
    private ProjectManagementService pmService = null;

    public CRCService(ProjectManagementService pmService, I2B2ExecutionConfiguration configuration, IHttpHelper httpHelper) {
        super(configuration, httpHelper);
        this.pmService = pmService;
    }

    public String createPanelXmlString(int panelNumber, boolean exclude, int itemOccurrence, ArrayList<Concept> concepts) {
        StringBuilder builder = new StringBuilder();
        builder.append("<panel>\n");
        builder.append(String.format("  <panel_number>%d</panel_number>\n", panelNumber));
        builder.append("  <panel_timing>ANY</panel_timing>\n");
        builder.append("  <panel_accuracy_scale>100</panel_accuracy_scale>\n");
        builder.append(String.format("  <invert>%d</invert>\n", (exclude ? 1 : 0)));
        builder.append(String.format("<total_item_occurrences>%d</total_item_occurrences>\n", itemOccurrence));
        for (Concept concept : concepts) {
            builder.append(createConceptPanelItemXmlString(concept));
        }
        builder.append("</panel>\n");
        return builder.toString().trim();
    }

    private String createConceptPanelItemXmlString(Concept concept) {
        StringBuilder builder = new StringBuilder();
        builder.append("  <item>\n");
        builder.append(String.format("    <hlevel>%d</hlevel>\n", concept.getHierarchyLevel()));
        builder.append(String.format("    <item_name>%s</item_name>\n", StringEscapeUtils.escapeXml(concept.getName())));
        builder.append(String.format("    <item_key>%s</item_key>\n", StringEscapeUtils.escapeXml(concept.getKey())));
        builder.append(String.format("    <item_icon>%s</item_icon>\n", concept.getVisualAttributes()));
        builder.append(String.format("    <tooltip>%s</tooltip>\n", StringEscapeUtils.escapeXml(concept.getTooltip())));
        builder.append(String.format("    <item_is_synonym>%s</item_is_synonym>\n", concept.isSynonym()));
        builder.append("  </item>\n");
        return builder.toString();
    }

    private String createQueryPanelItemXmlString(QueryMaster query) {
        StringBuilder builder = new StringBuilder();
        builder.append("  <item>\n");
        builder.append(String.format("    <item_key>masterid:%ld</item_key>\n", query.getId()));
        builder.append(String.format("    <item_name>(PrevQuery)%s</item_name>\n", query.getName()));
        builder.append(String.format("    <tooltip>%s</tooltip>\n", query.getName()));
        builder.append("    <item_is_synonym>false</item_is_synonym>\n");
        builder.append("    <hlevel>0</hlevel>\n");
        builder.append("  </item>\n");
        return builder.toString();
    }

    public QueryMaster runQueryInstance(String queryName, String panelXml) throws XPathExpressionException, PhemaUserException {
        loadRequest("i2b2_runQueryInstance");
        message = message.replace("{{domain}}", configuration.getI2b2Domain());
        message = message.replace("{{username}}", configuration.getI2b2Login());
        message = message.replace("{{token}}", pmService.getAuthenticationToken());
        message = message.replace("{{project}}", configuration.getI2b2Project());
        message = message.replace("{{query_name}}", queryName);
        message = message.replace("{{panels}}", panelXml);
        Document document = null;
        try {
            document = getMessage();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Document i2b2Result = null;
        try {
            i2b2Result = httpHelper.postXml(new URI(getProjectManagementService().getCellUrl("CRC") + "request"), document);
        } catch (Exception e) {
            //return new DescriptiveResult(false, "We were unable to attempt to log in to your i2b2 instance.  Please make sure that you have entered the correct Project Management URL, and that i2b2 is up and running.");
        }
        XPath xPath = XPathFactory.newInstance().newXPath();
        NamespaceContext context = new UniversalNamespaceCache(i2b2Result, true, "");
        xPath.setNamespaceContext(context);

        Element documentElement = i2b2Result.getDocumentElement();
        Element queryMasterElement = (Element)xPath.evaluate("//message_body/ns4:response/query_master", documentElement, XPathConstants.NODE);
        QueryMaster query = new QueryMaster(
                XmlHelpers.getChildContentAsInt(queryMasterElement, "query_master_id"),
                XmlHelpers.getChildContent(queryMasterElement, "name", "(Unknown)")
        );

        return query;
    }

    @Override
    public ProjectManagementService getProjectManagementService() {
        return pmService;
    }
}
