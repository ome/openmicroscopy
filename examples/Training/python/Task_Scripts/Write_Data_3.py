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
from omero.rtypes import rstring, unwrap
from omero.gateway import BlitzGateway
import omero
import omero.scripts as scripts
from omero import ValidationException
# Script definition

# Script name, description and 2 parameters are defined here.
# These parameters will be recognised by the Insight and web clients and
# populated with the currently selected Image(s)

client = scripts.client(
    "Write_Data_3.py",
    """Adds Image to Dataset (if not alreay in the Dataset).""",
    # first parameter
    scripts.Long("ImageId", optional=False),
    # second parameter
    scripts.Long("DatasetId", optional=False),
)
# we can now create our Blitz Gateway by wrapping the client object
conn = BlitzGateway(client_obj=client)

# get the parameters
imageId = unwrap(client.getInput("ImageId"))
datasetId = unwrap(client.getInput("DatasetId"))


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

client.setOutput("Message", rstring(message))
client.closeSession()
