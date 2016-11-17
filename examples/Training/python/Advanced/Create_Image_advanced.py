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
image_id2 = imageId
for img in dataset.listChildren():
    if img.getName() == "test.fake":
        image_id2 = img.getId()
        break

replace_channel = 0


# Create an Image from 2 others
# =================================================================
# Replace one channel with a channel from another image.
# orphaned image has sizeT=10
img = conn.getObject('Image', imageId)
# Use the image in dataset as source image
img_2 = conn.getObject('Image', image_id2)
size_z, size_c, size_t = img_2.getSizeZ(), img_2.getSizeC(), img_2.getSizeT()
dataset = img.getParent()
pixels = img.getPrimaryPixels()
pixels2 = img_2.getPrimaryPixels()


def plane_gen():
    """set up a generator of 2D numpy arrays."""
    for z in range(size_z):          # all Z sections
        for c in range(size_c):
            for t in range(size_t):      # all time-points
                print "Plane: ", z, c, t
                if c == replace_channel:
                    yield pixels.getPlane(z, c, t)
                else:
                    yield pixels2.getPlane(z, c, t)


desc = ("Image created from Image ID: %s, replacing Channel %s from Image ID:"
        " %s" % (image_id2, replace_channel, imageId))
new_img = conn.createImageFromNumpySeq(
    plane_gen(), "ImageFromTwo", size_z, size_c, size_t, description=desc,
    dataset=dataset)


# Get original channel names and colors to apply to new image
# =================================================================
c_names = []
colors = []
for ch in img.getChannels():
    c_names.append(ch.getLabel())
    colors.append(ch.getColor().getRGB())

# Save channel names and colors
# =================================================================
print "Applying channel Names:", c_names, " Colors:", colors
for i, c in enumerate(new_img.getChannels()):
    lc = c.getLogicalChannel()
    lc.setName(c_names[i])
    lc.save()
    r, g, b = colors[i]
    # need to reload channels to avoid optimistic lock on update
    c_obj = conn.getQueryService().get("Channel", c.getId())
    c_obj.red = rint(r)
    c_obj.green = rint(g)
    c_obj.blue = rint(b)
    c_obj.alpha = rint(255)
    conn.getUpdateService().saveObject(c_obj)
new_img.resetRDefs()  # reset based on colors above


# Apply pixel sizes from original image
# =================================================================
new_pix = conn.getQueryService().get("Pixels", pixels2.getPixelsId())

new_pix.setPhysicalSizeX(pixels.getPhysicalSizeX())
new_pix.setPhysicalSizeY(pixels.getPhysicalSizeY())
new_pix.setPhysicalSizeZ(pixels.getPhysicalSizeZ())
conn.getUpdateService().saveObject(new_pix)


# Close connection:
# =================================================================
# When you are done, close the session to free up server resources.
conn.close()
