@echo off

REM  Windows test bat for OMERO.matlab
REM  Copyright (c) 2009, Glencoe Software, Inc.
REM  See LICENSE for details.
REM
REM OMERO_HOME must be set in the calling environment.
REM

cd %~dp0
start matlab -nosplash -nodesktop -minimize -r "omero_test" -logfile ..\target\omero_test.log

