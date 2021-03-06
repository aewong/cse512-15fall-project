package edu.asu.cse512;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;

import scala.Tuple2;

public class JTSUtils {
	private static GeometryFactory factory = new GeometryFactory();

	public static void main(String[] args) {
		String l1 = "1,1,1,2,2";
		String l2 = "1,1,1";
		Geometry g1= getIdGeometryFromString(l1)._2;
		Geometry g2 = getIdPointFromString(l2)._2;
		
		System.out.println(g1.contains(g2));
		System.out.println(g1.intersects(g2));
		
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

	public static String getBoundingBoxString(Geometry g) {
		// Envelope() returns a Polygon whose points are (minx, miny), (maxx,
		// miny), (maxx, maxy), (minx, maxy), (minx, miny)
		g = g.getEnvelope();
		Coordinate[] coords = g.getCoordinates();

		if (null == coords || coords.length != 5)
			return null;

		StringBuilder sb = new StringBuilder();
		sb.append("{").append(coords[3].x).append(", ").append(coords[3].y).append("}, {").append(coords[1].x)
				.append(", ").append(coords[1].y).append("}");
		return sb.toString();
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

	public static Coordinate getCoordinateFromString(String line) {
		Coordinate ret = null;
		try {
			Double[] doubles = getDoublesFromLine(line);
			if (null == doubles || doubles.length != 2)
				throw new Exception("Invalid input format:" + line);

			ret = new Coordinate(doubles[0], doubles[1]);
		} catch (Exception e) {
			return null;
		}
		return ret;
	}

	public static Geometry getGeometryFromPoint(String line) {
		Geometry ret = null;
		try {
			ret = factory.createPoint(getCoordinateFromString(line));
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

	public static Tuple2<String, Point> getIdPointFromString(String line) {
		String[] strs = line.split(",");
		String id = strs[0];
		Double x = Double.parseDouble(strs[1]);
		Double y = Double.parseDouble(strs[2]);
		Point p = factory.createPoint(new Coordinate(x, y));
		return new Tuple2<String, Point>(id, p);
	}

	public static Tuple2<String, Coordinate> getIdCoordinateFromString(String line) {
		String[] strs = line.split(",");
		String id = strs[0];
		Double x = Double.parseDouble(strs[1]);
		Double y = Double.parseDouble(strs[2]);
		return new Tuple2<String, Coordinate>(id, new Coordinate(x, y));
	}

	public static Tuple2<String, Geometry> getIdGeometryFromString(String line) {
		String[] strs = line.split(",");
		String id = strs[0];
		Geometry g = null;
		Double x1 = Double.parseDouble(strs[1]);
		Double y1 = Double.parseDouble(strs[2]);
		if (strs.length == 3) { // point
			g = factory.createPoint(new Coordinate(x1, y1));
		} else { // rectangle
			Double x2 = Double.parseDouble(strs[3]);
			Double y2 = Double.parseDouble(strs[4]);
			g = getRectangleFromLeftTopAndRightBottom(x1, y1, x2, y2);
		}
		return new Tuple2<String, Geometry>(id, g);
	}

	// LAME DESIGN, the requirement needs to sort the coordinates, and remove
	// the connecting point
	public static List<Coordinate> convertGeometryToSortedCoordinates(Geometry g) {
		List<Coordinate> coords = Arrays.asList(g.getCoordinates());

		if (coords.size() <= 1)
			return coords;

		if (coords.get(0).equals2D(coords.get(coords.size() - 1)))
			coords = coords.subList(0, coords.size() - 1);

		Collections.sort(coords, new Comparator<Coordinate>() {
			public int compare(Coordinate c1, Coordinate c2) {
				if (c1.x == c2.x) {
					return (c1.y == c2.y) ? 0 : ((c1.y - c2.y > 0) ? 1 : -1);
				} else {
					return (c1.x - c2.x > 0) ? 1 : -1;
				}
			}
		});
		return coords;
	}
}