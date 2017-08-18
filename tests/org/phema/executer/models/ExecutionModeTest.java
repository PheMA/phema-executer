package org.phema.executer.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by Luke Rasmussen on 7/17/17.
 */
class ExecutionModeTest {
    @Test
    void getValue() {
        // Simple check that we can convert enum to int
        assertEquals(1, ExecutionModeType.OPTIMIZED.getValue());
    }
}