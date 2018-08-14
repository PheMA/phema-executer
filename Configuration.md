# ConfigurationIn order to execute a phenotype, a folder containing the phenotype definition (logic, value sets) also requires a configuration file.  The following is an example configuration file, and each of the sub-sections and individual elements are detailed below.

```
execution {
  phenotypeDefinition = "phema-test.xml"

  // Can be 'counts' or 'patients'
  returnType = "counts"

  // Can be 'normal' or 'debug'
  executionMode = "normal"

  // Can be 'i2b2' or 'ohdsi'
  engine = "i2b2"

  valueSets = [
    { type = "File", path = "phema-test.csv", format = "CSV" }
  ]

  trustAllSsl = true

  i2b2 {
    projectManagementUrl = "http://127.0.0.1:9090/i2b2/services/PMService/"
    domain = "i2b2demo"
    project = "Demo"
    login = "demo"
    password = "demouser"

    queryPrefix = "Test"

    // Whether or not you want to wait for each sub-query to complete before continuing with
    // the next sub-query.  This will reduce load on your i2b2 server (especially if there are
    // several long-running queries), but will increase execution time.
    waitForEachQueryPart = true

    // The mapping rules provide general strategies to create a map from the value set
    // term and the i2b2 ontology term
    valueSetMapping {
      // Rules defined for a terminology will apply globally across all value sets
      terminologyRules = [
        {
          sourceTerminologyName = "ICD10CM"
          destinationTerminology {
            prefix = "ICD10"
          }
        },
        {
          sourceTerminologyName = "Age"
          destinationTerminology {
            prefix = "DEM|AGE"
            delimiter = ":"
            restrictToOntologyPath = "\\\\i2b2_DEMO\\i2b2\\Demographics"
          }
        }

      ]
    }
  }
}
```## Phenotype Configuration
* `phenotypeDefinition` - this is the relative path to the file containing the phenotype logic.  Currently this is an HQMF XML file, but will be expanded to include CQL in the future. The path is relative to the configuration file's location.  The typical convention is to have all files (configuration, logic and value set) within the same directory, so this is usually just expressed as the file name.* `returnType` – this defines what you expect the result of the execution to be - either a count of patients (`"counts"`), or a list of patient identifiers (`"patients"`).  Currently only patient counts are supported. 
* `executionMode` - this controls the amount of analysis that the executer does as it is running your phenotype definition.  If your phenotype definition is returning unexpected results (e.g., too low or too high), you can set this to `"debug"` to enable additional logging.  Within the same directory as the configuration file, a folder named "phema-logging" will be created.  The default for this is `"normal"`, which will perform a pre-defined subset of analytics as the phenotype is run.
* `engine` - specifies the endpoint that you will be running against.  Currently only i2b2 (`"i2b2"`) is supported, but support for OHDSI (`"ohdsi"`) is underway.
* `trustAllSsl` = a boolean flag (`true` or `false`), which controls how connections to HTTPS URLs (such as for the i2b2 API calls) are trusted.  If this is set to `false`, you may receive connection errors if the server you are connecting to over HTTPS uses a certificate that is not stored within your Java key store.  You will need to add those certificates then for the executer to run.  If you are sure of the integrity of the endpoints, you may set this to `true` and avoid the additional certificate management steps.
* `valueSets` – see sub-section below.  This represents a list of 1 or more value set endpoints to be used for the phenotype.* Execution Engine Configuration – the specific configuration details needed for the execution engine to operate.  Additional details are provided in the Execution Engine sub-section below for the engines supported by PhEx.  Other engines may be added, with their own configuration details.## Value SetsAll value sets should be represented in a local file that can then be loaded.  A configuration option allows for other types of value set repositories, but for now only file-based repositories are supported.* File Path – the path of the file containing the value set definitions.* Format – how the value set definitions are structured.  Allowed values include (TBD – will include stuff like CVS and Excel files for CTS2, VSAC, etc.)## Execution Engine### I2B2* Project Management URL - the URL of the i2b2 Project Management (PM) cell* Login - a login in the i2b2 system that has permissions to execute queries* Password - the password for the i2b2 account* Project - the name of the i2b2 project to run against* Domain - the i2b2 domain to run against* Value Set Mapping - the rules for mapping value set terms into i2b2 concepts (described more below)#### I2B2 Value Set Mapping RulesValue set mapping rules are processed in the following order.  If more than one mapping rule applies toa value set or term, the result of the last rule will be used.  A more comprehensive review of the mappingrules and examples are available.##### Terminology Rules* Source Terminology Name - the name of the terminology from the value set.  This must be an exact match.* Destination Terminology - a structure containing the rules to map the source terminology terms.  * Prefix - the i2b2 terminology prefix that's used in the basecode. If not specified, it will use the Source Terminology Name.  * Delimiter - the delimiter that appears between the prefix and the code in the basecode.  If not specified, it defaults to a colon (":").  * Code Replace - an optional rule for how to manipulate the term code to the way it's represented in i2b2.  If not specified, it uses      the exact code from the value set.  Otherwise, it uses a regular expression replacement.     * Match - a regular expression to match against, including capture groups.     * Replace - the replacement rule to use, including capture groups.### OHDSI[ TODO ]