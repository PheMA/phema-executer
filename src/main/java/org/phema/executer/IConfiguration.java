package org.phema.executer;

import org.phema.executer.models.DescriptiveResult;

import java.util.ArrayList;

/**
 * Created by Luke Rasmussen on 7/19/17.
 */
public interface IConfiguration {
    public DescriptiveResult Validate();
    public ArrayList<IValueSetRepository> getValueSetRepositories();
}
