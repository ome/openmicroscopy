#!/bin/sh

# OME Shoola startup script for MacOS X
# -------------------------------------------------------------------------------
# 
#  Copyright (C) 2003 Open Microscopy Environment
#        Massachusetts Institute of Technology,
#        National Institutes of Health,
#        University of Dundee
# 
# 
# 
#     This library is free software; you can redistribute it and/or
#     modify it under the terms of the GNU Lesser General Public
#     License as published by the Free Software Foundation; either
#     version 2.1 of the License, or (at your option) any later version.
# 
#     This library is distributed in the hope that it will be useful,
#     but WITHOUT ANY WARRANTY; without even the implied warranty of
#     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
#     Lesser General Public License for more details.
# 
#     You should have received a copy of the GNU Lesser General Public
#     License along with this library; if not, write to the Free Software
#     Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
# 
# -------------------------------------------------------------------------------

# If you have problems with memory errors you may need to change the "start"
# -Xms or "max" -Xmx memory size. More information about these command line
# switches may be found by running "java -X"

java -Xms128000000 -Xmx256000000 -jar shoola.jar
