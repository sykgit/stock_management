/**
 * 
 */
package com.syk.sm.analysis.processor;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;

import com.syk.sm.bean.GroupIndustryPEAvgBean;
import com.syk.sm.broker.SMBroker;
import com.syk.sm.utility.SM_Utilities;

/**
 * @author skuppuraju
 *
 */
public class Update_GroupIndustryMidPE {
	public static void update(Connection con) throws Exception {
		SM_Utilities.log("Update_GroupIndustryMidPE | Update | Start");

		ArrayList<GroupIndustryPEAvgBean> gipaList = new ArrayList<GroupIndustryPEAvgBean>();
		HashMap<String, HashMap<String, ArrayList<Integer>>> groupIndustryPEMap = SMBroker.getGroupAndIndustryPEMap(con);

		if (groupIndustryPEMap != null) {
			for (String group : groupIndustryPEMap.keySet()) {
				HashMap<String, ArrayList<Integer>> industryPEMap = groupIndustryPEMap.get(group);
				if (industryPEMap != null) {
					for (String industry : industryPEMap.keySet()) {
						ArrayList<Integer> peList = industryPEMap.get(industry);
						if (peList != null && peList.size() > 0) {
							int midPE = peList.size() / 2;
							if (midPE == 0) {
								midPE = 1;
							}

							GroupIndustryPEAvgBean gipaBean = new GroupIndustryPEAvgBean();
							gipaBean.setGroup(group);
							gipaBean.setIndustry(industry);
							gipaBean.setPe(peList.get(midPE - 1));

							gipaList.add(gipaBean);
						}
					}
				}
			}
		}

		if (gipaList != null && gipaList.size() > 0) {
			SMBroker.updateGroupAndIndustryPEMids(con, gipaList);
		}

		SM_Utilities.log("Update_GroupIndustryMidPE | Update | End");

	}
}
