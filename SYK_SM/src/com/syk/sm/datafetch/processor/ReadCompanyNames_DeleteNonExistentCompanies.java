package com.syk.sm.datafetch.processor;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import com.syk.sm.bean.DataBseCompanyBean;
import com.syk.sm.utility.SM_Utilities;

public class ReadCompanyNames_DeleteNonExistentCompanies {

	public static void doReadAndUpdate(ArrayList<DataBseCompanyBean> companyBeans, boolean goMultiThreaded) throws Exception {
		if (goMultiThreaded) {
			ExecutorService executor = Executors.newFixedThreadPool(5000);
			ArrayList<FutureTask<DataBseCompanyBean>> futureTasks = new ArrayList<FutureTask<DataBseCompanyBean>>();

			for (DataBseCompanyBean dataBseCompanyBean : companyBeans) {
				SM_Utilities.log("ReadCompanyNames_DeleteNonExistentCompanies | doCheck | Processing:" + dataBseCompanyBean.getScripCode() + " | MultiThread");
				CompanyExistsInBSECallable callable = new CompanyExistsInBSECallable(dataBseCompanyBean);
				FutureTask<DataBseCompanyBean> futureTask = new FutureTask<DataBseCompanyBean>(callable);
				futureTasks.add(futureTask);
				executor.execute(futureTask);
			}

			for (FutureTask<DataBseCompanyBean> futureTask : futureTasks) {
				futureTask.get();
			}
			SM_Utilities.log("ReadCompanyNames_DeleteNonExistentCompanies | MultiThread | Complete");

			executor.shutdown();

		} else {
			for (DataBseCompanyBean company : companyBeans) {
				SM_Utilities.log("ReadCompanyNames_DeleteNonExistentCompanies | doCheck | Processing:" + company.getScripCode());
				doReadAndUpdate(company);
			}
		}
	}

	public static void doReadAndUpdate(DataBseCompanyBean company) throws Exception {
		String html = SM_Utilities.getURLContentAsString(SM_Utilities.getSMProperty("URL_CheckIfCompanyExistsInBSE").replaceAll("@SCRIP_CODE@", "" + company.getScripCode()));

		if (html.indexOf("No Match found") > -1) {
			company.setMarkForDeletion(true);
			SM_Utilities.log("ReadCompanyNames_DeleteNonExistentCompanies | doCheck | " + company.getScripCode() + " marked for deletion.");
		}else{
			html=html.substring(html.indexOf("<span class='leftspan'>")+23);
			html=html.substring(0, html.indexOf("</span>"));
			company.setCompanyName(html);
		}
	}

	public static class CompanyExistsInBSECallable implements Callable<DataBseCompanyBean> {
		private DataBseCompanyBean dataBseCompanyBean;

		public CompanyExistsInBSECallable(DataBseCompanyBean dataBseCompanyBean) {
			this.dataBseCompanyBean = dataBseCompanyBean;
		}

		@Override
		public DataBseCompanyBean call() throws Exception {
			doReadAndUpdate(dataBseCompanyBean);
			return dataBseCompanyBean;
		}

	}
}
