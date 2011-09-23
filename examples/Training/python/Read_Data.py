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
import my_omero_config as conf
from omero.gateway import BlitzGateway
from omero.rtypes import *
conn = BlitzGateway(conf.USERNAME, conf.PASSWORD, host=conf.HOST, port=conf.PORT)
conn.connect()
imageId = 101
datasetId = 101
plateId = 1


# list all Projects available to me, and their Datasets and Images. 

for project in conn.listProjects():
    print project.getName()
    for dataset in project.listChildren():
        print "   ", dataset.getName()
        for image in dataset.listChildren():
            print "      -", image.getName()


# Retrieve the datasets owned by the user currently logged in.

datasets = conn.getObjects("Dataset")
print "\nList Datasets:"
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

screens = conn.getObjects("Screen")
print "\nScreens:"
for s in screens:
    print s.getName(), s.getId()


# Retrieve Wells within a Plate

plate = conn.getObject("Plate", plateId)
print "\nWells in Plate:", plate.getName()
for well in plate.listChildren():
    print "  Well: ", well.row, well.column