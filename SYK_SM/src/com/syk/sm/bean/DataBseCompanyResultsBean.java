/**
 * 
 */
package com.syk.sm.bean;

/**
 * @author skuppuraju
 *
 */
public class DataBseCompanyResultsBean {
	private int scripCode;
	private double qtrNo;
	private boolean yearResult;
	private double totalIncome;
	private double otherIncome;
	private double expenditure;
	private double netProfit;
	private double equityCapital;
	private double reserves;
	private double bseEps;
	private double faceValue;
	private boolean triggeredByResultNews;

	public int getScripCode() {
		return scripCode;
	}

	public void setScripCode(int scripCode) {
		this.scripCode = scripCode;
	}

	public double getQtrNo() {
		return qtrNo;
	}

	public void setQtrNo(double qtrNo) {
		this.qtrNo = qtrNo;
	}

	public boolean isYearResult() {
		return yearResult;
	}

	public void setYearResult(boolean yearResult) {
		this.yearResult = yearResult;
	}

	public double getTotalIncome() {
		return totalIncome;
	}

	public void setTotalIncome(double totalIncome) {
		this.totalIncome = totalIncome;
	}

	public double getOtherIncome() {
		return otherIncome;
	}

	public void setOtherIncome(double otherIncome) {
		this.otherIncome = otherIncome;
	}

	public double getExpenditure() {
		return expenditure;
	}

	public void setExpenditure(double expenditure) {
		this.expenditure = expenditure;
	}

	public double getNetProfit() {
		return netProfit;
	}

	public void setNetProfit(double netProfit) {
		this.netProfit = netProfit;
	}

	public double getEquityCapital() {
		return equityCapital;
	}

	public void setEquityCapital(double equityCapital) {
		this.equityCapital = equityCapital;
	}

	public double getReserves() {
		return reserves;
	}

	public void setReserves(double reserves) {
		this.reserves = reserves;
	}

	public double getBseEps() {
		return bseEps;
	}

	public void setBseEps(double bseEps) {
		this.bseEps = bseEps;
	}

	public double getFaceValue() {
		return faceValue;
	}

	public void setFaceValue(double faceValue) {
		this.faceValue = faceValue;
	}

	public boolean isTriggeredByResultNews() {
		return triggeredByResultNews;
	}

	public void setTriggeredByResultNews(boolean triggeredByResultNews) {
		this.triggeredByResultNews = triggeredByResultNews;
	}

	@Override
	public String toString() {
		return "DataBseCompanyResultsBean [scripCode=" + scripCode + ", qtrNo=" + qtrNo + ", yearResult=" + yearResult + ", totalIncome=" + totalIncome + ", otherIncome=" + otherIncome
				+ ", expenditure=" + expenditure + ", netProfit=" + netProfit + ", equityCapital=" + equityCapital + ", reserves=" + reserves + ", bseEps=" + bseEps + ", faceValue=" + faceValue
				+ ", triggeredByResultNews=" + triggeredByResultNews + "]";
	}

}
