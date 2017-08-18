package org.phema.executer.models;

/**
 * Created by Luke Rasmussen on 8/18/17.
 */
public enum ValueSetLocationType {
    LOCAL(1), CTS2(2);

    private final int value;

    private ValueSetLocationType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
