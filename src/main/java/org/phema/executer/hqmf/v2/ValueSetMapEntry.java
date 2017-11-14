package org.phema.executer.hqmf.v2;

/**
 * Created by Luke Rasmussen on 9/21/17.
 */
public class ValueSetMapEntry {
    private String valuesetPath;
    private String resultPath;

    public ValueSetMapEntry(String valuesetPath, String resultPath) {
        this.valuesetPath = valuesetPath;
        this.resultPath = resultPath;
    }

    public String getValuesetPath() {
        return valuesetPath;
    }

    public void setValuesetPath(String valuesetPath) {
        this.valuesetPath = valuesetPath;
    }

    public String getResultPath() {
        return resultPath;
    }

    public void setResultPath(String resultPath) {
        this.resultPath = resultPath;
    }
}
