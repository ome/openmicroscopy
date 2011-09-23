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

# Connect to the Python Blitz Gateway

# See OmeroPy/Gateway for more info

# import the libraries we need
import my_omero_config as conf
from omero.gateway import BlitzGateway
# create a connection
conn = BlitzGateway(conf.USERNAME, conf.PASSWORD, host=conf.HOST, port=conf.PORT)
connected = conn.connect()
# check if you are connected.
if not connected:
    import sys
    sys.stderr.write("Error: Connection not available, please check your user name and password.\n")
    sys.exit(1)

# Using secure connection.

# By default, once we have logged in, data transfer is not encrypted (faster)

# To use secure connection
conn.setSecure(True)

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

print "Other Members of current group:"
for g in conn.listColleagues():
    print "   ID:", g.getId(), " Name:", g.getFullName()

print "Owner of:"
for g in conn.listOwnedGroups():
    print "   ID:", g.getName(), " Name:", g.getId()

# The 'context' of our current session
ctx = conn.getEventContext()
# print ctx     # for more info

# Close connection

# When you're done, close the session to free up server resources. 

conn._closeSession()