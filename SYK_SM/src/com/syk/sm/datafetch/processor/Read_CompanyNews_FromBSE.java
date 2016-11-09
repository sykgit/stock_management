package com.syk.sm.datafetch.processor;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.syk.sm.utility.SM_Utilities;
import com.syk.sm.utility.SM_Utilities.COMPANY_NEWS_TYPE;

public class Read_CompanyNews_FromBSE {

	public static HashMap<COMPANY_NEWS_TYPE, ArrayList<String>> read(int scripCode, Calendar dateFrom, Calendar dateTo, COMPANY_NEWS_TYPE newsType, String searchParam) throws Exception {
		SM_Utilities.log("Read_CompanyNews_FromBSE | Processing:" + scripCode + " | " + ((dateFrom != null) ? dateFrom.getTime() : null) + " | " + ((dateTo != null) ? dateTo.getTime() : null) + " | "
				+ newsType + " | " + searchParam);
		HashMap<COMPANY_NEWS_TYPE, ArrayList<String>> companyNews = new HashMap<COMPANY_NEWS_TYPE, ArrayList<String>>();
		String urlStr = SM_Utilities.getSMProperty("URL_CompanyNews_FromBSE");
		urlStr = urlStr.replaceAll("@SCRIP_CODE@", "" + scripCode);

		if (dateFrom == null) {
			dateTo = null;
		}

		urlStr = urlStr.replaceAll("@FROM_DATE@", SM_Utilities.formatCalDate(dateFrom, SM_Utilities.getSMProperty("companyNewsURL_DateFormat")));
		urlStr = urlStr.replaceAll("@TO_DATE@", SM_Utilities.formatCalDate(dateTo, SM_Utilities.getSMProperty("companyNewsURL_DateFormat")));
		if (newsType != null) {
			urlStr = urlStr.replaceAll("@CATEGORY_TYPE@", SM_Utilities.getStringInURLFormat(newsType.toString()));
		} else {
			urlStr = urlStr.replaceAll("@CATEGORY_TYPE@", "");
		}

		String html = SM_Utilities.getURLContentAsString(urlStr);

		if (html.indexOf("TTHeadergrey") > -1) {
			html = html.substring(html.indexOf("<span id=\"ctl00_ContentPlaceHolder1_lblann\">"), html.indexOf("</span>") + 7);
			Document doc = Jsoup.parse(html);
			Elements els = doc.getElementsByClass("TTHeadergrey");

			if (els != null) {
				for (int i = 0; i < els.size(); i = i + 4) {
					String type = els.get(i + 1).outerHtml();
					type = type.substring(0, type.length() - 1);
					type = (type.substring(type.lastIndexOf(">") + 1, type.lastIndexOf("<"))).trim();

					String message = els.get(i).outerHtml();
					message = message.substring(0, message.length() - 1);
					message = message.replaceAll("</a>", "");
					message = (message.substring(message.lastIndexOf(">") + 1, message.lastIndexOf("<"))).trim();

					if (newsType == null) {
						newsType = COMPANY_NEWS_TYPE.getEnum(type);
					}

					// Search Based on the search term
					if (searchParam != null && message != null && (message.toLowerCase()).indexOf(searchParam.toLowerCase()) < 0) {
						continue;
					}

					ArrayList<String> messages = null;
					if (companyNews.get(newsType) == null) {
						messages = new ArrayList<String>();
					} else {
						messages = companyNews.get(newsType);
					}

					messages.add(message);
					companyNews.put(newsType, messages);
					SM_Utilities.log("Read_CompanyNews_FromBSE | " + scripCode + " | " + newsType + " | " + message);
				}
			}
		}
		return companyNews;
	}

	public static boolean newsExist(int scripCode, Calendar dateFrom, Calendar dateTo, COMPANY_NEWS_TYPE newsType, String searchParam) throws Exception {
		SM_Utilities.log("Read_CompanyNews_FromBSE | newsExist | Processing:" + scripCode + " | " + ((dateFrom != null) ? dateFrom.getTime() : null) + " | "
				+ ((dateTo != null) ? dateTo.getTime() : null) + " | " + newsType + " | " + searchParam);
		String urlStr = SM_Utilities.getSMProperty("URL_CompanyNews_FromBSE");
		urlStr = urlStr.replaceAll("@SCRIP_CODE@", "" + scripCode);

		if (dateFrom == null) {
			dateTo = null;
		}

		urlStr = urlStr.replaceAll("@FROM_DATE@", SM_Utilities.formatCalDate(dateFrom, SM_Utilities.getSMProperty("companyNewsURL_DateFormat")));
		urlStr = urlStr.replaceAll("@TO_DATE@", SM_Utilities.formatCalDate(dateTo, SM_Utilities.getSMProperty("companyNewsURL_DateFormat")));
		if (newsType != null) {
			urlStr = urlStr.replaceAll("@CATEGORY_TYPE@", SM_Utilities.getStringInURLFormat(newsType.toString()));
		} else {
			urlStr = urlStr.replaceAll("@CATEGORY_TYPE@", "");
		}

		String html = SM_Utilities.getURLContentAsString(urlStr);

		if (html.indexOf("TTHeadergrey") > -1) {
			if (searchParam != null && html.indexOf(searchParam) < 0) {
				return false;
			} else {
				return true;
			}
		} else {
			return false;
		}
	}
}
