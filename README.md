\# MatSub



\## Introduction

MatSub is an open-access software package designed to facilitate the application of Subgroup Discovery (SGD) algorithms in machine learning and data-driven scientific discovery. A key contribution of MatSub lies in the development of novel quality functions tailored to materials informatics.



\## Requirements

\- XXX



\## Input Files

\- \*\*data.xarf\*\*

The input file data.xarf contains the dataset used in SGD, where rows represent individuals and columns correspond to attributes and target properties.

\- \*\*input.json\*\*

The input file input.json specifies parameters for the SGD process, including quality functions, search strategies, and clustering settings.



\## Example Usage



An example of the usage of MatSub is in the \*\*workingExample-SAAs\*\* folder. The example should be run with the following command:

~~~

cd workingExample-SAAs

java -jar MatSub.java input.json > out.dat

~~~



\## List of input keywords

Important keywords and their descriptions in \*\*input.json\*\* are listed below.



\### workspaces

\-  \*\*Tag: datafile\*\* 

&nbsp;	- Usage: "datafile": "\*sting\*"

&nbsp;	- Purpose: 

> -   specify the dataset file

> -     data.xarf

> -    (use: "datafile": "data.xarf")



\### workspaces\_defaultMetricRule

\-  \*\*Tag: numberOfCutoffs\*\*

 	- Usage: "numberOfCutoffs": \*value\*

 	- Purpose:

> -   specify the number of cutoffs of k-means cluatering

> -    (use: "numberOfCutoffs": 14)



\-  \*\*Tag: maxNumberOfIterations\*\*

 	- Usage: "maxNumberOfIterations": \*value\*

 	- Purpose:

> -   specify the max number of iterations of k-means cluatering

> -    (use: "maxNumberOfIterations": 1000)



\### computations

\-  \*\*Tag: id\*\*

 	- Usage: "id": "\*string\*"

 	- Purpose:

> -   specify the id of the job

> -     pos\_median\_shift\_1\_0

> -    (use: "id": "pos\_median\_shift\_1\_0")



\-  \*\*Tag: algorithm\*\*

 	- Usage: "algorithm": "\*string\*"

 	- Purpose:

> -   specify the searching algorithm

> -     EMM\_SAMPLER

> -    (use: "algorithm": "EMM\_SAMPLER")



\### computation\_parameters

\-  \*\*Tag: targets\*\*

 	- Usage: "targets": "\[\*string\*]"

 	- Purpose:

> -   specify the name of the column of the target property

> -     property

> -    (use: "targets": \["property"])



\-  \*\*Tag: attr\_filter\*\*

 	- Usage: "attr\_filter": "\[\*string\*,\*string\*]"

 	- Purpose:

> -   specify the name of the column of the features to exclude

> -     id, EA

> -    (use: "attr\_filter": "\[id,EA]")



\-  \*\*Tag: dev\_measure\*\*

 	- Usage: "dev\_measure": "\*string\*"

 	- Purpose:

> -   specify the method to measure the devation 

> -     normalized\_positive\_mean\_shift

> -    (use: "dev\_measure": "normalized\_positive\_mean\_shift")



\-  \*\*Tag: obj\_func\*\*

 	- Usage: "obj\_func": "\*string\*"

 	- Purpose:

> -   specify the quality function 

> -     frequency times deviation

> -    (use: "obj\_func": "frequency times deviation")



\-  \*\*Tag: num\_res\*\*

 	- Usage: "num\_res": "\*value\*"

 	- Purpose:

> -   specify the number of best orthogonal subgroups to return

> -     2

> -    (use: "num\_res": "2")



\-  \*\*Tag: num\_threads\*\*

 	- Usage: "num\_threads": \*value\*

 	- Purpose:

> -   specify the number of threads to call

> -     10

> -    (use: "num\_threads": 10)



\-  \*\*Tag: num\_seeds\*\*

 	- Usage: "num\_seeds": \*value\*

 	- Purpose:

> -   specify the number of seeds of Monte Carlo sampling

> -     10000000

> -    (use: "num\_seeds": 10000000)



\### dataPath

\-  \*\*Tag: dataPath\*\*

 	- Usage: "dataPath": "\*string\*"

 	- Purpose:

> -   specify the path of the data and input files

> -     .

> -    (use: "dataPath": ".")



\## Outputs



\- \*\*out.dat\*\*

"out.dat" includes all the selectors, their supporting scores and the degeneracies.

\- \*\*output\*\*

"output"  includes details of the job results.



\## Contact



Any feedback, questions, bug reports should be report through the \[Issue Tracker](https://github.com/XiaojuanHu/MatSub/issues).



\## License



This package is provided under license:



\## Citation

When using MatSub in published work, please cite the following paper:

























