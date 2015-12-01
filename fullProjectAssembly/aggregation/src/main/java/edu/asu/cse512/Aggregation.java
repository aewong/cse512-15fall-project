package edu.asu.cse512;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
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
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFlatMapFunction;
import org.apache.spark.api.java.function.PairFunction;

import com.vividsolutions.jts.geom.Geometry;

import scala.Tuple2;

public class Aggregation {
	private static final String HDFS_PATH = "hdfs://192.168.1.56:54310/";
	private static final String LOCAL_PATH = "";
	
	private static final int POWER = 6;

	private static final boolean FILE_LOCAL = false;
	private static final String FILE_PATH = FILE_LOCAL ? LOCAL_PATH : HDFS_PATH;
	private static final String DEFAULT_INPUT_FILE1 = FILE_PATH + "AggregationPoints" + POWER + ".csv";
	private static final String DEFAULT_INPUT_FILE2 = FILE_PATH + "AggregationRects" + POWER + ".csv";
//	private static final String DEFAULT_INPUT_FILE1 = FILE_PATH + "testAggregationInput1.csv";
//	private static final String DEFAULT_INPUT_FILE2 = FILE_PATH + "testAggregationInput2.csv";
	private static final String DEFAULT_OUTPUT_FILE = FILE_PATH + "AggregationOutput.csv";

	private static final boolean SPARK_LOCAL = false;
	private static final String SPARK_APP_NAME = "Group2-Aggregatation-full";
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
//				 sc = new JavaSparkContext(SPARK_MASTER, SPARK_APP_NAME,
//				 SPARK_HOME,
//				 new String[] { "target/aggregation-0.1.jar",
//				 "../lib/jts-1.8.jar" });

				// code from TA
				SparkConf conf = new SparkConf().setAppName(SPARK_APP_NAME);
				sc = new JavaSparkContext(conf);
			}

			// polygons or points
			JavaRDD<String> lines1 = sc.textFile(inputFile1);
			// query windows
			JavaRDD<String> lines2 = sc.textFile(inputFile2);

			// make a pair out of a geometry
			JavaPairRDD<String, Geometry> idpoints = lines1.mapToPair(new PairFunction<String, String, Geometry>() {
				private static final long serialVersionUID = 5064721835378357189L;

				public Tuple2<String, Geometry> call(String line) throws Exception {
					return JTSUtils.getIdGeometryFromString(line);
				}
			});

			JavaPairRDD<String, Geometry> idrects = lines2.mapToPair(new PairFunction<String, String, Geometry>() {
				private static final long serialVersionUID = 5064721835378357999L;

				public Tuple2<String, Geometry> call(String line) throws Exception {
					return JTSUtils.getIdGeometryFromString(line);
				}
			});

			// // the way to get ranges
			// JavaRDD<Double> pointsx = lines1.map(new Function<String,
			// Double>() {
			// public Double call(String line) throws Exception {
			// String[] strs = line.split(",");
			// return Double.parseDouble(strs[1]);
			// }
			// });
			// POINT_MINX = pointsx.reduce(new Function2<Double, Double,
			// Double>() {
			// public Double call(Double v1, Double v2) throws Exception {
			// return Math.min(v1, v2);
			// }
			// });

			// Points range
			final double POINT_MINX = -179.147236;
			final double POINT_MAXX = 179.475569;
			final double POINT_MINY = -14.548699;
			final double POINT_MAXY = 71.355134;

			// Rects range
			final double RECT_MINX = -176.685;
			final double RECT_MAXX = 145.8305;
			final double RECT_MINY = -14.3738;
			final double RECT_MAXY = 71.34132;

			final double MIN_X = Math.min(POINT_MINX, RECT_MINX);
			final double MAX_X = Math.max(POINT_MAXX, RECT_MAXX);
			final double MIN_Y = Math.min(POINT_MINY, RECT_MINY);
			final double MAX_Y = Math.max(POINT_MAXY, RECT_MAXY);
			final int TAG_ROWS = 20;
			final int TAG_COLS = 20;
			
			// evenly divide range into many rectangle areas with tag
			// ROWID_COLID
			// it goes like
			// 1_1, 1_2, 1_3 ... 1_N
			// ...
			// N_1, N_2, N_3 ... N_N

			JavaPairRDD<String, String> taggedLines1 = lines1.mapToPair(new PairFunction<String, String, String>() {
				private static final long serialVersionUID = -7087938390029595038L;

				public Tuple2<String, String> call(String t) throws Exception {
					String[] strs = t.split(",");
					Double x = Double.parseDouble(strs[1]);
					Double y = Double.parseDouble(strs[2]);
					String tag = getTag(MIN_X, MIN_Y, MAX_X, MAX_Y, x, y, TAG_ROWS, TAG_COLS);
//					System.out.println("tagged: " + t + " --> " + tag);
					return new Tuple2<String, String>(tag, t);
				}
			});

			JavaPairRDD<String, String> taggedLines2 = lines2
					.flatMapToPair(new PairFlatMapFunction<String, String, String>() {
						private static final long serialVersionUID = -5273121957558075875L;

						public Iterable<Tuple2<String, String>> call(String t) throws Exception {
							String[] strs = t.split(",");
							Double x1 = Double.parseDouble(strs[1]);
							Double y1 = Double.parseDouble(strs[2]);
							Double x2 = Double.parseDouble(strs[3]);
							Double y2 = Double.parseDouble(strs[4]);
							Map<String, Tuple2<String, String>> ret = new HashMap<String, Tuple2<String, String>>();
							List<Integer> xtags = getTags(MIN_X, MAX_X, x1, x2, TAG_COLS);
							List<Integer> ytags = getTags(MIN_Y, MAX_Y, y1, y2, TAG_ROWS);
							for (Integer y : ytags) {
								for (Integer x : xtags) {
									String tag = y + "_" + x;
//									System.out.println("tagged: " + t + " --> " + tag);
									ret.put(tag, new Tuple2<String, String>(tag, t));
								}
							}
							return ret.values();
						}
					});

			JavaPairRDD<String, Tuple2<String, String>> joinedLines = taggedLines2.join(taggedLines1);

			JavaPairRDD<String, Tuple2<String, String>> filteredLines = joinedLines
					.filter(new Function<Tuple2<String, Tuple2<String, String>>, Boolean>() {
						private static final long serialVersionUID = 9012151790901152254L;

						public Boolean call(Tuple2<String, Tuple2<String, String>> t) throws Exception {
							String l1 = t._2._1;
							String l2 = t._2._2;
							Geometry rect = JTSUtils.getIdGeometryFromString(l1)._2;
							Geometry point = JTSUtils.getIdPointFromString(l2)._2;
							return rect.contains(point) || rect.intersects(point);
						}
					});

