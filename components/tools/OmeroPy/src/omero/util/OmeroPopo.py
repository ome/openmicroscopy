#!/usr/bin/env python
# -*- coding: utf-8 -*-

# Copyright (C) 2006-2010 University of Dundee. All rights reserved.
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License along
# with this program; if not, write to the Free Software Foundation, Inc.,
# 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.

"""
@author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
@version 3.0
<small>
(<b>Internal version:</b> $Revision: $Date: $)
</small>
@since 3.0-Beta4.2
"""

import math
import numpy

from omero.model import ImageI
from omero.model import RoiI
from omero.model import EllipseI
from omero.model import RectangleI
from omero.model import PolygonI
from omero.model import MaskI
from omero.model import NamespaceI
from omero.rtypes import rdouble, rint, rlong, rstring

# Popo helpers #


def toCSV(list):
    """
    Convert a list to a Comma Separated Value string.
    @param list The list to convert.
    @return See above.
    """
    lenList = len(list)
    cnt = 0
    str = ""
    for item in list:
        str = str + item
        if(cnt < lenList - 1):
            str = str + ","
        cnt = cnt + 1
    return str


def toList(csvString):
    """
    Convert a csv string to a list of strings
    @param csvString The CSV string to convert.
    @return See above.
    """
    list = csvString.split(',')
    for index in range(len(list)):
        list[index] = list[index].strip()
    return list

##
# Create instance of data object this object wraps the basic OMERO types.
#


class DataObject(object):

    ##
    # Create instance.
    #

    def __init__(self):
        self.value = None
        self.dirty = False

    ##
    # Sets the {@link IObject}.
    #
    # @param value The value to set.
    #
    def setValue(self, value):
        if(value is None):
            raise Exception("IObject delegate for DataObject cannot be null.")
        self.value = value

    ##
    # get the id of the Dataobject.
    # @return See above.
    #
    def getId(self):
        if(self.value.getId() is None):
            return -1
        return self.value.getId().getValue()

    ##
    # Set the id of the data object
    # @param id See above.
    def setId(self, id):
        self.setDirty(True)
        self.value.setId(rlong(id))

    ##
    # Get the current object.
    # @return See above.
    #
    def asIObject(self):
        return self.value

    ##
    # The object has been changed and is now dirty.
    # @param boolean See above.
    #
    def setDirty(self, boolean):
        self.dirty = boolean

    ##
    # Has the object has been changed.
    # @return See above.
    #
    def getDirty(self):
        return self.dirty

    ##
    # Is the object loaded
    # @return see above.
    def isLoaded(self):
        return self.value.isLoaded()

    ##
    # Get the user details for the object.
    # @return see above.
    def getDetails(self):
        return self.asIObject().getDetails()


class ImageData(DataObject):

    ##
    # Create Instance
    #

    def __init__(self, image=None):
        DataObject.__init__(self)
        if(image is None):
            self.setValue(ImageI())
        else:
            self.setValue(image)

    ##
    # Sets the name of the image.
    #
    # @param name
    # The name of the image. Mustn't be <code>null</code>.
    #
    def setName(self, name):
        image = self.asIObject()
        if(image is None):
            raise Exception("No Image specified.")
        image.setName(rstring(name))
        self.setDirty(True)

    ##
    # Returns the name of the image.
    #
    # @return See above.
    #
    def getName(self):
        image = self.asIObject()
        if(image is None):
            raise Exception("No Image specified.")
        name = image.getName()
        if(name is None):
            return ""
        return name.getValue()

    ##
    # Sets the description of the image.
    #
    # @param description
    #            The description of the image.
    #
    def setDescription(self, description):
        image = self.asIObject()
        if(image is None):
            raise Exception("No Image specified.")
        image.setDescription(rstring(description))
        self.setDirty(True)

    ##
    # Returns the description of the image.
    #
    # @return See above.
    #
    def getDescription(self):
        image = self.asIObject()
        if(image is None):
            raise Exception("No Image specified.")
        description = image.getDescription()
        if(description is None):
            return ""
        return description.getValue()

##
# This class stores the ROI Coordinate (Z,T).
#


