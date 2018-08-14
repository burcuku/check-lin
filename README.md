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

- Python 2.7


## Usage
###Creating strong *d*-hitting schedules:

You can construct the family of schedules for a given history file or a directory of history files. The program generates a Java class for each file, where each method runs a schedule in the family. 

- The produced Java class files will be saved in `produced` directory. 
- The statistics (e.g., the number of operations, number of schedules in the family, etc) will be recorded in `stats` directory.

**Processing a single history file:**

Generate a test class for a sample history file by specifying the input json file, the name for the Java class to be generated,
the name for the stat file to be generated and the depth of d-hitting families:

    sbt "run example/historyArrayBlockingQueue.json TestABQ StatABQ 2"
    
This will produce `TestLQ.java` inside `produced/testD2` directory.
You can compile and run this file to check the history in `historyLinkedQueue.json` is linearizable by a *2*-hitting schedule.
    
**Processing a directory of history files:**

You can also test linearizability of a directory of history files for a range of depths for d-hitting families by running the following scripts.
As an example, you can decompress the `histories.zip` file in the `example` directory and generate tests for the files inside decompressed folder.

    python scripts/createTests.py example/histories/ArrayBlockingQueue TestABQ StatABQ 2 3

This will produce tests in the `produced` directory for each history file for each depth *d* in the range.

###Running strong *d*-hitting schedules:

Compile and run test classes in a directory by specifying the input directory name of test files and an output directory name for the output files

    python scripts/compileTests.py produced ABQ
    python scripts/runTests.py produced ABQ

This will produce the linearizability output files in the directory `out`.

### (Optional) Processing stats:

You can process the statistics files (generated while processing histories and constructing strong *d*-hitting family of schedules) by using the following command:

	python scripts/processStats.py stats/StatABQ

## (Optional) Configuration 

- Using the configuration in `test.conf`, you can provide parameters for:

   - Linearizability checking tests - such as the input history file, the depth of the hitting-family of schedules, etc.

   - Auto-generated test class - such as the class name, variable names, etc.