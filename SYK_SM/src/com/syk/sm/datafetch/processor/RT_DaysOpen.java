package com.syk.sm.datafetch.processor;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import com.syk.sm.bean.AnalysisBseCompCallToMake;
import com.syk.sm.utility.SM_Utilities;

public class RT_DaysOpen {

	public static void getDaysOpenPrice(ArrayList<AnalysisBseCompCallToMake> analysisBseCompCallToMakeBeans) throws Exception {
		ExecutorService executor = Executors.newFixedThreadPool(5000);
		ArrayList<FutureTask<AnalysisBseCompCallToMake>> futureTasks = new ArrayList<FutureTask<AnalysisBseCompCallToMake>>();

		for (AnalysisBseCompCallToMake analysisBseCompCallBean : analysisBseCompCallToMakeBeans) {
			SM_Utilities.log("RT_DaysOpen | Scrip Code:" + analysisBseCompCallBean.getScripCode() + " | MultiThread");

			RT_DO_Callable callable = new RT_DO_Callable(analysisBseCompCallBean);
			FutureTask<AnalysisBseCompCallToMake> futureTask = new FutureTask<AnalysisBseCompCallToMake>(callable);
			futureTasks.add(futureTask);
			executor.execute(futureTask);
		}

		for (FutureTask<AnalysisBseCompCallToMake> futureTask : futureTasks) {
			futureTask.get();
		}
		SM_Utilities.log("RT_DaysOpen | MultiThread | Complete");

		executor.shutdown();
	}

	public static void getDaysOpenPrice(AnalysisBseCompCallToMake analysisBseCompCallToMake) throws Exception {
		String html = SM_Utilities.getURLContentAsString(SM_Utilities.getSMProperty("URL_RealTimeDaysOpen_FromBSE").replaceAll("@SCRIP_CODE@", "" + analysisBseCompCallToMake.getScripCode()));

		if (html != null) {
			html = html.substring(html.indexOf(",") + 1);
			html = html.substring(0, html.indexOf(","));
			double daysOpen = Double.valueOf(html);

			analysisBseCompCallToMake.setDaysOpen(daysOpen);
			
		}
	}

	public static class RT_DO_Callable implements Callable<AnalysisBseCompCallToMake> {
		private AnalysisBseCompCallToMake analysisBseCompCallToMake;

		public RT_DO_Callable(AnalysisBseCompCallToMake analysisBseCompCallToMake) {
			this.analysisBseCompCallToMake = analysisBseCompCallToMake;
		}

		@Override
		public AnalysisBseCompCallToMake call() throws Exception {
			getDaysOpenPrice(analysisBseCompCallToMake);
			return analysisBseCompCallToMake;
		}

	}

}
