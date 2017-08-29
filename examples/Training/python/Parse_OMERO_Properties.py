#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2015 University of Dundee & Open Microscopy Environment.
#                    All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
#

"""
FOR TRAINING PURPOSES ONLY!
"""

import omero

client = omero.client()

omeroProperties = client.getProperties().getPropertiesForPrefix('omero')

# Set encoding so that print statements or str(unicode) don't fail
# in testing. See https://github.com/openmicroscopy/openmicroscopy/pull/5400
import sys
reload(sys)
sys.setdefaultencoding('utf-8')

# Configuration
# =================================================================
# These values will be imported by all the other training scripts.
HOST = omeroProperties.get('omero.host', 'localhost')
PORT = omeroProperties.get('omero.port', 4064)
USERNAME = omeroProperties.get('omero.user')
PASSWORD = omeroProperties.get('omero.pass')
OMERO_WEB_HOST = omeroProperties.get('omero.webhost')
SERVER_NAME = omeroProperties.get('omero.servername')
projectId = omeroProperties.get('omero.projectid')
datasetId = omeroProperties.get('omero.datasetid')
imageId = omeroProperties.get('omero.imageid')
plateId = omeroProperties.get('omero.plateid')
