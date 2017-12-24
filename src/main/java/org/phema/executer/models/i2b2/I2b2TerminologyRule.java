package org.phema.executer.models.i2b2;

/**
 * Created by Luke Rasmussen on 12/22/17.
 */
public class I2b2TerminologyRule {
    private String sourceTerminologyName;
    private String destinationTerminologyPrefix;
    private String destinationTerminologyDelimiter;
    private String destinationCodeMatch;
    private String destinationCodeReplace;

    public I2b2TerminologyRule() {
    }

    public I2b2TerminologyRule(String sourceTerminologyName, String destinationTerminologyPrefix, String destinationTerminologyDelimiter,
                               String destinationCodeMatch, String destinationCodeReplace) {
        this.sourceTerminologyName = sourceTerminologyName;
        this.destinationTerminologyPrefix = destinationTerminologyPrefix;
        this.destinationTerminologyDelimiter = destinationTerminologyDelimiter;
        this.destinationCodeMatch = destinationCodeMatch;
        this.destinationCodeReplace = destinationCodeReplace;
    }
}
