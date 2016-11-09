import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.TimeZone;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.syk.sm.bean.AnalysisBseCompCallToMake;
import com.syk.sm.broker.SMBroker;
import com.syk.sm.datafetch.processor.RT_Prices_From_MoneyControl;
import com.syk.sm.utility.SM_Utilities;
import com.syk.sm.utility.SM_Utilities.COMPANY_NEWS_TYPE;

@SuppressWarnings("unused")
public class Test {


	public static void main24(String args[]) throws ParseException {
		Date d1 = getDateFromString("01/01/2014");
		Date d2 = getDateFromString("01/01/2016");

		Calendar accDate = Calendar.getInstance();
		accDate.setTime(d1);

		Calendar offerDate = Calendar.getInstance();
		offerDate.setTime(d2);

		System.out.println(accDate.getTime());
		System.out.println(offerDate.getTime());

		System.out.println("+++++++++++" + offerDate.get(Calendar.YEAR));

		int diffMonth = ((offerDate.get(Calendar.YEAR) - accDate.get(Calendar.YEAR)) * 12) + offerDate.get(Calendar.MONTH) - accDate.get(Calendar.MONTH);

		System.out.println(diffMonth);
		System.out.println(Math.floor(52.99));

		String output[] = new String[3];

		TreeSet<Account> ts = new TreeSet<Account>();

		ts.add(new Account("a", getDateFromString("01/12/2012"), 750.0, 1000.0));
		ts.add(new Account("b", getDateFromString("01/12/2011"), 10000.0, 10000.0));

		int i = 0;
		for (Account ac1 : ts) {
			output[i++] = ac1.getName();
		}

		for (String str : output) {
			System.out.println(str);
		}
	}

	private static String DATE_STRING_FORMAT = "MM/dd/yyyy";
	private static final String OFFER_START_DATE = "07/12/2014";

