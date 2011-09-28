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
from omero.gateway import BlitzGateway
from omero.rtypes import *
from Connect_To_OMERO import USERNAME, PASSWORD, HOST, PORT
# create a connection
conn = BlitzGateway(USERNAME, PASSWORD, host=HOST, port=PORT)
conn.connect()
datasetId = 101
projectId = 51

# Create a new Dataset

dataset = omero.model.DatasetI()
dataset.setName(rstring("New Dataset"))
dataset = conn.getUpdateService().saveAndReturnObject(dataset) 
print "New dataset, Id:" , dataset.getId().getValue()

dataset2 = omero.model.DatasetI()
dataset2.setName(rstring("New Dataset2"))
dataset2 = conn.getUpdateService().saveAndReturnObject(dataset2) 
print "New dataset 2, Id:" , dataset2.getId().getValue()

# How to annotate multiple Datasets

# you define objects you want to tag
dataset_list = [dataset, dataset2]

# create a tag
tag = omero.model.TagAnnotationI()
tag.setTextValue(rstring("new tag"))

# build list of links
link_list = list()
for ds in dataset_list:
    link = omero.model.DatasetAnnotationLinkI()
    link.setParent(ds)
    link.setChild(tag)
    link_list.append(link)
conn.getUpdateService().saveArray(link_list)

# When you're done, close the session to free up server resources. 

conn._closeSession()