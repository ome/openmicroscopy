@echo off
REM
REM  Prepends the OMERO distribution containing this bat file
REM  to the PYTHONPATH environment variable if necessary.
REM
REM  Copyright (c) 2009, University of Dundee
REM  See LICENSE for details.

for %%i in ("%~dp0\..\") do (set dist=%%~dpi)
set found=0
FOR /F "usebackq delims=; tokens=*" %%i in (`echo %PYTHONPATH%`) do call :PARSE %%i
goto :EOF

:PARSE
if "%1"=="" goto OMERO
if /I %1 EQU %dist%lib\python goto FOUND
Shift
goto :PARSE

:FOUND
set found=1

:OMERO
if %found% EQU 0 set PYTHONPATH=%dist%lib\python;%PYTHONPATH%
