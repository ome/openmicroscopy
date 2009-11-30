@echo off

rem OMERO.importer startup script for Windows
rem ----------------------------------------------------------------------------
rem  Copyright (C) 2006 University of Dundee. All rights reserved.
rem
rem
rem  This program is free software; you can redistribute it and/or modify
rem  it under the terms of the GNU General Public License as published by
rem  the Free Software Foundation; either version 2 of the License, or
rem  (at your option) any later version.
rem  This program is distributed in the hope that it will be useful,
rem  but WITHOUT ANY WARRANTY; without even the implied warranty of
rem  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
rem  GNU General Public License for more details.
rem  
rem  You should have received a copy of the GNU General Public License along
rem  with this program; if not, write to the Free Software Foundation, Inc.,
rem  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
rem
rem ----------------------------------------------------------------------------

rem If you have problems with memory errors you may need to change the "start"
rem -Xms or "max" -Xmx memory size. More information about these command line
rem switches may be found by running "java -X"

REM needed to overcome weird loop behavior
REM in conjunction with variable expansion

@echo off

SETLOCAL EnableDelayedExpansion

set cp = "
	FOR /R ./ %%a in (*.jar) DO (
	SET cp=!cp!;%%a
)

set cp=!cp!"
echo !cp!

java -Xms128000000 -Xmx512000000 -cp "%cp%" ome.formats.importer.Main

REM IMPORTANT NOTE: Windows only supports a short 8k class path. 
REM If the app fails to start you may need to shorten the path by moving the java app folder 
REM to a location closer to the root if your hard drive (like 'C:/myJavaApp/' for example) 
