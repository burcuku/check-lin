# Checking Linearizability using Hitting Families

Linearizability is a key correctness property for concurrent data types. Linearizability requires that the behavior of concurrently invoked operations of the data type be equivalent to the behavior in an execution where each operation takes effect at an instantaneous point of time between its invocation and return. Given an execution trace of operations, the problem of verifying its linearizability is NP-complete, and current exhaustive search tools scale poorly.

We empirically show that linearizability of an execution trace is often witnessed by a schedule that orders only a small number of operations (the "linearizability depth") in a specific way, independently of other operations. Accordingly, one can structure the search for linearizability witnesses by exploring schedules of low linearizability depth first. We provide such an algorithm. Key to our algorithm is a procedure to generate a strongly *d*-hitting family of schedules, which is guaranteed to cover all linearizability witnesses of depth *d*. A strongly *d*-hitting family of schedules of an execution trace consists of a set of schedules, such that for each tuple of *d* operations in the trace, there is a schedule in the family that (i) executes these operations in the order they appear in the tuple, and (ii) as late as possible in the execution.

We show that most linearizable execution traces from existing benchmarks can be witnessed by strongly *d*-hitting schedules for *d <= 5*. Our result suggests a practical and automated method for showing linearizability of a trace based on a prioritization of schedules parameterized by the linearizability depth. 

# Strong Hitting Linearizability Checker

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

- Java 1.8, Scala 2.12, Scala Build Tool, Python 3.6

## Data sets

The experimental results check the linearizability of the history files provided in ```example/histories``` folder, which contains  ```ArrayBlockingQueue(ABQ)```, ```ConcurrentHashMap (CHM)```, 
```ConcurrentLinkedDeque (CLD)```, ```ConcurrentLinkedQueue (CLQ)```,  ```ConcurrentSkipListMap (CSLM)```,  ```ConcurrentSkipListSet (CSLS)```,  ```LinkedBlockingDeque (LBD)```,  ```LinkedBlockingQueue (LBQ)```, ```LinkedTransferQueue (LTQ)``` and ```PriorityBlockingQueue (PBQ)``` data structures in the  ```ava.util.concurrent``` package.  

## Reproducing the results on the example data tests 




- **Extract Data sets**:
Extract the compressed file ```example/histories.zip``` into ```example``` folder. 

- **Producing the results for all data structure histories**:


	The following command checks the linearizability of all the history files for the data structures *ABQ*, *CHM*, *CLD*, *CLQ*, *CSLM*, *CSLS*,*LBD*, *LBQ*, *LTQ* and *PBQ* in ```example/histories``` folder.

	```
	python scripts/main.py
	```

	The linearizability checking results are collected in the ```results``` folder.

	Note: It takes ~1-3 hours to run the script on for each concurrentdata structure data set, in total ~20 hours for the whole script ona machine with a 2.6 GHz Intel Core i7 Processor and 16 GBmemory,

- **Producing the results for the histories of a data structure**:

	Alternatively, the history files for only a single data structure can be checked for 	linearizability by providing the shortname for the data structure:

	```
	python scripts/main.py ABQ
	```
	The short name for the data structure can be one of *ABQ*, *CHM*, *CLD*, *CLQ*, *CSLM*, *CSLS*, *LBD*, *LBQ*, *LTQ* or *PBQ*.

- **Output folders**:
The execution of the main script produces the following output files:
	- ```produced``` contains the produced .java files each of which invoke the strong *d*-hitting family of schedules for a history file and depth parameter *d*
	- ```stat``` contains the statistics related to the processed history files and constructed strong family of schedules
	- ```out``` contains the linearizability result for each history

    The information collected in these three folders are summarized in the ```results``` folder.

- **Reading the results**: The results are summarized in the ```results``` folder which contains three files:

	- The file ```results/table1.txt``` keeps the properties of the processed history files for each data structure which is given in **Table 1** in the paper.
	- The file ```results/table2.txt``` keeps the number of schedules generated for each data structure for increasing *d* values which is given in **Table 2** in the paper.
	- The file ```results/table3.txt``` keeps the number and percentage of the linearizable history files shown by strong gitting families of schedules for increasing *d* values which is given in **Table 3** in the paper.


## Notes
Before rerunning the project (e.g. via the main script) for the same set of data sets, the files produced by an earlier execution should be cleaned by:
```
$ sbt clean
```
to avoid appending to the existing produced files.

## Running custom history files
- **Creating strong *d*-hitting schedules:**

	Construct strong *d*-hitting schedules by using the script ```scripts/tools/createTests```, by specifying the class under test, directory of input files, name of test files, name of the stats directory, name of the stat files, and the min and max depth for the hitting families.

	```
	python scripts/tools/createTests.py "java.util.concurrent.ArrayBlockingQueue<Integer>" "example/histories/ArrayBlockingQueue" "produced.testABQ" "TestABQ" "statsABQ" "StatABQ" 2 3  

	```
        
	This will produce .java source files in the `produced` directory. These .java files invoke strong hitting family of schedules of operations in the given history.  

- **Compiling and running the produced files:**

	Compile the produced classes using the script ```scripts/tools/compileTests.py``` by specifying the directory of files to be compiled: 
	
	```
	python scripts/tools/compileTests.py "produced/testABQ/D2" 

	```
	Run the compiled test classes using the script ```scripts/tools/runTests.py``` by specifying the directory of files to be run  directory of test files and a directory name for the output files ```scripts/tools/runTests.py```
	
	```
	python scripts/tools/runTests.py "produced/testABQ/D2" "out/ABQ/D2.txt"
	```
	
	After these steps, the output folders ```produced```, ```stat``` and ```out``` are 	created, where the files in the ```out``` folder lists whether the histories are shown to be linearizable or not.
