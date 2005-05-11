@ECHO OFF
if %1a==a goto usage
if "%MAVEN_HOME%"=="" goto MHusage
set REPO_DIR=%1
:start
shift
if %1a==a goto parsed
set REPO_DIR=%REPO_DIR% %1
goto start
:parsed
echo copying to repository %REPO_DIR%
REM this warns but works on WinNT+, should work on Win9x too
if not exist "%REPO_DIR%\nul" mkdir "%REPO_DIR%"
if not exist "%REPO_DIR%\hibernate\nul" mkdir "%REPO_DIR%\hibernate"
if not exist "%REPO_DIR%\hibernate\jars\nul" mkdir "%REPO_DIR%\dom4j\jars"
if not exist "%REPO_DIR%\hibernate\jars\asm-hibernate.jar" copy "asm-hibernate.jar" "%REPO_DIR%\hibernate\jars"
if not exist "%REPO_DIR%\nul" mkdir "%REPO_DIR%"
if not exist "%REPO_DIR%\hibernate\nul" mkdir "%REPO_DIR%\hibernate"
if not exist "%REPO_DIR%\hibernate\jars\nul" mkdir "%REPO_DIR%\dom4j\jars"
if not exist "%REPO_DIR%\hibernate\jars\jta-hibernate.jar" copy "jta-hibernate.jar" "%REPO_DIR%\hibernate\jars"
