/**
 * 
 */
package com.syk.sm.broker;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import com.syk.sm.bean.AnalysisBseCompCallToMake;
import com.syk.sm.bean.AnalysisCompanyResultsBean;
import com.syk.sm.bean.BseBhavCopyBean;
import com.syk.sm.bean.DataBseCompanyBean;
import com.syk.sm.bean.DataBseCompanyResultsBean;
import com.syk.sm.bean.DataBseQtrYearMarkerBean;
import com.syk.sm.bean.GroupIndustryPEAvgBean;
import com.syk.sm.bean.ShareHoldingPatternBean;
import com.syk.sm.utility.SM_Utilities;
import com.syk.sm.utility.SM_Utilities.CALL_TO_MAKE;

/**
 * @author skuppuraju
 *
 */
public class SMBroker {
	public static void saveMetaQYMarkers(ArrayList<DataBseQtrYearMarkerBean> quarterYearMarkerList, Connection con) throws SQLException {
		con.setAutoCommit(false);
		Statement stmt = con.createStatement();
		PreparedStatement updateQuery = con.prepareStatement("UPDATE DATA_BSE_QTR_YEAR_MARKER SET YEAR=?,QTR_IN_YEAR=?,QTR_DESC_STR=? WHERE BSE_QTR_NO=?");
		for (DataBseQtrYearMarkerBean qyMarker : quarterYearMarkerList) {
			updateQuery.setInt(1, qyMarker.getYear());
			updateQuery.setInt(2, qyMarker.getQtrInYear());
			updateQuery.setString(3, qyMarker.getQtrInYearStr());
			updateQuery.setDouble(4, qyMarker.getBseQtrNum());
			updateQuery.addBatch();
		}

		int results[] = updateQuery.executeBatch();

		if (results != null) {
			for (int itr = 0; itr < quarterYearMarkerList.size(); itr++) {
				if (results[itr] <= 0) {
					DataBseQtrYearMarkerBean qyMarker = quarterYearMarkerList.get(itr);
					stmt.addBatch("insert into DATA_BSE_QTR_YEAR_MARKER(BSE_QTR_NO,YEAR,QTR_IN_YEAR,QTR_DESC_STR) values(" + qyMarker.getBseQtrNum() + ", " + qyMarker.getYear() + ", "
							+ qyMarker.getQtrInYear() + ",'" + qyMarker.getQtrInYearStr() + "')");
				}

			}
		}
		stmt.executeBatch();

		con.commit();
		updateQuery.close();
		stmt.close();
		SM_Utilities.log("------------------------------------------------------------");
		SM_Utilities.log("--------- Table: DATA_BSE_QTR_YEAR_MARKER | UPDATED --------");
		SM_Utilities.log("------------------------------------------------------------");

	}

	public static void saveMetaBseCompanyInfo_FromBhavCopy(ArrayList<BseBhavCopyBean> bhavCopyBeansList, Connection con) throws SQLException {
		con.setAutoCommit(false);
		PreparedStatement updateQueryStmt = con.prepareStatement("UPDATE DATA_BSE_COMPANY SET company_name=?, scrip_group=?, scrip_type=?,deleted_flag='N' WHERE scrip_code=?");
		PreparedStatement insetQueryStmt = con.prepareStatement("insert into DATA_BSE_COMPANY(SCRIP_CODE,COMPANY_NAME,SCRIP_GROUP,SCRIP_TYPE) values (?,?,?,?)");
		for (BseBhavCopyBean bean : bhavCopyBeansList) {
			updateQueryStmt.setString(1, bean.getCompanyName());
			updateQueryStmt.setString(2, bean.getScripGroup());
			updateQueryStmt.setString(3, bean.getScripType());
			updateQueryStmt.setInt(4, bean.getScripCode());
			int noOfRowsUpdated = updateQueryStmt.executeUpdate();

			if (noOfRowsUpdated <= 0) {
				insetQueryStmt.setInt(1, bean.getScripCode());
				insetQueryStmt.setString(2, bean.getCompanyName());
				insetQueryStmt.setString(3, bean.getScripGroup());
				insetQueryStmt.setString(4, bean.getScripType());
				insetQueryStmt.executeUpdate();
			}
		}
		con.commit();
		updateQueryStmt.close();
		insetQueryStmt.close();
		SM_Utilities.log("------------------------------------------------------------------------");
		SM_Utilities.log("--------- Table: DATA_BSE_COMPANY | UPDATED from BHAV COPY --------");
		SM_Utilities.log("------------------------------------------------------------------------");
	}

	public static void saveBseDailyEquityTradeInfo_FromBhavCopy(ArrayList<BseBhavCopyBean> bhavCopyBeansList, String dateStr, Connection con) throws SQLException {
		Statement deleteStatement = con.createStatement();
		deleteStatement.execute("delete from DATA_BSE_DAILY_TRADE where to_char(date_of_trade, 'ddMMyy') = '" + dateStr + "'");
		con.commit();
		deleteStatement.close();

		con.setAutoCommit(false);
		Statement insetQueryStmt = con.createStatement();
		for (BseBhavCopyBean bean : bhavCopyBeansList) {
			if (bean.getScripType().equals("Q")) {
				insetQueryStmt.addBatch(
						"insert into DATA_BSE_DAILY_TRADE(SCRIP_CODE,DATE_OF_TRADE,DAYS_OPEN,DAYS_HIGH,DAYS_LOW,DAYS_CLOSE,DAYS_LAST,PREV_DAYS_CLOSE,NO_OF_TRADES,NO_OF_SHARES,NET_TURNOVER) values("
								+ bean.getScripCode() + ",to_date('" + dateStr + "','ddMMyy')," + bean.getDaysOpen() + "," + bean.getDaysHigh() + "," + bean.getDaysLow() + "," + bean.getDaysClose()
								+ "," + bean.getDaysLast() + "," + bean.getPrevDaysClose() + "," + bean.getNoOfTrades() + "," + bean.getNoOfShares() + "," + bean.getNetTurnOver() + ")");
			}
		}
		insetQueryStmt.executeBatch();
		con.commit();
		insetQueryStmt.close();

		SM_Utilities.log("------------------------------------------------------------------------");
		SM_Utilities.log("--------- Table: DATA_BSE_DAILY_TRADE | UPDATED from BHAV COPY --------");
		SM_Utilities.log("------------------------------------------------------------------------");
	}

	public static Calendar getMaxBhavDateFromDB(Connection con) throws SQLException {
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("select max(date_of_trade) MAX_TRADE_DATE from DATA_BSE_DAILY_TRADE");
		Calendar cal = Calendar.getInstance();

		if (rs.next()) {
			cal.setTime(new Date(rs.getDate("MAX_TRADE_DATE").getTime()));
		}
		rs.close();
		stmt.close();

		SM_Utilities.log("------------------------------------------------------------------------");
		SM_Utilities.log("--------- getMaxBhavDateFromDB |" + cal.getTime() + " --------");
		SM_Utilities.log("------------------------------------------------------------------------");

		return cal;
	}

