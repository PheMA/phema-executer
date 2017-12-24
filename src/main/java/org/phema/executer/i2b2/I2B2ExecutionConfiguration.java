package org.phema.executer.i2b2;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigObject;
import org.phema.executer.interfaces.IExecutionEngineConfiguration;
import org.phema.executer.models.DescriptiveResult;
import org.phema.executer.util.ConfigHelper;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by Luke Rasmussen on 7/19/17.
 */
public class I2B2ExecutionConfiguration implements IExecutionEngineConfiguration {
    private URI i2b2ProjectManagementUrl = null;
    private String i2b2Login = "";
    private String i2b2Password = "";
    private String i2b2Project = "";
    private String i2b2Domain = "";

    public I2B2ExecutionConfiguration() {
        super();
    }

    public I2B2ExecutionConfiguration(URI i2b2ProjectManagementUrl, String i2b2Login, String i2b2Password, String i2b2Project, String i2b2Domain) {
        setI2b2ProjectManagementUrl(i2b2ProjectManagementUrl);
        setI2b2Login(i2b2Login);
        setI2b2Password(i2b2Password);
        setI2b2Project(i2b2Project);
        setI2b2Domain(i2b2Domain);
    }

    public DescriptiveResult loadFromConfiguration(Config config) {
        ConfigObject i2b2Object = config.getObject("execution.i2b2");
        try {
            setI2b2ProjectManagementUrl(new URI(ConfigHelper.getStringValue(i2b2Object, "projectManagementUrl", "")));
        } catch (URISyntaxException e) {
            return new DescriptiveResult(false, "The i2b2 Project Management cell URL is not valid.\r\nIf you need assistance with specifying the URL, please contact your local i2b2 administrator.");
        }
        setI2b2Login(ConfigHelper.getStringValue(i2b2Object, "login", ""));
        setI2b2Password(ConfigHelper.getStringValue(i2b2Object, "password", ""));
        setI2b2Domain(ConfigHelper.getStringValue(i2b2Object, "domain", ""));
        setI2b2Project(ConfigHelper.getStringValue(i2b2Object, "project", ""));
        return validate();
    }

    public URI getI2b2ProjectManagementUrl() {
        return i2b2ProjectManagementUrl;
    }

    public void setI2b2ProjectManagementUrl(URI i2b2ProjectManagementUrl) {
        this.i2b2ProjectManagementUrl = i2b2ProjectManagementUrl;
    }

    public String getI2b2Login() {
        return i2b2Login;
    }

    public void setI2b2Login(String i2b2Login) {
        this.i2b2Login = i2b2Login;
    }

    public String getI2b2Password() {
        return i2b2Password;
    }

    public void setI2b2Password(String i2b2Password) {
        this.i2b2Password = i2b2Password;
    }

    public String getI2b2Project() {
        return i2b2Project;
    }

    public void setI2b2Project(String i2b2Project) {
        this.i2b2Project = i2b2Project;
    }

    public String getI2b2Domain() {
        return i2b2Domain;
    }

    public void setI2b2Domain(String i2b2Domain) {
        this.i2b2Domain = i2b2Domain;
    }

    @Override
    public DescriptiveResult validate() {
        DescriptiveResult result = new DescriptiveResult(true);
        if (i2b2ProjectManagementUrl == null) {
            result.setSuccess(false);
            result.addDescription("You must specify an i2b2 project management (PM) cell URL.  This will be in a format like \"http://hostname:9090/i2b2/services/PMService/\" (note the inclusion of PMService)");
        }

        if (i2b2Login == null || i2b2Login.isEmpty()) {
            result.setSuccess(false);
            result.addDescription("You must specify a login (username) for i2b2");
        }

        if (i2b2Password == null || i2b2Password.isEmpty()) {
            result.setSuccess(false);
            result.addDescription("You must specify the password for your i2b2 login");
        }

        if (i2b2Project == null || i2b2Project.isEmpty()) {
            result.setSuccess(false);
            result.addDescription("You must specify the i2b2 project that this phenotype should be run against.");
        }

        if (i2b2Domain == null || i2b2Domain.isEmpty()) {
            result.setSuccess(false);
            result.addDescription("You must specify the i2b2 domain that this phenotype should be run against.  You may need to contact your i2b2 Administrator to get this value.");
        }

        return result;
    }
}
