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
import com.syk.sm.utility.SM_Utilities;
import com.syk.sm.utility.SM_Utilities.CALL_TO_MAKE;

public class RT_Prices_From_MoneyControl {

	private static HashMap<Integer, String> scripCodeCompanyNameMap = null;
	private static DateTime dateTime = DateTime.now(DateTimeZone.forID("Asia/Kolkata"));
	private static DateTimeFormatter formatter = DateTimeFormat.forPattern("YYYYMMdd");
	private static String dateTimeStr = formatter.print(dateTime.toLocalDateTime().toDateTime().getMillis());
	// private static String dateTimeStr = "20160523";
	private static HashMap<Integer, String> scMCMap = null;
	private static ConcurrentHashMap<Integer, String> scMCMapConc = new ConcurrentHashMap<Integer, String>();

	public static void updateScripCodeCompanyNameMap(Connection con) throws SQLException {
		scripCodeCompanyNameMap = SMBroker.getScripCodeCompanyNameMap(con);
	}

	public static void getRealTimePrices(ArrayList<AnalysisBseCompCallToMake> analysisBseCompCallToMakeBeans, Connection con) throws Exception {
		if (scripCodeCompanyNameMap == null) {
			scripCodeCompanyNameMap = SMBroker.getScripCodeCompanyNameMap(con);
		}
		ExecutorService executor = Executors.newFixedThreadPool(5000);
		ArrayList<FutureTask<AnalysisBseCompCallToMake>> futureTasks = new ArrayList<FutureTask<AnalysisBseCompCallToMake>>();

		for (AnalysisBseCompCallToMake analysisBseCompCallBean : analysisBseCompCallToMakeBeans) {
			SM_Utilities.log("RT_Prices_From_MoneyControl | Scrip Code:" + analysisBseCompCallBean.getScripCode() + " | MultiThread");

			RT_CP_Callable callable = new RT_CP_Callable(analysisBseCompCallBean);
			FutureTask<AnalysisBseCompCallToMake> futureTask = new FutureTask<AnalysisBseCompCallToMake>(callable);
			futureTasks.add(futureTask);
			executor.execute(futureTask);
		}

		for (FutureTask<AnalysisBseCompCallToMake> futureTask : futureTasks) {
			futureTask.get();
		}
		SM_Utilities.log("RT_Prices_From_MoneyControl | MultiThread | Complete");

		executor.shutdown();

		if (scMCMapConc != null && scMCMapConc.size() > 0) {
			scMCMap = new HashMap<Integer, String>();
			for (Integer key : scMCMapConc.keySet()) {
				scMCMap.put(key, scMCMapConc.get(key));
			}
			scMCMapConc = new ConcurrentHashMap<Integer, String>();
		}
	}

	public static void getRealTimePrice(AnalysisBseCompCallToMake analysisBseCompCallToMake) throws Exception {
		try {
			String html = "";
			if (scMCMap == null) {
				String mcId = "";
				html = SM_Utilities
						.getURLContentAsString(SM_Utilities.getSMProperty("URL_RealTimeCircuitPrices_FromMoneyControl_1").replaceAll("@SCRIP_CODE@", "" + analysisBseCompCallToMake.getScripCode()));
				String url;
				String alternativeId = "";

				if (html != null) {
					url = html.substring(html.indexOf(":\"") + 2);
					url = url.substring(0, url.indexOf("\""));

					alternativeId = html.substring(html.indexOf("profilingSuggestions.php?id=") + 28);
					if (!alternativeId.equals("")) {
						alternativeId = alternativeId.substring(0, alternativeId.indexOf("&"));
					}
					html = "";

					if (url != null && url.indexOf("javascript:") > -1) {
						String compName = scripCodeCompanyNameMap.get(analysisBseCompCallToMake.getScripCode());
						if (compName != null) {
							String splitCompName[] = compName.split(" ");
							if (splitCompName.length > 1) {
								compName = splitCompName[0] + "%20" + splitCompName[1];
							}

							html = SM_Utilities.getURLContentAsString(SM_Utilities.getSMProperty("URL_RealTimeCircuitPrices_FromMoneyControl_1").replaceAll("@SCRIP_CODE@", compName));
							url = html.substring(html.indexOf(":\"") + 2);
							url = url.substring(0, url.indexOf("\""));
							alternativeId = html.substring(html.indexOf("profilingSuggestions.php?id=") + 28);
							if (!alternativeId.equals("")) {
								alternativeId = alternativeId.substring(0, alternativeId.indexOf("&"));
							}
							html = "";

						}
					}

					// oldApproach(url, analysisBseCompCallToMake);
					// Get Prices & Current Stock Price
					url = url.substring(url.lastIndexOf("/") + 1);

					html = SM_Utilities.getURLContentAsString(SM_Utilities.getSMProperty("URL_RealTimeCircuitPrices_FromMoneyControl_2").replaceAll("@MC_SC_ID@", alternativeId));
					mcId = alternativeId;
					if (html != null && html.equals("jsonCallback([null])")) {
						html = SM_Utilities.getURLContentAsString(SM_Utilities.getSMProperty("URL_RealTimeCircuitPrices_FromMoneyControl_2").replaceAll("@MC_SC_ID@", url));
						mcId = url;
					}
					newApproach(html, analysisBseCompCallToMake);
					scMCMapConc.putIfAbsent(analysisBseCompCallToMake.getScripCode(), mcId);
				}
			} else {
				if (scMCMap.get(analysisBseCompCallToMake.getScripCode()) != null) {
					html = SM_Utilities.getURLContentAsString(
							SM_Utilities.getSMProperty("URL_RealTimeCircuitPrices_FromMoneyControl_2").replaceAll("@MC_SC_ID@", scMCMap.get(analysisBseCompCallToMake.getScripCode())));
					newApproach(html, analysisBseCompCallToMake);
				}
			}
		} catch (Exception exp) {
			SM_Utilities.log("Scrip_Code:" + analysisBseCompCallToMake.getScripCode() + " || " + exp.toString());
		}
	}