	public static ArrayList<DataBseCompanyBean> getDataBseCompanyBeans(Connection con, boolean getDeleted) throws SQLException {
		ArrayList<DataBseCompanyBean> dataBseCompanyBeans = new ArrayList<DataBseCompanyBean>();
		Statement stmt = con.createStatement();
		String sqlQuery = "";
		if (getDeleted) {
			sqlQuery = "select * from DATA_BSE_COMPANY";
		} else {
			sqlQuery = "select * from DATA_BSE_COMPANY where DELETED_FLAG='N'";
		}
		ResultSet rs = stmt.executeQuery(sqlQuery);

		while (rs.next()) {
			DataBseCompanyBean dataBseCompanyBean = new DataBseCompanyBean();

			dataBseCompanyBean.setScripCode(rs.getInt("SCRIP_CODE"));
			dataBseCompanyBean.setCompanyName(rs.getString("COMPANY_NAME"));
			dataBseCompanyBean.setScripGroup(rs.getString("SCRIP_GROUP"));
			dataBseCompanyBean.setScripType(rs.getString("SCRIP_TYPE"));
			dataBseCompanyBean.setLastResultsFetchQtr(rs.getDouble("LAST_RESULTS_FETCH_QTR"));
			dataBseCompanyBean.setScripID(rs.getString("SCRIP_ID"));
			dataBseCompanyBean.setAssociatedIndex(rs.getString("ASSOCIATED_INDEX"));
			dataBseCompanyBean.setFaceValue(rs.getDouble("FACE_VALUE"));
			dataBseCompanyBean.setIndustry(rs.getString("INDUSTRY"));
			dataBseCompanyBean.setMarkForDeletion(rs.getString("DELETED_FLAG").equals("Y"));
			dataBseCompanyBean.setYearHigh(rs.getDouble("YEAR_HIGH"));
			if (rs.getDate("YEAR_HIGH_DATE") != null) {
				dataBseCompanyBean.setYearHighDate(SM_Utilities.formatSqlDate(rs.getDate("YEAR_HIGH_DATE"), SM_Utilities.getSMProperty("fiftyTwoWeekDateFormat")));
			}
			dataBseCompanyBean.setYearLow(rs.getDouble("YEAR_LOW"));
			if (rs.getDate("YEAR_LOW_DATE") != null) {
				dataBseCompanyBean.setYearLowDate(SM_Utilities.formatSqlDate(rs.getDate("YEAR_LOW_DATE"), SM_Utilities.getSMProperty("fiftyTwoWeekDateFormat")));
			}
			dataBseCompanyBean.setLastResultsFetchDate(SM_Utilities.getCalFromSqlDate(rs.getDate("LAST_RESULTS_FETCH_DATE")));
			dataBseCompanyBean.setLatestResultsNewsDate(SM_Utilities.getCalFromSqlDate(rs.getDate("LATEST_RESULTS_NEWS_DATE")));

			dataBseCompanyBean.setLastShpFetchQtr(rs.getDouble("LAST_SHP_FETCH_QTR"));
			dataBseCompanyBean.setLastShpFetchDate(SM_Utilities.getCalFromSqlDate(rs.getDate("LAST_SHP_FETCH_DATE")));
			dataBseCompanyBean.setLatestShpNewsDate(SM_Utilities.getCalFromSqlDate(rs.getDate("LATEST_SHP_NEWS_DATE")));

			dataBseCompanyBeans.add(dataBseCompanyBean);
		}
		rs.close();
		stmt.close();

		SM_Utilities.log("------------------------------------------------------------------------");
		SM_Utilities.log("--------- getDataBseCompanyBeans | Total Recs:" + dataBseCompanyBeans.size() + " --------");
		SM_Utilities.log("------------------------------------------------------------------------");

		return dataBseCompanyBeans;
	}

	public static void updateCompanyNames_DeleteNonExistentCompanies(ArrayList<DataBseCompanyBean> companyBeans, Connection con) throws SQLException {
		con.setAutoCommit(false);
		PreparedStatement updateQueryStmt = con.prepareStatement("update DATA_BSE_COMPANY set COMPANY_NAME=?,deleted_flag=? where scrip_code=?");
		for (DataBseCompanyBean companyBean : companyBeans) {
			updateQueryStmt.setString(1, companyBean.getCompanyName());
			updateQueryStmt.setString(2, companyBean.isMarkForDeletion() ? "Y" : "N");
			updateQueryStmt.setInt(3, companyBean.getScripCode());
			updateQueryStmt.addBatch();
		}
		updateQueryStmt.executeBatch();
		con.commit();
		updateQueryStmt.close();
		SM_Utilities.log("------------------------------------------------------------------------");
		SM_Utilities.log("--------- Table: DATA_BSE_COMPANY | updateCompanyNames_DeleteNonExistentCompanies --------");
		SM_Utilities.log("------------------------------------------------------------------------");

	}

	public static void updateScripIDIndexFaceValueIndustry(ArrayList<DataBseCompanyBean> companyBeans, Connection con) throws SQLException {
		ArrayList<Integer> companiesMarkedForDeletion = new ArrayList<Integer>();
		con.setAutoCommit(false);
		PreparedStatement updateQueryDeleteFlagStmt = con.prepareStatement("update DATA_BSE_COMPANY set deleted_flag='Y' where scrip_code=?");
		PreparedStatement updateStmt = con.prepareStatement("update DATA_BSE_COMPANY set SCRIP_ID=?,ASSOCIATED_INDEX=?,FACE_VALUE=?,INDUSTRY=? where scrip_code=?");
		for (DataBseCompanyBean companyBean : companyBeans) {
			if (companyBean.getFaceValue() == null) {
				updateQueryDeleteFlagStmt.setInt(1, companyBean.getScripCode());
				updateQueryDeleteFlagStmt.addBatch();
				companiesMarkedForDeletion.add(companyBean.getScripCode());
			} else {
				updateStmt.setString(1, companyBean.getScripID());
				updateStmt.setString(2, companyBean.getAssociatedIndex());
				updateStmt.setDouble(3, companyBean.getFaceValue());
				updateStmt.setString(4, companyBean.getIndustry());
				updateStmt.setInt(5, companyBean.getScripCode());
				updateStmt.addBatch();
			}
		}
		updateQueryDeleteFlagStmt.executeBatch();
		updateStmt.executeBatch();
		con.commit();
		updateQueryDeleteFlagStmt.close();
		updateStmt.close();
		SM_Utilities.log("------------------------------------------------------------------------");
		SM_Utilities.log("--------- Table: DATA_BSE_COMPANY | Updated ScripIDIndexFaceValueIndustry --------");
		SM_Utilities.log("--------- Table: DATA_BSE_COMPANY | DELETED Following Scrips --------");
		SM_Utilities.log(companiesMarkedForDeletion.toString());
		SM_Utilities.log("------------------------------------------------------------------------");

	}

	public static void update52WeekHighLows(ArrayList<DataBseCompanyBean> companyBeans, Connection con) throws SQLException, ParseException {
		con.setAutoCommit(false);
		PreparedStatement updateStmt = con.prepareStatement("update DATA_BSE_COMPANY set YEAR_HIGH=?,YEAR_HIGH_DATE=?,YEAR_LOW=?,YEAR_LOW_DATE=? where scrip_code=?");
		for (DataBseCompanyBean companyBean : companyBeans) {
			updateStmt.setDouble(1, companyBean.getYearHigh());
			updateStmt.setDate(2, SM_Utilities.getSqlDate(companyBean.getYearHighDate(), SM_Utilities.getSMProperty("fiftyTwoWeekDateFormat")));
			updateStmt.setDouble(3, companyBean.getYearLow());
			updateStmt.setDate(4, SM_Utilities.getSqlDate(companyBean.getYearLowDate(), SM_Utilities.getSMProperty("fiftyTwoWeekDateFormat")));
			updateStmt.setInt(5, companyBean.getScripCode());
			updateStmt.addBatch();
		}
		updateStmt.executeBatch();
		con.commit();
		updateStmt.close();
		SM_Utilities.log("------------------------------------------------------------------------");
		SM_Utilities.log("--------- Table: DATA_BSE_COMPANY | Updated 52WeekHighLows --------");
		SM_Utilities.log("------------------------------------------------------------------------");

	}

