----------------  REMOVING NON-EXISTANT COMPANIES ----------------------------------------------------------------------
select * from DATA_BSE_COMPANY where deleted_flag='Y';
--Analyse Deleted Companies. If Everything looks okay delete the companies

delete from DATA_BSE_DAILY_TRADE where SCRIP_CODE IN (select SCRIP_CODE from DATA_BSE_COMPANY where deleted_flag='Y');

delete from DATA_BSE_COMPANY_SHP where SCRIP_CODE IN (select SCRIP_CODE from DATA_BSE_COMPANY where deleted_flag='Y');

delete from DATA_BSE_COMPANY_RESULTS where SCRIP_CODE IN (select SCRIP_CODE from DATA_BSE_COMPANY where deleted_flag='Y');

delete from DATA_BSE_COMPANY where deleted_flag='Y';

----------------  REMOVING TRADES, ANALYSIS THAT ARE OLDER THAN 14 MONTHS  ------------------------------------------------------
select distinct(DATE_OF_TRADE) from DATA_BSE_DAILY_TRADE where DATE_OF_TRADE < sysdate-500;
--Analyse and delete

delete from DATA_BSE_DAILY_TRADE where DATE_OF_TRADE < sysdate-500;

delete from A_CALL_TO_MAKE where ANALYSIS_DATE < sysdate-500;

--Finally if everthing looks okay - commit

----------------  REMOVING COMPANY_SHP & RESULTS THAT ARE OLDER THAN 3 YEARS | ONCE A YEAR ACTIVITY ------------------
select BSE_QTR_NO from DATA_BSE_QTR_YEAR_MARKER where YEAR < 2013;

delete from DATA_BSE_COMPANY_SHP where BSE_QTR_NO IN (select BSE_QTR_NO from DATA_BSE_QTR_YEAR_MARKER where YEAR < 2014);

delete from DATA_BSE_COMPANY_RESULTS where BSE_QTR_NO IN (select BSE_QTR_NO from DATA_BSE_QTR_YEAR_MARKER where YEAR < 2014);



