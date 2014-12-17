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
from omero.rtypes import rlong, rstring, unwrap
from omero.gateway import BlitzGateway
import omero
import omero.scripts as scripts

# Script definition

# Script name, description and 2 parameters are defined here.
# These parameters will be recognised by the Insight and web clients and
# populated with the currently selected Image(s)

dataTypes = [rstring('Project')]
client = scripts.client(
    "Write_Data-4.py",
    """Downloads a named file annotation on a Project""",
    # first parameter
    scripts.String(
        "Data_Type", optional=False, values=dataTypes, default="Project"),
    # second parameter
    scripts.List("IDs", optional=False).ofType(rlong(0)),
    scripts.String("File_Name", optional=False),
)
# we can now create our Blitz Gateway by wrapping the client object
conn = BlitzGateway(client_obj=client)

# get the parameters
IDs = unwrap(client.getInput("IDs"))
projectId = IDs[0]
fileName = unwrap(client.getInput("File_Name"))

project = conn.getObject('Project', projectId)
message = "No file downloaded."
ratingNs = None
for ann in project.listAnnotations():
    if isinstance(ann, omero.gateway.FileAnnotationWrapper):
        name = ann.getFile().getName()
        print "File ID: %s Name: %s Size: %s" % (
            ann.getFile().getId(), name, ann.getFile().getSize())
        if fileName == name:
            file_path = 'downloadFile'
            f = open(file_path, 'w')
            print "\nDownloading file ", fileName, "to", file_path, "..."
            try:
                for chunk in ann.getFileInChunks():
                    f.write(chunk)
            finally:
                f.close()
                print "File downloaded!"

            message = "File Downloaded."

    elif isinstance(ann, omero.gateway.LongAnnotationWrapper):
        # This may be a 'Rating' annotation, so let's get it's namespace
        ratingNs = ann.getNs()

if ratingNs is not None:
    message += " Rating Ns: %s" % ratingNs
else:
    message += " Project not rated."

# Return some value(s).

# Here, we return anything useful the script has produced.
# NB: The Insight and web clients will display the "Message" output.

client.setOutput("Message", rstring(message))
client.closeSession()