class ROICoordinate:

    ##
    # Initialise the ROICoordinate.
    # @param z The z-section.
    # @param t The timepoint.

    def __init__(self, z=0, t=0):
        self.theZ = z
        self.theT = t
        self.ZBITSPLIT = 18

    ##
    # Overload the equals operator
    #
    def __eq__(self, obj):
        if(self.theZ == obj.theZ and self.theT == obj.theT):
            return True
        return False

    ##
    # Overload the equals operator
    #
    def __ne__(self, obj):
        if(self.theZ != obj.theZ or self.theT != obj.theT):
            return True
        return False

    ##
    # Overload the lessthan or equals operator
    #
    def __lt__(self, obj):
        if(self.theT >= obj.theT):
            return False
        if(self.theZ >= obj.theZ):
            return False
        return True

    ##
    # Overload the lessthan or equals operator
    #
    def __le__(self, obj):
        if(self.theT < obj.theT):
            return False
        if(self.theZ < obj.theZ):
            return False
        return True

    ##
    # Overload the greater than equals operator
    #
    def __gt__(self, obj):
        if(self.theT <= obj.theT):
            return False
        if(self.theZ <= obj.theZ):
            return False
        return True

    ##
    # Overload the greater than or equals operator
    #
    def __ge__(self, obj):
        if(self.theT < obj.theT):
            return False
        if(self.theZ < obj.theZ):
            return False
        return True

    ##
    # Overload the hash operator
    #
    def __hash__(self):
        return self.theZ << self.ZBITSPLIT + self.theT

    ##
    # Returns the timepoint.
    #
    # @return See above.
    #
    def getTimepoint(self):
        return self.theT

    ##
    # Returns the Z-Section.
    #
    # @return See above.
    #
    def getZSection(self):
        return self.theZ

    ##
    # Set the Z-Section of the Coordinate
    # @param z See above.
    #
    def setZSection(self, z):
        self.z = z

    ##
    # Set the Timepoint of the Coordinate
    # @param t See above.
    #
    def setTimepoint(self, t):
        self.t = t


###
# Shape wrapper.
#
def shapeWrapper(serverSideShape):
    """
    Wrap the serverSide shape as the appropriate OmeroPopos
    @param serverSideShape The shape object to wrap.
    @return See above.
    """
    print "ServerSideShape"
    print serverSideShape.__class__.__name__
    if serverSideShape.__class__.__name__ == 'EllipseI':
        return EllipseData(serverSideShape)
    if serverSideShape.__class__.__name__ == 'RectangleI':
        return RectData(serverSideShape)
    if serverSideShape.__class__.__name__ == 'MaskI':
        return MaskData(serverSideShape)
    if serverSideShape.__class__.__name__ == 'PolygonI':
        return PolygonData(serverSideShape)
    return None

##
# This class defines the python mapping of
# the ROIData object {See Pojos#ROIData}
#


class ROIData(DataObject):

    ##
    # Create a new instance of an ROIData object.
    #

    def __init__(self, roi=None):
        DataObject.__init__(self)
        if(roi is None):
            self.setValue(RoiI())
        else:
            self.setValue(roi)
        self.roiShapes = dict()
        if(roi is not None):
            self.initialise()
    ##
    # Initialise the shape map of the ROIData object.
    #

    def initialise(self):
        self.roiShapes = dict()
        roi = self.asIObject()
        shapes = roi.copyShapes()
        s = None
        for shape in shapes:
            s = shapeWrapper(shape)
            if(s is not None):
                coord = ROICoordinate(s.getZ(), s.getT())
                if(coord not in self.roiShapes.keys()):
                    self.roiShapes[coord] = list()
                data = self.roiShapes[coord]
                data.append(s)

    ##
    # Set the imageId for the ROI.
    # @param imageId See above.
    #
    def setImage(self, image):
        roi = self.asIObject()
        if(roi is None):
            raise Exception("No Roi specified.")
        roi.setImage(image)
        self.setDirty(True)

    ##
    # Get the image for the ROI.
    # @return See above.
    #
    def getImage(self):
        roi = self.asIObject()
        if(roi is None):
            raise Exception("No Roi specified.")
        return roi.getImage()

    ##
    # Add ShapeData object to ROIData.
    # @param shape See above.
    #
    def addShapeData(self, shape):
        roi = self.asIObject()
        if(roi is None):
            raise Exception("No Roi specified.")
        coord = shape.getROICoordinate()
        shapeList = None
        if(coord not in self.roiShapes.keys()):
            shapeList = list()
            self.roiShapes[coord] = shapeList
        else:
            shapeList = self.roiShapes[coord]
        shapeList.append(shape)
        roi.addShape(shape.asIObject())
        self.setDirty(True)

    ##
    # Remove ShapeData object from ROIData.
    # @param shape See above.
    #
    def removeShapeData(self, shape):
        roi = self.asIObject()
        if(roi is None):
            raise Exception("No Roi specified.")
        coord = shape.getROICoordinate()
        shapeList = self.roiShapes[coord]
        shapeList.remove(shape)
        roi.removeShape(shape.asIObject())
        self.setDirty(True)

    ##
    # Get the number of planes occupied by the ROI.
    # @return See above.
    #
    def getPlaneCount(self):
        return len(self.roiShapes)

    ##
    # Get the number of shapes in the ROI.
    # @return See above.
    #
    def getShapeCount(self):
        count = 0
        for coord in self.roiShapes:
            list = self.roiShapes[coord]
            count = count + len(list)
        return count

    ##
    # Returns the list of shapes on a given plane.
    #
    # @param z The z-section.
    # @param t The timepoint.
    # @return See above.
    #
    def getShapes(self, z, t):
        return self.roiShapes[ROICoordinate(z, t)]

    ##
    # Returns the iterator of the collection of the map.
    #
    # @return See above.
    #
    def getIterator(self):
        return self.roiShapes.iteritems()

    ##
    # Returns an iterator of the Shapes in the ROI in the range [start, end].
    #
    # @param start The starting plane where the Shapes should reside.
    # @param end The final plane where the Shapes should reside.
    # @return See above.
    #
    def getShapesInRange(self, start, end):
        coordList = self.roiShapes.keys()
        coordList.sort()
        keyList = []
        for coord in coordList:
            if(coord >= start and coord <= end):
                keyList.append(coord)
        return self.roiShapes.from_keys(keyList)

    ##
    # Returns the namespace of the ROI.
    #
    # @return see above.
    #
    def setNamespaceKeywords(self, namespace, keywords):
        roi = self.asIObject()
        if(roi is None):
            raise Exception("No Roi specified.")
        if(len(keywords) == 0):
            self.removeNamespace(namespace)
        else:
            map = self.getNamespaceKeywords()
            map[namespace] = keywords
            self.setNamespaceMap(map)
            self.setDirty(True)

    ##
    # Remove the namespace from the ROI
    # @param namespace See above.
    #
    def removeNamespace(self, namespace):
        roi = self.asIObject()
        if(roi is None):
            raise Exception("No Roi specified.")
        map = self.getNamespaceKeywords()
        if(namespace in map.keys()):
            del map[namespace]
        self.setNamespaceMap(map)
        self.setDirty(True)

    ##
    # Update the ROIData object to have the namespaces of the
    # map, and the keywords of the map.
    # @param map See above.
    #
    def setNamespaceMap(self, map):
        roi = self.asIObject()
        if(roi is None):
            raise Exception("No Roi specified.")
        roi.setNamespaces(map.keys)
        keywords = []
        for namespace in map.keys:
            keywords.append(map[namespace])
        roi.setKeywords(keywords)
        self.setDirty(True)

    ##
    # Retrieve the namespaces of the ROI
    # @return See above.
    #
    def getNamespaces(self):
        roi = self.asIObject()
        if(roi is None):
            raise Exception("No Roi specified.")
        namespaces = roi.getNamespaces()
        if(namespaces is None):
            return []
        return namespaces

    ##
    # Get the keywords and namespaces as a map<namespace, keywords>
    # @return See above.
    #
    def getNamespaceKeywords(self):
        roi = self.asIObject()
        if (roi is None):
            raise Exception("No Roi specified.")
        namespaces = self.getNamespaces()
        namespaceKeywords = roi.getKeywords()
        if(len(namespaces) != len(namespaceKeywords)):
            raise Exception(
                "Namespaces length does not match keywords namespace length.")
        map = {}
        for i in range(len(namespaces)):
            map[namespaces[i]] = namespaceKeywords[i]
        return map


