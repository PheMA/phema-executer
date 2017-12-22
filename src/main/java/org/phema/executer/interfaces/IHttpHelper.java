package org.phema.executer.interfaces;

import org.w3c.dom.Document;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by Luke Rasmussen on 7/28/17.
 */
public interface IHttpHelper {
    Document postXml(URI uri, Document message) throws Exception;
    Document getXml(URI uri) throws Exception;
    URI concatenateUri(URI base, String extraPath) throws URISyntaxException;
}
