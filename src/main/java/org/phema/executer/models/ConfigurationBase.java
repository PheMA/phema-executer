package org.phema.executer.models;

import org.phema.executer.interfaces.IConfiguration;
import org.phema.executer.interfaces.IValueSetRepository;

import java.util.ArrayList;

/**
 * Created by Luke Rasmussen on 7/17/17.
 */
public abstract class ConfigurationBase implements IConfiguration {
    private ExecutionReturnType ReturnType = ExecutionReturnType.COUNTS;
    private ExecutionModeType Mode = ExecutionModeType.OPTIMIZED;
    private ValueSetLocationType ValueSetLocation = ValueSetLocationType.LOCAL;
    private String UMLSLogin = "";
    private String UMLSPassword = "";
    private ArrayList<IValueSetRepository> ValueSetRepositories;

    public ConfigurationBase() {
    }

    public ConfigurationBase(ExecutionReturnType returnType, ExecutionModeType mode, String umlsLogin, String umlsPassword) {
        setReturnType(returnType);
        setMode(mode);
        setUMLSLogin(umlsLogin);
        setUMLSPassword(umlsPassword);
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

    public String getUMLSLogin() {
        return UMLSLogin;
    }

    public void setUMLSLogin(String umlsLogin) {
        this.UMLSLogin = umlsLogin;
    }

    public String getUMLSPassword() {
        return UMLSPassword;
    }

    public void setUMLSPassword(String umlsPassword) {
        this.UMLSPassword = umlsPassword;
    }

    public ArrayList<IValueSetRepository> getValueSetRepositories() {
        return null;
    }
}
