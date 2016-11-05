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

from omero.rtypes import rint
from omero.gateway import BlitzGateway
from Parse_OMERO_Properties import USERNAME, PASSWORD, HOST, PORT
from Parse_OMERO_Properties import imageId, datasetId

# Create a connection
# =================================================================
conn = BlitzGateway(USERNAME, PASSWORD, host=HOST, port=PORT)
conn.connect()


# Retrieve image in specified dataset
# =================================================================
dataset = conn.getObject("Dataset", datasetId)
for image in dataset.listChildren():
    imageId2 = image.getId()
    break

replaceChannel = 0


# Create an Image from 2 others
# =================================================================
# Replace one channel with a channel from another image.
image = conn.getObject('Image', imageId)
image2 = conn.getObject('Image', imageId2)
sizeZ, sizeC, sizeT = image.getSizeZ(), image.getSizeC(), image.getSizeT()
dataset = image.getParent()
pixels = image.getPrimaryPixels()
pixels2 = image2.getPrimaryPixels()


def planeGen():
    """set up a generator of 2D numpy arrays."""
    for z in range(sizeZ):          # all Z sections
        for c in range(sizeC):
            for t in range(sizeT):      # all time-points
                print "Plane: ", z, c, t
                if c == replaceChannel:
                    yield pixels2.getPlane(z, c, t)
                else:
                    yield pixels.getPlane(z, c, t)


desc = ("Image created from Image ID: %s, replacing Channel %s from Image ID:"
        " %s" % (imageId, replaceChannel, imageId2))
newImg = conn.createImageFromNumpySeq(
    planeGen(), "ImageFromTwo", sizeZ, sizeC, sizeT, description=desc,
    dataset=dataset)


# Get original channel names and colors to apply to new image
# =================================================================
cNames = []
colors = []
for ch in image.getChannels():
    cNames.append(ch.getLabel())
    colors.append(ch.getColor().getRGB())

# Save channel names and colors
# =================================================================
print "Applying channel Names:", cNames, " Colors:", colors
for i, c in enumerate(newImg.getChannels()):
    lc = c.getLogicalChannel()
    lc.setName(cNames[i])
    lc.save()
    r, g, b = colors[i]
    # need to reload channels to avoid optimistic lock on update
    cObj = conn.getQueryService().get("Channel", c.id)
    cObj.red = rint(r)
    cObj.green = rint(g)
    cObj.blue = rint(b)
    cObj.alpha = rint(255)
    conn.getUpdateService().saveObject(cObj)
newImg.resetRDefs()  # reset based on colors above


# Apply pixel sizes from original image
# =================================================================
newPix = conn.getQueryService().get("Pixels", newImg.getPixelsId())

newPix.setPhysicalSizeX(pixels.getPhysicalSizeX())
newPix.setPhysicalSizeY(pixels.getPhysicalSizeY())
newPix.setPhysicalSizeZ(pixels.getPhysicalSizeZ())
conn.getUpdateService().saveObject(newPix)


# Close connection:
# =================================================================
# When you are done, close the session to free up server resources.
conn._closeSession()
