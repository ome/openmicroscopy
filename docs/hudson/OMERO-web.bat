REM
REM Run tests
REM
python build.py clean
if errorlevel 1 goto ERROR
python build.py
if errorlevel 1 goto ERROR
if %OMERO_PREFIX%==0 (
set webport=4064
) else (
set webport=%OMERO_PREFIX%4064
)

REM
REM Create a user
REM
python dist\bin\omero -s localhost -p %webport% -u root -w ome login
if errorlevel 1 goto ERROR
python dist\bin\omero group add web_group --perms=rwrw--
if errorlevel 1 goto ERROR
python dist\bin\omero user add web_user Web User web_group --userpassword abc
if errorlevel 1 goto ERROR
python dist\bin\omero logout
if errorlevel 1 goto ERROR

python dist\bin\omero config set omero.web.server_list ''[[""%OMERO_HOST%"", %webport%, ""omero""]]''
if errorlevel 1 goto ERROR
python dist\bin\omero config set omero.web.debug True
if errorlevel 1 goto ERROR
python dist\bin\omero web unittest --config=$ICE_CONFIG --test=webadmin
if errorlevel 1 goto ERROR

REM
REM Write test file for OMERO-web jobs
REM
cd ..
if not exist target mkdir target
if not exist target\reports mkdir target\reports
set FILE=web.xml
wget -O - http://hudson.openmicroscopy.org.uk/userContent/%FILE% > target\reports\%FILE%

exit /b 0
:ERROR
  echo Failed %ERRORLEVEL%
  exit /b %ERRORLEVEL%
