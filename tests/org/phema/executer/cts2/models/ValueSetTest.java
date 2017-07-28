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
}