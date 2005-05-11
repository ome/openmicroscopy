@REM ----------------------------------------------------------------------------
@REM Copyright 2001-2004 The Apache Software Foundation.
@REM 
@REM Licensed under the Apache License, Version 2.0 (the "License");
@REM you may not use this file except in compliance with the License.
@REM You may obtain a copy of the License at
@REM 
@REM      http://www.apache.org/licenses/LICENSE-2.0
@REM 
@REM Unless required by applicable law or agreed to in writing, software
@REM distributed under the License is distributed on an "AS IS" BASIS,
@REM WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@REM See the License for the specific language governing permissions and
@REM limitations under the License.
@REM ----------------------------------------------------------------------------
@REM 

@REM ----------------------------------------------------------------------------
@REM Maven Start Up Batch script
@REM
@REM Required ENV vars:
@REM JAVA_HOME - location of a JDK home dir
@REM MAVEN_HOME - location of maven's installed home dir
@REM
@REM Optional ENV vars
@REM MAVEN_HOME_LOCAL - may override default dir Maven writes work files
@REM MAVEN_BATCH_ECHO - set to 'on' to enable the echoing of the batch commands
@REM MAVEN_BATCH_PAUSE - set to 'on' to wait for a key stroke before ending
@REM MAVEN_OPTS - parameters passed to the Java VM when running Maven
@REM     e.g. to debug Maven itself, use
@REM set MAVEN_OPTS=-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000
@REM ----------------------------------------------------------------------------

@REM Begin all REM lines with '@' in case MAVEN_BATCH_ECHO is 'on'
@echo off
@REM enable echoing my setting MAVEN_BATCH_ECHO to 'on'
@if "%MAVEN_BATCH_ECHO%" == "on"  echo %MAVEN_BATCH_ECHO%

@REM Execute a user defined script before this one
if exist "%HOME%\mavenrc_pre.bat" call "%HOME%\mavenrc_pre.bat"

@REM set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" @setlocal

@REM ==== START VALIDATION ====
if not "%JAVA_HOME%" == "" goto OkJHome

echo.
echo ERROR: JAVA_HOME not found in your environment.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation
echo.
goto end

:OkJHome
if exist "%JAVA_HOME%\bin\java.exe" goto chkMHome

echo.
echo ERROR: JAVA_HOME is set to an invalid directory.
echo JAVA_HOME = %JAVA_HOME%
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation
echo.
goto end

:chkMHome
if not "%MAVEN_HOME%"=="" goto valMHome

echo.
echo ERROR: MAVEN_HOME not found in your environment.
echo Please set the MAVEN_HOME variable in your environment to match the
echo location of the Maven installation
echo.
goto end

:valMHome
if exist "%MAVEN_HOME%\bin\maven.bat" goto init

echo.
echo ERROR: MAVEN_HOME is set to an invalid directory.
echo MAVEN_HOME = %MAVEN_HOME%
echo Please set the MAVEN_HOME variable in your environment to match the
echo location of the Maven installation
echo.
goto end
@REM ==== END VALIDATION ====

:init
@REM Decide how to startup depending on the version of windows

@REM -- Win98ME
if NOT "%OS%"=="Windows_NT" goto Win9xArg

@REM -- 4NT shell
if "%eval[2+2]" == "4" goto 4NTArgs

@REM -- Regular WinNT shell
set MAVEN_CMD_LINE_ARGS=%*
goto endInit

@REM The 4NT Shell from jp software
:4NTArgs
set MAVEN_CMD_LINE_ARGS=%$
goto endInit

:Win9xArg
@REM Slurp the command line arguments.  This loop allows for an unlimited number
@REM of agruments (up to the command line limit, anyway).
set MAVEN_CMD_LINE_ARGS=
:Win9xApp
if %1a==a goto endInit
set MAVEN_CMD_LINE_ARGS=%MAVEN_CMD_LINE_ARGS% %1
shift
goto Win9xApp

@REM Reaching here means variables are defined and arguments have been captured
:endInit
if "%MAVEN_OPTS%"=="" SET MAVEN_OPTS="-Xmx256m"
SET MAVEN_JAVA_EXE="%JAVA_HOME%\bin\java.exe"
SET MAVEN_CLASSPATH="%MAVEN_HOME%\lib\forehead-1.0-beta-5.jar"
SET MAVEN_MAIN_CLASS="com.werken.forehead.Forehead"
SET MAVEN_ENDORSED="%JAVA_HOME%\lib\endorsed;%MAVEN_HOME%\lib\endorsed"
if not "%MAVEN_HOME_LOCAL%" == "" goto StartMHL

