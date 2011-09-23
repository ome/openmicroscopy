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
import my_omero_config as conf
from omero.gateway import BlitzGateway
from omero.rtypes import *
conn = BlitzGateway(conf.USERNAME, conf.PASSWORD, host=conf.HOST, port=conf.PORT)
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
r = updateService.saveAndReturnObject(roi) 
# create and save a rectangle shape
rect = omero.model.RectI()
rect.x = rdouble(x)
rect.y = rdouble(y)
rect.width = rdouble(width)
rect.height = rdouble(height)
rect.theZ = rint(theZ)
rect.theT = rint(theT)
# link the rectangle to the ROI and save it 
rect.setRoi(r)
r.addShape(rect)    
sh = updateService.saveAndReturnObject(rect)


#Retrieve ROIs linked to an Image.

roiService = conn.getRoiService()
result = roiService.findByImage(imageId, None)
for roi in result.rois:
    print "ROI:"
    for shape in roi.copyShapes():
        print "  Shape:", shape.__class__.__name__ # E.g. omero.model.RectI
        theZ = shape.getTheZ().getValue()
        theT = shape.getTheT().getValue()
        x = int(shape.getX().getValue())
        y = int(shape.getY().getValue())
        width = int(shape.getWidth().getValue())
        height = int(shape.getHeight().getValue())
        print "  at theZ: %s, theT: %s, X: %s, Y: %s, width: %s, height: %s" % (theZ,theT,x,y,width,height)
        tile = (x, y, width, height)
        print image.getPrimaryPixels().getTile(theZ,0,theT, tile)