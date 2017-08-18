package org.phema.executer.i2b2;

import org.phema.executer.interfaces.IHttpHelper;
import org.phema.executer.util.XmlHelpers;
import org.w3c.dom.Document;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

/**
 * Created by Luke Rasmussen on 7/19/17.
 */
public class I2b2ServiceBase {
    protected String message = "";
    protected I2B2ExecutionConfiguration configuration = null;
    protected IHttpHelper httpHelper = null;


    public I2b2ServiceBase(I2B2ExecutionConfiguration configuration, IHttpHelper httpHelper) {
        this.configuration = configuration;
        this.httpHelper = httpHelper;
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

    public void loadRequest(String messageName) {
        message = getFile("i2b2/" + messageName + ".xml");
    }

    public Document getMessage() throws Exception {
        return XmlHelpers.LoadXMLFromString(message);
    }
}
