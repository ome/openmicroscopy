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

"""
start-code
"""

# Create a connection
# ===================
conn = BlitzGateway(USERNAME, PASSWORD, host=HOST, port=PORT)
conn.connect()


# Get the 'Fileset' for an Image
# ==============================
# A Fileset is a collection of the original files imported to
# create an image or set of images in OMERO.
image = conn.getObject("Image", imageId)
fileset = image.getFileset()       # will be None for pre-FS images
fs_id = fileset.getId()
# List all images that are in this fileset
for fs_image in fileset.copyImages():
    print fs_image.getId(), fs_image.getName()
# List original imported files
for orig_file in fileset.listFiles():
    name = orig_file.getName()
    path = orig_file.getPath()
    print path, name


# Get Original Imported Files directly from the image
# ===================================================
# this will include pre-FS data IF images were archived on import
print image.countImportedImageFiles()
# specifically count Fileset files
file_count = image.countFilesetFiles()
# list files
if file_count > 0:
    for orig_file in image.getImportedImageFiles():
        name = orig_file.getName()
        path = orig_file.getPath()
        print path, name


# Can get the Fileset using conn.getObject()
# ==========================================
fileset = conn.getObject("Fileset", fs_id)


# Close connection
# ================
# When you are done, close the session to free up server resources.
conn.close()
