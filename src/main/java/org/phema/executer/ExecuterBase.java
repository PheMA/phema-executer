/**
 * Created by Luke Rasmussen on 7/14/17.
 */
package org.phema.executer;

public abstract class ExecuterBase implements IExecuter {
    private IConfiguration configuration = null;

    public ExecuterBase(IConfiguration configuration) {
        setConfiguration(configuration);
    }

    public IConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(IConfiguration configuration) {
        this.configuration = configuration;
    }

    public void loadValueSets() {
        if (configuration == null) {
            throw new NullPointerException("You must specify a configuration for the executer");
        }


    }
}
