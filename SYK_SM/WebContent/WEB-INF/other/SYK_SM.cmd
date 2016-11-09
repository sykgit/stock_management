cd D:\Workspace\SYK_SM\build\classes
d:
java -server -Xms2g -Xmx4g -XX:MaxDirectMemorySize=4096m -XX:PermSize=256m -XX:MaxPermSize=256m -XX:NewSize=1g -XX:MaxNewSize=1g -XX:+UseParNewGC -XX:MaxTenuringThreshold=2 -XX:SurvivorRatio=8 -XX:+UnlockDiagnosticVMOptions -XX:ParGCCardsPerStrideChunk=4096 -XX:+UseConcMarkSweepGC -XX:+ParallelRefProcEnabled -XX:+CMSClassUnloadingEnabled  -XX:CMSInitiatingOccupancyFraction=80 -XX:+UseCMSInitiatingOccupancyOnly   -XX:+AlwaysPreTouch -XX:-OmitStackTraceInFastThrow -cp .;D:\Workspace\SYK_SM\WebContent\WEB-INF\lib\ojdbc7.jar;D:\Workspace\SYK_SM\WebContent\WEB-INF\lib\jsoup-1.8.3.jar; Main
pause
