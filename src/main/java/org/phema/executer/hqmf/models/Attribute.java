package org.phema.executer.hqmf.models;

/**
 * Created by Luke Rasmussen on 8/21/17.
 */
public class Attribute {
    private String id;
    private String code;
    private String value;
    private String unit;
    private String name;
    private Identifier identifierObject;
    private Coded codeObject;
    private Object valueObject;

    public Attribute(String id, String code, String value, String unit, String name, Identifier identifierObject, Coded codeObject, Object valueObject) {
        setId(id);
        setCode(code);
        setValue(value);
        setUnit(unit);
        setName(name);
        setIdentifierObject(identifierObject);
        setCodeObject(codeObject);
        setValueObject(valueObject);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Identifier getIdentifierObject() {
        return identifierObject;
    }

    public void setIdentifierObject(Identifier identifierObject) {
        this.identifierObject = identifierObject;
    }

    public Coded getCodeObject() {
        return codeObject;
    }

    public void setCodeObject(Coded codeObject) {
        this.codeObject = codeObject;
    }

    public Object getValueObject() {
        return valueObject;
    }

    public void setValueObject(Object valueObject) {
        this.valueObject = valueObject;
    }
}
