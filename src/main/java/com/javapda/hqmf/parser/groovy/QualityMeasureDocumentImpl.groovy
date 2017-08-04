package com.javapda.hqmf.parser.groovy;
import com.javapda.hqmf.QualityMeasureDocument;

import groovy.util.slurpersupport.GPathResult

public class QualityMeasureDocumentImpl implements QualityMeasureDocument {
   GPathResult qrdadoc
   public QualityMeasureDocumentImpl(String xml) {
       qrdadoc = new XmlSlurper().parseText(xml)
      
   }
   public String toString() {
      """
        description: ${this.description}
        measureType: ${measureType}
        measureIdentifier: ${measureIdentifier}
        nqf: ${nqf}
        versionNumber: ${versionNumber}
        versionSpecificIdentifier: ${versionSpecificIdentifier}
        versionNeutralIdentifier: ${versionNeutralIdentifier}
        codeCode: ${codeCode}
        codeDisplayName: ${codeDisplayName}
        numeratorGuid: ${numeratorGuid}
        denominatorGuid: ${denominatorGuid}
        denominatorExclusionsGuid: ${denominatorExclusionsGuid}
        denominatorExceptionsGuid: ${denominatorExceptionsGuid}
		initialPatientPopulationGuid: ${initialPatientPopulationGuid}
      """
   }
   @Override
   public String getMeasureType() {
          for (subjectOf in qrdadoc.subjectOf) {
           if (subjectOf.measureAttribute.code.@code == "MSRSCORE") {
			   return subjectOf.measureAttribute.value.@code;
           }
      }
      
      null
   }
   @Override
   public String getMeasureIdentifier() {
    
          for (subjectOf in qrdadoc.subjectOf) {
           if (subjectOf.measureAttribute.code.@nullFlavor == "OTH") {
             if (subjectOf.measureAttribute.code.originalText.text() == "eMeasure Identifier") {
               return subjectOf.measureAttribute.value.text()
             }
           }
      }
      
      null
   }
   @Override
   public String getInitialPatientPopulationGuid() {
          for (entry in qrdadoc.component.section.entry) {
              if(entry.observation.value.@code == "IPP" && entry.observation.@actionNegationInd!="true") {
                return entry.observation.id.@root
              }
          }
          null

   }
   @Override
   public String getDenominatorGuid() {
          for (entry in qrdadoc.component.section.entry) {
              if(entry.observation.value.@code == "DENOM" && entry.observation.@actionNegationInd!="true") {
                return entry.observation.id.@root
              }
          }
          null

   }
   @Override
   public String getDenominatorExceptionsGuid() {
          for (entry in qrdadoc.component.section.entry) {
              if(entry.observation.value.@code == "DENEXCEP") {
                return entry.observation.id.@root
              }
          }
          null

   }
   @Override
   public String getDenominatorExclusionsGuid() {
          for (entry in qrdadoc.component.section.entry) {
              if(entry.observation.value.@code == "DENOM" && entry.observation.@actionNegationInd=="true") {
                return entry.observation.id.@root
              }
          }
          null

   }
   @Override
   public String getNumeratorGuid() {
          for (entry in qrdadoc.component.section.entry) {
              if(entry.observation.value.@code == "NUMER") {
                return entry.observation.id.@root
              }
          }
          null

   }
   // http://www.healthit.gov/sites/default/files/qdm_hqmf_templates_dec2013.pdf
   public static final String TEMPLATEID_ETHNICITY="2.16.840.1.113883.3.560.1.403"
   public static final String TEMPLATEID_RACE="2.16.840.1.113883.3.560.1.406"
   public static final String TEMPLATEID_SEX="2.16.840.1.113883.3.560.1.402"
   public static final String TEMPLATEID_PAYER="2.16.840.1.113883.3.560.1.405"
   @Override
   public String getEthnicityGuid() {
	   getGuidByTemplateId(TEMPLATEID_ETHNICITY)
   }
   @Override
   public String getRaceGuid() {
	   getGuidByTemplateId(TEMPLATEID_RACE)
   }
   @Override
   public String getSexGuid() {
	   getGuidByTemplateId(TEMPLATEID_SEX)
   }
   @Override
   public String getPayerGuid() {
	   getGuidByTemplateId(TEMPLATEID_PAYER)
   }
   private String getGuidByTemplateId(templateId) {
	   for (entry in qrdadoc.component.section.entry) {
		   if(templateId.equals(""+entry.act.templateId.@root)) {
			   return entry.act.id.@root
		   }
	   }

   }
   public String traverseEntries() {
    println "traverseEntries"
          for (entry in qrdadoc.component.section.entry) {
              println entry
          }
   }
   @Override
   public String getDescription() {
      qrdadoc.title.text()
   }
   @Override
   public String getVersionNumber() {
      qrdadoc.versionNumber.@value
   }
   @Override
   public String getVersionSpecificIdentifier() {
      qrdadoc.id.@root
   }
   @Override
   public String getVersionNeutralIdentifier() {
      qrdadoc.setId.@root
   }
   public String getCodeCode() {
      qrdadoc.code.@code
   }
   public String getCodeDisplayName() {
      qrdadoc.code.@displayName
   }
   public int getNumSubjectOf() {
      qrdadoc.subjectOf.size()
   }
   public int getCrs() {
      for (subjectOf in qrdadoc.subjectOf) {
           if (subjectOf.measureAttribute.code.@code == "CRS") {
               return 23535
           }

      }
      return -243 

   }
   @Override
   public String getSupplementalFields() {
      for (subjectOf in qrdadoc.subjectOf) {
           if (subjectOf.measureAttribute.code.originalText.text() == "Supplemental Data Elements") {
               return subjectOf.measureAttribute.value.text()
           }
      }
	  null
   }
   
   @Override
   public String getNqf() {
      for (subjectOf in qrdadoc.subjectOf) {
           if (subjectOf.measureAttribute.value.@root == "2.16.840.1.113883.3.560.1") {
               return subjectOf.measureAttribute.value.@extension
           }
      }

      null
   }


   public static usage() {
      println """
        Usage:
           groovy QualityMeasureDocument.groovy <xmlfilename>
      """
   }
   public static void abort(String msg) {
      println "ABORT: ${msg}"
      System.exit(-1)
   }
   public static void main(args) {
       args=[
         "xmlfiles/CMS129v4.xml",
         "xmlfiles/CMS69v3.xml",
       ]
       if (args.length==0) {
          usage()
          abort "Missing command-line arg eMeasureXmlFileName"
       }
       if (!new File(args[0]).exists()) {
          abort "File ${args[0]} does not exist"
       }
       String filename = args[0]
       String fileContents = new File(filename).getText('UTF-8')

       def qmd = new QualityMeasureDocumentImpl(fileContents)
       println qmd
   }

}
