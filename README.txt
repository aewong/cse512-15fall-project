CSE512 DDS class project for Group2
Members: 
    Audrey Wong
    Boxin Du
    Lei Chen
    Rongyu Lin
    Yihan Lu

0. rectangles are defined by left-top and right-bottom corners.

1. Using JTS library.

2. the main() for each function takes 2 or 3 strings as parameters which define the absolute path to the input and output files.
(e.g. hdfs://192.168.0.34:54310/UnionTestData.csv hdfs://192.168.0.34:54310/UnionOutput.csv)

3. In each function's source code, there are two boolean variables:
FILE_LOCAL : use local file system or hdfs
SPARK_LOCAL : use local spark or remote spark

4. Passed tests when run in Eclipse, using hdfs and remote spark