class ShapeData(DataObject):

    def __init__(self):
        DataObject.__init__(self)
        self.text = None
        self.coord = ROICoordinate()

    ##
    # Returns the z-section.
    #
    # @return See above.
    #
    def getZ(self):
        shape = self.asIObject()
        if(shape is None):
            raise Exception("No Shape specified.")
        z = shape.getTheZ()
        if(z is None):
            return 0
        else:
            return z.getValue()

    ##
    # Set the z-section.
    # @param theZ See above.
    #
    def setZ(self, theZ):
        shape = self.asIObject()
        if(shape is None):
            raise Exception("No Shape specified.")
        shape.setTheZ(rint(theZ))
        self.coord.setZSection(theZ)
        self.setDirty(True)

    ##
    # Returns the timepoint.
    #
    # @return See above.
    #
    def getT(self):
        shape = self.asIObject()
        if(shape is None):
            raise Exception("No Shape specified.")
        t = shape.getTheT()
        if(t is None):
            return 0
        else:
            return t.getValue()

    ##
    # Set the timepoint.
    # @param See above.
    #
    def setT(self, theT):
        shape = self.asIObject()
        if(shape is None):
            raise Exception("No Shape specified.")
        shape.setTheT(rint(theT))
        self.coord.setTimePoint(theT)
        self.setDirty(True)

    ##
    # Set the ROICoordinate for the ShapeData
    # @param roiCoordinate See above.
    #
    def setROICoordinate(self, coord):
        shape = self.asIObject()
        if(shape is None):
            raise Exception("No Shape specified.")
        self.setZ(coord.getZSection())
        self.setT(coord.getTimePoint())
        self.coord.setZSection(coord.getZSection())
        self.coord.setTimePoint(coord.getTimePoint())
        self.setDirty(True)

    ##
    # Get the ROICoordinate for the ShapeData
    # @return See above.
    #
    def getROICoordinate(self):
        shape = self.asIObject()
        if(shape is None):
            raise Exception("No Shape specified.")
        return self.coord

    ##
    # Get the text for the Object
    # @return See above.
    def getText(self):
        shape = self.asIObject()
        if(shape is None):
            raise Exception("No Shape specified.")
        text = shape.getTextValue()
        if(text is None):
            return ""
        else:
            return text.getValue()

    ##
    # Set the text for the Obect.
    # @param See above.
    def setText(self, text):
        shape = self.asIObject()
        if(shape is None):
            raise Exception("No Shape specified.")
        shape.setTextValue(rstring(text))
        self.setDirty(True)

    ##
    # Get the affinetransform from the object,
    # returned as a string matrix(m00 m01 m10 m11 m02 m12)
    # see Java affinetransform toMatrix.
    # @return see above.
    #
    def getTransform(self):
        shape = self.asIObject()
        if(shape is None):
            raise Exception("No Shape specified.")
        transform = shape.getTransform()
        if(transform is not None):
            transformValue = transform.getValue()
            if(transformValue == "none"):
                return ""
            else:
                return transformValue
        return ""

    def setTransform(self, transform):
        shape = self.asIObject()
        if(shape is None):
            raise Exception("No Shape specified.")
        shape.setTransform(rstring(transform))
        self.setDirty(True)

    ##
    # Transform the affine transform matrix from the string
    # 'matrix(m00 m01 m10 m 11 m02 m12)' to a more appropriate
    # numpy.array([m00 m01 m02], [m10 m11 m12]).
    #
    def transformToMatrix(self, str):
        if (str == ""):
            return numpy.matrix([[1, 0, 0], [0, 1, 0]])
        transformstr = str[str.find('(') + 1:len(str) - 1]
        values = transformstr.split(' ')
        b = numpy.matrix(numpy.array(values, dtype='double'))
        t = numpy.matrix(numpy.zeros((3, 3)))
        t[0, 0] = b[0, 0]
        t[0, 1] = b[0, 2]
        t[1, 0] = b[0, 1]
        t[1, 1] = b[0, 3]
        t[0, 2] = b[0, 4]
        t[1, 2] = b[0, 5]
        t[2, 2] = 1
        return t

    ##
    # does the shape contain the point
    # @return see above.
    #
    def contains(self, point):
        return False

    ##
    #
    #
    def containsPoints(self):
        return []

