package org.phema.executer.models.i2b2;

import java.util.ArrayList;

/**
 * Created by Luke Rasmussen on 2/14/18.
 */
public class TemporalDefinition {
    private ArrayList<TemporalRelationship> relationships;

    public TemporalDefinition() {
        this.relationships = new ArrayList<>();
    }

    public TemporalDefinition(ArrayList<TemporalRelationship> relationships) {
        this.relationships = relationships;
    }

    public ArrayList<TemporalRelationship> getRelationships() {
        return relationships;
    }

    public void setRelationships(ArrayList<TemporalRelationship> relationships) {
        this.relationships = relationships;
    }

}
