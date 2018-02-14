package org.phema.executer.hqmf.models;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.phema.executer.util.FileHelper;

import java.util.HashMap;

/**
 * Created by Luke Rasmussen on 1/30/18.
 */
public class HqmfSettingsHelper {
    public static HashMap<String, Setting> readJsonFile(String fileName) throws ParseException {
        JSONParser parser = new JSONParser();
        String jsonContentAsString = FileHelper.getFileFromResource(fileName);
        HashMap<String, Setting> settings = new HashMap<String, Setting>();
        JSONObject rootObject = (JSONObject) parser.parse(jsonContentAsString);
        for (Object key : rootObject.keySet()) {
            String id = (String)key;
            JSONObject jsonTemplate = (JSONObject) rootObject.get(id);
            Setting setting = new Setting((String)jsonTemplate.get("title"),
                    (String)jsonTemplate.get("category"),
                    (String)jsonTemplate.get("definition"),
                    (String)jsonTemplate.get("status"),
                    (String)jsonTemplate.get("sub_category"),
                    (boolean)jsonTemplate.get("hard_status"),
                    (String)jsonTemplate.get("patient_api_function"),
                    (boolean)jsonTemplate.get("not_supported"));
            settings.put(id, setting);
        }

        return settings;
    }
}