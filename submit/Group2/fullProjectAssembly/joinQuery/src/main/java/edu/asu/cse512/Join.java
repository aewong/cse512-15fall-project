package edu.asu.cse512;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.IteratorUtils;
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

import scala.Tuple2;

public class Join {
	private static final String HDFS_PATH = "hdfs://192.168.184.165:54310/";

	private static final String LOCAL_PATH = "";
	private static final boolean FILE_LOCAL = false;
	private static final String FILE_PATH = FILE_LOCAL ? LOCAL_PATH : HDFS_PATH;
	private static final String DEFAULT_INPUT_FILE1 = FILE_PATH + "JoinQueryInput1.csv";
	private static final String DEFAULT_INPUT_FILE2 = FILE_PATH + "JoinQueryInput2.csv";
	private static final String DEFAULT_OUTPUT_FILE = FILE_PATH + "JoinQueryOutput.csv";

	private static final boolean SPARK_LOCAL = false;
	private static final String SPARK_APP_NAME = "Group2-JoinQuery";
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
				// new String[] { "target/joinQuery-0.1.jar",
				// "../lib/jts-1.8.jar" });

				// code from TA
				SparkConf conf = new SparkConf().setAppName(SPARK_APP_NAME);
				sc = new JavaSparkContext(conf);
			}

			JavaRDD<String> lines1 = sc.textFile(inputFile1); // polygons or
																// points
			JavaRDD<String> lines2 = sc.textFile(inputFile2); // query windows

			JavaPairRDD<String, Geometry> idpoly1 = lines1.mapToPair(new PairFunction<String, String, Geometry>() {
				private static final long serialVersionUID = 5064721835378357189L;

				public Tuple2<String, Geometry> call(String line) throws Exception {
					return JTSUtils.getIdGeometryFromString(line);
				}
			});

			JavaPairRDD<String, Geometry> idpoly2 = lines2.mapToPair(new PairFunction<String, String, Geometry>() {
				private static final long serialVersionUID = 5064721835378357999L;

				public Tuple2<String, Geometry> call(String line) throws Exception {
					return JTSUtils.getIdGeometryFromString(line);
				}
			});

			JavaPairRDD<Tuple2<String, Geometry>, Tuple2<String, Geometry>> pairs = idpoly2.cartesian(idpoly1);

			pairs = pairs.filter(new Function<Tuple2<Tuple2<String, Geometry>, Tuple2<String, Geometry>>, Boolean>() {
				private static final long serialVersionUID = -9132236068978817563L;

				public Boolean call(Tuple2<Tuple2<String, Geometry>, Tuple2<String, Geometry>> t) throws Exception {
					Geometry g1 = t._1._2;
					Geometry g2 = t._2._2;
					return g1.intersects(g2) || g1.contains(g2) || g2.contains(g1);
				}
			});

			JavaPairRDD<Integer, Integer> idPairs = pairs.mapToPair(
					new PairFunction<Tuple2<Tuple2<String, Geometry>, Tuple2<String, Geometry>>, Integer, Integer>() {
						public Tuple2<Integer, Integer> call(
								Tuple2<Tuple2<String, Geometry>, Tuple2<String, Geometry>> t) throws Exception {
							return new Tuple2<Integer, Integer>(Integer.parseInt(t._1._1), Integer.parseInt(t._2._1));
						}

					});

			Map<Integer, Iterable<Integer>> result = idPairs.groupByKey().collectAsMap();
			List<Tuple2<Integer, Iterable<Integer>>> resultList = new ArrayList<Tuple2<Integer, Iterable<Integer>>>();
			for (Entry<Integer, Iterable<Integer>> entry : result.entrySet()) {
				Tuple2<Integer, Iterable<Integer>> item = new Tuple2<Integer, Iterable<Integer>>(entry.getKey(),
						entry.getValue());
				resultList.add(item);
			}

			Collections.sort(resultList, new Comparator<Tuple2<Integer, Iterable<Integer>>>() {
				public int compare(Tuple2<Integer, Iterable<Integer>> t1, Tuple2<Integer, Iterable<Integer>> t2) {
					return t1._1 - t2._1;
				}
			});

			for (Tuple2<Integer, Iterable<Integer>> entry : resultList) {
				Integer key = entry._1;
				List<Integer> values = IteratorUtils.toList(entry._2.iterator());
				Collections.sort(values);
				StringBuilder sb = new StringBuilder();
				sb.append(key).append(",");
				Integer num;
				for (Iterator<Integer> it = values.iterator(); it.hasNext();) {
					num = it.next();
					sb.append(num);
					if (it.hasNext())
						sb.append(",");
				}
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
