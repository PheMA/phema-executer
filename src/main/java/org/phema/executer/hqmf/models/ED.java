package org.phema.executer.hqmf.models;

/**
 * Created by Luke Rasmussen on 8/23/17.
 */
public class ED {
    private String type;
    private String value;
    private String mediaType;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public ED(String type, String value, String mediaType) {
        this.type = type;
        this.value = value;
        this.mediaType = mediaType;
    }
}
