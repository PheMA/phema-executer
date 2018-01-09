# i2b2 Terminology Mapping Rules

In order to map from value set terms (which contain a code system and a code value) to the appropriate i2b2 basecode,
often times some manipulation is needed.  The default ontology that comes with i2b2, for example, typically combines
the code system, a delimiter (a colon) and the code into a single string.

For example, ICD10-CM code G21.1 would have an i2b2 basecode of:
`` ICD10:G21.1 ``

Note that in this case, the code system is recorded as "ICD10", although value sets from the VSAC will use the more
complete form "ICD10CM".

Several mapping rules may be applied to account for this.

## Terminology Mapping
For the example just presented, we always want to translate an ICD10-CM code to use the prefix "ICD10".
We can define a single terminology rule entry (note that the terminologyRules entry is an array of values, although
you are free to omit it or only have one entry in the array) to account for this.

Here we define that any time a value set uses "ICD10CM" for the terminology we want to conver it to "ICD10".  The rule
would be written as:

```
terminologyRules = [
  # ICD10-CM G21.1 --> ICD10:G21.1
  {
    sourceTerminologyName = "ICD10CM"
    destinationTerminology {
        prefix = "ICD10"
    }
  }
]
```

Of course, it's not always that simple.  Let's expand on this example and say that we chose to use a different delimiter
(a pipe) between our terminology and code.  So in our system, ICD10-CM code G21.1 would be `` ICD10|G21.1 ``

We can expand our rule to include a new delimiter to use.  Note that we didn't specify the delimiter in the previous rule.
If it doesn't exist, the default is to use a colon.

```
terminologyRules = [
  # ICD10-CM G21.1 --> ICD10|G21.1
  {
    sourceTerminologyName = "ICD10CM"
    destinationTerminology {
        prefix = "ICD10"
        delimiter = "|"
    }
  }
]
```

If you didn't use a delimiter, you could write the same rule but just leave the delimiter value as a blank string ("")

The last part of a terminology rule that we can define is how to process the code value.  If this part of the rule is not
explicitly specified, the default is to just use the code from the value set definition.

Building on our example again, let's say that in our i2b2 instance we remove the period in ICD10 and ICD9 codes.  So we
want to get a result like `` ICD10|G211 ``.

We can do this with regular expressions.  We want to capture the first part of the code (before the period) and the second
part (after the period) and then concatenate them with the period removed.  One way to represent this would be with the
regex ``([^\.]*)\.(.*)``.

You'll notice that the two capture groups surround the period.  In order to build our new code value, we define the replacement
value as the concatenated version of those two capture groups: ``$1$2``

Written as a rule, this would look like:

```
terminologyRules = [
  # ICD10-CM G21.1 --> ICD10|G211
  {
    sourceTerminologyName = "ICD10CM"
    destinationTerminology {
        prefix = "ICD10"
        delimiter = "|"
        codeReplace {
            match = "([^\.]*)\.(.*)"
            replaceWith = "$1$2"
        }
    }
  }
]
```

Since multiple rules can be defined, you can define new terminologies in the same configuration.  Here we show how ICD-10
and ICD-9 codes could be handled differently.

```
terminologyRules = [
  # ICD10-CM G21.1 --> ICD10|G211
  {
    sourceTerminologyName = "ICD10CM"
    destinationTerminology {
        prefix = "ICD10"
        delimiter = "|"
        codeReplace {
            match = "([^\.]*)\.(.*)"
            replaceWith = "$1$2"
        }
    }
  }
  # ICD9-CM 250.1 --> ICD250.1
  {
    sourceTerminologyName = "ICD9CM"
    destinationTerminology {
        prefix = "ICD"
        delimiter = ""
    }
  }
]
```

Remember that the default behavior is to take the exact terminology name, a colon delimiter and the exact code value and
concatenate them into a string.  If your i2b2 instance is already configured this way, you do not need to define any
mapping rules.


## Example Configuration
The following is an example of a configuration file which includes i2b2 mapping rules.

```
  execution {
    // Can be 'counts' or 'patients'
    returnType = "counts"

    // Can be 'optimized' or 'debug'
    executionMode = "optimized"

    // Can be 'i2b2' or 'ohdsi'
    engine = "i2b2"

    umls {
        login = ""
        password = ""
    }

    valueSets = [
      { type = "File", path = "phema-bph-use-case.csv", format = "CSV" }
    ]

    i2b2 {
        projectManagementUrl = "http://127.0.0.1:9090/i2b2/services/PMService/"
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
                    // If not specified, it will use the sourceTerminologyName
                    prefix = "ICD10CM"
                    // If not specified, it defaults to a colon (":")
                    delimiter = ":"
                    // If not specified, it uses the exact code from the value set.
                    // Otherwise, it uses a regular expression replacement.
                    // This example takes G12.2 -> G122
                    codeReplace {
                        match = "([^\\.]*)\\.(.*)"
                        replaceWith = "$1$2"
                    }
                }
              }
            ]

            // Rules defined for a value set will apply to a specific value set, as noted
            // by the OID
            valueSetRules = [
              { valueSetOid = "1.2.3.4" }
            ]

            // This file provides specific maps from a single value set term to one or more
            // i2b2 concepts.  This is run after the mapping rules (if any exist) are run, and
            // override or supplement any mappings already established.
            overrideRules = [
              { fileName = "" }
            ]
        }
    }
}
```