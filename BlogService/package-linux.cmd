@echo off
setlocal
set "JAVA_HOME=E:\environment\Java\JDK\jdk21"
set "PATH=%JAVA_HOME%\bin;%PATH%"
set "MVN=E:\java\maven\apache-maven-3.6.3\bin\mvn.cmd"
set "SETTINGS=E:\java\maven\apache-maven-3.6.3\conf\settings.xml"
cd /d "%~dp0"

echo JAVA_HOME=%JAVA_HOME%
echo Packaging Linux x86_64 jar (natives-linux, skip tests)

call "%MVN%" -s "%SETTINGS%" -B -DskipTests package -P-natives-windows,natives-linux
if errorlevel 1 exit /b 1
if not exist "target\blog-service.jar" (
  echo Maven succeeded but target\blog-service.jar is missing
  exit /b 1
)

if not exist "release" mkdir release
copy /y "target\blog-service.jar" "release\blog-service-linux.jar" >nul

for /f %%i in ('powershell -NoProfile -Command "[TimeZoneInfo]::ConvertTimeBySystemTimeZoneId((Get-Date),'China Standard Time').ToString('yyyyMMdd-HHmmss')"') do set "STAMP=%%i"
copy /y "target\blog-service.jar" "release\blog-service-linux-%STAMP%.jar" >nul

echo Linux jar ready:
echo   %CD%\release\blog-service-linux.jar
echo   %CD%\release\blog-service-linux-%STAMP%.jar
