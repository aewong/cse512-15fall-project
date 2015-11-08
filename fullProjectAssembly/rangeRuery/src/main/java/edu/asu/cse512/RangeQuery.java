package edu.asu.cse512;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.PairFunction;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import scala.Tuple2;

public class RangeQuery {
	private static final String HDFS_ROOT_PATH = "hdfs://192.168.184.165:54310/";

	private static final String LOCAL_PATH = "";
	private static final String DEFAULT_INPUT_FILE1 = LOCAL_PATH + "rangequery_input_1.csv";
	private static final String DEFAULT_INPUT_FILE2 = LOCAL_PATH + "rangequery_input_2.csv";
	private static final String DEFAULT_OUTPUT_FILE = LOCAL_PATH + "rangequery_output.csv";

	private static final boolean LOCAL_SPARK = true;
	private static final String SPARK_APP_NAME = "RangeQuery";
	private static final String SPARK_MASTER = "spark://192.168.184.165:7077";
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
			String inputFile1 = DEFAULT_INPUT_FILE1;
			String inputFile2 = DEFAULT_INPUT_FILE2;
			String outputFile = DEFAULT_OUTPUT_FILE;

			if (args.length == 0) {
				System.out.println("Using default input and output files (Usage: " + SPARK_APP_NAME
						+ " <inputFile1> <inputFile2> <outputFile>)");
			} else if (args.length == 3) {
				inputFile1 = args[0];
				inputFile2 = args[1];
				outputFile = args[2];
			} else {
				System.out.println("Usage: " + SPARK_APP_NAME + " <inputFile1> <inputFile1> <outputFile>");
				return;
			}
			System.out.println(
					"inputFile1 = " + inputFile1 + ", inputFile2 = " + inputFile2 + ", outputFile = " + outputFile);

			// open the output file
			Path pt = new Path(outputFile);
			FileSystem fs = FileSystem.get(new Configuration());
			bw = new BufferedWriter(new OutputStreamWriter(fs.create(pt, true)));

			// to use local spark or distributed one
			if (LOCAL_SPARK) {
				sc = new JavaSparkContext("local", SPARK_APP_NAME);
			} else {
				sc = new JavaSparkContext(SPARK_MASTER, SPARK_APP_NAME, SPARK_HOME,
						new String[] { "target/d-0.1.jar", "lib/jts/lib/jts-1.8.jar" });
			}

			// Read input points
			JavaRDD<String> lines = sc.textFile(inputFile1);

			JavaRDD<Geometry> polygons = lines.map(new Function<String, Geometry>() {
				private static final long serialVersionUID = 1L;

				public Geometry call(String s) {
					return JTSUtils.getRectangleFromLeftTopAndRightBottom(s);
				}
			});

			// read the query window
			JavaRDD<String> lines2 = sc.textFile(inputFile2);
			List<String> strs = lines2.collect();
			if (null == strs || strs.size() != 1) {
				throw new Exception("more than one line in file " + inputFile2);
			}
			final Geometry window = JTSUtils.getRectangleFromLeftTopAndRightBottom(strs.get(0));

			polygons = polygons.filter(new Function<Geometry, Boolean>() {
				private static final long serialVersionUID = 4704248633585265871L;

				public Boolean call(Geometry g) throws Exception {
					return window.intersects(g);
				}
				
			});

			List<Geometry> result = polygons.collect();

			// Output your result, you need to sort your result!!!
			for (Geometry g : result) {
				String s = JTSUtils.getBoundingBoxString(g)+"\n";
				System.out.println(s);
				bw.write(s);
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
