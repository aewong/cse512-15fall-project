package edu.asu.cse512;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

public class ConvexHull {
	private static final String HDFS_ROOT_PATH = "hdfs://192.168.184.165:54310/";

	private static final String LOCAL_PATH = "";
	private static final String DEFAULT_INPUT_FILE = LOCAL_PATH + "convexhull_input.csv";
	private static final String DEFAULT_OUTPUT_FILE = LOCAL_PATH + "convexhull_output.csv";

	private static final boolean LOCAL_SPARK = true;
	private static final String SPARK_APP_NAME = "ConvexHull";
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
		BufferedWriter br = null;

		try {

			System.out.println("Convex Hull Starts");

			// set the input and output file
			String inputFile = DEFAULT_INPUT_FILE;
			String outputFile = DEFAULT_OUTPUT_FILE;

			if (args.length == 0) {
				System.out.println("Using default input and output files (Usage: ConvexHull <inputFile> <outputFile>)");
			} else if (args.length == 2) {
				inputFile = args[0];
				outputFile = args[1];
			} else {
				System.out.println("Usage: ConvexHull <inputFile> <outputFile>");
				return;
			}
			System.out.println("inputFile = " + inputFile + ", outputFile = " + outputFile);

			// open the output file
			Path pt = new Path(outputFile);
			FileSystem fs = FileSystem.get(new Configuration());
			br = new BufferedWriter(new OutputStreamWriter(fs.create(pt, true)));

			// to use local spark or distributed one
			if (LOCAL_SPARK) {
				sc = new JavaSparkContext("local", "ConvexHull");
			} else {
				sc = new JavaSparkContext(SPARK_MASTER, SPARK_APP_NAME, SPARK_HOME,
						new String[] { "target/d-0.1.jar", "lib/jts/lib/jts-1.8.jar" });
			}

			// Read input points
			JavaRDD<String> lines = sc.textFile(inputFile);

			// Convert each line of input to array with single coordinate
			JavaRDD<Geometry> convexHulls = lines.map(new Function<String, Geometry>() {
				private static final long serialVersionUID = 2594771192711015986L;

				public Geometry call(String line) {
					return JTSUtils.getGeometryFromPoint(line);
				}
			});

			// Convert coordinates to final coordinates of convex hull
			Geometry finalConvexHull = convexHulls.reduce(new Function2<Geometry, Geometry, Geometry>() {
				private static final long serialVersionUID = -8531187108258818193L;

				public Geometry call(Geometry arg0, Geometry arg1) throws Exception {
					return arg0.union(arg1).convexHull();
				}
			});

			// Output your result, you need to sort your result!!!
			for (Coordinate cor : finalConvexHull.getCoordinates()) {
				br.write(cor.x + ", " + cor.y + "\n");
				System.out.println(cor.x + ", " + cor.y);
			}
			br.flush();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeStream(br);
			if (null != sc)
				sc.close();
		}
	}
}
