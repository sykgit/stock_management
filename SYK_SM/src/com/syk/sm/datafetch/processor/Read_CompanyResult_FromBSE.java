package com.syk.sm.datafetch.processor;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import com.syk.sm.bean.DataBseCompanyBean;
import com.syk.sm.bean.DataBseCompanyResultsBean;
import com.syk.sm.utility.SM_Utilities;
import com.syk.sm.utility.SM_Utilities.COMPANY_NEWS_TYPE;

public class Read_CompanyResult_FromBSE {

	public static void main(String args[]) throws Exception {
		System.out.println(getResult(530369, 88));
	}

	public static ArrayList<DataBseCompanyResultsBean> getResult(DataBseCompanyBean compBean, ArrayList<Double> qtrNosToFetch) throws Exception {
		ArrayList<DataBseCompanyResultsBean> resultsList = new ArrayList<DataBseCompanyResultsBean>();
		if (qtrNosToFetch != null && qtrNosToFetch.size() > 0) {
			for (double qtrNo : qtrNosToFetch) {
				DataBseCompanyResultsBean resultBean = getResult(compBean.getScripCode(), qtrNo);
				if (resultBean != null) {
					resultsList.add(resultBean);
				}
			}
		}
		return resultsList;
	}

	public static ArrayList<DataBseCompanyResultsBean> getResult(ArrayList<DataBseCompanyBean> companyBeans, double maxFetchQtrNo, ArrayList<Double> bseQtrNosList, boolean goMultiThreaded,
			boolean fetchAlways) throws Exception {
		ArrayList<DataBseCompanyResultsBean> resultsBeanList = new ArrayList<DataBseCompanyResultsBean>();
		if (goMultiThreaded) {
			ExecutorService executor = Executors.newFixedThreadPool(2000);

			ArrayList<FutureTask<DataBseCompanyBean>> futureTasksResultNews = new ArrayList<FutureTask<DataBseCompanyBean>>();
			ArrayList<FutureTask<DataBseCompanyResultsBean>> futureTasksResults = new ArrayList<FutureTask<DataBseCompanyResultsBean>>();

			// Fetch News
			for (DataBseCompanyBean dataBseCompanyBean : companyBeans) {
				SM_Utilities.log("Read_CompanyResult_FromBSE | getResult  | " + dataBseCompanyBean.getScripCode() + " | RESULT_NEWS | MultiThreaded");

				CompanyResultsNewsCallable callable = new CompanyResultsNewsCallable(dataBseCompanyBean);
				FutureTask<DataBseCompanyBean> futureTask = new FutureTask<DataBseCompanyBean>(callable);
				futureTasksResultNews.add(futureTask);
				executor.execute(futureTask);
			}

			for (FutureTask<DataBseCompanyBean> futureTask : futureTasksResultNews) {
				futureTask.get();
			}

			// Fetch Results
			for (DataBseCompanyBean dataBseCompanyBean : companyBeans) {
				if (dataBseCompanyBean.getLastResultsFetchQtr() == null) {
					dataBseCompanyBean.setLastResultsFetchQtr(0.0);
				}

				if (fetchAlways || dataBseCompanyBean.getLatestResultsNewsDate() == null
						|| SM_Utilities.areCalenderObjectsEqualToDates(dataBseCompanyBean.getLatestResultsNewsDate(), Calendar.getInstance())
						|| (SM_Utilities.isDateAfter(dataBseCompanyBean.getLatestResultsNewsDate(), SM_Utilities.getStartOfQtrDate())
								&& dataBseCompanyBean.getLastResultsFetchQtr() < ((int) maxFetchQtrNo))) {

					ArrayList<Double> qtrNosToFetch = SM_Utilities.getQtrNosToFetch(dataBseCompanyBean.getLastResultsFetchQtr(), maxFetchQtrNo, bseQtrNosList);
					for (double qtrNo : qtrNosToFetch) {
						SM_Utilities.log("Read_CompanyResult_FromBSE | getResult | " + dataBseCompanyBean.getScripCode() + " | " + qtrNo + " | RESULTS_RETRIEVE | MultiThreaded");
						CompanyResultsCallable callable = new CompanyResultsCallable(dataBseCompanyBean, qtrNo);
						FutureTask<DataBseCompanyResultsBean> futureTask = new FutureTask<DataBseCompanyResultsBean>(callable);
						futureTasksResults.add(futureTask);
						executor.execute(futureTask);
					}
				}
			}

			for (FutureTask<DataBseCompanyResultsBean> futureTask : futureTasksResults) {
				DataBseCompanyResultsBean resultBean = futureTask.get();
				if (resultBean != null) {
					resultsBeanList.add(resultBean);
				}
			}

			SM_Utilities.log("Read_CompanyResult_FromBSE | MultiThread | Complete");

			executor.shutdown();

		} else {
			for (DataBseCompanyBean dataBseCompanyBean : companyBeans) {
				HashMap<COMPANY_NEWS_TYPE, ArrayList<String>> companyResultNews = Read_CompanyNews_FromBSE.read(dataBseCompanyBean.getScripCode(), dataBseCompanyBean.getLastResultsFetchDate(),
						Calendar.getInstance(), COMPANY_NEWS_TYPE.RESULT, null);

				ArrayList<Double> qtrNosToFetch = SM_Utilities.getQtrNosToFetch(dataBseCompanyBean.getLastResultsFetchQtr(), maxFetchQtrNo, bseQtrNosList);
				for (double qtrNo : qtrNosToFetch) {
					SM_Utilities.log("Read_CompanyResult_FromBSE | getResult | " + dataBseCompanyBean.getScripCode() + " | " + qtrNo);
					DataBseCompanyResultsBean resultBean = getResult(dataBseCompanyBean.getScripCode(), qtrNo);
					if (resultBean != null) {
						resultsBeanList.add(resultBean);
					}
				}

				if (companyResultNews != null && companyResultNews.size() > 0) {
					dataBseCompanyBean.setLatestResultsNewsDate(Calendar.getInstance());
				}
			}
		}
		return resultsBeanList;

	}

