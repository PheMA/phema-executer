package org.phema.executer.hqmf.models;

import java.util.HashMap;

/**
 * Created by Luke Rasmussen on 8/29/17.
 *
 * Represents a data criteria specification
 */
public class DataCriteria {
    public static final String SOURCE_DATA_CRITERIA_TEMPLATE_ID = "2.16.840.1.113883.3.100.1.1";
    public static final String SOURCE_DATA_CRITERIA_TEMPLATE_TITLE = "Source data criteria";

    public static final String XPRODUCT = "XPRODUCT";
    public static final String UNION = "UNION";
    public static final String INTERSECT = "INTERSECT";

    public static final String SATISFIES_ALL = "satisfies_all";
    public static final String SATISFIES_ANY = "satisfies_any";
    public static final String VARIABLE = "variable";

    // An object containing metadata information for all attributes that are used within the measure data criteria being parsed.
    //
    // fields include:
    // `title`: The QDM human readable title for the attribute.
    // `coded_entry_method`: this appears to be a way that fields here are referenced within Bonnie.
    // `field_type`: The type of whatever will be stored for this attribute. This will often be `:timestamp` or `:value`.
    // `code`: The code for the entry. This should be included to make HQMF generation work properly. This is whatever code is dictated in the HQMF. For Diagnosis, this is in [HQMF QDM IG](http://www.hl7.org/implement/standards/product_brief.cfm?product_id=346) vol 2 page 155 and is `29308-4`.
    // `code_system`: This is the oid for whatever code system contains `code`. For Diagnosis, this is LOINC: `2.16.840.1.113883.6.1`. This is also located at (http://www.hl7.org/implement/standards/product_brief.cfm?product_id=346) vol 2 page 155.
    // `template_id`: These appear to be related to HQMFr1 template ids. These appear to be dangerously out of date. Don"t use.
    public static HashMap<String, FieldMetadata> FIELDS = new HashMap<String, FieldMetadata>()
    {{
        put("ABATEMENT_DATETIME", new FieldMetadata("Abatement Datetime", "end_date", "timestamp", "", "", ""));
        put("ACTIVE_DATETIME", new FieldMetadata("Active Date/Time", "active_date_time", "timestamp", "", "", ""));
        put("ADMISSION_DATETIME", new FieldMetadata("Admission Date/Time", "admit_time", "timestamp","399423000", "2.16.840.1.113883.6.96", ""));
        put("ANATOMICAL_APPROACH_SITE", new FieldMetadata("Anatomical Approach Site", "anatomical_approach", "value", "", "", ""));
        put("ANATOMICAL_LOCATION_SITE", new FieldMetadata("Anatomical Location Site", "anatomical_location", "value", "", "", ""));
        put("ANATOMICAL_STRUCTURE", new FieldMetadata("Anatomical Structure", "anatomical_structure", "value","91723000", "2.16.840.1.113883.6.96", "2.16.840.1.113883.3.560.1.1000.2"));
        put("CAUSE", new FieldMetadata("Cause", "cause_of_death", "value","42752001", "2.16.840.1.113883.6.96", "2.16.840.1.113883.3.560.1.1017.2"));
        put("CUMULATIVE_MEDICATION_DURATION", new FieldMetadata("Cumulative Medication Duration", "cumulative_medication_duration","value","261773006", "2.16.840.1.113883.6.96", "2.16.840.1.113883.3.560.1.1001.3"));
        // MISSING Date - The date that the patient passed away. - Patient Characteristic Expired
        put("DIAGNOSIS", new FieldMetadata("Diagnosis", "diagnosis", "value", "", "", ""));
        put("DISCHARGE_DATETIME", new FieldMetadata("Discharge Date/Time", "discharge_time", "timestamp","442864001", "2.16.840.1.113883.6.96", "2.16.840.1.113883.3.560.1.1025.1"));
        put("DISCHARGE_STATUS", new FieldMetadata("Discharge Status", "discharge_disposition", "value","309039003", "2.16.840.1.113883.6.96",  "2.16.840.1.113883.3.560.1.1003.2"));
        put("DOSE", new FieldMetadata("Dose", "dose", "value","398232005", "2.16.840.1.113883.6.96", "2.16.840.1.113883.3.560.1.1004.1"));
        put("FACILITY_LOCATION", new FieldMetadata("Facility Location", "facility", "value","SDLOC","", ""));
        put("FACILITY_LOCATION_ARRIVAL_DATETIME", new FieldMetadata("Facility Location Arrival Date/Time", "facility_arrival", "nested_timestamp", "SDLOC_ARRIVAL","", ""));
        put("FACILITY_LOCATION_DEPARTURE_DATETIME", new FieldMetadata("Facility Location Departure Date/Time", "facility_departure", "nested_timestamp", "SDLOC_DEPARTURE","", ""));
        put("FREQUENCY", new FieldMetadata("Frequency", "administration_timing", "value", "307430002", "2.16.840.1.113883.6.96", "2.16.840.1.113883.3.560.1.1006.1"));
        put("HEALTH_RECORD_FIELD", new FieldMetadata("Health Record Field", "health_record_field", "value", "395676008", "2.16.840.1.113883.6.96", "2.16.840.1.113883.10.20.28.3.102:2014-11-24"));
        put("INCISION_DATETIME", new FieldMetadata("Incision Date/Time", "incision_time", "timestamp", "34896006", "2.16.840.1.113883.6.96", "2.16.840.1.113883.10.20.24.3.89"));
        put("LATERALITY", new FieldMetadata("Laterality", "laterality", "value", "272741003", "2.16.840.1.113883.6.96", ""));
        put("LENGTH_OF_STAY", new FieldMetadata("Length of Stay", "length_of_stay", "value", "183797002", "2.16.840.1.113883.6.96", "2.16.840.1.113883.3.560.1.1029.3"));
        put("METHOD", new FieldMetadata("Method", "method", "value","", "", ""));
        // Negation Rationale isn"t encoded
        put("ONSET_AGE", new FieldMetadata("Onset Age", "onset_age", "value", "445518008", "2.16.840.1.113883.6.96", ""));
        put("ONSET_DATETIME", new FieldMetadata("Onset Datetime", "start_date", "timestamp", "", "", ""));
        put("ORDINAL", new FieldMetadata("Ordinality", "ordinality", "value", "117363000", "2.16.840.1.113883.6.96", "2.16.840.1.113883.3.560.1.1012.2")); // previous
        put("ORDINALITY", new FieldMetadata("Ordinality", "ordinality", "value", "117363000", "2.16.840.1.113883.6.96", "2.16.840.1.113883.3.560.1.1012.2"));
        put("PATIENT_PREFERENCE", new FieldMetadata("Patient Preference", "patient_preference", "value",  "PAT", "2.16.840.1.113883.5.8", "2.16.840.1.113883.10.20.24.3.83"));
        put("PRINCIPAL_DIAGNOSIS", new FieldMetadata("Principal Diagnosis", "principal_diagnosis", "value", "", "", ""));
        put("PROVIDER_PREFERENCE", new FieldMetadata("Provider Preference", "provider_preference", "value","103323008", "2.16.840.1.113883.6.96", "2.16.840.1.113883.10.20.24.3.84"));
        put("RADIATION_DOSAGE", new FieldMetadata("Radiation Dosage", "radiation_dose", "value","228815006", "2.16.840.1.113883.6.96", "2.16.840.1.113883.10.20.24.3.91"));
        put("RADIATION_DURATION", new FieldMetadata("Radiation Duration", "radiation_duration", "value", "306751006","2.16.840.1.113883.6.96", "2.16.840.1.113883.10.20.24.3.91"));
        put("REACTION", new FieldMetadata("Reaction", "reaction", "value", "263851003", "2.16.840.1.113883.6.96", "2.16.840.1.113883.10.20.24.3.85"));
        put("REASON", new FieldMetadata("Reason", "reason", "value", "410666004", "2.16.840.1.113883.6.96", "2.16.840.1.113883.10.20.24.3.88"));
        put("RECORDED_DATETIME", new FieldMetadata("Recorded Datetime", "start_date", "timestamp", "", "", ""));
        put("REFERENCE_RANGE_HIGH", new FieldMetadata("Reference Range High", "reference_range_high", "value", "", "", ""));
        put("REFERENCE_RANGE_LOW", new FieldMetadata("Reference Range Low", "reference_range_low", "value", "", "", ""));
        put("REFILLS", new FieldMetadata("Refills", "refills", "value", "", "", ""));
        put("RELATED_TO", new FieldMetadata("Related To", "related_to", "value",  "REL", "2.16.840.1.113883.1.11.11603", ""));
        put("RELATIONSHIP", new FieldMetadata("Relationship", "relationship_to_patient", "value", "", "", ""));
        put("REMOVAL_DATETIME", new FieldMetadata("Removal Date/Time", "removal_time", "timestamp", "118292001", "2.16.840.1.113883.6.96", "2.16.840.1.113883.3.560.1.1032.1"));
        // Result isn"t encoded
        put("ROUTE", new FieldMetadata("Route", "route", "value", "263513008", "2.16.840.1.113883.6.96", "2.16.840.1.113883.3.560.1.1020.2"));
        put("SEVERITY", new FieldMetadata("Severity", "severity", "value", "SEV", "2.16.840.1.113883.5.4", "2.16.840.1.113883.10.20.22.4.8"));
        put("SIGNED_DATETIME", new FieldMetadata("Signed Date/Time", "signed_date_time", "timestamp", "", "", ""));
        put("START_DATETIME", new FieldMetadata("Start Date/Time", "start_date", "timestamp", "398201009", "2.16.840.1.113883.6.96", "2.16.840.1.113883.3.560.1.1027.1"));
        // STATUS is referenced in the code as `qdm_status` because entry/Record already has a `status`/`status_code` field which has a different meaning
        put("STATUS", new FieldMetadata("Status", "qdm_status", "value", "33999-4", "2.16.840.1.113883.6.1", ""));
        put("STOP_DATETIME", new FieldMetadata("Stop Date/Time", "end_date", "timestamp", "397898000", "2.16.840.1.113883.6.96", "2.16.840.1.113883.3.560.1.1026.1"));
        put("TARGET_OUTCOME", new FieldMetadata("Target Outcome", "target_outcome", "value", "385676005", "2.16.840.1.113883.6.96", ""));
        // MISSING Time - The time that the patient passed away

        // Custom field values
        put("FLFS", new FieldMetadata("Fulfills", "fulfills", "reference", "FLFS","", ""));
        put("SOURCE", new FieldMetadata("Source", "source", "value", "260753009", "2.16.840.1.113883.6.96", "2.16.840.1.113883.3.560.1.2001.2"));
        put("TRANSFER_FROM", new FieldMetadata("Transfer From", "transfer_from", "value", "TRANSFER_FROM", "2.16.840.1.113883.10.20.24.3.81", ""));
        put("TRANSFER_FROM_DATETIME", new FieldMetadata("Transfer From Date/Time", "transfer_from_time", "nested_timestamp", "ORG_TIME", "2.16.840.1.113883.10.20.24.3.81", ""));
        put("TRANSFER_TO", new FieldMetadata("Transfer To", "transfer_to", "value", "TRANSFER_TO", "", "2.16.840.1.113883.10.20.24.3.82"));
        put("TRANSFER_TO_DATETIME", new FieldMetadata("Transfer To Date/Time", "transfer_to_time", "nested_timestamp", "DST_TIME", "", "2.16.840.1.113883.10.20.24.3.82"));
    }};

