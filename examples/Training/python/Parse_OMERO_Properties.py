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
HOST = "localhost"#omeroProperties.get('omero.host', 'localhost')
PORT = omeroProperties.get('omero.port', 4064)
USERNAME = "root"#omeroProperties.get('omero.user')
PASSWORD = "omero"#omeroProperties.get('omero.pass')
OMERO_WEB_HOST = "http://localhost"
projectId = 1#omeroProperties.get('omero.projectid')
datasetId = 1#omeroProperties.get('omero.datasetid')
imageId = 1#omeroProperties.get('omero.imageid')
plateId = omeroProperties.get('omero.plateid')
