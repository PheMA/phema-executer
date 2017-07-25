package org.phema.executer;

import org.junit.jupiter.api.Test;
import org.phema.executer.models.DescriptiveResult;
import org.phema.executer.models.ExecutionMode;
import org.phema.executer.models.ExecutionReturnType;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by Luke Rasmussen on 7/17/17.
 */
class ConfigurationBaseTest {
    class ConfgurationTest extends ConfigurationBase {


        public ConfgurationTest(ExecutionReturnType returnType, ExecutionMode mode, String umlsLogin, String umlsPassword) {
            super(returnType, mode, umlsLogin, umlsPassword);
        }

        public ConfgurationTest() {
            super();
        }

        @Override
        public DescriptiveResult Validate() {
            return null;
        }
    }

    @Test
    void defaultConstructor() {
        ConfgurationTest config = new ConfgurationTest();
        assertEquals(ExecutionReturnType.COUNTS, config.getReturnType());
        assertEquals(ExecutionMode.OPTIMIZED, config.getMode());
        assertEquals("", config.getUMLSLogin());
        assertEquals("", config.getUMLSPassword());
    }

    @Test
    void constructorWithParams() {
        String login = "login";
        String password = "password";
        ConfgurationTest config = new ConfgurationTest(ExecutionReturnType.PATIENTS, ExecutionMode.DEBUG, login, password);
        assertEquals(ExecutionReturnType.PATIENTS, config.getReturnType());
        assertEquals(ExecutionMode.DEBUG, config.getMode());
        assertEquals(login, config.getUMLSLogin());
        assertEquals(password, config.getUMLSPassword());
    }
}