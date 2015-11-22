package datagen;

import java.io.IOException;
import java.util.Random;

import javax.swing.JFrame;

public class DataGenDriver {
    private static final int WIN_W = 600;
    private static final int WIN_H = 600;
    private static DataGenView panel = new DataGenView(WIN_W, WIN_H);
    private static void createAndShowGUI() throws IOException {
	JFrame frame = new JFrame("CSE 512 Group2: Data Generator");
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	frame.getContentPane().add(panel);
	frame.setSize(WIN_W, WIN_H);
	frame.setVisible(true);

	double xmin = -500;
	double ymin = -500;
	double xrange = 1000;
	double yrange = 1000;

	Thread updateThread = new Thread(new Runnable() {
	    @Override
	    public void run() {
		try {
		    Random rand = new Random();
		    rand.setSeed(System.currentTimeMillis());
		    for (int i = 0; i < 500; ++i) {
			double x1 = rand.nextDouble() * xrange + xmin;
			double y1 = rand.nextDouble() * yrange + ymin;
			double x2 = rand.nextDouble() * xrange + xmin;
			double y2 = rand.nextDouble() * yrange + ymin;
			panel.AddRectangle(Math.min(x1, x2), Math.min(y1, y2), Math.max(x1, x2), Math.max(y1, y2));
			panel.repaint();
			Thread.sleep(10);
		    }
		} catch (Exception e) {
		    e.printStackTrace();
		}
	    }
	});
	updateThread.start();
    }

    public static void main(String[] args) {
	javax.swing.SwingUtilities.invokeLater(new Runnable() {
	    public void run() {
		try {
		    createAndShowGUI();
		} catch (IOException e) {
		    e.printStackTrace();
		}
	    }
	});
    }
}