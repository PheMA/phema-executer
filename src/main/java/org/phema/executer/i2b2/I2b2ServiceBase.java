package org.phema.executer.i2b2;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;

/**
 * Created by Luke Rasmussen on 7/19/17.
 */
public class I2b2ServiceBase {
    protected String message = "";
    protected I2b2Configuration configuration = null;


    public I2b2ServiceBase(I2b2Configuration configuration) {
        this.configuration = configuration;
    }

    public static Document loadXMLFromString(String xml) throws Exception
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(xml));
        return builder.parse(is);
    }

    private String getFile(String fileName) {

        StringBuilder result = new StringBuilder("");

        //Get file from resources folder
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(fileName).getFile());

        try (Scanner scanner = new Scanner(file)) {

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                result.append(line).append("\n");
            }

            scanner.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return result.toString();

    }

    private Document postMessage(Document message) throws Exception {
        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(configuration.getI2b2ProjectManagementUrl());
        HttpEntity entity = new ByteArrayEntity(message.toString().getBytes("UTF-8"));
        post.setEntity(entity);
        HttpResponse response = client.execute(post);
        String result = EntityUtils.toString(response.getEntity());
        return loadXMLFromString(result);
    }

    public void loadRequest() {
        message = getFile("i2b2/i2b2_request.xml");
    }

    public Document getMessage() throws Exception {
        return loadXMLFromString(message);
    }
}
