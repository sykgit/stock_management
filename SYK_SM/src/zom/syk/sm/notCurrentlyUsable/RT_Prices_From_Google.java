package zom.syk.sm.notCurrentlyUsable;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import com.syk.sm.bean.AnalysisBseCompCallToMake;
import com.syk.sm.utility.SM_Utilities;

public class RT_Prices_From_Google {

	public static void getRealTimePrices(ArrayList<AnalysisBseCompCallToMake> analysisBseCompCallToMakeBeans, Connection con) throws Exception {
		ExecutorService executor = Executors.newFixedThreadPool(5000);
		ArrayList<FutureTask<AnalysisBseCompCallToMake>> futureTasks = new ArrayList<FutureTask<AnalysisBseCompCallToMake>>();

		for (AnalysisBseCompCallToMake analysisBseCompCallBean : analysisBseCompCallToMakeBeans) {
			SM_Utilities.log("RT_Prices_From_Google | Scrip Code:" + analysisBseCompCallBean.getScripCode() + " | MultiThread");

			RT_Google_CP_Callable callable = new RT_Google_CP_Callable(analysisBseCompCallBean);
			FutureTask<AnalysisBseCompCallToMake> futureTask = new FutureTask<AnalysisBseCompCallToMake>(callable);
			futureTasks.add(futureTask);
			executor.execute(futureTask);
		}
		
		
//		for()
		
		for (FutureTask<AnalysisBseCompCallToMake> futureTask : futureTasks) {
			futureTask.get();
		}
		SM_Utilities.log("RT_Prices_From_Google | MultiThread | Complete");

		executor.shutdown();
	}

	public static void getRealTimePrice(AnalysisBseCompCallToMake analysisBseCompCallToMake) throws Exception {
		try {

			String html = SM_Utilities.getURLContentAsString(SM_Utilities.getSMProperty("URL_RealTimePrices_FromGoogle").replaceAll("@SCRIP_CODE@", "" + analysisBseCompCallToMake.getScripCode()));
			SM_Utilities.log(html);
			double currPrice = getGooogleValue("l", html, analysisBseCompCallToMake.getScripCode());
			double openPrice = getGooogleValue("op", html, analysisBseCompCallToMake.getScripCode());
			long volume = Double.valueOf(getGooogleValue("vo", html, analysisBseCompCallToMake.getScripCode())).longValue();

			analysisBseCompCallToMake.setCurrentPrice(currPrice);
			analysisBseCompCallToMake.setCurrentVolume(volume);
			analysisBseCompCallToMake.setDaysOpen(openPrice);

		} catch (Exception exp) {
			SM_Utilities.log("Scrip_Code:" + analysisBseCompCallToMake.getScripCode() + " || " + exp.toString());
		}
	}

	private static double getGooogleValue(String token, String html, int scripCode) {
		double ret = 0.0;
		try {
			String tokenValStr = html.substring(html.indexOf("\"" + token + "\" : \"") + 6 + token.length());
			tokenValStr = tokenValStr.substring(0, tokenValStr.indexOf("\""));

			tokenValStr = tokenValStr.replaceAll(",", "");
			ret = Double.valueOf(tokenValStr);
		} catch (Exception exp) {
			SM_Utilities.log("Scrip_Code:" + scripCode + " || " + exp.toString());
		}
		return ret;
	}

	public static class RT_Google_CP_Callable implements Callable<AnalysisBseCompCallToMake> {
		private AnalysisBseCompCallToMake analysisBseCompCallToMake;

		public RT_Google_CP_Callable(AnalysisBseCompCallToMake analysisBseCompCallToMake) {
			this.analysisBseCompCallToMake = analysisBseCompCallToMake;
		}

		@Override
		public AnalysisBseCompCallToMake call() throws Exception {
			getRealTimePrice(analysisBseCompCallToMake);
			return analysisBseCompCallToMake;
		}

	}

}
