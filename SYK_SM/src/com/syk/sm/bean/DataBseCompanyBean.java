/**
 * 
 */
package com.syk.sm.bean;

import java.util.Calendar;

/**
 * @author skuppuraju
 *
 */
public class DataBseCompanyBean {
	private int scripCode;
	private String companyName;
	private String scripGroup;
	private String scripType;
	private Double lastResultsFetchQtr;
	private String scripID;
	private String associatedIndex;
	private Double faceValue;
	private String industry;
	private boolean markForDeletion;
	private Double yearHigh;
	private String yearHighDate;
	private Double yearLow;
	private String yearLowDate;
	private Calendar lastResultsFetchDate;
	private Calendar latestResultsNewsDate;
	private Double lastShpFetchQtr;
	private Calendar lastShpFetchDate;
	private Calendar latestShpNewsDate;

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

	public Double getLastResultsFetchQtr() {
		return lastResultsFetchQtr;
	}

	public void setLastResultsFetchQtr(Double lastResultsFetchQtr) {
		this.lastResultsFetchQtr = lastResultsFetchQtr;
	}

	public String getScripID() {
		return scripID;
	}

	public void setScripID(String scripID) {
		this.scripID = scripID;
	}

	public String getAssociatedIndex() {
		return associatedIndex;
	}

	public void setAssociatedIndex(String associatedIndex) {
		this.associatedIndex = associatedIndex;
	}

	public Double getFaceValue() {
		return faceValue;
	}

	public void setFaceValue(Double faceValue) {
		this.faceValue = faceValue;
	}

	public String getIndustry() {
		return industry;
	}

	public void setIndustry(String industry) {
		this.industry = industry;
	}

	public boolean isMarkForDeletion() {
		return markForDeletion;
	}

	public void setMarkForDeletion(boolean markForDeletion) {
		this.markForDeletion = markForDeletion;
	}

	public Double getYearHigh() {
		return yearHigh;
	}

	public void setYearHigh(Double yearHigh) {
		this.yearHigh = yearHigh;
	}

	public String getYearHighDate() {
		return yearHighDate;
	}

	public void setYearHighDate(String yearHighDate) {
		this.yearHighDate = yearHighDate;
	}

	public Double getYearLow() {
		return yearLow;
	}

	public void setYearLow(Double yearLow) {
		this.yearLow = yearLow;
	}

	public String getYearLowDate() {
		return yearLowDate;
	}

	public void setYearLowDate(String yearLowDate) {
		this.yearLowDate = yearLowDate;
	}

	public Calendar getLastResultsFetchDate() {
		return lastResultsFetchDate;
	}

	public void setLastResultsFetchDate(Calendar lastResultsFetchDate) {
		this.lastResultsFetchDate = lastResultsFetchDate;
	}

	public Calendar getLatestResultsNewsDate() {
		return latestResultsNewsDate;
	}

	public void setLatestResultsNewsDate(Calendar latestResultsNewsDate) {
		this.latestResultsNewsDate = latestResultsNewsDate;
	}

	public Double getLastShpFetchQtr() {
		return lastShpFetchQtr;
	}

	public void setLastShpFetchQtr(Double lastShpFetchQtr) {
		this.lastShpFetchQtr = lastShpFetchQtr;
	}

	public Calendar getLastShpFetchDate() {
		return lastShpFetchDate;
	}

	public void setLastShpFetchDate(Calendar lastShpFetchDate) {
		this.lastShpFetchDate = lastShpFetchDate;
	}

	public Calendar getLatestShpNewsDate() {
		return latestShpNewsDate;
	}

	public void setLatestShpNewsDate(Calendar latestShpNewsDate) {
		this.latestShpNewsDate = latestShpNewsDate;
	}

	@Override
	public String toString() {
		return "DataBseCompanyBean [scripCode=" + scripCode + ", companyName=" + companyName + ", scripGroup=" + scripGroup + ", scripType=" + scripType + ", lastResultsFetchQtr="
				+ lastResultsFetchQtr + ", scripID=" + scripID + ", associatedIndex=" + associatedIndex + ", faceValue=" + faceValue + ", industry=" + industry + ", markForDeletion=" + markForDeletion
				+ ", yearHigh=" + yearHigh + ", yearHighDate=" + yearHighDate + ", yearLow=" + yearLow + ", yearLowDate=" + yearLowDate + ", lastResultsFetchDate=" + lastResultsFetchDate
				+ ", latestResultsNewsDate=" + latestResultsNewsDate + ", lastShpFetchQtr=" + lastShpFetchQtr + ", lastShpFetchDate=" + lastShpFetchDate + ", latestShpNewsDate=" + latestShpNewsDate
				+ ", getScripCode()=" + getScripCode() + ", getCompanyName()=" + getCompanyName() + ", getScripGroup()=" + getScripGroup() + ", getScripType()=" + getScripType()
				+ ", getLastResultsFetchQtr()=" + getLastResultsFetchQtr() + ", getScripID()=" + getScripID() + ", getAssociatedIndex()=" + getAssociatedIndex() + ", getFaceValue()=" + getFaceValue()
				+ ", getIndustry()=" + getIndustry() + ", isMarkForDeletion()=" + isMarkForDeletion() + ", getYearHigh()=" + getYearHigh() + ", getYearHighDate()=" + getYearHighDate()
				+ ", getYearLow()=" + getYearLow() + ", getYearLowDate()=" + getYearLowDate() + ", getLastResultsFetchDate()=" + getLastResultsFetchDate() + ", getLatestResultsNewsDate()="
				+ getLatestResultsNewsDate() + ", getLastShpFetchQtr()=" + getLastShpFetchQtr() + ", getLastShpFetchDate()=" + getLastShpFetchDate() + ", getLatestShpNewsDate()="
				+ getLatestShpNewsDate() + ", getClass()=" + getClass() + ", hashCode()=" + hashCode() + "]";
	}

}
