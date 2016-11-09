import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.ConcurrentHashMap;

import com.syk.sm.analysis.processor.AnalyseCompanyResults;
import com.syk.sm.analysis.processor.Update_AnalysisBseCompCallToMake;
import com.syk.sm.analysis.processor.Update_GroupIndustryMidPE;
import com.syk.sm.bean.AnalysisBseCompCallToMake;
import com.syk.sm.bean.BseBhavCopyBean;
import com.syk.sm.bean.DataBseCompanyBean;
import com.syk.sm.bean.DataBseCompanyResultsBean;
import com.syk.sm.bean.DataBseQtrYearMarkerBean;
import com.syk.sm.bean.ShareHoldingPatternBean;
import com.syk.sm.broker.SMBroker;
import com.syk.sm.datafetch.processor.GenerateBSEQuarterAndYearMarkers;
import com.syk.sm.datafetch.processor.Get_IntraEligibleStocks_FromZerodha;
import com.syk.sm.datafetch.processor.RT_CurrentPrice_ComputedCall;
import com.syk.sm.datafetch.processor.RT_DaysOpen;
import com.syk.sm.datafetch.processor.RT_Prices_From_MoneyControl;
import com.syk.sm.datafetch.processor.RT_Prices_From_NSE;
import com.syk.sm.datafetch.processor.ReadCompanyNames_DeleteNonExistentCompanies;
import com.syk.sm.datafetch.processor.Read_52WeekHighLow_FromBSE;
import com.syk.sm.datafetch.processor.Read_CompanyResult_FromBSE;
import com.syk.sm.datafetch.processor.Read_DailyBhavCopy_FromBSE;
import com.syk.sm.datafetch.processor.Read_ScripIDIndexFaceValueIndustry_FromBSE;
import com.syk.sm.datafetch.processor.Read_ShareHoldingPattern_FromBSE;
import com.syk.sm.utility.SM_CACHE;
import com.syk.sm.utility.SM_Utilities;

/**
 * 
 */

/**
 * @author skuppuraju
 *
 */
@SuppressWarnings("unused")
public class Main {

	private static Connection con;

