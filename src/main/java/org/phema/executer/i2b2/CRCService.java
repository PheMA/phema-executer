package org.phema.executer.i2b2;

import org.apache.commons.lang.StringEscapeUtils;
import org.phema.executer.UniversalNamespaceCache;
import org.phema.executer.exception.PhemaUserException;
import org.phema.executer.interfaces.IHttpHelper;
import org.phema.executer.models.DescriptiveResult;
import org.phema.executer.models.i2b2.Concept;
import org.phema.executer.models.i2b2.QueryMaster;
import org.phema.executer.models.i2b2.TemporalDefinition;
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
import java.net.URISyntaxException;
import java.util.ArrayList;

/**
 * Created by Luke Rasmussen on 1/9/18.
 */
public class CRCService extends I2b2ServiceBase {
    public static final String QUEUED = "QUEUED";
    public static final String ERROR = "ERROR";
    public static final String FINISHED = "FINISHED";
    public static final String PROCESSING = "PROCESSING";
    public static final String SMALL_QUEUE = "SMALL_QUEUE";
    public static final String MEDIUM_QUEUE = "MEDIUM_QUEUE";
    public static final String LARGE_QUEUE = "LARGE_QUEUE";
    public static final String MEDIUM_QUEUE_RUNNING = "MEDIUM_QUEUE_RUNNING";
    public static final String LARGE_QUEUE_RUNNING = "LARGE_QUEUE_RUNNING";
    public static final String RUNNING = "RUNNING";
    public final static String QUERY_STATUS_PARAM = "QUERY_STATUS_PARAM";
    public final static String QT_QUERY_RESULT_INSTANCE_ID_PARAM = "QT_QUERY_RESULT_INSTANCE_ID_PARAM";

    private ProjectManagementService pmService;

    public CRCService(ProjectManagementService pmService, I2B2ExecutionConfiguration configuration, IHttpHelper httpHelper) {
        super(configuration, httpHelper);
        this.pmService = pmService;
    }

    public String createConceptPanelXmlString(int panelStartNumber, boolean isAnd, boolean exclude, int itemOccurrence, ArrayList<Concept> concepts) {
        StringBuilder builder = new StringBuilder();
        if (isAnd) {
            int panelCounter = panelStartNumber;
            for (Concept concept : concepts) {
                builder.append("<panel>\n");
                builder.append(String.format("  <panel_number>%d</panel_number>\n", panelCounter));
                builder.append("  <panel_timing>ANY</panel_timing>\n");
                builder.append("  <panel_accuracy_scale>100</panel_accuracy_scale>\n");
                builder.append(String.format("  <invert>%d</invert>\n", (exclude ? 1 : 0)));
                builder.append(String.format("  <total_item_occurrences>%d</total_item_occurrences>\n", itemOccurrence));
                builder.append(createConceptPanelItemXmlString(concept));
                builder.append("</panel>\n");
                panelCounter++;
            }
        }
        else {
            builder.append("<panel>\n");
            builder.append(String.format("  <panel_number>%d</panel_number>\n", panelStartNumber));
            builder.append("  <panel_timing>ANY</panel_timing>\n");
            builder.append("  <panel_accuracy_scale>100</panel_accuracy_scale>\n");
            builder.append(String.format("  <invert>%d</invert>\n", (exclude ? 1 : 0)));
            builder.append(String.format("  <total_item_occurrences>%d</total_item_occurrences>\n", itemOccurrence));
            for (Concept concept : concepts) {
                builder.append(createConceptPanelItemXmlString(concept));
            }
            builder.append("</panel>\n");
        }
        return builder.toString().trim();
    }

    public String createQueryPanelXmlString(int panelStartNumber, boolean isAnd, boolean exclude, int itemOccurrence, ArrayList<QueryMaster> queries) {
        StringBuilder builder = new StringBuilder();
        if (isAnd) {
            int panelCounter = panelStartNumber;
            for (QueryMaster query : queries) {
                builder.append("<panel>\n");
                builder.append(String.format("  <panel_number>%d</panel_number>\n", panelCounter));
                builder.append("  <panel_timing>ANY</panel_timing>\n");
                builder.append("  <panel_accuracy_scale>100</panel_accuracy_scale>\n");
                builder.append(String.format("  <invert>%d</invert>\n", (exclude ? 1 : 0)));
                builder.append(String.format("<total_item_occurrences>%d</total_item_occurrences>\n", itemOccurrence));
                builder.append(createQueryPanelItemXmlString(query));
                builder.append("</panel>\n");
                panelCounter++;
            }
        }
        else {
            builder.append("<panel>\n");
            builder.append(String.format("  <panel_number>%d</panel_number>\n", panelStartNumber));
            builder.append("  <panel_timing>ANY</panel_timing>\n");
            builder.append("  <panel_accuracy_scale>100</panel_accuracy_scale>\n");
            builder.append(String.format("  <invert>%d</invert>\n", (exclude ? 1 : 0)));
            builder.append(String.format("<total_item_occurrences>%d</total_item_occurrences>\n", itemOccurrence));
            for (QueryMaster query : queries) {
                builder.append(createQueryPanelItemXmlString(query));
            }
            builder.append("</panel>\n");
        }
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
        builder.append(String.format("    <item_key>masterid:%d</item_key>\n", query.getId()));
        builder.append(String.format("    <item_name>(PrevQuery)%s</item_name>\n", query.getName()));
        builder.append(String.format("    <tooltip>%s</tooltip>\n", query.getName()));
        builder.append("    <item_is_synonym>false</item_is_synonym>\n");
        builder.append("    <hlevel>0</hlevel>\n");
        builder.append("  </item>\n");
        return builder.toString();
    }

