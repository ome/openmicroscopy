'''
*
*------------------------------------------------------------------------------
*  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
*
*
* 	This program is free software; you can redistribute it and/or modify
*  it under the terms of the GNU General Public License as published by
*  the Free Software Foundation; either version 2 of the License, or
*  (at your option) any later version.
*  This program is distributed in the hope that it will be useful,
*  but WITHOUT ANY WARRANTY; without even the implied warranty of
*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*  GNU General Public License for more details.
*
*  You should have received a copy of the GNU General Public License along
*  with this program; if not, write to the Free Software Foundation, Inc.,
*  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*
*------------------------------------------------------------------------------
'''

###
#
# ROIUtils allows the mapping of omero.model.ROIDataTypesI to python types
# and to create ROIDataTypesI from ROIUtil types. 
# These methods also implement the acceptVisitor method linking to the ROIDrawingCanvas.
#
#
# @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
# 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
# @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
# 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
# @version 3.0
# <small>
# (<b>Internal version:</b> $Revision: $Date: $)
# </small>
# @since 3.0-Beta4
#/

from omero.model import RoiI
from omero.model import EllipseI
from omero.model import LineI
from omero.model import RectI
from omero.model import PointI
from omero.model import TextI
from omero.model import PolylineI
from omero.model import PolygonI
from omero.model import PathI
from omero.rtypes import rdouble 
from omero.rtypes import rstring 
from omero.rtypes import rint 
from omero.rtypes import rfloat 


##
# abstract, defines the method that call it as abstract.
#
#
def abstract():
    import inspect
    caller = inspect.getouterframes(inspect.currentframe())[1][3]
    raise NotImplementedError(caller + ' must be implemented in subclass')

##
# ShapeSettingsData contains all the display information about the ROI that aggregates it.
#
class ShapeSettingsData:

    ##
    # Initialises the default values of the ShapeSettings.
    # Stroke has default colour of darkGrey
    # StrokeWidth defaults to 1
    #
    def __init__(self):   
        self.strokeColour = rstring('#444444');
        self.strokeWidth = rint(1);
        self.strokeOpacity = rfloat(0);
        self.strokeDashArray = rstring('');
        self.strokeDashOffset = rint(0);
        self.strokeLineCap = rstring('');
        self.strokeLineJoin = rstring('');
        self.strokeMiterLimit = rint(0);
        self.fillColour = rstring('#222222');
        self.fillOpacity = rfloat(0);
        self.fillRule = rstring('');
        
    ##
    # Applies the settings in the ShapeSettingsData to the ROITypeI
    # @param shape the omero.model.ROITypeI that these settings will be applied to
    #
    def setROIShapeSettings(self, shape):
        shape.setStrokeColor(self.strokeColour);
        shape.setStrokeWidth(self.strokeWidth);
        shape.setStrokeDashArray(self.strokeDashArray);
        shape.setStrokeDashOffset(self.strokeDashOffset);
        shape.setStrokeLineCap(self.strokeLineCap);
        shape.setStrokeLineJoin(self.strokeLineJoin);
        shape.setStrokeMiterLimit(self.strokeMiterLimit);
        shape.setFillColor(self.fillColour);
        shape.setFillOpacity(self.fillOpacity);
        shape.setFillRule(self.fillRule);
    
    ##
    # Set the Stroke settings of the ShapeSettings.
    # @param colour The colour of the stroke.
    # @param width The stroke width.
    # @param opacity The stroke opacity.
    #
    def setStrokeSettings(self, colour, width = 1, opacity = 0):
        self.strokeColour = rsting(colour);
        self.strokeWidth = rint(width);
        self.strokeOpacity = rfloat(opacity);

    ###
    # Set the Fill Settings for the ShapeSettings.
    # @param colour The fill colour of the shape.
    # @param opacity The opacity of the fill.
    def setFillSettings(self, colour, opacity = 0):
        self.fillColour = rsting(colour);
        self.fillOpacity = rfloat(opacity);   
    
    ##
    # Get the stroke settings as the tuple (strokeColour, strokeWidth).
    # @return See above.
    #
    def getStrokeSettings(self):
        return (self.strokeColour.getValue(), self.strokeWidth.getValue());
    
    ##
    # Get the fill setting as a tuple of (fillColour, opacity)
    # @return See above.
    #
    def getFillSettings(self):
        return (self.fillColour.getValue(), self.fillOpacity.getValue());
    
    ##
    # Get the tuple ((stokeColor, strokeWidth), (fillColour, fillOpacity)).
    # @return see above.
    #
    def getSettings(self):
        return (self.getStrokeSettings(), self.getFillSettings());
    
    ##
    # Set the current shapeSettings from the ROI roi.
    # @param roi see above.
    #
    def getShapeSettingsFromROI(self, roi):
        self.strokeColour = roi.getStrokeColor();
        self.strokeWidth = roi.getStrokeWidth();
        self.strokeOpacity = roi.getStrokeOpacity();
        self.strokeDashArray = roi.getStrokeDashArray();
        self.strokeDashOffset = roi.getStrokeDashOffset();
        self.strokeLineCap = roi.getStrokeLineCap();
        self.strokeLineJoin = roi.getStrokeLineJoin();
        self.strokeMiterLimit =  roi.getStrokeMiterLimit();
        self.fillColour = roi.getFillColor();
        self.fillOpacity = roi.getFillOpacity();
        self.fillRule = roi.getFillRule();