	static {
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	// {
	// mainOneTimeTasks();
	// mainRealTime(false);
	// }
	public static void main(String args[]) throws SQLException, Exception {
		ConcurrentHashMap<String, Object> cacheMap = SM_CACHE.getCacheMap();
		mainEODTask(false);// boolean --> goMinimal
		// mainRealTimeRecurring(true);// boolean --> doOneTimeTasks
		SM_Utilities.shutdown();
	}

	public static void mainEODTask(boolean goMinimal) throws SQLException, Exception {
		long t1 = System.currentTimeMillis();
		/**
		 * Create a new Oracle connection
		 */
		con = DriverManager.getConnection(SM_Utilities.getSMProperty("dbUrl"), SM_Utilities.getSMProperty("dbUser"), SM_Utilities.getSMProperty("dbPwd"));

		// ************************************* TASKS - START *******************************************

		/**
		 * Persist MetaData (Quarter & Year Markers)
		 * One time activity
		 */
		// generateAndPersistQYMetaMarkers();

		/**
		 * Execute this function everyday.
		 * Daily Task.
		 */
		dailyTask(goMinimal);

		// ************************************* TASKS - END *******************************************

		/**
		 * Close Oracle connection.
		 */
		con.close();
		double totalTimeTaken = ((double) (System.currentTimeMillis() - t1)) / (60 * 1000);
		if ((int) totalTimeTaken != totalTimeTaken) {
			totalTimeTaken = ((int) totalTimeTaken + 1);
		}
		System.out.println("Total Time Taken: " + totalTimeTaken + " minutes");
	}

	public static void mainRealTime(boolean doOneTimeRTCalls) throws SQLException, Exception {
		long t1 = System.currentTimeMillis();
		/**
		 * Create a new Oracle connection
		 */
		con = DriverManager.getConnection(SM_Utilities.getSMProperty("dbUrl"), SM_Utilities.getSMProperty("dbUser"), SM_Utilities.getSMProperty("dbPwd"));

		// ************************************* TASKS - START *******************************************

		if (doOneTimeRTCalls) {
			justBeforeMarketOpens();
		}

		marketRealTimeFromMoneyControl();
		marketRealTimeFromNSE();

		// ************************************* TASKS - END *******************************************

		/**
		 * Close Oracle connection.
		 */
		con.close();
		double totalTimeTaken = ((double) (System.currentTimeMillis() - t1)) / (60 * 1000);
		if ((int) totalTimeTaken != totalTimeTaken) {
			totalTimeTaken = ((int) totalTimeTaken + 1);
		}
		System.out.println("Total Time Taken: " + totalTimeTaken + " minutes");
	}

	public static void mainRealTimeRecurring(boolean doOneTimeRTCalls) throws SQLException, Exception {

		while (true) {
			mainRealTime(doOneTimeRTCalls);
			if (doOneTimeRTCalls) {
				doOneTimeRTCalls = false;
			}
			Thread.sleep(1000 * 5);
		}
	}

	public static void mainOneTimeTasks() throws Exception {
		long t1 = System.currentTimeMillis();
		/**
		 * Create a new Oracle connection
		 */
		con = DriverManager.getConnection(SM_Utilities.getSMProperty("dbUrl"), SM_Utilities.getSMProperty("dbUser"), SM_Utilities.getSMProperty("dbPwd"));

		// marketRealTimeFromMoneyControl();
		update52WeekHighLowOfCompanies();

		/**
		 * Close Oracle connection.
		 */
		con.close();
		double totalTimeTaken = ((double) (System.currentTimeMillis() - t1)) / (60 * 1000);
		if ((int) totalTimeTaken != totalTimeTaken) {
			totalTimeTaken = ((int) totalTimeTaken + 1);
		}
		System.out.println("Total Time Taken: " + totalTimeTaken + " minutes");
	}

	private static void justBeforeMarketOpens() throws Exception {
		SM_Utilities.logConsole("******************   JustBeforeMarketOpens | START   ******************");
		long t1 = System.currentTimeMillis();
		SM_Utilities.logConsole("---------------------------------------------------------------------------------------------");
		SM_Utilities.logConsole("INTRA_FLAGS | Fetching Intra Eligible Stocks from Zerodha ....");
		ArrayList<String> intraEligibleScripIds = Get_IntraEligibleStocks_FromZerodha.getScripIds();
		SMBroker.updateIntraEligibleFlags(intraEligibleScripIds, con);
		SM_Utilities.logConsole("Done fething & updating INTRA_ELIGIBLE Flags |  " + (((System.currentTimeMillis() - t1) / 1000)) + " secs");
		SM_Utilities.logConsole("---------------------------------------------------------------------------------------------");
		SM_Utilities.logConsole("******************   JustBeforeMarketOpens | DONE   *******************");

	}

	private static void marketRT_ComputedCall_CurrentPrice_NotUSED() throws Exception {
		SM_Utilities.logConsole("******************   MarketRealTime_UpdateComputedCall_CurrentPrice | START   ******************");
		long t1 = System.currentTimeMillis();
		SM_Utilities.logConsole("---------------------------------------------------------------------------------------------");
		SM_Utilities.logConsole("Call_TO_MAKE | Fetching Latest Prices of Stocks from BSE ....");
		ArrayList<AnalysisBseCompCallToMake> latestAnalysisBseCompCallToMakeBeans = SMBroker.getLatestAnalysisBseCompCallToMakeBeans(con);
		RT_CurrentPrice_ComputedCall.updateAnalysis(latestAnalysisBseCompCallToMakeBeans);
		SMBroker.updateCompCallToMakeWithCurrentPrice(latestAnalysisBseCompCallToMakeBeans, con);
		SM_Utilities.logConsole("Done updating Call_TO_MAKE |  " + (((System.currentTimeMillis() - t1) / 1000)) + " secs");
		SM_Utilities.logConsole("---------------------------------------------------------------------------------------------");
		SM_Utilities.logConsole("******************   MarketRealTime_UpdateComputedCall_CurrentPrice | DONE   *******************");

	}

	private static void marketRT_DaysOpenPrice_NOTUSED() throws Exception {
		SM_Utilities.logConsole("******************   MarketRealTime_DaysOpenPrice | START   ******************");
		long t1 = System.currentTimeMillis();
		SM_Utilities.logConsole("---------------------------------------------------------------------------------------------");
		SM_Utilities.logConsole("Call_TO_MAKE | Fetching Days Open Prices of Stocks from BSE ....");
		ArrayList<AnalysisBseCompCallToMake> latestAnalysisBseCompCallToMakeBeans = SMBroker.getLatestAnalysisBseCompCallToMakeBeans(con);
		RT_DaysOpen.getDaysOpenPrice(latestAnalysisBseCompCallToMakeBeans);
		SMBroker.updateDaysOpenPrice(latestAnalysisBseCompCallToMakeBeans, con);
		SM_Utilities.logConsole("Done updating Call_TO_MAKE |  " + (((System.currentTimeMillis() - t1) / 1000)) + " secs");
		SM_Utilities.logConsole("---------------------------------------------------------------------------------------------");
		SM_Utilities.logConsole("******************   MarketRealTime_DaysOpenPrice | DONE   *******************");

	}

	private static void marketRealTimeFromMoneyControl() throws Exception {
		SM_Utilities.logConsole("******************   MarketRealTimeFromMoneyControl | START   ******************");
		long t1 = System.currentTimeMillis();
		SM_Utilities.logConsole("---------------------------------------------------------------------------------------------");
		SM_Utilities.logConsole("Call_TO_MAKE | Fetching RealTime Prices of Stocks from MoneyControl ....");
		ArrayList<AnalysisBseCompCallToMake> latestAnalysisBseCompCallToMakeBeans = SMBroker.getLatestAnalysisBseCompCallToMakeBeans(con);
		RT_Prices_From_MoneyControl.getRealTimePrices(latestAnalysisBseCompCallToMakeBeans, con);
		SMBroker.updateRealTimePricesFromMoneyControl(latestAnalysisBseCompCallToMakeBeans, con);
		SM_Utilities.logConsole("Done updating Call_TO_MAKE |  " + (((System.currentTimeMillis() - t1) / 1000)) + " secs");
		SM_Utilities.logConsole("---------------------------------------------------------------------------------------------");
		SM_Utilities.logConsole("******************   MarketRealTimeFromMoneyControl | DONE   *******************");
	}

	private static void marketRealTimeFromNSE() throws Exception {
		SM_Utilities.logConsole("******************   MarketRealTimeFromNSE | START   ******************");
		long t1 = System.currentTimeMillis();
		SM_Utilities.logConsole("---------------------------------------------------------------------------------------------");
		SM_Utilities.logConsole("Call_TO_MAKE | Fetching RealTime Prices of Stocks from NSE ....");
		ArrayList<AnalysisBseCompCallToMake> latestAnalysisBseCompCallToMakeBeans = SMBroker.getLatestAnalysisBseCompCallToMakeBeans(con);
		RT_Prices_From_NSE.getRealTimeData(latestAnalysisBseCompCallToMakeBeans, con);
		SMBroker.updateRealTimeDataFromNSE(latestAnalysisBseCompCallToMakeBeans, con);
		SM_Utilities.logConsole("Done updating Call_TO_MAKE |  " + (((System.currentTimeMillis() - t1) / 1000)) + " secs");
		SM_Utilities.logConsole("---------------------------------------------------------------------------------------------");
		SM_Utilities.logConsole("******************   MarketRealTimeFromNSE | DONE   *******************");
	}

	public static void dailyTask(boolean goMinimal) throws Exception {
		SM_Utilities.logConsole("******************   Daily Task | START   ******************");
		long t1 = System.currentTimeMillis();

		/**
		 * Daily Bhav Copy
		 * Date param - ddMMyy | Ex: 280116
		 * Null - Will get Last retrieved date from DATA_BSE_DAILY_TRADE and retrieve data from that date till today
		 */
		SM_Utilities.logConsole("---------------------------------------------------------------------------------------------");
		t1 = System.currentTimeMillis();
		SM_Utilities.logConsole("Fetching Bhav Copy from BSE ....");
		dailyBhavCopy(null);
		SM_Utilities.logConsole("Done Fetching Bhav Copy from BSE |  " + (((System.currentTimeMillis() - t1) / 1000)) + " secs");
		SM_Utilities.logConsole("---------------------------------------------------------------------------------------------");

		/**
		 * Check if Company is Valid company, else mark it deleted in DATA_BSE_COMPANY
		 */
		SM_Utilities.logConsole("Updating Company Names and Cleaning up Non-Existent Companies ....");
		readCompanyNamesAndDeleteNonExistentCompanies();
		SM_Utilities.logConsole("Done Updating Company Names and Cleaning up Non-Existent Companies | " + (((System.currentTimeMillis() - t1) / 1000)) + " secs");
		SM_Utilities.logConsole("---------------------------------------------------------------------------------------------");

		if (goMinimal) {
			SM_Utilities.logConsole("Going Minimal |  NOT updating Company Results & SHP");
			SM_Utilities.logConsole("---------------------------------------------------------------------------------------------");
		} else {
			/**
			 * Fetch & Update Company Results
			 * fetchCompanyResults(goMultiThreaded, fetchAlways);
			 * Daily - fetchCompanyResults(true, false);
			 * Weekend - fetchCompanySHP(true, true);
			 */
			t1 = System.currentTimeMillis();
			SM_Utilities.logConsole("Fetching & Updating Company Results ....");
			fetchCompanyResults(true, true);
			SM_Utilities.logConsole("Done Fetching & Updating Company Results |  " + (((System.currentTimeMillis() - t1) / 1000)) + " secs");
			SM_Utilities.logConsole("---------------------------------------------------------------------------------------------");

			/**
			 * Fetch & Update Company Share Holding Pattern
			 * fetchCompanySHP(goMultiThreaded, fetchAlways);
			 * Daily - fetchCompanySHP(true, false);
			 * Weekend - fetchCompanySHP(true, true);
			 */
			t1 = System.currentTimeMillis();
			SM_Utilities.logConsole("Fetching & Updating Company Share Holding Pattern ....");
			fetchCompanySHP(true, false);
			SM_Utilities.logConsole("Done Fetching & Updating Company Share Holding Pattern |  " + (((System.currentTimeMillis() - t1) / 1000)) + " secs");
			SM_Utilities.logConsole("---------------------------------------------------------------------------------------------");
		}

		/**
		 * Update ScripIDIndexFaceValueIndustry FromBSE
		 */
		t1 = System.currentTimeMillis();
		SM_Utilities.logConsole("Updating ScripIDIndexFaceValueIndustry Of companies ....");
		updateScripIDIndexFaceValueIndustryOfCompanies();
		SM_Utilities.logConsole("Done Updating ScripIDIndexFaceValueIndustry Of companies |  " + (((System.currentTimeMillis() - t1) / 1000)) + " secs");
		SM_Utilities.logConsole("---------------------------------------------------------------------------------------------");

		/**
		 * Update 52Week High Low FromBSE
		 */
		t1 = System.currentTimeMillis();
		SM_Utilities.logConsole("Updating 52 Week High Low Of companies ....");
		update52WeekHighLowOfCompanies();
		SM_Utilities.logConsole("Done Updating 52 Week High Low Of companies |  " + (((System.currentTimeMillis() - t1) / 1000)) + " secs");
		SM_Utilities.logConsole("---------------------------------------------------------------------------------------------");

		/**
		 * Update ANALYSIS_BSE_COMP_CALL_TO_MAKE
		 */
		t1 = System.currentTimeMillis();
		SM_Utilities.logConsole("Updating ANALYSIS_BSE_COMP_CALL_TO_MAKE ....");
		Update_AnalysisBseCompCallToMake.update(con);
		SM_Utilities.logConsole("Done Updating ANALYSIS_BSE_COMP_CALL_TO_MAKE |  " + (((System.currentTimeMillis() - t1) / 1000)) + " secs");
		SM_Utilities.logConsole("---------------------------------------------------------------------------------------------");

		/**
		 * Update AnalyseCompanyResults
		 */
		t1 = System.currentTimeMillis();
		SM_Utilities.logConsole("Analysing Company Results ....");
		AnalyseCompanyResults.computeAndSave(con);
		SM_Utilities.logConsole("Done Analysing Company Results |  " + (((System.currentTimeMillis() - t1) / 1000)) + " secs");
		SM_Utilities.logConsole("---------------------------------------------------------------------------------------------");

		/**
		 * Update GroupIndustryMidPE
		 */
		t1 = System.currentTimeMillis();
		SM_Utilities.logConsole("Updating GroupIndustryMidPE's ....");
		Update_GroupIndustryMidPE.update(con);
		SM_Utilities.logConsole("Done GroupIndustryMidPE's |  " + (((System.currentTimeMillis() - t1) / 1000)) + " secs");
		SM_Utilities.logConsole("---------------------------------------------------------------------------------------------");

		/**
		 * Delete Ignored_UC_Companies
		 */
		t1 = System.currentTimeMillis();
		SM_Utilities.logConsole("Deleting Ignored UC companies ....");
		SMBroker.deleteIgnored_UC_Companies(con);
		SM_Utilities.logConsole("Done Deleting Ignored UC companies |  " + (((System.currentTimeMillis() - t1) / 1000)) + " secs");
		SM_Utilities.logConsole("---------------------------------------------------------------------------------------------");

		SM_Utilities.logConsole("******************   Daily Task | DONE   *******************");
	}

	private static void fetchCompanySHP(boolean goMultiThreaded, boolean fetchAlways) throws Exception {
		int maxQtrYearToFetch[] = SM_Utilities.getMaxQtrAndYearToCheck();
		double maxFetchQtrNo = SMBroker.getBseQtrNo(maxQtrYearToFetch, con);
		ArrayList<DataBseCompanyBean> companyBeans = SMBroker.getDataBseCompanyBeans(con, false);
		ArrayList<Double> bseQtrNosList = SMBroker.getBseQtrNosList(con);

		ArrayList<ShareHoldingPatternBean> shpBeans = Read_ShareHoldingPattern_FromBSE.getSHP(companyBeans, maxFetchQtrNo, bseQtrNosList, goMultiThreaded, fetchAlways);

		if (shpBeans.size() > 0) {
			SMBroker.updateDataBseCompanyShp(shpBeans, con);
		}
		SMBroker.updateDataBseCompanyWithSHP_Info(companyBeans, con);
	}

	private static void fetchCompanyResults(boolean goMultiThreaded, boolean fetchAlways) throws Exception {
		int maxQtrYearToFetch[] = SM_Utilities.getMaxQtrAndYearToCheck();
		double maxFetchQtrNo = SMBroker.getBseQtrNo(maxQtrYearToFetch, con);
		double maxFetchQtrYear = maxFetchQtrNo + 0.5;
		ArrayList<DataBseCompanyBean> companyBeans = SMBroker.getDataBseCompanyBeans(con, false);
		ArrayList<Double> bseQtrNosList = SMBroker.getBseQtrNosList(con);

		ArrayList<DataBseCompanyResultsBean> resultsBean = Read_CompanyResult_FromBSE.getResult(companyBeans, maxFetchQtrYear, bseQtrNosList, goMultiThreaded, fetchAlways);

		if (resultsBean.size() > 0) {
			SMBroker.updateDataBseCompanyResults(resultsBean, con);
		}
		SMBroker.updateDataBseCompanyWithResults_Info(companyBeans, con);
	}

	private static void update52WeekHighLowOfCompanies() throws Exception {
		ArrayList<DataBseCompanyBean> companyBeans = SMBroker.getDataBseCompanyBeans(con, false);
		Read_52WeekHighLow_FromBSE.read(companyBeans, true);
		SMBroker.update52WeekHighLows(companyBeans, con);
	}

	private static void updateScripIDIndexFaceValueIndustryOfCompanies() throws Exception {
		ArrayList<DataBseCompanyBean> companyBeans = SMBroker.getDataBseCompanyBeans(con, false);
		Read_ScripIDIndexFaceValueIndustry_FromBSE.read(companyBeans, true);
		SMBroker.updateScripIDIndexFaceValueIndustry(companyBeans, con);
	}

	private static void readCompanyNamesAndDeleteNonExistentCompanies() throws Exception {
		ArrayList<DataBseCompanyBean> companyBeans = SMBroker.getDataBseCompanyBeans(con, true);
		ReadCompanyNames_DeleteNonExistentCompanies.doReadAndUpdate(companyBeans, true);
		SMBroker.updateCompanyNames_DeleteNonExistentCompanies(companyBeans, con);
	}

	// Daily Bhav Copy
	private static void dailyBhavCopy(String dateStr) throws Exception {
		Calendar maxBhavDate = Calendar.getInstance();
		Calendar currentDate = Calendar.getInstance();
		DateFormat dateFormat = new SimpleDateFormat("ddMMyy");

		if (dateStr == null) {
			maxBhavDate = SMBroker.getMaxBhavDateFromDB(con);
			while (maxBhavDate.before(currentDate)) {
				if ((dateFormat.format(maxBhavDate.getTime())).equals(dateFormat.format(currentDate.getTime()))) {
					break;
				}
				if (!SM_Utilities.isTradingHoliday(maxBhavDate)) {
					dateStr = dateFormat.format(maxBhavDate.getTime());
					SM_Utilities.logConsole("Fetching:" + dateStr);
					ArrayList<BseBhavCopyBean> bhavCopyBeansList = Read_DailyBhavCopy_FromBSE.getBhavCopyForDate(dateStr);

					if (bhavCopyBeansList != null && bhavCopyBeansList.size() > 0) {
						SMBroker.saveMetaBseCompanyInfo_FromBhavCopy(bhavCopyBeansList, con);
						SMBroker.saveBseDailyEquityTradeInfo_FromBhavCopy(bhavCopyBeansList, dateStr, con);
						SM_Utilities.logConsole("Saved | Daily Bhav Copy for date:" + dateStr);
					}
				} else {
					dateStr = dateFormat.format(maxBhavDate.getTime());
					SM_Utilities.logConsole("Skipping:" + dateStr);
				}
				maxBhavDate.add(Calendar.DATE, 1);
			}

			dateStr = dateFormat.format(currentDate.getTime());
		}

		if (!SM_Utilities.isTradingHoliday(currentDate)) {
			SM_Utilities.logConsole("Fetching:" + dateStr);
			ArrayList<BseBhavCopyBean> bhavCopyBeansList = Read_DailyBhavCopy_FromBSE.getBhavCopyForDate(dateStr);

			if (bhavCopyBeansList != null && bhavCopyBeansList.size() > 0) {
				SMBroker.saveMetaBseCompanyInfo_FromBhavCopy(bhavCopyBeansList, con);
				SMBroker.saveBseDailyEquityTradeInfo_FromBhavCopy(bhavCopyBeansList, dateStr, con);
				SM_Utilities.logConsole("Saved | Daily Bhav Copy for date:" + dateStr);
			}
		}
	}

	// Persist MetaData (Quarter & Year Markers)
	private static void generateAndPersistQYMetaMarkers() throws Exception {
		SM_Utilities.logConsole("******************   Generate Qtr & YEar Markers | START   ******************");
		/**
		 * generate(BSE_START_QTR_NO, RESPECTIVE_QTR_IN_YEAR, RESPECTIVE_YEAR, NO_OF_QTRS_TO_GENERATE);
		 * Table : DATA_BSE_QTR_YEAR_MARKER
		 * One Year -- 4 Qtrs | 81 ==> Qtr 1 , 2014
		 * Args (81, 1, 2014, 28) --> Generate from Qtr 1 | From: 81 | Year 2014 | 28 qtrs (4*7) ==> 7 years from 2014 Inclusive ==> Till 2020
		 */
		ArrayList<DataBseQtrYearMarkerBean> quarterYearMarkerList = GenerateBSEQuarterAndYearMarkers.generate(80, 4, 2013, 40);
		for (DataBseQtrYearMarkerBean qyMarker : quarterYearMarkerList) {
			SM_Utilities.log(qyMarker.toString());
		}

		SMBroker.saveMetaQYMarkers(quarterYearMarkerList, con);
		SM_Utilities.logConsole("******************   Generate Qtr & Year Markers | END   ******************");
	}

}
