package org.phema.executer.models.i2b2;

/**
 * Created by Luke Rasmussen on 8/1/18.
 */
public class TemporalRelationship {
    private TemporalEvent event1;
    private TemporalEvent event2;
    private String spanOperator;
    private String eventOperator;
    private String value;
    private String units;

    public TemporalRelationship() {}

    public TemporalRelationship(TemporalEvent event1, TemporalEvent event2, String eventOperator, String spanOperator, String value, String units) {
        this.event1 = event1;
        this.event2 = event2;
        this.eventOperator = eventOperator;
        this.spanOperator = spanOperator;
        this.value = value;
        this.units = units;
    }

    public TemporalEvent getEvent1() {
        return event1;
    }

    public void setEvent1(TemporalEvent event1) {
        this.event1 = event1;
    }

    public TemporalEvent getEvent2() {
        return event2;
    }

    public void setEvent2(TemporalEvent event2) {
        this.event2 = event2;
    }

    public String getSpanOperator() {
        return spanOperator;
    }

    public void setSpanOperator(String operator) {
        this.spanOperator = operator;
    }

    public String getEventOperator() {
        return eventOperator;
    }

    public void setEventOperator(String operator) {
        this.eventOperator = operator;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }
}
