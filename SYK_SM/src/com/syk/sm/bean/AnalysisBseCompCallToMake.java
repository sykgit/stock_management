/**
 * 
 */
package com.syk.sm.bean;

import com.syk.sm.utility.SM_Utilities.CALL_TO_MAKE;

/**
 * @author skuppuraju
 *
 */
public class AnalysisBseCompCallToMake {
	private int scripCode;
	private String tradeDate;
	private CALL_TO_MAKE callToMake;
	private double oneDayDiff;
	private double twoDaySumOfDiff;
	private double threeDaySumOfDiff;
	private double sumOfAllDays;
	private double daysClosePrice;
	private CALL_TO_MAKE computedCall;
	private double currentPrice;
	private double daysOpen;
	private double upperCkt;
	private double lowerCkt;
	private long currentVolume;
	private long totBuyQty;
	private long totSellQty;
	private double topBuyRate;
	private long topBuyQty;
	private double topSellRate;
	private long topSellQty;
	private long top5BuyQty;
	private long top5SellQty;
	private double nCurrPrice;
	private long nCurrVolume;
	private long nTotalBuyQty;
	private long nTotalSellQty;
	private long nTop5BuyQty;
	private long nTop5SellQty;

	public int getScripCode() {
		return scripCode;
	}

	public void setScripCode(int scripCode) {
		this.scripCode = scripCode;
	}

	public String getTradeDate() {
		return tradeDate;
	}

	public void setTradeDate(String tradeDate) {
		this.tradeDate = tradeDate;
	}

	public CALL_TO_MAKE getCallToMake() {
		return callToMake;
	}

	public void setCallToMake(CALL_TO_MAKE callToMake) {
		this.callToMake = callToMake;
	}

	public double getOneDayDiff() {
		return oneDayDiff;
	}

	public void setOneDayDiff(double oneDayDiff) {
		this.oneDayDiff = oneDayDiff;
	}

	public double getTwoDaySumOfDiff() {
		return twoDaySumOfDiff;
	}

	public void setTwoDaySumOfDiff(double twoDaySumOfDiff) {
		this.twoDaySumOfDiff = twoDaySumOfDiff;
	}

	public double getThreeDaySumOfDiff() {
		return threeDaySumOfDiff;
	}

	public void setThreeDaySumOfDiff(double threeDaySumOfDiff) {
		this.threeDaySumOfDiff = threeDaySumOfDiff;
	}

	public double getSumOfAllDays() {
		return sumOfAllDays;
	}

	public void setSumOfAllDays(double sumOfAllDays) {
		this.sumOfAllDays = sumOfAllDays;
	}

	public double getDaysClosePrice() {
		return daysClosePrice;
	}

	public void setDaysClosePrice(double daysClosePrice) {
		this.daysClosePrice = daysClosePrice;
	}

	public CALL_TO_MAKE getComputedCall() {
		return computedCall;
	}

	public void setComputedCall(CALL_TO_MAKE computedCall) {
		this.computedCall = computedCall;
	}

	public double getCurrentPrice() {
		return currentPrice;
	}

	public void setCurrentPrice(double currentPrice) {
		this.currentPrice = currentPrice;
	}

	public double getDaysOpen() {
		return daysOpen;
	}

	public void setDaysOpen(double daysOpen) {
		this.daysOpen = daysOpen;
	}

	public double getLowerCkt() {
		return lowerCkt;
	}

	public void setLowerCkt(double lowerCkt) {
		this.lowerCkt = lowerCkt;
	}

	public double getUpperCkt() {
		return upperCkt;
	}

	public void setUpperCkt(double upperCkt) {
		this.upperCkt = upperCkt;
	}

	public long getCurrentVolume() {
		return currentVolume;
	}

	public void setCurrentVolume(long currentVolume) {
		this.currentVolume = currentVolume;
	}

	public long getTotBuyQty() {
		return totBuyQty;
	}

