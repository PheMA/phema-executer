package org.phema.executer.i2b2;

import org.w3c.dom.Document;

/**
 * Created by Luke Rasmussen on 7/17/17.
 */

public class ProjectManagementService extends I2b2ServiceBase {
    public ProjectManagementService(I2b2Configuration configuration) {
        super(configuration);
    }

    public void login() throws Exception {
        StringBuilder builder = new StringBuilder();
        builder.append(
                "        <pm:get_user_configuration>\n" +
                "            <project>undefined</project>\n" +
                "        </pm:get_user_configuration>");

        loadRequest();
        message = message.replace("<message_body/>", builder.toString());
        Document document = getMessage();

    }
}