	public static double getBseQtrNo(int[] maxQtrYearToFetch, Connection con) throws Exception {
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("select BSE_QTR_NO from DATA_BSE_QTR_YEAR_MARKER where year=" + maxQtrYearToFetch[1] + " and qtr_in_year=" + maxQtrYearToFetch[0]);
		double qtrNo = 0;
		if (rs.next()) {
			qtrNo = rs.getDouble("BSE_QTR_NO");
		} else {
			rs.close();
			stmt.close();
			throw new Exception("DATA_BSE_QTR_YEAR_MARKER | Incorrect or Invalid");
		}
		rs.close();
		stmt.close();
		SM_Utilities.log("------------------------------------------------------------------------");
		SM_Utilities.log("--------- Table: DATA_BSE_QTR_YEAR_MARKER | getBseQtrNo | " + maxQtrYearToFetch[1] + "~" + maxQtrYearToFetch[0] + "==>" + qtrNo);
		SM_Utilities.log("------------------------------------------------------------------------");
		return qtrNo;
	}

	public static ArrayList<Double> getBseQtrNosToFetch(Double lastResultsFetchQtr, double maxFetchQtrNo, Connection con) throws SQLException {
		ArrayList<Double> qtrNosToFetch = new ArrayList<Double>();
		String query = "select BSE_QTR_NO from DATA_BSE_QTR_YEAR_MARKER where bse_qtr_no >= " + lastResultsFetchQtr + " and bse_qtr_no <= " + maxFetchQtrNo + " order by bse_qtr_no";
		if (lastResultsFetchQtr == null) {
			query = "select BSE_QTR_NO from DATA_BSE_QTR_YEAR_MARKER where bse_qtr_no <=" + maxFetchQtrNo + " order by bse_qtr_no";
		}

		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery(query);
		while (rs.next()) {
			qtrNosToFetch.add(rs.getDouble("BSE_QTR_NO"));
		}
		rs.close();
		stmt.close();
		SM_Utilities.log("------------------------------------------------------------------------");
		SM_Utilities.log("--------- Table: DATA_BSE_QTR_YEAR_MARKER | getBseQtrNosToFetch | " + lastResultsFetchQtr + "~" + maxFetchQtrNo + "==>" + qtrNosToFetch);
		SM_Utilities.log("------------------------------------------------------------------------");
		return qtrNosToFetch;
	}

	public static ArrayList<Double> getBseQtrNosList(Connection con) throws SQLException {
		ArrayList<Double> bseQtrNosList = new ArrayList<Double>();
		String query = "select BSE_QTR_NO from DATA_BSE_QTR_YEAR_MARKER order by bse_qtr_no";

		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery(query);
		while (rs.next()) {
			bseQtrNosList.add(rs.getDouble("BSE_QTR_NO"));
		}
		rs.close();
		stmt.close();
		SM_Utilities.log("------------------------------------------------------------------------");
		SM_Utilities.log("--------- Table: DATA_BSE_QTR_YEAR_MARKER | getBseQtrNosList | " + bseQtrNosList);
		SM_Utilities.log("------------------------------------------------------------------------");
		return bseQtrNosList;
	}

	public static HashMap<Integer, String> getScripCodeCompanyNameMap(Connection con) throws SQLException {
		HashMap<Integer, String> scripCodeCompanyNameMap = new HashMap<Integer, String>();
		String query = "select scrip_code,company_name from data_bse_company where deleted_flag='N'";

		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery(query);
		while (rs.next()) {
			scripCodeCompanyNameMap.put(rs.getInt("scrip_code"), rs.getString("company_name"));
		}
		rs.close();
		stmt.close();
		SM_Utilities.log("------------------------------------------------------------------------");
		SM_Utilities.log("--------- Table: DATA_BSE_COMPANY | getScripCodeCompanyNameMap ");
		SM_Utilities.log("------------------------------------------------------------------------");
		return scripCodeCompanyNameMap;
	}

	public static HashMap<Integer, String> getScripCodeScripIDMapFor_AB_Group(Connection con) throws SQLException {
		HashMap<Integer, String> scripCodeScripIDMap = new HashMap<Integer, String>();
		String query = "select scrip_code,scrip_id from data_bse_company where scrip_group in('A','B') and deleted_flag='N'";

		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery(query);
		while (rs.next()) {
			scripCodeScripIDMap.put(rs.getInt("scrip_code"), rs.getString("scrip_id"));
		}
		rs.close();
		stmt.close();
		SM_Utilities.log("------------------------------------------------------------------------");
		SM_Utilities.log("--------- Table: DATA_BSE_COMPANY | getScripCodeScripIDMapFor_AB_Group ");
		SM_Utilities.log("------------------------------------------------------------------------");
		return scripCodeScripIDMap;
	}

	public static void updateDataBseCompanyShp(ArrayList<ShareHoldingPatternBean> shpBeansList, Connection con) throws SQLException {
		if (shpBeansList != null && shpBeansList.size() > 0) {
			con.setAutoCommit(false);
			PreparedStatement updateStmt = con.prepareStatement(
					"update DATA_BSE_COMPANY_SHP set PROMOTER_SPLIT_PCT=?,PROMOTER_SPLIT_PLGD_PCT=?,PUBLIC_SPLIT_PCT=?,PUBLIC_SPLIT_PLGD_PCT=?,CUSTODIAN_SPLIT_PCT=?,CUSTODIAN_SPLIT_PLGD_PCT=? where scrip_code=? and bse_qtr_no=?");
			PreparedStatement insertStmt = con.prepareStatement(
					"insert into data_bse_company_shp(SCRIP_CODE,BSE_QTR_NO,PROMOTER_SPLIT_PCT,PROMOTER_SPLIT_PLGD_PCT,PUBLIC_SPLIT_PCT,PUBLIC_SPLIT_PLGD_PCT,CUSTODIAN_SPLIT_PCT,CUSTODIAN_SPLIT_PLGD_PCT) values(?,?,?,?,?,?,?,?)");

			for (ShareHoldingPatternBean shpBean : shpBeansList) {
				updateStmt.setDouble(1, shpBean.getPromoterSplitPercent());
				updateStmt.setDouble(2, shpBean.getPromoterSplitPledgedPercent());
				updateStmt.setDouble(3, shpBean.getPublicSplitPercent());
				updateStmt.setDouble(4, shpBean.getPublicSplitPledgedPercent());
				updateStmt.setDouble(5, shpBean.getCustodianSplitPercent());
				updateStmt.setDouble(6, shpBean.getCustodianSplitPledgedPercent());
				updateStmt.setInt(7, shpBean.getScripCode());
				updateStmt.setDouble(8, shpBean.getBseQtrNo());

				updateStmt.addBatch();
			}

			int updateResults[] = updateStmt.executeBatch();

			if (updateResults != null && updateResults.length > 0) {
				for (int i = 0; i < shpBeansList.size(); i++) {
					if (updateResults[i] <= 0) {
						insertStmt.setInt(1, shpBeansList.get(i).getScripCode());
						insertStmt.setDouble(2, shpBeansList.get(i).getBseQtrNo());
						insertStmt.setDouble(3, shpBeansList.get(i).getPromoterSplitPercent());
						insertStmt.setDouble(4, shpBeansList.get(i).getPromoterSplitPledgedPercent());
						insertStmt.setDouble(5, shpBeansList.get(i).getPublicSplitPercent());
						insertStmt.setDouble(6, shpBeansList.get(i).getPublicSplitPledgedPercent());
						insertStmt.setDouble(7, shpBeansList.get(i).getCustodianSplitPercent());
						insertStmt.setDouble(8, shpBeansList.get(i).getCustodianSplitPledgedPercent());

						insertStmt.addBatch();
					}
				}
				insertStmt.executeBatch();
			}

			con.commit();
			updateStmt.close();
			insertStmt.close();
			SM_Utilities.log("------------------------------------------------------------------------");
			SM_Utilities.log("--------- Table: DATA_BSE_COMPANY_SHP | UPDATED  --------");
			SM_Utilities.log("------------------------------------------------------------------------");
		} else {
			SM_Utilities.log("------------------------------------------------------------------------");
			SM_Utilities.log("--------- Table: DATA_BSE_COMPANY_SHP |  NO Results To UPDATE  --------");
			SM_Utilities.log("------------------------------------------------------------------------");
		}

	}

