/**
 * 
 */
package com.syk.sm.bean;

/**
 * @author skuppuraju
 *
 */
public class ShareHoldingPatternBean {
	private int scripCode;
	private double bseQtrNo;
	private double promoterSplitPercent;
	private double promoterSplitPledgedPercent;
	private double publicSplitPercent;
	private double publicSplitPledgedPercent;
	private double custodianSplitPercent;
	private double custodianSplitPledgedPercent;

	public int getScripCode() {
		return scripCode;
	}

	public void setScripCode(int scripCode) {
		this.scripCode = scripCode;
	}

	public double getBseQtrNo() {
		return bseQtrNo;
	}

	public void setBseQtrNo(double bseQtrNo) {
		this.bseQtrNo = bseQtrNo;
	}

	public double getPromoterSplitPercent() {
		return promoterSplitPercent;
	}

	public void setPromoterSplitPercent(double promoterSplitPercent) {
		this.promoterSplitPercent = promoterSplitPercent;
	}

	public double getPromoterSplitPledgedPercent() {
		return promoterSplitPledgedPercent;
	}

	public void setPromoterSplitPledgedPercent(double promoterSplitPledgedPercent) {
		this.promoterSplitPledgedPercent = promoterSplitPledgedPercent;
	}

	public double getPublicSplitPercent() {
		return publicSplitPercent;
	}

	public void setPublicSplitPercent(double publicSplitPercent) {
		this.publicSplitPercent = publicSplitPercent;
	}

	public double getPublicSplitPledgedPercent() {
		return publicSplitPledgedPercent;
	}

	public void setPublicSplitPledgedPercent(double publicSplitPledgedPercent) {
		this.publicSplitPledgedPercent = publicSplitPledgedPercent;
	}

	public double getCustodianSplitPercent() {
		return custodianSplitPercent;
	}

	public void setCustodianSplitPercent(double custodianSplitPercent) {
		this.custodianSplitPercent = custodianSplitPercent;
	}

	public double getCustodianSplitPledgedPercent() {
		return custodianSplitPledgedPercent;
	}

	public void setCustodianSplitPledgedPercent(double custodianSplitPledgedPercent) {
		this.custodianSplitPledgedPercent = custodianSplitPledgedPercent;
	}

	@Override
	public String toString() {
		return "ShareHoldingPatternBean [scripCode=" + scripCode + ", bseQtrNo=" + bseQtrNo + ", promoterSplitPercent=" + promoterSplitPercent + ", promoterSplitPledgedPercent="
				+ promoterSplitPledgedPercent + ", publicSplitPercent=" + publicSplitPercent + ", publicSplitPledgedPercent=" + publicSplitPledgedPercent + ", custodianSplitPercent="
				+ custodianSplitPercent + ", custodianSplitPledgedPercent=" + custodianSplitPledgedPercent + "]";
	}

}
