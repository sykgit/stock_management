package com.syk.sm.datafetch.processor;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import com.syk.sm.bean.DataBseCompanyBean;
import com.syk.sm.bean.ShareHoldingPatternBean;
import com.syk.sm.utility.SM_Utilities;
import com.syk.sm.utility.SM_Utilities.COMPANY_NEWS_TYPE;

public class Read_ShareHoldingPattern_FromBSE {
	public static void main(String args[]) throws Exception {
		System.out.println(getSHP(530127,88));
	}

	public static ArrayList<ShareHoldingPatternBean> getSHP(ArrayList<DataBseCompanyBean> dataBseCompanyBeans, double maxFetchQtrNo, ArrayList<Double> bseQtrNosList, boolean goMultiThreaded,
			boolean fetchAlways) throws Exception {
		ArrayList<ShareHoldingPatternBean> shpBeansList = new ArrayList<ShareHoldingPatternBean>();
		if (goMultiThreaded) {
			ExecutorService executor = Executors.newFixedThreadPool(5000);

			ArrayList<FutureTask<DataBseCompanyBean>> futureTasksSHPNews = new ArrayList<FutureTask<DataBseCompanyBean>>();
			ArrayList<FutureTask<ShareHoldingPatternBean>> futureTasksSHPs = new ArrayList<FutureTask<ShareHoldingPatternBean>>();

			// Fetch News
			for (DataBseCompanyBean dataBseCompanyBean : dataBseCompanyBeans) {
				SM_Utilities.log("Read_ShareHoldingPattern_FromBSE | getSHP | " + dataBseCompanyBean.getScripCode() + " | SHP_NEWS | MultiThreaded");
				ShareHoldingPatternNewsCallable callable = new ShareHoldingPatternNewsCallable(dataBseCompanyBean);
				FutureTask<DataBseCompanyBean> futureTask = new FutureTask<DataBseCompanyBean>(callable);
				futureTasksSHPNews.add(futureTask);
				executor.execute(futureTask);
			}

			for (FutureTask<DataBseCompanyBean> futureTask : futureTasksSHPNews) {
				futureTask.get();
			}

			// Fetch SHP
			for (DataBseCompanyBean dataBseCompanyBean : dataBseCompanyBeans) {
				if (dataBseCompanyBean.getLastShpFetchQtr() == null) {
					dataBseCompanyBean.setLastShpFetchQtr(0.0);
				}

				if (fetchAlways || dataBseCompanyBean.getLatestShpNewsDate() == null || SM_Utilities.areCalenderObjectsEqualToDates(dataBseCompanyBean.getLatestShpNewsDate(), Calendar.getInstance())
						|| (SM_Utilities.isDateAfter(dataBseCompanyBean.getLatestShpNewsDate(), SM_Utilities.getStartOfQtrDate()) && dataBseCompanyBean.getLastShpFetchQtr() < maxFetchQtrNo)) {

					ArrayList<Double> qtrNosToFetch = SM_Utilities.getQtrNosToFetch(dataBseCompanyBean.getLastShpFetchQtr(), maxFetchQtrNo, bseQtrNosList);
					for (double qtrNo : qtrNosToFetch) {
						if (((int) qtrNo) == qtrNo) {
							SM_Utilities.log("Read_ShareHoldingPattern_FromBSE | getSHP | " + dataBseCompanyBean.getScripCode() + " | " + qtrNo + " | SHP_RETRIEVE | MultiThreaded");
							ShareHoldingPatternCallable callable = new ShareHoldingPatternCallable(dataBseCompanyBean, qtrNo);
							FutureTask<ShareHoldingPatternBean> futureTask = new FutureTask<ShareHoldingPatternBean>(callable);
							futureTasksSHPs.add(futureTask);
							executor.execute(futureTask);
						}
					}

				}
			}

			for (FutureTask<ShareHoldingPatternBean> futureTask : futureTasksSHPs) {
				ShareHoldingPatternBean shpBean = futureTask.get();
				if (shpBean != null) {
					shpBeansList.add(shpBean);
				}
			}

			SM_Utilities.log("Read_ShareHoldingPattern_FromBSE | MultiThread | Complete");

			executor.shutdown();

		} else {
			for (DataBseCompanyBean dataBseCompanyBean : dataBseCompanyBeans) {
				HashMap<COMPANY_NEWS_TYPE, ArrayList<String>> companyShpNews = Read_CompanyNews_FromBSE.read(dataBseCompanyBean.getScripCode(), dataBseCompanyBean.getLastShpFetchDate(),
						Calendar.getInstance(), COMPANY_NEWS_TYPE.COMPANY_UPDATE, "Shareholding");

				ArrayList<Double> qtrNosToFetch = SM_Utilities.getQtrNosToFetch(dataBseCompanyBean.getLastShpFetchQtr(), maxFetchQtrNo, bseQtrNosList);
				for (double qtrNo : qtrNosToFetch) {
					if (((int) qtrNo) == qtrNo) {
						SM_Utilities.log("Read_ShareHoldingPattern_FromBSE | getSHP | " + dataBseCompanyBean.getScripCode() + " | " + qtrNo);
						ShareHoldingPatternBean shpBean = getSHP(dataBseCompanyBean.getScripCode(), qtrNo);
						if (shpBean != null) {
							shpBeansList.add(shpBean);
						}
					}
				}

				if (companyShpNews != null && companyShpNews.size() > 0) {
					dataBseCompanyBean.setLatestShpNewsDate(Calendar.getInstance());
				}
			}
		}
		return shpBeansList;
	}

