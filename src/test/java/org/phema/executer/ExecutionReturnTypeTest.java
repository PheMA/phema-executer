package org.phema.executer.models;

import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by Luke Rasmussen on 7/17/17.
 */
@RunWith(JUnitPlatform.class)
public class ExecutionReturnTypeTest {
    @Test
    void getValue() {
        // Simple check that we can convert enum to int
        assertEquals(1, ExecutionReturnType.COUNTS.getValue());
    }
}