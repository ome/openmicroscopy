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
set ICE_CONFIG=%WORKSPACE%\%OMERO_CONFIG%.config

echo omero.host=localhost >> %ICE_CONFIG%
echo omero.user=user >> %ICE_CONFIG%
echo omero.pass=ome >> %ICE_CONFIG%
echo omero.rootpass=ome >> %ICE_CONFIG%
echo omero.port=%ROUTER% >> %ICE_CONFIG%
echo omero.prefix=%ROUTERPREFIX% >> %ICE_CONFIG%

REM Trying to delete data\.omero to detect orphaned server
REM If found, prep will be needed.
cmd /c exit 0
if exist dist (
        if exist dist\data\ (
                if exist dist\data\.omero rd /s /q dist\data\.omero
        )
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

