# CS4402 Practical 2 - Constraint Solver Implementation

This is a private repository to work on CS4402 Practical 2 of Constraint Programming module.

### Overview
This practical involved implementing a constraint solver supporting two algorithms,
Forward Checking (FC) and Maintaining Arc Consistency (MAC). Given the number
of problems translated as binary constraints, the solver is designed to be capable of
executing the two efficiently with good scalability. The efficient OO design is sought
with a test environment for empirical evaluation. The two methods are compared in
terms of performance metrics. Beyond the basic requirement the project is extended to
support various types of heuristics for variable and value assignment ordering with their
implication discussed in their relative performance improvement. Additional problems are
implemented for further analysis.

### Instructions
The test suite is designed as a Maven project where source code and JUnit tests are
located in relevant directories. Commands can be run on Terminal at the directory runner
where “pom.xml” is located. Following commands will be used mainly to execute the
tests.
````
mvn clean
````
Removes the current compiled class and output files for a new build.
````
mvn clean test [-Dtest {SolverTest}] [-Dprint {Y,N}]
````
Runs the solver for all 10 problems sources in the specification using both FC
and MAC under the default variable and value ordering as specified for the basic solver. 
This takes around 20 seconds to execute including the build time (16 solver runs).
For testing other problem instances other than those available in *src/test/resources*,
create a new directory or use others as mentioned above, containing those files under
*src/test* and pass the directory name as following: (“extra” directory is an example of
this.)
````
mvn clean test [-Dtest {SolverTest}] [-Ddir DIR_NAME] [-Dprint {Y, N}]

Description

optional arguments:
  -Dprint Y             generates csv statistics output (with no solution), with every problem output as
                        a row entry (includes headers: problem name, type of algorithm used, time taken in
                        ms and s, depth (number of nodes), extra count, type of variable heuristics, type of
                        value heuristics)
  -Dprint N             generates txt file output of statistics and solution
  -Ddir DIR_NAME        directory located under src/test where problem instances to test reside. 
                        (default is src/test/resources)
````
As an extension, other types of heuristics are implemented and these can be using the
following test class. (would recommend also reading empirical evaluation and extension
section prior to the usage).

````
 mvn clean test [-Dtest {AdvSolverTest}] [-Dorder {all, allVal, basic, def (dynamic)}] [-Dm {b, fc, mac}]
````
As with previous test class data directory can be customised to solve new instances, so
does print type which can be adjusted using *-Dprint* argument. *-Dorder* is the important
argument to use. The test class can run with 3 settings.

#### Additional Arguments
````
-Dorder={all, allVal, basic, def (dynamic)}
````
| all | def/dynamic/none | allVal | basic |
|-----|------------------|--------|-------|
| will run under all 5 variable order heuristics, both static and dynamic, and 2 value order heuristics. (includes 2 static variable ordering) | will run under all 3 dynamic variable order heuristics with 1 value order heuristics. (default)    | will run under 3 dynamic variable order heuristics with 2 value order heuristics.  | same as SolverTest |

Both *SolverTest* and *AdvSolverTest* support additional optional argument *-Dm=<type>*
where you can specify to run only with one algorithm type, as the default set up is to use
each one in solving the given problems.
  
````
-Dm {b, fc, mac}
````

| b | fc | mac |
|-------|----------|-------|
| to run on both (can be omitted for being a default option) | to run only with Forward Checking     | to run only with Maintaining Arc Consistency  |
