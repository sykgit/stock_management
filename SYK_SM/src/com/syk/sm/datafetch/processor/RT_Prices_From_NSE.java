package com.syk.sm.datafetch.processor;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.syk.sm.bean.AnalysisBseCompCallToMake;
import com.syk.sm.broker.SMBroker;
import com.syk.sm.utility.SM_CACHE;
import com.syk.sm.utility.SM_Utilities;

public class RT_Prices_From_NSE {

	private static HashMap<Integer, String> scripCodeScripIDMap = null;
	private static DateTime dateTime = DateTime.now(DateTimeZone.forID("Asia/Kolkata"));
	private static DateTimeFormatter formatter = DateTimeFormat.forPattern("ddMMMYYYY");
	private static String dateTimeStr = (formatter.print(dateTime.toLocalDateTime().toDateTime().getMillis())).toUpperCase();
	// private static String dateTimeStr = "24MAY2016";
	private static ConcurrentHashMap<Integer, String> errorScrips = new ConcurrentHashMap<Integer, String>();

	public static void updateScripCodeCompanyNameMap(Connection con) throws SQLException {
		scripCodeScripIDMap = SMBroker.getScripCodeScripIDMapFor_AB_Group(con);
	}

	@SuppressWarnings("unchecked")
	public static void getRealTimeData(ArrayList<AnalysisBseCompCallToMake> analysisBseCompCallToMakeBeans, Connection con) throws Exception {
		scripCodeScripIDMap = (HashMap<Integer, String>) SM_CACHE.getCacheMap().get("RT_Prices_From_NSE-scripCodeScripIDMap");
		if (scripCodeScripIDMap == null) {
			scripCodeScripIDMap = SMBroker.getScripCodeScripIDMapFor_AB_Group(con);
		}
		ExecutorService executor = Executors.newFixedThreadPool(5000);
		ArrayList<FutureTask<AnalysisBseCompCallToMake>> futureTasks = new ArrayList<FutureTask<AnalysisBseCompCallToMake>>();

		for (AnalysisBseCompCallToMake analysisBseCompCallBean : analysisBseCompCallToMakeBeans) {
			SM_Utilities.log("RT_Prices_From_NSE | Scrip Code:" + analysisBseCompCallBean.getScripCode() + " | MultiThread");

			RT_NSE_Callable callable = new RT_NSE_Callable(analysisBseCompCallBean);
			FutureTask<AnalysisBseCompCallToMake> futureTask = new FutureTask<AnalysisBseCompCallToMake>(callable);
			futureTasks.add(futureTask);
			executor.execute(futureTask);
		}

		for (FutureTask<AnalysisBseCompCallToMake> futureTask : futureTasks) {
			futureTask.get();
		}
		SM_Utilities.log("RT_Prices_From_NSE | MultiThread | Complete");

		executor.shutdown();

		for (int scripCode : errorScrips.keySet()) {
			scripCodeScripIDMap.remove(scripCode);
		}
		errorScrips = new ConcurrentHashMap<Integer, String>();

		SM_CACHE.getCacheMap().put("RT_Prices_From_NSE-scripCodeScripIDMap", scripCodeScripIDMap);
	}

