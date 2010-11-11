del ice.config
wget http://hudson.openmicroscopy.org.uk/job/OMERO-trunk/lastSuccessfulBuild/artifact/trunk/ice.config
set ICE_CONFIG=%CD%\ice.config

call "C:\Program Files\Microsoft Visual Studio 9.0\VC\vcvarsall.bat" x86
if errorlevel 1 exit /b 1

set OMERO_BUILD=r%SVN_REVISION%-d%BUILD_NUMBER%
set OMERO_CONFIG=%JOB_NAME%
set ICE_HOME=c:\Ice-3.3.1-VC90
set CXXFLAGS=/DBOOST_TEST_SOURCE
set CPPPATH=c:\progra~1\boost\boost_1_39\
set LIBPATH=%CPPPATH%\lib
REM set PATH=%ICE_HOME%\bin\x64;%ICE_HOME%\bin;%PATH%
set VERBOSE=1
REM set J=8 Possibly causes strange build issues in blitz

cd trunk

set

python build.py clean
if errorlevel 1 exit /b 1

python build.py build-default
if errorlevel 1 exit /b 1

python build.py build-cpp
if errorlevel 1 exit /b 1

set RELEASE=Os
python build.py build-cpp
if errorlevel 1 exit /b 1

python build.py -f components\tools\OmeroCpp\build.xml test
if errorlevel 1 exit /b 1

python build.py -f components\tools\OmeroCpp\build.xml integration
if errorlevel 1 exit /b 1
