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
python bin\omero group add test
python bin\omero user add user Test User test
python bin\omero logout
if errorlevel 1 goto ERROR

REM
REM Import a file for testing
REM
python bin\omero -s localhost -p %ROUTER% -u user -w ome import C:\hudson\test11_R3D.dv
if errorlevel 1 goto ERROR


exit /b 0
:ERROR
  echo Failed %ERRORLEVEL%
  exit /b %ERRORLEVEL%
