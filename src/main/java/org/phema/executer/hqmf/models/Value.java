package org.phema.executer.hqmf.models;

/**
 * Created by Luke Rasmussen on 8/29/17.
 */
public class Value {
    private String type;
    private String unit;
    private String value;
    private boolean inclusive;
    private boolean derived;
    private String expression;

    public Value(String type, String unit, String value, boolean inclusive, boolean derived, String expression) {
        this.type = type;
        this.unit = unit;
        this.value = value;
        this.inclusive = inclusive;
        this.derived = derived;
        this.expression = expression;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isInclusive() {
        return inclusive;
    }

    public void setInclusive(boolean inclusive) {
        this.inclusive = inclusive;
    }

    public boolean isDerived() {
        return derived;
    }

    public void setDerived(boolean derived) {
        this.derived = derived;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }
}
