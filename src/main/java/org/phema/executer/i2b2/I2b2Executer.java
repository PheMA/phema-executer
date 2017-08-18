package org.phema.executer.i2b2;

import org.phema.executer.ExecuterBase;
import org.phema.executer.interfaces.IConfiguration;

/**
 * Created by Luke Rasmussen on 7/19/17.
 */
public class I2b2Executer extends ExecuterBase {
    public I2b2Executer(IConfiguration configuration) {
        super(configuration);
    }

    @Override
    public boolean ValidateConfiguration(IConfiguration configuration) {
        return false;
    }
}
