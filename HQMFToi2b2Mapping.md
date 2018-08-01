# Mapping HQMF to i2b2

## Temporal operators
Mapping between the HQMF/QDM and i2b2 temporal constructs is as follows.  Note that while i2b2 does support a "Same Instance Of" (or called "Occurs Simultaneously With" in the temporal panel), that does not have a direct mapping to QDM/HQMF.  There are situations where it does (for example, if we are relating something to an encounter), but that requires us to have explicit knowledge what elements in an i2b2 instance relate to an event.  That's not feasible, so instead we use broader definitions that accomplish the same intent.

| HQMF Definition | i2b2 Definition |
| ----------------|-----------------|
| A Concurrent with B | A starts at the same time as B, and A ends at the same time as B |
| A During B | A starts after start of B, and A ends before end of B |