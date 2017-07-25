package org.phema.executer;

import org.phema.executer.cts2.models.ValueSet;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Luke Rasmussen on 7/25/17.
 */
public interface IValueSetRepository {
    void Initialize(HashMap<String, String> parameters) throws Exception;
    ArrayList<ValueSet> Search(String searchTerm);
}
