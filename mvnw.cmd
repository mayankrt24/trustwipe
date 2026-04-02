@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements.  See the NOTICE file
@REM distributed with this work for additional information
@REM regarding copyright ownership.  The ASF licenses this file
@REM to you under the Apache License, Version 2.0 (the
@REM "License"); you may not use this file except in compliance
@REM with the License.  You may obtain a copy of the License at
@REM
@REM    http://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing,
@REM software distributed under the License is distributed on an
@REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@REM KIND, either express or implied.  See the License for the
@REM specific language governing permissions and limitations
@REM under the License.
@REM ----------------------------------------------------------------------------

@REM ----------------------------------------------------------------------------
@REM Apache Maven Wrapper startup script for Windows, version 3.3.0
@REM
@REM Required ENV vars:
@REM JAVA_HOME - location of a JDK home dir
@REM
@REM Optional ENV vars
@REM MAVEN_BATCH_ECHO - set to 'on' to enable the echoing of the batch commands
@REM MAVEN_BATCH_PAUSE - set to 'on' to wait for a key stroke before ending
@REM MAVEN_OPTS - parameters passed to the Java VM when running Maven
@REM     e.g. to debug Maven itself, use
@REM set MAVEN_OPTS=-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000
@REM MAVEN_SKIP_RC - flag to disable loading of mavenrc files
@REM ----------------------------------------------------------------------------

@echo off
@REM set title of command prompt window
title %0
@REM enable echoing by setting MAVEN_BATCH_ECHO to 'on'
@if "%MAVEN_BATCH_ECHO%" == "on"  echo %MAVEN_BATCH_ECHO%

@REM set %HOME% to equivalent of $HOME
if "%HOME%" == "" (set "HOME=%USERPROFILE%")

@REM Execute a user defined script before this one
if not "%MAVEN_SKIP_RC%" == "" goto skipArgs
if exist "%PROGRAMDATA%\mavenrc.cmd" call "%PROGRAMDATA%\mavenrc.cmd" %*
if exist "%USERPROFILE%\mavenrc.cmd" call "%USERPROFILE%\mavenrc.cmd" %*
:skipArgs

setlocal

set "DIRNAME=%~dp0"
if "%DIRNAME%" == "" set "DIRNAME=.\"

set "WRAPPER_JAR=%DIRNAME%\.mvn\wrapper\maven-wrapper.jar"
set "WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain"

set "DOWNLOAD_URL="
if exist "%DIRNAME%\.mvn\wrapper\maven-wrapper.properties" (
  for /f "tokens=1,2 delims==" %%A in (%DIRNAME%\.mvn\wrapper\maven-wrapper.properties) do (
    if "%%A" == "wrapperUrl" set "DOWNLOAD_URL=%%B"
  )
)

@REM Extension to help our wrapper find the jar file
if not exist "%WRAPPER_JAR%" (
  if not "%DOWNLOAD_URL%" == "" (
    echo "Downloading maven-wrapper.jar from %DOWNLOAD_URL%"
    powershell -Command "Invoke-WebRequest -Uri '%DOWNLOAD_URL%' -OutFile '%WRAPPER_JAR%'"
  ) else (
    echo "maven-wrapper.jar not found at %WRAPPER_JAR%"
    exit /b 1
  )
)

@REM Find the project base dir, i.e. the directory that contains the folder ".mvn".
@REM Fallback to current working directory if not found.

set "MAVEN_PROJECTBASEDIR=%MAVEN_BASEDIR%"
if not "%MAVEN_PROJECTBASEDIR%" == "" goto endReadBaseDir

set "MAVEN_PROJECTBASEDIR=%DIRNAME%"
:findBaseDir
if exist "%MAVEN_PROJECTBASEDIR%\.mvn" goto endReadBaseDir
set "MAVEN_PROJECTBASEDIR=%MAVEN_PROJECTBASEDIR%\.."
if exist "%MAVEN_PROJECTBASEDIR%\.mvn" goto endReadBaseDir
if "%MAVEN_PROJECTBASEDIR%" == "\" goto fallbackBaseDir
if "%MAVEN_PROJECTBASEDIR%" == "." goto fallbackBaseDir
goto findBaseDir

:fallbackBaseDir
set "MAVEN_PROJECTBASEDIR=%DIRNAME%"

:endReadBaseDir

@REM Start Maven
set "CLASSWORLDS_JAR="
for /f "delims=" %%i in ('dir /b /s "%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar"') do set CLASSWORLDS_JAR=%%i

if not "%JAVA_HOME%" == "" goto gotJdkHome
for %%i in (java.exe) do set "JAVACMD=%%~$PATH:i"
goto checkJava

:gotJdkHome
set "JAVACMD=%JAVA_HOME%\bin\java.exe"

:checkJava
if exist "%JAVCMD%" goto runGui
echo.
echo Error: JAVA_HOME is set to an invalid directory.
echo JAVA_HOME = "%JAVA_HOME%"
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.
goto error

:runGui
@REM Use the wrapper to download and run Maven
"%JAVACMD%" %MAVEN_OPTS% %MAVEN_DEBUG_OPTS% -classpath "%WRAPPER_JAR%" %WRAPPER_LAUNCHER% %*
if ERRORLEVEL 1 goto error
goto end

:error
set ERROR_CODE=1

:end
@if "%MAVEN_BATCH_PAUSE%" == "on" pause

if "%MAVEN_TERMINATE_CMD%" == "on" exit %ERROR_CODE%

exit /b %ERROR_CODE%
