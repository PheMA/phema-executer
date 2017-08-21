package org.phema.executer.hqmf.models;

/**
 * Created by Luke Rasmussen on 8/21/17.
 */
public class Coded {
    private String type;
    private String system;
    private String code;
    private String codeListId;
    private String title;
    private String nullFlavor;
    private String originalText;

    public Coded(String type, String system, String code, String codeListId, String title, String nullFlavor, String originalText) {
        this.type = type;
        this.system = system;
        this.code = code;
        this.codeListId = codeListId;
        this.title = title;
        this.nullFlavor = nullFlavor;
        this.originalText = originalText;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSystem() {
        return system;
    }

    public void setSystem(String system) {
        this.system = system;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCodeListId() {
        return codeListId;
    }

    public void setCodeListId(String codeListId) {
        this.codeListId = codeListId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getNullFlavor() {
        return nullFlavor;
    }

    public void setNullFlavor(String nullFlavor) {
        this.nullFlavor = nullFlavor;
    }

    public String getOriginalText() {
        return originalText;
    }

    public void setOriginalText(String originalText) {
        this.originalText = originalText;
    }

}
