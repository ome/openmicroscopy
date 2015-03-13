#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2014 University of Dundee & Open Microscopy Environment.
#                    All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
#

"""
FOR TRAINING PURPOSES ONLY!
"""

from omero.gateway import BlitzGateway
from Connect_To_OMERO import USERNAME, PASSWORD, HOST, PORT


# Create a connection
# =================================================================
conn = BlitzGateway(USERNAME, PASSWORD, host=HOST, port=PORT)
conn.connect()


# Configuration
# =================================================================
imageId = 27544     # This image must have at least 2 channels


# Create an image from scratch
# =================================================================
# This example demonstrates the usage of the convenience method
# createImageFromNumpySeq() Here we create a multi-dimensional image from a
# hard-coded array of data.
from numpy import array, int8
import omero
sizeX, sizeY, sizeZ, sizeC, sizeT = 5, 4, 1, 2, 1
plane1 = array(
    [[0, 1, 2, 3, 4], [5, 6, 7, 8, 9], [0, 1, 2, 3, 4], [5, 6, 7, 8, 9]],
    dtype=int8)
plane2 = array(
    [[5, 6, 7, 8, 9], [0, 1, 2, 3, 4], [5, 6, 7, 8, 9], [0, 1, 2, 3, 4]],
    dtype=int8)
planes = [plane1, plane2]


def planeGen():
    """generator will yield planes"""
    for p in planes:
        yield p


desc = "Image created from a hard-coded arrays"
i = conn.createImageFromNumpySeq(
    planeGen(), "numpy image", sizeZ, sizeC, sizeT, description=desc,
    dataset=None)

print 'Created new Image:%s Name:"%s"' % (i.getId(), i.getName())


# Set the pixel size using units (new in 5.1.0)
# =================================================================
# Lengths are specified by value and a unit enumeration
# Here we set the pixel size X and Y to be 9.8 Angstroms

from omero.model.enums import UnitsLength
# Re-load the image to avoid update conflicts
i = conn.getObject("Image", i.getId())
u = omero.model.LengthI(9.8, UnitsLength.ANGSTROM)
p = i.getPrimaryPixels()._obj
p.setPhysicalSizeX(u)
p.setPhysicalSizeY(u)
conn.getUpdateService().saveObject(p)


# Create an Image from an existing image
# =================================================================
# We are going to create a new image by passing the method a 'generator' of 2D
# planes This will come from an existing image, by taking the average of 2
# channels.
zctList = []
image = conn.getObject('Image', imageId)
sizeZ, sizeC, sizeT = image.getSizeZ(), image.getSizeC(), image.getSizeT()
dataset = image.getParent()
pixels = image.getPrimaryPixels()
newSizeC = 1


def planeGen():
    """
    set up a generator of 2D numpy arrays.

    The createImage method below expects planes in the order specified here
    (for z.. for c.. for t..)
    """
    for z in range(sizeZ):              # all Z sections
        # Illustrative purposes only, since we only have 1 channel
        for c in range(newSizeC):
            for t in range(sizeT):      # all time-points
                channel0 = pixels.getPlane(z, 0, t)
                channel1 = pixels.getPlane(z, 1, t)
                # Here we can manipulate the data in many different ways. As
                # an example we are doing "average"
                # average of 2 channels
                newPlane = (channel0 + channel1) / 2
                print "newPlane for z,t:", z, t, newPlane.dtype, \
                    newPlane.min(), newPlane.max()
                yield newPlane


desc = ("Image created from Image ID: %s by averaging Channel 1 and Channel 2"
        % imageId)
i = conn.createImageFromNumpySeq(
    planeGen(), "new image", sizeZ, newSizeC, sizeT, description=desc,
    dataset=dataset)


# Close connection:
# =================================================================
# When you are done, close the session to free up server resources.
conn._closeSession()
