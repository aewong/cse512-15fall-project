HADOOP_HOME="/home/user/hadoop-2.7.1"
HDFS_PATH=hdfs://192.168.184.165:54310/

INPUT_FILES="fullProjectAssembly/union/union_input.csv 
fullProjectAssembly/convexHull/convexhull_input.csv 
fullProjectAssembly/farthestPair/farthestpair_input.csv 
fullProjectAssembly/closetPair/closestpair_input.csv 
fullProjectAssembly/rangeRuery/rangequery_input_1.csv 
fullProjectAssembly/rangeRuery/rangequery_input_2.csv 
fullProjectAssembly/joinQuery/join_input_1.csv 
fullProjectAssembly/joinQuery/join_input_2.csv"

for file in $INPUT_FILES
do 
	echo "copying ${file##*/}"
	$HADOOP_HOME/bin/hdfs dfs -put $file $HDFS_PATH/${file##*/}
done



