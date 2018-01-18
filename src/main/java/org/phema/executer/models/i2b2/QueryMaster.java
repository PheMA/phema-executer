package org.phema.executer.models.i2b2;

/**
 * Created by Luke Rasmussen on 1/18/18.
 */
public class QueryMaster {
    public int id;
    public String name;

    public QueryMaster(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
