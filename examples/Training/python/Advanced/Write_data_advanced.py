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

import omero
from omero.rtypes import rstring
from omero.gateway import BlitzGateway
from Connect_To_OMERO import USERNAME, PASSWORD, HOST, PORT


# Create a connection
# =================================================================
conn = BlitzGateway(USERNAME, PASSWORD, host=HOST, port=PORT)
conn.connect()

# Create a new Dataset
# =================================================================
dataset = omero.model.DatasetI()
dataset.setName(rstring("New Dataset"))
dataset = conn.getUpdateService().saveAndReturnObject(dataset)
print "New dataset, Id:", dataset.getId().getValue()

dataset2 = omero.model.DatasetI()
dataset2.setName(rstring("New Dataset2"))
dataset2 = conn.getUpdateService().saveAndReturnObject(dataset2)
print "New dataset 2, Id:", dataset2.getId().getValue()


# How to annotate multiple Datasets
# =================================================================
# 1. you define objects you want to tag
dataset_list = [dataset, dataset2]

# 2. create a tag
tag = omero.model.TagAnnotationI()
tag.setTextValue(rstring("new tag"))

# 3. build a list of links
link_list = list()
for ds in dataset_list:
    link = omero.model.DatasetAnnotationLinkI()
    link.setParent(ds)
    link.setChild(tag)
    link_list.append(link)

# 4. save the links to the server
conn.getUpdateService().saveArray(link_list)


# Close connection:
# =================================================================
# When you are done, close the session to free up server resources.
conn.close()
