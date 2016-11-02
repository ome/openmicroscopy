#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2016 University of Dundee & Open Microscopy Environment.
#                    All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
#

import sys
import os
sys.path.append(os.path.join('..', 'python'))

"""
FOR TRAINING PURPOSES ONLY!
"""

from omero.gateway import BlitzGateway
import omero
from omero.rtypes import rstring, rdouble, rint
from Parse_OMERO_Properties import USERNAME, PASSWORD, HOST, PORT
from Parse_OMERO_Properties import datasetId

# Create a connection
# =================================================================
conn = BlitzGateway(USERNAME, PASSWORD, host=HOST, port=PORT)
conn.connect()

from random import random
from numpy import array
import math

# The Dataset we want to Analyse
# =================================================================
dataset = conn.getObject("Dataset", datasetId)


# We have a helper function for creating an ROI and linking it to new shapes
def createROI(img):
    # create an ROI, link it to Image
    roi = omero.model.RoiI()
    # use the omero.model.ImageI that underlies the 'image' wrapper
    roi.setImage(img._obj)
    # create an ROI with single line shape
    line = omero.model.LineI()
    line.x1 = rdouble(0)
    line.x2 = rdouble(image.getSizeX())
    line.y1 = rdouble(0)
    line.y2 = rdouble(image.getSizeY())
    line.theZ = rint(img.getSizeZ() / 2)
    line.theT = rint(0)
    line.textValue = rstring("test-Line")
    roi.addShape(line)
    # Save the ROI (saves any linked shapes too)
    return conn.getUpdateService().saveAndReturnObject(roi)

# Create Lines one per image in the dataset
# =================================================================
for image in dataset.listChildren():
    # create an ROI, link it to Image
    createROI(image)

# first create our table...
# columns we want are: imageId, roiId, shapeId, theZ, theT, lineLength,
# shapetext.
columns = [
    omero.grid.LongColumn('imageId', '', []),
    omero.grid.RoiColumn('roidId', '', []),
    omero.grid.LongColumn('shapeId', '', []),
    omero.grid.LongColumn('theZ', '', []),
    omero.grid.LongColumn('theT', '', []),
    omero.grid.DoubleColumn('lineLength', '', []),
    omero.grid.StringColumn('shapeText', '', 64, [])
    ]
# create and initialize the table
table = conn.c.sf.sharedResources().newTable(
    1, "LineLengths%s" % str(random()))
table.initialize(columns)

# make a local array of our data (add it to table in one go)
imageIds = []
roiIds = []
shapeIds = []
theZs = []
theTs = []
lineLengths = []
shapeTexts = []
roiService = conn.getRoiService()
lengthsForImage = []
for image in dataset.listChildren():
    result = roiService.findByImage(image.getId(), None)
    for roi in result.rois:
        for s in roi.copyShapes():
            if type(s) == omero.model.LineI:
                imageIds.append(image.getId())
                roiIds.append(roi.getId().getValue())
                shapeIds.append(s.getId().getValue())
                theZs.append(s.getTheZ().getValue())
                theTs.append(s.getTheT().getValue())
                x1 = s.getX1().getValue()
                x2 = s.getX2().getValue()
                y1 = s.getY1().getValue()
                y2 = s.getY2().getValue()
                x = x1 - x2
                y = y1 - y2
                length = math.sqrt(math.pow(x, 2) + math.pow(y, 2))
                lineLengths.append(length)
                lengthsForImage.append(length)
                if s.getTextValue():
                    shapeTexts.append(s.getTextValue().getValue())
                else:
                    shapeTexts.append("")
    if len(lengthsForImage) == 0:
        print "No lines found on Image:", image.getName()
        continue
    imgAverage = sum(lengthsForImage) / len(lengthsForImage)
    print "Average length of line for Image: %s is %s" % (
        image.getName(), imgAverage)

    # Add the average as an annotation on each image.
    lengthAnn = omero.model.DoubleAnnotationI()
    lengthAnn.setDoubleValue(rdouble(imgAverage))
    lengthAnn.setNs(rstring("imperial.training.demo.lineLengthAverage"))
    link = omero.model.ImageAnnotationLinkI()
    link.setParent(omero.model.ImageI(image.getId(), False))
    link.setChild(lengthAnn)
    conn.getUpdateService().saveAndReturnObject(link)
    lengthsForImage = []    # reset for next image.


# Prepare data for adding to OMERO table.
data = [
    omero.grid.LongColumn('imageId', '', imageIds),
    omero.grid.RoiColumn('roidId', '', roiIds),
    omero.grid.LongColumn('shapeId', '', shapeIds),
    omero.grid.LongColumn('theZ', '', theZs),
    omero.grid.LongColumn('theT', '', theTs),
    omero.grid.DoubleColumn('lineLength', '', lineLengths),
    omero.grid.StringColumn('shapeText', '', 64, shapeTexts),
    ]
table.addData(data)

# get the table as an original file & attach this data to Dataset
orig_file = table.getOriginalFile()
fileAnn = omero.model.FileAnnotationI()
fileAnn.setFile(orig_file)
link = omero.model.DatasetAnnotationLinkI()
link.setParent(omero.model.DatasetI(datasetId, False))
link.setChild(fileAnn)
# conn.getUpdateService().saveAndReturnObject(link)

a = array(lineLengths)
print "std", a.std()
print "mean", a.mean()
print "max", a.max()
print "min", a.min()


# lets retrieve all the lines that are longer than 3 standard deviations above
# mean
limit = a.mean() + (2 * a.std())
print "Retrieving all lines longer than: ", limit
rowCount = table.getNumberOfRows()
queryRows = table.getWhereList(
    "lineLength > %s" % limit, variables={}, start=0, stop=rowCount, step=0)
if len(queryRows) == 0:
    print "No lines found"
else:
    data = table.readCoordinates(queryRows)
    for col in data.columns:
        print "Query Results for Column: ", col.name
        for v in col.values:
            print "   ", v


# Close connection
# ================
# When you are done, close the session to free up server resources.
conn._closeSession()