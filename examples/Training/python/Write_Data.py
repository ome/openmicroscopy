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
from Parse_OMERO_Properties import USERNAME, PASSWORD, HOST, PORT
from Parse_OMERO_Properties import projectId

"""
start-code
"""

# Create a connection
# ===================
conn = BlitzGateway(USERNAME, PASSWORD, host=HOST, port=PORT)
conn.connect()


# Create a new Dataset
# ====================
dataset_obj = omero.model.DatasetI()
dataset_obj.setName(rstring("New Dataset"))
dataset_obj = conn.getUpdateService().saveAndReturnObject(dataset_obj)
dataset_id = dataset_obj.getId().getValue()
print "New dataset, Id:", dataset_id


# Link to Project
# ===============
project = conn.getObject("Project", projectId)
if project is None:
    import sys
    sys.stderr.write("Error: Object does not exist.\n")
    sys.exit(1)
link = omero.model.ProjectDatasetLinkI()
link.setParent(omero.model.ProjectI(project.getId(), False))
link.setChild(dataset_obj)
conn.getUpdateService().saveObject(link)


# Annotate Project with a new 'tag'
# =================================
tag_ann = omero.gateway.TagAnnotationWrapper(conn)
tag_ann.setValue("New Tag")
tag_ann.save()
project = conn.getObject("Project", projectId)
project.linkAnnotation(tag_ann)


# Create a 'map' annotation (list of key: value pairs)
# ====================================================
key_value_data = [["Drug Name", "Monastrol"], ["Concentration", "5 mg/ml"]]
map_ann = omero.gateway.MapAnnotationWrapper(conn)
# Use 'client' namespace to allow editing in Insight & web
namespace = omero.constants.metadata.NSCLIENTMAPANNOTATION
map_ann.setNs(namespace)
map_ann.setValue(key_value_data)
map_ann.save()
project = conn.getObject("Project", projectId)
# NB: only link a client map annotation to a single object
project.linkAnnotation(map_ann)


# How to create a file annotation and link to a Dataset
# =====================================================
dataset = conn.getObject("Dataset", dataset_id)
# Specify a local file e.g. could be result of some analysis
file_to_upload = "README.txt"   # This file should already exist
with open(file_to_upload, 'w') as f:
    f.write('annotation test')
# create the original file and file annotation (uploads the file etc.)
namespace = "imperial.training.demo"
print "\nCreating an OriginalFile and FileAnnotation"
file_ann = conn.createFileAnnfromLocalFile(
    file_to_upload, mimetype="text/plain", ns=namespace, desc=None)
print "Attaching FileAnnotation to Dataset: ", "File ID:", file_ann.getId(), \
    ",", file_ann.getFile().getName(), "Size:", file_ann.getFile().getSize()
dataset.linkAnnotation(file_ann)     # link it to dataset.
os.remove(file_to_upload)

# Download a file annotation linked to a Dataset
# ==============================================
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
# ====================================================
ns_to_include = [namespace]
ns_to_exclude = []
metadataService = conn.getMetadataService()
annotations = metadataService.loadSpecifiedAnnotations(
    'omero.model.FileAnnotation', ns_to_include, ns_to_exclude, None)
for ann in annotations:
    print ann.getId().getValue(), ann.getFile().getName().getValue()


# Get first annotation with specified namespace
# =============================================
ann = dataset.getAnnotation(namespace)
print "Found Annotation with namespace: ", ann.getNs()


# Close connection
# ================
# When you are done, close the session to free up server resources.
conn.close()
