package org.phema.executer.util;

import com.typesafe.config.ConfigObject;

/**
 * Created by Luke Rasmussen on 12/22/17.
 */
public class ConfigHelper {
    public static String getStringValue(ConfigObject object, String key, String defaultValue) {
        if (object == null || !object.containsKey(key)) {
            return defaultValue;
        }

        return object.get(key).unwrapped().toString();
    }
}
