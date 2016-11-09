package com.syk.sm.bean;
import java.util.Calendar;

import com.syk.sm.utility.SM_Utilities;

/**
 * @author skuppuraju
 *
 */
public class AnalysisCompanyResultsBean {
	private int scripCode;
	private Calendar analysisDate;

	// ----RESERVES---------
	private double reservesPctChange;

	// ----NET SALES---------
	private double netSalesQToQPctChange;
	private double netSalesQOnQPctChange;

	// ----Net Profit---------
	private double netProfitQToQPctChange;
	private double netProfitQOnQPctChange;

	// ----NET PERCENTAGE ---------
	private double netPercentageChange;

	public int getScripCode() {
		return scripCode;
	}

	public void setScripCode(int scripCode) {
		this.scripCode = scripCode;
	}

	public Calendar getAnalysisDate() {
		return analysisDate;
	}

	public void setAnalysisDate(Calendar analysisDate) {
		this.analysisDate = analysisDate;
	}

	public double getReservesPctChange() {
		return reservesPctChange;
	}

	public void setReservesPctChange(double reservesPctChange) {
		this.reservesPctChange = Double.valueOf(SM_Utilities.formatDoubleToTwoDecimals(reservesPctChange));
	}

	public double getNetSalesQToQPctChange() {
		return netSalesQToQPctChange;
	}

	public void setNetSalesQToQPctChange(double netSalesQToQPctChange) {
		this.netSalesQToQPctChange = Double.valueOf(SM_Utilities.formatDoubleToTwoDecimals(netSalesQToQPctChange));
	}

	public double getNetSalesQOnQPctChange() {
		return netSalesQOnQPctChange;
	}

	public void setNetSalesQOnQPctChange(double netSalesQOnQPctChange) {
		this.netSalesQOnQPctChange = Double.valueOf(SM_Utilities.formatDoubleToTwoDecimals(netSalesQOnQPctChange));
	}

	public double getNetProfitQToQPctChange() {
		return netProfitQToQPctChange;
	}

	public void setNetProfitQToQPctChange(double netProfitQToQPctChange) {
		this.netProfitQToQPctChange = Double.valueOf(SM_Utilities.formatDoubleToTwoDecimals(netProfitQToQPctChange));
	}

	public double getNetProfitQOnQPctChange() {
		return netProfitQOnQPctChange;
	}

	public void setNetProfitQOnQPctChange(double netProfitQOnQPctChange) {
		this.netProfitQOnQPctChange = Double.valueOf(SM_Utilities.formatDoubleToTwoDecimals(netProfitQOnQPctChange));
	}

	public double getNetPercentageChange() {
		return netPercentageChange;
	}

	public void setNetPercentageChange(double netPercentageChange) {
		this.netPercentageChange = Double.valueOf(SM_Utilities.formatDoubleToTwoDecimals(netPercentageChange));
	}

	@Override
	public String toString() {
		return "AnalysisCompanyResultsBean [scripCode=" + scripCode + ", analysisDate=" + analysisDate + ", reservesPctChange=" + reservesPctChange + ", netSalesQToQPctChange=" + netSalesQToQPctChange
				+ ", netSalesQOnQPctChange=" + netSalesQOnQPctChange + ", netProfitQToQPctChange=" + netProfitQToQPctChange + ", netProfitQOnQPctChange=" + netProfitQOnQPctChange
				+ ", netPercentageChange=" + netPercentageChange + "]";
	}
}
