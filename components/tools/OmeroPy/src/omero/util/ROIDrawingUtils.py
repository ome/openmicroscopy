#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
#
# ------------------------------------------------------------------------------
#  Copyright (C) 2006-2016 University of Dundee. All rights reserved.
#
#
#   This program is free software; you can redistribute it and/or modify
#  it under the terms of the GNU General Public License as published by
#  the Free Software Foundation; either version 2 of the License, or
#  (at your option) any later version.
#  This program is distributed in the hope that it will be useful,
#  but WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#  GNU General Public License for more details.
#
#  You should have received a copy of the GNU General Public License along
#  with this program; if not, write to the Free Software Foundation, Inc.,
#  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
#
# ------------------------------------------------------------------------------

###
#
# ROIDrawingCanvas draws the shapes from the obejct visited.
# These map to the ROI types in omero.model.*
#
# @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
#   <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
# @author   Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
#   <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk
#   </a>
# @version 3.0
# <small>
# (<b>Internal version:</b> $Revision: $Date: $)
# </small>
# @since 3.0-Beta4
#

"""

Example code to draw 10 ellipses randomly on an image::

    import ROI_utils;
    import ROIDrawingUtils;
    from random import randint;

    l = [];
    for i in range(0,10):
        e = ROI_utils.EllipseData(
            ROI_utils.ROICoordinate(),
            randint(100, 300), randint(100, 300),
            randint(20, 50), randint(20, 50))
        l.append(e);
    d = ROIDrawingUtils.DrawingCanvas();
    d.createImage(400,400)
    v = d.drawElements(l);
    d.image.show()


Example code to draw a polyline on an image an display it in PIL::

    try:
        from PIL import Image, ImageDraw # see ticket:2597
    except ImportError:
        import Image, ImageDraw # see ticket:2597

    import ROI_utils
    import ROIDrawingUtils

    drawingCanvas = ROIDrawingUtils.DrawingCanvas();
    points = [10,30, 40, 80, 100, 150]
    polygonData = ROI_utils.PolylineData(ROI_utils.ROICoordinate(), points);
    drawingCanvas.createImage(400,400);
    drawingCanvas.drawElements([polygonData]);
    drawingCanvas.image.show()


"""


try:
    from PIL import Image, ImageDraw  # see ticket:2597
except ImportError:
    import Image
    import ImageDraw  # see ticket:2597