##
# This class stores the ROI Coordinate (Z,T).
#
class ROICoordinate:
    
    ##
    # Initialise the ROICoordinate.
    # @param z The z-section.
    # @param t The timepoint.
    def __init__(self, z = 0, t = 0):
        self.theZ = rint(z);
        self.theT = rint(t);

    ##
    # Set the (z, t) for the roi using the (z, t) of the ROICoordinate.
    # @param roi The ROI to set the (z, t) on.
    #
    def setROICoord(self, roi):
        roi.setTheZ(self.theZ);
        roi.setTheT(self.theT);
        
    ## 
    # Get the (z, t) from the ROI.
    # @param See above.
    # 
    def setCoordFromROI(self, roi):
        self.theZ = roi.getTheZ();
        self.theT = roi.getTheT();

##
# Interface to inherit for accepting ROIDrawing as a visitor.
# @param visitor The ROIDrawingCompoent.
#
class ROIDrawingI:
    def acceptVisitor(self, visitor):
        abstract();

class ShapeData:

    def __init__(self):
        self.coord = ROICoordinate();
        self.shapeSettings = ShapeSettingsData();
        self.ROI = None;
        
    def setCoord(self, coord):
        self.coord = coord;
    
    def setROICoord(self, roi):
        self.coord.setROICoord(roi);
        
    def setROIGeometry(self, shape):
        abstract();

    def setShapeSettings(self, settings):
        self.shapeSettings = settings;
    
    def setROIShapeSettings(self, roi):
        self.shapeSettings.setROIShapeSettings(roi);

    def acceptVisitor(self, visitor):
        abstract();

    def createBaseType(self):
        abstract();
        
    def getROI(self):
        if(self.roi != None):
            return self.roi;
        self.roi = self.createBaseType();
        self.setROICoord(roi);
        self.setROIGeometry(roi);
        self.setROIShapeSettings(roi);
        return self.roi;
        
    def getShapeSettingsFromROI(self, roi):
        self.shapeSettings.getShapeSettingsFromROI(roi);
        
    def getCoordFromROI(self, roi):
        self.coord.setCoordFromROI(roi);
        
    def getGeometryFromROI(self , roi):
        abstract();
        
    def fromROI(self, roi):
        self.roi = roi;
        self.getShapeSettingsFromROI(roi);
        self.getCoordFromROI(roi);
        self.getGeometryFromROI(roi);
        
