package org.phema.executer.util;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.phema.executer.interfaces.IHttpHelper;
import org.w3c.dom.Document;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by Luke Rasmussen on 7/25/17.
 */
public class HttpHelper implements IHttpHelper {
    public Document postXml(URI uri, Document message) throws Exception {
        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(uri);
        String messageText = XmlHelpers.documentToString(message);
        HttpEntity entity = new ByteArrayEntity(messageText.getBytes("UTF-8"),
                org.apache.http.entity.ContentType.create("application/xml"));
        post.setEntity(entity);
        HttpResponse response = client.execute(post);
        String result = EntityUtils.toString(response.getEntity());
        return XmlHelpers.loadXMLFromString(result);
    }

    public Document getXml(URI uri) throws Exception {
        HttpClient client = new DefaultHttpClient();
        HttpGet get = new HttpGet(uri);
        get.setHeader("Accept", "application/xml");
        get.setHeader("Content-Type", "application/xml");
        HttpResponse response = client.execute(get);
        String result = EntityUtils.toString(response.getEntity());
        return XmlHelpers.loadXMLFromString(result);
    }

    public URI concatenateUri(URI base, String extraPath) throws URISyntaxException {
        if (base == null) {
            return null;
        }

        String basePath = base.getPath();
        if (basePath.endsWith("/")) {
            basePath = basePath.substring(0, basePath.length() - 1);
        }
        String newPath = basePath + '/' + extraPath;
        URI newUri = base.resolve(newPath);
        return newUri;
    }
}
