package edu.asu.cse512;

import java.util.Iterator;

import com.vividsolutions.jts.algorithm.ConvexHull;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiPolygon;

public class JTSUtils {
	private static GeometryFactory factory = new GeometryFactory();

	public static void main(String[] args) {
//		Geometry[] polygons = new Geometry[5];
//		polygons[0] = getRectangleFromLeftTopAndRightBottom(0.321534855, 0.036295831, -0.23567288, -0.415640992);
//		polygons[1] = getRectangleFromLeftTopAndRightBottom(0.115064798, 0.105952147, -0.161920957, -0.405533972);
//		polygons[2] = getRectangleFromLeftTopAndRightBottom(0.238709092, 0.016298271, -0.331934184, -0.18218141);
//		polygons[3] = getRectangleFromLeftTopAndRightBottom(0.2069243, 0.223297076, -0.050542958, -0.475492946);
//		polygons[4] = getRectangleFromLeftTopAndRightBottom(0.321534855, 0.036295831, -0.440428957, -0.289485599);
//
//		Geometry res = polygons[0];
//		for (int i = 1; i < polygons.length; i++) {
//			res = res.union(polygons[i]);
//		}
//
//		System.out.println(res);
		Coordinate[] coords = new Coordinate[4];
		coords[0] = new Coordinate(0,0);
		coords[1]= new Coordinate(0,2);
		coords[2]= new Coordinate(2,0);
		coords[3]= new Coordinate(0.5, 0.5);
		Geometry ch1 = new ConvexHull(coords, factory).getConvexHull();
		System.out.println(ch1);

		coords = new Coordinate[3];
		coords[0] = new Coordinate(0.1,0);
		coords[1]= new Coordinate(3,3);
		coords[2]= new Coordinate(3,0);
		Geometry ch2 = new ConvexHull(coords, factory).getConvexHull();
		System.out.println(ch2);
		
		System.out.println(ch1.union(ch2).convexHull());

	}

	/**
	 * Get a rectangle from a input of 4 doubles, x1, y1, x2, y2, which are the
	 * left-top and right-bottom corner of a rectangle
	 */
	public static Geometry getRectangleFromLeftTopAndRightBottom(double x1, double y1, double x2, double y2) {
		Coordinate[] ps = new Coordinate[5];
		ps[0] = new Coordinate(x1, y1);
		ps[1] = new Coordinate(x2, y1);
		ps[2] = new Coordinate(x2, y2);
		ps[3] = new Coordinate(x1, y2);
		ps[4] = new Coordinate(x1, y1);
		LinearRing ring = factory.createLinearRing(ps);
		return factory.createPolygon(ring, null);
	}

	public static Geometry getRectangleFromLeftTopAndRightBottom(String line) {
		Geometry ret = null;
		try {
			Double[] doubles = getDoublesFromLine(line);
			if (null == doubles || doubles.length != 4)
				throw new Exception("Invalid input format:" + line);

			ret = getRectangleFromLeftTopAndRightBottom(doubles[0], doubles[1], doubles[2], doubles[3]);
		} catch (Exception e) {
			return null;
		}
		return ret;
	}
	
	public static Geometry getGeometryFromPoint(String line) {
		Geometry ret = null;
		try {
			Double[] doubles = getDoublesFromLine(line);
			if (null == doubles || doubles.length != 2)
				throw new Exception("Invalid input format:" + line);
			
			ret = factory.createPoint(new Coordinate(doubles[0], doubles[1]));
		} catch (Exception e) {
			return null;
		}
		return ret;
		
	}

	/**
	 * each string should be in format x1, y1, x2, y2, which are the left-top
	 * and right-bottom corner of a rectangle
	 */
	public static Geometry UnionRectangles(Iterator<String> it) {
		Geometry ret = null;
		try {
			while (null != it && it.hasNext()) {
				String line = it.next();
				Double[] doubles = getDoublesFromLine(line);
				if (null == doubles || doubles.length != 4)
					throw new Exception("Invalid input format:" + line);

				Geometry next = getRectangleFromLeftTopAndRightBottom(doubles[0], doubles[1], doubles[2], doubles[3]);
				if (null == ret) {
					ret = next;
				} else {
					ret = ret.union(next);
				}
			}
		} catch (Exception e) {
			return null;
		}
		return ret;
	}

	/**
	 * Geometric Union a set of polygons
	 */
	public static Geometry UnionPolygons(Iterator<Geometry> it) {
		Geometry ret = factory.createPolygon(null, null);
		while (null != it && it.hasNext()) {
			ret = ret.union(it.next());
		}
		return ret;
	}

	public static Double[] getDoublesFromLine(String line) {
		Double[] ret = null;
		try {
			String[] strs = line.split(",");
			int size = strs.length;
			ret = new Double[size];
			for (int i = 0; i < size; i++) {
				ret[i] = Double.parseDouble(strs[i]);
			}
		} catch (Exception e) {
			return null;
		}
		return ret;

	}
}