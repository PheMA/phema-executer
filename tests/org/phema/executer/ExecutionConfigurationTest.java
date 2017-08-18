package org.phema.executer;

import org.junit.jupiter.api.Test;
import org.phema.executer.configuration.ExecutionConfiguration;
import org.phema.executer.interfaces.IValueSetRepository;
import org.phema.executer.models.ExecutionModeType;
import org.phema.executer.models.ExecutionReturnType;
import org.phema.executer.models.ValueSetLocationType;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Created by Luke Rasmussen on 7/17/17.
 */
class ExecutionConfigurationTest {
    @Test
    void defaultConstructor() {
        ExecutionConfiguration config = new ExecutionConfiguration();
        assertEquals("", config.getExecutionEngineName());
        assertEquals(ExecutionReturnType.COUNTS, config.getReturnType());
        assertEquals(ExecutionModeType.OPTIMIZED, config.getMode());
        assertEquals(ValueSetLocationType.LOCAL, config.getValueSetLocation());
        assertEquals(null, config.getValueSetRepositories());
    }

    @Test
    void constructorWithParams() {
        String executionEngineName = "TestEngine";
        ExecutionReturnType returnType = ExecutionReturnType.PATIENTS;
        ExecutionModeType mode = ExecutionModeType.DEBUG;
        ValueSetLocationType valueSetLocation = ValueSetLocationType.CTS2;
        ArrayList<IValueSetRepository> valueSetRepositories = new ArrayList<IValueSetRepository>();
        ExecutionConfiguration config = new ExecutionConfiguration(executionEngineName, returnType, mode, valueSetLocation, valueSetRepositories);
        assertEquals(executionEngineName, config.getExecutionEngineName());
        assertEquals(returnType, config.getReturnType());
        assertEquals(mode, config.getMode());
        assertEquals(valueSetLocation, config.getValueSetLocation());
        assertNotNull(config.getValueSetRepositories());
    }
}