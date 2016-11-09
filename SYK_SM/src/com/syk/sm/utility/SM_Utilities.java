/**
 * 
 */
package com.syk.sm.utility;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.RoundingMode;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Properties;
import java.util.Scanner;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;

/**
 * @author skuppuraju
 *
 */
public final class SM_Utilities {
	private static final Properties properties = new Properties();
	private static Logger LOGGER = Logger.getLogger("SM_Log");
	private static Formatter logFormatter = new SM_LogFormatter();
	private static DecimalFormat df2 = new DecimalFormat("#0.00");
	private static FileHandler fh = null;
	private static String tradingHolidaysHtml;
	
	static {
		try {
			ClassLoader classLoader = SM_Utilities.class.getClassLoader();
			properties.load(classLoader.getResourceAsStream("sm.properties"));
			LOGGER.setUseParentHandlers(false);
			fh = new FileHandler(properties.getProperty("logFile"));
			fh.setFormatter(logFormatter);
			LOGGER.addHandler(fh);
			// ConsoleHandler ch = new ConsoleHandler();
			// ch.setFormatter(logFormatter);
			// LOGGER.addHandler(ch);
			df2.setRoundingMode(RoundingMode.DOWN);
			tradingHolidaysHtml = FileUtils.readFileToString(new File(SM_Utilities.getSMProperty("html_BSE_Holidays")), "utf-8");
		} catch (Exception exp) {
			exp.printStackTrace();
			System.err.println("SM_Utilities | Properties not loaded successfully.");
		}
	}

	public static enum COMPANY_NEWS_TYPE {
		AGM_EGM, BOARD_MEETING, COMPANY_UPDATE, CORP_ACTION, INSIDER_TRADING_SAST, NEW_LISTING, RESULT;

		public String toString() {
			switch (this) {
				case AGM_EGM:
					return "AGM/EGM";
				case BOARD_MEETING:
					return "Board Meeting";
				case COMPANY_UPDATE:
					return "Company Update";
				case CORP_ACTION:
					return "Corp. Action";
				case INSIDER_TRADING_SAST:
					return "Insider Trading / SAST";
				case NEW_LISTING:
					return "New Listing";
				case RESULT:
					return "Result";
				default:
					return "";
			}
		}

		public static COMPANY_NEWS_TYPE getEnum(String str) {
			if (str != null) {
				if (str.equals("AGM/EGM")) {
					return COMPANY_NEWS_TYPE.AGM_EGM;
				} else if (str.equals("Board Meeting")) {
					return COMPANY_NEWS_TYPE.BOARD_MEETING;
				} else if (str.equals("Company Update")) {
					return COMPANY_NEWS_TYPE.COMPANY_UPDATE;
				} else if (str.equals("Corp. Action")) {
					return COMPANY_NEWS_TYPE.CORP_ACTION;
				} else if (str.equals("Insider Trading / SAST")) {
					return COMPANY_NEWS_TYPE.INSIDER_TRADING_SAST;
				} else if (str.equals("New Listing")) {
					return COMPANY_NEWS_TYPE.NEW_LISTING;
				} else if (str.equals("Result")) {
					return COMPANY_NEWS_TYPE.RESULT;
				} else {
					return null;
				}
			}
			return null;
		}
	}

	public static enum CALL_TO_MAKE {
		BUY, SELL, SELL_ELIGIBLE, NO_CALL
	}

	public static final Properties getSMProperties() {
		return properties;
	}

	public static final String getSMProperty(String key) {
		return properties.getProperty(key);
	}

	public static final void logConsole(String logText) {
		System.out.println(logText);
		log(logText);
	}

	public static final void log(String logText) {
		LOGGER.log(Level.INFO, logText);
	}

	public static InputStream getURLContentAsStream(String urlStr) throws Exception {
		InputStream is = null;
		URL url = new URL(urlStr);
		boolean dataYetToBeRetrieved = true;
		while (dataYetToBeRetrieved) {
			try {
				is = url.openStream();
				dataYetToBeRetrieved = false;
			} catch (Exception exp) {
				try {
					is.close();
				} catch (Exception e1) {
				}
				SM_Utilities.log("!!!! ERROR Retrieving !!!!");
				SM_Utilities.log(urlStr);
				SM_Utilities.log(exp.toString());
				SM_Utilities.log("---- Retrying ----");
				Thread.sleep(1000);
				url = new URL(urlStr);
			}
		}
		if (is != null) {
			return is;
		} else {
			throw new Exception("Input Stream is NULL");
		}
	}