##
# Instance of the EllipseData Object
#


class EllipseData(ShapeData):

    ##
    # Create instance of EllipseData Object
    #

    def __init__(self, shape=None):
        ShapeData.__init__(self)
        if(shape is None):
            self.setValue(EllipseI())
            self.setCx(0)
            self.setCy(0)
            self.setRx(0)
            self.setRy(0)
        else:
            self.setValue(shape)

    ##
    # Set the centre x coord of the Ellipse
    # @param cx See above.
    def setCx(self, cx):
        shape = self.asIObject()
        if(shape is None):
            raise Exception("No Shape specified.")
        shape.setCx(rdouble(cx))

    ##
    # Get the centre x coord of the Ellipse
    # @return See Above.
    def getCx(self):
        shape = self.asIObject()
        if(shape is None):
            raise Exception("No Shape specified.")
        cx = shape.getCx()
        if(cx is None):
            return 0
        return cx.getValue()

    ##
    # Set the centre y coord of the Ellipse
    # @param cy See above.
    def setCy(self, cy):
        shape = self.asIObject()
        if(shape is None):
            raise Exception("No Shape specified.")
        shape.setCy(rdouble(cy))

    ##
    # Get the centre y coord of the Ellipse
    # @return See Above.
    def getCy(self):
        shape = self.asIObject()
        if(shape is None):
            raise Exception("No Shape specified.")
        cy = shape.getCy()
        if(cy is None):
            return 0
        return cy.getValue()

    ##
    # Set the radius on the x-axis of the Ellipse
    # @param rx See above.
    def setRx(self, rx):
        shape = self.asIObject()
        if(shape is None):
            raise Exception("No Shape specified.")
        shape.setRx(rdouble(rx))

    ##
    # Get the radius of the x-axis of the Ellipse
    # @return See Above.
    def getRx(self):
        shape = self.asIObject()
        if(shape is None):
            raise Exception("No Shape specified.")
        rx = shape.getRx()
        if(rx is None):
            return 0
        return rx.getValue()

    ##
    # Set the radius on the y-axis of the Ellipse
    # @param rx See above.
    def setRy(self, ry):
        shape = self.asIObject()
        if(shape is None):
            raise Exception("No Shape specified.")
        shape.setRy(rdouble(ry))

    ##
    # Get the radius of the y-axis of the Ellipse
    # @return See Above.
    def getRy(self):
        shape = self.asIObject()
        if(shape is None):
            raise Exception("No Shape specified.")
        ry = shape.getRy()
        if(ry is None):
            return 0
        return ry.getValue()

    ##
    # Transform the point by the affineTransform transform.
    # @param transform See above.
    # @param point See above.
    # @return See above.
    #
    def transformPoint(self, transform, point):
        p = numpy.matrix(point).transpose()
        return transform * p

    ##
    # Return a map of points(x,y) contained within the Shape
    # @return See above.
    #
    def containsPoints(self):
        cx = self.getCx()
        cy = self.getCy()
        rx = self.getRx()
        ry = self.getRy()
        transform = self.transformToMatrix(self.getTransform())
        point = numpy.matrix((cx, cy, 1)).transpose()
        centre = transform * point
        BL = numpy.matrix((cx - rx, cy + ry, 1)).transpose()
        BR = numpy.matrix((cx + rx, cy + ry, 1)).transpose()
        TL = numpy.matrix((cx - rx, cy - ry, 1)).transpose()
        TR = numpy.matrix((cx + rx, cy - ry, 1)).transpose()
        MajorAxisLeft = numpy.matrix((cx - rx, cy, 1)).transpose()
        MajorAxisRight = numpy.matrix((cx + rx, cy, 1)).transpose()
        lb = transform * BL
        rb = transform * BR
        lt = transform * TL
        rt = transform * TR
        majl = transform * MajorAxisLeft
        majr = transform * MajorAxisRight
        o = (majr[1] - majl[1])
        a = (majr[0] - majl[0])
        h = math.sqrt(o * o + a * a)
        majorAxisAngle = math.asin(o / h)
        boundingBoxMinX = min(lt[0], rt[0], lb[0], rb[0])
        boundingBoxMaxX = max(lt[0], rt[0], lb[0], rb[0])
        boundingBoxMinY = min(lt[1], rt[1], lb[1], rb[1])
        boundingBoxMaxY = max(lt[1], rt[1], lb[1], rb[1])
        boundingBox = (
            (boundingBoxMinX, boundingBoxMinY),
            (boundingBoxMaxX, boundingBoxMaxY))
        centredBoundingBox = (
            (boundingBox[0][0] - centre[0], boundingBox[0][1] - centre[1]),
            (boundingBox[1][0] - centre[0], boundingBox[1][1] - centre[1]))
        points = {}
        cx = float(centre[0])
        cy = float(centre[1])
        xrange = range(centredBoundingBox[0][0], centredBoundingBox[1][0])
        yrange = range(centredBoundingBox[0][1], centredBoundingBox[1][1])
        for x in xrange:
            for y in yrange:
                newX = x * math.cos(majorAxisAngle) + y * \
                    math.sin(majorAxisAngle)
                newY = -x * math.sin(majorAxisAngle) + \
                    y * math.cos(majorAxisAngle)
                val = (newX * newX) / (rx * rx) + (newY * newY) / (ry * ry)
                if(val <= 1):
                    points[(int(x + cx), int(y + cy))] = 1
        return points

