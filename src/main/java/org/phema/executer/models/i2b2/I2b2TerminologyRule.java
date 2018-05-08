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
    private String restrictToOntologyPath;

    public I2b2TerminologyRule() {
    }

    public I2b2TerminologyRule(String sourceTerminologyName, String destinationTerminologyPrefix, String destinationTerminologyDelimiter,
                               String destinationCodeMatch, String destinationCodeReplace, String restrictToOntologyPath) {
        this.sourceTerminologyName = sourceTerminologyName;
        this.destinationTerminologyPrefix = destinationTerminologyPrefix;
        this.destinationTerminologyDelimiter = destinationTerminologyDelimiter;
        this.destinationCodeMatch = destinationCodeMatch;
        this.destinationCodeReplace = destinationCodeReplace;
        this.restrictToOntologyPath = restrictToOntologyPath;
    }

    public String getSourceTerminologyName() {
        return sourceTerminologyName;
    }

    public void setSourceTerminologyName(String sourceTerminologyName) {
        this.sourceTerminologyName = sourceTerminologyName;
    }

    public String getDestinationTerminologyPrefix() {
        return destinationTerminologyPrefix;
    }

    public void setDestinationTerminologyPrefix(String destinationTerminologyPrefix) {
        this.destinationTerminologyPrefix = destinationTerminologyPrefix;
    }

    public String getDestinationTerminologyDelimiter() {
        return destinationTerminologyDelimiter;
    }

    public void setDestinationTerminologyDelimiter(String destinationTerminologyDelimiter) {
        this.destinationTerminologyDelimiter = destinationTerminologyDelimiter;
    }

    public String getDestinationCodeMatch() {
        return destinationCodeMatch;
    }

    public void setDestinationCodeMatch(String destinationCodeMatch) {
        this.destinationCodeMatch = destinationCodeMatch;
    }

    public String getDestinationCodeReplace() {
        return destinationCodeReplace;
    }

    public void setDestinationCodeReplace(String destinationCodeReplace) {
        this.destinationCodeReplace = destinationCodeReplace;
    }

    public String getRestrictToOntologyPath() {
        return restrictToOntologyPath;
    }

    public void setRestrictToOntologyPath(String restrictToOntologyPath) {
        this.restrictToOntologyPath = restrictToOntologyPath;
    }
}
