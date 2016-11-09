package com.syk.sm.analysis.processor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import com.syk.sm.bean.AnalysisCompanyResultsBean;
import com.syk.sm.bean.DataBseCompanyResultsBean;
import com.syk.sm.broker.SMBroker;
import com.syk.sm.utility.SM_Utilities;

public class AnalyseCompanyResults {

	private static final int VALUE_NOT_SET = -123;

	public static void main(String[] args) throws ClassNotFoundException, Exception {
		// DATA_BSE_COMPANY_FOR_ANALYSIS
		System.out.println("Start");

		Class.forName("oracle.jdbc.driver.OracleDriver");
		Connection con = DriverManager.getConnection(SM_Utilities.getSMProperty("dbUrl"), SM_Utilities.getSMProperty("dbUser"), SM_Utilities.getSMProperty("dbPwd"));

		// analysisResults(con);
		computeAndSave(88, con);
		System.out.println("Done");
	}

	public static void computeAndSave(Connection con) throws Exception {
		int maxQtrYearToFetch[] = SM_Utilities.getMaxQtrAndYearToCheck();
		double latestQtr = SMBroker.getBseQtrNo(maxQtrYearToFetch, con);

		computeAndSave(latestQtr, con);
	}

	public static void computeAndSave(double latestQtr, Connection con) throws SQLException {
		SM_Utilities.log("AnalyseCompanyResults | computeAndSave | " + latestQtr);
		HashMap<Integer, ArrayList<DataBseCompanyResultsBean>> companyResultsMap = SMBroker.getCompanyResultsToAnalyseForTheQtr(latestQtr, con);
		ArrayList<AnalysisCompanyResultsBean> resultAnalysisBeanList = new ArrayList<AnalysisCompanyResultsBean>();

		for (int scripCode : companyResultsMap.keySet()) {
			SM_Utilities.log("AnalyseCompanyResults | computeAndSave | " + scripCode + " | " + latestQtr);
			try {
				ArrayList<DataBseCompanyResultsBean> resultsList = companyResultsMap.get(scripCode);
				if (resultsList != null && resultsList.size() > 0 && resultsList.size() != 1) {
					HashMap<Double, DataBseCompanyResultsBean> qtrResultMap = generateQtrResultMapFromResultList(resultsList);
					AnalysisCompanyResultsBean resultAnalysisBean = new AnalysisCompanyResultsBean();

					calculateReservesPercentageChange(resultAnalysisBean, resultsList);
					calculateOtherImpParamChanges(latestQtr, resultAnalysisBean, qtrResultMap);

					// resultAnalysisBean.setNetPercentageChange(resultAnalysisBean.getNetSalesQToQPctChange() + resultAnalysisBean.getNetSalesQOnQPctChange()
					// + resultAnalysisBean.getNetProfitQToQPctChange() + resultAnalysisBean.getNetProfitQOnQPctChange());

					resultAnalysisBean.setNetPercentageChange(resultAnalysisBean.getNetProfitQToQPctChange());

					resultAnalysisBean.setScripCode(scripCode);
					resultAnalysisBeanList.add(resultAnalysisBean);
				}
			} catch (Exception exp) {
				SM_Utilities.logConsole("AnalyseCompanyResults | computeAndSave | ERROR | " + scripCode + " | " + latestQtr);
				continue;
			}
		}
		SMBroker.saveCompanyResultAnalysis(resultAnalysisBeanList, latestQtr, con);
	}