	/**
	 * Takes a properly formatted string and converts to a java.util.date.
	 * 
	 * @param date the date to parse
	 * @return a date as java.util.Date
	 * @throws ParseException
	 */
	private static Date getDateFromString(String date) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_STRING_FORMAT, Locale.ENGLISH);
		return sdf.parse(date);
	}

	/**
	 * Primary implementation method. Takes a list of accounts and returns the top 3 as a String array of names.
	 *
	 * @param accounts the list of accounts to evaluate.
	 * @return a String array of length X of the names of the top qualifying accounts.
	 */
	protected String[] getAccountsInOrder(List<Account> accounts) {
		String output[] = new String[3];
		if (accounts != null && accounts.size() > 0) {
			TreeSet<Account> ts = new TreeSet<Account>();

			for (Account ac1 : accounts) {
				ts.add(ac1);
			}

			int i = 0;
			for (Account ac1 : ts) {
				if (i < 3) {
					output[i++] = ac1.getName();
				} else {
					break;
				}
			}

		}
		return output;
	}

	/**
	 * Account object...the primary object for this class.
	 */
	protected static class Account implements Comparable<Account> {
		private String name;
		private Date accountOpenDate;
		private Double accountCurrentBalance;
		private Double accountCreditLimit;
		private int ageInMonthsAtTimeOfOffer;
		private Double accountScore;

		protected Account(String name, Date accountOpenDate, Double accountCurrentBalance, Double accountCreditLimit) throws ParseException {
			this.name = name;
			this.accountOpenDate = accountOpenDate;
			this.accountCurrentBalance = accountCurrentBalance;
			this.accountCreditLimit = accountCreditLimit;
			setAge();
			setScore();
		}

		public String getName() {
			return name;
		}

		public Date getAccountOpenDate() {
			return accountOpenDate;
		}

		public Double getAccountCurrentBalance() {
			return accountCurrentBalance;
		}

		public Double getAccountCreditLimit() {
			return accountCreditLimit;
		}

		public int getAgeInMonthsAtTimeOfOffer() {
			return ageInMonthsAtTimeOfOffer;
		}

		public Double getAccountScore() {
			return accountScore;
		}

		public double getCreditAvailable() {
			if (accountCreditLimit != null && accountCurrentBalance != null && accountCurrentBalance <= accountCreditLimit) {
				return accountCreditLimit - accountCurrentBalance;
			} else {
				return 0;
			}
		}

		public double getCreditAvailableAsPctOfLimit() {
			if (accountCreditLimit != null) {
				return getCreditAvailable() * 100 / accountCreditLimit;
			} else {
				return 0;
			}
		}

		public void setAge() throws ParseException {
			Date offerStartDate = getDateFromString(OFFER_START_DATE);
			if (accountOpenDate == null || accountOpenDate.after(offerStartDate)) {
				ageInMonthsAtTimeOfOffer = 0;
			} else {
				Calendar accDate = Calendar.getInstance();
				accDate.setTime(accountOpenDate);

				Calendar offerDate = Calendar.getInstance();
				offerDate.setTime(offerStartDate);

				ageInMonthsAtTimeOfOffer = ((offerDate.get(Calendar.YEAR) - accDate.get(Calendar.YEAR)) * 12) + offerDate.get(Calendar.MONTH) - accDate.get(Calendar.MONTH);
			}
		}

		public void setScore() {
			if (ageInMonthsAtTimeOfOffer > 24) {
				if (accountCurrentBalance != null && accountCurrentBalance >= 0 && accountCurrentBalance <= 10000) {
					if (accountCreditLimit != null && accountCreditLimit >= 1000 && accountCreditLimit <= 10000) {
						accountScore = getCreditAvailableAsPctOfLimit() * ageInMonthsAtTimeOfOffer;
					} else {
						accountScore = 0.0;
					}
				} else {
					accountScore = 0.0;
				}
			} else {
				accountScore = 0.0;
			}
		}

		@Override
		public int compareTo(Account o) {
			if (o != null) {
				if (this.getAccountScore() > o.getAccountScore()) {
					return -1;
				} else if (this.getAccountScore() < o.getAccountScore()) {
					return 1;
				}
			} else {
				return -1;
			}
			return 0;
		}
	}

	private static final String NO_TRANSACTIONS_ERR = "No transactions found.";
	private static final String NO_POSITIVE_ERR = "No positive transactions found.";

	public static void main666(String args[]) {
		Double d[] = { -1.01, -1.01, -23.01, -40.61, -16.54, -23.00, -123.10, -55.0, -10.0, .9 };
		System.out.println(getStatistics(d));
	}

	static String getStatistics(Double[] inputValues) {
		// No positive transactions found.
		String average = "Average: ";
		if (inputValues == null || inputValues.length == 0) {
			average = NO_TRANSACTIONS_ERR;
		} else {
			ArrayList<Double> positiveDoubles = new ArrayList<Double>();
			for (double val : inputValues) {
				if (val >= 0) {
					positiveDoubles.add(val);
				}
			}

			if (positiveDoubles.size() > 0) {
				double sum = 0.0;
				for (double val : positiveDoubles) {
					sum = sum + val;
				}
				double avg = sum / positiveDoubles.size();
				average = average + decimalFormatter(avg);
			} else {
				average = NO_POSITIVE_ERR;
			}
		}
		return average;
	}

	/**
	 * A helper method that returns a Double as a string in a 2 decimal format
	 *
	 * @param value the double value to format
	 * @return the value as a String with a 2 decimal format
	 */
	private static String decimalFormatter(Double value) {
		return String.format("%.2f", value);
	}

	public static void main23(String args[]) {
		boolean flag = false;
		int B = 0;
		int H = 0;

		try {
			Scanner sc = new Scanner(System.in);
			B = sc.nextInt();
			H = sc.nextInt();

			if (B > 0 && H > 0) {
				flag = true;
			} else {
				System.out.println("java.lang.Exception: Breadth and height must be positive");
			}
		} catch (Exception exp) {
			exp.printStackTrace();
		}
	}

	public static void mainCO5(String args[]) {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			String s = br.readLine();
			ArrayList<String> strList = new ArrayList<String>();
			while (s != null) {
				strList.add(s);
				s = br.readLine();
			}
			br.close();
			for (int i = 1; i <= strList.size(); i++) {
				System.out.println(i + " " + strList.get(i - 1));
			}
		} catch (Exception exp) {
			exp.printStackTrace();
		}
	}

	public static void mainCO4(String[] argh) {
		Scanner sc = new Scanner(System.in);
		int t = sc.nextInt();

		for (int i = 0; i < t; i++) {
			try {
				long x = sc.nextLong();
				System.out.println(x + " can be fitted in:");
				if (x >= -(new Double(Math.pow(2, 7)).longValue()) && x <= (new Double(Math.pow(2, 7)).longValue() - 1)) {
					System.out.println("* byte");
					System.out.println("* short");
					System.out.println("* int");
					System.out.println("* long");
				} else if (x >= -(new Double(Math.pow(2, 15)).longValue()) && x <= (new Double(Math.pow(2, 15)).longValue() - 1)) {
					System.out.println("* short");
					System.out.println("* int");
					System.out.println("* long");
				} else if (x >= -(new Double(Math.pow(2, 31)).longValue()) && x <= (new Double(Math.pow(2, 31)).longValue() - 1)) {
					System.out.println("* int");
					System.out.println("* long");
				} else {
					System.out.println("* long");
				}

			} catch (Exception e) {
				System.out.println(sc.next() + " can't be fitted anywhere.");
			}
		}
		sc.close();
	}

	public static void mainCO3(String args[]) {
		Scanner sc = new Scanner(System.in);
		int x = sc.nextInt();

		int a[] = new int[x];
		int b[] = new int[x];
		int n[] = new int[x];

		for (int i = 0; i < x; i++) {
			a[i] = sc.nextInt();
			b[i] = sc.nextInt();
			n[i] = sc.nextInt();
		}

		for (int i = 0; i < x; i++) {
			for (int k = 0; k < n[i]; k++) {
				if (k == 0) {
					int value = a[i] + new Double((b[i] * Math.pow(2, 0))).intValue();
					System.out.print(value);
				} else {
					int value = a[i] + new Double((b[i] * Math.pow(2, 0))).intValue();
					System.out.print(" ");
					for (int m = 1; m <= k; m++) {
						value = value + new Double((b[i] * Math.pow(2, m))).intValue();
					}
					System.out.print(value);
				}
			}
			System.out.println();
		}
		sc.close();
	}

	public static void mainCO2(String args[]) {
		String str = "hello";
		StringBuilder spaces = new StringBuilder();
		for (int i = str.length(); i < 15; i++) {
			spaces.append(" ");
		}
		str = str + spaces.toString();
		System.out.println(str);
		System.out.println(str.length());

		System.out.printf("%08d", 42);
	}

	public static void mainCO1(String args[]) {
		Scanner sc = new Scanner(System.in);
		int x = sc.nextInt();
		double y = sc.nextDouble();
		sc.nextLine();
		String s = sc.nextLine();
		sc.close();
		System.out.println("String: " + s);
		System.out.println("Double: " + y);
		System.out.println("Int: " + x);
	}

	static void readIntsFromStdIn(int[] nums) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

	}

	private static int readIntFromStdIn() {
		// TODO Auto-generated method stub
		return 0;
	}

	public static void main22(String args[]) {
		DateTime dateTime = DateTime.now(DateTimeZone.forID("Asia/Kolkata"));
		DateTimeFormatter formatter = DateTimeFormat.forPattern("HH@@@mm");
		String dateTimeStr = formatter.print(dateTime.toLocalDateTime().toDateTime().getMillis());
		String hhmi[] = dateTimeStr.split("@@@");

		int hh = Integer.parseInt(hhmi[0]);
		int mi = Integer.parseInt(hhmi[1]);

		System.out.println(dateTimeStr);
		System.out.println(hh);
		System.out.println(mi);
	}

	public static void main21(String args[]) throws ClassNotFoundException, Exception {
		// Class.forName("oracle.jdbc.driver.OracleDriver");
		// Connection con = DriverManager.getConnection(SM_Utilities.getSMProperty("dbUrl"), SM_Utilities.getSMProperty("dbUser"), SM_Utilities.getSMProperty("dbPwd"));
		//
		// System.out.println(SMBroker.getScripCodeScripIDMapFor_AB_Group(con));
		// String scripID="ONMOBILE*";
		// scripID = scripID.replaceAll("[*]", "");
		// System.out.println(scripID);

		String html = SM_Utilities.getURLContentAsString(SM_Utilities.getSMProperty("URL_ScripIDIndexFaceValueIndustry").replaceAll("@SCRIP_CODE@", "532944"));

		if (html != null && html.indexOf("No record found") < 0) {
			String secId = html.substring(html.lastIndexOf("Security ID"));
			secId = secId.substring(secId.indexOf("'7%'"));
			secId = secId.substring(secId.indexOf(">") + 1);
			secId = secId.substring(0, secId.indexOf("<")).trim();
			if (secId.length() == 0) {
				throw new Exception("SECURITY_ID is empty");
			}
			System.out.println(secId);
		}
	}

	public static void main20(String args[]) throws Exception {
		String html = SM_Utilities.getURLContentAsString("https://www.nseindia.com/live_market/dynaContent/live_watch/get_quote/GetQuote.jsp?symbol=IBREALEST&illiquid=0&smeFlag=0&itpFlag=0");
		// System.out.println(html);
		html = html.substring(html.indexOf("{\"futLink\":"), html.indexOf("optLink\":"));
		System.out.println(html);

		DateTime dateTime = DateTime.now(DateTimeZone.forID("Asia/Kolkata"));
		DateTimeFormatter formatter = DateTimeFormat.forPattern("ddMMMYYYY");
		String dateTimeStr = formatter.print(dateTime.toLocalDateTime().toDateTime().getMillis());

		System.out.println(dateTimeStr.toUpperCase());
		dateTimeStr = dateTimeStr.toUpperCase();
		// dateTimeStr = "24MAY2016";

		if (html != null && html.indexOf(dateTimeStr) > -1) {
			double currentPrice = getDoubleValueFromNSEStr(html.substring(html.indexOf("\"lastPrice\":\"") + 13));
			double currentVolume = getDoubleValueFromNSEStr(html.substring(html.indexOf("\"quantityTraded\":\"") + 18));
			long totalBuyQty = getDoubleValueFromNSEStr(html.substring(html.indexOf("\"totalBuyQuantity\":\"") + 20)).longValue();
			long totalSellQty = getDoubleValueFromNSEStr(html.substring(html.indexOf("\"totalSellQuantity\":\"") + 21)).longValue();

			int buyQty1 = getDoubleValueFromNSEStr(html.substring(html.indexOf("\"buyQuantity1\":\"") + 16)).intValue();
			int buyQty2 = getDoubleValueFromNSEStr(html.substring(html.indexOf("\"buyQuantity2\":\"") + 16)).intValue();
			int buyQty3 = getDoubleValueFromNSEStr(html.substring(html.indexOf("\"buyQuantity3\":\"") + 16)).intValue();
			int buyQty4 = getDoubleValueFromNSEStr(html.substring(html.indexOf("\"buyQuantity4\":\"") + 16)).intValue();
			int buyQty5 = getDoubleValueFromNSEStr(html.substring(html.indexOf("\"buyQuantity5\":\"") + 16)).intValue();
			long top5BuyQty = buyQty1 + buyQty2 + buyQty3 + buyQty4 + buyQty5;

			int sellQty1 = getDoubleValueFromNSEStr(html.substring(html.indexOf("\"sellQuantity1\":\"") + 17)).intValue();
			int sellQty2 = getDoubleValueFromNSEStr(html.substring(html.indexOf("\"sellQuantity2\":\"") + 17)).intValue();
			int sellQty3 = getDoubleValueFromNSEStr(html.substring(html.indexOf("\"sellQuantity3\":\"") + 17)).intValue();
			int sellQty4 = getDoubleValueFromNSEStr(html.substring(html.indexOf("\"sellQuantity4\":\"") + 17)).intValue();
			int sellQty5 = getDoubleValueFromNSEStr(html.substring(html.indexOf("\"sellQuantity5\":\"") + 17)).intValue();
			long top5SellQty = sellQty1 + sellQty2 + sellQty3 + sellQty4 + sellQty5;

			System.out.println("currentPrice:" + currentPrice);
			System.out.println("currentVolume:" + currentVolume);
			System.out.println("totalBuyQty:" + totalBuyQty);
			System.out.println("totalSellQty:" + totalSellQty);
			System.out.println("top5BuyQty:" + top5BuyQty);
			System.out.println("top5SellQty:" + top5SellQty);
		}

	}

	private static Double getDoubleValueFromNSEStr(String nseStr) {
		double val = 0.0;
		try {
			if (nseStr != null) {
				nseStr = nseStr.substring(0, nseStr.indexOf("\""));
				nseStr = nseStr.replaceAll(",", "");
				val = Double.valueOf(nseStr);
			}

		} catch (Exception exp) {
			System.out.println("Eating Away Exception:" + exp);
		}
		return val;
	}

	public static void main19(String args[]) {
		System.out.println("Hello");
		System.out.println(Calendar.getInstance().getTime());
		System.out.println(Calendar.getInstance(TimeZone.getTimeZone("America/New_York")).getTime());
		System.out.println(TimeZone.getDefault().inDaylightTime(new Date()));
		System.out.println(Calendar.getInstance(TimeZone.getTimeZone("GMT+5.30")).getTime());
		System.out.println(TimeZone.getAvailableIDs()[0].toString());

		DateTime dateTime = DateTime.now(DateTimeZone.forID("Asia/Kolkata"));
		DateTimeFormatter formatter = DateTimeFormat.forPattern("YYYYMMdd");
		System.out.println(formatter.print(dateTime.toLocalDateTime().toDateTime().getMillis()));
		System.out.println(dateTime);

	}

	public static void main18(String args[]) throws Exception {
		// String html = SM_Utilities.getURLContentAsString("http://www.google.com/finance/info?infotype=infoquoteall&q=BOM:533321");
		String html = SM_Utilities.getURLContentAsString(SM_Utilities.getSMProperty("URL_RealTimePrices_FromGoogle").replaceAll("@SCRIP_CODE@", "" + 533321));
		System.out.println(html);

		// String currPriceStr = html.substring(html.indexOf("\"l\" : \"") + 7);
		// currPriceStr = currPriceStr.substring(0, currPriceStr.indexOf("\""));

		double currPrice = getGooogleValue("l", html);
		double openPrice = getGooogleValue("op", html);
		double volume = getGooogleValue("vo", html);

		System.out.println(currPrice);
		System.out.println(openPrice);
		System.out.println(volume);

	}

	private static double getGooogleValue(String token, String html) {
		double ret = 0.0;
		try {
			String tokenValStr = html.substring(html.indexOf("\"" + token + "\" : \"") + 6 + token.length());
			tokenValStr = tokenValStr.substring(0, tokenValStr.indexOf("\""));

			tokenValStr = tokenValStr.replaceAll(",", "");
			ret = Double.valueOf(tokenValStr);
		} catch (Exception exp) {
		}
		return ret;
	}

	public static void main17(String args[]) throws Exception {
		Class.forName("oracle.jdbc.driver.OracleDriver");
		Connection con = DriverManager.getConnection(SM_Utilities.getSMProperty("dbUrl"), SM_Utilities.getSMProperty("dbUser"), SM_Utilities.getSMProperty("dbPwd"));

		SMBroker.deleteIgnored_UC_Companies(con);
	}

	public static void main16(String args[]) throws Exception {
		Class.forName("oracle.jdbc.driver.OracleDriver");
		Connection con = DriverManager.getConnection(SM_Utilities.getSMProperty("dbUrl"), SM_Utilities.getSMProperty("dbUser"), SM_Utilities.getSMProperty("dbPwd"));

		AnalysisBseCompCallToMake analysisBseCompCallToMake = new AnalysisBseCompCallToMake();
		analysisBseCompCallToMake.setScripCode(533540);
		RT_Prices_From_MoneyControl.updateScripCodeCompanyNameMap(con);
		RT_Prices_From_MoneyControl.getRealTimePrice(analysisBseCompCallToMake);
		System.out.println(analysisBseCompCallToMake);

	}

	public static void main14(String args[]) throws Exception {
		String splitCompName[] = "Rohit".split(" ");
		System.out.println(splitCompName.length);
		System.out.println(splitCompName[0]);
	}

	public static void main15(String args[]) throws Exception {
		Class.forName("oracle.jdbc.driver.OracleDriver");
		Connection con = DriverManager.getConnection(SM_Utilities.getSMProperty("dbUrl"), SM_Utilities.getSMProperty("dbUser"), SM_Utilities.getSMProperty("dbPwd"));

		HashMap<Integer, String> scripCodeCompanyNameMap = SMBroker.getScripCodeCompanyNameMap(con);

		String url = SM_Utilities.getURLContentAsString("http://www.moneycontrol.com/mccode/common/autosuggesion.php?query=539313&type=1&format=json");
		url = url.substring(url.indexOf(":\"") + 2);
		url = url.substring(0, url.indexOf("\""));

		System.out.println(url);

		if (url != null && url.indexOf("javascript:") > -1) {
			String compName = scripCodeCompanyNameMap.get(539331);
			if (compName != null) {
				String splitCompName[] = compName.split(" ");
				if (splitCompName.length > 1) {
					compName = splitCompName[0] + "%20" + splitCompName[1];
				}

				url = SM_Utilities.getURLContentAsString("http://www.moneycontrol.com/mccode/common/autosuggesion.php?query=" + compName + "&type=1&format=json");
				url = url.substring(url.indexOf(":\"") + 2);
				url = url.substring(0, url.indexOf("\""));

				System.out.println(url);
			}
		}

		String html = SM_Utilities.getURLContentAsString(url);
		html = html.substring(html.indexOf("b_low_price_limit\">") + 19);

		String lowerCktStr = html.substring(0, html.indexOf("<"));
		if (lowerCktStr != null && lowerCktStr.equals("-")) {
			// http://www.moneycontrol.com/india/stockpricequote/computerssoftwaretraining/treehouseeducationaccessories/THE01
			url = url.substring(url.lastIndexOf("/") + 1);
			System.out.println(url);

			html = SM_Utilities.getURLContentAsString("http://pricefeed.moneycontrol.com/mc_get/g.js?key=stk_" + url + "_B&type=mget&callback=jsonCallback");
			html = html.substring(html.indexOf("lower_circuit_limit\\\":\\\"") + 24);
			lowerCktStr = html.substring(0, html.indexOf("\\"));
			lowerCktStr = lowerCktStr.replaceAll(",", "");

			html = html.substring(html.indexOf("upper_circuit_limit\\\":\\\"") + 24);
			html = html.substring(0, html.indexOf("\\"));
			html = html.replaceAll(",", "");

			System.out.println(html);

		} else {
			html = html.substring(html.indexOf("b_high_price_limit\">") + 20);
			html = html.substring(0, html.indexOf("<"));
		}

		double lowerCkt = Double.valueOf(lowerCktStr);
		double upperCkt = Double.valueOf(html);

		System.out.println(lowerCkt);
		System.out.println(upperCkt);

		// select company_name from data_bse_company where scrip_code=539122
	}

	public static void main12(String args[]) throws Exception {
		// Class.forName("oracle.jdbc.driver.OracleDriver");
		// Connection con = DriverManager.getConnection(SM_Utilities.getSMProperty("dbUrl"), SM_Utilities.getSMProperty("dbUser"), SM_Utilities.getSMProperty("dbPwd"));

		// ArrayList<DataBseCompanyBean> companyBeans = SMBroker.getDataBseCompanyBeans(con, false);
		//
		// ArrayList<DataBseDailyTradeBean> dailyTrades = Read_CircuitHighLows_FromBSE.read(companyBeans, true);
		//
		// for (DataBseDailyTradeBean dailyTrade : dailyTrades) {
		// System.out.println(dailyTrade);
		// }

		// Update_AnalysisBseCompCallToMake.update(con);
		// String str = "1234";
		// System.out.println(str.substring(0,16));

		// String html = SM_Utilities.getURLContentAsString("http://www.bseindia.com");
		String html1 = SM_Utilities.getURLContentAsString("http://www.bseindia.com/stock-share-price/SiteCache/EQHeaderData.aspx?text=532540");

		System.out.println(html1);
	}

	public static void main11(String args[]) throws Exception {
		long t1 = System.currentTimeMillis();
		String html = SM_Utilities.getURLContentAsString("http://www.bseindia.com/stock-share-price/SiteCache/Stock_Trading.aspx?text=531879&type=EQ");

		Document doc = Jsoup.parse(html);
		Elements els = doc.getElementsByClass("TTHeadergrey");

		if (els != null) {
			for (int i = 0; i < els.size(); i = i + 4) {
				String type = els.get(i + 1).outerHtml();
				type = type.substring(0, type.length() - 1);
				type = (type.substring(type.lastIndexOf(">") + 1, type.lastIndexOf("<"))).trim();

				String message = els.get(i).outerHtml();
				message = message.substring(0, message.length() - 1);
				message = message.replaceAll("</a>", "");
				message = (message.substring(message.lastIndexOf(">") + 1, message.lastIndexOf("<"))).trim();

				System.out.println(type + " | " + message);
			}
		}
		System.out.println(System.currentTimeMillis() - t1);
	}

	public static void main10(String args[]) throws Exception {
		long t1 = System.currentTimeMillis();
		String html = SM_Utilities.getURLContentAsString("http://www.bseindia.com/corporates/ann.aspx?curpg=1&annflag=1&dt=&dur=D&dtto=&cat=&scrip=534731&anntype=A");

		if (html.indexOf("TTHeadergrey") > -1) {
			html = html.substring(html.indexOf("<span id=\"ctl00_ContentPlaceHolder1_lblann\">"));
			html = html.substring(0, html.indexOf("</span>") + 7);
			Document doc = Jsoup.parse(html);
			Elements els = doc.getElementsByClass("TTHeadergrey");

			if (els != null) {
				for (int i = 0; i < els.size(); i = i + 4) {
					String type = els.get(i + 1).outerHtml();
					type = type.substring(0, type.length() - 1);
					type = (type.substring(type.lastIndexOf(">") + 1, type.lastIndexOf("<"))).trim();

					String message = els.get(i).outerHtml();
					message = message.substring(0, message.length() - 1);
					message = message.replaceAll("</a>", "");
					message = (message.substring(message.lastIndexOf(">") + 1, message.lastIndexOf("<"))).trim();

					System.out.println(type + " | " + message);
				}
			}
		}
		System.out.println(System.currentTimeMillis() - t1);
	}

	public static void main9(String args[]) throws InterruptedException, ExecutionException {
		ExecutorService executor = Executors.newFixedThreadPool(2);

		MyCallable callable1 = new MyCallable(10000);
		MyCallable callable2 = new MyCallable(2000);

		FutureTask<String> futureTask1 = new FutureTask<String>(callable1);
		FutureTask<String> futureTask2 = new FutureTask<String>(callable2);

		executor.execute(futureTask1);
		executor.execute(futureTask2);

		long t1 = System.currentTimeMillis();
		System.out.println("futureTask2:" + futureTask2.get());
		System.out.println(System.currentTimeMillis() - t1);
		t1 = System.currentTimeMillis();
		System.out.println("futureTask1:" + futureTask1.get());
		System.out.println(System.currentTimeMillis() - t1);
		t1 = System.currentTimeMillis();
		System.out.println("futureTask2:" + futureTask2.get());
		System.out.println(System.currentTimeMillis() - t1);
		t1 = System.currentTimeMillis();

		executor.shutdown();
	}

	public static class MyCallable implements Callable<String> {

		private long waitTime;

		public MyCallable(int timeInMillis) {
			this.waitTime = timeInMillis;
		}

		@Override
		public String call() throws Exception {
			Thread.sleep(waitTime);
			// return the thread name executing this callable task
			return Thread.currentThread().getName();
		}

	}

	public static void main8(String[] args) throws Exception {
		Calendar newsDate = null;

		if (newsDate == null) {
			newsDate = Calendar.getInstance();
			newsDate.set(Calendar.YEAR, 2016);
			newsDate.set(Calendar.MONTH, 1);
			newsDate.set(Calendar.DATE, 1);
		}

		System.out.println(newsDate.getTime());
		System.out.println(newsDate.get(Calendar.MONTH));

		/// Actual Code

		Calendar currentDate = Calendar.getInstance();
		Calendar compareDate = Calendar.getInstance();
		int currentMonth = currentDate.get(Calendar.MONTH);

		if (currentMonth >= 0 && currentMonth < 3) {
			compareDate.set(Calendar.YEAR, currentDate.get(Calendar.YEAR));
			compareDate.set(Calendar.MONTH, 0);
			compareDate.set(Calendar.DATE, 1);
		} else if (currentMonth >= 3 && currentMonth < 6) {
			compareDate.set(Calendar.YEAR, currentDate.get(Calendar.YEAR));
			compareDate.set(Calendar.MONTH, 3);
			compareDate.set(Calendar.DATE, 1);
		} else if (currentMonth >= 6 && currentMonth < 9) {
			compareDate.set(Calendar.YEAR, currentDate.get(Calendar.YEAR));
			compareDate.set(Calendar.MONTH, 6);
			compareDate.set(Calendar.DATE, 1);
		} else {
			compareDate.set(Calendar.YEAR, currentDate.get(Calendar.YEAR));
			compareDate.set(Calendar.MONTH, 9);
			compareDate.set(Calendar.DATE, 1);
		}

		if (SM_Utilities.isDateAfter(newsDate, compareDate)) {
			System.out.println("TRUE");
		}

	}

	public static void main7(String[] args) throws Exception {

		Calendar maxAnalysisDate = null;

		if (maxAnalysisDate == null) {
			maxAnalysisDate = Calendar.getInstance();
			maxAnalysisDate.set(Calendar.YEAR, 2016);
			maxAnalysisDate.set(Calendar.MONTH, 1);
			maxAnalysisDate.set(Calendar.DATE, 1);
		}

		Class.forName("oracle.jdbc.driver.OracleDriver");
		Connection con = DriverManager.getConnection(SM_Utilities.getSMProperty("dbUrl"), SM_Utilities.getSMProperty("dbUser"), SM_Utilities.getSMProperty("dbPwd"));

		// System.out.println(SM_Utilities.quoteStringsInListForSql(SMBroker.getDistinctTradingDates(con)));

		String HTML = Request.Post("http://www.bseindia.com/corporates/Comp_Resultsnew.aspx")
				.bodyForm(Form.form().add("ctl00$ContentPlaceHolder1$periioddd", "ALL").add("ctl00$ContentPlaceHolder1$industrydd", "ALL").add("ctl00$ContentPlaceHolder1$broadcastdd", "6")
						.add("ctl00$ContentPlaceHolder1$btnSubmit.x", "36").add("ctl00$ContentPlaceHolder1$btnSubmit.y", "16").add("ctl00$ContentPlaceHolder1$hdnCode", "500877").build())
				.execute().returnContent().asString();

		System.out.println(HTML);

	}

	public static void main6(String[] args) throws Exception {
		COMPANY_NEWS_TYPE type = COMPANY_NEWS_TYPE.INSIDER_TRADING_SAST;
		System.out.println(type.toString());
		System.out.println(COMPANY_NEWS_TYPE.valueOf(type.toString()));
		double d1 = 80.00;
		double d2 = 80.5;

		System.out.println(((int) d1) == d1);
		System.out.println(((int) d2) == d2);
	}

	public static void main5(String[] args) throws Exception {
		Calendar current = Calendar.getInstance();
		int day = current.get(Calendar.DATE);
		int month = current.get(Calendar.MONTH);
		int year = current.get(Calendar.YEAR);

		System.out.println(day);
		System.out.println(month);
		System.out.println(year);

		int qtrYr[] = SM_Utilities.getMaxQtrAndYearToCheck();
		System.out.println(qtrYr[0]);
		System.out.println(qtrYr[1]);
	}

	public static void main4(String[] args) throws Exception {
		System.out.println(SM_Utilities.formatDoubleToTwoDecimals(2.37999999999));
		System.out.println(String.format("%.2f", 2.38999999999));
	}

	public static void main3(String[] args) throws Exception {
		System.out.println("START");
		Class.forName("oracle.jdbc.driver.OracleDriver");
		Connection con = DriverManager.getConnection(SM_Utilities.getSMProperty("dbUrl"), SM_Utilities.getSMProperty("dbUser"), SM_Utilities.getSMProperty("dbPwd"));
		con.setAutoCommit(false);
		PreparedStatement updateQueryDeleteFlagStmt = con.prepareStatement("update DATA_BSE_COMPANY set deleted_flag='Y' where scrip_code=?");
		updateQueryDeleteFlagStmt.executeBatch();
		con.commit();
		updateQueryDeleteFlagStmt.close();
		System.out.println("END");
	}

	public static void main2(String[] args) throws Exception {
		long currenTime = System.currentTimeMillis();
		URL url = new URL("http://www.bseindia.com/stock-share-price/SiteCache/52WeekHigh.aspx?Type=EQ&text=" + 500209);
		Scanner scanner = new Scanner(url.openStream());
		String text = scanner.useDelimiter("\\A").next();
		scanner.close();

		Document doc = Jsoup.parse(text);
		Elements els = doc.getElementsByClass("newseoscripfig");

		System.out.println();
		if (text != null) {
			text = null;
			String tempStr = els.get(0).text();
			double yearHighVal = Double.valueOf((tempStr.substring(0, tempStr.indexOf("("))).replaceAll(",", ""));
			String yearHighDate = tempStr.substring(tempStr.indexOf("(") + 1, tempStr.indexOf(")"));
			System.out.println("yearHighVal:" + yearHighVal);
			System.out.println("yearHighDate:" + yearHighDate);

			tempStr = els.get(1).text();
			double yearLowVal = Double.valueOf((tempStr.substring(0, tempStr.indexOf("("))).replaceAll(",", ""));
			String yearLowDate = tempStr.substring(tempStr.indexOf("(") + 1, tempStr.indexOf(")"));
			System.out.println("yearLowVal:" + yearLowVal);
			System.out.println("yearLowDate:" + yearLowDate);

		}
		System.out.println("Time Taken:" + (System.currentTimeMillis() - currenTime));
	}

	public static void main1(String[] args) throws Exception {
		long currenTime = System.currentTimeMillis();
		URL url = new URL("http://www.bseindia.com/stock-share-price/SiteCache/52WeekHigh.aspx?Type=EQ&text=" + 500209);
		Scanner scanner = new Scanner(url.openStream());
		String text = scanner.useDelimiter("\\A").next();
		scanner.close();

		if (text != null) {
			String usableText = text.substring(text.indexOf("newseoscripfig"));
			text = null;
			String tempStr = usableText.substring(usableText.indexOf(">") + 1, usableText.indexOf("<"));
			double yearHighVal = Double.valueOf((tempStr.substring(0, tempStr.indexOf("("))).replaceAll(",", ""));
			String yearHighDate = tempStr.substring(tempStr.indexOf("(") + 1, tempStr.indexOf(")"));
			System.out.println("yearHighVal:" + yearHighVal);
			System.out.println("yearHighDate:" + yearHighDate);

			usableText = usableText.substring(usableText.indexOf("newseoscripfig"));
			tempStr = usableText.substring(usableText.indexOf(">") + 1, usableText.indexOf("<"));
			usableText = null;
			double yearLowVal = Double.valueOf((tempStr.substring(0, tempStr.indexOf("("))).replaceAll(",", ""));
			String yearLowDate = tempStr.substring(tempStr.indexOf("(") + 1, tempStr.indexOf(")"));
			System.out.println("yearLowVal:" + yearLowVal);
			System.out.println("yearLowDate:" + yearLowDate);

		}
		System.out.println("Time Taken:" + (System.currentTimeMillis() - currenTime));
	}
}
