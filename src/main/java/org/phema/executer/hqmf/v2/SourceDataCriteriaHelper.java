package org.phema.executer.hqmf.v2;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * Created by Luke Rasmussen on 8/25/17.
 */
public class SourceDataCriteriaHelper {
    /*

      sha256 << (criteria.children_criteria.nil? ? '<nil>:' : "#{criteria.children_criteria.sort.join(',')}:")

      Digest::SHA256.hexdigest sha256
    end
     */

    // Generates an identifier based on the leftover elements included in the source data criteria.
    public static String identifier(DataCriteria criteria) {
        ArrayList<String> childrenCriteria = criteria.getChildrenCriteria();
        if (childrenCriteria != null) {
            childrenCriteria.sort((d1, d2) -> d1.compareTo(d2));
        }
        String sha256 = String.format("%s%s%s%s%s%s",
                criteria.getCodeListId(), criteria.getDefinition(), criteria.getStatus(), criteria.isSpecificOccurrence(),
                criteria.getSpecificOccurrenceConst(), criteria.isVariable(),
                (criteria.getChildrenCriteria() == null ? "<nil>:" : String.join(",", childrenCriteria)));
        return org.apache.commons.codec.digest.DigestUtils.sha256Hex(sha256);
    }

    // Given a list of criteria obtained from the XML, generate most of the source data criteria (since no explicit
    // sources are given). After generating the source data criteria, filter the list to not include repeated,
    // unnecessary sources, but maintain and return map of those that have been removed to those that they were replaced
    // with.
    public static Object[] getSourceDataCriteriaList(NodeList fullCriteriaList, HashMap<String, DataCriteria> dataCriteriaReferences, HashMap<String, String> occurrencesMap) throws Exception {
        // currently, this will erase the sources if the ids are the same, but will not correct references later on
        ArrayList<DataCriteria> sourceDataCriteria = new ArrayList<DataCriteria>();
        for (int index = 0; index < fullCriteriaList.getLength(); index++) {
            sourceDataCriteria.add(SourceDataCriteriaHelper.asSourceDataCriteria(fullCriteriaList.item(index),
                    dataCriteriaReferences, occurrencesMap));
        }

        HashMap<String, String> collapsedSourceDataCriteriaMap = new HashMap<>();
        HashMap<String, DataCriteria> uniqueSourceDataCriteria = new HashMap<>();
        for (DataCriteria sdc : sourceDataCriteria) {
            String identifier = SourceDataCriteriaHelper.identifier(sdc);
            if (uniqueSourceDataCriteria.containsKey(identifier)) {
                collapsedSourceDataCriteriaMap.put(sdc.getOriginalId(), uniqueSourceDataCriteria.get(identifier).getId());
            }
            else {
                uniqueSourceDataCriteria.put(identifier, sdc);
            }
        }

        ArrayList<DataCriteria> unique = new ArrayList<>();
        for (DataCriteria criteria : uniqueSourceDataCriteria.values()) {
            if (!SourceDataCriteriaHelper.shouldReject(criteria)) {
                unique.add(criteria);
            }
        }

        // we need an empty data criteria in source that acts as the target for the specific occurrence
        // the data criteria that we are duplicating will eventually get turned into a specific occurrence
        ArrayList<DataCriteria> occurrences = unique.stream().filter(c -> occurrencesMap.containsKey(c.getId()) && !c.getDefinition().equals("derived")).collect(Collectors.toCollection(ArrayList::new));
        for (DataCriteria occurrence : occurrences) {
            if (occurrence.isVariable()) {
                continue;
            }
            DataCriteria dc = SourceDataCriteriaHelper.asSourceDataCriteria(occurrence.getEntry());
            dc.setId(String.format("%s_nonSpecific", dc.getId()));
            dc.setSourceDataCriteria(dc.getId());
            if (SourceDataCriteriaHelper.findExistingSourceDataCriteria(unique, dc) == null) {
                unique.add(dc);
            }
        }

        return new Object[] { unique, collapsedSourceDataCriteriaMap };
    }

    // Check if there is an existing entry in the source data criteria list that matches the candidate passed in
    // this is used to prevent adding duplicate source data criteria entries when one already exists
    private static DataCriteria findExistingSourceDataCriteria(ArrayList<DataCriteria> list, DataCriteria candidate) {
        for (DataCriteria sdc : list) {
            if (SourceDataCriteriaHelper.identifier(sdc).equals(SourceDataCriteriaHelper.identifier(candidate))) {
                return sdc;
            }

            // we have another existing copy of the specific occurrence (identified via the constant and occurrence lettering),
            // use that rather than duplicating... there will not be an exact match for variables since a new child will
            // have been generated
            if (sdc.getSpecificOccurrenceConst() != null
                    && sdc.getSpecificOccurrenceConst().equals(candidate.getSpecificOccurrenceConst())
                    && sdc.isSpecificOccurrence() == candidate.isSpecificOccurrence()) {
                return sdc;
            }
        }

        return null;
    }

    // Rejects any derived elements as they should never be used as source.
    public static boolean shouldReject(DataCriteria dc) {
        return (dc.getDefinition() != null && dc.getDefinition().equals("derived"));
    }

    // Creates a data criteria based on an entry xml, removes any unnecessary elements (for the source),
    // and adds a data criteria reference if none exist
    private static DataCriteria asSourceDataCriteria(Node entry, HashMap<String, DataCriteria> dataCriteriaReferences, HashMap<String, String> occurrencesMap) throws Exception {
        DataCriteria dc = new DataCriteria(entry, dataCriteriaReferences, occurrencesMap);
        dc.setOriginalId(dc.getId());
        if (dc.getDefinition() == null || !dc.getDefinition().equals("derived")) {
            dc.setId(String.format("%s_source", dc.getId()));
        }

        dc = SourceDataCriteriaHelper.stripNonSourceCriteriaElements(dc);
        // add it as a reference
        if (dc != null && (!dataCriteriaReferences.containsKey(dc.getId()) || dataCriteriaReferences.get(dc.getId()).getCodeListId() == null)) {
            dataCriteriaReferences.put(dc.getId(), dc);
        }
        return dc;
    }

    private static DataCriteria asSourceDataCriteria(Node entry) throws Exception {
        return asSourceDataCriteria(entry, new HashMap<String, DataCriteria>(), new HashMap<String, String>());
    }

    // Removes unnecessary elements from a data criteria to create a source data criteria
    private static DataCriteria stripNonSourceCriteriaElements(DataCriteria dc) {
        if (dc.getDefinition() != null
                && (dc.getDefinition().equals(DataCriteria.SATISFIES_ANY) || dc.getDefinition().equals(DataCriteria.SATISFIES_ALL))) {
            dc.setDefinition("derived");
        }
        dc.setSourceDataCriteria(dc.getId());
        dc.setFieldValues(new HashMap<>());
        dc.setTemporalReferences(new ArrayList<>());
        dc.setSubsetOperators(new ArrayList<>());
        dc.setValue(null);
        dc.setNegation(false);
        dc.setNegationCodeListId(null);
        return dc;
    }
}