class EllipseData(ShapeData, ROIDrawingI):
        
    def __init__(self, roicoord = ROICoordinate(), cx = 0, cy = 0, rx = 0, ry = 0):
        ShapeData.__init__(self);
        self.cx = rdouble(cx);
        self.cy = rdouble(cy);
        self.rx = rdouble(rx);
        self.ry = rdouble(ry);
        self.setCoord(roicoord);
        
    def setROIGeometry(self, ellipse):
        ellipse.setTheZ(self.coord.theZ);
        ellipse.setTheT(self.coord.theZ);
        ellipse.setCx(self.cx);
        ellipse.setCy(self.cy);
        ellipse.setRx(self.rx);
        ellipse.setRy(self.ry);

    def getGeometryFromROI(self, roi):
        self.cx = roi.getCx();
        self.cy = roi.getCy();
        self.rx = roi.getRx();
        self.ry = roi.getRy();

    def createBaseType(self):
        return EllipseI();

    def acceptVisitor(self, visitor):
        visitor.drawEllipse(self.cx.getValue(), self.cy.getValue(), self.rx.getValue(), self.ry.getValue(), self.shapeSettings.getSettings());
        
class RectangleData(ShapeData, ROIDrawingI):
        
    def __init__(self, roicoord = ROICoordinate(), x = 0, y = 0, width = 0, height = 0):
        ShapeData.__init__(self);
        self.x = rdouble(x);
        self.y = rdouble(y);
        self.width = rdouble(width);
        self.height = rdouble(height);
        self.setCoord(roicoord);
    
    def setGeometry(self, rectangle):
        rectangle.setTheZ(self.coord.theZ);
        rectangle.setTheT(self.coord.theZ);
        rectangle.setX(self.x);
        rectangle.setY(self.y);
        rectangle.setWidth(self.width);
        rectangle.setHeight(self.height);

    def getGeometryFromROI(self, roi):
        self.x = roi.getX();
        self.y = roi.getY();
        self.width = roi.getWidth();
        self.height = roi.getHeight();

    def createBaseType(self):
        return RectI();

    def acceptVisitor(self, visitor):
        visitor.drawRectangle(self.x, self.y, self.width, self.height, self.shapeSettings.getSettings());

class LineData(ShapeData, ROIDrawingI):
        
    def __init__(self, roicoord = ROICoordinate(), x1 = 0, y1 = 0, x2 = 0, y2 = 0):
        ShapeData.__init__(self);
        self.x1 = rdouble(x1);
        self.y1 = rdouble(y1);
        self.x2 = rdouble(x2);
        self.y2 = rdouble(y2);
        self.setCoord(roicoord);
    
    def setGeometry(self, line):
        line.setTheZ(self.coord.theZ);
        line.setTheT(self.coord.theZ);
        line.setX1(self.x1);
        line.setY1(self.y1);
        line.setX2(self.x2);
        line.setY2(self.y2);

    def getGeometryFromROI(self, roi):
        self.x1 = roi.getX1();
        self.y1 = roi.getY1();
        self.x2 = roi.getX2();
        self.y2 = roi.getY2();

    def createBaseType(self):
        return LineI();

    def acceptVisitor(self, visitor):
        visitor.drawLine(self.x1.getValue(), self.y1.getValue(), self.x2.getValue(), self.y2.getValue(), self.shapeSettings.getSettings());