    private String replaceCommonMessagePlaceholders(String message) {
        message = message.replace("{{domain}}", configuration.getI2b2Domain());
        message = message.replace("{{username}}", configuration.getI2b2Login());
        message = message.replace("{{token}}", pmService.getAuthenticationToken());
        message = message.replace("{{project}}", configuration.getI2b2Project());
        return message;
    }

    public QueryMaster runQueryInstance(String queryName, String panelXml, boolean returnResults) throws Exception {
        loadRequest("i2b2_runQueryInstance");
        message = replaceCommonMessagePlaceholders(message);
        message = message.replace("{{query_name}}", queryName);
        message = message.replace("{{panels}}", panelXml);
        message = message.replace("{{result_type}}", (returnResults ? "<result_output priority_index=\"10\" name=\"patient_count_xml\"/>" : ""));
        Document document = getMessage();
        Document i2b2Result = httpHelper.postXml(new URI(getProjectManagementService().getCellUrl("CRC") + "request"), document);
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

    private boolean isStatusFailure(Element documentElement, XPath xPath) throws XPathExpressionException {
        String status = XmlHelpers.getAttributeValue((Node)documentElement, xPath, "//message_body/ns4:response/status/condition/@condition", "ERROR");
        return status.equalsIgnoreCase("DONE");
    }

    /**
     * Periodically check the i2b2 server to see if our query definition has completed running, if it's still
     * running, or if it encountered some type of error.  This will only return once some type of stoppping
     * condition has been reached.
     * @param masterQuery The query definition that is to be monitored.
     */
    public DescriptiveResult pollForQueryCompletion(QueryMaster masterQuery) throws Exception {
        loadRequest("i2b2_getQueryStatus");
        message = replaceCommonMessagePlaceholders(message);
        message = message.replace("{{query_master_id}}", Integer.toString(masterQuery.getId()));

        Document document = getMessage();
        while(true) {
            Document i2b2Result = httpHelper.postXml(new URI(getProjectManagementService().getCellUrl("CRC") + "request"), document);
            XPath xPath = XPathFactory.newInstance().newXPath();
            NamespaceContext context = new UniversalNamespaceCache(i2b2Result, true, "");
            xPath.setNamespaceContext(context);

            Element documentElement = i2b2Result.getDocumentElement();
            if (isStatusFailure(documentElement, xPath)) {
                return new DescriptiveResult(false, "i2b2 reported an error when trying to run your phenotype definition.");
            }

            Element queryInstanceElement = (Element) xPath.evaluate("//message_body/ns4:response/query_instance", documentElement, XPathConstants.NODE);
            String batchModeResult = XmlHelpers.getChildContent(queryInstanceElement, "batch_mode", "ERROR");
            switch (batchModeResult) {
                case ERROR:
                    return new DescriptiveResult(false, "i2b2 reported an error when trying to run your phenotype definition.");
                case FINISHED:
                    return new DescriptiveResult(true, "The i2b2 query has completed running");
                default:
                    // Assume it's still running
                    break;
            }

            Thread.sleep(10000);
        }
    }


    public String createTemporalQueryXmlString(QueryMaster event1, QueryMaster event2, TemporalDefinition temporalDefinition) {
        String template = 
                "<subquery_constraint>\n" +
                "    <first_query>\n" +
                "      <query_id>" + temporalDefinition.getEvent1().getId() + "</query_id>\n" +
                "      <join_column>" + temporalDefinition.getEvent1().getTiming() + "</join_column>\n" +
                "      <aggregate_operator>" + temporalDefinition.getEvent1().getOccurrence() + "</aggregate_operator>\n" +
                "    </first_query>\n" +
                "    <operator>LESS</operator>\n" +
                "    <second_query>\n" +
                "      <query_id>" + temporalDefinition.getEvent2().getId() + "</query_id>\n" +
                "      <join_column>" + temporalDefinition.getEvent2().getTiming() + "</join_column>\n" +
                "      <aggregate_operator>" + temporalDefinition.getEvent2().getOccurrence() + "</aggregate_operator>\n" +
                "    </second_query>\n" +
                "    <span>\n" +
                "      <operator>" + temporalDefinition.getOperator() + "</operator>\n" +
                "      <span_value>" + temporalDefinition.getValue() + "</span_value>\n" +
                "      <units>" + temporalDefinition.getUnits() + "</units>\n" +
                "    </span>\n" +
                "  </subquery_constraint>\n" +
                "<subquery>\n" +
                "    <query_id>Event 1</query_id>\n" +
                "    <query_type>EVENT</query_type>\n" +
                "    <query_name>Event 1</query_name>\n" +
                "    <query_timing>SAMEINSTANCENUM</query_timing>\n" +
                "    <specificity_scale>0</specificity_scale>\n" +
                    createQueryPanelXmlString(1, true, false, 1, new ArrayList<QueryMaster>() {{ add(event1); }}) +
                "</subquery>\n" +
                "<subquery>\n" +
                "    <query_id>Event 2</query_id>\n" +
                "    <query_type>EVENT</query_type>\n" +
                "    <query_name>Event 2</query_name>\n" +
                "    <query_timing>SAMEINSTANCENUM</query_timing>\n" +
                "    <specificity_scale>0</specificity_scale>\n" +
                    createQueryPanelXmlString(1, true, false, 1, new ArrayList<QueryMaster>() {{ add(event2); }}) +
                "</subquery>";

        return template;
    }

    @Override
    public ProjectManagementService getProjectManagementService() {
        return pmService;
    }
}
