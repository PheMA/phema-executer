package org.phema.executer.i2b2;

import org.junit.jupiter.api.Test;
import org.phema.executer.util.HttpHelper;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by Luke Rasmussen on 7/19/17.
 */
class I2b2ServiceBaseTest {
    @Test
    void loadRequestMessageTemplate() {
        I2b2ServiceBase service = new I2b2ServiceBase(new I2B2ExecutionConfiguration(), new HttpHelper());
        service.loadRequest("i2b2_login");
        try {
            assertNotNull(service.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false, "Unexpected exception thrown");
        }
    }

    @Test
    void loadRequest_MissingTemplate() {
        I2b2ServiceBase service = new I2b2ServiceBase(new I2B2ExecutionConfiguration(), new HttpHelper());
        assertThrows(NullPointerException.class, () -> { service.loadRequest("blahblah"); });
    }

}