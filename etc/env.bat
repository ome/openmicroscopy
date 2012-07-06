@echo off
REM
REM Copyright (C) 2012 Glencoe Software, Inc. All rights reserved.
REM
REM This program is free software; you can redistribute it and/or modify
REM it under the terms of the GNU General Public License as published by
REM the Free Software Foundation; either version 2 of the License, or
REM (at your option) any later version.
REM
REM This program is distributed in the hope that it will be useful,
REM but WITHOUT ANY WARRANTY; without even the implied warranty of
REM MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
REM GNU General Public License for more details.
REM
REM You should have received a copy of the GNU General Public License along
REM with this program; if not, write to the Free Software Foundation, Inc.,
REM 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
REM


REM --------------------------------------------------------------------------
REM Environment variables for use by the server. The values listed here will
REM usually be the defaults. These files should be configured by users and then
REM run in the current shell before starting OMERO.
REM --------------------------------------------------------------------------


REM Time until secondary servers startup, in milliseconds.
set OMERO_STARTUP_WAIT=10000

REM Location for the creation of the OMERO temporary directory.
REM If %HOMEPATH% fails, then %TEMP% will be used.
set OMERO_TEMPDIR=%HOMEPATH%\.omero

REM ONLY set OMERO_HOME if you know what you are doing.
REM ----------------------------------------------------
REM Alternative installation directory to use.
REM set OMERO_HOME=c:\omero_dist
