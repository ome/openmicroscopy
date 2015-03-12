#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2014 University of Dundee & Open Microscopy Environment.
#                    All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
#

"""
FOR TRAINING PURPOSES ONLY!
"""

import os
import omero
from omero.rtypes import rstring
from omero.gateway import BlitzGateway
from Connect_To_OMERO import USERNAME, PASSWORD, HOST, PORT


# Create a connection
# =================================================================
conn = BlitzGateway(USERNAME, PASSWORD, host=HOST, port=PORT)
conn.connect()


# Configuration
# =================================================================
projectId = 2
# Specify a local file. E.g. could be result of some analysis
fileToUpload = "README.txt"   # This file should already exist


# Create a new Dataset
# =================================================================
datasetObj = omero.model.DatasetI()
datasetObj.setName(rstring("New Dataset"))
datasetObj = conn.getUpdateService().saveAndReturnObject(datasetObj)
datasetId = datasetObj.getId().getValue()
print "New dataset, Id:", datasetId


# Link to Project
# =================================================================
project = conn.getObject("Project", projectId)
if project is None:
    import sys
    sys.stderr.write("Error: Object does not exist.\n")
    sys.exit(1)
link = omero.model.ProjectDatasetLinkI()
link.setParent(omero.model.ProjectI(project.getId(), False))
link.setChild(datasetObj)
conn.getUpdateService().saveObject(link)


# Annotate Project with a new 'tag'
# =================================================================
tagAnn = omero.gateway.TagAnnotationWrapper(conn)
tagAnn.setValue("New Tag")
tagAnn.save()
project = conn.getObject("Project", projectId)
project.linkAnnotation(tagAnn)


# Create a 'map' annotation (list of key: value pairs)
# =================================================================
keyValueData = [["Drug Name", "Monastrol"],
    ["Concentration", "5 mg/ml"]]
mapAnn = omero.gateway.MapAnnotationWrapper(conn)
# Use 'client' namespace to allow editing in Insight & web
namespace = omero.constants.metadata.NSCLIENTMAPANNOTATION
mapAnn.setNs(namespace)
mapAnn.setValue(keyValueData)
mapAnn.save()
project = conn.getObject("Project", projectId)
# NB: only link a client map annotation to a single object
project.linkAnnotation(mapAnn)


# How to create a file annotation and link to a Dataset
# =================================================================
dataset = conn.getObject("Dataset", datasetId)
# create the original file and file annotation (uploads the file etc.)
namespace = "imperial.training.demo"
print "\nCreating an OriginalFile and FileAnnotation"
fileAnn = conn.createFileAnnfromLocalFile(
    fileToUpload, mimetype="text/plain", ns=namespace, desc=None)
print "Attaching FileAnnotation to Dataset: ", "File ID:", fileAnn.getId(), \
    ",", fileAnn.getFile().getName(), "Size:", fileAnn.getFile().getSize()
dataset.linkAnnotation(fileAnn)     # link it to dataset.


# Download a file annotation linked to a Dataset
# =================================================================
# make a location to download the file. "download" folder.
path = os.path.join(os.path.dirname(__file__), "download")
if not os.path.exists(path):
    os.makedirs(path)

# Go through all the annotations on the Dataset. Download any file annotations
# we find.
print "\nAnnotations on Dataset:", dataset.getName()
for ann in dataset.listAnnotations():
    if isinstance(ann, omero.gateway.FileAnnotationWrapper):
        print "File ID:", ann.getFile().getId(), ann.getFile().getName(), \
            "Size:", ann.getFile().getSize()

        file_path = os.path.join(path, ann.getFile().getName())

        f = open(str(file_path), 'w')
        print "\nDownloading file to", file_path, "..."
        try:
            for chunk in ann.getFileInChunks():
                f.write(chunk)
        finally:
            f.close()
            print "File downloaded!"


# Load all the file annotations with a given namespace
# =================================================================
nsToInclude = [namespace]
nsToExclude = []
metadataService = conn.getMetadataService()
annotations = metadataService.loadSpecifiedAnnotations(
    'omero.model.FileAnnotation', nsToInclude, nsToExclude, None)
for ann in annotations:
    print ann.getId().getValue(), ann.file.name.val


# Get first annotation with specified namespace
# =================================================================
ann = dataset.getAnnotation(namespace)
print "Found Annotation with namespace: ", ann.getNs()


# Close connection:
# =================================================================
# When you are done, close the session to free up server resources.
conn._closeSession()
