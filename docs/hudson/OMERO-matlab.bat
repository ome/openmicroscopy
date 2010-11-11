REM
REM Try Matlab
REM

python build.py clean
if errorlevel 1 goto ERROR
python build.py
if errorlevel 1 goto ERROR

set OMERO_HOME=%CD%\dist
call components\tools\OmeroM\test\omero_test
if errorlevel 1 goto ERROR

exit /b 0
:ERROR
  echo Failed %ERRORLEVEL%
  exit /b %ERRORLEVEL%
