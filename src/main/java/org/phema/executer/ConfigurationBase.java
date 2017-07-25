package org.phema.executer;

import org.phema.executer.models.DescriptiveResult;
import org.phema.executer.models.ExecutionReturnType;
import org.phema.executer.models.ExecutionMode;

/**
 * Created by Luke Rasmussen on 7/17/17.
 */
public abstract class ConfigurationBase implements IConfiguration {
    private ExecutionReturnType ReturnType = ExecutionReturnType.COUNTS;
    private ExecutionMode Mode = ExecutionMode.OPTIMIZED;
    private String UMLSLogin = "";
    private String UMLSPassword = "";

    public ConfigurationBase() {
    }

    public ConfigurationBase(ExecutionReturnType returnType, ExecutionMode mode, String umlsLogin, String umlsPassword) {
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

    public ExecutionMode getMode() {
        return Mode;
    }

    public void setMode(ExecutionMode mode) {
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
}
