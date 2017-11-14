package org.phema.executer.util;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

/**
 * Created by Luke Rasmussen on 9/21/17.
 */
public class FileHelper {
    /**
     * Retrieve the string contents of a file that is an embedded resource
     * @param fileName
     * @return
     */
    public static String getFileFromResource(String fileName) {
        StringBuilder result = new StringBuilder("");

        //Get file from resources folder
        ClassLoader classLoader = FileHelper.class.getClassLoader();
        File file = new File(classLoader.getResource(fileName).getFile());

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                result.append(line).append("\n");
            }

            scanner.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result.toString();
    }
}
