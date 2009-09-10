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

def abstract():
    import inspect
    caller = inspect.getouterframes(inspect.currentframe())[1][3]
    raise NotImplementedError(caller + ' must be implemented in subclass')


class ShapeSettingsData:

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
        
    def setStrokeSettings(self, colour, width = 1, opacity = 0):
        self.strokeColour = rsting(colour);
        self.strokeWidth = rint(width);
        self.strokeOpacity = rfloat(opacity);

    def setFillSettings(self, colour, opacity = 0):
        self.fillColour = rsting(colour);
        self.fillOpacity = rfloat(opacity);   
        
class ROICoordinate:
    
    def __init__(self, z = 0, t = 0):
        self.theZ = rint(z);
        self.theT = rint(t);

    def setROICoord(self, roi):
        roi.setTheZ(roi.theZ);
        roi.setTheT(roi.theT);

class ShapeData:

    def __init__(self):
        self.coord = ROICoordinate();
        self.shapeSettings = ShapeSettingsData();
        
    def setCoord(self, coord):
        self.coord = coord;
    
    def setROICoord(self, roi):
        self.coord.setROICoord(roi);
        
    def setROIGeometry(self, shape):
        abstract();

    def setShapeSettings(self, settings):
        self.shapeSettings = settings;
    
    def setROIShapeSettings(self, roi):
        self.shapeSettings.setShapeSettings(roi);

    def acceptVisitor(self, visitor):
        abstract();

    def createBaseType(self):
        abstract();
        
    def createROI(self):
        roi = createBaseType();
        self.setROICoord(roi);
        self.setROIGeometry(roi);
        self.setROIShapeSettings(roi);
        return roi;
        
class EllipseData(ShapeData):
        
    def __init__(self, roicoord = ROICoordinate(), cx = 0, cy = 0, rx = 0, ry = 0):
        self.cx = rdouble(cx);
        self.cy = rdouble(cy);
        self.rx = rdouble(rx);
        self.ry = rdouble(ry);
        self.setROICoord(roicoord);
        
    def setROIGeometry(self, ellipse):
        ellipse.setTheZ(self.coord.theZ);
        ellipse.setTheT(self.coord.theZ);
        ellipse.setCx(self.cx);
        ellipse.setCy(self.cy);
        ellipse.setRx(self.rx);
        ellipse.setRy(self.ry);

    def createBaseType(self):
        return EllipseI();

    def acceptVisitor(self, visitor):
        visitor.visitEllipse(cx, cy, rx, ry);

class RectangleData(ShapeData):
        
    def __init__(self, roicoord = ROICoordinate(), x = 0, y = 0, width = 0, height = 0):
        self.x = rdouble(x);
        self.y = rdouble(y);
        self.width = rdouble(width);
        self.height = rdouble(height);
        self.setROICoord(roicoord);
    
    def setGeometry(self, rectangle):
        rectangle.setTheZ(self.coord.theZ);
        rectangle.setTheT(self.coord.theZ);
        rectangle.setX(self.x);
        rectangle.setY(self.y);
        rectangle.setWidth(self.width);
        rectangle.setHeight(self.height);

    def createBaseType(self):
        return RectI();

    def acceptVisitor(self, visitor):
        visitor.visitRectangle(self.x, self.y, self.width, self.height);

class LineData(ShapeData):
        
    def __init__(self, roicoord = ROICoordinate(), x1 = 0, y1 = 0, x2 = 0, y2 = 0):
        self.x1 = rdouble(x1);
        self.y1 = rdouble(y1);
        self.x2 = rdouble(x2);
        self.y2 = rdouble(y2);
        self.setROICoord(roicoord);
    
    def setGeometry(self, line):
        line.setTheZ(self.coord.theZ);
        line.setTheT(self.coord.theZ);
        line.setX1(self.x1);
        line.setY1(self.y1);
        line.setX2(self.x2);
        line.setY2(self.y2);

    def createBaseType(self):
        return LineI();

class MaskData(ShapeData):
        
    def __init__(self, roicoord = ROICoordinate(), bytes = None, x = 0, y = 0, width = 0, height = 0):
        self.x = rdouble(x);
        self.y = rdouble(y);
        self.width = rdouble(width);
        self.height = rdouble(height);
        self.bytesdata = bytes;
        self.setROICoord(roicoord);
    
    def setGeometry(self, mask):
        mask.setTheZ(self.coord.theZ);
        mask.setTheT(self.coord.theZ);
        mask.setX(self.x);
        mask.setY(self.y);
        mask.setWidth(self.width);
        mask.setHeight(self.height);
        mask.setBytes(self.bytedata);

    def createBaseType(self):
        return MaskI();

class PointData(ShapeData):
            
    def __init__(self, roicoord = ROICoordinate(), x = 0, y = 0):
        self.x = rdouble(x);
        self.y = rdouble(y);
        self.setROICoord(roicoord);
    
    def setGeometry(self, point):
        point.setTheZ(self.coord.theZ);
        point.setTheT(self.coord.theZ);
        point.setX(self.x);
        point.setY(self.y);

    def createBaseType(self):
        return PointI();

class PolygonData(ShapeData):
    
    def __init__(self, roicoord = ROICoordinate(), pointsList = [0,0]):
        self.points = rstring(self.listToString(pointsList));
        self.setROICoord(roicoord);
    
    def setGeometry(self, point):
        point.setTheZ(self.coord.theZ);
        point.setTheT(self.coord.theZ);
        point.setPoints(self.points);

    def listToString(pointsList):
        string = '';
        cnt = 0;
        for element in pointsList:
            if(cnt > 0 and cnt % 2 == 0 ):
                string = string + ',';
            string = string + str(element);
        return string;

    def createBaseType(self):
        return PolygonI();

class PolylineData(ShapeData):
        
    def __init__(self, roicoord = ROICoordinate(), pointsList = [0,0]):
        self.points = rstring(self.listToString(pointsList));
        self.setROICoord(roicoord);
    
    def setGeometry(self, point):
        point.setTheZ(self.coord.theZ);
        point.setTheT(self.coord.theZ);
        point.setPoints(self.points);

    def listToString(pointsList):
        string = '';
        cnt = 0;
        for element in pointsList:
            if(cnt > 0 and cnt % 2 == 0 ):
                string = string + ',';
            string = string + str(element);
        return string;
        
    def createBaseType(self):
        return PolylineI();


