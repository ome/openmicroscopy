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

import traceback
import omero
from omero.gateway import BlitzGateway
from Connect_To_OMERO import USERNAME, PASSWORD, HOST, PORT
# create a connection
conn = BlitzGateway(USERNAME, PASSWORD, host=HOST, port=PORT)
connected = conn.connect()
groupId = 5

# Current session details

# By default, you will have logged into your 'current' group in OMERO. This can be changed by
# switching group in the OMERO insight or web clients.

user = conn.getUser()
print "Current user:"
print "   ID:", user.getId()
print "   Username:", user.getName()
print "   Full Name:", user.getFullName()

print "Member of:"
for g in conn.getGroupsMemberOf():
    print "   ID:", g.getName(), " Name:", g.getId()
group = conn.getGroupFromContext()
print "Current group: ", group.getName()

# Change active group

# Every time session is created the user's default group is used as the initial context 
# and is loaded with the security for the current user and thread.
# setSecurityContext sets the context for the current session to the given value.
# WARNING: if there are any jobs run and services cannot be closed, 
# active group will not be changed.

try:
    for k in conn._proxies.keys():
        conn._proxies[k].close()
    conn.c.sf.setSecurityContext(omero.model.ExperimenterGroupI(groupId, False))
    conn.getAdminService().setDefaultGroup(conn.getUser()._obj, omero.model.ExperimenterGroupI(groupId, False))
    conn._ctx = conn.getAdminService().getEventContext()
    conn._user = conn.getObject("Experimenter", conn._userid)
except omero.SecurityViolation:
    print traceback.format_exc()
except:
    print traceback.format_exc()

user = conn.getUser()
print "Current user:"
print "   ID:", user.getId()
print "   Username:", user.getName()
print "   Full Name:", user.getFullName()

print "Member of:"
for g in conn.getGroupsMemberOf():
    print "   ID:", g.getName(), " Name:", g.getId()
group = conn.getGroupFromContext()
print "Current group: ", group.getName()


# Keep the connection alive

conn.getAdminService().getEventContext()

# Close connection

# When you're done, close the session to free up server resources. 

conn._closeSession()
