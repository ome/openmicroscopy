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
from Connecting import USERNAME, PASSWORD, HOST, PORT
# create a connection
conn = BlitzGateway(USERNAME, PASSWORD, host=HOST, port=PORT)
conn.connect()
dataset_name = "MyDataset"
object_array = list()
for i in xrange(3):
    dataset = omero.model.DatasetI()
    dataset.setName(rstring(dataset_name))
    object_array.append(dataset) 
conn.getUpdateService().saveArray(object_array)
tag_name = "MyTag"
object_array = list()
for i in xrange(3):
    tag = omero.model.TagAnnotationI()
    tag.setTextValue(rstring(tag_name))
    tag.setDescription(rstring("%s %i" % (tag_name,i)))
    object_array.append(tag) 
conn.getUpdateService().saveArray(object_array)

# Find the datasets by name.

datasets = conn.getObjects("Dataset", attributes={'name':dataset_name})
print "\nList Datasets:"
for d in datasets:
    print "ID:", d.getId(), "Name:", d.getName()

# Find the tag by name.

Tags = conn.getObjects("TagAnnotation", attributes={'textValue':tag_name})
print "\nList Tags:"
for t in Tags:
    print "ID:", t.getId(), "Text:", t.getTextValue(), "Desc:", t.getDescription()

# When you're done, close the session to free up server resources. 

conn._closeSession()