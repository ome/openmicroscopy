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
import traceback
import omero
from omero.gateway import BlitzGateway
from Connecting import USERNAME, PASSWORD, HOST, PORT
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