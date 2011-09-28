#!/usr/bin/env python
# 
# Copyright (c) 2011 University of Dundee. 
# 
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU Affero General Public License as
# published by the Free Software Foundation, either version 3 of the
# License, or (at your option) any later version.
# 
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Affero General Public License for more details.
# 
# You should have received a copy of the GNU Affero General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
# 
# Version: 1.0
#
# This script shows a simple connection to OMERO, printing details of the connection.
# NB: You will need to edit the config.py before running.
# 
# 
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