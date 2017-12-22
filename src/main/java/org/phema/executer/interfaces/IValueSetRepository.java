package org.phema.executer.interfaces;

import org.phema.executer.valueSets.models.ValueSet;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Luke Rasmussen on 7/25/17.
 */
public interface IValueSetRepository {
    void initialize(HashMap<String, String> parameters) throws Exception;
    ArrayList<ValueSet> search(String searchTerm);
    ValueSet getByOID(String oid);
}
