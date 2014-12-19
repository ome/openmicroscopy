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

from numpy import zeros, uint8
from omero.gateway import BlitzGateway
from Connect_To_OMERO import USERNAME, PASSWORD, HOST, PORT
try:
    from PIL import Image
except ImportError:
    import Image


# Create a connection
# =================================================================
conn = BlitzGateway(USERNAME, PASSWORD, host=HOST, port=PORT)
conn.connect()


# Configuration
# =================================================================
imageId = 401


# Save a plane (raw data) as tiff for analysis
# =================================================================
image = conn.getObject("Image", imageId)  # first plane of the image
pixels = image.getPrimaryPixels()
# make a note of min max pixel values for each channel
# so that we can scale all the planes from each channel to the same range
channelMinMax = []
for c in image.getChannels():
    minC = c.getWindowMin()
    maxC = c.getWindowMax()
    channelMinMax.append((minC, maxC))
print channelMinMax


# Go through each channel (looping through Z and T not shown - go for mid-Z
# only)
# =================================================================
theZ = image.getSizeZ() / 2
theT = 0
cIndex = 0
for minMax in channelMinMax:
    plane = pixels.getPlane(theZ, cIndex, theT)
    print "dtype:", plane.dtype.name
    # need plane dtype to be uint8 (or int8) for conversion to tiff by PIL
    if plane.dtype.name not in ('uint8', 'int8'):      # we need to scale...
        minVal, maxVal = minMax
        valRange = maxVal - minVal
        scaled = (plane - minVal) * (float(255) / valRange)
        convArray = zeros(plane.shape, dtype=uint8)
        convArray += scaled
        print ("using converted int8 plane: dtype: %s min: %s max: %s"
               % (convArray.dtype.name, convArray.min(), convArray.max()))
        i = Image.fromarray(convArray)
    else:
        i = Image.fromarray(plane)
    i.save("tiffPlaneInt8%s.tiff" % cIndex)
    cIndex += 1


# Close connection:
# =================================================================
# When you are done, close the session to free up server resources.
conn._closeSession()
