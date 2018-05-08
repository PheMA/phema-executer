package org.phema.executer.i2b2;

import org.phema.executer.interfaces.IHttpHelper;
import org.phema.executer.util.FileHelper;
import org.phema.executer.util.XmlHelpers;
import org.w3c.dom.Document;

import java.io.File;
import java.io.IOException;
import java.util.Observable;
import java.util.Scanner;

/**
 * Created by Luke Rasmussen on 7/19/17.
 */
public abstract class I2b2ServiceBase extends Observable {
    protected String message = "";
    protected I2B2ExecutionConfiguration configuration = null;
    protected IHttpHelper httpHelper = null;


    public I2b2ServiceBase(I2B2ExecutionConfiguration configuration, IHttpHelper httpHelper) {
        this.configuration = configuration;
        this.httpHelper = httpHelper;
    }

    public void loadRequest(String messageName) {
        message = FileHelper.getFileFromResource("i2b2/" + messageName + ".xml");
    }

    public Document getMessage() throws Exception {
        return XmlHelpers.loadXMLFromString(message);
    }

    /**
     * Trigger an update to all Observers that progress has been made.
     * @param progress String describing the progress/message that will be sent to the Observer
     */
    protected void updateProgress(String progress) {
        this.setChanged();
        this.notifyObservers(progress);
    }

    public abstract ProjectManagementService getProjectManagementService();
}
