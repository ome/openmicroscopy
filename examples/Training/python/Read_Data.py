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

import omero
from omero.gateway import BlitzGateway
from Parse_OMERO_Properties import USERNAME, PASSWORD, HOST, PORT
from Parse_OMERO_Properties import datasetId, imageId, plateId


# Create a connection
# =================================================================
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


# List all Projects available to me, and their Datasets and Images:
# =================================================================
# The only_owned=True parameter limits the Projects which are returned.
# If the parameter is omitted or the value is False, then all Projects
# visible in the current group are returned.
print "\nList Projects:"
print "=" * 50
my_expId = conn.getUser().getId()
for project in conn.listProjects(my_expId):
    print_obj(project)
    for dataset in project.listChildren():
        print_obj(dataset, 2)
        for image in dataset.listChildren():
            print_obj(image, 4)


# Retrieve the datasets owned by the user currently logged in:
# =================================================================
# Here we create an omero.sys.ParametersI instance which we
# can use to filter the results that are returned. If we did
# not pass the params argument to getObjects, then all Datasets
# in the current group would be returned.
print "\nList Datasets:"
print "=" * 50

params = omero.sys.ParametersI()
params.exp(conn.getUser().getId())  # only show current user's Datasets

datasets = conn.getObjects("Dataset", params=params)
for dataset in datasets:
    print_obj(dataset)


# Retrieve the images contained in a dataset:
# =================================================================
print "\nDataset:%s" % datasetId
print "=" * 50
dataset = conn.getObject("Dataset", datasetId)
print "\nImages in Dataset:", dataset.getName()
for image in dataset.listChildren():
    print_obj(image)


# Retrieve an image by Image ID:
# =================================================================
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
# render the first timepoint, mid Z section
z = image.getSizeZ() / 2
t = 0
renderedImage = image.renderImage(z, t)
# renderedImage.show()               # popup (use for debug only)
# renderedImage.save("test.jpg")     # save in the current folder


# Get Pixel Sizes for the above Image:
# =================================================================
sizeX = image.getPixelSizeX()       # E.g. 0.132
print " Pixel Size X:", sizeX
if sizeX:
    # Units support, new in OMERO 5.1.0
    sizeXobj = image.getPixelSizeX(units=True)
    print " Pixel Size X:", sizeXobj.getValue(), "(%s)" % sizeXobj.getSymbol()
    # To get the size with different units, E.g. Angstroms
    sizeXang = image.getPixelSizeX(units="ANGSTROM")
    print " Pixel Size X:", sizeXang.getValue(), "(%s)" % sizeXang.getSymbol()


# Retrieve Screening data:
# =================================================================
print "\nList Screens:"
print "=" * 50
for screen in conn.getObjects("Screen"):
    print_obj(screen)
    for plate in screen.listChildren():
        print_obj(plate, 2)


# Retrieve Wells and Images within a Plate:
# =================================================================
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

# Close connection:
# =================================================================
# When you are done, close the session to free up server resources.
conn._closeSession()
