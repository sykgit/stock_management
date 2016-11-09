package zom.syk.sm.notCurrentlyUsable;
import java.net.URL;
import java.util.Scanner;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class Read_DailyCircuitLimit_ForStock_FromBSE {
	public static void main(String args[]) throws Exception {
		URL url = new URL("http://www.bseindia.com/stock-share-price/SiteCache/Stock_Trading.aspx?text=500209&type=EQ");
		Scanner scanner = new Scanner(url.openStream());
		String text = scanner.useDelimiter("\\A").next();
		scanner.close();
		System.out.println("text:\n" + text);

		Document doc = Jsoup.parse(text);
		Element elem = doc.getElementById("ehd5");
		
		System.out.println(elem.val());
		
	}
}
