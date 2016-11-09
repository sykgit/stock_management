package com.syk.sm.datafetch.processor;

import java.util.ArrayList;
import java.util.HashMap;

import com.syk.sm.bean.DataBseQtrYearMarkerBean;
import com.syk.sm.utility.SM_Utilities;

/**
 *  This class is not required to be executed everyday.
 */

/**
 * @author skuppuraju
 *
 */
public class GenerateBSEQuarterAndYearMarkers {

	public static ArrayList<DataBseQtrYearMarkerBean> generate(double bseQtrNum, int qtrInYear, int year, int countOfQtrs) {
		HashMap<Integer, String> qtrNoToStrMap = new HashMap<Integer, String>();
		qtrNoToStrMap.put(1, "Mar");
		qtrNoToStrMap.put(2, "Jun");
		qtrNoToStrMap.put(3, "Sep");
		qtrNoToStrMap.put(4, "Dec");

		ArrayList<DataBseQtrYearMarkerBean> qyMarkersList = new ArrayList<DataBseQtrYearMarkerBean>();

		for (int i = 0; i < countOfQtrs; i++) {
			DataBseQtrYearMarkerBean qAndYMarkerMonth = new DataBseQtrYearMarkerBean();
			qAndYMarkerMonth.setQtrInYear(qtrInYear);
			qAndYMarkerMonth.setYear(year);
			qAndYMarkerMonth.setBseQtrNum(bseQtrNum);
			qAndYMarkerMonth.setQtrInYearStr(qtrNoToStrMap.get(qtrInYear) + "-" + year);
			qyMarkersList.add(qAndYMarkerMonth);

			DataBseQtrYearMarkerBean qAndYMarkerYear = new DataBseQtrYearMarkerBean();
			qAndYMarkerYear.setQtrInYear(-1);
			qAndYMarkerYear.setYear(year);
			qAndYMarkerYear.setBseQtrNum(bseQtrNum + 0.5);
			qAndYMarkerYear.setQtrInYearStr("" + year);
			qyMarkersList.add(qAndYMarkerYear);

			qtrInYear++;
			bseQtrNum++;

			if (qtrInYear == 5) {
				qtrInYear = 1;
				year++;
			}
		}

		SM_Utilities.log("GenerateBSEQuarterAndYearMarkers|generate|Completed Successfully.");
		return qyMarkersList;

	}
}
