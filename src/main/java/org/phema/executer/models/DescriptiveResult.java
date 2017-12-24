package org.phema.executer.models;

import java.util.ArrayList;

/**
 * Created by Luke Rasmussen on 7/19/17.
 */
public class DescriptiveResult {
    private boolean success;
    private ArrayList<String> descriptions;

    public DescriptiveResult() {
        setDescriptions(new ArrayList<>());
    }

    public DescriptiveResult(boolean result) {
        setSuccess(result);
        setDescriptions(new ArrayList<>());
    }

    public DescriptiveResult(boolean result, ArrayList<String> descriptions) {
        setSuccess(result);
        setDescriptions(descriptions);
    }

    public DescriptiveResult(boolean result, String description) {
        setSuccess(result);
        setDescriptions(new ArrayList<>());
        addDescription(description);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean result) {
        success = result;
    }

    public ArrayList<String> getDescriptions() {
        return descriptions;
    }

    public void setDescriptions(ArrayList<String> description) {
        descriptions = description;
    }

    public boolean addDescription(String description) {
        return this.descriptions.add(description);
    }
}
