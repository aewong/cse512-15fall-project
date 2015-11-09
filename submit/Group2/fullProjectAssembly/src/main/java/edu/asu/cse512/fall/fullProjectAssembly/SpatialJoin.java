package mygroup.myartifact;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URI;
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

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import scala.Tuple2;

public class SpatialJoin {
	private static final String HDFS_ROOT_PATH = "hdfs://192.168.184.165:54310/";
	private static final String LOCAL_PATH = "/home/user/";

	private static final String INPUT_FILE_1 = "join_input_1.csv";
	private static final String INPUT_FILE_2 = "join_input_2.csv";
	private static final String OUTPUT_FILE = "range_output.csv";

	public static void main(String[] args) {
		JavaSparkContext sc = null;
		InputStream inputStream = null;
		OutputStream outputStream = null;

		try {
			System.out.println("Spatial Range Query Start!");

			// 0. copy local file to HDFS
			HDFSFileCopy.copyToHDFS(INPUT_FILE_1, INPUT_FILE_1, HDFS_ROOT_PATH, LOCAL_PATH);

			// 1. read the lines from the input file in HDFS
			SparkConf conf = new SparkConf().setAppName("MyAppName").setMaster("local");
			sc = new JavaSparkContext(conf);
			JavaRDD<String> lines1 = sc.textFile(HDFS_ROOT_PATH + INPUT_FILE_1);
			JavaRDD<String> lines2 = sc.textFile(HDFS_ROOT_PATH + INPUT_FILE_2);

			// 2. spatial join query
			// 2.1 map: convert each line of string to a polygon
			JavaRDD<Geometry> polygons1 = lines1.map(new Function<String, Geometry>() {
				private static final long serialVersionUID = -4119796271168086533L;

				public Geometry call(String s) {
					return UnionJTS.getRectangleFromLeftTopAndRightBottom(s);
				}
			});
			JavaRDD<Geometry> polygons2 = lines2.map(new Function<String, Geometry>() {
				private static final long serialVersionUID = -4119796271168086534L;

				public Geometry call(String s) {
					return UnionJTS.getRectangleFromLeftTopAndRightBottom(s);
				}
			});

			// 2.2 get the cartesian product
			JavaPairRDD<Geometry, Geometry> cartesian = polygons1.cartesian(polygons2);

			// 2.3 filter: to get the pairs that overlaps
			JavaPairRDD<Geometry, Geometry> filtered = cartesian
					.filter(new Function<Tuple2<Geometry, Geometry>, Boolean>() {
						public Boolean call(Tuple2<Geometry, Geometry> keyValue) {
							return keyValue._1().intersects(keyValue._2());
						}
					});
			
			// 2.4 combineByKey: to get the list of polygons for each key
			// USE combineByKey to "aggregate 
			// TODO
			
			// 3. output to an HDFS file
			Configuration configuration = new Configuration();
			FileSystem hdfs = FileSystem.get(new URI(HDFS_ROOT_PATH), configuration);
			outputStream = hdfs.create(new Path(HDFS_ROOT_PATH + OUTPUT_FILE), new Progressable() {
				public void progress() {
					// System.out.print(".");
				}
			});
			PrintWriter pw = new PrintWriter(outputStream);
//			for (Geometry g : rangeResults.collect()) {
//				for (Coordinate cor : g.getCoordinates()) {
//					pw.println(cor.x + ", " + cor.y);
//				}
//			}
			pw.flush();
			pw.close();

			// 4. copy the output file from HDFS to local disk
			HDFSFileCopy.copyFromHDFS(OUTPUT_FILE, OUTPUT_FILE, HDFS_ROOT_PATH, LOCAL_PATH);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeStream(inputStream);
			IOUtils.closeStream(outputStream);
			if (null != sc)
				sc.close();
		}
	}
}