	public static void updateDataBseCompanyResults(ArrayList<DataBseCompanyResultsBean> resultList, Connection con) throws SQLException {
		if (resultList != null && resultList.size() > 0) {
			con.setAutoCommit(false);
			PreparedStatement updateStmt = con.prepareStatement(
					"update DATA_BSE_COMPANY_RESULTS set TOTAL_INCOME=?,OTHER_INCOME=?,EXPENDITURE=?,NET_PROFIT=?,EQUITY_CAPITAL=?,RESERVES=?,BSE_EPS=?,FACE_VALUE=? where scrip_code=? and bse_qtr_no=?");
			PreparedStatement insertStmt = con.prepareStatement(
					"insert into DATA_BSE_COMPANY_RESULTS(SCRIP_CODE,BSE_QTR_NO,TOTAL_INCOME,OTHER_INCOME,EXPENDITURE,NET_PROFIT,EQUITY_CAPITAL,RESERVES,BSE_EPS,FACE_VALUE) values(?,?,?,?,?,?,?,?,?,?)");

			for (DataBseCompanyResultsBean resultBean : resultList) {
				updateStmt.setDouble(1, resultBean.getTotalIncome());
				updateStmt.setDouble(2, resultBean.getOtherIncome());
				updateStmt.setDouble(3, resultBean.getExpenditure());
				updateStmt.setDouble(4, resultBean.getNetProfit());
				updateStmt.setDouble(5, resultBean.getEquityCapital());
				updateStmt.setDouble(6, resultBean.getReserves());
				updateStmt.setDouble(7, resultBean.getBseEps());
				updateStmt.setDouble(8, resultBean.getFaceValue());
				updateStmt.setInt(9, resultBean.getScripCode());
				updateStmt.setDouble(10, resultBean.getQtrNo());

				updateStmt.addBatch();
			}

			int updateResults[] = updateStmt.executeBatch();

			if (updateResults != null && updateResults.length > 0) {
				for (int i = 0; i < resultList.size(); i++) {
					if (updateResults[i] <= 0) {
						DataBseCompanyResultsBean resultBean = resultList.get(i);

						insertStmt.setInt(1, resultBean.getScripCode());
						insertStmt.setDouble(2, resultBean.getQtrNo());
						insertStmt.setDouble(3, resultBean.getTotalIncome());
						insertStmt.setDouble(4, resultBean.getOtherIncome());
						insertStmt.setDouble(5, resultBean.getExpenditure());
						insertStmt.setDouble(6, resultBean.getNetProfit());
						insertStmt.setDouble(7, resultBean.getEquityCapital());
						insertStmt.setDouble(8, resultBean.getReserves());
						insertStmt.setDouble(9, resultBean.getBseEps());
						insertStmt.setDouble(10, resultBean.getFaceValue());

						insertStmt.addBatch();
					}
				}
				insertStmt.executeBatch();
			}

			SM_Utilities.log("------------------------------------------------------------------------");
			SM_Utilities.log("--------- Table: DATA_BSE_COMPANY_RESULTS | UPDATED  --------");
			SM_Utilities.log("------------------------------------------------------------------------");

			con.commit();
			updateStmt.close();
			insertStmt.close();
		} else {
			SM_Utilities.log("------------------------------------------------------------------------");
			SM_Utilities.log("--------- Table: DATA_BSE_COMPANY_SHP |  NO Results To UPDATE  --------");
			SM_Utilities.log("------------------------------------------------------------------------");
		}
	}

	public static Calendar getMaxCallToMakeTradeDate(Connection con) throws SQLException {
		Calendar cl = null;
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("select max(TRADE_DATE) MAX_TRADE_DATE from A_CALL_TO_MAKE");
		while (rs.next()) {
			cl = SM_Utilities.getCalFromSqlDate(rs.getDate("MAX_TRADE_DATE"));
		}
		rs.close();
		stmt.close();
		SM_Utilities.log("------------------------------------------------------------------------");
		SM_Utilities.log("--------- Table: A_CALL_TO_MAKE | getMaxCallToMakeTradeDate ");
		SM_Utilities.log("------------------------------------------------------------------------");
		return cl;
	}

	public static void persistAnalysisBseCompCallToMakeBeans(ArrayList<AnalysisBseCompCallToMake> analysisCTMBeans, Connection con) throws SQLException, ParseException {
		if (analysisCTMBeans != null && analysisCTMBeans.size() > 0) {
			con.setAutoCommit(false);
			PreparedStatement updateStmt = con.prepareStatement(
					"update A_CALL_TO_MAKE set CALL_TO_MAKE=?,ONE_DAY_DIFF=?,two_day_sum_of_diff=?,three_day_sum_of_diff=?,sum_of_all_days=?,DAYS_CLOSE=? where scrip_code=? and TRUNC(TRADE_DATE)=?");

			for (AnalysisBseCompCallToMake analysisCTMBean : analysisCTMBeans) {
				updateStmt.setString(1, (analysisCTMBean.getCallToMake() == null) ? null : analysisCTMBean.getCallToMake().toString());
				updateStmt.setDouble(2, analysisCTMBean.getOneDayDiff());
				updateStmt.setDouble(3, analysisCTMBean.getTwoDaySumOfDiff());
				updateStmt.setDouble(4, analysisCTMBean.getThreeDaySumOfDiff());
				updateStmt.setDouble(5, analysisCTMBean.getSumOfAllDays());
				updateStmt.setDouble(6, analysisCTMBean.getDaysClosePrice());
				updateStmt.setInt(7, analysisCTMBean.getScripCode());
				updateStmt.setString(8, analysisCTMBean.getTradeDate());

				updateStmt.addBatch();
			}

			int updateResults[] = updateStmt.executeBatch();

			PreparedStatement insertStmt = con.prepareStatement(
					"insert into A_CALL_TO_MAKE(SCRIP_CODE,TRADE_DATE,CALL_TO_MAKE,ONE_DAY_DIFF,TWO_DAY_SUM_OF_DIFF,THREE_DAY_SUM_OF_DIFF,SUM_OF_ALL_DAYS,DAYS_CLOSE) values(?,?,?,?,?,?,?,?)");

			if (updateResults != null && updateResults.length > 0) {
				for (int i = 0; i < analysisCTMBeans.size(); i++) {
					if (updateResults[i] <= 0) {
						AnalysisBseCompCallToMake analysisCTMBean = analysisCTMBeans.get(i);
						insertStmt.setInt(1, analysisCTMBean.getScripCode());
						insertStmt.setDate(2, (analysisCTMBean.getTradeDate() == null) ? null : SM_Utilities.getSqlDate(analysisCTMBean.getTradeDate(), "dd-MMM-yyyy"));
						insertStmt.setString(3, (analysisCTMBean.getCallToMake() == null) ? null : analysisCTMBean.getCallToMake().toString());
						insertStmt.setDouble(4, analysisCTMBean.getOneDayDiff());
						insertStmt.setDouble(5, analysisCTMBean.getTwoDaySumOfDiff());
						insertStmt.setDouble(6, analysisCTMBean.getThreeDaySumOfDiff());
						insertStmt.setDouble(7, analysisCTMBean.getSumOfAllDays());
						insertStmt.setDouble(8, analysisCTMBean.getDaysClosePrice());

						insertStmt.addBatch();
					}
				}
				insertStmt.executeBatch();
			}

			updateStmt.close();
			insertStmt.close();

			SM_Utilities.log("------------------------------------------------------------------------");
			SM_Utilities.log("--------- Table: A_CALL_TO_MAKE | UPDATED  --------");
			SM_Utilities.log("------------------------------------------------------------------------");
			con.commit();
		}
	}

