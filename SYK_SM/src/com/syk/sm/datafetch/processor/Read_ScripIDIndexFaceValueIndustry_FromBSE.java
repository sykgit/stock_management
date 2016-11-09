package com.syk.sm.datafetch.processor;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import com.syk.sm.bean.DataBseCompanyBean;
import com.syk.sm.utility.SM_Utilities;

public class Read_ScripIDIndexFaceValueIndustry_FromBSE {

	public static void read(ArrayList<DataBseCompanyBean> dataBseCompanyBeans, boolean goMultiThreaded) throws Exception {
		if (goMultiThreaded) {
			ExecutorService executor = Executors.newFixedThreadPool(5000);
			ArrayList<FutureTask<DataBseCompanyBean>> futureTasks = new ArrayList<FutureTask<DataBseCompanyBean>>();

			for (DataBseCompanyBean dataBseCompanyBean : dataBseCompanyBeans) {
				SM_Utilities.log("Read_ScripIDIndexFaceValueIndustry_FromBSE | Scrip Code:" + dataBseCompanyBean.getScripCode() + " | MultiThread");
				ScripIDIndexFaceValueIndustryCallable callable = new ScripIDIndexFaceValueIndustryCallable(dataBseCompanyBean);
				FutureTask<DataBseCompanyBean> futureTask = new FutureTask<DataBseCompanyBean>(callable);
				futureTasks.add(futureTask);
				executor.execute(futureTask);
			}

			for (FutureTask<DataBseCompanyBean> futureTask : futureTasks) {
				futureTask.get();
			}
			SM_Utilities.log("Read_ScripIDIndexFaceValueIndustry_FromBSE | MultiThread | Complete");

			executor.shutdown();

		} else {
			for (DataBseCompanyBean dataBseCompanyBean : dataBseCompanyBeans) {
				SM_Utilities.log("Read_ScripIDIndexFaceValueIndustry_FromBSE | Scrip Code:" + dataBseCompanyBean.getScripCode());
				read(dataBseCompanyBean);
			}
		}
	}

	private static void read(DataBseCompanyBean dataBseCompany) throws Exception {
		try {
			String html = SM_Utilities.getURLContentAsString(SM_Utilities.getSMProperty("URL_ScripIDIndexFaceValueIndustry").replaceAll("@SCRIP_CODE@", "" + dataBseCompany.getScripCode()));

			if (html != null && html.indexOf("No record found") < 0) {
				String secId = html.substring(html.lastIndexOf("Security ID"));
				secId = secId.substring(secId.indexOf("'7%'"));
				secId = secId.substring(secId.indexOf(">") + 1);
				secId = secId.substring(0, secId.indexOf("<")).trim();
				if (secId.length() == 0) {
					throw new Exception("SECURITY_ID is empty");
				}

				secId = secId.replaceAll("[*]", "");
				dataBseCompany.setScripID(secId);

				String associatedIndex = html.substring(html.indexOf("'13%'"));
				associatedIndex = associatedIndex.substring(0, associatedIndex.indexOf("</td>"));
				if (associatedIndex.split("/").length > 1) {
					associatedIndex = (associatedIndex.split("/")[1]).trim();
				} else {
					associatedIndex = null;
				}
				dataBseCompany.setAssociatedIndex(associatedIndex);

				String faceValue = html.substring(html.lastIndexOf("Face value"));
				faceValue = faceValue.substring(faceValue.indexOf("'7%'"));
				faceValue = faceValue.substring(faceValue.indexOf(">") + 1);
				faceValue = faceValue.substring(0, faceValue.indexOf("<")).trim();
				if (faceValue.equals("") || faceValue.equals("-")) {
					dataBseCompany.setFaceValue(null);
				} else {
					dataBseCompany.setFaceValue(Double.valueOf(faceValue));
				}

				String industry = html.substring(html.lastIndexOf("'18%'"));
				industry = industry.substring(industry.indexOf(">") + 1);
				industry = (industry.substring(0, industry.indexOf("<"))).trim();
				dataBseCompany.setIndustry(industry);
			}
		} catch (Exception ex) {
			SM_Utilities.logConsole("Read_ScripIDIndexFaceValueIndustry_FromBSE | Error Reading Scrip Code:" + dataBseCompany.getScripCode());
			throw ex;
		}
	}

	public static class ScripIDIndexFaceValueIndustryCallable implements Callable<DataBseCompanyBean> {
		private DataBseCompanyBean dataBseCompanyBean;

		public ScripIDIndexFaceValueIndustryCallable(DataBseCompanyBean dataBseCompanyBean) {
			this.dataBseCompanyBean = dataBseCompanyBean;
		}

		@Override
		public DataBseCompanyBean call() throws Exception {
			read(dataBseCompanyBean);
			return dataBseCompanyBean;
		}

	}
}