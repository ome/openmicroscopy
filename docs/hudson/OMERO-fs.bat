python build.py clean
if errorlevel 1 goto ERROR
python build.py
if errorlevel 1 goto ERROR
python build.py -f components\tools\OmeroFS\build.xml -Dtest.with.fail=true test
if errorlevel 1 goto ERROR
python build.py -f components\tools\OmeroFS\build.xml -Dtest.with.fail=true integration
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

exit /b 0
:ERROR
  echo Failed %ERRORLEVEL%
  exit /b %ERRORLEVEL%
