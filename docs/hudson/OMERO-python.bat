python build.py -f components\tools\OmeroPy\build.xml -Dtest.with.fail=true test
if errorlevel 1 goto ERROR
python build.py -f components\tools\OmeroPy\build.xml -Dtest.with.fail=true integration
if errorlevel 1 goto ERROR

exit /b 0
:ERROR
  echo Failed %ERRORLEVEL%
  exit /b %ERRORLEVEL%
