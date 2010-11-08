set
sc stop OMERO.hudson

set OMERO_BUILD=r%SVN_REVISION%_w%BUILD_NUMBER%
set OMERO_CONFIG=%JOB_NAME%
set ICE_HOME=c:\Ice-3.3.1-VC90
set ICE_CONFIG=
set ROUTERPREFIX=2
set ROUTER=%ROUTERPREFIX%4064

REM workaround for IceSSL.dll issues
REM set PATH=%ICE_HOME%\bin\x64;%ICE_HOME%\bin\;%PATH%

REM see ticket1502
set OMERO_MASTER=hudson
set PASSWORD=hudson

cd trunk

set

REM Trying to delete data\.omero to detect orphaned server
REM If found, prep will be needed.
cmd /c exit 0
if exist dist (
        if exist dist\data\ (
                if exist dist\data\.omero rd /s /q dist\data\.omero
        )
)
if errorlevel 1 goto ERROR

python build.py clean
if errorlevel 1 goto ERROR

call docs\QUICKSTART.bat
if errorlevel 1 goto ERROR
cd dist

set ICE_CONFIG=%CD%\%OMERO_CONFIG%.cfg
echo omero.host=localhost >> %ICE_CONFIG%
echo omero.user=user >> %ICE_CONFIG%
echo omero.pass=ome >> %ICE_CONFIG%
echo omero.rootpass=ome >> %ICE_CONFIG%
echo omero.port=%ROUTER% >> %ICE_CONFIG%

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


REM
REM Try DropBox, Hudson will look for ERROR in the output log.
REM
dir ..\..\..\..\
copy "..\..\..\..\test11_R3D with spaces.dv" .
if errorlevel 1 goto ERROR
echo omero.fstest.srcFile=test11_R3D with spaces.dv >> etc\testdropbox.config
echo omero.fs.watchDir=TestDropBox >> etc\testdropbox.config
if errorlevel 1 goto ERROR

mkdir TestDropBox
if errorlevel 1 goto ERROR
python bin\omero admin ice server start TestDropBox
if errorlevel 1 goto ERROR

REM
REM Get configuration file to point to server
REM
rm -f ice.config
wget http://hudson.openmicroscopy.org.uk/job/OMERO-trunk/lastSuccessfulBuild/artifact/trunk/ice.config
export ICE_CONFIG=`pwd`/ice.config

REM
REM Try Matlab
REM
cd OMERO.
set OMERO_HOME=%CD%
call %WORKSPACE%\trunk\components\tools\OmeroM\test\omero_test
if errorlevel 1 goto ERROR

exit /b 0
:ERROR
  echo Failed %ERRORLEVEL%
  exit /b %ERRORLEVEL%

. $HOME/.bashrc
rm -rf OMERO.server*
cp /hudson/.hudson/jobs/OMERO-trunk/lastSuccessful/archive/trunk/target/OMERO.server-*.zip .
unzip -d OMERO.server OMERO.server-*.zip
rm -f test && ln -s OMERO.server/OMERO.server* test

rm -rf OMERO.cpp*
cp /hudson/.hudson/jobs/OMERO-trunk-cpp-so/lastSuccessful/archive/trunk/target/OMERO.cpp-*64dbg.zip .
unzip OMERO.cpp*.zip
rsync -va OMERO.cpp*/include test
rsync -va OMERO.cpp*/lib test


export TEST=$WORKSPACE/test
rm -rf examples && cp -r /hudson/.hudson/jobs/OMERO-trunk/workspace/trunk/examples .
rm -rf scons && cp -r /hudson/.hudson/jobs/OMERO-trunk/workspace/trunk/target/scons .
cd examples
python ../scons/scons.py builddir=$TEST run=1

