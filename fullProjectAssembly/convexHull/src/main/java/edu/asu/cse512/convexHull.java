package edu.asu.cse512;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

import org.apache.commons.lang.ArrayUtils;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.SparkConf;

import com.vividsolutions.jts.algorithm.ConvexHull;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

/**
 * Hello world!
 *
 */
public class convexHull
{
	/*
	 * Main function, take two parameter as input, output
	 * @param inputLocation
	 * @param outputLocation
	 * 
	*/
    public static void main( String[] args )
    {
        //Initialize, need to remove existing in output file location.
    	
    	//Implement 
    	
    	//Output your result, you need to sort your result!!!
    	//And,Don't add a additional clean up step delete the new generated file...
    	GeometryConvexHull("ConvexHullTestData.csv", "ConvexHullResult.csv");
    }
    
    public static void GeometryConvexHull(String inputLocation, String outputLocation)
    {
    	SparkConf conf = new SparkConf().setAppName("convexHull").setMaster("local");
    	JavaSparkContext sc = new JavaSparkContext(conf);
    	
    	//Read input points
    	JavaRDD<String> lines = sc.textFile(inputLocation);
    	
    	//Convert each line of input to array with single coordinate
    	JavaRDD<Coordinate[]> coordinates = lines.map(new Function<String, Coordinate[]>() {
			private static final long serialVersionUID = 2594771192711015986L;

			public Coordinate[] call(String line)
    		{
    			//Split line into x and y
				String[] xy = line.split(",");
				
				//Return empty array for first line
				if (xy[0].equals("x") || xy[1].equals("y"))
				{
					return new Coordinate[0];
				}
				else
				{
					//Convert x and y to doubles
	    			double x = Double.parseDouble(xy[0]);
	    			double y = Double.parseDouble(xy[1]);
	    			
	    			//Create array with single coordinate using x and y
	    			Coordinate[] coordinate = { new Coordinate(x, y) };
	    			
	    			return coordinate;
				}
    		}
    	});
    	
    	//Convert coordinates to final coordinates of convex hull
    	Coordinate[] finalCoordinates = coordinates.reduce(new Function2<Coordinate[], Coordinate[], Coordinate[]>() {
			private static final long serialVersionUID = -8531187108258818193L;

			public Coordinate[] call(Coordinate[] c1, Coordinate[] c2)
    		{
    			//Combine coordinates into single array
				Coordinate[] coordinates = (Coordinate[]) ArrayUtils.addAll(c1, c2);
    			
				//Create convex hull from coordinates
    			ConvexHull convexHull = new ConvexHull(coordinates, new GeometryFactory());
    			
    			//Get geometry from convex hull to obtain final coordinates
    			Geometry chGeometry = convexHull.getConvexHull();
    			Coordinate[] finalCoordinates = chGeometry.getCoordinates();
    			
    			return finalCoordinates;
    		}
    	});
    	
    	try 
    	{
			PrintWriter pw = new PrintWriter(outputLocation);
			
			//Write x and y of each coordinate of convex hull
			for (Coordinate coord : finalCoordinates)
			{
				pw.println(coord.x + "," + coord.y);
			}
			
			pw.close();
		} 
    	catch (FileNotFoundException e) 
    	{
			e.printStackTrace();
		}
    	
    	sc.close();
    }
}
