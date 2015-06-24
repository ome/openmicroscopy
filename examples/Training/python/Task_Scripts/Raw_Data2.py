#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2011 University of Dundee & Open Microscopy Environment.
#                    All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
#

"""
FOR TRAINING PURPOSES ONLY!
"""

# This is a 'bare-bones' template to allow easy conversion from a simple
# client-side Python script to a script run by the server, on the OMERO
# scripting service.
# To use the script, simply paste the body of the script (not the connection
# code) into the point indicated below.
# A more complete template, for 'real-world' scripts, is also included in this
# folder
# This script takes an Image ID as a parameter from the scripting service.
from omero.gateway import BlitzGateway
from numpy import hstack, zeros, uint8
try:
    import Image
except ImportError:
    from PIL import Image

from Parse_OMERO_Properties import USERNAME, PASSWORD, HOST, PORT

# Script definition

conn = BlitzGateway(USERNAME, PASSWORD, host=HOST, port=PORT)
conn.connect()
imageId = 27565

# First Task: Get the Average pixel value for a specified region:
image = conn.getObject("Image", imageId)
print image.getName()
sizeZ = image.getSizeZ()
sizeC = image.getSizeC()
sizeT = image.getSizeT()
t, c = 0, 0                     # first plane of the image
z = sizeZ/2                     # at the mid Z-section
x, y, w, h = 5, 5, 100, 100        # Our pre-defined x, y, w, h
tile = (x, y, w, h)
pixels = image.getPrimaryPixels()
tile_data = pixels.getTile(z, c, t, tile)      # get a numpy array.
print "\Tile at zct tile: ", z, c, t, tile
print tile_data
print "shape: ", tile_data.shape
print "min:", tile_data.min(), " max:", tile_data.max(),\
    "pixel type:", tile_data.dtype.name
average = float(tile_data.sum()) / (w * h)
print "Average :", average


# Advanced: For each time point, get a column of data (E.g. for kymograph)
sizeY = image.getSizeY()
sizeX = image.getSizeX()
x = sizeX/2
y = 0
width = 10        # we were asked for '1' but this looks nicer
height = sizeX
tile = (x, y, width, height)
col_data = []   # let's collect the column data for each time point

for theT in range(sizeT):
    print "Getting data for T, tile:", theT, tile
    col = pixels.getTile(z, c, theT, tile)
    col_data.append(col)

# ** BONUS **
# stack the numpy columns horizontally. hstack is a numpy function
kymograph_data = hstack(col_data)
print "kymograph_data", kymograph_data.shape


if kymograph_data.dtype.name not in ('uint8', 'int8'):  # we need to scale...
    minVal = kymograph_data.min()
    maxVal = kymograph_data.max()
    valRange = maxVal - minVal
    scaled = (kymograph_data - minVal) * (float(255) / valRange)
    convArray = zeros(kymograph_data.shape, dtype=uint8)
    convArray += scaled
    print ("using converted int8 plane: dtype: %s min: %s max: %s"
           % (convArray.dtype.name, convArray.min(), convArray.max()))
    i = Image.fromarray(convArray)
else:
    i = Image.fromarray(kymograph_data)
i.show()
i.save("kymograph.png", 'PNG')

# attach the png to the image
fileAnn = conn.createFileAnnfromLocalFile(
    "kymograph.png", mimetype="image/png")
print "Attaching kymograph.png to image"
image.linkAnnotation(fileAnn)

message = "Tile average value: %s" % average
# client.setOutput("Message", rstring(message))
# client.setOutput("Kymograph", robject(fileAnn._obj))
# client.closeSession()
