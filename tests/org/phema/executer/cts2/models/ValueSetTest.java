package org.phema.executer.cts2.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by Luke Rasmussen on 7/25/17.
 */
class ValueSetTest {
    @Test
    void valueSetConstructor_Empty() {
        ValueSet valueSet = new ValueSet();
        assertEquals("", valueSet.getName());
        assertEquals("", valueSet.getOid());
    }

    @Test
    void valueSetConstructor_Parameters() {
        ValueSet valueSet = new ValueSet("oid", "name");
        assertEquals("name", valueSet.getName());
        assertEquals("oid", valueSet.getOid());
    }

    @Test
    void equals_null() {
        ValueSet valueSet = new ValueSet("oid", "name");
        assertFalse(valueSet.equals(null));
    }

    @Test
    void equals_sameObject() {
        ValueSet valueSet = new ValueSet("oid", "name");
        assertTrue(valueSet.equals(valueSet));
    }

    @Test
    void equals_wrongType() {
        ValueSet valueSet = new ValueSet("oid", "name");
        assertFalse(valueSet.equals("oid"));
    }

    @Test
    void equals_differentOIDs() {
        ValueSet valueSet = new ValueSet("oid", "name");
        ValueSet otherValueSet = new ValueSet("oid2", "name");
        assertFalse(valueSet.equals(otherValueSet));
    }

    @Test
    void equals_differentNames() {
        ValueSet valueSet = new ValueSet("oid", "name");
        ValueSet otherValueSet = new ValueSet("oid", "name2");
        assertFalse(valueSet.equals(otherValueSet));
    }

    @Test
    void equals_match() {
        ValueSet valueSet = new ValueSet("oid", "name");
        ValueSet otherValueSet = new ValueSet("oid", "name");
        assertTrue(valueSet.equals(otherValueSet));
    }
}