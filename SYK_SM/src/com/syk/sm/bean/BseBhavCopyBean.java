/**
 * 
 */
package com.syk.sm.bean;

/**
 * @author skuppuraju
 *
 */
public class BseBhavCopyBean {
	int scripCode;
	String companyName;
	String scripGroup;
	String scripType;
	double daysOpen;
	double daysHigh;
	double daysLow;
	double daysClose;
	double daysLast;
	double prevDaysClose;
	long noOfTrades;
	long noOfShares;
	double netTurnOver;

	public int getScripCode() {
		return scripCode;
	}

	public void setScripCode(int scripCode) {
		this.scripCode = scripCode;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public String getScripGroup() {
		return scripGroup;
	}

	public void setScripGroup(String scripGroup) {
		this.scripGroup = scripGroup;
	}

	public String getScripType() {
		return scripType;
	}

	public void setScripType(String scripType) {
		this.scripType = scripType;
	}

	public double getDaysOpen() {
		return daysOpen;
	}

	public void setDaysOpen(double daysOpen) {
		this.daysOpen = daysOpen;
	}

	public double getDaysHigh() {
		return daysHigh;
	}

	public void setDaysHigh(double daysHigh) {
		this.daysHigh = daysHigh;
	}

	public double getDaysLow() {
		return daysLow;
	}

	public void setDaysLow(double daysLow) {
		this.daysLow = daysLow;
	}

	public double getDaysClose() {
		return daysClose;
	}

	public void setDaysClose(double daysClose) {
		this.daysClose = daysClose;
	}

	public double getDaysLast() {
		return daysLast;
	}

	public void setDaysLast(double daysLast) {
		this.daysLast = daysLast;
	}

	public double getPrevDaysClose() {
		return prevDaysClose;
	}

	public void setPrevDaysClose(double prevDaysClose) {
		this.prevDaysClose = prevDaysClose;
	}

	public long getNoOfTrades() {
		return noOfTrades;
	}

	public void setNoOfTrades(long noOfTrades) {
		this.noOfTrades = noOfTrades;
	}

	public long getNoOfShares() {
		return noOfShares;
	}

	public void setNoOfShares(long noOfShares) {
		this.noOfShares = noOfShares;
	}

	public double getNetTurnOver() {
		return netTurnOver;
	}

	public void setNetTurnOver(double netTurnOver) {
		this.netTurnOver = netTurnOver;
	}

	@Override
	public String toString() {
		return "BseBhavCopyBean [scripCode=" + scripCode + ", companyName=" + companyName + ", scripGroup=" + scripGroup + ", scripType=" + scripType + ", daysOpen=" + daysOpen + ", daysHigh="
				+ daysHigh + ", daysLow=" + daysLow + ", daysClose=" + daysClose + ", daysLast=" + daysLast + ", prevDaysClose=" + prevDaysClose + ", noOfTrades=" + noOfTrades + ", noOfShares="
				+ noOfShares + ", netTurnOver=" + netTurnOver + "]";
	}
}
