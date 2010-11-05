REM
REM Try Matlab
REM
set OMERO_HOME=%CD%\dist
build
call components\tools\OmeroM\test\omero_test
if errorlevel 1 goto ERROR

exit /b 0
:ERROR
  echo Failed %ERRORLEVEL%
  exit /b %ERRORLEVEL%
