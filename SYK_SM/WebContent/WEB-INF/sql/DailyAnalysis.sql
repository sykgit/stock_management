----- Selecting all values in Call_To_Make
select * from A_CALL_TO_MAKE where scrip_code=534816 order by trade_date desc;
----- ==============================================================================================================================

-- Analysis | Just checking Call_To_Make
select * from
(select V1.*,ROUND(V1.PERCENT_CHANGE*V1.AVG_TRADES,0) SHARE_WEIGHT from
	(select C.*,ROUND(((c.sum_of_all_days*100)/c.days_close),2) PERCENT_CHANGE,ROUND(t.NO_OF_SHARES/t.NO_OF_TRADES,0) AVG_TRADES from A_CALL_TO_MAKE C,DATA_BSE_DAILY_TRADE T 
    where TRUNC(C.TRADE_DATE) = '15-FEB-16' and C.scrip_code in
		(select scrip_code from A_CALL_TO_MAKE where TRUNC(TRADE_DATE) = '15-FEB-16' and call_to_make='BUY' and scrip_code IN
			(	select scrip_code from A_CALL_TO_MAKE where TRUNC(TRADE_DATE) = '12-FEB-16' and call_to_make='SELL' and scrip_code IN
				(select scrip_code from A_CALL_TO_MAKE where TRUNC(TRADE_DATE) IN ('15-FEB-16','12-FEB-16') group by scrip_code having count(*)=2)
			)
		) and c.days_close > 5 and TRUNC(T.DATE_OF_TRADE) = '15-FEB-16'	and C.scrip_code=t.scrip_code and t.NO_OF_TRADES > 100
	) V1
) order by SHARE_WEIGHT desc;

-- With 30 Day Imporvement

select * from
(select V1.*,ROUND(V1.PERCENT_CHANGE*V1.AVG_TRADES,0) SHARE_WEIGHT from
	(select C.*,ROUND(((c.sum_of_all_days*100)/c.days_close),2) PERCENT_CHANGE,ROUND(t.NO_OF_SHARES/t.NO_OF_TRADES,0) AVG_TRADES from A_CALL_TO_MAKE C,DATA_BSE_DAILY_TRADE T 
    where TRUNC(C.TRADE_DATE) = '15-FEB-16' and C.scrip_code in
		(select scrip_code from A_CALL_TO_MAKE where TRUNC(TRADE_DATE) = '15-FEB-16' and call_to_make='BUY' and scrip_code IN
			(	select scrip_code from A_CALL_TO_MAKE where TRUNC(TRADE_DATE) = '12-FEB-16' and call_to_make='SELL' and scrip_code IN
				(select scrip_code from A_CALL_TO_MAKE where TRUNC(TRADE_DATE) IN ('15-FEB-16','12-FEB-16') group by scrip_code having count(*)=2)
			)
		) and c.days_close > 5 and TRUNC(T.DATE_OF_TRADE) = '15-FEB-16'	and C.scrip_code=t.scrip_code and t.NO_OF_TRADES > 100
	) V1
) 
where scrip_code IN (SELECT scrip_code FROM 
(select T.*,ROUND(((T.DAYS_CLOSE-T.DAYS_CLOSE_PREV)*100/T.DAYS_CLOSE_PREV),2) PERCENT_CHANGE_IN_MONTH FROM
  (select C1.*,C2.DAYS_CLOSE DAYS_CLOSE_PREV from A_CALL_TO_MAKE C1, 
  (select scrip_code,DAYS_CLOSE from A_CALL_TO_MAKE where TRADE_DATE IN (select max(TRADE_DATE) from A_CALL_TO_MAKE where TRADE_DATE < sysdate-30)) C2
  where C1.scrip_code = C2.SCRIP_CODE and C1.TRADE_DATE IN (select max(TRADE_DATE) from A_CALL_TO_MAKE )) T
)where PERCENT_CHANGE_IN_MONTH > 0)
order by SHARE_WEIGHT desc;

-- ============================================================================================================================

