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

# This is a 'bare-bones' template to allow easy conversion from a simple
# client-side Python script to a script run by the server, on the OMERO
# scripting service.
# To use the script, simply paste the body of the script (not the connection
# code) into the point indicated below.
# A more complete template, for 'real-world' scripts, is also included in this
# folder
# This script takes an Image ID as a parameter from the scripting service.
from omero.rtypes import rlong, rstring, unwrap
from omero.gateway import BlitzGateway
import omero.scripts as scripts

# Script definition

# Script name, description and 2 parameters are defined here.
# These parameters will be recognised by the Insight and web clients and
# populated with the currently selected Image(s)

# this script only takes Images (not Datasets etc.)
dataTypes = [rstring('Image')]
client = scripts.client(
    "Scripting_Service_Example.py",
    ("Example script to use as a template for getting started with the"
     " scripting service."),
    # first parameter
    scripts.String(
        "Data_Type", optional=False, values=dataTypes, default="Image"),
    # second parameter
    scripts.List("IDs", optional=False).ofType(rlong(0)),
)
# we can now create our Blitz Gateway by wrapping the client object
conn = BlitzGateway(client_obj=client)

# get the 'IDs' parameter (which we have restricted to 'Image' IDs)
ids = unwrap(client.getInput("IDs"))
imageId = ids[0]        # simply use the first ID for this example


# ** paste here **
# Replace the code block below. NB: we have established a connection "conn"
# and we have an "imageId"
image = conn.getObject("Image", imageId)
print image.getName()


# Return some value(s).

# Here, we return anything useful the script has produced.
# NB: The Insight and web clients will display the "Message" output.

message = "Script ran with Image ID: %s, Name: %s" % (imageId, image.getName())
client.setOutput("Message", rstring(message))
client.closeSession()