	public static ArrayList<ShareHoldingPatternBean> getSHP(DataBseCompanyBean compBean, ArrayList<Double> qtrNosToFetch) throws Exception {
		ArrayList<ShareHoldingPatternBean> shpBeansForCompany = new ArrayList<ShareHoldingPatternBean>();
		if (qtrNosToFetch != null && qtrNosToFetch.size() > 0) {
			for (double qtrNo : qtrNosToFetch) {
				if (((int) qtrNo) == qtrNo) {
					SM_Utilities.log("Read_ShareHoldingPattern_FromBSE | getSHP | " + compBean.getScripCode() + " | " + qtrNo);
					ShareHoldingPatternBean shpBean = getSHP(compBean.getScripCode(), qtrNo);
					if (shpBean != null) {
						shpBeansForCompany.add(shpBean);
					}
				}
			}
		}
		return shpBeansForCompany;
	}

	public static ShareHoldingPatternBean getSHP(int scripCode, double qtrNo) throws Exception {
		String html = SM_Utilities.getURLContentAsString(
				(SM_Utilities.getSMProperty("URL_ShareHoldingPattern_FromBSE").replaceAll("@SCRIP_CODE@", "" + scripCode)).replaceAll("@QTR_NO@", SM_Utilities.formatDoubleToTwoDecimals(qtrNo)));

		try {
			if (html.indexOf("Total shareholding of Promoter and Promoter Group (A)") > -1) {
				ShareHoldingPatternBean shpBean = new ShareHoldingPatternBean();
				shpBean.setScripCode(scripCode);
				shpBean.setBseQtrNo(qtrNo);

				String promoterSH_HTML = html.substring(html.indexOf("Total shareholding of Promoter and Promoter Group (A)"), html.indexOf("(B) Public Shareholding"));
				String publicSH_HTML = html.substring(html.indexOf("Total Public shareholding (B)"), html.indexOf("Total (A)+(B)"));
				String custodianSH_HTML = html.substring(html.indexOf("(C) Shares held by Custodians and against which Depository Receipts have been issued"), html.indexOf("Total (A)+(B)+(C)"));
				custodianSH_HTML = custodianSH_HTML.substring(custodianSH_HTML.indexOf("Sub Total"));

				html = null;

				// Get Promoter - Details
				String promoterSplit[] = promoterSH_HTML.split("<b>");
				shpBean.setPromoterSplitPercent(Double.valueOf(promoterSplit[5].substring(0, promoterSplit[5].indexOf("<"))));
				shpBean.setPromoterSplitPledgedPercent(Double.valueOf(promoterSplit[7].substring(0, promoterSplit[7].indexOf("<"))));

				// Get Public - Details
				String publicSplit[] = publicSH_HTML.split("<b>");
				shpBean.setPublicSplitPercent(Double.valueOf(publicSplit[5].substring(0, publicSplit[5].indexOf("<"))));
				shpBean.setPublicSplitPledgedPercent(Double.valueOf(publicSplit[7].substring(0, publicSplit[7].indexOf("<"))));

				// Get Custodian - Details
				String custodianSplit[] = custodianSH_HTML.split("<b>");
				shpBean.setCustodianSplitPercent(Double.valueOf(custodianSplit[5].substring(0, custodianSplit[5].indexOf("<"))));
				shpBean.setCustodianSplitPledgedPercent(Double.valueOf(custodianSplit[7].substring(0, custodianSplit[7].indexOf("<"))));

				return shpBean;
			}
		} catch (Exception exp) {
			SM_Utilities.logConsole("Read_ShareHoldingPattern_FromBSE | getSHP | ERROR | " + scripCode + " | " + qtrNo);
			exp.printStackTrace();
			throw exp;
		}
		return null;
	}

	public static class ShareHoldingPatternCallable implements Callable<ShareHoldingPatternBean> {
		private DataBseCompanyBean dataBseCompanyBean;
		private double qtrNo;

		public ShareHoldingPatternCallable(DataBseCompanyBean dataBseCompanyBean, double qtrNo) {
			this.dataBseCompanyBean = dataBseCompanyBean;
			this.qtrNo = qtrNo;
		}

		@Override
		public ShareHoldingPatternBean call() throws Exception {
			ShareHoldingPatternBean shpBean = getSHP(dataBseCompanyBean.getScripCode(), qtrNo);
			return shpBean;
		}

	}

	public static class ShareHoldingPatternNewsCallable implements Callable<DataBseCompanyBean> {
		private DataBseCompanyBean dataBseCompanyBean;

		public ShareHoldingPatternNewsCallable(DataBseCompanyBean dataBseCompanyBean) {
			this.dataBseCompanyBean = dataBseCompanyBean;
		}

		@Override
		public DataBseCompanyBean call() throws Exception {
			boolean newsExists = Read_CompanyNews_FromBSE.newsExist(dataBseCompanyBean.getScripCode(), dataBseCompanyBean.getLatestShpNewsDate(), Calendar.getInstance(),
					COMPANY_NEWS_TYPE.COMPANY_UPDATE, "Shareholding");

			if (newsExists) {
				dataBseCompanyBean.setLatestShpNewsDate(Calendar.getInstance());
			}
			return dataBseCompanyBean;
		}

	}
}
