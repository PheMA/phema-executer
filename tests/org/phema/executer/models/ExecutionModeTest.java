package org.phema.executer.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by Luke Rasmussen on 7/17/17.
 */
class ExecutionModeTest {
    @Test
    void getValue() {
        // Simple check that we can convert enum to int
        assertEquals(1, ExecutionModeType.NORMAL.getValue());
    }

    @Test
    void fromString_empty() {
        assertEquals(ExecutionModeType.NORMAL, ExecutionModeType.fromString(null));
        assertEquals(ExecutionModeType.NORMAL, ExecutionModeType.fromString(""));
        assertEquals(ExecutionModeType.NORMAL, ExecutionModeType.fromString("  "));
    }

    @Test
    void fromString_invalid() {
        assertEquals(ExecutionModeType.NORMAL, ExecutionModeType.fromString("BLAH"));
        assertEquals(ExecutionModeType.NORMAL, ExecutionModeType.fromString("DBUG"));
    }
    @Test
    void fromString_valid() {
        assertEquals(ExecutionModeType.NORMAL, ExecutionModeType.fromString("normal"));
        assertEquals(ExecutionModeType.NORMAL, ExecutionModeType.fromString("NORMAL"));
        assertEquals(ExecutionModeType.DEBUG, ExecutionModeType.fromString("debug"));
        assertEquals(ExecutionModeType.DEBUG, ExecutionModeType.fromString("DeBuG"));
    }
}