REM
REM Run tests
REM
python build.py clean
if errorlevel 1 goto ERROR
python build.py
if errorlevel 1 goto ERROR
python dist\bin\omero config set omero.web.server_list '[["%OMERO_HOST%", %OMERO_PREFIX%4064, "omero"]]'
if errorlevel 1 goto ERROR
python dist\bin\omero web unittest --config=$ICE_CONFIG --test=webadmin
if errorlevel 1 goto ERROR

exit /b 0
:ERROR
  echo Failed %ERRORLEVEL%
  exit /b %ERRORLEVEL%
