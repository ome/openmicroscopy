REM
REM Run tests
REM
python build.py clean
if errorlevel 1 goto ERROR
python build.py
if errorlevel 1 goto ERROR
if %OMERO_PREFIX%==0 (
set webport=4064
) else (
set webport=%OMERO_PREFIX%4064
)
python dist\bin\omero config set omero.web.server_list '[["%OMERO_HOST%", %webport%, "omero"]]'
if errorlevel 1 goto ERROR
python dist\bin\omero web unittest --config=$ICE_CONFIG --test=webadmin
if errorlevel 1 goto ERROR

exit /b 0
:ERROR
  echo Failed %ERRORLEVEL%
  exit /b %ERRORLEVEL%
