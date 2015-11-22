import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Random;

public class DataGenPoints {

    private static final double MIN_X = -100;
    private static final double MAX_X = 100;
    private static final double MIN_Y = -100;
    private static final double MAX_Y = 100;

    public static void main(String[] args) {
	PrintWriter pw = null;
	try {
	    if (args.length != 3 && args.length != 2) {
		System.out.println("Usage: DataGenPoints <size> <output file name> [\"id\"]");
		return;
	    }

	    int size = Integer.parseInt(args[0]);
	    pw = new PrintWriter(new FileWriter(args[1], false));
	    boolean withid = false;
	    if (args.length == 3 && args[2].equalsIgnoreCase("id")) {
		withid = true;
	    }
	    Random rand = new Random();
	    rand.setSeed(System.currentTimeMillis());
	    for (int i = 0; i < size; ++i) {
		double x1 = rand.nextDouble() * (MAX_X - MIN_X) + MIN_X;
		double y1 = rand.nextDouble() * (MAX_Y - MIN_Y) + MIN_Y;
		pw.println((withid ? (i + ","):"") + x1 + "," + y1);
	    }
	    pw.flush();
	} catch (Exception e) {
	    e.printStackTrace();
	} finally {
	    if (null != pw)
		pw.close();
	}
    }

}