##
# Instance of Polygon object.
#


class PolygonData(ShapeData):

    ###
    # Create instance of PolygonData Object
    #

    def __init__(self, shape=None):
        ShapeData.__init__(self)
        self.NUMREGEX = "\\[.*\\]"
        # Regex for a data in block.
        if(shape is None):
            self.setValue(PolygonI())
            self.points = []
            self.points1 = []
            self.points2 = []
            self.mask = []
        else:
            self.setValue(shape)
            self.parseShapeStringToPoints()

    ##
    # Get the points from the points String
    # @return See above.
    #
    def getPoints(self):
        pts = self.fromPoints("points")
        return self.parsePointsToList(pts)

    ##
    # Get the points1 from the points String
    # @return See above.
    #
    def getPoints1(self):
        pts = self.fromPoints("points1")
        return self.parsePointsToList(pts)

    ##
    # Get the points2 from the points String
    # @return See above.
    #
    def getPoints2(self):
        pts = self.fromPoints("points2")
        return self.parsePointsToList(pts)

    ##
    # Get the mask type from the points String
    # @return See above.
    #
    def getMaskPoints(self):
        pts = self.fromPoints("mask")
        return self.parsePointsToList(pts)

    ##
    # Set the points from the original PolygonI type.
    # @param pts The points values.
    #
    def setPointsString(self, pts):
        shape = self.asIObject()
        if(shape is None):
            raise Exception("No Shape specified.")
        shape.setPoints(pts)
        self.setDirty(True)
        self.parseShapeStringToPoints()

    ##
    # Set the points from a series of lists, and also set the points string.
    # @param points The points list.
    # @param points1 The points1 list.
    # @param points2 The points2 list.
    # @param mask The mask represents the curve type, lineTo, ArcTo..
    def setPointsFromList(self, points, points1, point2, mask):
        pts = self.toString(points)
        pts1 = self.toString(points)
        pts2 = self.toString(points)
        mask = self.toString(points)
        str = "points[" + pts + "] "
        str = str + "points1[" + pts1 + "] "
        str = str + "points2[" + pts2 + "] "
        str = str + "mask[" + mask + "]"
        self.setPointsString(str)

    ##
    # Get the points string from the IObject
    # @return See above.
    def getPointsString(self):
        shape = self.asIObject()
        if(shape is None):
            raise Exception("No Shape specified.")
        pts = shape.getPoints()
        if(pts is None):
            return ""
        else:
            return pts.getValue()

    ##
    # Get the points of type from the point string.
    # @param type The points type to return, points, point1, point2, mask
    # @return See above.
    #
    def fromPoints(self, type):
        return self.getContents(self.getPointsString(), type + "[", "]")

    ##
    # Helper method to get the contents of the values
    # from the string of form type[values].
    # @param string The string to parse.
    # @param start the first part of the string to break apart on. "type["
    # @param end the last part of the string to break apart "]".
    # @return The contents of the string between start ReturnValues end
    #
    def getContents(self, string, start, end):
        lIndex = string.find(start)
        if(lIndex == -1):
            return ""
        strFragment = string[lIndex:]
        rIndex = strFragment.find(']')
        if(rIndex == -1):
            return ""
        return string[lIndex + len(start):rIndex]

    ##
    # Convert the pts string to a list
    # @return See above.
    #
    def parsePointsToList(self, pts):
        numberList = pts.split(',')
        return numberList

    ##
    # Convert the pointsList to a string, of CSV
    # @return See above.
    #
    def toString(self, pointsList):
        str = ""
        for index, pt in enumerate(pointsList):
            str = str + pt
            if(index < len(pointsList) - 1):
                str = str + ","
        return str

    ##
    # Convert the points string to the points lists,
    # points, point1, point2 and mask.
    #
    def parseShapeStringToPoints(self):
        self.points = self.fromPoints("points")
        self.points1 = self.fromPoints("points1")
        self.points2 = self.fromPoints("points2")
        self.mask = self.fromPoints("mask")

    ##
    # Returns the bounding rectangle of the polygon,
    # as a list of coords [(x1,y1), (x2,y2)]
    # @return See above.
    #
    def getBoundingRectangle(self):
        pts = self.toCoords(self.getPoints())
        minx = pts[0][0]
        maxx = minx
        miny = pts[0][1]
        maxy = miny

        for pt in pts:
            minx = min(pt[0], minx)
            miny = min(pt[1], miny)
            maxx = max(pt[0], maxx)
            maxy = max(pt[1], maxy)
        return [(minx, miny), (maxx, maxy)]

    ##
    # Convert the points list to a coord list.
    # @param ptsList The list of points.
    # @return See above.
    def toCoords(self, ptsList):
        coords = []
        for index in range(len(ptsList) / 2):
            coords.append(
                (int(ptsList[index * 2]), int(ptsList[index * 2 + 1])))
        return coords

    ##
    # Return a map of points(x,y) contained within the Shape
    # @return See above.
    #
    def containsPoints(self):
        points = {}
        boundingRectangle = self.getBoundingRectangle()
        xrange = range(boundingRectangle[0][0], boundingRectangle[1][0])
        yrange = range(boundingRectangle[0][1], boundingRectangle[1][1])
        for xx in xrange:
            for yy in yrange:
                if(self.inPolygon((xx, yy))):
                    points[(xx, yy)] = 1
        return points

    ##
    # Return true if the point p is inside the polygon,
    # defined by the vertexes in points.
    # @param p The point (x,y)
    # @return See above.
    #
    def inPolygon(self, p):
        angle = 0.0
        polypoints = self.getPoints()
        polygon = []
        for index in range(0, len(polypoints) / 2):
            polygon.append(
                (int(polypoints[index * 2]), int(polypoints[index * 2 + 1])))

        n = len(polygon)

        for i, (h, v) in enumerate(polygon):
            p1 = (h - p[0], v - p[1])
            h, v = polygon[(i + 1) % n]
            p2 = (h - p[0], v - p[1])
            angle += self.Angle2D(p1[0], p1[1], p2[0], p2[1])

        if abs(angle) < math.pi:
            return False
        return True

    ##
    # Return the angle(in radians) between the two vectors (x1,y1), (x2,y2)
    # @param x1 The x of the first vector
    # @param y1 The y of the first vector
    # @param x2 The x of the second vector
    # @param y2 The y of the second vector
    # @return see above.
    #
    def Angle2D(self,  x1, y1, x2, y2):
        theta1 = math.atan2(y1, x1)
        theta2 = math.atan2(y2, x2)
        dtheta = theta2 - theta1
        while dtheta > math.pi:
            dtheta -= 2.0 * math.pi
        while dtheta < -math.pi:
            dtheta += 2.0 * math.pi
        return dtheta

