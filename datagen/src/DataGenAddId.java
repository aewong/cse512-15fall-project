import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;

public class DataGenAddId {
    public static void main(String[] args) {
	PrintWriter pw = null;
	BufferedReader rd = null;
	try {
	    if (args.length != 1) {
		System.out.println("Usage: DataGenAddId <file name>");
		return;
	    }

	    rd = new BufferedReader(new FileReader(args[0]));
	    pw = new PrintWriter(new FileWriter("id" + args[0], false));
	    String line = rd.readLine();
	    int linenum = 1;
	    while (null != line) {
		pw.println(linenum + "," + line);
		linenum++;
		line = rd.readLine();
	    }

	    pw.flush();
	} catch (Exception e) {
	    e.printStackTrace();
	} finally {
	    try {
		if (null != pw)
		    pw.close();
		if (null != rd)
		    rd.close();
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}
    }

}
