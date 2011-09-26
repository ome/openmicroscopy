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
from omero.gateway import BlitzGateway
from omero.rtypes import *
from Connecting import USERNAME, PASSWORD, HOST, PORT
# create a connection
conn = BlitzGateway(USERNAME, PASSWORD, host=HOST, port=PORT)
conn.connect()
updateService = conn.getUpdateService()
imageId = 101
x = 50
y = 200
width = 3
height = 2

# Create ROI. 

# We are using the core Python API and omero.model objects here, since ROIs are not
# yet supported in the Python Blitz Gateway.

#In this example, we create an ROI with a rectangular shape and attach it to an image. 

image = conn.getObject("Image", imageId)
theZ = image.getSizeZ()/2
theT = 0
print "Adding a rectangle at theZ: %s, theT: %s, X: %s, Y: %s, width: %s, height: %s" % (theZ,theT,x,y,width,height)
# create an ROI, link it to Image
roi = omero.model.RoiI()
roi.setImage(image._obj)    # use the omero.model.ImageI that underlies the 'image' wrapper
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
# create an Ellips shape and add to ROI
ellipse = omero.model.EllipseI()
ellipse.cx = rdouble(y)
ellipse.cy = rdouble(x)
ellipse.rx = rdouble(width)
ellipse.ry = rdouble(height)
ellipse.theZ = rint(theZ)
ellipse.theT = rint(theT)
ellipse.textValue = rstring("test-Ellipse")
roi.addShape(ellipse)
# Save the ROI (saves any linked shapes too)
r = updateService.saveAndReturnObject(roi) 

#Retrieve ROIs linked to an Image.

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
        elif type(s) in (omero.model.MaskI, omero.model.LabelI, omero.model.PolygonI):
            print type(s), " Not supported by this code"
        # Do some processing here, or just print:
        print "   Shape:",
        for key, value in shape.items():
            print "  ", key, value,
        print ""


# Remove shape from ROI

result = roiService.findByImage(imageId, None)
for roi in result.rois:
    for s in roi.copyShapes():
        # Find and remove the Shape we added above
        if s.getTextValue() and s.getTextValue().getValue() == "test-Ellipse":
            print "Removing Shape from ROI..."
            roi.removeShape(s)
            roi = updateService.saveAndReturnObject(roi) 