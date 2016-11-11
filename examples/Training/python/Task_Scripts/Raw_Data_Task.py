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

import omero.util.script_utils as scriptUtil
from omero.gateway import BlitzGateway
from numpy import hstack, uint8

from Parse_OMERO_Properties import USERNAME, PASSWORD, HOST, PORT
from Parse_OMERO_Properties import imageId
# Script definition


# Script name, description and 2 parameters are defined here.
# These parameters will be recognised by the Insight and web clients and
# populated with the currently selected Image(s)
conn = BlitzGateway(USERNAME, PASSWORD, host=HOST, port=PORT)
conn.connect()


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

name = "kymograph.png"
minMax = (kymograph_data.min(), kymograph_data.max())
scriptUtil.numpySaveAsImage(kymograph_data, minMax, uint8, name)

# attach the png to the image
fileAnn = conn.createFileAnnfromLocalFile(
    "kymograph.png", mimetype="image/png")
print "Attaching kymograph.png to image"
image.linkAnnotation(fileAnn)


message = "Tile average value: %s" % average


# Close connection:
# =================================================================
# When you are done, close the session to free up server resources.
conn._closeSession()
