package org.phema.executer.hqmf.models;

/**
 * Created by Luke Rasmussen on 9/21/17.
 */
public class Template {
    private String definition;
    private String status;
    private boolean negation;

    public Template() {}

    public Template(String definition, String status, boolean negation) {
        this.definition = definition;
        this.status = status;
        this.negation = negation;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isNegation() {
        return negation;
    }

    public void setNegation(boolean negation) {
        this.negation = negation;
    }
}
