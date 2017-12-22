package org.phema.executer.configuration;

import org.phema.executer.interfaces.IExecutionConfiguration;
import org.phema.executer.interfaces.IValueSetRepository;
import org.phema.executer.models.DescriptiveResult;
import org.phema.executer.models.ExecutionModeType;
import org.phema.executer.models.ExecutionReturnType;
import org.phema.executer.models.ValueSetLocationType;

import java.util.ArrayList;

/**
 * Created by Luke Rasmussen on 8/18/17.
 */
public class ExecutionConfiguration implements IExecutionConfiguration {
    private ExecutionReturnType ReturnType = ExecutionReturnType.COUNTS;
    private ExecutionModeType Mode = ExecutionModeType.OPTIMIZED;
    private ValueSetLocationType ValueSetLocation = ValueSetLocationType.LOCAL;
    private ArrayList<IValueSetRepository> ValueSetRepositories;
    private String ExecutionEngineName = "";

    public ExecutionConfiguration() {
    }

    public ExecutionConfiguration(String executionEngineName, ExecutionReturnType returnType, ExecutionModeType mode, ValueSetLocationType valueSetLocation, ArrayList<IValueSetRepository> valueSetRepositories) {
        setExecutionEngineName(executionEngineName);
        setReturnType(returnType);
        setMode(mode);
        setValueSetLocation(valueSetLocation);
        setValueSetRepositories(valueSetRepositories);
    }

    public ExecutionReturnType getReturnType() {
        return ReturnType;
    }

    public void setReturnType(ExecutionReturnType returnType) {
        ReturnType = returnType;
    }

    public ExecutionModeType getMode() {
        return Mode;
    }

    public void setMode(ExecutionModeType mode) {
        Mode = mode;
    }

    public String getExecutionEngineName() {
        return ExecutionEngineName;
    }

    public void setExecutionEngineName(String executionEngineName) {
        ExecutionEngineName = executionEngineName;
    }

    public ValueSetLocationType getValueSetLocation() {
        return ValueSetLocation;
    }

    public void setValueSetLocation(ValueSetLocationType valueSetLocation) {
        ValueSetLocation = valueSetLocation;
    }

    public void setValueSetRepositories(ArrayList<IValueSetRepository> valueSetRepositories) { ValueSetRepositories = valueSetRepositories; }

    @Override
    public DescriptiveResult validate() {
        return null;
    }

    @Override
    public ArrayList<IValueSetRepository> getValueSetRepositories() {
        return ValueSetRepositories;
    }
}
