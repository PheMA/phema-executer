package org.phema.executer.models;

/**
 * Created by Luke Rasmussen on 7/17/17.
 */
public enum ExecutionReturnType {
    COUNTS(1), PATIENTS(2);

    private final int value;

    private ExecutionReturnType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
