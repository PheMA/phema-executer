/**
 * Created by Luke Rasmussen on 7/14/17.
 */
package org.phema.executer;

import org.phema.executer.interfaces.IExecutionConfiguration;
import org.phema.executer.interfaces.IExecutionEngine;

public abstract class ExecutionEngineBase implements IExecutionEngine {
    private IExecutionConfiguration configuration = null;

    public ExecutionEngineBase(IExecutionConfiguration configuration) {
        setConfiguration(configuration);
    }

    public IExecutionConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(IExecutionConfiguration configuration) {
        this.configuration = configuration;
    }

    public void loadValueSets() {
        if (configuration == null) {
            throw new NullPointerException("You must specify a configuration for the executer");
        }


    }
}