@REM Start MAVEN without MAVEN_HOME_LOCAL override
%MAVEN_JAVA_EXE% -Djavax.xml.parsers.DocumentBuilderFactory=org.apache.xerces.jaxp.DocumentBuilderFactoryImpl -Djavax.xml.parsers.SAXParserFactory=org.apache.xerces.jaxp.SAXParserFactoryImpl "-Dmaven.home=%MAVEN_HOME%" "-Dtools.jar=%JAVA_HOME%\lib\tools.jar" "-Dforehead.conf.file=%MAVEN_HOME%\bin\forehead.conf" -Djava.endorsed.dirs=%MAVEN_ENDORSED% %MAVEN_OPTS% -classpath %MAVEN_CLASSPATH% %MAVEN_MAIN_CLASS% %MAVEN_CMD_LINE_ARGS%
@REM %MAVEN_JAVA_EXE% -Dorg.xml.sax.driver=org.apache.xerces.parsers.SAXParser -Djavax.xml.parsers.SAXParserFactory=org.apache.xerces.jaxp.SAXParserFactoryImpl -Djavax.xml.parsers.DocumentBuilderFactory=org.apache.xerces.jaxp.DocumentBuilderFactoryImpl "-Dmaven.home=%MAVEN_HOME%" "-Dtools.jar=%JAVA_HOME%\lib\tools.jar" "-Dforehead.conf.file=%MAVEN_HOME%\bin\forehead.conf" -Djava.endorsed.dirs=%MAVEN_ENDORSED% %MAVEN_OPTS% -classpath %MAVEN_CLASSPATH% %MAVEN_MAIN_CLASS% %MAVEN_CMD_LINE_ARGS%
goto :end

@REM Start MAVEN with MAVEN_HOME_LOCAL override
:StartMHL
%MAVEN_JAVA_EXE% -Djavax.xml.parsers.DocumentBuilderFactory=org.apache.xerces.jaxp.DocumentBuilderFactoryImpl -Djavax.xml.parsers.SAXParserFactory=org.apache.xerces.jaxp.SAXParserFactoryImpl "-Dmaven.home=%MAVEN_HOME%" "-Dmaven.home.local=%MAVEN_HOME_LOCAL%" "-Dtools.jar=%JAVA_HOME%\lib\tools.jar" "-Dforehead.conf.file=%MAVEN_HOME%\bin\forehead.conf" -Djava.endorsed.dirs=%MAVEN_ENDORSED% %MAVEN_OPTS% -classpath %MAVEN_CLASSPATH% %MAVEN_MAIN_CLASS% %MAVEN_CMD_LINE_ARGS%
@REM %MAVEN_JAVA_EXE% -Dorg.xml.sax.driver=org.apache.xerces.parsers.SAXParser -Djavax.xml.parsers.SAXParserFactory=org.apache.xerces.jaxp.SAXParserFactoryImpl -Djavax.xml.parsers.DocumentBuilderFactory=org.apache.xerces.jaxp.DocumentBuilderFactoryImpl "-Dmaven.home=%MAVEN_HOME%" "-Dmaven.home.local=%MAVEN_HOME_LOCAL%" "-Dtools.jar=%JAVA_HOME%\lib\tools.jar" "-Dforehead.conf.file=%MAVEN_HOME%\bin\forehead.conf" -Djava.endorsed.dirs=%MAVEN_ENDORSED% %MAVEN_OPTS% -classpath %MAVEN_CLASSPATH% %MAVEN_MAIN_CLASS% %MAVEN_CMD_LINE_ARGS%

:end
@REM set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" goto endNT

@REM For old DOS remove the set variables from ENV - we assume they were not set
@REM before we started - at least we don't leave any baggage around
set MAVEN_JAVA_EXE=
set MAVEN_CLASSPATH=
set MAVEN_MAIN_CLASS=
set MAVEN_CMD_LINE_ARGS=
goto postExec

:endNT
@endlocal

:postExec
if exist "%HOME%\mavenrc_post.bat" call "%HOME%\mavenrc_post.bat"
@REM pause the batch file if MAVEN_BATCH_PAUSE is set to 'on'
if "%MAVEN_BATCH_PAUSE%" == "on" pause

