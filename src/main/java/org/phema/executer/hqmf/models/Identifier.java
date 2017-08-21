package org.phema.executer.hqmf.models;

/**
 * Created by Luke Rasmussen on 8/21/17.
 */
public class Identifier {
    private String type;
    private String root;
    private String extension;

    public Identifier(String type, String root, String extension) {
        this.type = type;
        this.root = root;
        this.extension = extension;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRoot() {
        return root;
    }

    public void setRoot(String root) {
        this.root = root;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

}
