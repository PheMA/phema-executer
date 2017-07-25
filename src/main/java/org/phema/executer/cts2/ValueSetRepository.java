package org.phema.executer.cts2;

import org.phema.executer.IValueSetRepository;
import org.phema.executer.UniversalNamespaceCache;
import org.phema.executer.cts2.models.ValueSet;
import org.phema.executer.util.HttpHelpers;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Luke Rasmussen on 7/25/17.
 */
public class ValueSetRepository implements IValueSetRepository {
    public static class Parameters {
        public static String BaseUri = "BaseUri";
    }

    private URI baseUri = null;

    public void Initialize(HashMap<String, String> parameters) throws Exception {
        baseUri = new URI(parameters.get(Parameters.BaseUri));
    }

    public ArrayList<ValueSet> Search(String searchTerm) {
        try {
            URI searchUri = HttpHelpers.Concatenate(baseUri, String.format("valuesets?matchvalue=%s", searchTerm));
            Document result = HttpHelpers.GetXml(searchUri);
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
                ValueSet valueSet = new ValueSet(attributes.getNamedItem("formalName").getTextContent(), attributes.getNamedItem("valueSetName").getTextContent());
                valueSets.add(valueSet);
            }
            return valueSets;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