	public static String getURLContentAsString(String urlStr) throws Exception {
		InputStream is = null;
		String html = "";
		URL url = new URL(urlStr);
		boolean dataYetToBeRetrieved = true;
		int i = 0;
		while (dataYetToBeRetrieved) {
			try {
				URLConnection urlConnection = url.openConnection();
				urlConnection.setConnectTimeout(1000 * 60);
				urlConnection.setReadTimeout(2 * 1000 * 60);
				urlConnection.setUseCaches(false);
				is = urlConnection.getInputStream();
				dataYetToBeRetrieved = false;
			} catch (Exception exp) {
				try {
					is.close();
				} catch (Exception e1) {
				}
				i++;
				if (i >= 1000) {
					SM_Utilities.log("!!!! ERROR Retrieving !!!!");
					SM_Utilities.log(urlStr);
					SM_Utilities.log(exp.toString());
					SM_Utilities.log("---- Retrying ----");
					Thread.sleep(1000);
				}
				url = new URL(urlStr);
			}
		}
		if (is != null) {
			Scanner scanner = new Scanner(is);
			html = scanner.useDelimiter("\\A").next();
			scanner.close();
			is.close();
		} else {
			throw new Exception("Input Stream is NULL");
		}

		return html;
	}

	public static String formatDoubleToTwoDecimals(double dblNo) {
		return df2.format(dblNo);
	}

	public static String formatSqlDate(java.sql.Date date, String format) {
		if (date == null) {
			return null;
		} else {
			DateFormat dateFormat = new SimpleDateFormat(format);
			return dateFormat.format(new Date(date.getTime()));
		}
	}

	public static String formatCalDate(Calendar cal, String format) {
		if (cal != null) {
			DateFormat dateFormat = new SimpleDateFormat(format);
			return dateFormat.format(cal.getTime());
		} else {
			return "";
		}
	}

	public static java.sql.Date getSqlDate(String dateStr, String format) throws ParseException {
		DateFormat dateFormat = new SimpleDateFormat(format);
		java.sql.Date sqlDate = new java.sql.Date(dateFormat.parse(dateStr).getTime());
		return sqlDate;
	}

	public static java.sql.Date getSqlDate(Calendar cal) {
		if (cal == null) {
			return null;
		} else {
			java.sql.Date sqlDate = new java.sql.Date(cal.getTimeInMillis());
			return sqlDate;
		}
	}

	public static Calendar getCalFromSqlDate(java.sql.Date date) {
		if (date != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(new Date(date.getTime()));
			return cal;
		}
		return null;
	}

