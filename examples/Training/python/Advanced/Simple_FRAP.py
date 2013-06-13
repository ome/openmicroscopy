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

import omero
from omero.rtypes import rdouble, rint
from omero.gateway import BlitzGateway
from Connect_To_OMERO import USERNAME, PASSWORD, HOST, PORT
import omero.util.figureUtil as figUtil

# Create a connection
# =================================================================
conn = BlitzGateway("will", "ome", host="localhost")
conn.connect()


# Configuration
# =================================================================
imageId = 67


# Create an Image from 2 others
# =================================================================
# Replace one channel with a channel from another image.
image = conn.getObject('Image', imageId)


# To keep things simple, we'll work with a single Ellipse
# =================================================================
def getEllipses(conn, imageId):
    """ 
    Returns the a dict of t: {'cx':cx, 'cy':cy, 'rx':rx, 'ry':ry, 'z':z} 
    NB: Assume only 1 ellipse per time point
    """

    ellipses = {}
    result = conn.getRoiService().findByImage(imageId, None)

    for roi in result.rois:
        shapeMap = {} # map shapes by Time
        roiName = None
        for shape in roi.copyShapes():
            if type(shape) == omero.model.EllipseI:
                cx = int(shape.getCx().getValue())
                cy = int(shape.getCy().getValue())
                rx = int(shape.getRx().getValue())
                ry = int(shape.getRy().getValue())
                z = int(shape.getTheZ().getValue())
                t = int(shape.getTheT().getValue())
                ellipses[t] = {'cx':cx, 'cy':cy, 'rx':rx, 'ry':ry, 'z':z}
    return ellipses


def getEllipseData(image, ellipses):
    """ Returns a dict of t:averageIntensity for all ellipses. 
    
    @param ellipse:     The ellipse defined as a tuple (cx, cy, rx, ry, z, t)
    @returns:           A list of (x,y) points for the ellipse
    """
    data = {}
    for t, e in ellipses.items():
        cx = e['cx']
        cy = e['cy']
        rx = e['rx']
        ry = e['ry']
        # find bounding box of ellipse
        xStart = e['cx'] - e['rx']
        xEnd = e['cx'] + e['rx']
        yStart = e['cy'] - e['ry']
        yEnd = e['cy'] + e['ry']
        width = e['rx'] * 2
        height = e['ry'] * 2

        tileData = image.getPrimaryPixels().getTile(theZ=e['z'], theC=0, theT=t, tile=(xStart, yStart, width, height))
        pixelValues = []
        for x in range(xStart, xEnd):
            for y in range(yStart, yEnd):
                dx = x - e['cx']
                dy = y - e['cy']
                r = float(dx*dx)/float(rx*rx) + float(dy*dy)/float(ry*ry)
                if r <= 1:
                    pixelValues.append(tileData[dx][dy])
        average = sum(pixelValues)/len(pixelValues)
        data[t] = average
    return data


# Get dictionary of t:ellipse
ellipses = getEllipses(conn, imageId)
# Get dictionary of t:averageIntensity
intensityData = getEllipseData(image, ellipses)
timeValues = figUtil.getTimes(conn.getQueryService(), image.getPixelsId(), range(image.getSizeT()))

# create lists of times (secs) and intensities
timeList = []
valueList = []

for t in range(image.getSizeT()):
    if t in ellipses:
        timeList.append( timeValues[t] )
        valueList.append( intensityData[t] )

print timeList
print valueList

# Find the bleach intensity & time
bleachValue = min(valueList)
bleachTindex = valueList.index(bleachValue)
bleachTime = timeList[bleachTindex]
preBleachValue = valueList[bleachTindex-1]

# Use last timepoint for max recovery
recoveryValue = valueList[-1]
recoveryTime = timeList[-1]
mobileFraction = (recoveryValue - bleachValue)/(preBleachValue - bleachValue)

halfRecovery = (recoveryValue + bleachValue)/2

# quick & dirty - pick the first timepoint where we exceed half recovery
recoveryValues = valueList[bleachTindex:]   # just the values & times after bleach time
recoveryTimes = timeList[bleachTindex:]
for t, v in zip(recoveryTimes, recoveryValues):
    print t, v, halfRecovery
    if v >= halfRecovery:
        tHalf = t - bleachTime
        break

print ""
print ",".join([str(v) for v in valueList])

f = open("FRAP.csv", "w")
f.writelines( [ ",".join([str(t) for t in timeList]),
    "\n",
    ",".join([str(v) for v in valueList]) ])
f.close()

print "tHalf: %0.2f seconds" % tHalf
print "mobileFraction: %0.2f" % mobileFraction



# Close connection:
# =================================================================
# When you are done, close the session to free up server resources.
conn._closeSession()
