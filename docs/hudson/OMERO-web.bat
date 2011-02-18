REM
REM Run tests
REM
python build.py clean
if errorlevel 1 goto ERROR
python build.py
if errorlevel 1 goto ERROR
python dist\bin\omero web test webadmin
if errorlevel 1 goto ERROR

exit /b 0
:ERROR
  echo Failed %ERRORLEVEL%
  exit /b %ERRORLEVEL%
