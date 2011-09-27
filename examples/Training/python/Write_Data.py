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
import os
from omero.gateway import BlitzGateway
from omero.rtypes import *
from Connecting import USERNAME, PASSWORD, HOST, PORT
# create a connection
conn = BlitzGateway(USERNAME, PASSWORD, host=HOST, port=PORT)
conn.connect()
datasetId = 101
projectId = 51
#Specify a local file. E.g. could be result of some analysis
fileToUpload = "path/to/fileToUpload.txt"   # This file should already exist
downloadFileName = "path/to/download.txt"

# Create a new Dataset

dataset = omero.model.DatasetI()
dataset.setName(rstring("New Dataset"))
dataset = conn.getUpdateService().saveAndReturnObject(dataset) 
print "New dataset, Id:" , dataset.getId().getValue()

# Link to Project

project = conn.getObject("Project", projectId)
link = omero.model.ProjectDatasetLinkI()
link.setParent(omero.model.ProjectI(project.getId(), False))
link.setChild(dataset)
conn.getUpdateService().saveObject(link)

# Download a file annotation linked to a Project

path = os.path.join(os.path.dirname(__file__), "download")
if not os.path.exists(path):
    os.makedirs(path)
    

print "\nAnnotations on Project:", project.getName()
for ann in project.listAnnotations():
    if isinstance(ann, omero.gateway.FileAnnotationWrapper):
        print "File ID:", ann.getFile().getId(), ann.getFile().getName(), "Size:", ann.getFile().getSize()
        
        file_path = os.path.join(path, ann.getFile().getName())

        f = open(str(file_path), 'w')
        print "\nDownloading file to", file_path, "..."
        try:
            for chunk in ann.getFileInChunks():
                f.write(chunk)
        finally:
            f.close()
            print "File downloaded!"

#How to create a file annotation and link to a Dataset

dataset = conn.getObject("Dataset", datasetId)
# create the original file and file annotation (uploads the file etc.)
namespace = "omero.training.write_data"
print "\nCreating an OriginalFile and FileAnnotation"
fileAnn = conn.createFileAnnfromLocalFile(fileToUpload, mimetype="text/plain", ns=namespace, desc=None)
print "Attaching FileAnnotation to Dataset: ", "File ID:", fileAnn.getId(), ",", fileAnn.getFile().getName(), "Size:", fileAnn.getFile().getSize()
dataset.linkAnnotation(fileAnn)     # link it to dataset.


#Load all the file annotations with a given namespace

nsToInclude = [namespace]
nsToExclude = []
metadataService = conn.getMetadataService()
annotations = metadataService.loadSpecifiedAnnotations('omero.model.FileAnnotation', nsToInclude, nsToExclude, None)
for ann in annotations:
    print ann.getId().getValue(), ann.file.name.val


# Get all the annotations on an object

print "\nAnnotations on Dataset:", dataset.getName()
for ann in dataset.listAnnotations():
    if isinstance(ann, omero.gateway.FileAnnotationWrapper):
        print "File ID:", ann.getFile().getId(), ann.getFile().getName(), "Path:", ann.getFile().getPath(), "Size:", ann.getFile().getSize()

# Get first annotation with specified namespace and download file

ann = dataset.getAnnotation(namespace)
# download
f = open(downloadFileName, 'w')
print "\nDownloading file to ", downloadFileName
try:
    for chunk in ann.getFileInChunks():
        print "Chunk", chunk
        f.write(chunk)
finally:
    f.close()

# When you're done, close the session to free up server resources. 

conn._closeSession()