
ECHO To prevent needing a restart, this script uses the
ECHO current user to run the service. You will need to
ECHO enter your NT password below. Careful: your password
ECHO will be displayed in cleartext.
ECHO
ECHO "Logging in user for service: %USERDOMAIN%\%USERNAME%"
ECHO
SET /P PASSWORD=Password:

cd "%~dp0\.."
python build.py
if errorlevel 1 exit /b %ERRORLEVEL%
cd dist

python bin\omero admin stop
REM Errors ok

set OMERO_CONFIG=quickstart
dropdb -U postgres %OMERO_CONFIG%
if errorlevel 1 exit /b %ERRORLEVEL%

createdb -O omero -U postgres %OMERO_CONFIG%
if errorlevel 1 exit /b %ERRORLEVEL%

createlang -U postgres plpgsql %OMERO_CONFIG%
if errorlevel 1 exit /b %ERRORLEVEL%

python bin\omero db script OMERO4.1 0 ome
if errorlevel 1 exit /b %ERRORLEVEL%

psql -U omero -f OMERO4.1__0.sql %OMERO_CONFIG%
if errorlevel 1 exit /b %ERRORLEVEL%

set PYTHONPATH=%PYTHONPATH%;lib\python
python lib\python\omero\install\win_set_path.py
if errorlevel 1 exit /b %ERRORLEVEL%

REM Required because of environment-less service
python bin\omero config def %OMERO_CONFIG%
if errorlevel 1 exit /b %ERRORLEVEL%

mkdir data
if errorlevel 1 exit /b %ERRORLEVEL%

python bin\omero config set omero.data.dir %CD%\data
if errorlevel 1 exit /b %ERRORLEVEL%

python bin\omero config set omero.windows.user %USERDOMAIN%\%USERNAME%
if errorlevel 1 exit /b %ERRORLEVEL%

python bin\omero config set omero.windows.pass "%PASSWORD%"
if errorlevel 1 exit /b %ERRORLEVEL%

python bin\omero config set omero.db.user postgres
if errorlevel 1 exit /b %ERRORLEVEL%

python bin\omero config set omero.db.name %OMERO_CONFIG%
if errorlevel 1 exit /b %ERRORLEVEL%

python bin\omero admin start
if errorlevel 1 exit /b %ERRORLEVEL%

