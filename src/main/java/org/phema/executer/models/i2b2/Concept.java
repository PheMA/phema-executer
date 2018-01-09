package org.phema.executer.models.i2b2;

/**
 * Created by Luke Rasmussen on 1/3/18.
 */
public class Concept {
    private String key;
    private String name;
    private String baseCode;

    public Concept(String key, String name, String baseCode) {
        this.key = key;
        this.name = name;
        this.baseCode = baseCode;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBaseCode() {
        return baseCode;
    }

    public void setBaseCode(String baseCode) {
        this.baseCode = baseCode;
    }
}