##
# Instance of the Mask Object
#


class MaskData(ShapeData):

    ##
    # Create instance of MaskData Object
    #

    def __init__(self, maskShape=None):
        ShapeData.__init__(self)
        if(maskShape is None):
            self.setValue(MaskI())
            self.setX(0)
            self.setY(0)
            self.setWidth(0)
            self.setHeight(0)
            self.setBytes(None)
        else:
            self.setValue(maskShape)

    ##
    # Set the x coord of the Mask
    # @param x See above.
    def setX(self, x):
        shape = self.asIObject()
        if(shape is None):
            raise Exception("No Shape specified.")
        shape.setX(rdouble(x))

    ##
    # Get the x coord of the Mask
    # @return See Above.
    def getX(self):
        shape = self.asIObject()
        if(shape is None):
            raise Exception("No Shape specified.")
        x = shape.getX()
        if(x is None):
            return 0
        return x.getValue()

    ##
    # Set the y coord of the Mask
    # @param y See above.
    def setY(self, y):
        shape = self.asIObject()
        if(shape is None):
            raise Exception("No Shape specified.")
        shape.setY(rdouble(y))

    ##
    # Get the y coord of the Mask
    # @return See Above.
    def getY(self):
        shape = self.asIObject()
        if(shape is None):
            raise Exception("No Shape specified.")
        y = shape.getY()
        if(y is None):
            return 0
        return y.getValue()
    ##
    # Set the width the Mask
    # @param width See above.

    def setWidth(self, width):
        shape = self.asIObject()
        if(shape is None):
            raise Exception("No Shape specified.")
        shape.setWidth(rdouble(width))

    ##
    # Get the width of the Mask
    # @return See Above.
    def getWidth(self):
        shape = self.asIObject()
        if(shape is None):
            raise Exception("No Shape specified.")
        width = shape.getWidth()
        if(width is None):
            return 0
        return width.getValue()
    ##
    # Set the height of the Mask
    # @param height See above.

    def setHeight(self, height):
        shape = self.asIObject()
        if(shape is None):
            raise Exception("No Shape specified.")
        shape.setHeight(rdouble(height))

    ##
    # Get the height of the Mask
    # @return See Above.
    def getHeight(self):
        shape = self.asIObject()
        if(shape is None):
            raise Exception("No Shape specified.")
        height = shape.getHeight()
        if(height is None):
            return 0
        return height.getValue()
    ##
    # Set the bitmask of the Mask
    # @param See Above.

    def setMask(self, mask):
        shape = self.asIObject()
        if(shape is None):
            raise Exception("No Shape specified.")
        shape.setBytes(mask)

    ##
    # Get the bitmask of the Mask
    # @return See Above.
    def getMask(self):
        shape = self.asIObject()
        if(shape is None):
            raise Exception("No Shape specified.")
        mask = shape.getBytes()
        if(mask is None):  # ??
            return 0
        return mask.getValue()


