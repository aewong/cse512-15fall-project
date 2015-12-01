HADOOP_HOME="/home/user/hadoop-2.7.1"
HDFS_PATH=hdfs://192.168.1.56:54310/

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
testdata/UnionQueryTestData.csv
testdata/points1.dat
testdata/points2.dat
testdata/points3.dat
testdata/points4.dat
testdata/points5.dat
testdata/points6.dat
testdata/points7.dat
testdata/rects1.dat
testdata/rects2.dat
testdata/rects3.dat
testdata/rects4.dat
testdata/rects5.dat
testdata/rects6.dat
testdata/pointsid1.dat
testdata/pointsid2.dat
testdata/pointsid3.dat
testdata/pointsid4.dat
testdata/pointsid5.dat
testdata/pointsid6.dat
testdata/rectsid1.dat
testdata/rectsid2.dat
testdata/rectsid3.dat
testdata/rectsid4.dat
testdata/rectsid5.dat
testdata/rectsid6.dat
testdata/AggregationPoints1.csv
testdata/AggregationPoints2.csv
testdata/AggregationPoints3.csv
testdata/AggregationPoints4.csv
testdata/AggregationPoints5.csv
testdata/AggregationPoints6.csv
testdata/AggregationRects1.csv
testdata/AggregationRects2.csv
testdata/AggregationRects3.csv
testdata/AggregationRects4.csv
testdata/AggregationRects5.csv
testdata/AggregationRects6.csv"
INPUT_FILES="
testdata/AggregationPoints6.csv
testdata/AggregationRects5.csv
testdata/AggregationRects6.csv"


# DANGER!!! delete all the files
#$HADOOP_HOME/bin/hdfs dfs -rm -r $HDFS_PATH/*

for file in $INPUT_FILES
do 
	echo "copying ${file##*/}"
	$HADOOP_HOME/bin/hdfs dfs -rm -r $HDFS_PATH/${file##*/}
	$HADOOP_HOME/bin/hdfs dfs -put $file $HDFS_PATH/${file##*/}
done
