build -f components\tools\OmeroJava\build.xml -Dtest.with.fail=true test
if errorlevel 1 goto ERROR
build -f components\tools\OmeroJava\build.xml -Dtest.with.fail=true integration
if errorlevel 1 goto ERROR

exit /b 0
:ERROR
  echo Failed %ERRORLEVEL%
  exit /b %ERRORLEVEL%
