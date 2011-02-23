echo on



REM
REM First do our best to clean up any left over servers
REM
sc stop OMERO.%OMERO_BRANCH%

REM Print out the environment
set

set OMERO_MASTER=%OMERO_BRANCH%
set OMERO_CONFIG=%OMERO_BRANCH%
set ROUTERPREFIX=%OMERO_PREFIX%
set ROUTER=%ROUTERPREFIX%4064
set ICE_CONFIG=%cd%\%OMERO_CONFIG%.config
set OMERO_DATA=%cd%\target\datadir

echo omero.host=localhost >> %ICE_CONFIG%
echo omero.user=hudson >> %ICE_CONFIG%
echo omero.pass=ome >> %ICE_CONFIG%
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
cd dist

python bin\omero admin waitup

REM
REM Create a user
REM
python bin\omero -s localhost -p %ROUTER% -u root -w ome login
if errorlevel 1 goto ERROR
python bin\omero group add hudson_group --perms=rwrw--
if errorlevel 1 goto ERROR
python bin\omero user add --admin hudson Test User hudson_group
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
python bin\omero login -s localhost -p %ROUTER% -u hudson -w ome
if errorlevel 1 goto ERROR
python bin\omero import %FILE%
if errorlevel 1 goto ERROR

REM
REM Try DropBox, Hudson will look for ERROR in the output log.
REM Must happen from the -start since it runs in the main
REM icegridnode process
REM
set FILE=very_small.d3d%%20with%%20spaces.dv
del %FILE%
wget http://hudson.openmicroscopy.org.uk/userContent/%FILE%
if errorlevel 1 goto ERROR
echo omero.fstest.srcFile=very_small.d3d with spaces.dv >> etc\testdropbox.config
echo omero.fs.watchDir=TestDropBox >> etc\testdropbox.config
if errorlevel 1 goto ERROR

mkdir TestDropBox
if errorlevel 1 goto ERROR
python bin\omero admin ports --prefix=%OMERO_PREFIX%
if errorlevel 1 goto ERROR
python bin\omero admin ice server start TestDropBox
if errorlevel 1 goto ERROR

REM
REM Write test file for OMERO-start jobs
REM
cd ..
if not exist target mkdir target
if not exist target\reports mkdir target\reports
set FILE=startup.xml
wget -O - http://hudson.openmicroscopy.org.uk/userContent/%FILE% > target\reports\%FILE%


exit /b 0
:ERROR
  echo Failed %ERRORLEVEL%
  exit /b %ERRORLEVEL%