class MaskData(ShapeData, ROIDrawingI):
        
    def __init__(self, roicoord = ROICoordinate(), bytes = None, x = 0, y = 0, width = 0, height = 0):
        ShapeData.__init__(self);
        self.x = rdouble(x);
        self.y = rdouble(y);
        self.width = rdouble(width);
        self.height = rdouble(height);
        self.bytesdata = bytes;
        self.setCoord(roicoord);
    
    def setGeometry(self, mask):
        mask.setTheZ(self.coord.theZ);
        mask.setTheT(self.coord.theZ);
        mask.setX(self.x);
        mask.setY(self.y);
        mask.setWidth(self.width);
        mask.setHeight(self.height);
        mask.setBytes(self.bytedata);

    def getGeometryFromROI(self, roi):
        self.x = roi.getX();
        self.y = roi.getY();
        self.width = roi.getWidth();
        self.height = roi.getHeight();
        self.bytesdata = roi.getBytes();

    def createBaseType(self):
        return MaskI();

    def acceptVisitor(self, visitor):
        visitor.drawMask(self.x.getValue(), self.y.getValue(), self.width.getValue(), self.height.getValue(), self.bytesdata, self.shapeSettings.getSettings());

class PointData(ShapeData, ROIDrawingI):
            
    def __init__(self, roicoord = ROICoordinate(), x = 0, y = 0):
        ShapeData.__init__(self);
        self.x = rdouble(x);
        self.y = rdouble(y);
        self.setCoord(roicoord);
    
    def setGeometry(self, point):
        point.setTheZ(self.coord.theZ);
        point.setTheT(self.coord.theZ);
        point.setX(self.x);
        point.setY(self.y);

    def getGeometryFromROI(self, roi):
        self.x = roi.getX();
        self.y = roi.getY();

    def createBaseType(self):
        return PointI();

    def acceptVisitor(self, visitor):
        visitor.drawPoint(self.x.getValue(), self.y.getValue(), self.shapeSettings.getSettings());


class PolygonData(ShapeData, ROIDrawingI):
    
    def __init__(self, roicoord = ROICoordinate(), pointsList = [0,0]):
        ShapeData.__init__(self);
        self.points = rstring(self.listToString(pointsList));
        self.setCoord(roicoord);
    
    def setGeometry(self, polygon):
        polygon.setTheZ(self.coord.theZ);
        polygon.setTheT(self.coord.theZ);
        polygon.setPoints(self.points);

    def getGeometryFromROI(self, roi):
        self.points = roi.getPoints();

    def listToString(self, pointsList):
        string = '';
        cnt = 0;
        for element in pointsList:
            if(cnt!=0):
                string = string + ',';
            cnt += 1;
            string = string + str(element);
        return string;

    def stringToTupleList(self, pointString):
        elements = [];
        list = pointString.split(',');
        numTokens = len(list);
        for tokenPair in range(0,numTokens/2):
            elements.append((int(list[tokenPair*2]), int(list[tokenPair*2+1])));
        return elements;

    def createBaseType(self):
        return PolygonI();

    def acceptVisitor(self, visitor):
        visitor.drawPolygon(self.stringToTupleList(self.points.getValue()), self.shapeSettings.getSettings());

class PolylineData(ShapeData, ROIDrawingI):
        
    def __init__(self, roicoord = ROICoordinate(), pointsList = [0,0]):
        ShapeData.__init__(self);
        self.points = rstring(self.listToString(pointsList));
        self.setCoord(roicoord);
    
    def setGeometry(self, point):
        point.setTheZ(self.coord.theZ);
        point.setTheT(self.coord.theZ);
        point.setPoints(self.points);

    def getGeometryFromROI(self, roi):
        self.points = roi.getPoints();

    def listToString(self, pointsList):
        string = '';
        cnt = 0;
        for element in pointsList:
            if(cnt > 0):
                string = string + ',';
            string = string + str(element);
            cnt+=1;
        return string;
            
    def stringToTupleList(self, pointString):
        elements = [];
        list = pointString.split(',');
        numTokens = len(list);
        for tokenPair in range(0,numTokens/2):
            elements.append((int(list[tokenPair*2]), int(list[tokenPair*2+1])));
        return elements;
            
        
    def createBaseType(self):
        return PolylineI();

    def acceptVisitor(self, visitor):
        visitor.drawPolyline(self.stringToTupleList(self.points.getValue()), self.shapeSettings.getSettings());

