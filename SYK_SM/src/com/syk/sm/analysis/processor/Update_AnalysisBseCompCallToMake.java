package com.syk.sm.analysis.processor;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import com.syk.sm.bean.AnalysisBseCompCallToMake;
import com.syk.sm.broker.SMBroker;
import com.syk.sm.utility.SM_Utilities;
import com.syk.sm.utility.SM_Utilities.CALL_TO_MAKE;

public class Update_AnalysisBseCompCallToMake {

	public static void update(Connection con) throws Exception {
		SM_Utilities.log("Update_AnalysisBseCompCallToMake | update");
		Calendar maxAnalysedDate = SMBroker.getMaxCallToMakeTradeDate(con);

		if (maxAnalysedDate == null) {
			maxAnalysedDate = Calendar.getInstance();
			maxAnalysedDate.set(Calendar.YEAR, 2015);
			maxAnalysedDate.set(Calendar.MONTH, 0);
			maxAnalysedDate.set(Calendar.DATE, 1);
		}

		ArrayList<String> tradingDatesList = SMBroker.getDistinctTradingDatesDesc(con);
		if (tradingDatesList.size() > 3) {
			ArrayList<String> datesToAnalyse = new ArrayList<String>();
			String maxAnalysedDateStr = SM_Utilities.formatCalDate(maxAnalysedDate, "dd-MMM-yyyy").toUpperCase();
			boolean getThreeMoreTradingdates = false;
			int threeMoreDatesCounter = 0;

			for (int i = 0; i < tradingDatesList.size(); i++) {
				datesToAnalyse.add(tradingDatesList.get(i));
				if (!getThreeMoreTradingdates) {
					if (tradingDatesList.get(i).equals(maxAnalysedDateStr)) {
						getThreeMoreTradingdates = true;
					}
				} else {
					threeMoreDatesCounter++;
					if (threeMoreDatesCounter >= 3) {
						break;
					}
				}
			}

			if (datesToAnalyse.size() > 3) {
				HashMap<Integer, HashMap<String, Double>> scripToDateToClosePriceMap = SMBroker.getClosingPricesForGivenDates(datesToAnalyse, con);

				if (scripToDateToClosePriceMap.size() > 0) {
					ArrayList<AnalysisBseCompCallToMake> analysisCTMBeans = new ArrayList<AnalysisBseCompCallToMake>();

					for (int scripCode : scripToDateToClosePriceMap.keySet()) {
						HashMap<String, Double> dateToClosePriceMap = scripToDateToClosePriceMap.get(scripCode);
						if (dateToClosePriceMap != null) {
							for (int i = 0; i < datesToAnalyse.size() - 3; i++) {
								if (dateToClosePriceMap.get(datesToAnalyse.get(i)) != null && dateToClosePriceMap.get(datesToAnalyse.get(i + 1)) != null
										&& dateToClosePriceMap.get(datesToAnalyse.get(i + 2)) != null && dateToClosePriceMap.get(datesToAnalyse.get(i + 3)) != null) {

									AnalysisBseCompCallToMake analysisCallToMake = compute(scripCode, datesToAnalyse.get(i), dateToClosePriceMap.get(datesToAnalyse.get(i)),
											dateToClosePriceMap.get(datesToAnalyse.get(i + 1)), dateToClosePriceMap.get(datesToAnalyse.get(i + 2)), dateToClosePriceMap.get(datesToAnalyse.get(i + 3)));
									analysisCTMBeans.add(analysisCallToMake);
								}
							}

						}
					}
					SMBroker.persistAnalysisBseCompCallToMakeBeans(analysisCTMBeans, con);
				}
			}
		}
	}

	private static AnalysisBseCompCallToMake compute(int scripCode, String tradeDate, Double currentPrice, Double prevDay1Price, Double prevDay2Price, Double prevDay3Price) {
		double d1 = (int)(currentPrice - prevDay1Price);
		double d2 = (int)(prevDay1Price - prevDay2Price);
		double d3 = (int)(prevDay2Price - prevDay3Price);

		double dailyDiff = d1;
		double sumTwoDayDiff = d1 + d2;
		double sumThreeDayDiff = d1 + d2 + d3;
		double allSum = sumThreeDayDiff + sumTwoDayDiff + dailyDiff;

		CALL_TO_MAKE callToMake = CALL_TO_MAKE.NO_CALL;

		if (allSum > -1 && (sumThreeDayDiff > -1 || (dailyDiff + sumTwoDayDiff) > -1)) {
			callToMake = CALL_TO_MAKE.BUY;
		} else {
			callToMake = CALL_TO_MAKE.SELL;
		}

		AnalysisBseCompCallToMake analysisCallToMake = new AnalysisBseCompCallToMake();
		analysisCallToMake.setScripCode(scripCode);
		analysisCallToMake.setTradeDate(tradeDate);
		analysisCallToMake.setComputedCall(null);
		analysisCallToMake.setCallToMake(callToMake);
		analysisCallToMake.setOneDayDiff(Double.valueOf(SM_Utilities.formatDoubleToTwoDecimals(dailyDiff)));
		analysisCallToMake.setTwoDaySumOfDiff(Double.valueOf(SM_Utilities.formatDoubleToTwoDecimals(sumTwoDayDiff)));
		analysisCallToMake.setThreeDaySumOfDiff(Double.valueOf(SM_Utilities.formatDoubleToTwoDecimals(sumThreeDayDiff)));
		analysisCallToMake.setSumOfAllDays(Double.valueOf(SM_Utilities.formatDoubleToTwoDecimals(allSum)));
		analysisCallToMake.setDaysClosePrice(currentPrice);

		return analysisCallToMake;
	}

}
