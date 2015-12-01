set -x

rm -f bin/DataGen*.class
rm -f rects*.dat
rm -f points*.dat

javac src/DataGenRectangles.java -d bin
javac src/DataGenPoints.java -d bin

java -cp bin DataGenRectangles 10 rects1.dat
java -cp bin DataGenRectangles 100 rects2.dat
java -cp bin DataGenRectangles 1000 rects3.dat
java -cp bin DataGenRectangles 10000 rects4.dat
java -cp bin DataGenRectangles 100000 rects5.dat
java -cp bin DataGenRectangles 1000000 rects6.dat
#java -cp bin DataGenRectangles 10000000 rects7.dat

java -cp bin DataGenPoints 10 points1.dat
java -cp bin DataGenPoints 100 points2.dat
java -cp bin DataGenPoints 1000 points3.dat
java -cp bin DataGenPoints 10000 points4.dat
java -cp bin DataGenPoints 100000 points5.dat
java -cp bin DataGenPoints 1000000 points6.dat
java -cp bin DataGenPoints 10000000 points7.dat

java -cp bin DataGenRectangles 10 rectsid1.dat id
java -cp bin DataGenRectangles 100 rectsid2.dat id
java -cp bin DataGenRectangles 1000 rectsid3.dat id
java -cp bin DataGenRectangles 10000 rectsid4.dat id
java -cp bin DataGenRectangles 100000 rectsid5.dat id
java -cp bin DataGenRectangles 1000000 rectsid6.dat id
#java -cp bin DataGenRectangles 10000000 rectsid7.dat id

java -cp bin DataGenPoints 10 pointsid1.dat id
java -cp bin DataGenPoints 100 pointsid2.dat id
java -cp bin DataGenPoints 1000 pointsid3.dat id
java -cp bin DataGenPoints 10000 pointsid4.dat id
java -cp bin DataGenPoints 100000 pointsid5.dat id
java -cp bin DataGenPoints 1000000 pointsid6.dat id
#java -cp bin DataGenPoints 10000000 pointsid7.dat id
