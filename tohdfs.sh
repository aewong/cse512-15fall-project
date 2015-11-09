HADOOP_HOME="/home/user/hadoop-2.7.1"
HDFS_PATH=hdfs://192.168.184.165:54310/

INPUT_FILES="
fullProjectAssembly/rangeRuery/RangeQueryRectangle.csv
fullProjectAssembly/rangeRuery/RangeQueryTestData.csv
fullProjectAssembly/union/UnionQueryTestData.csv
fullProjectAssembly/convexHull/ConvexHullTestData.csv
fullProjectAssembly/farthestPair/FarthestPairTestData.csv
fullProjectAssembly/joinQuery/JoinQueryInput1.csv
fullProjectAssembly/joinQuery/JoinQueryInput2.csv
fullProjectAssembly/closetPair/ClosestPairTestData.csv"

$HADOOP_HOME/bin/hdfs dfs -rm -r $HDFS_PATH/*

for file in $INPUT_FILES
do 
	echo "copying ${file##*/}"
	$HADOOP_HOME/bin/hdfs dfs -put $file $HDFS_PATH/${file##*/}
done
