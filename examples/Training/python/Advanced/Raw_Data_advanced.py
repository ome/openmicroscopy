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
from numpy import int32
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
channel_min_max = []
for c in image.getChannels():
    min_c = c.getWindowMin()
    max_c = c.getWindowMax()
    channel_min_max.append((min_c, max_c))
print channel_min_max


# Go through each channel (looping through Z and T not shown - go for mid-Z
# only)
# =================================================================
z = image.getSizeZ() / 2
t = 0
c = 0
for min_max in channel_min_max:
    plane = pixels.getPlane(z, c, t)
    name = "tiffPlaneInt8%s.tiff" % c
    scriptUtil.numpy_save_as_image(plane, min_max, int32, name)


# Close connection:
# =================================================================
# When you are done, close the session to free up server resources.
conn.close()