	public static void getRealTimeData(AnalysisBseCompCallToMake analysisBseCompCallToMake) throws Exception {
		if (scripCodeScripIDMap.get(analysisBseCompCallToMake.getScripCode()) != null) {
			try {
				String scripId = scripCodeScripIDMap.get(analysisBseCompCallToMake.getScripCode());
				scripId = scripId.replaceAll("&", "%26");
				scripId = scripId.replaceAll("[*]", "");

				String html = SM_Utilities.getURLContentAsString(SM_Utilities.getSMProperty("URL_RealTime_NSE").replaceAll("@NSE_ID@", scripId));
				html = html.substring(html.indexOf("{\"futLink\":"), html.indexOf("optLink\":"));
				html = html.replaceAll(" ", "");

				if (html != null && html.indexOf(dateTimeStr) > -1 && html.indexOf("buyQuantity1") > -1) {
					double currentPrice = getDoubleValueFromNSEStr(html.substring(html.indexOf("\"lastPrice\":\"") + 13), analysisBseCompCallToMake);
					long currentVolume = getDoubleValueFromNSEStr(html.substring(html.indexOf("\"quantityTraded\":\"") + 18), analysisBseCompCallToMake).longValue();
					long totalBuyQty = getDoubleValueFromNSEStr(html.substring(html.indexOf("\"totalBuyQuantity\":\"") + 20), analysisBseCompCallToMake).longValue();
					long totalSellQty = getDoubleValueFromNSEStr(html.substring(html.indexOf("\"totalSellQuantity\":\"") + 21), analysisBseCompCallToMake).longValue();

					int buyQty1 = getDoubleValueFromNSEStr(html.substring(html.indexOf("\"buyQuantity1\":\"") + 16), analysisBseCompCallToMake).intValue();
					int buyQty2 = getDoubleValueFromNSEStr(html.substring(html.indexOf("\"buyQuantity2\":\"") + 16), analysisBseCompCallToMake).intValue();
					int buyQty3 = getDoubleValueFromNSEStr(html.substring(html.indexOf("\"buyQuantity3\":\"") + 16), analysisBseCompCallToMake).intValue();
					int buyQty4 = getDoubleValueFromNSEStr(html.substring(html.indexOf("\"buyQuantity4\":\"") + 16), analysisBseCompCallToMake).intValue();
					int buyQty5 = getDoubleValueFromNSEStr(html.substring(html.indexOf("\"buyQuantity5\":\"") + 16), analysisBseCompCallToMake).intValue();
					long top5BuyQty = buyQty1 + buyQty2 + buyQty3 + buyQty4 + buyQty5;

					int sellQty1 = getDoubleValueFromNSEStr(html.substring(html.indexOf("\"sellQuantity1\":\"") + 17), analysisBseCompCallToMake).intValue();
					int sellQty2 = getDoubleValueFromNSEStr(html.substring(html.indexOf("\"sellQuantity2\":\"") + 17), analysisBseCompCallToMake).intValue();
					int sellQty3 = getDoubleValueFromNSEStr(html.substring(html.indexOf("\"sellQuantity3\":\"") + 17), analysisBseCompCallToMake).intValue();
					int sellQty4 = getDoubleValueFromNSEStr(html.substring(html.indexOf("\"sellQuantity4\":\"") + 17), analysisBseCompCallToMake).intValue();
					int sellQty5 = getDoubleValueFromNSEStr(html.substring(html.indexOf("\"sellQuantity5\":\"") + 17), analysisBseCompCallToMake).intValue();
					long top5SellQty = sellQty1 + sellQty2 + sellQty3 + sellQty4 + sellQty5;

					analysisBseCompCallToMake.setnCurrPrice(currentPrice);
					analysisBseCompCallToMake.setnCurrVolume(currentVolume);
					analysisBseCompCallToMake.setnTotalBuyQty(totalBuyQty);
					analysisBseCompCallToMake.setnTotalSellQty(totalSellQty);
					analysisBseCompCallToMake.setnTop5BuyQty(top5BuyQty);
					analysisBseCompCallToMake.setnTop5SellQty(top5SellQty);
				}
			} catch (Exception exp) {
				SM_Utilities.log("RT_Prices_From_NSE | getRealTimeData | Scrip_Code:" + analysisBseCompCallToMake.getScripCode() + " | "
						+ scripCodeScripIDMap.get(analysisBseCompCallToMake.getScripCode()) + " | " + exp.toString());
				errorScrips.put(analysisBseCompCallToMake.getScripCode(), scripCodeScripIDMap.get(analysisBseCompCallToMake.getScripCode()));
			}
		}
	}

	private static Double getDoubleValueFromNSEStr(String nseStr, AnalysisBseCompCallToMake analysisBseCompCallToMake) {
		double val = 0.0;
		try {
			if (nseStr != null) {
				nseStr = nseStr.substring(0, nseStr.indexOf("\""));
				nseStr = nseStr.replaceAll(",", "");
				val = Double.valueOf(nseStr);
			}

		} catch (Exception exp) {
			SM_Utilities.log("RT_Prices_From_NSE | getDoubleValueFromNSEStr | " + analysisBseCompCallToMake.getScripCode() + " | " + scripCodeScripIDMap.get(analysisBseCompCallToMake.getScripCode())
					+ " | Eating Away Exception:" + exp);
		}
		return val;
	}

	public static class RT_NSE_Callable implements Callable<AnalysisBseCompCallToMake> {
		private AnalysisBseCompCallToMake analysisBseCompCallToMake;

		public RT_NSE_Callable(AnalysisBseCompCallToMake analysisBseCompCallToMake) {
			this.analysisBseCompCallToMake = analysisBseCompCallToMake;
		}

		@Override
		public AnalysisBseCompCallToMake call() throws Exception {
			getRealTimeData(analysisBseCompCallToMake);
			return analysisBseCompCallToMake;
		}

	}

}