##
# Drawing canvas allows the creation of shapes on an
# image using PIL, the class can be supplied with an
# image and will write on that or can create an image.
# The object will also visit a list of objects supplied
# and draw their respective shapes if they accept the
# DrawingCanvas visior.
#
class DrawingCanvas:

    ##
    # Create the default object.
    #

    def __init__(self):
        self.width = 0
        self.height = 0
        self.image = None
        self.draw = None

    ##
    # Create a new image to draw on with width, height
    # and background colour (0,0,0,0)
    # @param width See above.
    # @param height See above.
    def createImage(self, width, height):
        self.image = Image.new('RGBA', (width, height), (0, 0, 0, 0))
        self.width = width
        self.height = height

    ##
    # Set the image to draw on as image which has width, height.
    # @param image The image to draw on.
    # @param width See above.
    # @param height See above.
    def setImage(self, image, width, height):
        self.image = image
        self.width = width
        self.height = height

    ##
    # Visit all the elements in the element list and draw their shapes.
    # @param elementList See above.
    def drawElements(self, elementList):
        if(self.draw is None):
            self.draw = ImageDraw.Draw(self.image)
        for element in elementList:
            element.acceptVisitor(self)
        return self.image

    ##
    # Get the fill colour from the ShapeSettings object from it's tuple.
    # @param shapeSetting See above.
    #
    def getFillColour(self, shapeSettings):
        return shapeSettings[1][0]

    ##
    # Get the stroke colour from the ShapeSettings object from it's tuple.
    # @param shapeSetting See above.
    #
    def getStrokeColour(self, shapeSettings):
        return shapeSettings[0][0]

    ##
    # Get the stroke width from the ShapeSettings object from it's tuple.
    # @param shapeSetting See above.
    #
    def getStrokeWidth(self, shapeSettings):
        return shapeSettings[0][1]

    ##
    # Draw an ellipse at (x, y) with major and minor axis (radiusx, radiusy).
    # @param x See above.
    # @param y See above.
    # @param radiusx See above.
    # @param radiusy See above.
    # @param shapeSettings The shapes display properties(colour,etc).
    # @param affineTransform The affine transform that the shape has to
    #                        undergo before drawing.
    def drawEllipse(self, x, y, radiusx, radiusy, shapeSettings,
                    affineTransform=None):
        x0 = x - radiusx
        y0 = y - radiusy
        x1 = x0 + radiusx * 2
        y1 = y0 + radiusy * 2
        fillColour = self.getFillColour(shapeSettings)
        strokeColour = self.getStrokeColour(shapeSettings)
        self.draw.ellipse((x0, y0, x1, y1), fill=fillColour,
                          outline=strokeColour)

    ##
    # Draw a rectangle at (x, y) with width, height (width, height).
    # @param x See above.
    # @param y See above.
    # @param width See above.
    # @param height See above.
    # @param shapeSettings The shapes display properties(colour,etc).
    # @param affineTransform The affine transform that the shape has to
    #                        undergo before drawing.
    def drawRectangle(self, x, y, w, h, shapeSettings, affineTransform=None):
        fillColour = self.getFillColour(shapeSettings)
        strokeColour = self.getStrokeColour(shapeSettings)
        if(affineTransform is None):
            self.draw.rectangle(
                (x, y, w, h), fill=fillColour, outline=strokeColour)
        else:
            im = Image.new('RGBA', (self.width, self.height), (0, 0, 0, 0))
            newDraw = ImageDraw.Draw(im)
            newDraw.rectangle(
                (x, y, w, h), fill=fillColour, outline=strokeColour)
            newImage = im.transform(
                (self.width, self.height), Image.AFFINE, affineTransform)
            self.image.paste(newImage)

    ##
    # Draw an polygon with the points in pointTupleList
    # which are [(x1, y1), (x2, y2)...].
    # @param pointTupleList See above.
    # @param shapeSettings The shapes display properties(colour,etc).
    # @param affineTransform The affine transform that the shape has to
    #                        undergo before drawing.
    def drawPolygon(self, pointTupleList, shapeSettings, affineTransform=None):
        fillColour = self.getFillColour(shapeSettings)
        strokeColour = self.getStrokeColour(shapeSettings)
        if(affineTransform is None):
            self.draw.polygon(
                pointTupleList, fill=fillColour, outline=strokeColour)
        else:
            im = Image.new('RGBA', (self.width, self.height), (0, 0, 0, 0))
            ImageDraw.Draw(im)
            self.draw.polygon(
                pointTupleList, fill=fillColour, outline=strokeColour)
            newImage = im.transform(
                (self.width, self.height), Image.AFFINE, affineTransform)
            self.image.paste(newImage)

    ##
    # Draw a line from (x1, y1) to (x2,y2).
    # @param x1 See above.
    # @param y1 See above.
    # @param x2 See above.
    # @param y2 See above.
    # @param shapeSettings The shapes display properties(colour,etc).
    # @param affineTransform The affine transform that the shape has to
    #                        undergo before drawing.
    def drawLine(self, x1, y1, x2, y2, shapeSettings, affineTransform=None):
        strokeColour = self.getStrokeColour(shapeSettings)
        strokeWidth = self.getStrokeWidth(shapeSettings)
        if(affineTransform is None):
            self.draw.line(
                [(x1, y1), (x2, y2)], fill=strokeColour, width=strokeWidth)
        else:
            im = Image.new('RGBA', (self.width, self.height), (0, 0, 0, 0))
            ImageDraw.Draw(im)
            self.draw.line(
                [(x1, y1), (x2, y2)], fill=strokeColour, width=strokeWidth)
            newImage = im.transform(
                (self.width, self.height), Image.AFFINE, affineTransform)
            self.image.paste(newImage)

    ##
    # Draw an polyline with the points in pointTupleList
    #  which are [(x1, y1), (x2, y2)...].
    # @param pointTupleList See above.
    # @param shapeSettings The shapes display properties(colour,etc).
    # @param affineTransform The affine transform that the shape has to
    #                        undergo before drawing.
    def drawPolyline(self, pointTupleList,
                     shapeSettings, affineTransform=None):
        fillColour = self.getFillColour(shapeSettings)
        strokeColour = self.getStrokeColour(shapeSettings)
        strokeWidth = self.getStrokeWidth(shapeSettings)
        if(affineTransform is None):
            self.draw.line(pointTupleList, fill=fillColour, width=strokeWidth)
        else:
            im = Image.new('RGBA', (self.width, self.height), (0, 0, 0, 0))
            ImageDraw.Draw(im)
            self.draw.line(
                pointTupleList, fill=strokeColour, width=strokeColour)
            newImage = im.transform(
                (self.width, self.height), Image.AFFINE, affineTransform)
            self.image.paste(newImage)

    ##
    # Draw a mask at (x, y) with (width, height).
    # @param x See above.
    # @param y See above.
    # @param width See above.
    # @param height See above.
    # @param bytes The mask in bytes.
    # @param shapeSettings The shapes display properties(colour,etc).
    # @param affineTransform The affine transform that the shape has to
    #                        undergo before drawing.
    def drawMask(self, x, y, width, height, bytes,
                 shapeSettings, affineTransform=None):
        fillColour = self.getFillColour(shapeSettings)
        mask = Image.fromstring('1', (width, height), bytes)
        if(affineTransform is None):
            self.draw.bitmap(x, y, mask, fill=fillColour)
        else:
            im = Image.new('RGBA', (self.width, self.height), (0, 0, 0, 0))
            ImageDraw.Draw(im)
            self.draw.bitmap(x, y, mask, fill=fillColour)
            newImage = im.transform(
                (self.width, self.height), Image.AFFINE, affineTransform)
            self.image.paste(newImage)

    ##
    # Draw text at (x, y).
    # @param x See above.
    # @param y See above.
    # @param text The text to draw.
    # @param shapeSettings The shapes display properties(colour,etc).
    # @param affineTransform The affine transform that the shape has to
    #                        undergo before drawing.
    def drawText(self, x, y, text, shapeSettings, affineTransform=None):
        textColour = self.getStrokeColour(shapeSettings)
        if(affineTransform is None):
            self.draw.text((x, y), text, fill=textColour)
        else:
            im = Image.new('RGBA', (self.width, self.height), (0, 0, 0, 0))
            ImageDraw.Draw(im)
            self.draw.text((x, y), text, fill=textColour)
            newImage = im.transform(
                (self.width, self.height), Image.AFFINE, affineTransform)
            self.image.paste(newImage)
