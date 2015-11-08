package edu.asu.cse512;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.PairFunction;

import com.vividsolutions.jts.geom.Geometry;

import scala.Tuple2;

public class Join {
	private static final String HDFS_ROOT_PATH = "hdfs://192.168.184.165:54310/";

	private static final String LOCAL_PATH = "";
	private static final String DEFAULT_INPUT_FILE1 = LOCAL_PATH + "join_input_1.csv";
	private static final String DEFAULT_INPUT_FILE2 = LOCAL_PATH + "join_input_2.csv";
	private static final String DEFAULT_OUTPUT_FILE = LOCAL_PATH + "join_output.csv";

	private static final boolean LOCAL_SPARK = true;
	private static final String SPARK_APP_NAME = "Join";
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

			JavaRDD<String> lines1 = sc.textFile(inputFile1); // file1 is the index in the output file
			JavaRDD<String> lines2 = sc.textFile(inputFile2); 
			
			System.out.println(lines2.collect());

			JavaRDD<Geometry> polygons1 = lines1.map(new Function<String, Geometry>() {
				private static final long serialVersionUID = -3575879335450999899L;

				public Geometry call(String s) {
					return JTSUtils.getRectangleFromLeftTopAndRightBottom(s);
				}
			});

			JavaRDD<Geometry> polygons2= lines2.map(new Function<String, Geometry>() {
				private static final long serialVersionUID = -1088607376402284643L;

				public Geometry call(String s) {
					return JTSUtils.getRectangleFromLeftTopAndRightBottom(s);
				}
			});

			
			JavaPairRDD<Geometry, Geometry> pairs = polygons2.cartesian(polygons1);

			pairs = pairs.filter(new Function<Tuple2<Geometry, Geometry>, Boolean>() {
				private static final long serialVersionUID = -9132236068978817563L;

				public Boolean call(Tuple2<Geometry, Geometry> t) throws Exception {
					return t._1.intersects(t._2);
				}
				
			});
			
			// NOTE, in JTS, two geometry objects were only equal if they were in fact the same geometry object.
			// Use string presentation to remove duplicate if they are the same shape
			JavaPairRDD<String, Geometry> strPairs = pairs.mapToPair(new PairFunction<Tuple2<Geometry, Geometry>, String, Geometry>() {
				private static final long serialVersionUID = -8030017184012909270L;
				public Tuple2<String, Geometry> call(Tuple2<Geometry, Geometry> t) throws Exception {
					String s = JTSUtils.getBoundingBoxString(t._1);
					return new Tuple2<String, Geometry>(s, t._2);
				}
			});
			
			Map<String, Iterable<Geometry>> result = strPairs.groupByKey().collectAsMap();
			for (Entry<String , Iterable<Geometry>> entry : result.entrySet()) {
				Iterator<Geometry> it = entry.getValue().iterator();
				String key = entry.getKey();
				StringBuilder sb = new StringBuilder();
				sb.append(key).append(" : ");
				sb.append("{");
				while(it.hasNext()) {
					Geometry g = it.next();
					sb.append(JTSUtils.getBoundingBoxString(g));
					if (it.hasNext()) sb.append(" # ");
				}
				sb.append("}");
				System.out.println(sb.toString());
				bw.write(sb.toString() + "\n");
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
