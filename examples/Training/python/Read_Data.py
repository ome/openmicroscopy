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

from omero.gateway import BlitzGateway
from Connect_To_OMERO import USERNAME, PASSWORD, HOST, PORT
# create a connection
conn = BlitzGateway(USERNAME, PASSWORD, host=HOST, port=PORT)
conn.connect()
imageId = 101
datasetId = 101
plateId = 1


# list all Projects available to me, and their Datasets and Images. 
print "\nList Projects:"
for project in conn.listProjects():
    print project.getName(), project.getOwnerOmeName()
    for dataset in project.listChildren():
        print "   ", dataset.getName()
        for image in dataset.listChildren():
            print "      -", image.getName()


# Retrieve the datasets owned by the user currently logged in.

print "\nList Datasets:"
datasets = conn.getObjects("Dataset")
for d in datasets:
    print d.getName(), d.getOwnerOmeName()


# Retrieve the images contained in a dataset.

dataset = conn.getObject("Dataset", datasetId)
print "\nImages in Dataset:", dataset.getName()
for i in dataset.listChildren():
    print i.getName(), i.getId()


# Retrieve an image by Image ID.

image = conn.getObject("Image", imageId)
print "\nImage:"
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
#renderedImage.show()               # popup (use for debug only)
#renderedImage.save("test.jpg")     # save in the current folder


# Retrieve Screening data
print "\nList Screens:"
for screen in conn.getObjects("Screen"):
    print screen.getName(), screen.getOwnerOmeName()
    for plate in screen.listChildren():
        print "   ", plate.getName()
        
# Retrieve Wells within a Plate

plate = conn.getObject("Plate", plateId)
print "\nNumber of fields:", plate.getNumberOfFields()
print "\nGrid size:", plate.getGridSize()
print "\nWells in Plate:", plate.getName()
for well in plate.listChildren():
    index = well.countWellSample()
    print "  Well: ", well.row, well.column, " Fields:", index
    for index in xrange(0,index):
        print "    Image: ", well.getImage(index).getName(), well.getImage(index).getId()

# Close connection

# When you're done, close the session to free up server resources. 

conn._closeSession()
