package org.phema.executer.hqmf.v2;

import org.w3c.dom.NodeList;

import java.util.ArrayList;

/**
 * Created by Luke Rasmussen on 8/25/17.
 */
public class SourceDataCriteriaHelper {
    // Given a list of criteria obtained from the XML, generate most of the source data criteria (since no explicit
    // sources are given). After generating the source data criteria, filter the list to not include repeated,
    // unnecessary sources, but maintain and return map of those that have been removed to those that they were replaced
    // with.
    public static ArrayList getSourceDataCriteriaList(NodeList fullCriteriaList, Object data_criteria_references, Object occurrences_map) {
        // currently, this will erase the sources if the ids are the same, but will not correct references later on
        //TODO implement
        return null;
    }
}
