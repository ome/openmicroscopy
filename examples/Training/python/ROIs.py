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
from omero.rtypes import rdouble, rint, rstring
from omero.gateway import BlitzGateway
from Parse_OMERO_Properties import USERNAME, PASSWORD, HOST, PORT
from Parse_OMERO_Properties import imageId


# Create a connection
# =================================================================
conn = BlitzGateway(USERNAME, PASSWORD, host=HOST, port=PORT)
conn.connect()
updateService = conn.getUpdateService()


# Create ROI.
# =================================================================
# We are using the core Python API and omero.model objects here, since ROIs
# are not yet supported in the Python Blitz Gateway.
#
# In this example, we create an ROI with a rectangular shape and attach it to
# an image.
x = 50
y = 200
width = 100
height = 50
image = conn.getObject("Image", imageId)
theZ = image.getSizeZ() / 2
theT = 0
print ("Adding a rectangle at theZ: %s, theT: %s, X: %s, Y: %s, width: %s,"
       " height: %s" % (theZ, theT, x, y, width, height))

# create an ROI, link it to Image
roi = omero.model.RoiI()
# use the omero.model.ImageI that underlies the 'image' wrapper
roi.setImage(image._obj)

# create a rectangle shape and add to ROI
rect = omero.model.RectI()
rect.x = rdouble(x)
rect.y = rdouble(y)
rect.width = rdouble(width)
rect.height = rdouble(height)
rect.theZ = rint(theZ)
rect.theT = rint(theT)
rect.textValue = rstring("test-Rectangle")
roi.addShape(rect)

# create an Ellipse shape and add to ROI
ellipse = omero.model.EllipseI()
ellipse.cx = rdouble(y)
ellipse.cy = rdouble(x)
ellipse.rx = rdouble(width)
ellipse.ry = rdouble(height)
ellipse.theZ = rint(theZ)
ellipse.theT = rint(theT)
ellipse.textValue = rstring("test-Ellipse")
roi.addShape(ellipse)

# create a line shape and add to ROI
line = omero.model.LineI()
line.setX1(rdouble(x))
line.setX2(rdouble(x+width))
line.setY1(rdouble(y))
line.setY2(rdouble(y+height))
line.theZ = rint(theZ)
line.theT = rint(theT)
line.textValue = rstring("test-Line")
roi.addShape(line)

# create a point shape and add to ROI
point = omero.model.PointI()
point.setX(rdouble(x))
point.setY(rdouble(y))
point.theZ = rint(theZ)
point.theT = rint(theT)
point.textValue = rstring("test-Point")


# Save the ROI (saves any linked shapes too)
r = updateService.saveAndReturnObject(roi)


# Retrieve ROIs linked to an Image.
# =================================================================
roiService = conn.getRoiService()
result = roiService.findByImage(imageId, None)
for roi in result.rois:
    print "ROI:  ID:", roi.getId().getValue()
    for s in roi.copyShapes():
        shape = {}
        shape['id'] = s.getId().getValue()
        shape['theT'] = s.getTheT().getValue()
        shape['theZ'] = s.getTheZ().getValue()
        if s.getTextValue():
            shape['textValue'] = s.getTextValue().getValue()
        if type(s) == omero.model.RectI:
            shape['type'] = 'Rectangle'
            shape['x'] = s.getX().getValue()
            shape['y'] = s.getY().getValue()
            shape['width'] = s.getWidth().getValue()
            shape['height'] = s.getHeight().getValue()
        elif type(s) == omero.model.EllipseI:
            shape['type'] = 'Ellipse'
            shape['cx'] = s.getCx().getValue()
            shape['cy'] = s.getCy().getValue()
            shape['rx'] = s.getRx().getValue()
            shape['ry'] = s.getRy().getValue()
        elif type(s) == omero.model.PointI:
            shape['type'] = 'Point'
            shape['cx'] = s.getCx().getValue()
            shape['cy'] = s.getCy().getValue()
        elif type(s) == omero.model.LineI:
            shape['type'] = 'Line'
            shape['x1'] = s.getX1().getValue()
            shape['x2'] = s.getX2().getValue()
            shape['y1'] = s.getY1().getValue()
            shape['y2'] = s.getY2().getValue()
        elif type(s) in (
                omero.model.MaskI, omero.model.LabelI, omero.model.PolygonI):
            print type(s), " Not supported by this code"
        # Do some processing here, or just print:
        print "   Shape:",
        for key, value in shape.items():
            print "  ", key, value,
        print ""


# Remove shape from ROI
# =================================================================
result = roiService.findByImage(imageId, None)
for roi in result.rois:
    for s in roi.copyShapes():
        # Find and remove the Shape we added above
        if s.getTextValue() and s.getTextValue().getValue() == "test-Ellipse":
            print "Removing Shape from ROI..."
            roi.removeShape(s)
            roi = updateService.saveAndReturnObject(roi)


# Close connection:
# =================================================================
# When you are done, close the session to free up server resources.
conn._closeSession()
