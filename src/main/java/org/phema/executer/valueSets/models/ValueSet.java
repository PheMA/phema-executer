package org.phema.executer.valueSets.models;

import java.util.ArrayList;

/**
 * Created by Luke Rasmussen on 7/25/17.
 */
public class ValueSet {
    private String oid = "";
    private String name = "";
    private ArrayList<Member> members = null;
    private ArrayList<ValueSet> valueSets = null;

    public ValueSet() {
    }

    public ValueSet(String oid, String name) {
        setOid(oid);
        setName(name);
    }

    public void addMember(Member member) {
        if (this.members == null) {
            this.members = new ArrayList<>();
        }

        this.members.add(member);
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

    public ArrayList<Member> getMembers() {
        return members;
    }

    public void setMembers(ArrayList<Member> members) {
        this.members = members;
    }

    public ArrayList<ValueSet> getValueSets() {
        return valueSets;
    }

    public void setValueSets(ArrayList<ValueSet> valueSets) {
        this.valueSets = valueSets;
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
