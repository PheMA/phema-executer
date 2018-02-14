package org.phema.executer.hqmf.models;

/**
 * Created by Luke Rasmussen on 1/30/18.
 */
public class Setting {
    private String title;
    private String category;
    private String definition;
    private String status;
    private String subCategory;
    private boolean hardStatus;
    private String patientAPIFunction;
    private boolean notSupported;

    public Setting(String title, String category, String definition, String status, String subCategory, boolean hardStatus, String patientAPIFunction, boolean notSupported) {
        this.title = title;
        this.category = category;
        this.definition = definition;
        this.status = status;
        this.subCategory = subCategory;
        this.hardStatus = hardStatus;
        this.patientAPIFunction = patientAPIFunction;
        this.notSupported = notSupported;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSubCategory() {
        return subCategory;
    }

    public void setSubCategory(String subCategory) {
        this.subCategory = subCategory;
    }

    public boolean isHardStatus() {
        return hardStatus;
    }

    public void setHardStatus(boolean hardStatus) {
        this.hardStatus = hardStatus;
    }

    public String getPatientAPIFunction() {
        return patientAPIFunction;
    }

    public void setPatientAPIFunction(String patientAPIFunction) {
        this.patientAPIFunction = patientAPIFunction;
    }

    public boolean isNotSupported() {
        return notSupported;
    }

    public void setNotSupported(boolean notSupported) {
        this.notSupported = notSupported;
    }
}
