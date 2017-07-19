package org.phema.executer.models;

/**
 * Created by Luke Rasmussen on 7/17/17.
 */
public enum ExecutionMode {
    OPTIMIZED(1), DEBUG(2);

    private final int value;

    private ExecutionMode(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
