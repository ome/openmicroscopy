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

# Configuration
# =================================================================
# These values will be imported by all the other training scripts.
HOST = omeroProperties.get('omero.host', 'localhost')
PORT = omeroProperties.get('omero.port', 4064)
USERNAME = omeroProperties.get('omero.user')
PASSWORD = omeroProperties.get('omero.pass')
projectId = omeroProperties.get('omero.projectid')
datasetId = omeroProperties.get('omero.datasetid')
imageId = omeroProperties.get('omero.imageid')
plateId = omeroProperties.get('omero.plateid')
