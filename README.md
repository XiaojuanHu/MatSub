# MatSub v0.1.0

## Introduction
MatSub is an open-access Java-based software package designed to facilitate the application of Subgroup Discovery (SGD) algorithms in machine learning and data-driven scientific discovery. A key contribution of MatSub lies in the development of novel quality functions tailored to materials informatics.
This repository provides both source code and pre-built executables.

## Requirements
- **Java 8 or later** (tested with Java 8/11/17)
    - JRE is sufficient for running 
    - JDK + Maven (3.6+) are required for building from source

## Build from source
You can either build from source using Maven, or download the pre-built JAR from the [Releases](https://github.com/XiaojuanHu/MatSub/releases) page.
- from the project root (where pom.xml is located)
~~~
mvn clean package -DskipTests
~~~
- artifacts
target/matsub-0.1.0.jar
target/matsub-0.1.0-jar-with-dependencies.jar   <-- recommended runnable JAR

## Input Files
- **data.xarf**
The input file data.xarf contains the dataset used in SGD, where rows represent individuals and columns correspond to attributes and target properties.

- **input.json**
The input file input.json specifies parameters for the SGD process, including quality functions, search strategies, and clustering settings.

## Example Usage
An example of the usage of MatSub is in the **workingExample-SAAs** folder. The example should be run with the following command:
- Windows
~~~
cd workingExample-SAAs
java -jar ..\target\matsub-0.1.0-jar-with-dependencies.jar input.json > out.dat
~~~

-Linux/macOS
~~~
cd workingExample-SAAs
java -jar ../target/matsub-0.1.0-jar-with-dependencies.jar input.json > out.dat
~~~

## Input keywords (in **input.json**)
Important keywords and their descriptions are listed below.

### workspaces
- **Tag: datafile** 
	- Usage: "datafile": "*sting*"
	- Purpose: 
> -   specify the dataset file
> -   data.xarf
> -    (use: "datafile": "data.xarf")

### workspaces_defaultMetricRule
- **Tag: numberOfCutoffs**
	- Usage: "numberOfCutoffs": *value*
	- Purpose:
> -   specify the number of cutoffs of k-means cluatering
> -    (use: "numberOfCutoffs": 14)

- **Tag: maxNumberOfIterations**
	- Usage: "maxNumberOfIterations": *value*
	- Purpose:
> -   specify the max number of iterations of k-means cluatering
> -    (use: "maxNumberOfIterations": 1000)

### computations
-  **Tag: id**
	- Usage: "id": "*string*"
	- Purpose:
> -   specify the id of the job
> -     pos_median_shift_1_0
> -    (use: "id": "pos_median_shift_1_0")

-  **Tag: algorithm**
	- Usage: "algorithm": "*string*"
	- Purpose:
> -   specify the searching algorithm
> -     EMM_SAMPLER
> -    (use: "algorithm": "EMM_SAMPLER")

### computation_parameters
-  **Tag: targets**
	- Usage: "targets": "[*string*]"
	- Purpose:
> -   specify the name of the column of the target property
> -     property
> -    (use: "targets": ["property"])

-  **Tag: attr_filter**
	- Usage: "attr_filter": "[*string*,*string*]"
	- Purpose:
> -   specify the name of the column of the features to exclude
> -     id, EA
> -    (use: "attr_filter": "[id,EA]")

-  **Tag: dev_measure**
	- Usage: "dev_measure": "*string*"
	- Purpose:
> -   specify the method to measure the devation 
> -     possible values:
          - normalized_positive_mean_shift            <-- Subgroups biased toward high values, without boundary constraints
          - normalized_negative_mean_shift           <-- Subgroups biased toward low values, without boundary constraints
          - normalized_max_constant_ref                <-- Subgroups biased toward high values, with boundary constraints
          - normalized_min_constant_ref                <-- Subgroups biased toward low values, with boundary constraints
> -    (use: "dev_measure": "normalized_positive_mean_shift")

-  **Tag: obj_func**
	- Usage: "obj_func": "*string*"
	- Purpose:
> -   specify the quality function 
> -     possible values:
          - frequency times deviation
          - frequency times aamd-gain times deviation
          - sqrt(frequency) times deviation
          - H(frequency) times deviation                  <--  Entropy times deviation
          - multitask entropy gain
> -    (use: "obj_func": "frequency times deviation")

-  **Tag: num_res**
	- Usage: "num_res": "*value*"
	- Purpose:
> -   specify the number of best orthogonal subgroups to return
> -     2                                                                   <-- Output 2 orthogonal subgroups
> -    (use: "num_res": "2")

-  **Tag: num_threads**
	- Usage: "num_threads": *value*
	- Purpose:
> -   specify the number of threads to call
> -     10
> -    (use: "num_threads": 10)

-  **Tag: num_seeds**
	- Usage: "num_seeds": *value*
	- Purpose:
> -   specify the number of seeds of Monte Carlo sampling
> -     10000000
> -    (use: "num_seeds": 10000000)

-  **Tag: qual_func_params**
	- Usage: "qual_func_params": "*value*"
	- Purpose:
> -   if dev_measure = normalized_max_constant_ref/normalized_min_constant_ref, specify the value of boundary.
> -     0
> -    (use: "qual_func_params": "0")

-  **Tag: hard_cutoffs**
	- Usage: "hard_cutoffs": "above/below"
	- Purpose:
> -   if dev_measure = normalized_max_constant_ref, hard_cutoffs = above, if dev_measure = normalized_min_constant_ref, hard_cutoffs = below.
> -     above
> -    (use: "hard_cutoffs": "above")

### dataPath
-  **Tag: dataPath**
	- Usage: "dataPath": "*string*"
	- Purpose:
> -   specify the path of the data and input files
> -     .
> -    (use: "dataPath": ".")

## Outputs
- **out.dat**
"out.dat" includes all the selectors, their supporting scores and the degeneracies.

- **output**
"output" is a folder that includes details of the job results.

## Contact
Any feedback, questions, bug reports should be report through the [Issue Tracker](https://github.com/XiaojuanHu/MatSub/issues).

## License
This project is licensed under the Apache License 2.0 – see the LICENSE file for details.

## Citation
When using MatSub in published work, please cite the following paper:




















