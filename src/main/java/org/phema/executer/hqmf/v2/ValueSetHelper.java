package org.phema.executer.hqmf.v2;

import java.util.HashMap;

/**
 * Created by Luke Rasmussen on 9/21/17.
 */
public class ValueSetHelper {
    private static final HashMap<String, ValueSetMapEntry> VALUESET_MAP = new HashMap<String, ValueSetMapEntry>() {{
        put("2.16.840.1.113883.10.20.28.3.7", new ValueSetMapEntry("./*/cda:value", null));
        put("2.16.840.1.113883.10.20.28.3.8", new ValueSetMapEntry("./*/cda:code", null));
        put("2.16.840.1.113883.10.20.28.3.9", new ValueSetMapEntry("./*/cda:code", null));
        put("2.16.840.1.113883.10.20.28.3.10", new ValueSetMapEntry("./*/cda:code", null));
        put("2.16.840.1.113883.10.20.28.3.11", new ValueSetMapEntry("./*/cda:participation[@typeCode='PRD']/cda:role[@classCode='MANU']/cda:playingDevice[@classCode='DEV']/cda:code", null));
        put("2.16.840.1.113883.10.20.28.3.12", new ValueSetMapEntry("./*/cda:participation[@typeCode='PRD']/cda:role[@classCode='MANU']/cda:playingDevice[@classCode='DEV']/cda:code", null));
        put("2.16.840.1.113883.10.20.28.3.13", new ValueSetMapEntry("./*/cda:participation[@typeCode='DEV']/cda:role[@classCode='MANU']/cda:playingDevice[@classCode='DEV']/cda:code", null));
        put("2.16.840.1.113883.10.20.28.3.14", new ValueSetMapEntry("./*/cda:participation[@typeCode='PRD']/cda:role[@classCode='MANU']/cda:playingDevice[@classCode='DEV']/cda:code", null));
        put("2.16.840.1.113883.10.20.28.3.15", new ValueSetMapEntry("./*/cda:participation[@typeCode='DEV']/cda:role[@classCode='MANU']/cda:playingDevice[@classCode='DEV']/cda:code", null));
        put("2.16.840.1.113883.10.20.28.3.16", new ValueSetMapEntry("./*/cda:participation[@typeCode='DEV']/cda:role[@classCode='MANU']/cda:playingDevice[@classCode='DEV']/cda:code", null));
        put("2.16.840.1.113883.10.20.28.3.1", new ValueSetMapEntry("./*/cda:value", null));
        put("2.16.840.1.113883.10.20.28.3.17", new ValueSetMapEntry("./*/cda:value", null));
        put("2.16.840.1.113883.10.20.28.3.18", new ValueSetMapEntry("./*/cda:value", null));
        put("2.16.840.1.113883.10.20.28.3.19", new ValueSetMapEntry("./*/cda:value", null));
        put("2.16.840.1.113883.10.20.28.3.20", new ValueSetMapEntry("./*/cda:outboundRelationship[@typeCode='CAUS']/cda:observationCriteria/cda:code", null));
        put("2.16.840.1.113883.10.20.28.3.21", new ValueSetMapEntry("./*/cda:outboundRelationship[@typeCode='CAUS']/cda:observationCriteria/cda:code", null));
        put("2.16.840.1.113883.10.20.28.3.22", new ValueSetMapEntry("./*/cda:code", null));
        put("2.16.840.1.113883.10.20.28.3.23", new ValueSetMapEntry("./*/cda:code", "./*/cda:value"));
        put("2.16.840.1.113883.10.20.28.3.24", new ValueSetMapEntry("./*/cda:code", null));
        put("2.16.840.1.113883.10.20.28.3.26", new ValueSetMapEntry("./*/cda:code", null));
        put("2.16.840.1.113883.10.20.28.3.27", new ValueSetMapEntry("./*/cda:code", null));
        put("2.16.840.1.113883.10.20.28.3.5", new ValueSetMapEntry("./*/cda:code", null));
        put("2.16.840.1.113883.10.20.28.3.28", new ValueSetMapEntry("./*/cda:code", null));
        put("2.16.840.1.113883.10.20.28.3.29", new ValueSetMapEntry("./*/cda:code", null));
        put("2.16.840.1.113883.10.20.28.3.30", new ValueSetMapEntry("./*/cda:code", "./*/cda:value"));
        put("2.16.840.1.113883.10.20.28.3.31", new ValueSetMapEntry("./*/cda:code", null));
        put("2.16.840.1.113883.10.20.28.3.33", new ValueSetMapEntry("./*/cda:outboundRelationship[@typeCode='CAUS' and @inversionInd='true']/cda:procedureCriteria/cda:code", null));
        put("2.16.840.1.113883.10.20.28.3.34", new ValueSetMapEntry("./*/cda:outboundRelationship[@typeCode='CAUS']/cda:actCriteria/cda:code", null));
        put("2.16.840.1.113883.10.20.28.3.35", new ValueSetMapEntry("./*/cda:code", null));
        put("2.16.840.1.113883.10.20.28.3.36", new ValueSetMapEntry("./*/cda:code", "./*/cda:outboundRelationship[@typeCode='REFR']//cda:code[@code='394617004']/../cda:value"));
        put("2.16.840.1.113883.10.20.28.3.37", new ValueSetMapEntry("./*/cda:code", null));
        put("2.16.840.1.113883.10.20.28.3.39", new ValueSetMapEntry("./*/cda:outboundRelationship[@typeCode='CAUS']/cda:observationCriteria/cda:code", null));
        put("2.16.840.1.113883.10.20.28.3.40", new ValueSetMapEntry("./*/cda:outboundRelationship[@typeCode='CAUS']/cda:observationCriteria/cda:code", null));
        put("2.16.840.1.113883.10.20.28.3.41", new ValueSetMapEntry("./*/cda:code", null));
        put("2.16.840.1.113883.10.20.28.3.42", new ValueSetMapEntry("./*/cda:code", "./*/cda:value"));
        put("2.16.840.1.113883.10.20.28.3.43", new ValueSetMapEntry("./*/cda:code", null));
        put("2.16.840.1.113883.10.20.28.3.44", new ValueSetMapEntry("./*/cda:participation[@typeCode='CSM']/cda:role/cda:playingMaterial[@classCode='MMAT']/cda:code", null));
        put("2.16.840.1.113883.10.20.28.3.45", new ValueSetMapEntry("./*/cda:participation[@typeCode='CSM']/cda:role[@classCode='MANU']/cda:playingManufacturedMaterial[@classCode='MMAT']/cda:code", null));
        put("2.16.840.1.113883.10.20.28.3.46", new ValueSetMapEntry("./*/cda:participation[@typeCode='CSM']/cda:role[@classCode='MANU']/cda:playingEntity[@classCode='MMAT']/cda:code", null));
        put("2.16.840.1.113883.10.20.28.3.47", new ValueSetMapEntry("./*/cda:participation[@typeCode='CSM']/cda:role[@classCode='MANU']/cda:playingEntity[@classCode='MMAT']/cda:code", null));
        put("2.16.840.1.113883.10.20.28.3.48", new ValueSetMapEntry("./*/cda:participation[@typeCode='CSM']/cda:role[@classCode='MANU']/cda:playingManufacturedMaterial[@classCode='MMAT']/cda:code", null));
        put("2.16.840.1.113883.10.20.28.3.49", new ValueSetMapEntry("./*/cda:participation[@typeCode='CSM']/cda:role[@classCode='MANU']/cda:playingMaterial[@classCode='MMAT']/cda:code", null));
        put("2.16.840.1.113883.10.20.28.3.50", new ValueSetMapEntry("./*/cda:participation[@typeCode='CSM']/cda:role[@classCode='MANU']/cda:playingMaterial[@classCode='MMAT']/cda:code", null));
        put("2.16.840.1.113883.10.20.28.3.51", new ValueSetMapEntry("./*/cda:participation[@typeCode='CSM']/cda:role[@classCode='MANU']/cda:playingMaterial[@classCode='MMAT']/cda:code", null));
        put("2.16.840.1.113883.10.20.28.3.52", new ValueSetMapEntry("./*/cda:value", null));
        put("2.16.840.1.113883.10.20.28.3.53", new ValueSetMapEntry("./*/cda:code", "./*/cda:value"));
        put("2.16.840.1.113883.10.20.28.3.6", new ValueSetMapEntry("./*/cda:value", null));
        put("2.16.840.1.113883.10.20.28.3.54", new ValueSetMapEntry(null, null));
        put("2.16.840.1.113883.10.20.28.3.56", new ValueSetMapEntry("./*/cda:value", null));
        put("2.16.840.1.113883.10.20.28.3.57", new ValueSetMapEntry("./*/cda:value", null));
        put("2.16.840.1.113883.10.20.28.3.58", new ValueSetMapEntry("./*/cda:value", null));
        put("2.16.840.1.113883.10.20.28.3.59", new ValueSetMapEntry("./*/cda:value", null));
        put("2.16.840.1.113883.10.20.28.3.55", new ValueSetMapEntry("./*/cda:value", null));
        put("2.16.840.1.113883.10.20.28.3.86", new ValueSetMapEntry("./*/cda:value", null));
        put("2.16.840.1.113883.10.20.28.3.61", new ValueSetMapEntry("./*/cda:value", null));
        put("2.16.840.1.113883.10.20.28.3.62", new ValueSetMapEntry("./*/cda:value", "./*/cda:outboundRelationship[@typeCode='REFR']//cda:code[@code='394617004']/../cda:value"));
        put("2.16.840.1.113883.10.20.28.3.63", new ValueSetMapEntry("./*/cda:value", null));
        put("2.16.840.1.113883.10.20.28.3.64", new ValueSetMapEntry("./*/cda:outboundRelationship[@typeCode='CAUS' and @inversionInd='true']/cda:procedureCriteria/cda:code", null));
        put("2.16.840.1.113883.10.20.28.3.65", new ValueSetMapEntry("./*/cda:outboundRelationship[@typeCode='CAUS' and @inversionInd='true']/cda:procedureCriteria/cda:code", null));
        put("2.16.840.1.113883.10.20.28.3.66", new ValueSetMapEntry("./*/cda:code", null));
        put("2.16.840.1.113883.10.20.28.3.67", new ValueSetMapEntry("./*/cda:code", "./*/cda:outboundRelationship[@typeCode='REFR']//cda:code[@code='394617004']/../cda:value"));
        put("2.16.840.1.113883.10.20.28.3.68", new ValueSetMapEntry("./*/cda:code", null));
        put("2.16.840.1.113883.10.20.28.3.70", new ValueSetMapEntry("./*/cda:value", null));
        put("2.16.840.1.113883.10.20.28.3.71", new ValueSetMapEntry("./*/cda:participation/cda:role[@classCode='ASSIGNED']/cda:playingDevice[@classCode='DEV' and @determinerCode='KIND']/cda:code", null));
        put("2.16.840.1.113883.10.20.28.3.87", new ValueSetMapEntry("./*/cda:value", null));
        put("2.16.840.1.113883.10.20.28.3.72", new ValueSetMapEntry("./*/cda:code", "./*/cda:value"));
        put("2.16.840.1.113883.10.20.28.3.93", new ValueSetMapEntry("./*/cda:value", null));
        put("2.16.840.1.113883.10.20.28.3.73", new ValueSetMapEntry("./*/cda:participation[@typeCode='CSM']/cda:role[@classCode='ADMM']/cda:playingMaterial[@classCode='MAT' and @determinerCode='KIND']/cda:code", null));
        put("2.16.840.1.113883.10.20.28.3.74", new ValueSetMapEntry("./*/cda:participation[@typeCode='CSM']/cda:role[@classCode='ADMM']/cda:playingMaterial[@classCode='MAT' and @determinerCode='KIND']/cda:code", null));
        put("2.16.840.1.113883.10.20.28.3.75", new ValueSetMapEntry("./*/cda:participation[@typeCode='CSM']/cda:role[@classCode='ADMM']/cda:playingMaterial[@classCode='MAT' and @determinerCode='KIND']/cda:code", null));
        put("2.16.840.1.113883.10.20.28.3.76", new ValueSetMapEntry("./*/cda:participation[@typeCode='CSM']/cda:role[@classCode='ADMM']/cda:playingMaterial[@classCode='MAT' and @determinerCode='KIND']/cda:code", null));
        put("2.16.840.1.113883.10.20.28.3.77", new ValueSetMapEntry("./*/cda:participation[@typeCode='CSM']/cda:role[@classCode='ADMM']/cda:playingMaterial[@classCode='MAT' and @determinerCode='KIND']/cda:code", null));
        put("2.16.840.1.113883.10.20.28.3.78", new ValueSetMapEntry("./*/cda:participation[@typeCode='CSM']/cda:role[@classCode='MANU']/cda:playingMaterial[@classCode='MMAT' and @determinerCode='KIND']/cda:code", null));
        put("2.16.840.1.113883.10.20.28.3.79", new ValueSetMapEntry("./*/cda:value", null));
        put("2.16.840.1.113883.10.20.28.3.80", new ValueSetMapEntry("./*/cda:value", null));
        put("2.16.840.1.113883.10.20.28.3.81", new ValueSetMapEntry("./*/cda:value", null));
        put("2.16.840.1.113883.10.20.28.3.82", new ValueSetMapEntry("./*/cda:value", null));
        put("2.16.840.1.113883.10.20.28.3.84", new ValueSetMapEntry("./*/cda:participation[@typeCode='ORG']/cda:role[@classCode='LOCE']/cda:code", null));
        put("2.16.840.1.113883.10.20.28.3.85", new ValueSetMapEntry("./*/cda:participation[@typeCode='ORG']/cda:role[@classCode='LOCE']/cda:code", null));
        put("2.16.840.1.113883.10.20.28.3.110", new ValueSetMapEntry("./*/cda:value", null));
        put("2.16.840.1.113883.10.20.28.3.111", new ValueSetMapEntry("./*/cda:value", null));
        put("2.16.840.1.113883.10.20.28.3.112", new ValueSetMapEntry("./*/cda:participation[@typeCode='CSM']/cda:role/cda:playingManufacturedMaterial[@classCode='MMAT']/cda:code", null));
        put("2.16.840.1.113883.10.20.28.3.113", new ValueSetMapEntry("./*/cda:participation[@typeCode='CSM']/cda:role/cda:playingManufacturedMaterial[@classCode='MMAT']/cda:code", null));
        put("2.16.840.1.113883.10.20.28.3.114", new ValueSetMapEntry("./*/cda:participation[@typeCode='CSM']/cda:role/cda:playingEntity[@classCode='MMAT']/cda:code", null));
        put("2.16.840.1.113883.10.20.28.3.115", new ValueSetMapEntry("./*/cda:participation[@typeCode='CSM']/cda:role/cda:playingMaterial[@classCode='MMAT']/cda:code", null));
        put("2.16.840.1.113883.10.20.28.3.116", new ValueSetMapEntry("./*/cda:value", null));
        put("2.16.840.1.113883.10.20.28.3.117", new ValueSetMapEntry("./*/cda:code", "./*/cda:value"));
        put("2.16.840.1.113883.10.20.28.3.118", new ValueSetMapEntry("./*/cda:code", null));
    }};

    public static ValueSetMapEntry getMappingForTemplate(String template) {
        return VALUESET_MAP.get(template);
    }
}
