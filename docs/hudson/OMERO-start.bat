echo on



REM
REM First do our best to clean up any left over servers
REM
sc stop OMERO.%OMERO_BRANCH%

REM Print out the environment
set

set OMERO_SRC_DIR=%cd%
set OMERO_DIST_DIR=%cd%\..\dist
set OMERO_MASTER=%OMERO_BRANCH%
set OMERO_CONFIG=%OMERO_BRANCH%
set ROUTERPREFIX=%OMERO_PREFIX%
set ROUTER=%ROUTERPREFIX%4064
set ICE_CONFIG=%cd%\%OMERO_CONFIG%.config
set OMERO_DATA=%cd%\target\datadir
set GROUP=hudson_group
set USER=hudson
set USERPASSWORD=ome

echo omero.host=localhost >> %ICE_CONFIG%
echo omero.user=%USER% >> %ICE_CONFIG%
echo omero.pass=%USERPASSWORD% >> %ICE_CONFIG%
echo omero.rootpass=ome >> %ICE_CONFIG%
echo omero.host=%OMERO_HOST% >> %ICE_CONFIG%
echo omero.port=%ROUTER% >> %ICE_CONFIG%
echo omero.prefix=%ROUTERPREFIX% >> %ICE_CONFIG%

REM Trying to delete data\.omero to detect orphaned server
REM If found, prep will be needed.
cmd /c exit 0

if not exist target mkdir target
if exist %OMERO_DATA% (
    if exist %OMERO_DATA%\.omero rd /s /q %OMERO_DATA%\.omero
)
if errorlevel 1 goto ERROR

call build clean
if errorlevel 1 goto ERROR

call docs\QUICKSTART.bat
if errorlevel 1 goto ERROR
cd %OMERO_DIST_DIR%

python bin\omero admin waitup

REM
REM Create a user
REM
python bin\omero -s localhost -p %ROUTER% -u root -w ome login
if errorlevel 1 goto ERROR
python bin\omero group add %GROUP% --perms=rwrw--
if errorlevel 1 goto ERROR
python bin\omero user add --admin %USER% Test User %GROUP% -P %USERPASSWORD$
if errorlevel 1 goto ERROR
python bin\omero logout
if errorlevel 1 goto ERROR

REM
REM Import a file for testing
REM
set FILE=very_small.d3d.dv
del %FILE%
wget http://hudson.openmicroscopy.org.uk/userContent/%FILE%
if errorlevel 1 goto ERROR
python bin\omero login -s localhost -p %ROUTER% -u %USER% -w %USERPASSWORD%
if errorlevel 1 goto ERROR
python bin\omero import %FILE%
if errorlevel 1 goto ERROR

REM
REM Try DropBox, Hudson will look for ERROR in the output log.
REM Must happen from the -start since it runs in the main
REM icegridnode process
REM
set FILE1=very_small.d3d.dv
set FILE2=very_small.d3d%%20with%%20spaces.dv
del %FILE1%
del %FILE2%
wget http://hudson.openmicroscopy.org.uk/userContent/%FILE1%
wget http://hudson.openmicroscopy.org.uk/userContent/%FILE2%
if errorlevel 1 goto ERROR
echo omero.fstest.srcFile=%FILE1%;%FILE2%;%FILE1% >> etc\testdropbox.config
echo omero.fs.watchDir=TestDropBox >> etc\testdropbox.config
echo omero.fstest.timeout=480 >> etc\testdropbox.config
if errorlevel 1 goto ERROR

mkdir TestDropBox
if errorlevel 1 goto ERROR
python bin\omero admin ports --skipcheck --prefix=%OMERO_PREFIX%
if errorlevel 1 goto ERROR
python bin\omero admin ice server start TestDropBox
if errorlevel 1 goto ERROR

REM
REM Write test file for OMERO-start jobs
REM
cd %OMERO_SRC_DIR%
if not exist target mkdir target
if not exist target\reports mkdir target\reports
set FILE=startup.xml
wget -O - http://hudson.openmicroscopy.org.uk/userContent/%FILE% > target\reports\%FILE%


exit /b 0
:ERROR
  echo Failed %ERRORLEVEL%
  exit /b %ERRORLEVEL%

