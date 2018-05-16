# PhEMA Executer

## Building
The PhEMA Executer was built using Java 1.8, and can be built either from the command line using Maven:

`mvn clean compile assembly:single`

This will create a self-contained JAR with all dependencies in the ./target directory.


Or can be built and packaged using IntelliJ IDEA:

`Build -> Build Artifacts... -> phema-executer-lib:jar -> Build`

This will create a self-contained JAR with all dependencies in the ./out directory.

## Running a Phenotype
The PhEMA Executer is developed as a command-line utility.  A user interface that links to the library is
under development.  To run a phenotype from the command line, download a phenotype definition from the PhEMA
Authoring Tool.  You will need the following:
* Configuration file (described more below)
* Phenotype logic definition (HQMF XML document)
* CSV containing value set definitions

The easiest way to prepare these files is to have them all in their own directory.  You then execute the JAR file
from the command line, and specify the relative or absolute path to the config file that contains the phenotype definition.

Example:

`java -jar phema-executer-lib.jar ./phema-test/phema-test.conf`

## Configuration
The Executer uses Typesafe, (specifically [HOCON](https://github.com/lightbend/config#using-hocon-the-json-superset)) for specifying configuration parameters.

An example i2b2 configuration file is as follows:
```
execution {
  // Path to the HQMF XML document, relative to the configuration file
  phenotypeDefinition = "phema-test.xml"

  // Currently only "counts" is supported
  returnType = "counts"

  // Currently only "optimized" is supported
  executionMode = "optimized"

  // Currently only "i2b2" is supported
  engine = "i2b2"

  // A list of value set files (the path being relative to this configuration file)
  // used by the phenotype
  valueSets = [
    { type = "File", path = "phema-bph-use-case.csv", format = "CSV" }
  ]

  // Set to "true" if you trust any HTTPS calls that you are making to the i2b2 server.  If
  // set to "false", you may need to import SSL certificates into your local Java keystore.
  trustAllSsl = true

  i2b2 {
    projectManagementUrl = "http://1.2.3.4:9090/i2b2/services/PMService/"
    domain = "i2b2demo"
    project = "Demo"
    login = "demo"
    password = "demouser"

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
          sourceTerminologyName = "ICD9CM"
          destinationTerminology {
            prefix = "ICD9"
          }
        },
        {
          sourceTerminologyName = "AdministrativeGender"
          destinationTerminology {
            prefix = "DEM|SEX"
            delimiter = ":"
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
```

## Acknowledgements
This work has been funded by NIGMS grant R01GM105688.