	public static void updateCompCallToMakeWithCurrentPrice(ArrayList<AnalysisBseCompCallToMake> analysisCTMBeans, Connection con) throws SQLException, ParseException {
		if (analysisCTMBeans != null && analysisCTMBeans.size() > 0) {
			con.setAutoCommit(false);
			PreparedStatement updateStmt = con.prepareStatement("update A_CALL_TO_MAKE set COMPUTED_CALL=?,CURRENT_PRICE=?,LATEST_PRICE_FETCH_TIME=sysdate where scrip_code=? and TRUNC(TRADE_DATE)=?");

			for (AnalysisBseCompCallToMake analysisCTMBean : analysisCTMBeans) {
				updateStmt.setString(1, (analysisCTMBean.getComputedCall() == null) ? null : analysisCTMBean.getComputedCall().toString());
				updateStmt.setDouble(2, analysisCTMBean.getCurrentPrice());
				updateStmt.setInt(3, analysisCTMBean.getScripCode());
				updateStmt.setString(4, analysisCTMBean.getTradeDate());

				updateStmt.addBatch();
			}

			updateStmt.executeBatch();

			updateStmt.close();

			SM_Utilities.log("------------------------------------------------------------------------");
			SM_Utilities.log("--------- Table: A_CALL_TO_MAKE | UPDATED  --------");
			SM_Utilities.log("------------------------------------------------------------------------");
			con.commit();
		}
	}

	public static void updateDaysOpenPrice(ArrayList<AnalysisBseCompCallToMake> analysisCTMBeans, Connection con) throws SQLException, ParseException {
		if (analysisCTMBeans != null && analysisCTMBeans.size() > 0) {
			con.setAutoCommit(false);
			PreparedStatement updateStmt = con.prepareStatement("update A_CALL_TO_MAKE set DAYS_OPEN=? where scrip_code=? and TRUNC(TRADE_DATE)=?");

			for (AnalysisBseCompCallToMake analysisCTMBean : analysisCTMBeans) {
				updateStmt.setDouble(1, analysisCTMBean.getDaysOpen());
				updateStmt.setInt(2, analysisCTMBean.getScripCode());
				updateStmt.setString(3, analysisCTMBean.getTradeDate());

				updateStmt.addBatch();
			}

			updateStmt.executeBatch();

			updateStmt.close();

			SM_Utilities.log("------------------------------------------------------------------------");
			SM_Utilities.log("--------- Table: A_CALL_TO_MAKE | UPDATED  --------");
			SM_Utilities.log("------------------------------------------------------------------------");
			con.commit();
		}
	}

	public static void updateCicuitPrices(ArrayList<AnalysisBseCompCallToMake> analysisCTMBeans, Connection con) throws SQLException, ParseException {
		if (analysisCTMBeans != null && analysisCTMBeans.size() > 0) {
			con.setAutoCommit(false);
			PreparedStatement updateStmt = con.prepareStatement("update A_CALL_TO_MAKE set UPPER_CKT=?,LOWER_CKT=? where scrip_code=? and TRUNC(TRADE_DATE)=?");

			for (AnalysisBseCompCallToMake analysisCTMBean : analysisCTMBeans) {
				if (analysisCTMBean.getLowerCkt() > 0) {
					updateStmt.setDouble(1, analysisCTMBean.getUpperCkt());
					updateStmt.setDouble(2, analysisCTMBean.getLowerCkt());
					updateStmt.setInt(3, analysisCTMBean.getScripCode());
					updateStmt.setString(4, analysisCTMBean.getTradeDate());

					updateStmt.addBatch();
				}
			}

			updateStmt.executeBatch();

			updateStmt.close();

			SM_Utilities.log("------------------------------------------------------------------------");
			SM_Utilities.log("--------- Table: A_CALL_TO_MAKE | UPDATED  --------");
			SM_Utilities.log("------------------------------------------------------------------------");
			con.commit();
		}
	}

	public static void updateRealTimePricesFromMoneyControl(ArrayList<AnalysisBseCompCallToMake> analysisCTMBeans, Connection con) throws SQLException, ParseException {
		if (analysisCTMBeans != null && analysisCTMBeans.size() > 0) {
			con.setAutoCommit(false);
			PreparedStatement updateStmt = con
					.prepareStatement("update A_CALL_TO_MAKE set COMPUTED_CALL=?,CURRENT_PRICE=?,UPPER_CKT=?,LOWER_CKT=?,CURRENT_VOLUME=?,TOTAL_BUY_QTY=?,TOTAL_SELL_QTY=?,DAYS_OPEN=?"
							+ ",TOP_BUY_QTY=?,TOP_BUY_RATE=?,TOP_SELL_QTY=?,TOP_SELL_RATE=?,LATEST_PRICE_FETCH_TIME=SYSDATE,TOP5_BUY_QTY=?,TOP5_SELL_QTY=? where scrip_code=? and TRUNC(TRADE_DATE)=?");

			for (AnalysisBseCompCallToMake analysisCTMBean : analysisCTMBeans) {
				if (analysisCTMBean.getLowerCkt() > 0) {
					updateStmt.setString(1, (analysisCTMBean.getComputedCall() == null) ? null : analysisCTMBean.getComputedCall().toString());
					updateStmt.setDouble(2, analysisCTMBean.getCurrentPrice());
					updateStmt.setDouble(3, analysisCTMBean.getUpperCkt());
					updateStmt.setDouble(4, analysisCTMBean.getLowerCkt());
					updateStmt.setLong(5, analysisCTMBean.getCurrentVolume());
					updateStmt.setLong(6, analysisCTMBean.getTotBuyQty());
					updateStmt.setLong(7, analysisCTMBean.getTotSellQty());
					updateStmt.setDouble(8, analysisCTMBean.getDaysOpen());
					updateStmt.setLong(9, analysisCTMBean.getTopBuyQty());
					updateStmt.setDouble(10, analysisCTMBean.getTopBuyRate());
					updateStmt.setLong(11, analysisCTMBean.getTopSellQty());
					updateStmt.setDouble(12, analysisCTMBean.getTopSellRate());
					updateStmt.setLong(13, analysisCTMBean.getTop5BuyQty());
					updateStmt.setLong(14, analysisCTMBean.getTop5SellQty());

					updateStmt.setInt(15, analysisCTMBean.getScripCode());
					updateStmt.setString(16, analysisCTMBean.getTradeDate());

					updateStmt.addBatch();
				}
			}

			updateStmt.executeBatch();

			updateStmt.close();

			SM_Utilities.log("------------------------------------------------------------------------");
			SM_Utilities.log("--------- Table: A_CALL_TO_MAKE | UPDATED  --------");
			SM_Utilities.log("------------------------------------------------------------------------");
			con.commit();
		}
	}

