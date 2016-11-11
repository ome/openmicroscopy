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

import omero.util.script_utils as scriptUtil
from numpy import uint8
from omero.gateway import BlitzGateway
from Parse_OMERO_Properties import USERNAME, PASSWORD, HOST, PORT
from Parse_OMERO_Properties import imageId


# Create a connection
# =================================================================
conn = BlitzGateway(USERNAME, PASSWORD, host=HOST, port=PORT)
conn.connect()


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
    name = "tiffPlaneInt8%s.png" % cIndex
    scriptUtil.numpySaveAsImage(plane, minMax, uint8, name)
    cIndex += 1


# Close connection:
# =================================================================
# When you are done, close the session to free up server resources.
conn._closeSession()
