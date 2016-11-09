package zom.syk.sm.notCurrentlyUsable;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Scanner;

import com.syk.sm.utility.SM_Utilities;

public class Read_CurrencyValuations_FromBSE_And_ExtSource {
	public static void main(String[] args) throws Exception {
		Class.forName("oracle.jdbc.driver.OracleDriver");
		Connection con = DriverManager.getConnection(SM_Utilities.getSMProperty("dbUrl"), SM_Utilities.getSMProperty("dbUser"), SM_Utilities.getSMProperty("dbPwd"));
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("select distinct(date_of_trade) from DATA_BSE_DAILY_TRADE where date_of_trade > to_date('20160119','yyyyMMdd') order by date_of_trade desc");

		ArrayList<String> marketDates = new ArrayList<String>();

		Calendar today = Calendar.getInstance();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		while (rs.next()) {
			marketDates.add(dateFormat.format(new Date(rs.getDate("date_of_trade").getTime())));
		}
		rs.close();
		stmt.close();
		con.close();

		for (int i = 0; i < marketDates.size(); i++) {
			if (i == 0) {
				if (marketDates.get(0).equals(dateFormat.format(today.getTime()))) {
					getDateFromBse();
				} else {
					getDateFromExternalSource(marketDates.get(i));
				}
			} else {
				getDateFromExternalSource(marketDates.get(i));
			}
		}

	}

	public static void getDateFromExternalSource(String marketDate) throws Exception {
		DecimalFormat myFormatter = new DecimalFormat(".##");
		double dblVal = -1;

		String text = getDataFromStream("http://api.fixer.io/" + marketDate + "?base=USD&symbols=INR");
		if (text != null) {
			text = text.substring(text.indexOf("INR") + 5);
			dblVal = Double.valueOf(text.substring(0, text.indexOf("}")));
			dblVal = Double.valueOf(myFormatter.format(dblVal));
			System.out.println("USD:" + dblVal);
		} else {
			System.out.println("USD:" + dblVal);
		}

		dblVal = -1;
		text = getDataFromStream("http://api.fixer.io/" + marketDate + "?base=GBP&symbols=INR");
		if (text != null) {
			text = text.substring(text.indexOf("INR") + 5);
			dblVal = Double.valueOf(text.substring(0, text.indexOf("}")));
			dblVal = Double.valueOf(myFormatter.format(dblVal));
			System.out.println("GBP:" + dblVal);
		} else {
			System.out.println("GBP:" + dblVal);
		}

		dblVal = -1;
		text = getDataFromStream("http://api.fixer.io/" + marketDate + "?base=EUR&symbols=INR");
		if (text != null) {
			text = text.substring(text.indexOf("INR") + 5);
			dblVal = Double.valueOf(text.substring(0, text.indexOf("}")));
			dblVal = Double.valueOf(myFormatter.format(dblVal));
			System.out.println("EUR:" + dblVal);
		} else {
			System.out.println("EUR:" + dblVal);
		}

		dblVal = -1;
		text = getDataFromStream("http://api.fixer.io/" + marketDate + "?base=JPY&symbols=INR");
		if (text != null) {
			text = text.substring(text.indexOf("INR") + 5);
			dblVal = Double.valueOf(text.substring(0, text.indexOf("}")));
			dblVal = dblVal * 100;
			dblVal = Double.valueOf(myFormatter.format(dblVal));
			System.out.println("JPY:" + dblVal);
		} else {
			System.out.println("JPY:" + dblVal);
		}
	}

	private static String getDataFromStream(String urlStr) {
		String text = null;
		try {
			URL url = new URL(urlStr);
			Scanner scanner = new Scanner(url.openStream());
			text = scanner.useDelimiter("\\A").next();
			scanner.close();
		} catch (Exception exp) {
			exp.printStackTrace();
		}
		return text;
	}

	public static void getDateFromBse() throws Exception {

		DecimalFormat myFormatter = new DecimalFormat(".##");
		URL url = new URL("http://www.bseindia.com/Msource/FlagData.aspx");
		Scanner scanner = new Scanner(url.openStream());
		String text = scanner.useDelimiter("\\A").next();
		scanner.close();

		if (text != null) {
			System.out.println(text);

			text = text.substring(text.indexOf("USD@") + 4);
			double dblVal = Double.valueOf(text.substring(0, text.indexOf("#")));
			dblVal = Double.valueOf(myFormatter.format(dblVal));
			System.out.println("USD:" + dblVal);

			text = text.substring(text.indexOf("GBP@") + 4);
			dblVal = Double.valueOf(text.substring(0, text.indexOf("#")));
			dblVal = Double.valueOf(myFormatter.format(dblVal));
			System.out.println("GBP:" + dblVal);

			text = text.substring(text.indexOf("EUR@") + 4);
			dblVal = Double.valueOf(text.substring(0, text.indexOf("#")));
			dblVal = Double.valueOf(myFormatter.format(dblVal));
			System.out.println("EUR:" + dblVal);

			text = text.substring(text.indexOf("JPY@") + 4);
			dblVal = Double.valueOf(text.substring(0, text.indexOf("#")));
			dblVal = Double.valueOf(myFormatter.format(dblVal));
			System.out.println("JPY:" + dblVal);
		}

	}
}
