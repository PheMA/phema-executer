package org.phema.executer.models.i2b2;

/**
 * Created by Luke Rasmussen on 2/14/18.
 */
public class TemporalDefinition {
    private TemporalEvent event1;
    private TemporalEvent event2;
    private String operator;
    private String value;
    private String units;

    public TemporalDefinition() {}

    public TemporalDefinition(TemporalEvent event1, TemporalEvent event2, String operator, String value, String units) {
        this.event1 = event1;
        this.event2 = event2;
        this.operator = operator;
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

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
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
