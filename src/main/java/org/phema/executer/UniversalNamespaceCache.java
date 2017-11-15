package org.phema.executer;

import org.w3c.dom.*;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Luke Rasmussen on 7/21/17.
 * Code from: https://www.ibm.com/developerworks/library/x-nmspccontext/index.html
 */
public class UniversalNamespaceCache implements NamespaceContext {
    //private static final String DEFAULT_NS = "DEFAULT";
    private String defaultNamespace = "DEFAULT";
    private Map<String, String> prefix2Uri = new HashMap<String, String>();
    private Map<String, String> uri2Prefix = new HashMap<String, String>();

    /**
     * This constructor parses the document and stores all namespaces it can
     * find. If toplevelOnly is true, only namespaces in the root are used.
     *
     * @param document
     *            source document
     * @param toplevelOnly
     *            restriction of the search to enhance performance
     */
    public UniversalNamespaceCache(Document document, boolean toplevelOnly, String defaultNamespace) {
        this.defaultNamespace = defaultNamespace;
        examineNode(document.getDocumentElement(), toplevelOnly);
//        System.out.println("The list of the cached namespaces:");
//        for (String key : prefix2Uri.keySet()) {
//            System.out.println("prefix " + key + ": uri " + prefix2Uri.get(key));
//        }
    }

    /**
     * A single node is read, the namespace attributes are extracted and stored.
     *
     * @param node
     *            to examine
     * @param attributesOnly,
     *            if true no recursion happens
     */
    private void examineNode(Node node, boolean attributesOnly) {
        NamedNodeMap attributes = node.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            Node attribute = attributes.item(i);
            storeAttribute((Attr) attribute);
        }

        if (!attributesOnly) {
            NodeList chields = node.getChildNodes();
            for (int i = 0; i < chields.getLength(); i++) {
                Node chield = chields.item(i);
                if (chield.getNodeType() == Node.ELEMENT_NODE) {
                    examineNode(chield, false);
                }
            }
        }
    }

    /**
     * This method looks at an attribute and stores it, if it is a namespace
     * attribute.
     *
     * @param attribute
     *            to examine
     */
    private void storeAttribute(Attr attribute) {
        // examine the attributes in namespace xmlns
        if (attribute.getNodeName() != null
                && attribute.getNodeName().startsWith(XMLConstants.XMLNS_ATTRIBUTE)) {
            // Default namespace xmlns="uri goes here"
            if (attribute.getNodeName().equals(XMLConstants.XMLNS_ATTRIBUTE)) {
                putInCache(this.defaultNamespace, attribute.getNodeValue());
            } else {
                // The defined prefixes are stored here
                putInCache(attribute.getNodeName().replace(XMLConstants.XMLNS_ATTRIBUTE + ":", ""), attribute.getNodeValue());
            }
        }

    }

    private void putInCache(String prefix, String uri) {
        prefix2Uri.put(prefix, uri);
        uri2Prefix.put(uri, prefix);
    }

    /**
     * This method is called by XPath. It returns the default namespace, if the
     * prefix is null or "".
     *
     * @param prefix
     *            to search for
     * @return uri
     */
    public String getNamespaceURI(String prefix) {
        if (prefix == null || prefix.equals(XMLConstants.DEFAULT_NS_PREFIX)) {
            return prefix2Uri.get(this.defaultNamespace);
        } else {
            return prefix2Uri.get(prefix);
        }
    }

    /**
     * This method is not needed in this context, but can be implemented in a
     * similar way.
     */
    public String getPrefix(String namespaceURI) {
        return uri2Prefix.get(namespaceURI);
    }

    public Iterator getPrefixes(String namespaceURI) {
        // Not implemented
        return null;
    }

}