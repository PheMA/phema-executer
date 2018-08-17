# i2b2 Terminology Mapping Rules

1. [Introduction](#introduction)
2. [Terminology Mapping](#terminology-mapping)
	1. [Translate Terminology Name](#translate-terminology-name)
	2. [Change the Delimiter](#change-delimiter)
	3. [Translate Code Value](#translate-code-value)
	4. [Filter i2b2 Path](#filter-i2b2-path)
	5. [Multiple Rules](#multiple-rules)
3. [Example Configuration](#example-configuration)

## Introduction <a name="introduction"></a>

In order to map from value set terms (which contain a code system and a code value) to the appropriate i2b2 basecode,
often times some manipulation is needed.  The default ontology that comes with i2b2, for example, typically combines
the code system, a delimiter (a colon) and the code into a single string.

For example, in the default ontology that comes with i2b2, the ICD10-CM code G21.1 would have an i2b2 basecode of:
``ICD10:G21.1``

Note that in this case, the code system is recorded as "ICD10", although value sets from the VSAC will use the more
complete form "ICD10CM".

When looking at writing rules, the default behavior is to take the exact terminology name from the value set, append a colon delimiter, and append the exact code value from the value set definition.  If your i2b2 instance is already configured this way, you do not need to define any
mapping rules.

If not, several mapping rules can be applied to account for this, which is described in the sections below.

## Terminology Mapping <a name="terminology-mapping"></a>

### Translate Terminology Name <a name="translate-terminology-name"></a>
For the example just presented, we always want to translate an ICD10-CM code to use the prefix "ICD10".
We can define a single terminology rule entry (note that the terminologyRules entry is an array of values, although
you are free to omit it or only have one entry in the array) to account for this.

Here we define that any time a value set uses "ICD10CM" for the terminology we want to convert it to "ICD10".  The rule
would be written as:

```
terminologyRules = [
  # ICD10CM G21.1 --> ICD10:G21.1
  {
    sourceTerminologyName = "ICD10CM"
    destinationTerminology {
        prefix = "ICD10"
    }
  }
]
```

### Change the Delimiter <a name="change-delimiter"></a>
Of course, it's not always that simple.  Let's expand on this example and say that we chose to use a different delimiter
(a pipe) between our terminology name and code.  So in our i2b2 instance, the ICD10-CM code G21.1 would be `` ICD10|G21.1 ``

We can expand our rule to include a new delimiter to use.  Note that we didn't specify the delimiter in the previous rule.
If it doesn't exist, the default is to use a colon.

```
terminologyRules = [
  # ICD10CM G21.1 --> ICD10|G21.1
  {
    sourceTerminologyName = "ICD10CM"
    destinationTerminology {
        prefix = "ICD10"
        delimiter = "|"
    }
  }
]
```

If your i2b2 instance doesn't use a delimiter, you could write the same rule but just leave the delimiter value as a blank string ("")

### Translate Code Value <a name="translate-code-value"></a>
The next part of a terminology rule that we can define is how to process the code value.  If this part of the rule is not
explicitly specified, the default is to just use the code from the value set definition.

Building on our example again, let's say that in our i2b2 instance we remove the period in ICD10 and ICD9 codes.  So for the ICD-10 code G21.1, we
want to get a code like `` ICD10|G211 ``.

We can do this with regular expressions.  We want to capture the first part of the code (before the period) and the second
part (after the period) and then concatenate them with the period removed.  One way to represent this would be with the
regex ``([^\.]*)\.(.*)``.

You'll notice that two capture groups surround the period.  In order to build our new code value, we define the replacement
value as the concatenated version of those two capture groups: ``$1$2``

Written as a rule, this would look like the following (**NOTE** you must escape reserved characters like backslashes in your regex.  So ``\.`` becomes ``\\.`` when written out in the rule):

```
terminologyRules = [
  # ICD10-CM G21.1 --> ICD10|G211
  {
    sourceTerminologyName = "ICD10CM"
    destinationTerminology {
        prefix = "ICD10"
        delimiter = "|"
        codeReplace {
            match = "([^\\.]*)\\.(.*)"
            replaceWith = "$1$2"
        }
    }
  }
]
```

### Filter i2b2 Path <a name="filter-i2b2-path"></a>
Finally, sometimes the same terms are in the i2b2 ontology, but in different paths.  This is useful when you want to have
different visual representations of the hierarchy.  However, this may also be caused by other reasons, and cause you to have
incorrect code paths listed.  Suppose in our previous example there is an invalid ontology that exists for ICD10 codes, and
it is showing up when we search based on ICD10 codes.  Our two example paths for ICD-10 codes may look like this:
```
\\i2b2_icd10\G\G21\G21.1      <-- This is the path we want
\\i2b2_icd10_old\E\G\F\G21.1  <-- We don't want this
```

The last option you can specify for the destination terminology is the i2b2 ontology path that you want to use.  This allows you to pick
a single path prefix that you will allow ontology results from, and it will ignore all others.  In our previous example, we
didn't have to specify the exact code path - we assumed (or knew) that the codes we want all start with the i2b2 ontology path of \\i2b2_icd10\.  Changing this assumption, we now know we have two i2b2 ontology paths, only one of which we want to keep.  This can be
written into our rule as follows, using the `restrictToOntologyPath` key:

```
terminologyRules = [
  # ICD10CM G21.1 --> ICD10|G211
  {
    sourceTerminologyName = "ICD10CM"
    destinationTerminology {
        prefix = "ICD10"
        delimiter = "|"
        codeReplace {
            match = "([^\.]*)\.(.*)"
            replaceWith = "$1$2"
        }
        restrictToOntologyPath = "\\\\i2b2_icd10\\"
    }
  }
]
```

Again, note that HOCON requires us to escape our backslashes, so "\\" becomes "\\\\".

Also note that because this is checking for codes that start with a substring, we need to put the backslash at the end
of our rule. Otherwise, if we wrote it as
```
restrictToOntologyPath = "\\\\i2b2_icd10"
```

It would also match `\\i2b2_icd10_old`.


### Multiple Rules <a name="multiple-rules"></a>
PhEx allows multiple mapping rules to be applied.  This can be rules across different terminologies in the value set (e.g., ICD-9, ICD-10, RxNorm), but it may also include multiple rules for the same terminology if you have the same code represented different ways in your i2b2 ontology.

Starting simple, here we show how ICD-10
and ICD-9 codes could be handled differently.

```
terminologyRules = [
  # ICD10CM G21.1 --> ICD10|G211
  {
    sourceTerminologyName = "ICD10CM"
    destinationTerminology {
        prefix = "ICD10"
        delimiter = "|"
        codeReplace {
            match = "([^\\.]*)\\.(.*)"
            replaceWith = "$1$2"
        }
    }
  }
  # ICD9CM 250.1 --> ICD250.1
  {
    sourceTerminologyName = "ICD9CM"
    destinationTerminology {
        prefix = "ICD"
        delimiter = ""
    }
  }
]
```
This would allow you to map ICD-9 and ICD-10 codes from the value set to your i2b2 ontology in slightly different ways.  Even if your i2b2 ontology uses the same general approach for how it manages codes, you will need to write a terminology mapping rule for each source terminology.

Sometimes you may have the same code represented in your i2b2 instance in multiple ways.  For example, perhaps you suffix your i2b2 basecode depending on the source system that the data was loaded from.  If we do this, our example ICD-10 code G21.1  could be represented in i2b2 with two (or more) different basecodes.  For example:

```
ICD10:G21.1-INPATIENT     # Diagnosis code from our inpatient EHR
ICD10:G21.1-OUTPATIENT    # Diagnosis code from our outpatient EHR system
```

PhEx also allows you to specify multiple rules for the same terminology in the value set.  If we want to take a single value set code (ICD-10 G21.1) and translate it into the two codes shown above, we would do that using the following two rules:

```
terminologyRules = [
  # ICD10CM G21.1 --> ICD10|G21.1-INPATIENT
  {
    sourceTerminologyName = "ICD10CM"
    destinationTerminology {
        prefix = "ICD10"
        delimiter = "|"
        codeReplace {
            match = "(.*)"
            replaceWith = "$1-INPATIENT"
        }
    }
  }
  # ICD10CM G21.1 --> ICD10|G21.1-OUTPATIENT
  {
    sourceTerminologyName = "ICD10CM"
    destinationTerminology {
        prefix = "ICD10"
        delimiter = "|"
        codeReplace {
            match = "(.*)"
            replaceWith = "$1-OUTPATIENT"
        }
    }
  }
]
```


## Example Configuration <a name="example-configuration"></a>
The following is an example of a configuration file which includes i2b2 mapping rules.

```
execution {
    phenotypeDefinition = "test.xml"

    // Can be 'counts' or 'patients'
    returnType = "counts"

    // Can be 'normal' or 'debug'
    executionMode = "optimized"

    // Can be 'i2b2' or 'ohdsi'
    engine = "i2b2"

    valueSets = [
      { type = "File", path = "test.csv", format = "CSV" }
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
        }
      ]
    }
  }
}
```