package org.phema.executer.i2b2;

import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.phema.executer.interfaces.IHttpHelper;
import org.phema.executer.util.HttpHelper;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by Luke Rasmussen on 7/19/17.
 */
@RunWith(JUnitPlatform.class)
public class I2b2ServiceBaseTest {
    class I2B2ServiceBaseStub extends I2b2ServiceBase {
        public I2B2ServiceBaseStub(I2B2ExecutionConfiguration configuration, IHttpHelper httpHelper) {
            super(configuration, httpHelper, null);
        }
        @Override
        public ProjectManagementService getProjectManagementService() {
            return null;
        }
    }

    @Test
    void loadRequestMessageTemplate() {
        I2b2ServiceBase service = new I2B2ServiceBaseStub(new I2B2ExecutionConfiguration(), new HttpHelper(false));
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
        I2b2ServiceBase service = new I2B2ServiceBaseStub(new I2B2ExecutionConfiguration(), new HttpHelper(false));
        assertThrows(NullPointerException.class, () -> { service.loadRequest("blahblah"); });
    }

}