package org.phema.executer.i2b2;

import org.junit.jupiter.api.Test;
import org.phema.executer.models.ExecutionModeType;
import org.phema.executer.models.ExecutionReturnType;

import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by Luke Rasmussen on 7/19/17.
 */
class I2b2ConfigurationTest {
    @Test
    void defaultConstructor() {
        I2b2Configuration config = new I2b2Configuration();
        assertNull(config.getI2b2ProjectManagementUrl());
        assertEquals("", config.getI2b2Login());
        assertEquals("", config.getI2b2Password());
        assertEquals("", config.getI2b2Project());
        assertEquals("", config.getI2b2Domain());
        assertEquals(ExecutionReturnType.COUNTS, config.getReturnType());
        assertEquals(ExecutionModeType.OPTIMIZED, config.getMode());
    }

    @Test
    void constructorWithParams() {
        I2b2Configuration config = null;
        try {
            config = new I2b2Configuration(new URI("http://test.com"), "2", "3", "4", "5", ExecutionReturnType.PATIENTS, ExecutionModeType.DEBUG,
            "6", "7");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        assertNotNull(config);
        assertEquals("http://test.com", config.getI2b2ProjectManagementUrl().toString());
        assertEquals("2", config.getI2b2Login());
        assertEquals("3", config.getI2b2Password());
        assertEquals("4", config.getI2b2Project());
        assertEquals("5", config.getI2b2Domain());
        assertEquals(ExecutionReturnType.PATIENTS, config.getReturnType());
        assertEquals(ExecutionModeType.DEBUG, config.getMode());
        assertEquals("6", config.getUMLSLogin());
        assertEquals("7", config.getUMLSPassword());
    }
}