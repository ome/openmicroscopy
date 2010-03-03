@echo off
REM  Windows bat for OMERO
REM  Copyright (c) 2009, Glencoe Software, Inc.
REM  See LICENSE for details.

call "%~dp0\setpythonpath.bat"
python "%~dp0\omero" %*
