#!/bin/sh

# OMERO.importer startup script for MacOS X
# ------------------------------------------------------------------------------
#  Copyright (C) 2006-2001 University of Dundee & Open Microscopy Environment.
#  All rights reserved.
#
#
#  This program is free software; you can redistribute it and/or modify
#  it under the terms of the GNU General Public License as published by
#  the Free Software Foundation; either version 2 of the License, or
#  (at your option) any later version.
#  This program is distributed in the hope that it will be useful,
#  but WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#  GNU General Public License for more details.
#  
#  You should have received a copy of the GNU General Public License along
#  with this program; if not, write to the Free Software Foundation, Inc.,
#  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
#
#-------------------------------------------------------------------------------

# If you have problems with memory errors you may need to change the "start"
# -Xms or "max" -Xmx memory size. More information about these command line
# switches may be found by running "java -X"

java -Xms256000000 -Xmx1024000000 -jar omero.insight.jar containerImporter.xml
