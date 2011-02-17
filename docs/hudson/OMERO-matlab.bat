REM
REM Try Matlab
REM

python build.py clean
if errorlevel 1 goto ERROR
python build.py
if errorlevel 1 goto ERROR

REM Running short Java tests to produce testng output
REM The hudson matrix configuration assumes that there
REM will be test output available. In the future, though
REM we may want to actually run tests in Matlab.
python build.py test-compile
if errorlevel 1 goto ERROR
python build.py -f components\tools\OmeroJava\build.xml -Dtest.with.fail=true test
if errorlevel 1 goto ERROR

set OMERO_HOME=%CD%\dist
call components\tools\OmeroM\test\omero_test
if errorlevel 1 goto ERROR

exit /b 0
:ERROR
  echo Failed %ERRORLEVEL%
  exit /b %ERRORLEVEL%
