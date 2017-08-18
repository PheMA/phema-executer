package org.phema.executer.cts2;

import org.phema.executer.UniversalNamespaceCache;
import org.phema.executer.cts2.models.ValueSet;
import org.phema.executer.interfaces.IHttpHelper;
import org.phema.executer.interfaces.IValueSetRepository;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Luke Rasmussen on 7/25/17.
 */
public class ValueSetRepository implements IValueSetRepository {
    public static class Parameters {
        public static String BaseUri = "BaseUri";
    }

    public URI getBaseUri() {
        return baseUri;
    }

    private URI baseUri = null;

    public IHttpHelper getHttpHelper() {
        return httpHelper;
    }

    private IHttpHelper httpHelper = null;

    public ValueSetRepository(IHttpHelper httpHelper) {
        this.httpHelper = httpHelper;
    }

    public void Initialize(HashMap<String, String> parameters) throws Exception {
        baseUri = new URI(parameters.get(Parameters.BaseUri));
    }

    public ArrayList<ValueSet> Search(String searchTerm) {
        try {
            URI searchUri = httpHelper.ConcatenateUri(baseUri, String.format("valuesets?matchvalue=%s", searchTerm));
            Document result = httpHelper.GetXml(searchUri);
            if (result == null) {
                return null;
            }

            XPath xPath = XPathFactory.newInstance().newXPath();
            NamespaceContext context = new UniversalNamespaceCache(result, true);
            xPath.setNamespaceContext(context);
            NodeList entries = (NodeList)xPath.evaluate("//entry", result.getDocumentElement(), XPathConstants.NODESET);

            ArrayList<ValueSet> valueSets = new ArrayList<ValueSet>();
            for (int index = 0; index < entries.getLength(); index++) {
                Node entry = entries.item(index);
                NamedNodeMap attributes = entry.getAttributes();
                ValueSet valueSet = new ValueSet(attributes.getNamedItem("valueSetName").getTextContent(), attributes.getNamedItem("formalName").getTextContent());
                valueSets.add(valueSet);
            }
            return valueSets;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public ValueSet GetByOID(String oid) {
        try {
            URI searchUri = httpHelper.ConcatenateUri(baseUri, String.format("valueset/%s", oid));
            Document result = httpHelper.GetXml(searchUri);
            if (result == null) {
                return null;
            }

            XPath xPath = XPathFactory.newInstance().newXPath();
            NamespaceContext context = new UniversalNamespaceCache(result, true);
            xPath.setNamespaceContext(context);

            // For now we're just checking if the particular node exists - if not, we assume it's "not found" and move on.
            // If need be in the future, an actual "not found" returns a response with a parent node of UnknownValueSet.
            Node entry = (Node)xPath.evaluate("//valueSetCatalogEntry", result.getDocumentElement(), XPathConstants.NODE);
            if (entry == null) {
                return null;
            }

            NamedNodeMap attributes = entry.getAttributes();
            ValueSet valueSet = new ValueSet(attributes.getNamedItem("valueSetName").getTextContent(), attributes.getNamedItem("formalName").getTextContent());
            return valueSet;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
