package org.phema.executer.util;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.phema.executer.interfaces.IHttpHelper;
import org.w3c.dom.Document;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by Luke Rasmussen on 7/25/17.
 */
public class HttpHelper implements IHttpHelper {
    private boolean trustAllSsl;

    public HttpHelper(boolean trustAllSsl) {
        this.trustAllSsl = trustAllSsl;
    }

    public Document postXml(URI uri, Document message) throws Exception {
        HttpClient client = createHttpClient();
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
        HttpClient client = createHttpClient();
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

    private HttpClient createHttpClient() throws Exception {
        HttpClient client = null;
        if (trustAllSsl) {
            SSLContextBuilder builder = SSLContexts.custom();
            builder.loadTrustMaterial(null, (chain, authType) -> true);
            SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(builder.build());
            client = HttpClients.custom().setSSLSocketFactory(sslSocketFactory).build();
        }
        else {
            client = HttpClients.createDefault();
        }

        return client;
    }
}
