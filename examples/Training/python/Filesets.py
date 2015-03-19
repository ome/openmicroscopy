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
from Parse_OMERO_Properties import USERNAME, PASSWORD, HOST, PORT
from Parse_OMERO_Properties import imageId

# Create a connection
# =================================================================
conn = BlitzGateway(USERNAME, PASSWORD, host=HOST, port=PORT)
conn.connect()


# Get the 'Fileset' for an Image
# =================================================================
# A Fileset is a collection of the original files imported to
# create an image or set of images in OMERO.
image = conn.getObject("Image", imageId)
fileset = image.getFileset()       # will be None for pre-FS images
fsId = fileset.getId()
# List all images that are in this fileset
for fsImage in fileset.copyImages():
    print fsImage.getId(), fsImage.getName()
# List original imported files
for origFile in fileset.listFiles():
    name = origFile.getName()
    path = origFile.getPath()
    print path, name


# Get Original Imported Files directly from the image
# =================================================================
# this will include pre-FS data IF images were archived on import
print image.countImportedImageFiles()
# specifically count Fileset files
fileCount = image.countFilesetFiles()
# list files
if fileCount > 0:
    for origFile in image.getImportedImageFiles():
        name = origFile.getName()
        path = origFile.getPath()
        print path, name


# Can get the Fileset using conn.getObject()
# =================================================================
fileset = conn.getObject("Fileset", fsId)


# Close connection:
# =================================================================
# When you are done, close the session to free up server resources.
conn._closeSession()
