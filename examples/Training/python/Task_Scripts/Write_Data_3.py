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
from omero.gateway import BlitzGateway
import omero
from omero.rtypes import rstring
from omero import ValidationException
from Parse_OMERO_Properties import USERNAME, PASSWORD, HOST, PORT
from Parse_OMERO_Properties import datasetId

# Script definition
conn = BlitzGateway(USERNAME, PASSWORD, host=HOST, port=PORT)
conn.connect()

# Create a new Image
# ==================
imageObj = omero.model.ImageI()
imageObj.setName(rstring("New Image"))
imageObj = conn.getUpdateService().saveAndReturnObject(imageObj)
imageId = imageObj.getId().getValue()
print "New image, Id:", imageId

try:
    link = omero.model.DatasetImageLinkI()
    link.setParent(omero.model.DatasetI(datasetId, False))
    link.setChild(omero.model.ImageI(imageId, False))
    conn.getUpdateService().saveObject(link)
    message = "Added Image to Dataset"
except ValidationException:
    message = "Could not add Image to Dataset - Already added"

# Return some value(s).

# Here, we return anything useful the script has produced.
# NB: The Insight and web clients will display the "Message" output.


# Close connection
# ================
# When you are done, close the session to free up server resources.
conn._closeSession()
