HADOOP_HOME="/home/user/hadoop-2.7.1"
HDFS_PATH=hdfs://192.168.184.165:54310/

INPUT_FILES="
testdata/ClosestPairResult.csv
testdata/ClosestPairTestData.csv
testdata/ConvexHullResult.csv
testdata/ConvexHullTestData.csv
testdata/FarthestPairResult.csv
testdata/FarthestPairTestData.csv
testdata/JoinQueryInput1.csv
testdata/JoinQueryInput2.csv
testdata/JoinQueryInput3.csv
testdata/JoinQueryPointResult.csv
testdata/JoinQueryRectangleResult.csv
testdata/RangeQueryRectangle.csv
testdata/RangeQueryResult.csv
testdata/RangeQueryTestData.csv
testdata/UnionQueryResult.csv
testdata/UnionQueryTestData.csv"

# DANGER!!! delete all the files
#$HADOOP_HOME/bin/hdfs dfs -rm -r $HDFS_PATH/*

for file in $INPUT_FILES
do 
	echo "copying ${file##*/}"
	$HADOOP_HOME/bin/hdfs dfs -rm -r $HDFS_PATH/${file##*/}
	$HADOOP_HOME/bin/hdfs dfs -put $file $HDFS_PATH/${file##*/}
done
