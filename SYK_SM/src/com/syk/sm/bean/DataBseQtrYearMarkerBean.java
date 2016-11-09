/**
 * 
 */
package com.syk.sm.bean;

/**
 * @author skuppuraju
 *
 */
public class DataBseQtrYearMarkerBean {
	int qtrInYear;
	int year;
	double bseQtrNum;
	String qtrInYearStr;

	/**
	 * @return the qtrInYear
	 */
	public int getQtrInYear() {
		return qtrInYear;
	}

	/**
	 * @param qtrInYear the qtrInYear to set
	 */
	public void setQtrInYear(int qtrInYear) {
		this.qtrInYear = qtrInYear;
	}

	/**
	 * @return the year
	 */
	public int getYear() {
		return year;
	}

	/**
	 * @param year the year to set
	 */
	public void setYear(int year) {
		this.year = year;
	}

	/**
	 * @return the bseQtrNum
	 */
	public double getBseQtrNum() {
		return bseQtrNum;
	}

	/**
	 * @param bseQtrNum the bseQtrNum to set
	 */
	public void setBseQtrNum(double bseQtrNum) {
		this.bseQtrNum = bseQtrNum;
	}

	/**
	 * @return the qtrInYearStr
	 */
	public String getQtrInYearStr() {
		return qtrInYearStr;
	}

	/**
	 * @param qtrInYearStr the qtrInYearStr to set
	 */
	public void setQtrInYearStr(String qtrInYearStr) {
		this.qtrInYearStr = qtrInYearStr;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "DataBseQtrYearMarkerBean [year=" + year + ", qtrNum=" + bseQtrNum + ", qtrInYear=" + qtrInYear + ", qtrInYearStr=" + qtrInYearStr + " ]";
	}

}
