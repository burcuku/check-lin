# Checking Linearizability using Hitting Families

Linearizability is a key correctness property for concurrent data types. Linearizability requires that the behavior of concurrently invoked operations of the data type be equivalent to the behavior in an execution where each operation takes effect at an instantaneous point of time between its invocation and return. Given an execution trace of operations, the problem of verifying its linearizability is NP-complete, and current exhaustive search tools scale poorly.

We empirically show that linearizability of an execution trace is often witnessed by a schedule that orders only a small number of operations (the "linearizability depth") in a specific way, independently of other operations. Accordingly, one can structure the search for linearizability witnesses by exploring schedules of low linearizability depth first. We provide such an algorithm. Key to our algorithm is a procedure to generate a strongly *d*-hitting family of schedules, which is guaranteed to cover all linearizability witnesses of depth *d*. A strongly *d*-hitting family of schedules of an execution trace consists of a set of schedules, such that for each tuple of *d* operations in the trace, there is a schedule in the family that (i) executes these operations in the order they appear in the tuple, and (ii) as late as possible in the execution.

We show that most linearizable execution traces from existing benchmarks can be witnessed by strongly *d*-hitting schedules for *d <= 5*. Our result suggests a practical and automated method for showing linearizability of a trace based on a prioritization of schedules parameterized by the linearizability depth. 

# Strong d-Hitting Linearizability Checker

Given an execution history together with the outcomes of its operations, checks if the history is *d*-linearizable by enumerating strong *d*-hitting family of schedules of its operations.

## Contents

- Schedule Generator in `lin/scheduleGen`
    - Generates strong d-hitting family of schedules given a history json file.
    The history file format is the same as the histories produced by the linearizability tests in [Violat](https://github.com/michael-emmi/violat/).

- Test Generator in `lin/testGen`
	- Takes a set of schedules and expected results of the operations as inputs
    - Generates a test class which has a test method for each schedule 
    - Each test method compares the obtained results with the expected results 
    
## Requirements

- Scala 2.12

- Scala Build Tool

- Python 3.6

## Reproducing the results on the example data tests 

The experimental results check the linearizability of the history files provided in ```example/histories``` folder.

The following command checks the linearizanbility of all the history files for the data structures *ABQ*, *CHM*, *CLD*, *CLQ*, *CSLM*, *LBD*, *LBQ*, *LTQ* and *PBQ* in ```example/histories``` folder.

```
python scripts/main.py
```

The linearizability checking results are collected in the ```results``` folder.

Alternatively, the history files for only a single data structure can be checked for linearizability by providing the shortname for the data structure:

```
python scripts/main.py ABQ
```
The short name for the data structure can be one of *ABQ*, *CHM*, *CLD*, *CLQ*, *CSLM*, *LBD*, *LBQ*, *LTQ* or *PBQ*.

## Running custom history files
### Creating strong *d*-hitting schedules:

Construct strong *d*-hitting schedules by using the script ```scripts/tools/createTests```.

This will produce .java source files in the `produced` directory. These .java files invoke strong hitting family of schedules of operations in the given history.  

### Running strong *d*-hitting schedules:

Compile and run the produced test classes in a directory by using the scripts ```scripts/tools/compileTests.py``` and ```scripts/tools/runTests.py```