	public void setTotBuyQty(long totBuyQty) {
		this.totBuyQty = totBuyQty;
	}

	public long getTotSellQty() {
		return totSellQty;
	}

	public void setTotSellQty(long totSellQty) {
		this.totSellQty = totSellQty;
	}

	public double getTopBuyRate() {
		return topBuyRate;
	}

	public void setTopBuyRate(double topBuyRate) {
		this.topBuyRate = topBuyRate;
	}

	public long getTopBuyQty() {
		return topBuyQty;
	}

	public void setTopBuyQty(long topBuyQty) {
		this.topBuyQty = topBuyQty;
	}

	public double getTopSellRate() {
		return topSellRate;
	}

	public void setTopSellRate(double topSellRate) {
		this.topSellRate = topSellRate;
	}

	public long getTopSellQty() {
		return topSellQty;
	}

	public void setTopSellQty(long topSellQty) {
		this.topSellQty = topSellQty;
	}

	public long getTop5BuyQty() {
		return top5BuyQty;
	}

	public void setTop5BuyQty(long top5BuyQty) {
		this.top5BuyQty = top5BuyQty;
	}

	public long getTop5SellQty() {
		return top5SellQty;
	}

	public void setTop5SellQty(long top5SellQty) {
		this.top5SellQty = top5SellQty;
	}

	public double getnCurrPrice() {
		return nCurrPrice;
	}

	public void setnCurrPrice(double nCurrPrice) {
		this.nCurrPrice = nCurrPrice;
	}

	public long getnCurrVolume() {
		return nCurrVolume;
	}

	public void setnCurrVolume(long nCurrVolume) {
		this.nCurrVolume = nCurrVolume;
	}

	public long getnTotalBuyQty() {
		return nTotalBuyQty;
	}

	public void setnTotalBuyQty(long nTotalBuyQty) {
		this.nTotalBuyQty = nTotalBuyQty;
	}

	public long getnTotalSellQty() {
		return nTotalSellQty;
	}

	public void setnTotalSellQty(long nTotalSellQty) {
		this.nTotalSellQty = nTotalSellQty;
	}

	public long getnTop5BuyQty() {
		return nTop5BuyQty;
	}

	public void setnTop5BuyQty(long nTop5BuyQty) {
		this.nTop5BuyQty = nTop5BuyQty;
	}

	public long getnTop5SellQty() {
		return nTop5SellQty;
	}

	public void setnTop5SellQty(long nTop5SellQty) {
		this.nTop5SellQty = nTop5SellQty;
	}

	@Override
	public String toString() {
		return "AnalysisBseCompCallToMake [scripCode=" + scripCode + ", tradeDate=" + tradeDate + ", callToMake=" + callToMake + ", oneDayDiff=" + oneDayDiff + ", twoDaySumOfDiff=" + twoDaySumOfDiff
				+ ", threeDaySumOfDiff=" + threeDaySumOfDiff + ", sumOfAllDays=" + sumOfAllDays + ", daysClosePrice=" + daysClosePrice + ", computedCall=" + computedCall + ", currentPrice="
				+ currentPrice + ", daysOpen=" + daysOpen + ", upperCkt=" + upperCkt + ", lowerCkt=" + lowerCkt + ", currentVolume=" + currentVolume + ", totBuyQty=" + totBuyQty + ", totSellQty="
				+ totSellQty + ", topBuyRate=" + topBuyRate + ", topBuyQty=" + topBuyQty + ", topSellRate=" + topSellRate + ", topSellQty=" + topSellQty + ", top5BuyQty=" + top5BuyQty
				+ ", top5SellQty=" + top5SellQty + ", nCurrPrice=" + nCurrPrice + ", nCurrVolume=" + nCurrVolume + ", nTotalBuyQty=" + nTotalBuyQty + ", nTotalSellQty=" + nTotalSellQty
				+ ", nTop5BuyQty=" + nTop5BuyQty + ", nTop5SellQty=" + nTop5SellQty + "]";
	}
}
