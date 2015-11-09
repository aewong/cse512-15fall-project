package edu.asu.cse512;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.util.Progressable;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

public class Union {
	private static final String HDFS_PATH = "hdfs://192.168.184.165:54310/";
	private static final String LOCAL_PATH = "";

	private static final boolean FILE_LOCAL = false;
	private static final String FILE_PATH = FILE_LOCAL ? LOCAL_PATH : HDFS_PATH;
	private static final String DEFAULT_INPUT_FILE = FILE_PATH + "UnionQueryTestData.csv";
	private static final String DEFAULT_OUTPUT_FILE = FILE_PATH + "UnionQueryOutput.csv";

	private static final boolean SPARK_LOCAL = false;
	private static final String SPARK_APP_NAME = "Union";
	private static final String SPARK_MASTER = "spark://192.168.184.165:7077";
	private static final String SPARK_HOME = "/home/user/spark-1.5.0-bin-hadoop2.6";

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
				System.out.println("Using default input and output files (Usage: " + SPARK_APP_NAME + " <inputFile> <outputFile>)");
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
//				sc = new JavaSparkContext("local", SPARK_APP_NAME); 
				SparkConf conf = new SparkConf().setAppName("Group2-Union");
				sc = new JavaSparkContext(conf); 
			} else {
				sc = new JavaSparkContext(SPARK_MASTER, SPARK_APP_NAME, SPARK_HOME,
						new String[] { "target/union-0.1.jar", "../lib/jts-1.8.jar" });
			}

			// 1. read the lines from the input file in HDFS
			JavaRDD<String> lines = sc.textFile(inputFile);

			// 2. Geometry Union
			// 2.1 map: convert each line of string to a polygon
			JavaRDD<Geometry> polygons = lines.map(new Function<String, Geometry>() {
				private static final long serialVersionUID = -1928298089452870258L;

				public Geometry call(String s) {
					return JTSUtils.getRectangleFromLeftTopAndRightBottom(s);
				}
			});

			// 2.2 reduce: combine every 2 polygons
			Geometry finalPolygon = polygons.reduce(new Function2<Geometry, Geometry, Geometry>() {
				private static final long serialVersionUID = -1967342595615519573L;

				public Geometry call(Geometry arg0, Geometry arg1) throws Exception {
					return arg0.union(arg1);
				}
			});
			
			// convert to the format required by TA
			List<Coordinate> coords = JTSUtils.convertGeometryToSortedCoordinates(finalPolygon);

			// Output your result, you need to sort your result!!!
			for (Coordinate cor : coords) {
				bw.write(cor.x + ", " + cor.y + "\n");
				System.out.println(cor.x + ", " + cor.y);
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