package org.phema.executer;

import org.phema.executer.i2b2.I2b2Configuration;
import org.phema.executer.i2b2.ProjectManagementService;
import org.phema.executer.models.ExecutionMode;
import org.phema.executer.models.ExecutionReturnType;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by Luke Rasmussen on 7/19/17.
 */
public class CLITest {
    public static void main(String[] args) {
        I2b2Configuration configuration = null;
        try {
            configuration = new I2b2Configuration(new URI("http://172.16.51.215:9090/i2b2/services/PMService/"), "demo", "demouser", "demo", "i2b2demo", ExecutionReturnType.COUNTS, ExecutionMode.OPTIMIZED);
            ProjectManagementService service = new ProjectManagementService(configuration);
            service.login();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
