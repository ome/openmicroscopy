REM
REM Run tests
REM
python bin\omero web test webadmin
if errorlevel 1 goto ERROR

exit /b 0
:ERROR
  echo Failed %ERRORLEVEL%
  exit /b %ERRORLEVEL%