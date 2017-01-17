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
from numpy import hstack, int32
from Parse_OMERO_Properties import USERNAME, PASSWORD, HOST, PORT
from Parse_OMERO_Properties import imageId

# Script name, description and 2 parameters are defined here.
# These parameters will be recognised by the Insight and web clients and
# populated with the currently selected Image(s)

conn = BlitzGateway(USERNAME, PASSWORD, host=HOST, port=PORT)
conn.connect()

# First Task: Get the Average pixel value for a specified region:
image = conn.getObject("Image", imageId)
print image.getName()
size_z = image.getSizeZ()
size_c = image.getSizeC()
size_t = image.getSizeT()
t, c = 0, 0                     # first plane of the image
z = size_z/2                     # at the mid Z-section
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
size_y = image.getSizeY()
size_x = image.getSizeX()
x = size_x/2
y = 0
width = 10        # we were asked for '1' but this looks nicer
height = size_x
tile = (x, y, width, height)
col_data = []   # let's collect the column data for each time point

for t in range(size_t):
    print "Getting data for T, tile:", t, tile
    col = pixels.getTile(z, c, t, tile)
    col_data.append(col)

# ** BONUS **
# stack the numpy columns horizontally. hstack is a numpy function
kymograph_data = hstack(col_data)
print "kymograph_data", kymograph_data.shape

name = "kymograph.png"
min_max = (kymograph_data.min(), kymograph_data.max())
scriptUtil.numpy_save_as_image(kymograph_data, min_max, int32, name)


# attach the png to the image
file_ann = conn.createFileAnnfromLocalFile(
    name, mimetype="image/png")
print "Attaching %s to image" % name
image.linkAnnotation(file_ann)


message = "Tile average value: %s" % average

# Close connection:
# =================================================================
# When you are done, close the session to free up server resources.
conn.close()
