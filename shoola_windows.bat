rem OME Shoola startup script for Windows
rem -------------------------------------------------------------------------------
rem 
rem  Copyright (C) 2003 Open Microscopy Environment
rem        Massachusetts Institute of Technology,
rem        National Institutes of Health,
rem        University of Dundee
rem 
rem 
rem 
rem     This library is free software; you can redistribute it and/or
rem     modify it under the terms of the GNU Lesser General Public
rem     License as published by the Free Software Foundation; either
rem     version 2.1 of the License, or (at your option) any later version.
rem 
rem     This library is distributed in the hope that it will be useful,
rem     but WITHOUT ANY WARRANTY; without even the implied warranty of
rem     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
rem     Lesser General Public License for more details.
rem 
rem     You should have received a copy of the GNU Lesser General Public
rem     License along with this library; if not, write to the Free Software
rem     Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
rem 
rem -------------------------------------------------------------------------------

rem If you have problems with memory errors you may need to change the "start"
rem -Xms or "max" -Xmx memory size. More information about these command line
rem switches may be found by running "java -X"

java -Xms128000000 -Xmx256000000 -jar shoola.jar
