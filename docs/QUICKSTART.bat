@echo OFF
REM
REM QUICKSTART.bat is intended for getting developers
REM up and running quickly. You will need to have
REM passwordless login for the "omero" DB user configured
REM in postgres.
REM
REM For more information, see http://trac.openmicroscopy.org.uk/ome/wiki/OmeroContributing

echo.
echo -----------------------------------------------------
echo To prevent needing a restart, this script uses the
echo current user to run the service. You will need to
echo enter your NT password below. Careful: your password
echo will be displayed in cleartext.
echo -----------------------------------------------------
echo.
echo Logging in user for service: %USERDOMAIN%\%USERNAME%
echo.

REM Move to the directory above this script's directory
REM i.e. OMERO_HOME
cd "%~dp0\.."

if "x%PASSWORD%" == "x" (SET /P PASSWORD=Password:)
REM Other defaults
if "x%ROUTERPREFIX%" == "x" (SET ROUTERPREFIX="")
if "x%OMERO_DIST_DIR%" == "x" (SET OMERO_DIST_DIR="%cd%\dist")

if exist %OMERO_DIST_DIR% goto AlreadyBuilt
  echo Building...
  REM As of 5c6fc7313f129 it's no longer possible to pass
  REM dist.dir to build.py
  python -c "import sys, os; print 'dist.dir=%%s' %% os.environ['OMERO_DIST_DIR'].replace('\\','\\\\')" > etc\local.properties
  if errorlevel 1 goto ERROR

  python build.py
  if errorlevel 1 goto ERROR

  goto Ready
:AlreadyBuilt
  echo Server already built. To rebuild, use "build clean"
  goto Ready

:Ready
cd %OMERO_DIST_DIR%

echo Stopping server...
python bin\omero admin status && python bin\omero admin stop
if errorlevel 1 echo "Wasn't running?"

if "x%OMERO_CONFIG%" == "x" (set OMERO_CONFIG=quickstart)
echo Using OMERO_CONFIG=%OMERO_CONFIG%

echo Dropping %OMERO_CONFIG% db
dropdb -U postgres %OMERO_CONFIG%
if errorlevel 1 echo Didn't exist?

echo Creating omero database user
createuser -S -D -R -U postgres omero
if errorlevel 1 echo Already exists?

echo Creating %OMERO_CONFIG% db
createdb -E UTF8 -O omero -U postgres %OMERO_CONFIG%
if errorlevel 1 goto ERROR

echo Adding pgsql to db
createlang -U postgres plpgsql %OMERO_CONFIG%
if errorlevel 1 echo Already installed?

echo Creating latest DB script
python bin\omero db script -f %OMERO_CONFIG%.sql "" "" ome
if errorlevel 1 goto ERROR

echo Iniitializing DB
psql -U omero -f %OMERO_CONFIG%.sql %OMERO_CONFIG% 2>quickstart.err >quickstart.out
if errorlevel 1 goto ERROR

echo Setting PYTHONPATH
call bin\setpythonpath
if errorlevel 1 goto ERROR

echo Setting etc\grid directory paths to %CD%
python lib\python\omero\install\win_set_path.py
if errorlevel 1 goto ERROR

echo Setting etc\grid ports prefix to %ROUTERPREFIX%
python bin\omero admin ports --skipcheck --prefix=%ROUTERPREFIX%
if errorlevel 1 goto ERROR

REM Required because of environment-less service
echo Setting config
python bin\omero config def %OMERO_CONFIG%
if errorlevel 1 goto ERROR

if exist data echo Data directory already exists!
if not exist data (echo Creating data directory && mkdir data)
if errorlevel 1 goto ERROR

echo Configuring and creating data directory
if "x%OMERO_DATA%" == "x" (SET OMERO_DATA="%CD%\data")
python bin\omero config set omero.data.dir %OMERO_DATA%
if errorlevel 1 goto ERROR
mkdir %OMERO_DATA%
if errorlevel 1 goto ERROR

echo Configuring Windows user
python bin\omero config set omero.windows.user %USERDOMAIN%\%USERNAME%
if errorlevel 1 goto ERROR

echo Configuring password
python bin\omero config set omero.windows.pass "%PASSWORD%"
if errorlevel 1 goto ERROR

echo Configuring database user
python bin\omero config set omero.db.user omero
if errorlevel 1 goto ERROR

echo Configuring database name
python bin\omero config set omero.db.name %OMERO_CONFIG%
if errorlevel 1 goto ERROR

if "x%OMERO_MASTER%" == x (goto NoMaster)
    echo Copying master.cfg to %OMERO_MASTER%.cfg
    copy etc\master.cfg etc\%OMERO_MASTER%.cfg
    if errorlevel 1 goto ERROR
:NoMaster

echo Starting server
python bin\omero admin start
if errorlevel 1 goto ERROR


cd "%~dp0\.."
exit /b 0
:ERROR
  echo Failed %ERRORLEVEL%
  cd "%~dp0\.."
  exit /b %ERRORLEVEL%