	private static void calculateOtherImpParamChanges(double latestQtr, AnalysisCompanyResultsBean resultAnalysisBean, HashMap<Double, DataBseCompanyResultsBean> qtrResultMap) {

		if (isReqDataAvailable(qtrResultMap.get(latestQtr)) && isReqDataAvailable(qtrResultMap.get(latestQtr - 1)) && isReqDataAvailable(qtrResultMap.get(latestQtr - 2))
				&& isReqDataAvailable(qtrResultMap.get(latestQtr - 3)) && isReqDataAvailable(qtrResultMap.get(latestQtr - 4))) {
			DataBseCompanyResultsBean compResLatestQtr = qtrResultMap.get(latestQtr);
			DataBseCompanyResultsBean compResPrev1Qtr = qtrResultMap.get(latestQtr - 1);
			DataBseCompanyResultsBean compResPrev2Qtr = qtrResultMap.get(latestQtr - 2);
			DataBseCompanyResultsBean compResPrev3Qtr = qtrResultMap.get(latestQtr - 3);
			DataBseCompanyResultsBean compResPrev4Qtr = qtrResultMap.get(latestQtr - 4);

			double totalNetSalesCurrentQtr = compResLatestQtr.getTotalIncome() + compResPrev1Qtr.getTotalIncome() + compResPrev2Qtr.getTotalIncome() + compResPrev3Qtr.getTotalIncome()
					- (compResLatestQtr.getOtherIncome() + compResPrev1Qtr.getOtherIncome() + compResPrev2Qtr.getOtherIncome() + compResPrev3Qtr.getOtherIncome());
			double totalNetSalesPrevQtr = compResPrev4Qtr.getTotalIncome() + compResPrev1Qtr.getTotalIncome() + compResPrev2Qtr.getTotalIncome() + compResPrev3Qtr.getTotalIncome()
					- (compResPrev4Qtr.getOtherIncome() + compResPrev1Qtr.getOtherIncome() + compResPrev2Qtr.getOtherIncome() + compResPrev3Qtr.getOtherIncome());

			double totalNetProfitCurrentQtr = ((compResLatestQtr.getNetProfit() + compResPrev1Qtr.getNetProfit() + compResPrev2Qtr.getNetProfit() + compResPrev3Qtr.getNetProfit())
					/ (compResLatestQtr.getTotalIncome() + compResPrev1Qtr.getTotalIncome() + compResPrev2Qtr.getTotalIncome() + compResPrev3Qtr.getTotalIncome())) * totalNetSalesCurrentQtr;
			double totalNetProfitPrev = ((compResPrev4Qtr.getNetProfit() + compResPrev1Qtr.getNetProfit() + compResPrev2Qtr.getNetProfit() + compResPrev3Qtr.getNetProfit())
					/ (compResPrev4Qtr.getTotalIncome() + compResPrev1Qtr.getTotalIncome() + compResPrev2Qtr.getTotalIncome() + compResPrev3Qtr.getTotalIncome())) * totalNetSalesPrevQtr;

			
			double netProfitPctChange = ((totalNetProfitCurrentQtr-totalNetProfitPrev) / Math.abs(totalNetProfitPrev)) * 100;
			resultAnalysisBean.setNetProfitQToQPctChange(netProfitPctChange);


		}

	}

	private static void calculateReservesPercentageChange(AnalysisCompanyResultsBean resultAnalysisBean, ArrayList<DataBseCompanyResultsBean> resultsList) {
		double currentReserves = VALUE_NOT_SET;
		double prevReserves = VALUE_NOT_SET;
		for (DataBseCompanyResultsBean resultBean : resultsList) {
			if ((int) resultBean.getQtrNo() == resultBean.getQtrNo()) {
				if (resultBean.getReserves() != 0.0) {
					if (currentReserves == VALUE_NOT_SET) {
						currentReserves = resultBean.getReserves();
					} else {
						prevReserves = resultBean.getReserves();
						break;
					}
				}
			} else {
				if (currentReserves == VALUE_NOT_SET) {
					currentReserves = resultBean.getReserves();
				} else {
					prevReserves = resultBean.getReserves();
					break;
				}
			}
		}

		if (currentReserves != VALUE_NOT_SET && prevReserves != VALUE_NOT_SET && prevReserves != 0) {
			resultAnalysisBean.setReservesPctChange(((currentReserves - prevReserves) / prevReserves) * 100);
		}

	}

	private static HashMap<Double, DataBseCompanyResultsBean> generateQtrResultMapFromResultList(ArrayList<DataBseCompanyResultsBean> resultsList) {
		HashMap<Double, DataBseCompanyResultsBean> qtrResultMap = new HashMap<Double, DataBseCompanyResultsBean>();
		if (resultsList != null && resultsList.size() > 0) {
			for (DataBseCompanyResultsBean resBean : resultsList) {
				qtrResultMap.put(resBean.getQtrNo(), resBean);
			}
		}
		return qtrResultMap;
	}

	private static boolean isReqDataAvailable(DataBseCompanyResultsBean dataBseCompanyResultsBean) {
		boolean dataAvailable = false;
		if (dataBseCompanyResultsBean != null && dataBseCompanyResultsBean.getTotalIncome() != 0 && (dataBseCompanyResultsBean.getTotalIncome() - dataBseCompanyResultsBean.getOtherIncome()) > 0) {
			dataAvailable = true;
		}
		return dataAvailable;
	}
}