	public static boolean areCalenderObjectsEqualToDates(Calendar cal1, Calendar cal2) {
		if (cal1 != null && cal2 != null && (cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)) && (cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH))
				&& (cal1.get(Calendar.DATE) == cal2.get(Calendar.DATE))) {
			return true;
		}
		return false;
	}

	public static String quoteStringsInListForSql(ArrayList<String> listOfStr) {
		StringBuilder sb = new StringBuilder();
		boolean appendSet = false;
		for (String str : listOfStr) {
			sb.append("'").append(str).append("',");
			appendSet = true;
		}
		String retStr = sb.toString();
		if (appendSet) {
			retStr = retStr.substring(0, retStr.length() - 1);
		}
		return retStr;
	}

	public static String getStringInURLFormat(String str) {
		if (str != null) {
			str = str.replaceAll(" ", "%20");
		}
		return str;
	}

	public static String getURLInStringFormat(String strUrl) {
		if (strUrl != null) {
			strUrl = strUrl.replaceAll("%20", " ");
		}
		return strUrl;
	}

	public static boolean isNullOrZero(Double val) {
		return ((val == null) || val.equals(0.0));
	}

	public static int[] getMaxQtrAndYearToCheck() {
		int[] qtrYear = new int[2];
		Calendar current = Calendar.getInstance();
		int month = current.get(Calendar.MONTH) + 1;
		int year = current.get(Calendar.YEAR);

		if (month >= 1 && month <= 3) {
			qtrYear[0] = 4;
			qtrYear[1] = year - 1;
		} else if (month >= 4 && month <= 6) {
			qtrYear[0] = 1;
			qtrYear[1] = year;
		} else if (month >= 7 && month <= 9) {
			qtrYear[0] = 2;
			qtrYear[1] = year;
		} else if (month >= 10 && month <= 12) {
			qtrYear[0] = 3;
			qtrYear[1] = year;
		}

		return qtrYear;
	}

	public static Calendar getStartOfQtrDate() {
		Calendar currentDate = Calendar.getInstance();
		Calendar startOfQtrDate = Calendar.getInstance();
		int currentMonth = currentDate.get(Calendar.MONTH);

		if (currentMonth >= 0 && currentMonth < 3) {
			startOfQtrDate.set(Calendar.YEAR, currentDate.get(Calendar.YEAR));
			startOfQtrDate.set(Calendar.MONTH, 0);
			startOfQtrDate.set(Calendar.DATE, 1);
		} else if (currentMonth >= 3 && currentMonth < 6) {
			startOfQtrDate.set(Calendar.YEAR, currentDate.get(Calendar.YEAR));
			startOfQtrDate.set(Calendar.MONTH, 3);
			startOfQtrDate.set(Calendar.DATE, 1);
		} else if (currentMonth >= 6 && currentMonth < 9) {
			startOfQtrDate.set(Calendar.YEAR, currentDate.get(Calendar.YEAR));
			startOfQtrDate.set(Calendar.MONTH, 6);
			startOfQtrDate.set(Calendar.DATE, 1);
		} else {
			startOfQtrDate.set(Calendar.YEAR, currentDate.get(Calendar.YEAR));
			startOfQtrDate.set(Calendar.MONTH, 9);
			startOfQtrDate.set(Calendar.DATE, 1);
		}

		return startOfQtrDate;
	}

	public static boolean isDateAfter(Calendar date1, Calendar date2) {
		if (date1 != null && date2 != null) {
			return false;
		}

		if (date1.get(Calendar.YEAR) > date2.get(Calendar.YEAR)) {
			return true;
		} else if (date1.get(Calendar.YEAR) < date2.get(Calendar.YEAR)) {
			return false;
		} else {
			if (date1.get(Calendar.MONTH) > date2.get(Calendar.MONTH)) {
				return true;
			} else if (date1.get(Calendar.MONTH) < date2.get(Calendar.MONTH)) {
				return false;
			} else {
				if (date1.get(Calendar.DATE) > date2.get(Calendar.DATE)) {
					return true;
				} else {
					return false;
				}
			}
		}

	}

	private static class SM_LogFormatter extends Formatter {
		@Override
		public String format(LogRecord record) {
			SimpleDateFormat logTime = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
			Calendar cal = new GregorianCalendar();
			cal.setTimeInMillis(record.getMillis());
			return logTime.format(cal.getTime()) + " || " + record.getMessage() + "\n";
		}

	}

	// Main Method - testing
	public static void main(String[] args) throws IOException {
		System.out.println("Hello");
		System.out.println(properties.get("dbPwd"));
	}

	private SM_Utilities() {
	}

	public static ArrayList<Double> getQtrNosToFetch(Double lastShpFetchQtr, double maxFetchQtrNo, ArrayList<Double> bseQtrNosAscendingList) {
		ArrayList<Double> qtrNosToFetch = new ArrayList<Double>();
		if (bseQtrNosAscendingList != null && bseQtrNosAscendingList.size() > 0) {
			if (lastShpFetchQtr == null) {
				lastShpFetchQtr = 0.0;
			}

			for (double qtrNo : bseQtrNosAscendingList) {
				if (qtrNo >= lastShpFetchQtr && qtrNo <= maxFetchQtrNo) {
					qtrNosToFetch.add(qtrNo);
				}
				if (qtrNo > maxFetchQtrNo) {
					break;
				}
			}
		}
		return qtrNosToFetch;
	}

	public static boolean isTradingHoliday(Calendar cal) throws Exception {
		// TODO - Need to Have Holidays Saved to DB
		// String html = SM_Utilities.getURLContentAsString(SM_Utilities.getSMProperty("URL_TradingHolidays_MoneyControl"));

		if(cal.get(Calendar.DAY_OF_WEEK)== Calendar.SATURDAY || cal.get(Calendar.DAY_OF_WEEK)== Calendar.SUNDAY){
			return true;
		}
		
		
		// September 13, 2016
		DateFormat format = DateFormat.getDateInstance(1);
		String dateStr = format.format(cal.getTime());

		if (tradingHolidaysHtml.indexOf(dateStr) > -1) {
			return true;
		}

		return false;
	}
	
	public static void shutdown() {
		fh.close();
	}

}
