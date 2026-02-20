@echo off
chcp 65001 > nul
cd /d %~dp0

set "JAR=target\backend-0.0.1-SNAPSHOT.jar"

if not exist "%JAR%" (
  echo [ERROR] 找不到 jar：%JAR%
  echo 你需要先在 backend 目录执行：mvn -DskipTests clean package
  pause
  exit /b 1
)

echo ========================================
echo  Campus Env Backend Starting...
echo ========================================

java -jar "%JAR%" --server.address=0.0.0.0 --server.port=8080

echo.
echo Backend stopped. Press any key to exit.
pause > nul
