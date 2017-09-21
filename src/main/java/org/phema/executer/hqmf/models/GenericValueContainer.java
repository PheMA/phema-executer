package org.phema.executer.hqmf.models;

/**
 * Created by Luke Rasmussen on 8/23/17.
 */
public class GenericValueContainer {
    private String type;
    private String value;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public GenericValueContainer(String type, String value) {
        this.type = type;
        this.value = value;
    }
}
