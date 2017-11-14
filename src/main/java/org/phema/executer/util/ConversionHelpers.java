package org.phema.executer.util;

/**
 * Created by Luke Rasmussen on 10/24/17.
 */
public class ConversionHelpers {
    public static Integer tryIntParse(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
