package org.phema.executer.interfaces;

import com.typesafe.config.Config;
import org.phema.executer.models.DescriptiveResult;

import java.util.ArrayList;

/**
 * Created by Luke Rasmussen on 7/19/17.
 */
public interface IExecutionConfiguration {
    public DescriptiveResult loadFromConfiguration(Config config);
    public DescriptiveResult validate();
    public ArrayList<IValueSetRepository> getValueSetRepositories();
}
