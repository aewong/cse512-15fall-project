OUTPUT_PATH="../output"

FILE_PREFIX="
UnionQuery
ConvexHull
FarthestPair
ClosestPair
RangeQuery
JoinQueryRectangle
JoinQueryPoint"

RESULT_SURFIX="Result.csv"
OUTPUT_SURFIX="Output.csv"

for prefix in $FILE_PREFIX
	do diff -q $prefix$RESULT_SURFIX $OUTPUT_PATH/$prefix$OUTPUT_SURFIX
        if [ $? == 0 ]; then
           echo $prefix passed
        fi
done
