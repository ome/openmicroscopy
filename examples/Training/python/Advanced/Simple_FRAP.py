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
from omero.rtypes import rint, rlong
from omero.gateway import BlitzGateway
from Parse_OMERO_Properties import USERNAME, PASSWORD, HOST, PORT

# Create a connection
# =================================================================
conn = BlitzGateway(USERNAME, PASSWORD, host=HOST, port=PORT)
conn.connect()


# Configuration
# =================================================================
imageId = 67


# The Image we want to Analyse
# =================================================================
image = conn.getObject('Image', imageId)


# To keep things simple, we'll work with a single Ellipse per T
# =================================================================
def getEllipses(conn, imageId):
    """
    Returns the a dict of tIndex: {'cx':cx, 'cy':cy, 'rx':rx, 'ry':ry, 'z':z}
    NB: Assume only 1 ellipse per time point

    @param conn:    BlitzGateway connection
    @param imageId:     Image ID
    """

    ellipses = {}
    result = conn.getRoiService().findByImage(
        imageId, None, conn.SERVICE_OPTS)

    for roi in result.rois:
        for shape in roi.copyShapes():
            if type(shape) == omero.model.EllipseI:
                cx = int(shape.getCx().getValue())
                cy = int(shape.getCy().getValue())
                rx = int(shape.getRx().getValue())
                ry = int(shape.getRy().getValue())
                z = int(shape.getTheZ().getValue())
                t = int(shape.getTheT().getValue())
                ellipses[t] = {'cx': cx, 'cy': cy, 'rx': rx, 'ry': ry, 'z': z}
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
        xStart = cx - rx
        xEnd = cx + rx
        yStart = cy - ry
        yEnd = cy + ry
        width = rx * 2
        height = ry * 2

        # get pixel data for the 'tile'
        tileData = image.getPrimaryPixels().getTile(
            theZ=e['z'], theC=0, theT=t, tile=(xStart, yStart, width, height))

        # find the pixels within the ellipse
        pixelValues = []
        for x in range(xStart, xEnd):
            for y in range(yStart, yEnd):
                dx = x - e['cx']
                dy = y - e['cy']
                r = float(dx*dx)/float(rx*rx) + float(dy*dy)/float(ry*ry)
                if r <= 1:
                    pixelValues.append(tileData[dx][dy])
        # get the average intensity
        average = sum(pixelValues)/len(pixelValues)
        data[t] = average
    return data


def getTimes(conn, image, theC=0):
    """
    Get a dict of tIndex:time (seconds) for the first plane (Z = 0) at
    each time-point for the defined image and Channel.

    @param conn:        BlitzGateway connection
    @param image:       ImageWrapper
    @return:            A map of tIndex: timeInSecs
    """

    queryService = conn.getQueryService()
    pixelsId = image.getPixelsId()

    params = omero.sys.ParametersI()
    params.add("theC", rint(theC))
    params.add("theZ", rint(0))
    params.add("pixelsId", rlong(pixelsId))

    query = ("from PlaneInfo as Info where Info.theZ=:theZ"
             " and Info.theC=:theC and pixels.id=:pixelsId")
    infoList = queryService.findAllByQuery(query, params, conn.SERVICE_OPTS)

    timeMap = {}
    for info in infoList:
        tIndex = info.theT.getValue()
        time = info.deltaT.getValue()
        timeMap[tIndex] = time
    return timeMap

# Get dictionary of tIndex:ellipse
ellipses = getEllipses(conn, imageId)
# Get dictionary of tIndex:averageIntensity
intensityData = getEllipseData(image, ellipses)

# Get dictionary of tIndex:timeStamp (secs)
timeValues = getTimes(conn, image)


# We now have all the Data we need from OMERO

# create lists of times (secs) and intensities...
timeList = []
valueList = []

# ...Ordered by tIndex
for t in range(image.getSizeT()):
    if t in intensityData:
        timeList.append(timeValues[t])
        valueList.append(intensityData[t])

print "Analysing pixel values for %s time points" % len(timeList)

# Find the bleach intensity & time
bleachValue = min(valueList)
bleachTindex = valueList.index(bleachValue)
bleachTime = timeList[bleachTindex]
preBleachValue = valueList[bleachTindex-1]

print "Bleach at tIndex: %s, TimeStamp: %0.2f seconds" % (
    bleachTindex, bleachTime)
print "Before Bleach: %0.2f, After Bleach: %0.2f" % (
    preBleachValue, bleachValue)

# Use last timepoint for max recovery
recoveryValue = valueList[-1]
endTimepoint = timeList[-1]
mobileFraction = (recoveryValue - bleachValue)/(preBleachValue - bleachValue)

print "Recovered to: %0.2f, after %0.2f seconds" % (
    recoveryValue, endTimepoint)
print "Mobile Fraction: %0.2f" % mobileFraction

halfRecovery = (recoveryValue + bleachValue)/2

# quick & dirty - pick the first timepoint where we exceed half recovery
# just the values & times after bleach time
recoveryValues = valueList[bleachTindex:]
recoveryTimes = timeList[bleachTindex:]
for t, v in zip(recoveryTimes, recoveryValues):
    if v >= halfRecovery:
        tHalf = t - bleachTime
        break

print "tHalf: %0.2f seconds" % tHalf


csvLines = [
    "Time (secs)," + ",".join([str(t) for t in timeList]),
    "\n",
    "Average pixel value," + ",".join([str(v) for v in valueList]),
    "\n",
    "tHalf (secs), %0.2f seconds" % tHalf,
    "mobileFraction, %0.2f" % mobileFraction
    ]

f = open("FRAP.csv", "w")
f.writelines(csvLines)
f.close()

# Close connection:
# =================================================================
# When you are done, close the session to free up server resources.
conn._closeSession()