-- Analysis | Checking both Call_To_Make and computed call.
select * from
(select V1.*,ROUND(V1.PERCENT_CHANGE*V1.AVG_TRADES,0) SHARE_WEIGHT from
	(select C.*,ROUND(((c.sum_of_all_days*100)/c.days_close),2) PERCENT_CHANGE,ROUND(t.NO_OF_SHARES/t.NO_OF_TRADES,0) AVG_TRADES from A_CALL_TO_MAKE C,DATA_BSE_DAILY_TRADE T 
    where TRUNC(C.TRADE_DATE) = '15-FEB-16' and C.scrip_code in
		(select scrip_code from A_CALL_TO_MAKE where TRUNC(TRADE_DATE) = '15-FEB-16' and call_to_make='BUY' and COMPUTED_CALL='BUY' and scrip_code IN
			(	select scrip_code from A_CALL_TO_MAKE where TRUNC(TRADE_DATE) = '12-FEB-16' and call_to_make='SELL' and scrip_code IN
				(select scrip_code from A_CALL_TO_MAKE where TRUNC(TRADE_DATE) IN ('15-FEB-16','12-FEB-16') group by scrip_code having count(*)=2)
			)
		) and c.days_close > 5 and TRUNC(T.DATE_OF_TRADE) = '15-FEB-16'	and C.scrip_code=t.scrip_code and t.NO_OF_TRADES > 100
	) V1
) order by SHARE_WEIGHT desc;

-- With 30 Day Imporvement
select * from
(select V1.*,ROUND(V1.PERCENT_CHANGE*V1.AVG_TRADES,0) SHARE_WEIGHT from
	(select C.*,ROUND(((c.sum_of_all_days*100)/c.days_close),2) PERCENT_CHANGE,ROUND(t.NO_OF_SHARES/t.NO_OF_TRADES,0) AVG_TRADES from A_CALL_TO_MAKE C,DATA_BSE_DAILY_TRADE T 
    where TRUNC(C.TRADE_DATE) = '15-FEB-16' and C.scrip_code in
		(select scrip_code from A_CALL_TO_MAKE where TRUNC(TRADE_DATE) = '15-FEB-16' and call_to_make='BUY' and COMPUTED_CALL='BUY' and scrip_code IN
			(	select scrip_code from A_CALL_TO_MAKE where TRUNC(TRADE_DATE) = '12-FEB-16' and call_to_make='SELL' and scrip_code IN
				(select scrip_code from A_CALL_TO_MAKE where TRUNC(TRADE_DATE) IN ('15-FEB-16','12-FEB-16') group by scrip_code having count(*)=2)
			)
		) and c.days_close > 5 and TRUNC(T.DATE_OF_TRADE) = '15-FEB-16'	and C.scrip_code=t.scrip_code and t.NO_OF_TRADES > 100
	) V1
) where scrip_code IN (SELECT scrip_code FROM 
(select T.*,ROUND(((T.DAYS_CLOSE-T.DAYS_CLOSE_PREV)*100/T.DAYS_CLOSE_PREV),2) PERCENT_CHANGE_IN_MONTH FROM
  (select C1.*,C2.DAYS_CLOSE DAYS_CLOSE_PREV from A_CALL_TO_MAKE C1, 
  (select scrip_code,DAYS_CLOSE from A_CALL_TO_MAKE where TRADE_DATE IN (select max(TRADE_DATE) from A_CALL_TO_MAKE where TRADE_DATE < sysdate-30)) C2
  where C1.scrip_code = C2.SCRIP_CODE and C1.TRADE_DATE IN (select max(TRADE_DATE) from A_CALL_TO_MAKE )) T
)where PERCENT_CHANGE_IN_MONTH > 0)
order by SHARE_WEIGHT desc;

----- ==============================================================================================================================

-- 30 days improcement
SELECT scrip_code FROM 
(select T.*,ROUND(((T.DAYS_CLOSE-T.DAYS_CLOSE_PREV)*100/T.DAYS_CLOSE_PREV),2) PERCENT_CHANGE_IN_MONTH FROM
  (select C1.*,C2.DAYS_CLOSE DAYS_CLOSE_PREV from A_CALL_TO_MAKE C1, 
  (select scrip_code,DAYS_CLOSE from A_CALL_TO_MAKE where TRADE_DATE IN (select max(TRADE_DATE) from A_CALL_TO_MAKE where TRADE_DATE < sysdate-30)) C2
  where C1.scrip_code = C2.SCRIP_CODE and C1.TRADE_DATE IN (select max(TRADE_DATE) from A_CALL_TO_MAKE )) T
)where PERCENT_CHANGE_IN_MONTH > 0;
