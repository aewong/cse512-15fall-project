package edu.asu.cse512;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.util.Collections;
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
import org.apache.spark.api.java.function.PairFunction;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import scala.Tuple2;

public class RangeQuery {
	private static final String HDFS_PATH = "hdfs://192.168.184.165:54310/";

	private static final String LOCAL_PATH = "";
	private static final boolean FILE_LOCAL = false;
	private static final String FILE_PATH = FILE_LOCAL ? LOCAL_PATH : HDFS_PATH;
	private static final String DEFAULT_INPUT_FILE1 = FILE_PATH + "RangeQueryTestData.csv";
	private static final String DEFAULT_INPUT_FILE2 = FILE_PATH + "RangeQueryRectangle.csv";
	private static final String DEFAULT_OUTPUT_FILE = FILE_PATH + "RangeQueryOutput.csv";

	private static final boolean SPARK_LOCAL = false;
	private static final String SPARK_APP_NAME = "Group2-RangeQuery";
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
				// sc = new JavaSparkContext(SPARK_MASTER, SPARK_APP_NAME,
				// SPARK_HOME,
				// new String[] { "target/rangeQuery-0.1.jar",
				// "../lib/jts-1.8.jar" });

				// code from TA
				SparkConf conf = new SparkConf().setAppName(SPARK_APP_NAME);
				sc = new JavaSparkContext(conf);
			}

			// Read input points
			JavaRDD<String> lines = sc.textFile(inputFile1);

			JavaPairRDD<String, Point> idpoints = lines.mapToPair(new PairFunction<String, String, Point>() {
				private static final long serialVersionUID = 5064721835378357189L;

				public Tuple2<String, Point> call(String line) throws Exception {
					return JTSUtils.getIdPointFromString(line);
				}
			});

			// read the query window
			JavaRDD<String> lines2 = sc.textFile(inputFile2);
			List<String> strs = lines2.collect();
			if (null == strs || strs.size() != 1) {
				throw new Exception("more than one line in file " + inputFile2);
			}
			final Geometry window = JTSUtils.getRectangleFromLeftTopAndRightBottom(strs.get(0));

			// filter the pairs to get 
			idpoints = idpoints.filter(new Function<Tuple2<String, Point>, Boolean>() {
				private static final long serialVersionUID = -1230587948991657811L;

				public Boolean call(Tuple2<String, Point> t) throws Exception {
					return window.intersects(t._2) || window.contains(t._2) || t._2.contains(window);
				}
			});

			// get the ids
			JavaRDD<Integer> ids = idpoints.map(new Function<Tuple2<String, Point>, Integer>() {
				private static final long serialVersionUID = -2797763495076035001L;

				public Integer call(Tuple2<String, Point> t) throws Exception {
					return Integer.parseInt(t._1);
				}
			});

			List<Integer> result = ids.collect();
			Collections.sort(result);

			for (Integer id : result) {
				System.out.println(id);
				bw.write(id + "\n");
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
