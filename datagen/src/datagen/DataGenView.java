package datagen;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

public class DataGenView extends JPanel {
    private double minx;
    private double maxx;
    private double miny;
    private double maxy;

    private double marginRatio = 0.1;
    private int marginx;
    private int marginy;
    private int winw;
    private int winh;

    private static final long serialVersionUID = 6509217204123368745L;

    public DataGenView(int winw, int winh) {
	PointListener listener = new PointListener();
	this.addMouseMotionListener(listener);
	this.addMouseListener(listener);
	this.winw = winw;
	this.winh = winh;
	marginx = (int) (winw * marginRatio);
	marginy = (int) (winh * marginRatio);

	minx = marginx;
	maxx = winw - marginx;
	miny = marginy;
	maxy = winh - marginy;
    }

    public void paintComponent(Graphics g) {
	super.paintComponent(g);
	setBackground(Color.WHITE);
	synchronized (rects) {
	    for (Rectangle r : rects) {
		g.drawRect(r.x, r.y, r.width, r.height);
	    }
	}
    }

    private Point winPos(double x, double y) {
	if (x < minx)
	    x = minx;
	if (x > maxx)
	    x = maxx;
	if (y < miny)
	    y = miny;
	if (y > maxy)
	    y = maxy;

	int ix = marginx + (int) ((winw - marginx * 2) * ((x - minx) / (maxx - minx)));
	int iy = marginy + (int) ((winh - marginy * 2) * ((y - miny) / (maxy - miny)));
	return new Point(ix, iy);
    }

    List<Rectangle> rects = new ArrayList<>();

    public void AddRectangle(double x1, double y1, double x2, double y2) {
	try {
	    updateMinMax(x1, y1);
	    updateMinMax(x2, y2);
	    Point p1 = winPos(x1, y1);
	    Point p2 = winPos(x2, y2);
//	    System.out.println(x1 + ", " + y1 + ", " + x2 + ", " + y2 + " ---> " + p1.x + ", " + p1.y + ", " + p2.x + ", " + p2.y);
	    synchronized (rects) {
		rects.add(new Rectangle(p1.x, p1.y, p2.x - p1.x, p2.y - p1.y));
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    private void updateMinMax(double x, double y) {
	minx = Math.min(minx, x);
	maxx = Math.max(maxx, x);
	miny = Math.min(miny, y);
	maxy = Math.max(maxy, y);
    }

    public void AddPoint(double x, double y) {

    }

    // listener class that listens to the mouse
    public class PointListener implements MouseMotionListener, MouseListener {
	// in case that a user presses using a mouse,
	// record the point where it was pressed.
	public void mousePressed(MouseEvent event) {
	    int x = event.getX();
	    int y = event.getY();
	    System.out.println("Mouse pressed at " + x + ", " + y);
	    repaint();
	}

	public void mouseReleased(MouseEvent event) {
	    int x = event.getX();
	    int y = event.getY();
	    System.out.println("Mouse relased at " + x + ", " + y);
	    repaint();
	}

	public void mouseClicked(MouseEvent event) {
	}

	public void mouseEntered(MouseEvent event) {
	}

	public void mouseExited(MouseEvent event) {
	}

	@Override
	public void mouseDragged(MouseEvent event) {
	    int x = event.getX();
	    int y = event.getY();
	    System.out.println("Mouse Dragged to " + x + ", " + y);
	    repaint();
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
	}
    } // end of PointListener
} // end of Whole Panel Class