	public static void updateRealTimeDataFromNSE(ArrayList<AnalysisBseCompCallToMake> analysisCTMBeans, Connection con) throws SQLException {
		if (analysisCTMBeans != null && analysisCTMBeans.size() > 0) {
			con.setAutoCommit(false);
			PreparedStatement updateStmt = con.prepareStatement(
					"update A_CALL_TO_MAKE set N_CURR_PRICE=?,N_CURRENT_VOLUME=?,N_TOTAL_BUY_QTY=?,N_TOTAL_SELL_QTY=?,N_TOP5_BUY_QTY=?,N_TOP5_SELL_QTY=? where scrip_code=? and TRUNC(TRADE_DATE)=?");

			for (AnalysisBseCompCallToMake analysisCTMBean : analysisCTMBeans) {
				if (analysisCTMBean.getnCurrVolume() > 0) {
					updateStmt.setDouble(1, analysisCTMBean.getnCurrPrice());
					updateStmt.setLong(2, analysisCTMBean.getnCurrVolume());
					updateStmt.setLong(3, analysisCTMBean.getnTotalBuyQty());
					updateStmt.setLong(4, analysisCTMBean.getnTotalSellQty());
					updateStmt.setLong(5, analysisCTMBean.getnTop5BuyQty());
					updateStmt.setLong(6, analysisCTMBean.getnTop5SellQty());

					updateStmt.setInt(7, analysisCTMBean.getScripCode());
					updateStmt.setString(8, analysisCTMBean.getTradeDate());

					updateStmt.addBatch();
				}
			}

			updateStmt.executeBatch();

			updateStmt.close();

			SM_Utilities.log("------------------------------------------------------------------------");
			SM_Utilities.log("--------- Table: A_CALL_TO_MAKE | UPDATED  --------");
			SM_Utilities.log("------------------------------------------------------------------------");
			con.commit();
		}

	}

	public static void updateRealTimePricesFromGoogle(ArrayList<AnalysisBseCompCallToMake> analysisCTMBeans, Connection con) throws SQLException, ParseException {
		if (analysisCTMBeans != null && analysisCTMBeans.size() > 0) {
			con.setAutoCommit(false);
			PreparedStatement updateStmt = con
					.prepareStatement("update A_CALL_TO_MAKE set CURRENT_PRICE=?,CURRENT_VOLUME=?,DAYS_OPEN=?,LATEST_PRICE_FETCH_TIME=SYSDATE where scrip_code=? and TRUNC(TRADE_DATE)=?");

			for (AnalysisBseCompCallToMake analysisCTMBean : analysisCTMBeans) {
				if (analysisCTMBean.getLowerCkt() > 0) {
					updateStmt.setDouble(1, analysisCTMBean.getCurrentPrice());
					updateStmt.setLong(2, analysisCTMBean.getCurrentVolume());
					updateStmt.setDouble(3, analysisCTMBean.getDaysOpen());

					updateStmt.setInt(4, analysisCTMBean.getScripCode());
					updateStmt.setString(5, analysisCTMBean.getTradeDate());

					updateStmt.addBatch();
				}
			}

			updateStmt.executeBatch();

			updateStmt.close();

			SM_Utilities.log("------------------------------------------------------------------------");
			SM_Utilities.log("--------- Table: A_CALL_TO_MAKE | UPDATED  --------");
			SM_Utilities.log("------------------------------------------------------------------------");
			con.commit();
		}
	}

	public static ArrayList<AnalysisBseCompCallToMake> getLatestAnalysisBseCompCallToMakeBeans(Connection con) throws SQLException {
		ArrayList<AnalysisBseCompCallToMake> analysisCTMBeans = new ArrayList<AnalysisBseCompCallToMake>();
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("select * from A_CALL_TO_MAKE where trade_date=(select max(trade_date) from A_CALL_TO_MAKE)");
		/**
		 * SCRIP_CODE
		 * TRADE_DATE
		 * CALL_TO_MAKE
		 * ONE_DAY_DIFF
		 * TWO_DAY_SUM_OF_DIFF
		 * THREE_DAY_SUM_OF_DIFF
		 * SUM_OF_ALL_DAYS
		 * DAYS_CLOSE
		 * COMPUTED_CALL
		 */
		while (rs.next()) {
			AnalysisBseCompCallToMake analysisCTMBean = new AnalysisBseCompCallToMake();
			analysisCTMBean.setScripCode(rs.getInt("SCRIP_CODE"));
			analysisCTMBean.setTradeDate(SM_Utilities.formatSqlDate(rs.getDate("TRADE_DATE"), "dd-MMM-yyyy"));
			String callToMake = rs.getString("CALL_TO_MAKE");
			analysisCTMBean.setCallToMake((callToMake != null && callToMake.trim().length() > 1) ? CALL_TO_MAKE.valueOf(callToMake) : null);
			analysisCTMBean.setOneDayDiff(rs.getDouble("ONE_DAY_DIFF"));
			analysisCTMBean.setTwoDaySumOfDiff(rs.getDouble("TWO_DAY_SUM_OF_DIFF"));
			analysisCTMBean.setThreeDaySumOfDiff(rs.getDouble("THREE_DAY_SUM_OF_DIFF"));
			analysisCTMBean.setSumOfAllDays(rs.getDouble("SUM_OF_ALL_DAYS"));
			analysisCTMBean.setDaysClosePrice(rs.getDouble("DAYS_CLOSE"));
			String computedCall = rs.getString("COMPUTED_CALL");
			analysisCTMBean.setComputedCall((computedCall != null && computedCall.trim().length() > 1) ? CALL_TO_MAKE.valueOf(computedCall) : null);
			analysisCTMBean.setCurrentPrice(rs.getDouble("CURRENT_PRICE"));
			analysisCTMBeans.add(analysisCTMBean);
		}

		rs.close();
		stmt.close();

		SM_Utilities.log("------------------------------------------------------------------------");
		SM_Utilities.log("--------- Table: A_CALL_TO_MAKE | getLatestAnalysisBseCompCallToMakeBeans  --------");
		SM_Utilities.log("------------------------------------------------------------------------");
		return analysisCTMBeans;
	}

	public static ArrayList<String> getDistinctTradingDatesDesc(Connection con) throws SQLException {
		ArrayList<String> dateOfTrades = new ArrayList<String>();

		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("select distinct(DATE_OF_TRADE) FROM DATA_BSE_DAILY_TRADE order by DATE_OF_TRADE desc");

		while (rs.next()) {
			dateOfTrades.add(SM_Utilities.formatSqlDate(rs.getDate("DATE_OF_TRADE"), "dd-MMM-yyyy").toUpperCase());
		}

		rs.close();
		stmt.close();

		SM_Utilities.log("------------------------------------------------------------------------");
		SM_Utilities.log("--------- Table: DATA_BSE_DAILY_TRADE | getDistinctTradingDatesDesc ");
		SM_Utilities.log("------------------------------------------------------------------------");
		return dateOfTrades;
	}

