package org.phema.executer.hqmf.models;

/**
 * Created by Luke Rasmussen on 8/23/17.
 */
public class AnyValue {
    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public AnyValue(String type) {
        this.type = type;
    }

    public AnyValue() {
        setType("ANYNonNull");
    }
}
