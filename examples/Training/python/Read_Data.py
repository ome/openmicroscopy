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


def print_obj(obj, indent=0):
    """
    Helper method to display info about OMERO objects.
    Not all objects will have a "name" or owner field.
    """
    print """%s%s:%s  Name:"%s" (owner=%s)""" % (
        " " * indent,
        obj.OMERO_CLASS,
        obj.getId(),
        obj.getName(),
        obj.getOwnerOmeName())


# List all Projects owned by the user currently logged in
# =======================================================
# By default this returns Projects from all owners across
# all groups. We can filter by group and owner using the
# optional opts dict (new in 5.3.0)
# We also order by name and use 'limit' and 'offset',
# to load the first 5 Projects
print "\nList Projects:"
print "=" * 50
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


# Retrieve 'orphaned' objects
# ===========================
# We can use the 'orphaned' filter to find Datasets, Images
# or Plates that are not in any parent container
print "\nList orphaned Datasets: \n", "=" * 50
datasets = conn.getObjects("Dataset", opts={'orphaned': True})
for dataset in datasets:
    print_obj(dataset)


# Retrieve objects in a container
# ==========================================
# We can filter Images by their parent Dataset
# We can also filter Datasets by 'project', Plates by 'screen',
# Wells by 'plate'
print "\nImages in Dataset:", datasetId, "\n", "=" * 50
for image in conn.getObjects('Image', opts={'dataset': datasetId}):
    print_obj(image)


# Retrieve an image by ID
# =======================
# Pixels and Channels will be loaded automatically as needed
image = conn.getObject("Image", imageId)
print "\nImage:%s" % imageId
print "=" * 50
print image.getName(), image.getDescription()
# Retrieve information about an image.
print " X:", image.getSizeX()
print " Y:", image.getSizeY()
print " Z:", image.getSizeZ()
print " C:", image.getSizeC()
print " T:", image.getSizeT()
# List Channels (loads the Rendering settings to get channel colors)
for channel in image.getChannels():
    print 'Channel:', channel.getLabel(),
    print 'Color:', channel.getColor().getRGB()
    print 'Lookup table:', channel.getLut()
    print 'Is reverse intensity?', channel.isReverseIntensity()

# render the first timepoint, mid Z section
z = image.getSizeZ() / 2
t = 0
rendered_image = image.renderImage(z, t)
# renderedImage.show()               # popup (use for debug only)
# renderedImage.save("test.jpg")     # save in the current folder


# Get Pixel Sizes for the above Image
# ===================================
size_x = image.getPixelSizeX()       # e.g. 0.132
print " Pixel Size X:", size_x
if size_x:
    # Units support, new in OMERO 5.1.0
    size_x_obj = image.getPixelSizeX(units=True)
    print "Size X:", size_x_obj.getValue(), "(%s)" % size_x_obj.getSymbol()
    # To get the size with different units, e.g. Angstroms
    size_x_ang = image.getPixelSizeX(units="ANGSTROM")
    print "Size X:", size_x_ang.getValue(), "(%s)" % size_x_ang.getSymbol()


# Retrieve Screening data
# =======================
print "\nList Screens:"
print "=" * 50
for screen in conn.getObjects("Screen"):
    print_obj(screen)
    for plate in screen.listChildren():
        print_obj(plate, 2)


# Retrieve Wells and Images within a Plate
# ========================================
if plateId >= 0:
    print "\nPlate:%s" % plateId
    print "=" * 50
    plate = conn.getObject("Plate", plateId)
    print "\nNumber of fields:", plate.getNumberOfFields()
    print "\nGrid size:", plate.getGridSize()
    print "\nWells in Plate:", plate.getName()
    for well in plate.listChildren():
        index = well.countWellSample()
        print "  Well: ", well.row, well.column, " Fields:", index
        for index in xrange(0, index):
            print "    Image: ", \
                well.getImage(index).getName(),\
                well.getImage(index).getId()

# Close connection
# ================
# When you are done, close the session to free up server resources.
conn.close()
