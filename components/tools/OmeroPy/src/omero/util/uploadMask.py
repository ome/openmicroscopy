#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
 components/tools/OmeroPy/omero/util/UploadMask.py

-----------------------------------------------------------------------------
  Copyright (C) 2006-2009 University of Dundee. All rights reserved.


  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License along
  with this program; if not, write to the Free Software Foundation, Inc.,
  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.

------------------------------------------------------------------------------


@author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
@author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
@version 3.0
<small>
(<b>Internal version:</b> $Revision: $Date: $)
</small>
@since 3.0-Beta4.1

"""

from OmeroPopo import MaskData
from OmeroPopo import ROIData
from OmeroPopo import ImageData
from OmeroPopo import ROICoordinate
import math

class uploadMask():

    ##
    # Instantiate the uploadMask Object.
    #   
    def __init__(self):
       ## Map of colour, roiclass this will store all the masks in the image, and
       # all the roi with a particular colour. */
       self.roiMap = {};
    
    ##
    # Add a Mask Shape to the appropriate ROIClass, creating one if neccessary.
    # @param image The Image containing the mask data.
    # @param z The Z Section of the image.
    # @param t The Time point of the image.
    #
    def addMaskShape(self, image, z, t):
        maskMap = self.createMasks(image, z, t);
        
        for mask in maskMap:
            roiClass = None;
            if(self.roiMap.has_key(mask.getColour())):
                roiClass = self.roiMap[mask.getColour()];
            else:
                roiClass = ROIClass();
                self.roiMap[mask.getColour()] = roiClass;
            roiClass.addMask(mask, z, t);
    
    ##
    # Get all the masks for the image.
    # @param image The Image containing the mask data.
    # @param z The Z Section of the image.
    # @param t The Time point of the image.
    # @return See above.
    #
    def createMasks(self, inputImage, z, t):
        value = None;
        mask = None;
        map = {};
        for x in range(inputImage.getWidth()):
          for y in range(inputImage.getHeight()):
        
                value = inputImage.getRGB(x, y);
                if(value==Color.black.getRGB()):
                    continue;
                if (not map.has_key(value)):
                    mask = MaskClass(value);
                    map[value] = mask;
                else:
                    mask = map[value];
                mask.add(Point(x, y));
        return map;
    
    ##
    # Return all the roi for the image. 
    # @param image See above.
    # @return See above.
    #
    def getROIForImage(self, image):
        roiList = []
        for roi in self.roiMap:
            roiList.append(roi.getROI(image));
        return roiList;

class MaskClass():

    ##
    # Instantiate a new mask object with colour value. 
    # @param value The colour of the mask as packedInt
    #
    def __init__(self, value):
    
        ## The points in the mask. These points are in the image coordinates. 
        self.points = {};
        
        ## The colour of the mask. 
        self.colour = value;
        
        ## The min(x,y) and max(x,y) coordinates. */
        self.min = Point();
        self.max = Point();
        
        ## The width of the mask. 
        self.width = 0;
        
        ## The height of the mask. 
        self.height = 0;
        
    ##
    # Get the colour of the mask.
    # @return See above.
    #
    def getColour(self):
        return self.colour;
    
    ##
    # Get the Points in the mask as a bytestream that can be used to 
    # make an image.    
    # @return See above.
    #
    def asBytes(self): 
        import array
        bytesArray = array.array('B');
        for cnt in range(int(math.ceil(self.width*self.height))):
            bytesArray.append(0);
        position = 0;
        for y in range(self.max.y):
            for x in range(self.max.x):
                if self.points.has_key(Point(x,y)):
                    self.setBit(bytesArray, position, 1)
                else:
                    self.setBit(bytesArray, position, 0)
                position = position + 1;
                    
        byteSwappedArray = bytesArray.byteswap();
        bytesString = bytesArray.tostring();
        return bytesString;



    ##
    # Add Point p to the list of points in the mask.
    # @param p See above.
    #
    def add(self, p):
        if(len(self.points) == 0):
            self.min = Point(p);
            self.max = Point(p);
        else:
            self.min.x = min(p.x, min.x);
            self.min.y = min(p.y, min.y);
            self.max.x = max(p.x, max.x);
            self.max.y = max(p.y, max.y);
        self.width = max.x-min.x+1;
        self.height = max.y-min.y+1;
        self.points.add(p);
    
    ##
    # Create a MaskData Object from the mask.
    # @param z The Z section the mask data is on.
    # @param t The T section the mask data is on.
    # @return See above.
    #
    def asMaskData(self, z, t):
        mask = MaskData();
        mask.setX(self.min.x);
        mask.setY(self.min.y);
        mask.setWidth(self.width);
        mask.setHeight(self.height);
        mask.setFill(self.colour);
        mask.setT(t);
        mask.setZ(z);
        mask.setMask(self.asBytes());
        return mask;

    def setBit(self, data, bit, val): 
        bytePosition = bit/8;
        bitPosition = bit%8;
        data[bytePosition] = data[bytePosition] & ~(0x1<<bitPosition) | (val<<bitPosition);

    def getBit(self, data, bit):
        bytePosition = bit/8;
        bitPosition = bit%8;
        if ((data[bytePosition] & (0x1<<bitPosition))!=0):
            return 1
        else:
            return 0

class ROIClass():

    
    ##
    # Instantiate the ROIClass and create the maskMap.
    #
    def __init__(self):
       ##  
       # Map of the coordinates and mask objects, may have more than one 
       # mask on one plane. 
       #
       self.maskMap = {};
    
    ##
    # Add a mask to the ROIMap, this will store the mask and it's z,t
    # @param mask See above.
    # @param z See above.
    # @param t See above. 
    #
    def addMask(self, mask, z, t):
        maskList = [];
        coord = ROICoordinate(z, t);
        if(self.maskMap.has_key(coord)):
            maskList = self.maskMap[coord];
        else:
            maskList = [];
            self.maskMap.put(coord, maskList);
        maskList.append(mask);
    
    ##
    # Create the roi for the 
    # @param image
    # @return See above.
    #
    def getROI(self, image):
        roi = ROIData();
        roi.setId(image.getId());
        
        for coord in self.maskMap:
            maskList = self.maskMap[coord];
            for mask in maskList:
                toSaveMask = mask.asMaskData(coord.getZSection(), coord.getTimePoint());        
                roi.addShapeData(toSaveMask);
        return roi;

##
# Point class with x, y values
# 
class Point():
    
    ##
    # Initialise point class
    # @param p point class can be initialised from another point.
    #
    def __init__(self, p = None):
        if(p != None):
            x = p.x;
            y = p.y;
        else:
            x = 0;
            y = 0;
    
    ##
    # Get the x value of the point
    # @return See Above.
    #
    def getX(self):
        return self.x;

    ##
    # Set the x value of the point
    # @param x See Above.
    #
    def setX(self, x):
        self.x = x;

    ##
    # Get the y value of the point
    # @return See Above.
    #
    def getY(self):
        return self.y;

    ##
    # Set the y value of the point
    # @param y See Above.
    #
    def setY(self, y):
        self.y = y;
