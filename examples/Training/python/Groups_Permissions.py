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

from omero.gateway import BlitzGateway
from Connect_To_OMERO import USERNAME, PASSWORD, HOST, PORT


# Create a connection
# =================================================================
conn = BlitzGateway(USERNAME, PASSWORD, host=HOST, port=PORT)
conn.connect()


# Configuration
# =================================================================
imageId = 1


# We are logged in to our 'default' group
# =================================================================
group = conn.getGroupFromContext()
print "Current group: ", group.getName()


# Each group has defined Permissions set
# =================================================================
group_perms = group.getDetails().getPermissions()
perm_string = str(group_perms)
permission_names = {'rw----':'PRIVATE',
    'rwr---':'READ-ONLY',
    'rwra--':'READ-ANNOTATE',
    'rwrw--':'READ-WRITE'}  # Not exposed in 4.4.0 clients
print "Permissions: %s (%s)" % (permission_names[perm_string], perm_string)


# By default, any query applies to ALL data that we can access in our Current group.
# =================================================================
# This will be determined by group permissions.
# E.g. in Read-Only or Read-Annotate groups, this will include other users' data
# See http://www.openmicroscopy.org/site/support/omero4/server/permissions/

projects = conn.listProjects()      # may include other users' data
for p in projects:
    print p.getName(), "Owner: ", p.getDetails().getOwner().getFullName()

image = conn.getObject("Image", imageId)     # Will return None if Image is not in current group
print "Image: ", image


# In OMERO-4.4, we added 'cross-group' querying, use '-1'
# =================================================================
conn.SERVICE_OPTS.setOmeroGroup('-1')
image = conn.getObject("Image", imageId)     # Will query across all my groups
print "Image: ", image, 
if image is not None:
    print "Group: ", image.getDetails().getGroup().getName(), 
    print image.details.group.id.val    # access groupId without loading group


# To query only a single group (not necessarily your 'current' group)
# =================================================================
groupId = image.details.group.id.val
conn.SERVICE_OPTS.setOmeroGroup(groupId)   # This is how we 'switch group' in webclient
projects = conn.listProjects()
image = conn.getObject("Image", imageId)
print "Image: ", image,


# Close connection:
# =================================================================
# When you're done, close the session to free up server resources.
conn._closeSession()
