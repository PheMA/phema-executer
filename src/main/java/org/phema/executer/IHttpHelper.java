package org.phema.executer;

import org.w3c.dom.Document;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by Luke Rasmussen on 7/28/17.
 */
public interface IHttpHelper {
    Document PostXml(URI uri, Document message) throws Exception;
    Document GetXml(URI uri) throws Exception;
    URI ConcatenateUri(URI base, String extraPath) throws URISyntaxException;
}