	private static void newApproach(String html, AnalysisBseCompCallToMake analysisBseCompCallToMake) throws Exception {
		if (html != null && html.indexOf(dateTimeStr) > -1) {
			html = html.substring(html.indexOf("CP\\\":\\\"") + 7);
			String currentPrice = html.substring(0, html.indexOf("\\"));

			html = html.substring(html.indexOf("VOL\\\":\\\"") + 8);
			String volume = html.substring(0, html.indexOf("\\"));

			html = html.substring(html.indexOf("OPN\\\":\\\"") + 8);
			String daysOpenStr = html.substring(0, html.indexOf("\\"));

			html = html.substring(html.indexOf("lower_circuit_limit\\\":\\\"") + 24);
			String lowerCktStr = html.substring(0, html.indexOf("\\"));

			html = html.substring(html.indexOf("upper_circuit_limit\\\":\\\"") + 24);
			String upperCktStr = html.substring(0, html.indexOf("\\"));

			html = html.substring(html.indexOf("tot_buy_qty\\\":\\\"") + 16);
			String totBuyQty = html.substring(0, html.indexOf("\\"));

			html = html.substring(html.indexOf("tot_sell_qty\\\":\\\"") + 17);
			String totSellQty = html.substring(0, html.indexOf("\\"));

			String leftOverHtml = html;

			html = html.substring(html.indexOf("1\\\":{\\\"buyrate\\\":\\\"") + 19);
			String buyRateStr = html.substring(0, html.indexOf("\\"));

			html = html.substring(html.indexOf("buyqty\\\":\\\"") + 11);
			String buyQtyStr = html.substring(0, html.indexOf("\\"));

			html = html.substring(html.indexOf("sellrate\\\":\\\"") + 13);
			String sellRateStr = html.substring(0, html.indexOf("\\"));

			html = html.substring(html.indexOf("sellqty\\\":\\\"") + 12);
			String sellQtyStr = html.substring(0, html.indexOf("\\"));

			if (volume != null) {
				if (currentPrice != null && volume != null) {

					currentPrice = currentPrice.replaceAll(",", "");
					volume = volume.replaceAll(",", "");
					lowerCktStr = lowerCktStr.replaceAll(",", "");
					upperCktStr = upperCktStr.replaceAll(",", "");
					totBuyQty = totBuyQty.replaceAll(",", "");
					totSellQty = totSellQty.replaceAll(",", "");

					double currentPriceDbl = Double.valueOf(currentPrice);
					long volumeLong = Double.valueOf(volume).longValue();
					double daysOpen = Double.valueOf(daysOpenStr);
					double lowerCktDbl = Double.valueOf(lowerCktStr);
					double upperCktDbl = Double.valueOf(upperCktStr);
					long totBuyQtyLong = Double.valueOf(totBuyQty).longValue();
					long totSellQtyLong = Double.valueOf(totSellQty).longValue();
					long topBuyQty = Double.valueOf(buyQtyStr).longValue();
					double topBuyRate = Double.valueOf(buyRateStr);
					long topSellQty = Double.valueOf(sellQtyStr).longValue();
					double topSellRate = Double.valueOf(sellRateStr);

					analysisBseCompCallToMake.setCurrentPrice(currentPriceDbl);
					analysisBseCompCallToMake.setCurrentVolume(volumeLong);
					analysisBseCompCallToMake.setDaysOpen(daysOpen);
					analysisBseCompCallToMake.setLowerCkt(lowerCktDbl);
					analysisBseCompCallToMake.setUpperCkt(upperCktDbl);
					analysisBseCompCallToMake.setTotBuyQty(totBuyQtyLong);
					analysisBseCompCallToMake.setTotSellQty(totSellQtyLong);
					analysisBseCompCallToMake.setTopBuyQty(topBuyQty);
					analysisBseCompCallToMake.setTopBuyRate(topBuyRate);
					analysisBseCompCallToMake.setTopSellQty(topSellQty);
					analysisBseCompCallToMake.setTopSellRate(topSellRate);

					int currentPriceInt = (int) currentPriceDbl;
					int lastClosingPrice = (int) analysisBseCompCallToMake.getDaysClosePrice();
					int diff = currentPriceInt - lastClosingPrice;

					if (diff >= 0) {
						analysisBseCompCallToMake.setComputedCall(CALL_TO_MAKE.BUY);
					} else {
						analysisBseCompCallToMake.setComputedCall(CALL_TO_MAKE.SELL);
					}

					getTop5Qtys(leftOverHtml, analysisBseCompCallToMake);
				}
			}
		}
	}

