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

import Ice
import omero
import omero.callbacks
from omero.gateway import BlitzGateway
from Connect_To_OMERO import USERNAME, PASSWORD, HOST, PORT
# create a connection
conn = BlitzGateway(USERNAME, PASSWORD, host=HOST, port=PORT)
conn.connect()
projectId = 305
deleteChildren = True # FLAG

project = conn.getObject("Project", projectId)
if project is None:
    import sys
    sys.stderr.write("Error: Object does not exist.\n")
    sys.exit(1)
    
print "\nProject:", project.getName()

# Delete Image

# You can delete a number of objects of the same type at the same time. In this case 'Project'

obj_ids = [projectId]
# use deleteChildren=True if you are E.g. deleting a Project and you want to delete Datasets and Images.
handle = conn.deleteObjects("Project", obj_ids, deleteAnns=True, deleteChildren=deleteChildren)
# callback as string
# cbString = str(handle)

# retrieve callback and wait delete complete
callback = omero.callbacks.DeleteCallbackI(conn.c, handle)
print "Deleting, please wait."
while callback.block(500) is not None: # ms.
    print "."

# print errors
print "Errors:", handle.errors()
# close callback
callback.close()

# retrieve callback from string
# try:
#     post_handle = omero.api.delete.DeleteHandlePrx.checkedCast(conn.c.ic.stringToProxy(cbString))
#     self.fail("exception Ice.ObjectNotExistException was not thrown")
# except Ice.ObjectNotExistException:
#     pass

# Close connection

# When you're done, close the session to free up server resources. 

conn._closeSession()
