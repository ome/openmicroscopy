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
from omero.rtypes import *
from Connecting import USERNAME, PASSWORD, HOST, PORT
# create a connection
conn = BlitzGateway(USERNAME, PASSWORD, host=HOST, port=PORT)
conn.connect()
imageId = 101       # This image must have at least 2 channels

# Create an image from scratch

# This example demonstrates the usage of the convenience method createImageFromNumpySeq()
# Here we create a multi-dimensional image from a hard-coded array of data.

from numpy import array
sizeX, sizeY, sizeZ, sizeC, sizeT = 5,4,1,2,1
plane1 = array( [[0, 1, 2, 3, 4], [5, 6, 7, 8, 9], [0, 1, 2, 3, 4], [5, 6, 7, 8, 9]])
plane2 = array( [[5, 6, 7, 8, 9], [0, 1, 2, 3, 4], [5, 6, 7, 8, 9], [0, 1, 2, 3, 4]])
planes = [plane1, plane2]
# generator will yield planes
def planeGen():
    for p in planes:
        yield p
desc = "Image created from a hard-coded arrays"
i = conn.createImageFromNumpySeq(planeGen(), "numpy image", sizeZ, sizeC, sizeT, description=desc, dataset=dataset)


# Create an Image from an existing image

# We are going to create a new image by passing the method a 'generator' of 2D planes
# This will come from an existing image, dividing one channel by another

zctList = []
image = conn.getObject('Image', imageId)
sizeZ, sizeC, sizeT = image.getSizeZ(), image.getSizeC(), image.getSizeT()
dataset = image.getParent()
pixels = image.getPrimaryPixels()
newSizeC = 1
# set up a generator of 2D numpy arrays.
# The createImage method below expects planes in the order specified here (for z.. for c.. for t..)
def planeGen():
    for z in range(sizeZ):              # all Z sections
        for c in range(newSizeC):       # Illustrative purposes only, since we only have 1 channel
            for t in range(sizeT):      # all time-points
                channel0 = pixels.getPlane(z,0,t)
                channel1 = pixels.getPlane(z,1,t)
                newPlane = (channel0 + channel1)/2       # numpy allows us to divide arrays
                print "newPlane for z,t:", z, t, newPlane.dtype, newPlane.min(), newPlane.max()
                yield newPlane

desc = "Image created from Image ID: %s by averaging Channel 1 and Channel 2" % imageId
i = conn.createImageFromNumpySeq(planeGen(), "new image", sizeZ, newSizeC, sizeT, description=desc, dataset=dataset)