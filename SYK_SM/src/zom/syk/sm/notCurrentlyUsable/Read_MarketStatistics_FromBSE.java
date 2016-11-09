package zom.syk.sm.notCurrentlyUsable;
import java.net.URL;
import java.util.Scanner;

public class Read_MarketStatistics_FromBSE {
	public static void main(String args[]) throws Exception {

		URL url = new URL("http://www.bseindia.com/Msource/corporate.aspx?flag=MKTSTS");
		Scanner scanner = new Scanner(url.openStream());
		String text = scanner.useDelimiter("\\A").next();
		scanner.close();

		text = text.substring(text.indexOf("<strong>")+8);
		double marketCap = Double.valueOf((text.substring(0, text.indexOf("<"))).replaceAll(",", ""));
		System.out.println(marketCap);
	}
}