##
# Instance of the RectangleData object
#
class RectData(ShapeData):

    ##
    # Create instance of MaskData Object
    #

    def __init__(self, rectShape=None):
        ShapeData.__init__(self)
        if (rectShape is None):
            self.setValue(RectangleI())
            self.setX(0)
            self.setY(0)
            self.setWidth(0)
            self.setHeight(0)
        else:
            self.setValue(rectShape)

    ##
    # Set the x coord of the Rectangle
    # @param x See above.
    def setX(self, x):
        shape = self.asIObject()
        if (shape is None):
            raise Exception("No Shape specified.")
        shape.setX(rdouble(x))

    ##
    # Get the x coord of the Rectangle
    # @return See Above.
    def getX(self):
        shape = self.asIObject()
        if (shape is None):
            raise Exception("No Shape specified.")
        x = shape.getX()
        if (x is None):
            return 0
        return x.getValue()

    ##
    # Set the y coord of the Rectangle
    # @param y See above.
    def setY(self, y):
        shape = self.asIObject()
        if(shape is None):
            raise Exception("No Shape specified.")
        shape.setY(rdouble(y))

    ##
    # Get the y coord of the Rectangle
    # @return See Above.
    def getY(self):
        shape = self.asIObject()
        if(shape is None):
            raise Exception("No Shape specified.")
        y = shape.getY()
        if(y is None):
            return 0
        return y.getValue()
    ##
    # Set the width the Rectangle
    # @param width See above.

    def setWidth(self, width):
        shape = self.asIObject()
        if(shape is None):
            raise Exception("No Shape specified.")
        shape.setWidth(rdouble(width))

    ##
    # Get the width of the Rectangle
    # @return See Above.
    def getWidth(self):
        shape = self.asIObject()
        if(shape is None):
            raise Exception("No Shape specified.")
        width = shape.getWidth()
        if(width is None):
            return 0
        return width.getValue()
    ##
    # Set the height of the Rectangle
    # @param height See above.

    def setHeight(self, height):
        shape = self.asIObject()
        if(shape is None):
            raise Exception("No Shape specified.")
        shape.setHeight(rdouble(height))

    ##
    # Get the height of the Rectangle
    # @return See Above.
    def getHeight(self):
        shape = self.asIObject()
        if(shape is None):
            raise Exception("No Shape specified.")
        height = shape.getHeight()
        if(height is None):
            return 0
        return height.getValue()

    ##
    # Transform the point by the affineTransform transform.
    # @param transform See above.
    # @param point See above.
    # @return See above.
    #
    def transformPoint(self, transform, point):
        p = numpy.matrix(point).transpose()
        return transform * p

    ##
    # Return a map of points(x,y) contained within the Shape
    # @return See above.
    #
    def containsPoints(self):
        transform = self.transformToMatrix(self.getTransform())
        x = self.getX()
        y = self.getY()
        width = self.getWidth()
        height = self.getHeight()
        point = numpy.matrix((x, y, 1)).transpose()
        centre = transform * point
        BL = numpy.matrix((x, y + height, 1)).transpose()
        BR = numpy.matrix((x + width, y + height, 1)).transpose()
        TL = numpy.matrix((x, y, 1)).transpose()
        TR = numpy.matrix((x + width, y, 1)).transpose()
        lb = transform * BL
        rb = transform * BR
        lt = transform * TL
        rt = transform * TR
        majl = lb
        majr = rb
        o = (majr[1] - majl[1])
        a = (majr[0] - majl[0])
        h = math.sqrt(o * o + a * a)
        angle = math.asin(o / h)
        boundingBoxMinX = min(lt[0], rt[0], lb[0], rb[0])
        boundingBoxMaxX = max(lt[0], rt[0], lb[0], rb[0])
        boundingBoxMinY = min(lt[1], rt[1], lb[1], rb[1])
        boundingBoxMaxY = max(lt[1], rt[1], lb[1], rb[1])
        boundingBox = (
            (boundingBoxMinX, boundingBoxMinY),
            (boundingBoxMaxX, boundingBoxMaxY))
        points = {}
        xrange = range(boundingBox[0][0], boundingBox[1][0])
        yrange = range(boundingBox[0][1], boundingBox[1][1])
        transformedX = float(centre[0])
        transformedY = float(centre[1])
        cx = float(centre[0])
        cy = float(centre[1])
        for xx in xrange:
            for yy in yrange:
                newX = xx * math.cos(angle) + yy * math.sin(angle)
                newY = -xx * math.sin(angle) + yy * math.cos(angle)

                if (newX - transformedX < width
                        and newY - transformedY < height
                        and newX - transformedX > 0
                        and newY - transformedY > 0):
                    points[(int(x + cx), int(y + cy))] = 1
        return points

