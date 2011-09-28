#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2011 University of Dundee & Open Microscopy Environment.
#                    All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
#

"""
This script shows a simple connection to OMERO, printing details of the connection.
NB: You will need to edit the config.py before running.

FOR TRAINING PURPOSES ONLY!
"""

# Connect to the Python Blitz Gateway

# See OmeroPy/Gateway for more info

# import the libraries we need
from omero.gateway import BlitzGateway
# create a connection
HOST = 'localhost'
PORT = 4064
USERNAME = 'username'
PASSWORD = 'passwd'

if __name__ == '__main__':
    conn = BlitzGateway(USERNAME, PASSWORD, host=HOST, port=PORT)
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
