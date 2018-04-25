package org.phema.executer.util;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.phema.executer.interfaces.IHttpHelper;
import org.w3c.dom.Document;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.*;

/**
 * Created by Luke Rasmussen on 7/25/17.
 */
public class HttpHelper implements IHttpHelper {
    public Document postXml(URI uri, Document message) throws Exception {
        SSLContextBuilder builder = SSLContexts.custom();
        builder.loadTrustMaterial(null, new TrustStrategy() {
            @Override
            public boolean isTrusted(X509Certificate[] chain, String authType)
                    throws CertificateException {
                return true;
            }
        });
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                builder.build());
        HttpClient client = HttpClients.custom().setSSLSocketFactory(
                sslsf).build();


        //HttpClient client = new DefaultHttpClient();
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

    // Taken from https://stackoverflow.com/a/22524546/5670646
    // trusting all certificate
    public void doTrustToCertificates() throws Exception {
        Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) throws CertificateException {
                        return;
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType) throws CertificateException {
                        return;
                    }
                }
        };

        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        HostnameVerifier hv = new HostnameVerifier() {
            public boolean verify(String urlHostName, SSLSession session) {
                if (!urlHostName.equalsIgnoreCase(session.getPeerHost())) {
                    System.out.println("Warning: URL host '" + urlHostName + "' is different to SSLSession host '" + session.getPeerHost() + "'.");
                }
                return true;
            }
        };
        HttpsURLConnection.setDefaultHostnameVerifier(hv);
    }
}
