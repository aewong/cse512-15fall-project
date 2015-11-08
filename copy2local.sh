HADOOP_HOME="/home/user/hadoop-2.7.1"
HDFS_PATH="hdfs://192.168.184.165:54310/"

OUTPUT_PATH="output"
OUTPUT_FILES="rangequery_output.csv
union_output.csv
convexhull_output.csv
farthestpair_output.csv
join_output.csv
closestpair_output.csv"


mkdir $OUTPUT_PATH

for file in $OUTPUT_FILES
do 
	echo "copying $file"
	$HADOOP_HOME/bin/hdfs dfs -get $HDFS_PATH/$file $OUTPUT_PATH/$file
done