	public static HashMap<Integer, HashMap<String, Double>> getClosingPricesForGivenDates(ArrayList<String> tradingDates, Connection con) throws SQLException {
		HashMap<Integer, HashMap<String, Double>> scripToDateToClosePriceMap = new HashMap<Integer, HashMap<String, Double>>();
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery(
				"select SCRIP_CODE,DATE_OF_TRADE,DAYS_CLOSE from DATA_BSE_DAILY_TRADE where TRUNC(DATE_OF_TRADE) IN (" + SM_Utilities.quoteStringsInListForSql(tradingDates) + ") order by scrip_code");

		while (rs.next()) {
			int scripCode = rs.getInt("SCRIP_CODE");
			HashMap<String, Double> dateToClosePriceMap = scripToDateToClosePriceMap.get(scripCode);
			if (dateToClosePriceMap == null) {
				dateToClosePriceMap = new HashMap<String, Double>();
			}
			dateToClosePriceMap.put(SM_Utilities.formatSqlDate(rs.getDate("DATE_OF_TRADE"), "dd-MMM-yyyy").toUpperCase(), rs.getDouble("DAYS_CLOSE"));
			scripToDateToClosePriceMap.put(scripCode, dateToClosePriceMap);
		}
		rs.close();
		stmt.close();

		SM_Utilities.log("------------------------------------------------------------------------");
		SM_Utilities.log("--------- Table: DATA_BSE_DAILY_TRADE | getClosingPricesForGivenDates ");
		SM_Utilities.log("------------------------------------------------------------------------");

		return scripToDateToClosePriceMap;
	}

	public static void updateDataBseCompanyWithSHP_Info(ArrayList<DataBseCompanyBean> companyBeans, Connection con) throws SQLException {
		con.setAutoCommit(false);
		Statement stmt = con.createStatement();
		Statement stmt1 = con.createStatement();
		PreparedStatement ps = con.prepareStatement("update DATA_BSE_COMPANY set LATEST_SHP_NEWS_DATE=? where SCRIP_CODE=?");
		stmt.executeUpdate(
				"update DATA_BSE_COMPANY C set C.LAST_SHP_FETCH_DATE=sysdate,C.LAST_SHP_FETCH_QTR=(select max(BSE_QTR_NO) from DATA_BSE_COMPANY_SHP S where S.SCRIP_CODE=C.SCRIP_CODE) where C.DELETED_FLAG='N'");
		stmt1.executeUpdate("update data_bse_company set LATEST_SHP_NEWS_DATE=sysdate where LATEST_SHP_NEWS_DATE is null");

		for (DataBseCompanyBean compBean : companyBeans) {
			ps.setDate(1, SM_Utilities.getSqlDate(compBean.getLatestShpNewsDate()));
			ps.setInt(2, compBean.getScripCode());
			ps.addBatch();
		}

		ps.executeBatch();
		con.commit();
		ps.close();
		stmt.close();
		stmt1.close();
		SM_Utilities.log("------------------------------------------------------------------------");
		SM_Utilities.log("--------- Table: DATA_BSE_COMPANY | updateDataBseCompanyWithSHP_Info ");
		SM_Utilities.log("------------------------------------------------------------------------");

	}

	public static void updateDataBseCompanyWithResults_Info(ArrayList<DataBseCompanyBean> companyBeans, Connection con) throws SQLException {
		con.setAutoCommit(false);
		Statement stmt = con.createStatement();
		Statement stmt1 = con.createStatement();
		PreparedStatement ps = con.prepareStatement("update DATA_BSE_COMPANY set LATEST_RESULTS_NEWS_DATE=? where SCRIP_CODE=?");
		stmt.executeUpdate(
				"update DATA_BSE_COMPANY C set C.LAST_RESULTS_FETCH_DATE=sysdate,C.LAST_RESULTS_FETCH_QTR=(select max(BSE_QTR_NO) from DATA_BSE_COMPANY_RESULTS R where R.SCRIP_CODE=C.SCRIP_CODE) where C.DELETED_FLAG='N'");
		stmt1.executeUpdate("update data_bse_company set LATEST_RESULTS_NEWS_DATE=sysdate where LATEST_RESULTS_NEWS_DATE is null");
		for (DataBseCompanyBean compBean : companyBeans) {
			ps.setDate(1, SM_Utilities.getSqlDate(compBean.getLatestResultsNewsDate()));
			ps.setInt(2, compBean.getScripCode());
			ps.addBatch();
		}

		ps.executeBatch();
		con.commit();
		ps.close();
		stmt.close();
		stmt1.close();
		SM_Utilities.log("------------------------------------------------------------------------");
		SM_Utilities.log("--------- Table: DATA_BSE_COMPANY | updateDataBseCompanyWithResults_Info ");
		SM_Utilities.log("------------------------------------------------------------------------");

	}

	public static HashMap<Integer, ArrayList<DataBseCompanyResultsBean>> getCompanyResultsToAnalyseForTheQtr(double bseQtrNo, Connection con) throws SQLException {
		HashMap<Integer, ArrayList<DataBseCompanyResultsBean>> scripResultsMap = new HashMap<Integer, ArrayList<DataBseCompanyResultsBean>>();
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery(
				"select * from DATA_BSE_COMPANY_RESULTS where scrip_code in (select SCRIP_CODE from DATA_BSE_COMPANY_RESULTS where SCRIP_CODE IN(select SCRIP_CODE from DATA_BSE_COMPANY_RESULTS where BSE_QTR_NO = "
						+ bseQtrNo + ") and BSE_QTR_NO >= " + bseQtrNo + ") and BSE_QTR_NO >= " + bseQtrNo + "-6 order by scrip_code desc,bse_qtr_no desc");

		while (rs.next()) {
			int scripCode = rs.getInt("SCRIP_CODE");
			ArrayList<DataBseCompanyResultsBean> resultsList = scripResultsMap.get(scripCode);
			if (resultsList == null) {
				resultsList = new ArrayList<DataBseCompanyResultsBean>();
			}
			DataBseCompanyResultsBean resultBean = new DataBseCompanyResultsBean();
			resultBean.setScripCode(scripCode);
			resultBean.setQtrNo(rs.getDouble("BSE_QTR_NO"));
			resultBean.setTotalIncome(rs.getDouble("TOTAL_INCOME"));
			resultBean.setOtherIncome(rs.getDouble("OTHER_INCOME"));
			resultBean.setExpenditure(rs.getDouble("EXPENDITURE"));
			resultBean.setNetProfit(rs.getDouble("NET_PROFIT"));
			resultBean.setEquityCapital(rs.getDouble("EQUITY_CAPITAL"));
			resultBean.setReserves(rs.getDouble("RESERVES"));
			resultBean.setBseEps(rs.getDouble("BSE_EPS"));
			resultBean.setFaceValue(rs.getDouble("FACE_VALUE"));

			resultsList.add(resultBean);
			scripResultsMap.put(scripCode, resultsList);
		}

		SM_Utilities.log("------------------------------------------------------------------------");
		SM_Utilities.log("--------- Table: DATA_BSE_COMPANY_RESULTS | getCompanyResultsToAnalyseForTheQtr ");
		SM_Utilities.log("------------------------------------------------------------------------");

		return scripResultsMap;
	}

