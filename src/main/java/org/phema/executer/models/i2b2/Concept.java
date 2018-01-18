package org.phema.executer.models.i2b2;

/**
 * Created by Luke Rasmussen on 1/3/18.
 */
public class Concept {
    private String key;
    private String name;
    private String baseCode;
    private int hierarchyLevel;
    private String tooltip;
    private boolean isSynonym;
    private String visualAttributes;

    public Concept(String key, String name, String baseCode, int hierarchyLevel, String tooltip, String synonymCode, String visualAttributes) {
        this.key = key;
        this.name = name;
        this.baseCode = baseCode;
        this.hierarchyLevel = hierarchyLevel;
        this.tooltip = tooltip;
        this.isSynonym = translateSynonymCode(synonymCode);
        setVisualAttributes(visualAttributes);
    }

    public Concept(String key, String name, String baseCode, int hierarchyLevel, String tooltip, boolean isSynonym, String visualAttributes) {
        this.key = key;
        this.name = name;
        this.baseCode = baseCode;
        this.hierarchyLevel = hierarchyLevel;
        this.tooltip = tooltip;
        this.isSynonym = isSynonym;
        setVisualAttributes(visualAttributes);
    }

    private boolean translateSynonymCode(String synonymCode) {
        return (synonymCode.equalsIgnoreCase("Y") || synonymCode.equalsIgnoreCase("YES"));
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

    public int getHierarchyLevel() {
        return hierarchyLevel;
    }

    public void setHierarchyLevel(int hierarchyLevel) {
        this.hierarchyLevel = hierarchyLevel;
    }

    public String getTooltip() {
        return tooltip;
    }

    public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
    }

    public boolean isSynonym() {
        return isSynonym;
    }

    public void setSynonym(boolean synonym) {
        isSynonym = synonym;
    }

    public String getVisualAttributes() {
        return visualAttributes;
    }

    public void setVisualAttributes(String visualAttributes) {
        this.visualAttributes = visualAttributes.trim();
    }
}
