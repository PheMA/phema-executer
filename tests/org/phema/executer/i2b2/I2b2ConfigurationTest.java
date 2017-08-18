package org.phema.executer.i2b2;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by Luke Rasmussen on 7/19/17.
 */
class I2b2ConfigurationTest {
    @Test
    void defaultConstructor() {
        I2B2ExecutionConfiguration config = new I2B2ExecutionConfiguration();
        assertNull(config.getI2b2ProjectManagementUrl());
        assertEquals("", config.getI2b2Login());
        assertEquals("", config.getI2b2Password());
        assertEquals("", config.getI2b2Project());
        assertEquals("", config.getI2b2Domain());
    }

    @Test
    void constructorWithParams() {
        I2B2ExecutionConfiguration config = null;
        try {
            config = new I2B2ExecutionConfiguration(new URI("http://test.com"), "2", "3", "4", "5");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        assertNotNull(config);
        assertEquals("http://test.com", config.getI2b2ProjectManagementUrl().toString());
        assertEquals("2", config.getI2b2Login());
        assertEquals("3", config.getI2b2Password());
        assertEquals("4", config.getI2b2Project());
        assertEquals("5", config.getI2b2Domain());
    }
}