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

# Configuration
# =================================================================
# These values will be imported by all the other training scripts.
HOST = 'localhost'
PORT = 4064
USERNAME = 'username'
PASSWORD = 'passwd'

from omero.gateway import BlitzGateway

if __name__ == '__main__':
    """
    NB: This block is only run when calling this file directly
    and not when imported.
    """

    # Connect to the Python Blitz Gateway
    # =============================================================
    # Make a simple connection to OMERO, printing details of the
    # connection. See OmeroPy/Gateway for more info
    conn = BlitzGateway(USERNAME, PASSWORD, host=HOST, port=PORT)
    connected = conn.connect()


    # Check if you are connected.
    # =============================================================
    if not connected:
        import sys
        sys.stderr.write("Error: Connection not available, please check your user name and password.\n")
        sys.exit(1)


    # Using secure connection.
    # =============================================================
    # By default, once we have logged in, data transfer is not encrypted (faster)
    # To use a secure connection, call setSecure(True):

    # conn.setSecure(True)         # <--------- Uncomment this


    # Current session details
    # =============================================================
    # By default, you will have logged into your 'current' group in OMERO. This
    # can be changed by switching group in the OMERO.insight or OMERO.web clients.

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
    for exp in conn.listColleagues():
        print "   ID:", exp.getId(), exp.getOmeName(), " Name:", exp.getFullName()

    print "Owner of:"
    for g in conn.listOwnedGroups():
        print "   ID:", g.getName(), " Name:", g.getId()

    # The 'context' of our current session
    ctx = conn.getEventContext()
    # print ctx     # for more info


    # Close connection:
    # =================================================================
    # When you are done, close the session to free up server resources.
    conn._closeSession()
