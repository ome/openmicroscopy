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
from __future__ import division
from __future__ import print_function

from builtins import range
import omero
from omero.gateway import BlitzGateway
from Parse_OMERO_Properties import USERNAME, PASSWORD, HOST, PORT
from Parse_OMERO_Properties import datasetId, imageId, plateId

"""
start-code
"""

# Create a connection
# ===================
conn = BlitzGateway(USERNAME, PASSWORD, host=HOST, port=PORT)
conn.connect()
datasetId = int(datasetId)


def print_obj(obj, indent=0):
    """
    Helper method to display info about OMERO objects.
    Not all objects will have a "name" or owner field.
    """
    print("""%s%s:%s  Name:"%s" (owner=%s)""" % (
        " " * indent,
        obj.OMERO_CLASS,
        obj.getId(),
        obj.getName(),
        obj.getOwnerOmeName()))


# List all Projects owned by the user currently logged in
# =======================================================
# By default this returns Projects from all owners across
# all groups. We can filter by group and owner using the
# optional opts dict (new in 5.3.0)
# We also order by name and use 'limit' and 'offset',
# to load the first 5 Projects
print("\nList Projects:")
print("=" * 50)
my_exp_id = conn.getUser().getId()
default_group_id = conn.getEventContext().groupId
for project in conn.getObjects("Project", opts={'owner': my_exp_id,
                                                'group': default_group_id,
                                                'order_by': 'lower(obj.name)',
                                                'limit': 5, 'offset': 0}):
    print_obj(project)
    assert project.getDetails().getOwner().id == my_exp_id
    # We can get Datasets with listChildren, since we have the Project already.
    # Or conn.getObjects("Dataset", opts={'project', id}) if we have Project ID
    for dataset in project.listChildren():
        print_obj(dataset, 2)
        for image in dataset.listChildren():
            print_obj(image, 4)

# Find objects by ID. NB: getObjects() returns a generator, not a list
datasets = conn.getObjects("Dataset", [datasetId, datasetId+1])

# Get a single object by ID.
img = conn.getObject("Image", imageId)
img_name = img.getName()
# Can use "Annotation" for all types of annotations by ID
annotation = conn.getObject("Annotation", 1)

# Find an Object by attribute. E.g. 'name'
images = conn.getObjects("Image", attributes={"name": img_name})


# Get different types of Annotations
# ===========================

# Supported types are: ``tagannotation``, ``longannotation``,
# ``booleanannotation``, ``fileannotation``, ``doubleannotation``,
# ``termannotation``, ``timestampannotation``, ``mapannotation``

# List All Tags that you have permission to access
conn.getObjects("TagAnnotation")

# Find Tags with a known text value
tags = conn.getObjects("TagAnnotation", attributes={"textValue": "OK"})


# Retrieve 'orphaned' objects
# ===========================
# We can use the 'orphaned' filter to find Datasets, Images
# or Plates that are not in any parent container
print("\nList orphaned Datasets: \n", "=" * 50)
datasets = conn.getObjects("Dataset", opts={'orphaned': True})
for dataset in datasets:
    print_obj(dataset)


# Retrieve objects in a container
# ==========================================
# We can filter Images by their parent Dataset
# We can also filter Datasets by 'project', Plates by 'screen',
# Wells by 'plate'
print("\nImages in Dataset:", datasetId, "\n", "=" * 50)
for image in conn.getObjects('Image', opts={'dataset': datasetId}):
    print_obj(image)


# Retrieve an image by ID
# =======================
# Pixels and Channels will be loaded automatically as needed
image = conn.getObject("Image", imageId)
print("\nImage:%s" % imageId)
print("=" * 50)
print(image.getName(), image.getDescription())
# Retrieve information about an image.
print(" X:", image.getSizeX())
print(" Y:", image.getSizeY())
print(" Z:", image.getSizeZ())
print(" C:", image.getSizeC())
print(" T:", image.getSizeT())
# List Channels (loads the Rendering settings to get channel colors)
for channel in image.getChannels():
    print('Channel:', channel.getLabel(), end=' ')
    print('Color:', channel.getColor().getRGB())
    print('Lookup table:', channel.getLut())
    print('Is reverse intensity?', channel.isReverseIntensity())

# render the first timepoint, mid Z section
z = image.getSizeZ()// 2
t = 0
rendered_image = image.renderImage(z, t)
# renderedImage.show()               # popup (use for debug only)
# renderedImage.save("test.jpg")     # save in the current folder


# Get Pixel Sizes for the above Image
# ===================================
size_x = image.getPixelSizeX()       # e.g. 0.132
print(" Pixel Size X:", size_x)
if size_x:
    # Units support, new in OMERO 5.1.0
    size_x_obj = image.getPixelSizeX(units=True)
    print("Size X:", size_x_obj.getValue(), "(%s)" % size_x_obj.getSymbol())
    # To get the size with different units, e.g. Angstroms
    size_x_ang = image.getPixelSizeX(units="ANGSTROM")
    print("Size X:", size_x_ang.getValue(), "(%s)" % size_x_ang.getSymbol())


# Retrieve Screening data
# =======================
print("\nList Screens:")
print("=" * 50)
for screen in conn.getObjects("Screen"):
    print_obj(screen)
    for plate in screen.listChildren():
        print_obj(plate, 2)


# Retrieve Wells and Images within a Plate
# ========================================
if int(plateId) >= 0:
    print("\nPlate:%s" % plateId)
    print("=" * 50)
    plate = conn.getObject("Plate", plateId)
    print("\nNumber of fields:", plate.getNumberOfFields())
    print("\nGrid size:", plate.getGridSize())
    print("\nWells in Plate:", plate.getName())
    for well in plate.listChildren():
        index = well.countWellSample()
        print("  Well: ", well.row, well.column, " Fields:", index)
        for index in range(0, index):
            print("    Image: ",
                  well.getImage(index).getName(),
                  well.getImage(index).getId())


# List all annotations on an object. Filter for Tags and get textValue**

for ann in image.listAnnotations():
    print(ann.getId(), ann.OMERO_TYPE)
    print(" added by ", ann.link.getDetails().getOwner().getOmeName())
    if ann.OMERO_TYPE == omero.model.TagAnnotationI:
        print("Tag value:", ann.getTextValue())

# Get Links between Objects and Annotations
# Find Images linked to Annotation(s), unlink Images from these annotations
# and link them to another Tag Annotation
annotation_ids = [1, 2, 3]
tag_id = 4
for link in conn.getAnnotationLinks('Image', ann_ids=annotation_ids):
    print("Image ID:", link.getParent().id)
    print("Annotation ID:", link.getChild().id)
    # Update the child of the underlying omero.model.ImageAnnotationLinkI
    link._obj.child = omero.model.TagAnnotationI(tag_id, False)
    link.save()

# Find Annotations linked to Object(s), filter by namespace (optional)
for link in conn.getAnnotationLinks('Image', parent_ids=[1,2], ns="test.namespace"):
    print("Annotation ID:", link.getChild().id)


# Close connection
# ================
# When you are done, close the session to free up server resources.
conn.close()
