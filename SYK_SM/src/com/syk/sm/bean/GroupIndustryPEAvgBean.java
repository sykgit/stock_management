/**
 * 
 */
package com.syk.sm.bean;

/**
 * @author skuppuraju
 *
 */
public class GroupIndustryPEAvgBean {
	private String group;
	private String industry;
	private int pe;

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public String getIndustry() {
		return industry;
	}

	public void setIndustry(String industry) {
		this.industry = industry;
	}

	public int getPe() {
		return pe;
	}

	public void setPe(int pe) {
		this.pe = pe;
	}

	@Override
	public String toString() {
		return "GroupIndustryPEAvgBean [group=" + group + ", industry=" + industry + ", pe=" + pe + "]";
	}
}
