echo off
REM
REM  Configuration script for Windows
REM  Changes the installation paths in the
REM  etc\ directory
REM
REM  Copyright (c) 2009, Glencoe Software, Inc.
REM  See LICENSE for details.

cd %~dp0\..
call bin\setpythonpath.bat
python lib\python\omero\install\win_set_path.py
