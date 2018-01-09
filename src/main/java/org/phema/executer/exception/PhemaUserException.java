package org.phema.executer.exception;

import org.phema.executer.models.DescriptiveResult;

import java.util.ArrayList;

/**
 * Created by Luke Rasmussen on 12/22/17.
 *
 * This is a specialized exception which signals that it is acceptable
 * to show the error message to the user.  It will not contain confusing
 * or sensitive system details.
 */
public class PhemaUserException extends Exception {
    public PhemaUserException() { super(); }

    public PhemaUserException(String message) { super(message); }

    public PhemaUserException(String message, Exception innerException) { super(message, innerException); }

    public PhemaUserException(DescriptiveResult result) { super(String.join("\r\n", result.getDescriptions())); }

    public PhemaUserException(DescriptiveResult result, Exception innerException) { super(String.join("\r\n", result.getDescriptions()), innerException); }

    //public PhemaUserException(ArrayList<String> messages) { super(String.join("\r\n", messages)); }
}
