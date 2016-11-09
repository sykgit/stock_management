package com.syk.sm.datafetch.processor;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import com.syk.sm.bean.DataBseCompanyBean;
import com.syk.sm.utility.SM_Utilities;

public class Read_52WeekHighLow_FromBSE {

	public static void read(ArrayList<DataBseCompanyBean> dataBseCompanyBeans, boolean goMultiThreaded) throws Exception {
		if (goMultiThreaded) {
			ExecutorService executor = Executors.newFixedThreadPool(5000);
			ArrayList<FutureTask<DataBseCompanyBean>> futureTasks = new ArrayList<FutureTask<DataBseCompanyBean>>();

			for (DataBseCompanyBean dataBseCompanyBean : dataBseCompanyBeans) {
				SM_Utilities.log("Read_52WeekHighLow_FromBSE | Scrip Code:" + dataBseCompanyBean.getScripCode() + " | MultiThread");
				BSE52WeekHighLowCallable callable = new BSE52WeekHighLowCallable(dataBseCompanyBean);
				FutureTask<DataBseCompanyBean> futureTask = new FutureTask<DataBseCompanyBean>(callable);
				futureTasks.add(futureTask);
				executor.execute(futureTask);
			}

			for (FutureTask<DataBseCompanyBean> futureTask : futureTasks) {
				futureTask.get();
			}
			SM_Utilities.log("Read_52WeekHighLow_FromBSE | MultiThread | Complete");

			executor.shutdown();
		} else {
			for (DataBseCompanyBean dataBseCompanyBean : dataBseCompanyBeans) {
				SM_Utilities.log("Read_52WeekHighLow_FromBSE | Scrip Code:" + dataBseCompanyBean.getScripCode());
				read(dataBseCompanyBean);
			}
		}
	}

	public static void read(DataBseCompanyBean dataBseCompanyBean) throws Exception {
		try {
			String html = SM_Utilities.getURLContentAsString(SM_Utilities.getSMProperty("URL_52WeekHighLow_FromBSE").replaceAll("@SCRIP_CODE@", "" + dataBseCompanyBean.getScripCode()));

			if (html != null) {
				String usableText = html.substring(html.indexOf("newseoscripfig") + 14);
				html = null;
				String tempStr = usableText.substring(usableText.indexOf(">") + 1, usableText.indexOf("<"));
				double yearHighVal = Double.valueOf((tempStr.substring(0, tempStr.indexOf("("))).replaceAll(",", ""));
				String yearHighDate = tempStr.substring(tempStr.indexOf("(") + 1, tempStr.indexOf(")"));
				dataBseCompanyBean.setYearHigh(yearHighVal);
				dataBseCompanyBean.setYearHighDate(yearHighDate);

				usableText = usableText.substring(usableText.indexOf("newseoscripfig"));
				tempStr = usableText.substring(usableText.indexOf(">") + 1, usableText.indexOf("<"));
				usableText = null;
				double yearLowVal = Double.valueOf((tempStr.substring(0, tempStr.indexOf("("))).replaceAll(",", ""));
				String yearLowDate = tempStr.substring(tempStr.indexOf("(") + 1, tempStr.indexOf(")"));
				dataBseCompanyBean.setYearLow(yearLowVal);
				dataBseCompanyBean.setYearLowDate(yearLowDate);
			}
		} catch (Exception exp) {
			SM_Utilities.logConsole("Read_52WeekHighLow_FromBSE | read(comp) | " + dataBseCompanyBean.getScripCode() + " | " + exp.toString());
		}

	}

	public static class BSE52WeekHighLowCallable implements Callable<DataBseCompanyBean> {
		private DataBseCompanyBean dataBseCompanyBean;

		public BSE52WeekHighLowCallable(DataBseCompanyBean dataBseCompanyBean) {
			this.dataBseCompanyBean = dataBseCompanyBean;
		}

		@Override
		public DataBseCompanyBean call() throws Exception {
			read(dataBseCompanyBean);
			return dataBseCompanyBean;
		}

	}
}
