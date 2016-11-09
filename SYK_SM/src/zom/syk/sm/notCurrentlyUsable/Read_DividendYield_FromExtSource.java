package zom.syk.sm.notCurrentlyUsable;

import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Scanner;

import com.syk.sm.bean.DataBseCompanyBean;
import com.syk.sm.broker.SMBroker;
import com.syk.sm.utility.SM_Utilities;

public class Read_DividendYield_FromExtSource {
	public static void main(String args[]) throws Exception {
		Class.forName("oracle.jdbc.driver.OracleDriver");
		Connection con = DriverManager.getConnection(SM_Utilities.getSMProperty("dbUrl"), SM_Utilities.getSMProperty("dbUser"), SM_Utilities.getSMProperty("dbPwd"));

		ArrayList<DataBseCompanyBean> companyBeans = SMBroker.getDataBseCompanyBeans(con, false);
		for (DataBseCompanyBean companyBean : companyBeans) {
			double dividendYield = 0.0;
			URL url = new URL("https://www.screener.in/api/company/" + companyBean.getScripID() + "/");
			Scanner scanner = new Scanner(url.openStream());
			String jsonContent = scanner.useDelimiter("\\A").next();
			scanner.close();

			if (jsonContent != null) {
				if (jsonContent.indexOf("\"dividend_yield\":") > -1) {
					jsonContent = jsonContent.substring(jsonContent.indexOf("\"dividend_yield\":") + 17);
					jsonContent = jsonContent.substring(0, jsonContent.indexOf(","));
					jsonContent = jsonContent.replaceAll(",", "");
					dividendYield = Double.valueOf(jsonContent);
				}
			}

			System.out.println(companyBean.getScripID() + " | Dividend Yield:" + dividendYield);
		}
		con.close();
	}
}
