package org.phema.executer.util;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigObject;

import java.util.Arrays;

/**
 * Created by Luke Rasmussen on 12/22/17.
 */
public class ConfigHelper {
    public static String getStringValue(ConfigObject object, String key, String defaultValue) {
        if (object == null) {
            return defaultValue;
        }

        if (!key.contains(".")) {
            if (!object.containsKey(key)) {
                return defaultValue;
            }

            return object.get(key).unwrapped().toString();
        }

        String[] keys = key.split("\\.");
        if (!object.containsKey(keys[0])) {
            return defaultValue;
        }

        ConfigObject childObject = (ConfigObject)object.get(keys[0]);
        if (childObject == null) {
            return defaultValue;
        }

        return getStringValue(childObject, String.join(".", Arrays.copyOfRange(keys, 1, keys.length)), defaultValue);
    }
}