##
# The workflow data object, which wraps the omero.mdoel.NamespaceI class
#


class WorkflowData(DataObject):

    def __init__(self, workflow=None):
        DataObject.__init__(self)
        if(workflow is None):
            self.setValue(NamespaceI())
            self.setNamespace("")
            self.setKeywords([])
            self.setDirty(True)
        else:
            self.setValue(workflow)

    ##
    # Set the namespace of the workflow.
    # @param namespace See above.
    def setNamespace(self, namespace):
        workflow = self.asIObject()
        if(workflow is None):
            raise Exception("No workflow specified.")
        workflow.setName(rstring(namespace))
        self.setDirty(True)

    ##
    # Get the namespace of the workflow
    # @return See Above.
    def getNamespace(self):
        workflow = self.asIObject()
        if(workflow is None):
            raise Exception("No Workflow specified.")
        namespace = workflow.getName()
        if(namespace is None):
            return ""
        return namespace.getValue()

    ##
    # Set the keywords of the workflow.
    # @param namespace See above.
    def setKeywords(self, keywords):
        workflow = self.asIObject()
        if(workflow is None):
            raise Exception("No workflow specified.")
        workflow.setKeywords(keywords)
        self.setDirty(True)

    ##
    # Set the keywords of the workflow.
    # @param namespace See above.
    def setKeywordsFromString(self, keywords):
        workflow = self.asIObject()
        if(workflow is None):
            raise Exception("No workflow specified.")
        workflow.setKeywords(toList(keywords))
        self.setDirty(True)

    ##
    # Get the keywords of the workflow
    # @return See Above.
    def getKeywords(self):
        workflow = self.asIObject()
        if(workflow is None):
            raise Exception("No Workflow specified.")
        keywords = workflow.getKeywords()
        if(keywords is None):
            return []
        return keywords

    ##
    # Add a keyword to the workflow
    # @param keyword See Above.
    def addKeyword(self, keyword):
        if(self.containsKeyword(keyword)):
            return
        keywords = self.getKeywords()
        keywords.append(keyword)
        self.setKeywords(keywords)

    ##
    # Return <code>True</code> if the keyword is part of workflow
    # @return See Above.
    def containsKeyword(self, keyword):
        keywords = self.getKeywords()
        return (keyword in keywords)

    ##
    # Remove the keyword from the workflow
    # @param keyword See Above.
    def removeKeyword(self, keyword):
        if(not self.containsKeyword(keyword)):
            return
        newList = self.getKeywords()
        newList.remove(keyword)
        self.setKeywords(newList)
