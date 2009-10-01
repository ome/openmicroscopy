@echo off
set found=0
FOR /F "usebackq delims=; tokens=*" %%i in (`echo %PYTHONPATH%`) do call :PARSE %%i
goto :EOF

:PARSE
if "%1"=="" goto :OMERO
if /I %1 EQU %CD%\lib\python goto :FOUND 
Shift
goto :PARSE


:FOUND
set found=1

:OMERO
if %found% EQU 0 set PYTHONPATH=%CD%\lib\python;%PYTHONPATH%

