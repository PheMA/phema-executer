package org.phema.executer.hqmf.models;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Luke Rasmussen on 9/21/17.
 */
public class HqmfTemplateHelper {
    public static final String VERSION_R1 = "r1";
    public static final String VERSION_R2 = "r2";

    private static HashMap<String, ArrayList<Object>> idMap;

    public static Object definitionForTemplateId(String templateId) {
        return definitionForTemplateId(templateId, VERSION_R1);
    }

    public static Object definitionForTemplateId(String templateId, String version) {
        if (idMap == null) {
            idMap = new HashMap<String, ArrayList<Object>>(){{
                put(VERSION_R1, null);
                put(VERSION_R2, null);
            }};
        }
    }

    private static Object readJsonFile() {
        JSONParser parser = new JSONParser();
    }
}
