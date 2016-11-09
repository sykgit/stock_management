package com.syk.sm.datafetch.processor;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.syk.sm.utility.SM_Utilities;

public class Get_IntraEligibleStocks_FromZerodha {

	public static ArrayList<String> getScripIds() throws Exception {
		SM_Utilities.log("Get_IntraEligibleStocks_FromZerodha | getScripIds() | START");
		ArrayList<String> scripIdsList = new ArrayList<String>();
		String html = SM_Utilities.getURLContentAsString(SM_Utilities.getSMProperty("URL_IntraEligibleStocks_FromZerodha"));

		if (html.indexOf("td class=\"scrip\"") > -1) {
			Document doc = Jsoup.parse(html);
			Elements els = doc.getElementsByClass("scrip");
			if (els != null) {
				for (int i = 0; i < els.size(); i++) {
					String stock = els.get(i).html();
					if (stock != null && stock.indexOf(":EQ") > -1) {
						scripIdsList.add(stock.substring(0, stock.indexOf(":EQ")));
					}
				}
			}
		}
		SM_Utilities.log("Get_IntraEligibleStocks_FromZerodha | getScripIds() | END");
		return scripIdsList;
	}
}