	public static void saveCompanyResultAnalysis(ArrayList<AnalysisCompanyResultsBean> resultAnalysisBeanList, double bseQtrNo, Connection con) throws SQLException {
		if (resultAnalysisBeanList != null && resultAnalysisBeanList.size() > 0) {
			con.setAutoCommit(false);
			Statement deleteStmt = con.createStatement();
			deleteStmt.executeUpdate("update A_RESULTS set deleted_flag='Y' where BSE_QTR_NO=" + bseQtrNo);

			PreparedStatement updateStmt = con.prepareStatement(
					"update A_RESULTS set deleted_flag='N',NET_PCT_CHANGE=?,NET_PROFIT_Q_TO_Q=?,NET_PROFIT_Q_ON_Q=?,NET_SALES_Q_TO_Q=?,NET_SALES_Q_ON_Q=?,RESERVES_CHNG=?,LAST_UPDATED_DATE=sysdate where scrip_code=? and BSE_QTR_NO=?");
			PreparedStatement insertStmt = con.prepareStatement(
					"insert into A_RESULTS(SCRIP_CODE,BSE_QTR_NO,NET_PCT_CHANGE,NET_PROFIT_Q_TO_Q,NET_PROFIT_Q_ON_Q,NET_SALES_Q_TO_Q,NET_SALES_Q_ON_Q,RESERVES_CHNG) values(?,?,?,?,?,?,?,?)");

			for (AnalysisCompanyResultsBean analysisCompResult : resultAnalysisBeanList) {
				updateStmt.setDouble(1, analysisCompResult.getNetPercentageChange());
				updateStmt.setDouble(2, analysisCompResult.getNetProfitQToQPctChange());
				updateStmt.setDouble(3, analysisCompResult.getNetProfitQOnQPctChange());
				updateStmt.setDouble(4, analysisCompResult.getNetSalesQToQPctChange());
				updateStmt.setDouble(5, analysisCompResult.getNetSalesQOnQPctChange());
				updateStmt.setDouble(6, analysisCompResult.getReservesPctChange());
				updateStmt.setInt(7, analysisCompResult.getScripCode());
				updateStmt.setDouble(8, bseQtrNo);

				updateStmt.addBatch();
			}

			int updateResults[] = updateStmt.executeBatch();

			if (updateResults != null && updateResults.length > 0) {
				for (int i = 0; i < resultAnalysisBeanList.size(); i++) {
					if (updateResults[i] <= 0) {
						AnalysisCompanyResultsBean analysisCompResult = resultAnalysisBeanList.get(i);

						insertStmt.setInt(1, analysisCompResult.getScripCode());
						insertStmt.setDouble(2, bseQtrNo);
						insertStmt.setDouble(3, analysisCompResult.getNetPercentageChange());
						insertStmt.setDouble(4, analysisCompResult.getNetProfitQToQPctChange());
						insertStmt.setDouble(5, analysisCompResult.getNetProfitQOnQPctChange());
						insertStmt.setDouble(6, analysisCompResult.getNetSalesQToQPctChange());
						insertStmt.setDouble(7, analysisCompResult.getNetSalesQOnQPctChange());
						insertStmt.setDouble(8, analysisCompResult.getReservesPctChange());

						insertStmt.addBatch();
					}
				}
				insertStmt.executeBatch();
			}

			deleteStmt.close();
			updateStmt.close();
			insertStmt.close();

			SM_Utilities.log("------------------------------------------------------------------------");
			SM_Utilities.log("--------- Table: A_RESULTS | UPDATED  --------");
			SM_Utilities.log("------------------------------------------------------------------------");
			con.commit();
		}
	}

	public static void updateIntraEligibleFlags(ArrayList<String> intraEligibleScripIds, Connection con) throws SQLException {
		if (intraEligibleScripIds != null && intraEligibleScripIds.size() > 0) {
			con.setAutoCommit(false);
			Statement updateStmtNo = con.createStatement();
			updateStmtNo.executeUpdate("UPDATE data_bse_company SET intra_eligible='NO'");
			updateStmtNo.close();

			PreparedStatement updateStmt = con.prepareStatement("UPDATE data_bse_company SET intra_eligible='YES' where SCRIP_ID=?");
			for (String scripId : intraEligibleScripIds) {
				updateStmt.setString(1, scripId);
				updateStmt.addBatch();
			}
			updateStmt.executeBatch();

			con.commit();
			updateStmt.close();

			SM_Utilities.log("------------------------------------------------------------------------");
			SM_Utilities.log("--------- Table: DATA_BSE_COMPANY | updateIntraEligibleFlags ");
			SM_Utilities.log("------------------------------------------------------------------------");
		}
	}

	public static void deleteIgnored_UC_Companies(Connection con) throws SQLException {
		Statement stmt = con.createStatement();
		stmt.execute("truncate table Z_IGNORED_UC_STOCKS");
		stmt.close();
		SM_Utilities.log("------------------------------------------------------------------------");
		SM_Utilities.log("--------- Table: Z_IGNORED_UC_STOCKS | Truncated ");
		SM_Utilities.log("------------------------------------------------------------------------");
	}

	public static HashMap<String, HashMap<String, ArrayList<Integer>>> getGroupAndIndustryPEMap(Connection con) throws SQLException {
		HashMap<String, HashMap<String, ArrayList<Integer>>> grpIndustryPEMap = new HashMap<String, HashMap<String, ArrayList<Integer>>>();
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("select scrip_group,industry,pe from D_LATEST_COMPANY_PE order by scrip_group,industry,pe");
		while (rs.next()) {
			String group = rs.getString("scrip_group");
			String industry = rs.getString("industry");
			int pe = rs.getInt("pe");

			if (group == null || industry == null) {
				continue;
			}

			HashMap<String, ArrayList<Integer>> industryPEMap = grpIndustryPEMap.get(group);
			if (industryPEMap == null) {
				industryPEMap = new HashMap<String, ArrayList<Integer>>();
			}

			ArrayList<Integer> peList = industryPEMap.get(industry);
			if (peList == null) {
				peList = new ArrayList<Integer>();
			}

			peList.add(pe);
			industryPEMap.put(industry, peList);
			grpIndustryPEMap.put(group, industryPEMap);
		}
		rs.close();
		stmt.close();
		SM_Utilities.log("------------------------------------------------------------------------");
		SM_Utilities.log("--------- View: D_LATEST_COMPANY_PE | getGroupAndIndustryPEMap ");
		SM_Utilities.log("------------------------------------------------------------------------");
		return grpIndustryPEMap;
	}

	public static void updateGroupAndIndustryPEMids(Connection con, ArrayList<GroupIndustryPEAvgBean> gipaList) throws SQLException {
		if (gipaList != null && gipaList.size() > 0) {
			Statement truncateStmt = con.createStatement();
			truncateStmt.execute("truncate table A_GRP_INDUSTRY_PE_MID");
			truncateStmt.close();

			con.setAutoCommit(false);
			PreparedStatement ps = con.prepareStatement("insert into A_GRP_INDUSTRY_PE_MID values(?,?,?)");

			for (GroupIndustryPEAvgBean gipaBean : gipaList) {
				if (gipaBean.getGroup() == null || gipaBean.getGroup().equals("")) {
					System.out.println(gipaBean);
				}

				if (gipaBean.getIndustry() == null || gipaBean.getIndustry().equals("")) {
					System.out.println(gipaBean);
				}

				ps.setString(1, gipaBean.getGroup());
				ps.setString(2, gipaBean.getIndustry());
				ps.setInt(3, gipaBean.getPe());
				ps.addBatch();
			}

			ps.executeBatch();
			con.commit();
			ps.close();
		}

		SM_Utilities.log("------------------------------------------------------------------------");
		SM_Utilities.log("--------- Table: A_GRP_INDUSTRY_PE_MID | Updated ");
		SM_Utilities.log("------------------------------------------------------------------------");
	}
	
	
	public static void updateZerodhaInstList(Connection con, ArrayList<GroupIndustryPEAvgBean> gipaList) throws SQLException {
	}
}