    // Maps attribute codes to the attribute keys
    public static HashMap<String, String> VALUE_FIELDS = new HashMap<String, String>()
    {{
        put("399423000", "ADMISSION_DATETIME");
        put("42752001", "CAUSE");
        put("261773006", "CUMULATIVE_MEDICATION_DURATION");
        put("363819003", "CUMULATIVE_MEDICATION_DURATION"); // previous
        put("442864001", "DISCHARGE_DATETIME");
        put("309039003", "DISCHARGE_STATUS");
        put("398232005", "DOSE");
        put("SDLOC", "FACILITY_LOCATION");
        put("SDLOC_ARRIVAL", "FACILITY_LOCATION_ARRIVAL_DATETIME");
        put("SDLOC_DEPARTURE", "FACILITY_LOCATION_DEPARTURE_DATETIME");
        put("307430002", "FREQUENCY");
        put("260864003", "FREQUENCY"); // previous
        put("395676008", "HEALTH_RECORD_FIELD");
        put("34896006", "INCISION_DATETIME");
        put("272741003", "LATERALITY");
        put("183797002", "LENGTH_OF_STAY");
        put("445518008", "ONSET_AGE");
        put("117363000", "ORDINALITY");
        put("PAT", "PATIENT_PREFERENCE");
        put("103323008", "PROVIDER_PREFERENCE");
        put("228815006", "RADIATION_DOSAGE");
        put("306751006", "RADIATION_DURATION");
        put("263851003", "REACTION");
        put("410666004", "REASON");
        put("REL", "RELATED_TO");
        put("118292001", "REMOVAL_DATETIME");
        put("263513008", "ROUTE");
        put("SEV", "SEVERITY");
        put("398201009", "START_DATETIME");
        put("33999-4", "STATUS");
        put("397898000", "STOP_DATETIME");
        put("385676005", "TARGET_OUTCOME");

        // Custom field values
        put("91723000", "ANATOMICAL_STRUCTURE");
        put("FLFS", "FLFS");
        put("260753009", "SOURCE");
        put("TRANSFER_FROM", "TRANSFER_FROM");
        put("ORG_TIME", "TRANSFER_FROM_DATETIME");
        put("TRANSFER_TO", "TRANSFER_TO");
        put("DST_TIME", "TRANSFER_TO_DATETIME");
    }};
}
