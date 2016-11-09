package com.syk.sm.datafetch.processor;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import com.syk.sm.bean.AnalysisBseCompCallToMake;
import com.syk.sm.utility.SM_Utilities;
import com.syk.sm.utility.SM_Utilities.CALL_TO_MAKE;

public class RT_CurrentPrice_ComputedCall {

	public static void updateAnalysis(ArrayList<AnalysisBseCompCallToMake> analysisBseCompCallToMakeBeans) throws Exception {
		ExecutorService executor = Executors.newFixedThreadPool(5000);
		ArrayList<FutureTask<AnalysisBseCompCallToMake>> futureTasks = new ArrayList<FutureTask<AnalysisBseCompCallToMake>>();

		for (AnalysisBseCompCallToMake analysisBseCompCallBean : analysisBseCompCallToMakeBeans) {
			SM_Utilities.log("RT_CurrentPrice_ComputedCall | Scrip Code:" + analysisBseCompCallBean.getScripCode() + " | MultiThread");

			RT_CP_CC_Callable callable = new RT_CP_CC_Callable(analysisBseCompCallBean);
			FutureTask<AnalysisBseCompCallToMake> futureTask = new FutureTask<AnalysisBseCompCallToMake>(callable);
			futureTasks.add(futureTask);
			executor.execute(futureTask);
		}

		for (FutureTask<AnalysisBseCompCallToMake> futureTask : futureTasks) {
			futureTask.get();
		}
		SM_Utilities.log("RT_CurrentPrice_ComputedCall | MultiThread | Complete");

		executor.shutdown();
	}

	public static void updateAnalysis(AnalysisBseCompCallToMake analysisBseCompCallToMake) throws Exception {
		String html = SM_Utilities.getURLContentAsString(SM_Utilities.getSMProperty("URL_RealTimeScripPrice_FromBSE").replaceAll("@SCRIP_CODE@", "" + analysisBseCompCallToMake.getScripCode()));

		if (html != null) {
			html = html.substring(0, html.indexOf("</td>"));
			html = html.substring(html.lastIndexOf(">") + 1);
			html = html.replaceAll(",", "");

			double currentPriceToSave = Double.valueOf(html).doubleValue();
			int currentPrice = Double.valueOf(html).intValue();
			int lastClosingPrice = (int) analysisBseCompCallToMake.getDaysClosePrice();
			int diff = currentPrice - lastClosingPrice;

			analysisBseCompCallToMake.setCurrentPrice(currentPriceToSave);
			
			if (diff >= 0) {
				analysisBseCompCallToMake.setComputedCall(CALL_TO_MAKE.BUY);
			} else {
				analysisBseCompCallToMake.setComputedCall(CALL_TO_MAKE.SELL);
			}
		}
	}

	public static class RT_CP_CC_Callable implements Callable<AnalysisBseCompCallToMake> {
		private AnalysisBseCompCallToMake analysisBseCompCallToMake;

		public RT_CP_CC_Callable(AnalysisBseCompCallToMake analysisBseCompCallToMake) {
			this.analysisBseCompCallToMake = analysisBseCompCallToMake;
		}

		@Override
		public AnalysisBseCompCallToMake call() throws Exception {
			updateAnalysis(analysisBseCompCallToMake);
			return analysisBseCompCallToMake;
		}

	}

}
