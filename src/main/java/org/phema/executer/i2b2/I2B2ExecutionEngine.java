package org.phema.executer.i2b2;

import org.phema.executer.ExecutionEngineBase;
import org.phema.executer.interfaces.IExecutionConfiguration;

/**
 * Created by Luke Rasmussen on 7/19/17.
 */
public class I2B2ExecutionEngine extends ExecutionEngineBase {
    public I2B2ExecutionEngine(IExecutionConfiguration configuration) {
        super(configuration);
    }

    @Override
    public boolean validateConfiguration(IExecutionConfiguration configuration) {
        return false;
    }
}