//			List<Tuple2<String, Tuple2<String, String>>> prt1 = filteredLines.collect();
//			for (Tuple2<String, Tuple2<String, String>> t : prt1) {
//				System.out.println(t._1 + ", " + t._2);
//			}

			JavaPairRDD<Integer, Integer> rectids = filteredLines
					.mapToPair(new PairFunction<Tuple2<String, Tuple2<String, String>>, Integer, Integer>() {
						private static final long serialVersionUID = -5671628477969451600L;

						public Tuple2<Integer, Integer> call(Tuple2<String, Tuple2<String, String>> t)
								throws Exception {
							String l1 = t._2._1;
							Integer rectid = Integer.parseInt(l1.split(",", 2)[0]);
							return new Tuple2<Integer, Integer>(rectid, 1);
						}
					});

			rectids = rectids.reduceByKey(new Function2<Integer, Integer, Integer>() {
				private static final long serialVersionUID = -1285137147882687549L;

				public Integer call(Integer v1, Integer v2) throws Exception {
					return v1 + v2;
				}
			});

			List<Tuple2<Integer, Integer>> temp = rectids.collect();
			for (Tuple2<Integer, Integer> t : temp) {
				System.out.println(t._1 + ", " + t._2);
				bw.write(t._1 + ", " + t._2);
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

	private static List<Integer> getTags(double min, double max, double rangeStart, double rangeStop, int intervals) {
		List<Integer> ret = new ArrayList<Integer>();
		if (rangeStop < min) {
			ret.add(1);
		} else if (rangeStart >= max) {
			ret.add(intervals);
		} else {
			int indexStart = 1 + (int) (intervals * ((rangeStart - min) / (max - min)));
			int indexStop = Math.min(intervals, 1 + (int) (intervals * ((rangeStop - min) / (max - min))));
			for (int i = indexStart; i <= indexStop; ++i) {
				ret.add(i);
			}
		}
		return ret;
	}

	private static String getTag(double minx, double miny, double maxx, double maxy, double x, double y, int rows,
			int cols) {
		int tagx = 1;
		int tagy = 1;
		if (x >= maxx)
			tagx = cols;
		else
			tagx = 1 + (int) (cols * ((x - minx) / (maxx - minx)));

		if (y >= maxy)
			tagy = rows;
		else
			tagy = 1 + (int) (rows * ((y - miny) / (maxy - miny)));

		return tagy + "_" + tagx;
	}
}
