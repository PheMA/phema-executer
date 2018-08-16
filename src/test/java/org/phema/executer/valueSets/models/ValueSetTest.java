package org.phema.executer.valueSets.models;

import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by Luke Rasmussen on 7/25/17.
 */
@RunWith(JUnitPlatform.class)
public class ValueSetTest {
    @Test
    public void valueSetConstructor_Empty() {
        ValueSet valueSet = new ValueSet();
        assertEquals("", valueSet.getName());
        assertEquals("", valueSet.getOid());
    }

    @Test
    public void valueSetConstructor_Parameters() {
        ValueSet valueSet = new ValueSet("oid", "name");
        assertEquals("name", valueSet.getName());
        assertEquals("oid", valueSet.getOid());
    }

    @Test
    public void equals_null() {
        ValueSet valueSet = new ValueSet("oid", "name");
        assertFalse(valueSet.equals(null));
    }

    @Test
    public void equals_sameObject() {
        ValueSet valueSet = new ValueSet("oid", "name");
        assertTrue(valueSet.equals(valueSet));
    }

    @Test
    public void equals_wrongType() {
        ValueSet valueSet = new ValueSet("oid", "name");
        assertFalse(valueSet.equals("oid"));
    }

    @Test
    public void equals_differentOIDs() {
        ValueSet valueSet = new ValueSet("oid", "name");
        ValueSet otherValueSet = new ValueSet("oid2", "name");
        assertFalse(valueSet.equals(otherValueSet));
    }

    @Test
    public void equals_differentNames() {
        ValueSet valueSet = new ValueSet("oid", "name");
        ValueSet otherValueSet = new ValueSet("oid", "name2");
        assertFalse(valueSet.equals(otherValueSet));
    }

    @Test
    public void equals_match() {
        ValueSet valueSet = new ValueSet("oid", "name");
        ValueSet otherValueSet = new ValueSet("oid", "name");
        assertTrue(valueSet.equals(otherValueSet));
    }

    @Test
    public void addMember() {
        ValueSet valueSet = new ValueSet("oid", "name");
        Member member = new Member("1", "2", "3", "4", "5");
        valueSet.addMember(member);
        assertEquals(1, valueSet.getMembers().size());
        assertEquals(member, valueSet.getMembers().get(0));
    }
}