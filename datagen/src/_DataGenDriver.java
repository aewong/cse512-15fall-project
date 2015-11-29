import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JFrame;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

public class _DataGenDriver {
    private static final int WIN_W = 800;
    private static final int WIN_H = 800;

    private List<Geometry> genRectangles(final int size, final double minx, final double maxx, final double miny, final double maxy, final _DataGenView view) {
	final List<Geometry> ret = new ArrayList<>();
	try {
	    final double xrange = (maxx - minx);
	    final double yrange = (maxy - miny);
	    Thread updateThread = new Thread(new Runnable() {
		@Override
		public void run() {
		    try {
			Random rand = new Random();
			rand.setSeed(System.currentTimeMillis());
			view.clean();
			for (int i = 0; i < size; ++i) {
			    double x1 = rand.nextDouble() * xrange + minx;
			    double y1 = rand.nextDouble() * yrange + miny;
			    double x2 = rand.nextDouble() * xrange + minx;
			    double y2 = rand.nextDouble() * yrange + miny;
			    view.AddRectangle(Math.min(x1, x2), Math.min(y1, y2), Math.max(x1, x2), Math.max(y1, y2));
			    view.repaint();
			}
		    } catch (Exception e) {
			e.printStackTrace();
		    }
		}
	    });
	    updateThread.start();
	    updateThread.join();
	} catch (Exception e) {
	    e.printStackTrace();
	}
	return ret;
    }

    public void genDataUnion(BufferedReader reader, _DataGenView view) {
	try {
	    int size = 100;
	    double minx = -800;
	    double maxx = 800;
	    double miny = -800;
	    double maxy = 800;
	    String line;
	    // get size
	    System.out.print("Size = " + size + "? or ");
	    line = reader.readLine();
	    if (null != line && line.length() > 0)
		size = Integer.parseInt(line);

	    System.out.print("X ranges from " + minx + "? or ");
	    line = reader.readLine();
	    if (null != line && line.length() > 0)
		minx = Double.parseDouble(line);

	    System.out.print("X ranges to " + maxx + "? or ");
	    line = reader.readLine();
	    if (null != line && line.length() > 0)
		maxx = Double.parseDouble(line);

	    System.out.print("Y ranges from " + miny + "? or ");
	    line = reader.readLine();
	    if (null != line && line.length() > 0)
		miny = Double.parseDouble(line);

	    System.out.print("Y ranges to " + maxy + "? or ");
	    line = reader.readLine();
	    if (null != line && line.length() > 0)
		maxy = Double.parseDouble(line);

	    double t;
	    if (minx > maxx) {
		t = minx;
		minx = maxx;
		maxx = t;
	    }
	    if (miny > maxy) {
		t = miny;
		miny = maxx;
		maxy = t;
	    }

	    System.out.print("Generating " + size + " rectangles with x in (" + minx + ", " + maxx + ") and y in (" + miny + ", " + maxy + ") ... ");
	    List<Geometry> rects = genRectangles(size, minx, maxx, miny, maxy, view);

	    // FIXME: the result of union is not correct
	    if (null != rects && rects.size() > 0) {
		Geometry union = rects.get(0);
		for (int i = 0; i < rects.size(); i++) {
		    union = union.union(rects.get(i));
		}
		String type = union.getGeometryType();
		if (!type.equals("MultiPolygon")) {
		    Coordinate[] coords = union.getCoordinates();
		    for (int i = 0; i < coords.length - 1; i++) {
			Coordinate c1 = coords[i];
			Coordinate c2 = coords[i + 1];
			view.AddLine(c1.x, c1.y, c2.x, c2.y);
			view.repaint();
		    }
		}

	    }

	} catch (IOException e) {

	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public static void main(String[] args) {
	try {
	    final _DataGenDriver driver = new _DataGenDriver();
	    final _DataGenView view = new _DataGenView(WIN_W, WIN_H);

	    Thread controlThread = new Thread(new Runnable() {
		@Override
		public void run() {
		    try {
			BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
			System.out.println("Welcome to DataGen tool: choose which test data to generate[1-6], anything else to exit");
			System.out.println("1. Union");
			System.out.println("2. Convex Hull");
			System.out.println("3. Closest Pair");
			System.out.println("4. Farthest Pair");
			System.out.println("5. RangeQuery");
			System.out.println("6. JoinQuery");
			String text;
			text = console.readLine();
			int choice = Integer.parseInt(text);
			switch (choice) {
			case 1:
			    driver.genDataUnion(console, view);
			    break;
			case 2:
			    break;
			case 3:
			    break;
			case 4:
			    break;
			case 5:
			    break;
			case 6:
			    break;
			default:
			    break;

			}
		    } catch (IOException e) {
			e.printStackTrace();
		    } finally {
			System.out.println("Finished!");
		    }
		}
	    });
	    controlThread.start();
	    controlThread.join();
	    JFrame frame = new JFrame("CSE 512 Group2: Data Generator");
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    frame.getContentPane().add(view);
	    frame.setSize(WIN_W, WIN_H);
	    frame.setVisible(true);

	} catch (NumberFormatException e) {

	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
}