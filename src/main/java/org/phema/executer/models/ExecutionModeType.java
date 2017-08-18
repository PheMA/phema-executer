package org.phema.executer.models;

/**
 * Created by Luke Rasmussen on 7/17/17.
 */
public enum ExecutionModeType {
    OPTIMIZED(1), DEBUG(2);

    private final int value;

    private ExecutionModeType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
