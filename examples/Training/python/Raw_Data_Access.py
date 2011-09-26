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

# Retrieve a given plane

# Use the pixelswrapper to retrieve the plane as 
# a 2D numpy array. See http://www.scipy.org/Tentative_NumPy_Tutorial

# Numpy array can be used for various analysis routines

image = conn.getObject("Image", imageId)
sizeZ = image.getSizeZ()
sizeC = image.getSizeC()
sizeT = image.getSizeT()
z,t,c = 0,0,0                       # first plane of the image
pixels = image.getPrimaryPixels()
plane = pixels.getPlane(z,c,t)      # get a numpy array.
print "\nPlane at zct: ", z, c, t
print plane
print "shape: ", plane.shape
print "min:", plane.min(), " max:", plane.max(), "pixel type:", plane.dtype.name


# Retrieve a given stack

# Get a Z-stack of tiles. 
# Using getTiles or getPlanes (see below) returns a generator of data (not all the data in hand)
# The RawPixelsStore is only opened once (not closed after each plane)
# Alternative is to use getPlane() or getTile() multiple times - slightly slower.

c,t = 0,0                   # First channel and timepoint
tile = (50, 50, 10, 10)     # x, y, width, height of tile
zctList = [(z, c, t, tile) for z in range(sizeZ)]     # list of [ (0,0,0,(x,y,w,h)), (1,0,0,(x,y,w,h)), (2,0,0,(x,y,w,h))....etc... ]
print "\nZ stack of tiles:"
planes = pixels.getTiles(zctList)
for i, p in enumerate(planes):
    print "Tile:", zctList[i], " min:", p.min(), " max:", p.max(), " sum:", p.sum()


#Retrieve a given hypercube

zctList = []
for z in range(sizeZ/2, sizeZ):     # get the top half of the Z-stack
    for c in range(sizeC):          # all channels
        for t in range(sizeT):      # all time-points
            zctList.append( (z,c,t) )
print "\nHyper stack of planes:"
planes = pixels.getPlanes(zctList)
for i, p in enumerate(planes):
    print "plane zct:", zctList[i], " min:", p.min(), " max:", p.max()