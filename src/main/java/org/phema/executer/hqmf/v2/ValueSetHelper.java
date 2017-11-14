package org.phema.executer.hqmf.v2;

import java.util.HashMap;

/**
 * Created by Luke Rasmussen on 9/21/17.
 */
public class ValueSetHelper {
    private static final HashMap<String, ValueSetMapEntry> VALUESET_MAP = new HashMap<String, ValueSetMapEntry>() {{
        put("2.16.840.1.113883.10.20.28.3.7", new ValueSetMapEntry("./*/value", null));
        put("2.16.840.1.113883.10.20.28.3.8", new ValueSetMapEntry("./*/code", null));
        put("2.16.840.1.113883.10.20.28.3.9", new ValueSetMapEntry("./*/code", null));
        put("2.16.840.1.113883.10.20.28.3.10", new ValueSetMapEntry("./*/code", null));
        put("2.16.840.1.113883.10.20.28.3.11", new ValueSetMapEntry("./*/participation[@typeCode='PRD']/role[@classCode='MANU']/playingDevice[@classCode='DEV']/code", null));
        put("2.16.840.1.113883.10.20.28.3.12", new ValueSetMapEntry("./*/participation[@typeCode='PRD']/role[@classCode='MANU']/playingDevice[@classCode='DEV']/code", null));
        put("2.16.840.1.113883.10.20.28.3.13", new ValueSetMapEntry("./*/participation[@typeCode='DEV']/role[@classCode='MANU']/playingDevice[@classCode='DEV']/code", null));
        put("2.16.840.1.113883.10.20.28.3.14", new ValueSetMapEntry("./*/participation[@typeCode='PRD']/role[@classCode='MANU']/playingDevice[@classCode='DEV']/code", null));
        put("2.16.840.1.113883.10.20.28.3.15", new ValueSetMapEntry("./*/participation[@typeCode='DEV']/role[@classCode='MANU']/playingDevice[@classCode='DEV']/code", null));
        put("2.16.840.1.113883.10.20.28.3.16", new ValueSetMapEntry("./*/participation[@typeCode='DEV']/role[@classCode='MANU']/playingDevice[@classCode='DEV']/code", null));
        put("2.16.840.1.113883.10.20.28.3.1", new ValueSetMapEntry("./*/value", null));
        put("2.16.840.1.113883.10.20.28.3.17", new ValueSetMapEntry("./*/value", null));
        put("2.16.840.1.113883.10.20.28.3.18", new ValueSetMapEntry("./*/value", null));
        put("2.16.840.1.113883.10.20.28.3.19", new ValueSetMapEntry("./*/value", null));
        put("2.16.840.1.113883.10.20.28.3.20", new ValueSetMapEntry("./*/outboundRelationship[@typeCode='CAUS']/observationCriteria/code", null));
        put("2.16.840.1.113883.10.20.28.3.21", new ValueSetMapEntry("./*/outboundRelationship[@typeCode='CAUS']/observationCriteria/code", null));
        put("2.16.840.1.113883.10.20.28.3.22", new ValueSetMapEntry("./*/code", null));
        put("2.16.840.1.113883.10.20.28.3.23", new ValueSetMapEntry("./*/code", "./*/value"));
        put("2.16.840.1.113883.10.20.28.3.24", new ValueSetMapEntry("./*/code", null));
        put("2.16.840.1.113883.10.20.28.3.26", new ValueSetMapEntry("./*/code", null));
        put("2.16.840.1.113883.10.20.28.3.27", new ValueSetMapEntry("./*/code", null));
        put("2.16.840.1.113883.10.20.28.3.5", new ValueSetMapEntry("./*/code", null));
        put("2.16.840.1.113883.10.20.28.3.28", new ValueSetMapEntry("./*/code", null));
        put("2.16.840.1.113883.10.20.28.3.29", new ValueSetMapEntry("./*/code", null));
        put("2.16.840.1.113883.10.20.28.3.30", new ValueSetMapEntry("./*/code", "./*/value"));
        put("2.16.840.1.113883.10.20.28.3.31", new ValueSetMapEntry("./*/code", null));
        put("2.16.840.1.113883.10.20.28.3.33", new ValueSetMapEntry("./*/outboundRelationship[@typeCode='CAUS' and @inversionInd='true']/procedureCriteria/code", null));
        put("2.16.840.1.113883.10.20.28.3.34", new ValueSetMapEntry("./*/outboundRelationship[@typeCode='CAUS']/actCriteria/code", null));
        put("2.16.840.1.113883.10.20.28.3.35", new ValueSetMapEntry("./*/code", null));
        put("2.16.840.1.113883.10.20.28.3.36", new ValueSetMapEntry("./*/code", "./*/outboundRelationship[@typeCode='REFR']//code[@code='394617004']/../value"));
        put("2.16.840.1.113883.10.20.28.3.37", new ValueSetMapEntry("./*/code", null));
        put("2.16.840.1.113883.10.20.28.3.39", new ValueSetMapEntry("./*/outboundRelationship[@typeCode='CAUS']/observationCriteria/code", null));
        put("2.16.840.1.113883.10.20.28.3.40", new ValueSetMapEntry("./*/outboundRelationship[@typeCode='CAUS']/observationCriteria/code", null));
        put("2.16.840.1.113883.10.20.28.3.41", new ValueSetMapEntry("./*/code", null));
        put("2.16.840.1.113883.10.20.28.3.42", new ValueSetMapEntry("./*/code", "./*/value"));
        put("2.16.840.1.113883.10.20.28.3.43", new ValueSetMapEntry("./*/code", null));
        put("2.16.840.1.113883.10.20.28.3.44", new ValueSetMapEntry("./*/participation[@typeCode='CSM']/role/playingMaterial[@classCode='MMAT']/code", null));
        put("2.16.840.1.113883.10.20.28.3.45", new ValueSetMapEntry("./*/participation[@typeCode='CSM']/role[@classCode='MANU']/playingManufacturedMaterial[@classCode='MMAT']/code", null));
        put("2.16.840.1.113883.10.20.28.3.46", new ValueSetMapEntry("./*/participation[@typeCode='CSM']/role[@classCode='MANU']/playingEntity[@classCode='MMAT']/code", null));
        put("2.16.840.1.113883.10.20.28.3.47", new ValueSetMapEntry("./*/participation[@typeCode='CSM']/role[@classCode='MANU']/playingEntity[@classCode='MMAT']/code", null));
        put("2.16.840.1.113883.10.20.28.3.48", new ValueSetMapEntry("./*/participation[@typeCode='CSM']/role[@classCode='MANU']/playingManufacturedMaterial[@classCode='MMAT']/code", null));
        put("2.16.840.1.113883.10.20.28.3.49", new ValueSetMapEntry("./*/participation[@typeCode='CSM']/role[@classCode='MANU']/playingMaterial[@classCode='MMAT']/code", null));
        put("2.16.840.1.113883.10.20.28.3.50", new ValueSetMapEntry("./*/participation[@typeCode='CSM']/role[@classCode='MANU']/playingMaterial[@classCode='MMAT']/code", null));
        put("2.16.840.1.113883.10.20.28.3.51", new ValueSetMapEntry("./*/participation[@typeCode='CSM']/role[@classCode='MANU']/playingMaterial[@classCode='MMAT']/code", null));
        put("2.16.840.1.113883.10.20.28.3.52", new ValueSetMapEntry("./*/value", null));
        put("2.16.840.1.113883.10.20.28.3.53", new ValueSetMapEntry("./*/code", "./*/value"));
        put("2.16.840.1.113883.10.20.28.3.6", new ValueSetMapEntry("./*/value", null));
        put("2.16.840.1.113883.10.20.28.3.54", new ValueSetMapEntry(null, null));
        put("2.16.840.1.113883.10.20.28.3.56", new ValueSetMapEntry("./*/value", null));
        put("2.16.840.1.113883.10.20.28.3.57", new ValueSetMapEntry("./*/value", null));
        put("2.16.840.1.113883.10.20.28.3.58", new ValueSetMapEntry("./*/value", null));
        put("2.16.840.1.113883.10.20.28.3.59", new ValueSetMapEntry("./*/value", null));
        put("2.16.840.1.113883.10.20.28.3.55", new ValueSetMapEntry("./*/value", null));
        put("2.16.840.1.113883.10.20.28.3.86", new ValueSetMapEntry("./*/value", null));
        put("2.16.840.1.113883.10.20.28.3.61", new ValueSetMapEntry("./*/value", null));
        put("2.16.840.1.113883.10.20.28.3.62", new ValueSetMapEntry("./*/value", "./*/outboundRelationship[@typeCode='REFR']//code[@code='394617004']/../value"));
        put("2.16.840.1.113883.10.20.28.3.63", new ValueSetMapEntry("./*/value", null));
        put("2.16.840.1.113883.10.20.28.3.64", new ValueSetMapEntry("./*/outboundRelationship[@typeCode='CAUS' and @inversionInd='true']/procedureCriteria/code", null));
        put("2.16.840.1.113883.10.20.28.3.65", new ValueSetMapEntry("./*/outboundRelationship[@typeCode='CAUS' and @inversionInd='true']/procedureCriteria/code", null));
        put("2.16.840.1.113883.10.20.28.3.66", new ValueSetMapEntry("./*/code", null));
        put("2.16.840.1.113883.10.20.28.3.67", new ValueSetMapEntry("./*/code", "./*/outboundRelationship[@typeCode='REFR']//code[@code='394617004']/../value"));
        put("2.16.840.1.113883.10.20.28.3.68", new ValueSetMapEntry("./*/code", null));
        put("2.16.840.1.113883.10.20.28.3.70", new ValueSetMapEntry("./*/value", null));
        put("2.16.840.1.113883.10.20.28.3.71", new ValueSetMapEntry("./*/participation/role[@classCode='ASSIGNED']/playingDevice[@classCode='DEV' and @determinerCode='KIND']/code", null));
        put("2.16.840.1.113883.10.20.28.3.87", new ValueSetMapEntry("./*/value", null));
        put("2.16.840.1.113883.10.20.28.3.72", new ValueSetMapEntry("./*/code", "./*/value"));
        put("2.16.840.1.113883.10.20.28.3.93", new ValueSetMapEntry("./*/value", null));
        put("2.16.840.1.113883.10.20.28.3.73", new ValueSetMapEntry("./*/participation[@typeCode='CSM']/role[@classCode='ADMM']/playingMaterial[@classCode='MAT' and @determinerCode='KIND']/code", null));
        put("2.16.840.1.113883.10.20.28.3.74", new ValueSetMapEntry("./*/participation[@typeCode='CSM']/role[@classCode='ADMM']/playingMaterial[@classCode='MAT' and @determinerCode='KIND']/code", null));
        put("2.16.840.1.113883.10.20.28.3.75", new ValueSetMapEntry("./*/participation[@typeCode='CSM']/role[@classCode='ADMM']/playingMaterial[@classCode='MAT' and @determinerCode='KIND']/code", null));
        put("2.16.840.1.113883.10.20.28.3.76", new ValueSetMapEntry("./*/participation[@typeCode='CSM']/role[@classCode='ADMM']/playingMaterial[@classCode='MAT' and @determinerCode='KIND']/code", null));
        put("2.16.840.1.113883.10.20.28.3.77", new ValueSetMapEntry("./*/participation[@typeCode='CSM']/role[@classCode='ADMM']/playingMaterial[@classCode='MAT' and @determinerCode='KIND']/code", null));
        put("2.16.840.1.113883.10.20.28.3.78", new ValueSetMapEntry("./*/participation[@typeCode='CSM']/role[@classCode='MANU']/playingMaterial[@classCode='MMAT' and @determinerCode='KIND']/code", null));
        put("2.16.840.1.113883.10.20.28.3.79", new ValueSetMapEntry("./*/value", null));
        put("2.16.840.1.113883.10.20.28.3.80", new ValueSetMapEntry("./*/value", null));
        put("2.16.840.1.113883.10.20.28.3.81", new ValueSetMapEntry("./*/value", null));
        put("2.16.840.1.113883.10.20.28.3.82", new ValueSetMapEntry("./*/value", null));
        put("2.16.840.1.113883.10.20.28.3.84", new ValueSetMapEntry("./*/participation[@typeCode='ORG']/role[@classCode='LOCE']/code", null));
        put("2.16.840.1.113883.10.20.28.3.85", new ValueSetMapEntry("./*/participation[@typeCode='ORG']/role[@classCode='LOCE']/code", null));
        put("2.16.840.1.113883.10.20.28.3.110", new ValueSetMapEntry("./*/value", null));
        put("2.16.840.1.113883.10.20.28.3.111", new ValueSetMapEntry("./*/value", null));
        put("2.16.840.1.113883.10.20.28.3.112", new ValueSetMapEntry("./*/participation[@typeCode='CSM']/role/playingManufacturedMaterial[@classCode='MMAT']/code", null));
        put("2.16.840.1.113883.10.20.28.3.113", new ValueSetMapEntry("./*/participation[@typeCode='CSM']/role/playingManufacturedMaterial[@classCode='MMAT']/code", null));
        put("2.16.840.1.113883.10.20.28.3.114", new ValueSetMapEntry("./*/participation[@typeCode='CSM']/role/playingEntity[@classCode='MMAT']/code", null));
        put("2.16.840.1.113883.10.20.28.3.115", new ValueSetMapEntry("./*/participation[@typeCode='CSM']/role/playingMaterial[@classCode='MMAT']/code", null));
        put("2.16.840.1.113883.10.20.28.3.116", new ValueSetMapEntry("./*/value", null));
        put("2.16.840.1.113883.10.20.28.3.117", new ValueSetMapEntry("./*/code", "./*/value"));
        put("2.16.840.1.113883.10.20.28.3.118", new ValueSetMapEntry("./*/code", null));
    }};

    public static ValueSetMapEntry getMappingForTemplate(String template) {
        return VALUESET_MAP.get(template);
    }
}
