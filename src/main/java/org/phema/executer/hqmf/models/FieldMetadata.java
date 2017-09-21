package org.phema.executer.hqmf.models;

/**
 * Created by Luke Rasmussen on 8/29/17.
 */
public class FieldMetadata {
    private String title;
    private String codedEntryMethod;
    private String fieldType;
    private String code;
    private String codeSystem;
    private String templateId;

    public FieldMetadata(String title, String codedEntryMethod, String fieldType, String code, String codeSystem, String templateId) {
        this.title = title;
        this.codedEntryMethod = codedEntryMethod;
        this.fieldType = fieldType;
        this.code = code;
        this.codeSystem = codeSystem;
        this.templateId = templateId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCodedEntryMethod() {
        return codedEntryMethod;
    }

    public void setCodedEntryMethod(String codedEntryMethod) {
        this.codedEntryMethod = codedEntryMethod;
    }

    public String getFieldType() {
        return fieldType;
    }

    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCodeSystem() {
        return codeSystem;
    }

    public void setCodeSystem(String codeSystem) {
        this.codeSystem = codeSystem;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }
}
