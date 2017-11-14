package org.phema.executer.hqmf.models;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.phema.executer.util.FileHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Luke Rasmussen on 9/21/17.
 */
public class HqmfTemplateHelper {
    public static final String VERSION_R1 = "r1";
    public static final String VERSION_R2 = "r2";

    private static HashMap<String, HashMap<String, Template>> idMap;

    public static Object definitionForTemplateId(String templateId) throws ParseException {
        return definitionForTemplateId(templateId, VERSION_R1);
    }

    public static Template definitionForTemplateId(String templateId, String version) throws ParseException {
        if (idMap == null) {
            idMap = new HashMap<String, HashMap<String, Template>>(){{
                put(VERSION_R1, readJsonFile("hqmf_template_oid_map.json"));
                put(VERSION_R2, readJsonFile("hqmfr2_template_oid_map.json"));
            }};
        }

        return idMap.get(version).get(templateId);
    }

    private static HashMap<String, Template> readJsonFile(String fileName) throws ParseException {
        JSONParser parser = new JSONParser();
        String jsonContentAsString = FileHelper.getFileFromResource("hqmf/" + fileName);
        HashMap<String, Template> templates = new HashMap<String, Template>();
        JSONObject rootObject = (JSONObject) parser.parse(jsonContentAsString);
        for (Object key : rootObject.keySet()) {
            String id = (String)key;
            JSONObject jsonTemplate = (JSONObject) rootObject.get(id);
            Template template = new Template((String)jsonTemplate.get("definition"),
                    (String)jsonTemplate.get("status"),
                    (boolean)jsonTemplate.get("negation"));
            templates.put(id, template);
        }

        return templates;
    }
}
