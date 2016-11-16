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

"""
start-code
"""

# Create a connection
# ===================
conn = BlitzGateway(USERNAME, PASSWORD, host=HOST, port=PORT)
conn.connect()


# Create new Project
# ==================
def createProject():
    project = omero.model.ProjectI()
    project.setName(rstring("New Project"))
    project = conn.getUpdateService().saveAndReturnObject(project)
    projectId = project.id.val
    return projectId


projectId1 = createProject()
projectId2 = createProject()


# Delete Project
# ==============
# You can delete a number of objects of the same type at the same
# time. In this case 'Project'. Use deleteChildren=True if you are
# deleting a Project and you want to delete Datasets and Images.
# We use wait=True so that the async delete completes.
obj_ids = [projectId1]
deleteChildren = False
conn.deleteObjects(
    "Project", obj_ids, deleteAnns=True,
    deleteChildren=deleteChildren, wait=True)


# Delete Project, handling response
# =================================
# If you want to know when delete is finished or if there were
# any errors, then we can use a callback to wait for response
handle = conn.deleteObjects("Project", [projectId2])
cb = omero.callbacks.CmdCallbackI(conn.c, handle)
print "Deleting, please wait."
while not cb.block(500):
    print "."
err = isinstance(cb.getResponse(), omero.cmd.ERR)
print "Error?", err
if err:
    print cb.getResponse()
cb.close(True)      # close handle too


# Close connection
# ================
# When you are done, close the session to free up server resources.
conn.close()
