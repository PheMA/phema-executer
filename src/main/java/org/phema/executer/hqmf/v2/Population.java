package org.phema.executer.hqmf.v2;

import java.util.HashMap;

/**
 * Created by Luke Rasmussen on 11/29/17.
 */
public class Population {
    private String id;
    private String title;
    private String observation;

    // The original Ruby implementation was a Hash which allowed creating keys.  Instead of pre-defining all possible
    // keys as fields (some of which are dynamic), we'll implement this catch-all collection
    private HashMap<String, String> additionalData;

    public Population() {
    }

    public Population(Population population) {
        setId(population.getId());
        setTitle(population.getTitle());
        setObservation(population.getObservation());
    }

    public void setAdditionalKey(String key, String value) {
        if (additionalData == null) {
            additionalData = new HashMap<String, String>();
        }

        additionalData.put(key, value);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getObservation() {
        return observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }

}
