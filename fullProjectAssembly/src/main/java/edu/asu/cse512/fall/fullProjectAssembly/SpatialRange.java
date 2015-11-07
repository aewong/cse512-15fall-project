package mygroup.myartifact;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URI;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.util.Progressable;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.JavaRDD;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

public class SpatialRange {
	private static final String HDFS_ROOT_PATH = "hdfs://192.168.184.165:54310/";
	private static final String LOCAL_PATH = "/home/user/";

	private static final String INPUT_FILE_1 = "range_input_1.csv";
	// TODO read input 2 from file
	// private static final String INPUT_FILE = "range_input.csv";
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
			JavaRDD<String> lines = sc.textFile(HDFS_ROOT_PATH + INPUT_FILE_1);
			// TODO read from input_file_2 instead of hard coded
			final Geometry window = UnionJTS.getRectangleFromLeftTopAndRightBottom(0, 0, 1, 1);

			// 2. spatial range query
			// 2.1 map: convert each line of string to a polygon
			JavaRDD<Geometry> polygons = lines.map(new Function<String, Geometry>() {
				private static final long serialVersionUID = -4119796271168086532L;

				public Geometry call(String s) {
					return UnionJTS.getRectangleFromLeftTopAndRightBottom(s);
				}
			});

			// 2.2 filter: filter to allow the ones only in the query window
			JavaRDD<Geometry> rangeResults = polygons.filter(new Function<Geometry, Boolean>() {
				private static final long serialVersionUID = -5424930742935658378L;

				@Override
				public Boolean call(Geometry g) throws Exception {
					return window.contains(g);
				}
			});

			// 3. output to an HDFS file
			Configuration configuration = new Configuration();
			FileSystem hdfs = FileSystem.get(new URI(HDFS_ROOT_PATH), configuration);
			outputStream = hdfs.create(new Path(HDFS_ROOT_PATH + OUTPUT_FILE), new Progressable() {
				public void progress() {
					// System.out.print(".");
				}
			});
			PrintWriter pw = new PrintWriter(outputStream);
			for (Geometry g : rangeResults.collect()) {
				for (Coordinate cor : g.getCoordinates()) {
					pw.println(cor.x + ", " + cor.y);
				}
			}
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
