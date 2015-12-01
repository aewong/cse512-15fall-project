package edu.asu.cse512;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.util.Progressable;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineSegment;

import scala.Tuple2;

public class ClosestPair {
	private static final String HDFS_PATH = "hdfs://192.168.1.56:54310/";
	private static final String LOCAL_PATH = "";

	private static final int POWER = 3;

	private static final boolean FILE_LOCAL = false;
	private static final String FILE_PATH = FILE_LOCAL ? LOCAL_PATH : HDFS_PATH;
//	private static final String DEFAULT_INPUT_FILE = FILE_PATH + "ClosestPairTestData.csv";
	private static final String DEFAULT_INPUT_FILE = FILE_PATH + "points" + POWER + ".dat";
	private static final String DEFAULT_OUTPUT_FILE = FILE_PATH + "ClosestPairOutput.csv";

	private static final boolean SPARK_LOCAL = false;
	private static final String SPARK_APP_NAME = "Group2-ClosestPair-" + POWER;
	private static final String SPARK_MASTER = "spark://192.168.1.56:7077";
	private static final String SPARK_HOME = "/home/user/spark-1.5.0-bin-hadoop2.6";

	private static final double EPSL = 0.00000001;

	/*
	 * Main function, take two parameter as input, output
	 * 
	 * @param inputLocation
	 * 
	 * @param outputLocation
	 * 
	 */
	public static void main(String[] args) {
		JavaSparkContext sc = null;
		BufferedWriter bw = null;

		try {

			System.out.println(SPARK_APP_NAME + " Starts");

			// set the input and output file
			String inputFile = DEFAULT_INPUT_FILE;
			String outputFile = DEFAULT_OUTPUT_FILE;

			if (args.length == 0) {
				System.out.println("Using default input and output files (Usage: " + SPARK_APP_NAME
						+ " <inputFile> <outputFile>)");
			} else if (args.length == 2) {
				inputFile = args[0];
				outputFile = args[1];
			} else {
				System.out.println("Usage: " + SPARK_APP_NAME + " <inputFile> <outputFile>");
				return;
			}
			System.out.println("inputFile = " + inputFile + ", outputFile = " + outputFile);

			// open the output file
			if (FILE_LOCAL) {
				Path pt = new Path(outputFile);
				FileSystem fs = FileSystem.get(new Configuration());
				bw = new BufferedWriter(new OutputStreamWriter(fs.create(pt, true)));
			} else {
				Configuration configuration = new Configuration();
				FileSystem hdfs = FileSystem.get(new URI(FILE_PATH), configuration);
				OutputStream out = hdfs.create(new Path(outputFile), new Progressable() {
					public void progress() {
					}
				});
				bw = new BufferedWriter(new OutputStreamWriter(out));
			}

			// to use local spark or distributed one
			if (SPARK_LOCAL) {
				sc = new JavaSparkContext("local", SPARK_APP_NAME);
			} else {
				 sc = new JavaSparkContext(SPARK_MASTER, SPARK_APP_NAME,
				 SPARK_HOME,
				 new String[] { "target/closestPair-0.1.jar",
				 "../lib/jts-1.8.jar" });

				// code from TA
//				SparkConf conf = new SparkConf().setAppName(SPARK_APP_NAME);
//				sc = new JavaSparkContext(conf);
			}

			// Read input points
			JavaRDD<String> lines = sc.textFile(inputFile);

			// Convert each line of input to array with single coordinate
			JavaRDD<Coordinate> points = lines.map(new Function<String, Coordinate>() {
				private static final long serialVersionUID = 2594771192711015986L;

				public Coordinate call(String line) {
					return JTSUtils.getCoordinateFromString(line);
				}
			});

			// get all the pairs
			JavaPairRDD<Coordinate, Coordinate> pairs = points.cartesian(points);

			// filter pair of the same point
			pairs = pairs.filter(new Function<Tuple2<Coordinate, Coordinate>, Boolean>() {
				private static final long serialVersionUID = 2869765103096895872L;

				public Boolean call(Tuple2<Coordinate, Coordinate> c) throws Exception {
					return c._1.distance(c._2) > EPSL;
				}

			});

			// map to line segments
			JavaRDD<LineSegment> segments = pairs.map(new Function<Tuple2<Coordinate, Coordinate>, LineSegment>() {
				private static final long serialVersionUID = 3269060742156351040L;

				public LineSegment call(Tuple2<Coordinate, Coordinate> c) throws Exception {
					return new LineSegment(c._1, c._2);
				}
			});

			// reduce to the closest pair
			LineSegment closestPair = segments.reduce(new Function2<LineSegment, LineSegment, LineSegment>() {
				private static final long serialVersionUID = 5791564959041664385L;

				public LineSegment call(LineSegment l1, LineSegment l2) throws Exception {
					return (l1.getLength() <= l2.getLength()) ? l1 : l2;
				}

			});

			// sort the output points order
			List<Coordinate> line = new ArrayList<Coordinate>();
			line.add(closestPair.p0);
			line.add(closestPair.p1);
			Collections.sort(line, new Comparator<Coordinate>() {
				public int compare(Coordinate c1, Coordinate c2) {
					if (c1.x == c2.x) {
						return (c1.y == c2.y) ? 0 : ((c1.y - c2.y > 0) ? 1 : -1);
					} else {
						return (c1.x - c2.x > 0) ? 1 : -1;
					}
				}
			});

			// Output your result, you need to sort your result!!!
			for (Coordinate c : line) {
				bw.write(c.x + "," + c.y + "\n");
				System.out.println(c.x + "," + c.y);
			}
			bw.flush();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeStream(bw);
			if (null != sc)
				sc.close();
		}
	}
}
