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

import os
from omero.gateway import BlitzGateway
from omero.rtypes import *
from Connect_To_OMERO import USERNAME, PASSWORD, HOST, PORT

# Create a connection
# =================================================================
conn = BlitzGateway(USERNAME, PASSWORD, host=HOST, port=PORT)
conn.connect()


# Configuration
# =================================================================
datasetId = 101
projectId = 51
#Specify a local file. E.g. could be result of some analysis
fileToUpload = "path/to/fileToUpload.txt"   # This file should already exist
downloadFileName = "path/to/download.txt"


# Create a new Dataset
# =================================================================
dataset = omero.model.DatasetI()
dataset.setName(rstring("New Dataset"))
dataset = conn.getUpdateService().saveAndReturnObject(dataset)
print "New dataset, Id:", dataset.getId().getValue()


# Link to Project
# =================================================================
project = conn.getObject("Project", projectId)
link = omero.model.ProjectDatasetLinkI()
link.setParent(omero.model.ProjectI(project.getId(), False))
link.setChild(dataset)
conn.getUpdateService().saveObject(link)


# Download a file annotation linked to a Project
# =================================================================
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

# How to create a file annotation and link to a Dataset
# =================================================================
dataset = conn.getObject("Dataset", datasetId)
# create the original file and file annotation (uploads the file etc.)
namespace = "omero.training.write_data"
print "\nCreating an OriginalFile and FileAnnotation"
fileAnn = conn.createFileAnnfromLocalFile(fileToUpload, mimetype="text/plain", ns=namespace, desc=None)
print "Attaching FileAnnotation to Dataset: ", "File ID:", fileAnn.getId(), ",", fileAnn.getFile().getName(), "Size:", fileAnn.getFile().getSize()
dataset.linkAnnotation(fileAnn)     # link it to dataset.


# Load all the file annotations with a given namespace
# =================================================================
nsToInclude = [namespace]
nsToExclude = []
metadataService = conn.getMetadataService()
annotations = metadataService.loadSpecifiedAnnotations('omero.model.FileAnnotation', nsToInclude, nsToExclude, None)
for ann in annotations:
    print ann.getId().getValue(), ann.file.name.val


# Get all the annotations on an object
# =================================================================
print "\nAnnotations on Dataset:", dataset.getName()
for ann in dataset.listAnnotations():
    if isinstance(ann, omero.gateway.FileAnnotationWrapper):
        print "File ID:", ann.getFile().getId(), ann.getFile().getName(), "Path:", ann.getFile().getPath(), "Size:", ann.getFile().getSize()


# Get first annotation with specified namespace and download file
# =================================================================
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


# Close connection:
# =================================================================
# When you're done, close the session to free up server resources.
conn._closeSession()
