package org.phema.executer.cts2.models;

/**
 * Created by Luke Rasmussen on 7/25/17.
 */
public class ValueSet {
    private String oid = "";
    private String name = "";

    public ValueSet() {

    }

    public ValueSet(String oid, String name) {
        setOid(oid);
        setName(name);
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) { return false; }
        if (obj == this) { return true; }
        if (!(obj instanceof ValueSet)) { return false; }
        ValueSet otherValueSet = (ValueSet)obj;
        if (otherValueSet.getOid().equals(this.getOid())) {
            return otherValueSet.getName().equals(this.getName());
        }

        return false;
    }
}