	public static DataBseCompanyResultsBean getResult(int scripCode, double qtrNo) throws Exception {
		SM_Utilities.log("Read_CompanyResult_FromBSE | getResult | " + scripCode + " | " + qtrNo);
		try {
			String html = SM_Utilities.getURLContentAsString(
					(SM_Utilities.getSMProperty("URL_CompanyResult_FromBSE").replaceAll("@SCRIP_CODE@", "" + scripCode)).replaceAll("@QTR_NO@", SM_Utilities.formatDoubleToTwoDecimals(qtrNo)));

			String incomeTextUsage = "Net Sales / Income";
			if (html.indexOf(incomeTextUsage) <= 0) {
				incomeTextUsage = "Net Sales";
				if (html.indexOf(incomeTextUsage) <= 0) {
					incomeTextUsage = "Net Income from";
				}
			}

			if (html.indexOf(incomeTextUsage) > -1) {
				SM_Utilities.log("Result FOUND for:" + scripCode + " | " + qtrNo);
				DataBseCompanyResultsBean companyResultsBean = new DataBseCompanyResultsBean();
				companyResultsBean.setScripCode(scripCode);
				companyResultsBean.setQtrNo(qtrNo);
				companyResultsBean.setYearResult(((int) qtrNo) != qtrNo);

				// Finding Out NetSales Income - Total Income
				html = html.substring(html.indexOf(incomeTextUsage));
				html = html.substring(html.indexOf("<td") + 3);
				double totalIncome = Double.valueOf(((html.substring(html.indexOf(">") + 1, html.indexOf("<"))).replaceAll(",", "")).trim());
				companyResultsBean.setTotalIncome(totalIncome);

				// Find Out Expenditure
				if(html.indexOf("Expenditure") > -1){
					html = html.substring(html.indexOf("Expenditure"));
					html = html.substring(html.indexOf("<td") + 3);
					double expenditure = Double.valueOf(((html.substring(html.indexOf(">") + 1, html.indexOf("<"))).replaceAll(",", "")).trim());
					companyResultsBean.setExpenditure(expenditure);
				}

				String tempHtml = "";

				// Find Out Other Income
				double otherIncome = 0.0;
				if (html.indexOf("Other Income<") > -1) {
					tempHtml = html.substring(html.indexOf("Other Income<"));
					tempHtml = tempHtml.substring(tempHtml.indexOf("<td") + 3);
					otherIncome = Double.valueOf(((tempHtml.substring(tempHtml.indexOf(">") + 1, tempHtml.indexOf("<"))).replaceAll(",", "")).trim());
					companyResultsBean.setOtherIncome(otherIncome);
				}

				// Find Out Net Profit
				double netProfit = 0.0;
				if (html.indexOf("Net Profit<") > -1) {
					tempHtml = html.substring(html.indexOf("Net Profit<"));
					tempHtml = tempHtml.substring(tempHtml.indexOf("<td") + 3);
					netProfit = Double.valueOf(((tempHtml.substring(tempHtml.indexOf(">") + 1, tempHtml.indexOf("<"))).replaceAll(",", "")).trim());
					companyResultsBean.setNetProfit(netProfit);
				}

				// Find Out Equity Capital
				double equityCapital = 0.0;
				if (html.indexOf("Equity Capital<") > -1) {
					tempHtml = html.substring(html.indexOf("Equity Capital<"));
					tempHtml = tempHtml.substring(tempHtml.indexOf("<td") + 3);
					equityCapital = Double.valueOf(((tempHtml.substring(tempHtml.indexOf(">") + 1, tempHtml.indexOf("<"))).replaceAll(",", "")).trim());
					companyResultsBean.setEquityCapital(equityCapital);
				}

				// Find Out Equity Capital
				double faceVal = 0.0;
				if (html.indexOf("Face Value") > -1) {
					tempHtml = html.substring(html.indexOf("Face Value"));
					tempHtml = tempHtml.substring(tempHtml.indexOf("<td") + 3);
					faceVal = Double.valueOf(((tempHtml.substring(tempHtml.indexOf(">") + 1, tempHtml.indexOf("<"))).replaceAll(",", "")).trim());
					companyResultsBean.setFaceValue(faceVal);
				}

				// Find Out Reserves
				double reserves = 0.0;
				if (html.indexOf("Reserves<") > -1) {
					tempHtml = html.substring(html.indexOf("Reserves<"));
					tempHtml = tempHtml.substring(tempHtml.indexOf("<td") + 3);
					reserves = Double.valueOf(((tempHtml.substring(tempHtml.indexOf(">") + 1, tempHtml.indexOf("<"))).replaceAll(",", "")).trim());
					companyResultsBean.setReserves(reserves);
				}

				// Find out EPS
				double basicEPS = 0.0;
				String epsStr = "Basic EPS after Extraordinary items<";
				if (html.indexOf(epsStr) < 0) {
					epsStr = "Basic & Diluted EPS after Extraordinary items<";
					if (html.indexOf(epsStr) < 0) {
						epsStr = "Basic EPS before Extraordinary items<";
						if (html.indexOf(epsStr) < 0) {
							epsStr = "Basic & Diluted EPS before Extraordinary items<";
						}
					}
				}

				if (html.indexOf(epsStr) > -1) {
					tempHtml = html.substring(html.indexOf(epsStr));
					tempHtml = tempHtml.substring(tempHtml.indexOf("<td") + 3);
					basicEPS = Double.valueOf(((tempHtml.substring(tempHtml.indexOf(">") + 1, tempHtml.indexOf("<"))).replaceAll(",", "")).trim());
					companyResultsBean.setBseEps(basicEPS);
				}
				SM_Utilities.log(companyResultsBean.toString());

				return companyResultsBean;

			} else if ((html.indexOf("Net Profit") > -1) && (html.indexOf("Total Income<") > -1) && (html.indexOf("Expenditure") > -1)) {

				SM_Utilities.log("Result FOUND for:" + scripCode + " | " + qtrNo);
				DataBseCompanyResultsBean companyResultsBean = new DataBseCompanyResultsBean();
				companyResultsBean.setScripCode(scripCode);
				companyResultsBean.setQtrNo(qtrNo);
				companyResultsBean.setYearResult(((int) qtrNo) != qtrNo);

				String tempHtmlHolder;
				// Finding Out Total Income
				tempHtmlHolder = html.substring(html.indexOf("Total Income<"));
				tempHtmlHolder = tempHtmlHolder.substring(tempHtmlHolder.indexOf("<td") + 3);
				double totalIncome = Double.valueOf(((tempHtmlHolder.substring(tempHtmlHolder.indexOf(">") + 1, tempHtmlHolder.indexOf("<"))).replaceAll(",", "")).trim());
				companyResultsBean.setTotalIncome(totalIncome);

				// Find Out Expenditure
				tempHtmlHolder = html.substring(html.indexOf("Expenditure"));
				tempHtmlHolder = tempHtmlHolder.substring(tempHtmlHolder.indexOf("<td") + 3);
				double expenditure = Double.valueOf(((tempHtmlHolder.substring(tempHtmlHolder.indexOf(">") + 1, tempHtmlHolder.indexOf("<"))).replaceAll(",", "")).trim());
				companyResultsBean.setExpenditure(expenditure);

				// Find Out Other Income
				double otherIncome = 0.0;
				if (html.indexOf("Other Income<") > -1) {
					tempHtmlHolder = html.substring(html.indexOf("Other Income<"));
					tempHtmlHolder = tempHtmlHolder.substring(tempHtmlHolder.indexOf("<td") + 3);
					otherIncome = Double.valueOf(((tempHtmlHolder.substring(tempHtmlHolder.indexOf(">") + 1, tempHtmlHolder.indexOf("<"))).replaceAll(",", "")).trim());
					companyResultsBean.setOtherIncome(otherIncome);
				}

				// Find Out Net Profit
				double netProfit = 0.0;
				if (html.indexOf("Net Profit<") > -1) {
					tempHtmlHolder = html.substring(html.indexOf("Net Profit<"));
					tempHtmlHolder = tempHtmlHolder.substring(tempHtmlHolder.indexOf("<td") + 3);
					netProfit = Double.valueOf(((tempHtmlHolder.substring(tempHtmlHolder.indexOf(">") + 1, tempHtmlHolder.indexOf("<"))).replaceAll(",", "")).trim());
					companyResultsBean.setNetProfit(netProfit);
				}

				// Find Out Equity Capital
				double equityCapital = 0.0;
				if (html.indexOf("Equity Capital<") > -1) {
					tempHtmlHolder = html.substring(html.indexOf("Equity Capital<"));
					tempHtmlHolder = tempHtmlHolder.substring(tempHtmlHolder.indexOf("<td") + 3);
					equityCapital = Double.valueOf(((tempHtmlHolder.substring(tempHtmlHolder.indexOf(">") + 1, tempHtmlHolder.indexOf("<"))).replaceAll(",", "")).trim());
					companyResultsBean.setEquityCapital(equityCapital);
				}

				// Find Out Equity Capital
				double faceVal = 0.0;
				if (html.indexOf("Face Value") > -1) {
					tempHtmlHolder = html.substring(html.indexOf("Face Value"));
					tempHtmlHolder = tempHtmlHolder.substring(tempHtmlHolder.indexOf("<td") + 3);
					faceVal = Double.valueOf(((tempHtmlHolder.substring(tempHtmlHolder.indexOf(">") + 1, tempHtmlHolder.indexOf("<"))).replaceAll(",", "")).trim());
					companyResultsBean.setFaceValue(faceVal);
				}

				// Find Out Reserves
				double reserves = 0.0;
				if (html.indexOf("Reserves<") > -1) {
					tempHtmlHolder = html.substring(html.indexOf("Reserves<"));
					tempHtmlHolder = tempHtmlHolder.substring(tempHtmlHolder.indexOf("<td") + 3);
					reserves = Double.valueOf(((tempHtmlHolder.substring(tempHtmlHolder.indexOf(">") + 1, tempHtmlHolder.indexOf("<"))).replaceAll(",", "")).trim());
					companyResultsBean.setReserves(reserves);
				}

				// Find out EPS
				double basicEPS = 0.0;
				String epsStr = "Basic EPS after Extraordinary items<";
				if (html.indexOf(epsStr) < 0) {
					epsStr = "Basic & Diluted EPS after Extraordinary items<";
					if (html.indexOf(epsStr) < 0) {
						epsStr = "Basic EPS before Extraordinary items<";
						if (html.indexOf(epsStr) < 0) {
							epsStr = "Basic & Diluted EPS before Extraordinary items<";
						}
					}
				}

				if (html.indexOf(epsStr) > -1) {
					tempHtmlHolder = html.substring(html.indexOf(epsStr));
					tempHtmlHolder = tempHtmlHolder.substring(tempHtmlHolder.indexOf("<td") + 3);
					basicEPS = Double.valueOf(((tempHtmlHolder.substring(tempHtmlHolder.indexOf(">") + 1, tempHtmlHolder.indexOf("<"))).replaceAll(",", "")).trim());
					companyResultsBean.setBseEps(basicEPS);
				}
				SM_Utilities.log(companyResultsBean.toString());

				return companyResultsBean;
			} else {
				SM_Utilities.log("-----Result NOT_FOUND for:" + scripCode + " | " + qtrNo);
				return null;
			}
		} catch (Exception exp) {
			SM_Utilities.logConsole("Read_CompanyResult_FromBSE | getResult | ERROR | " + scripCode + " | " + qtrNo);
			exp.printStackTrace();
			throw exp;
		}

	}

