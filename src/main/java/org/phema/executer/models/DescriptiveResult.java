package org.phema.executer.models;

import java.util.ArrayList;

/**
 * Created by Luke Rasmussen on 7/19/17.
 */
public class DescriptiveResult {
    private boolean Result;
    private ArrayList<String> Description;

    public DescriptiveResult() {
        setDescription(new ArrayList<String>());
    }

    public DescriptiveResult(boolean result) {
        setResult(result);
        setDescription(new ArrayList<String>());
    }

    public DescriptiveResult(boolean result, ArrayList<String> description) {
        setResult(result);
        setDescription(description);
    }

    public boolean isResult() {
        return Result;
    }

    public void setResult(boolean result) {
        Result = result;
    }

    public ArrayList<String> getDescription() {
        return Description;
    }

    public void setDescription(ArrayList<String> description) {
        Description = description;
    }

    public boolean addDescription(String description) {
        return Description.add(description);
    }
}
