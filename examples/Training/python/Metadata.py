#!/usr/bin/env python
# 
# Copyright (c) 2011 University of Dundee. 
# 
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU Affero General Public License as
# published by the Free Software Foundation, either version 3 of the
# License, or (at your option) any later version.
# 
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Affero General Public License for more details.
# 
# You should have received a copy of the GNU Affero General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
# 
# Version: 1.0
#
# This script shows a simple connection to OMERO, printing details of the connection.
# NB: You will need to edit the config.py before running.
# 
# 
from omero.gateway import BlitzGateway
from Connecting import USERNAME, PASSWORD, HOST, PORT
# create a connection
conn = BlitzGateway(USERNAME, PASSWORD, host=HOST, port=PORT)
conn.connect()
imageId = 101

# Load the 'Original Metadata' for the image

image = conn.getObject("Image", imageId)
om = image.loadOriginalMetadata()
if om is not None:
    print "original_metadata"
    print "    File Annotation ID:", om[0].getId()
    print "global_metadata"
    for keyValue in om[1]:
        if len(keyValue) > 1:
            print "   ", keyValue[0], keyValue[1]
        else:
            print "   ", keyValue[0], "NOT FOUND"
    print "series_metadata"
    for keyValue in om[2]:
        if len(keyValue) > 1:
            print "   ", keyValue[0], keyValue[1]
        else:
            print "   ", keyValue[0], "NOT FOUND" 



conn._closeSession()