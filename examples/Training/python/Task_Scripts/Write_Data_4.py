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
from Parse_OMERO_Properties import USERNAME, PASSWORD, HOST, PORT
from Parse_OMERO_Properties import projectId

# Script definition
conn = BlitzGateway(USERNAME, PASSWORD, host=HOST, port=PORT)
conn.connect()

fileName = "File_Name"

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


# Close connection
# ================
# When you are done, close the session to free up server resources.
conn.close()
