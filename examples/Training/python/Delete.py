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

import omero
import omero.callbacks
from omero.rtypes import rstring
from omero.gateway import BlitzGateway
from Parse_OMERO_Properties import USERNAME, PASSWORD, HOST, PORT


# Create a connection
# =================================================================
conn = BlitzGateway(USERNAME, PASSWORD, host=HOST, port=PORT)
conn.connect()

# Create a new Project
# =================================================================
project = omero.model.ProjectI()
project.setName(rstring("New Project"))
project = conn.getUpdateService().saveAndReturnObject(project)
projectId = project.id.val

# Load the Project
# =================================================================
project = conn.getObject("Project", projectId)
if project is None:
    import sys
    sys.stderr.write("Error: Object does not exist.\n")
    sys.exit(1)

print "\nProject:", project.getName()


# Delete Project
# =================================================================
# You can delete a number of objects of the same type at the same
# time. In this case 'Project'. Use deleteChildren=True if you are
# deleting a Project and you want to delete Datasets and Images.
obj_ids = [projectId]
deleteChildren = False
handle = conn.deleteObjects(
    "Project", obj_ids, deleteAnns=True, deleteChildren=deleteChildren)


# Retrieve callback and wait until delete completes
# =================================================================
# This is not necessary for the Delete to complete. Can be used
# if you want to know when delete is finished or if there were any errors
cb = omero.callbacks.CmdCallbackI(conn.c, handle)
print "Deleting, please wait."
while not cb.block(500):
    print "."
err = isinstance(cb.getResponse(), omero.cmd.ERR)
print "Error?", err
if err:
    print cb.getResponse()
cb.close(True)      # close handle too


# Close connection:
# =================================================================
# When you are done, close the session to free up server resources.
conn._closeSession()
