@REM ----------------------------------------------------------------------------
@REM Apache Maven Wrapper startup batch script, version 3.2.0
@REM ----------------------------------------------------------------------------

@echo off
setlocal

set MAVEN_WRAPPER_PROPERTIES=.mvn\wrapper\maven-wrapper.properties

@REM Read distributionUrl
for /f "tokens=2 delims==" %%a in ('findstr "distributionUrl" "%MAVEN_WRAPPER_PROPERTIES%"') do set DISTRIBUTION_URL=%%a

@REM Derive archive name and directory name
for %%F in ("%DISTRIBUTION_URL:/= %") do set DISTRIBUTION_FILE_NAME=%%~nxF
set DISTRIBUTION_DIR_NAME=%DISTRIBUTION_FILE_NAME:-bin.zip=%

set MAVEN_USER_HOME=%USERPROFILE%\.m2
set MAVEN_HOME=%MAVEN_USER_HOME%\wrapper\dists\%DISTRIBUTION_DIR_NAME%

if not exist "%MAVEN_HOME%" (
  echo Downloading Maven from %DISTRIBUTION_URL%
  mkdir "%MAVEN_USER_HOME%\wrapper\dists" 2>nul
  set TMP_ZIP=%MAVEN_USER_HOME%\wrapper\dists\%DISTRIBUTION_FILE_NAME%
  powershell -Command "Invoke-WebRequest -Uri '%DISTRIBUTION_URL%' -OutFile '%TMP_ZIP%'"
  powershell -Command "Expand-Archive -Path '%TMP_ZIP%' -DestinationPath '%MAVEN_USER_HOME%\wrapper\dists' -Force"
  del "%TMP_ZIP%"
)

@REM Find mvn.cmd
for /r "%MAVEN_USER_HOME%\wrapper\dists\%DISTRIBUTION_DIR_NAME%" %%F in (mvn.cmd) do set MVN_CMD=%%F

if not defined MVN_CMD (
  echo ERROR: Could not find mvn.cmd after extraction.
  exit /b 1
)

"%MVN_CMD%" %*
endlocal