	public static class CompanyResultsCallable implements Callable<DataBseCompanyResultsBean> {
		private DataBseCompanyBean dataBseCompanyBean;
		private double qtrNo;

		public CompanyResultsCallable(DataBseCompanyBean dataBseCompanyBean, double qtrNo) {
			this.dataBseCompanyBean = dataBseCompanyBean;
			this.qtrNo = qtrNo;
		}

		@Override
		public DataBseCompanyResultsBean call() throws Exception {
			DataBseCompanyResultsBean resultBean = getResult(dataBseCompanyBean.getScripCode(), qtrNo);
			return resultBean;
		}

	}

	public static class CompanyResultsNewsCallable implements Callable<DataBseCompanyBean> {
		private DataBseCompanyBean dataBseCompanyBean;

		public CompanyResultsNewsCallable(DataBseCompanyBean dataBseCompanyBean) {
			this.dataBseCompanyBean = dataBseCompanyBean;
		}

		@Override
		public DataBseCompanyBean call() throws Exception {
			boolean newsExists = Read_CompanyNews_FromBSE.newsExist(dataBseCompanyBean.getScripCode(), dataBseCompanyBean.getLatestResultsNewsDate(), Calendar.getInstance(), COMPANY_NEWS_TYPE.RESULT,
					null);

			if (newsExists) {
				dataBseCompanyBean.setLatestResultsNewsDate(Calendar.getInstance());
			}
			return dataBseCompanyBean;
		}

	}
}
