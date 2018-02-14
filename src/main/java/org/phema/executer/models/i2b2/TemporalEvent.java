package org.phema.executer.models.i2b2;

/**
 * Created by Luke Rasmussen on 2/14/18.
 */
public class TemporalEvent {
    private String id;
    private String timing;
    private String occurrence;

    public TemporalEvent() {
    }

    public TemporalEvent(String id, String timing, String occurrence) {
        this.id = id;
        this.timing = timing;
        this.occurrence = occurrence;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTiming() {
        return timing;
    }

    public void setTiming(String timing) {
        this.timing = timing;
    }

    public String getOccurrence() {
        return occurrence;
    }

    public void setOccurrence(String occurrence) {
        this.occurrence = occurrence;
    }
}
