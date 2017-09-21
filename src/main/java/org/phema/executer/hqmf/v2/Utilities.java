package org.phema.executer.hqmf.v2;

/**
 * Created by Luke Rasmussen on 9/1/17.
 */
public class Utilities {
    // General helper for stripping '-' and ',' into '_' for processable ids
    public static String stripTokens(String input) {
        if (input == null) {
            return null;
        }
        String stripped = input.replaceAll("(?i)[^0-9a-z]", "_");
        //Prefix digits with 'prefix_' to prevent JS syntax errors
        stripped = stripped.replaceAll("^[0-9]", "prefix_" + input.charAt(0));
        return stripped;
    }
}