	private static void getTop5Qtys(String leftOverHtml, AnalysisBseCompCallToMake analysisBseCompCallToMake) {
		ArrayList<Integer> buyQtys = new ArrayList<Integer>();
		ArrayList<Integer> sellQtys = new ArrayList<Integer>();
		try {
			while (leftOverHtml.indexOf("buyqty") > -1) {
				try {
					leftOverHtml = leftOverHtml.substring(leftOverHtml.indexOf("buyqty\\\":\\\"") + 11);
					buyQtys.add(Integer.valueOf(leftOverHtml.substring(0, leftOverHtml.indexOf("\\"))));
					leftOverHtml = leftOverHtml.substring(leftOverHtml.indexOf("sellqty\\\":\\\"") + 12);
					sellQtys.add(Integer.valueOf(leftOverHtml.substring(0, leftOverHtml.indexOf("\\"))));
				} catch (Exception exp) {
					SM_Utilities.log("Top 5 Qtys | EXP1 |" + exp + " | " + analysisBseCompCallToMake.getScripCode());
				}
			}
		} catch (Exception exp) {
			SM_Utilities.log("Top 5 Qtys | EXP2 |" + exp + " | " + analysisBseCompCallToMake.getScripCode());
		}

		try {
			int totalTop5BuyQty = 0;
			int totalTop5SellQty = 0;

			for (int qty : buyQtys) {
				totalTop5BuyQty = totalTop5BuyQty + qty;
			}

			for (int qty : sellQtys) {
				totalTop5SellQty = totalTop5SellQty + qty;
			}

			analysisBseCompCallToMake.setTop5BuyQty(totalTop5BuyQty);
			analysisBseCompCallToMake.setTop5SellQty(totalTop5SellQty);

		} catch (Exception exp) {
			SM_Utilities.log("Top 5 Qtys | EXP3 |" + exp + " | " + analysisBseCompCallToMake.getScripCode());
		}
	}

	@SuppressWarnings("unused")
	private static void oldApproach(String url, AnalysisBseCompCallToMake analysisBseCompCallToMake) throws Exception {
		String html = SM_Utilities.getURLContentAsString(url);
		if (html != null) {
			html = html.substring(html.indexOf("b_low_price_limit\">") + 19);

			String lowerCktStr = html.substring(0, html.indexOf("<"));

			if (lowerCktStr != null && lowerCktStr.equals("-")) {
				url = url.substring(url.lastIndexOf("/") + 1);

				html = SM_Utilities.getURLContentAsString(SM_Utilities.getSMProperty("URL_RealTimeCircuitPrices_FromMoneyControl_2").replaceAll("@MC_SC_ID@", url));
				html = html.substring(html.indexOf("lower_circuit_limit\\\":\\\"") + 24);
				lowerCktStr = html.substring(0, html.indexOf("\\"));

				html = html.substring(html.indexOf("upper_circuit_limit\\\":\\\"") + 24);
				html = html.substring(0, html.indexOf("\\"));

			} else {
				html = html.substring(html.indexOf("b_high_price_limit\">") + 20);
				html = html.substring(0, html.indexOf("<"));
			}

			if (lowerCktStr != null && html != null) {
				lowerCktStr = lowerCktStr.replaceAll(",", "");
				html = html.replaceAll(",", "");

				double lowerCkt = Double.valueOf(lowerCktStr);
				double upperCkt = Double.valueOf(html);

				analysisBseCompCallToMake.setLowerCkt(lowerCkt);
				analysisBseCompCallToMake.setUpperCkt(upperCkt);
			}
		}

	}

	public static class RT_CP_Callable implements Callable<AnalysisBseCompCallToMake> {
		private AnalysisBseCompCallToMake analysisBseCompCallToMake;

		public RT_CP_Callable(AnalysisBseCompCallToMake analysisBseCompCallToMake) {
			this.analysisBseCompCallToMake = analysisBseCompCallToMake;
		}

		@Override
		public AnalysisBseCompCallToMake call() throws Exception {
			getRealTimePrice(analysisBseCompCallToMake);
			return analysisBseCompCallToMake;
		}

	}

}
