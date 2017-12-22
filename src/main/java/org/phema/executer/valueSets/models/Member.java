package org.phema.executer.valueSets.models;

/**
 * Created by Luke Rasmussen on 12/12/17.
 */
public class Member {
    private String code = "";
    private String description = "";
    private String codeSystem = "";
    private String version = "";
    private String codeSystemOid = "";

    public Member() {
    }

    public Member(String code, String description, String codeSystem, String version, String codeSystemOid) {
        this.code = code;
        this.description = description;
        this.codeSystem = codeSystem;
        this.version = version;
        this.codeSystemOid = codeSystemOid;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCodeSystem() {
        return codeSystem;
    }

    public void setCodeSystem(String codeSystem) {
        this.codeSystem = codeSystem;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getCodeSystemOid() {
        return codeSystemOid;
    }

    public void setCodeSystemOid(String codeSystemOid) {
        this.codeSystemOid = codeSystemOid;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) { return false; }
        if (obj == this) { return true; }
        if (!(obj instanceof Member)) { return false; }
        Member otherObj = (Member)obj;
        return otherObj.getCode().equals(this.getCode())
                && otherObj.getCodeSystem().equals(this.getCodeSystem())
                && otherObj.getCodeSystemOid().equals(this.getCodeSystemOid())
                && otherObj.getDescription().equals(this.getDescription())
                && otherObj.getVersion().equals(this.getVersion());
    }
}



