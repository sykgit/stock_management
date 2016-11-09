package zom.syk.sm.notCurrentlyUsable;

import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import com.syk.sm.utility.SM_Utilities;

public class Read_CompanyResultCalenderIndex_FromBSE {
	public static ArrayList<String> scripCodesErrors = new ArrayList<String>();
	public static ArrayList<String> scripCodesDiffYear = new ArrayList<String>();
	public static HashMap<String, Double> yearAdditions = new HashMap<String, Double>();

	public static void main(String args[]) throws Exception {
		Class.forName("oracle.jdbc.driver.OracleDriver");
		Connection con = DriverManager.getConnection(SM_Utilities.getSMProperty("dbUrl"), SM_Utilities.getSMProperty("dbUser"), SM_Utilities.getSMProperty("dbPwd"));
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("select DISTINCT T1 FROM TEMP");

		ArrayList<String> scripCodes = new ArrayList<String>();

		while (rs.next()) {
			scripCodes.add(rs.getString("T1"));
		}

		System.out.println("Records:\n" + scripCodes);
		System.out.println("\n\n");

		for (String scripCode : scripCodes) {
			findResult(scripCode);
		}

		System.out.println("Year Details Not Found:\n" + scripCodesDiffYear);
		System.out.println("\n\n");
		System.out.println("Exception:\n" + scripCodesErrors);
		System.out.println("\n\n");
		System.out.println("yearAdditions:\n" + yearAdditions);
	}

	private static void findResult(String scripCode) throws Exception {
		System.out.println("Getting Result for:" + scripCode);
		String html = getText(scripCode, "84.50");

		if (html.indexOf("Net Sales / Income from Operations") > -1) {
			yearAdditions.put(scripCode, 0.5);
			return;
		} else {
			html = getText(scripCode, "85.50");
			if (html.indexOf("Net Sales / Income from Operations") > -1) {
				yearAdditions.put(scripCode, 1.5);
				return;
			} else {
				html = getText(scripCode, "86.50");
				if (html.indexOf("Net Sales / Income from Operations") > -1) {
					yearAdditions.put(scripCode, 2.5);
					return;
				} else {
					html = getText(scripCode, "87.50");
					if (html.indexOf("Net Sales / Income from Operations") > -1) {
						yearAdditions.put(scripCode, 3.5);
						return;
					} else {
						scripCodesDiffYear.add(scripCode);
					}
				}
			}
		}
	}
	
	public static boolean isResultPresent(String html){
		if(html.indexOf("Net Sales / Income from Operations") > -1){
			return true;
		}else{
			
		}
		return false;
	}

	private static String getText(String scripCode, String qtr) throws Exception {
		URL url = new URL("http://www.bseindia.com/corporates/results.aspx?Code=" + scripCode + "&Company=C1&qtr=" + qtr + "&RType=D");
		InputStream is = null;
		boolean dataYetToBeRetrieved = true;
		while (dataYetToBeRetrieved) {
			try {
				is = url.openStream();
				dataYetToBeRetrieved = false;
			} catch (Exception exp) {
				System.out.println("!!!!ERROR!!!!");
				Thread.sleep(1000);
				url = new URL("http://www.bseindia.com/corporates/results.aspx?Code=" + scripCode + "&Company=C1&qtr=" + qtr + "&RType=D");
			}
		}
		Scanner scanner = new Scanner(is);
		String html = scanner.useDelimiter("\\A").next();
		scanner.close();
		return html;
	}
}
