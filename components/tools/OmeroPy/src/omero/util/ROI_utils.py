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


class ROIProperties:

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
        
    def setShapeProperties(self, shape):
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
        
class ROICoordinate:
    
    
    def __init__(self, z, t):
        self.theZ = rint(z);
        self.theT = rint(t);

    def __init__(self):
        self.theZ = rint(0);
        self.theT = rint(0);

class ShapeData:

    def __init__(self):
        self.coord = ROICoordinate();

    def setGeometry(self, shape):
        abstract();

class EllipseData(ShapeData):
    
    def __init__(self):
        self.cx = rdouble(0);
        self.cy = rdouble(0);
        self.rx = rdouble(0);
        self.ry = rdouble(0);
    
    def __init__(self, roicoord, x, y, rx, ry):
        self.cx = rdouble(cx);
        self.cy = rdouble(cy);
        self.rx = rdouble(rx);
        self.ry = rdouble(ry);
        self.coord = roicoord;
    
    def setGeomery(self, ellipse):
        ellipse.setTheZ(self.coord.theZ);
        ellipse.setTheT(self.coord.theZ);
        ellipse.setCx(self.cx);
        ellipse.setCy(self.cy);
        ellipse.setRx(self.rx);
        ellipse.setRy(self.ry);

class RectangleData(ShapeData):
    
    def __init__(self):
        self.x = rdouble(0);
        self.y = rdouble(0);
        self.width = rdouble(0);
        self.height = rdouble(0);
    
    def __init__(self, roicoord, x, y, width, height):
        self.x = rdouble(x);
        self.y = rdouble(y);
        self.width = rdouble(width);
        self.height = rdouble(height);
        self.coord = roicoord;
    
    def setGeomery(self, rectangle):
        rectangle.setTheZ(self.coord.theZ);
        rectangle.setTheT(self.coord.theZ);
        rectangle.setX(self.x);
        rectangle.setY(self.y);
        rectangle.setWidth(self.width);
        rectangle.setHeight(self.height);
        
class LineData(ShapeData):
    
    def __init__(self):
        self.x1 = rdouble(0);
        self.y1 = rdouble(0);
        self.x2 = rdouble(0);
        self.y2 = rdouble(0);
    
    def __init__(self, roicoord, x1, y1, x2, y2):
        self.x1 = rdouble(x1);
        self.y1 = rdouble(y1);
        self.x2 = rdouble(x2);
        self.y2 = rdouble(y2);
        self.coord = roicoord;
    
    def setGeomery(self, line):
        line.setTheZ(self.coord.theZ);
        line.setTheT(self.coord.theZ);
        line.setX1(self.x1);
        line.setY1(self.y1);
        line.setX2(self.x2);
        line.setY2(self.y2);

class MaskData(ShapeData):
    
    def __init__(self):
        self.x = rdouble(0);
        self.y = rdouble(0);
        self.width = rdouble(0);
        self.height = rdouble(0);
        
    def __init__(self, roicoord, bytes, x, y, width, height):
        self.x = rdouble(x);
        self.y = rdouble(y);
        self.width = rdouble(width);
        self.height = rdouble(height);
        self.bytesdata = bytes;
        self.coord = roicoord;
    
    def setGeomery(self, mask):
        mask.setTheZ(self.coord.theZ);
        mask.setTheT(self.coord.theZ);
        mask.setX(self.x);
        mask.setY(self.y);
        mask.setWidth(self.width);
        mask.setHeight(self.height);
        mask.setBytes(self.bytedata);

class PointData(ShapeData):
    
    def __init__(self):
        self.x = rdouble(0);
        self.y = rdouble(0);
        
    def __init__(self, roicoord, x, y):
        self.x = rdouble(x);
        self.y = rdouble(y);
        self.coord = roicoord;
    
    def setGeomery(self, point):
        point.setTheZ(self.coord.theZ);
        point.setTheT(self.coord.theZ);
        point.setX(self.x);
        point.setY(self.y);

class PolygonData(ShapeData):
    
    def __init__(self):
        self.points = rstring('');
    
    def __init__(self, roicoord, pointsList):
        self.points = rstring(self.listToString(pointsList));
        self.coord = roicoord;
    
    def setGeomery(self, point):
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

class PolylineData(ShapeData):
    
    def __init__(self):
        self.points = rstring('');
    
    def __init__(self, roicoord, pointsList):
        self.points = rstring(self.listToString(pointsList));
        self.coord = roicoord;
    
    def setGeomery(self, point):
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


def createDefaultProperties():
    properties = ROIProperties();
    return properties;

def createEllipse(ellipsedata, properties):
    ellipse = EllipseI();
    ellipsedata.setGeometry(ellipse);
    properties.setShapeProperties(ellipse);

def createRectangle(rectangledata, properties):
    rectangle = RectangleI();
    rectangledata.setGeometry(rectangle);
    properties.setShapeProperties(rectangle);
    
def createLine(linedata, properties):
    line = LineI();
    linedata.setGeometry(line);
    properties.setShapeProperties(line);
    
def createMask(maskdata, properties):
    mask = MaskI();
    maskdata.setGeometry(mask);
    properties.setShapeProperties(mask);
    
def createPoint(pointdata, properties):
    point = PointI();
    pointdata.setGeometry(point);
    properties.setShapeProperties(point);
    
def createPolygon(polygondata, properties):
    polygon = PolygonI();
    polygondata.setGeometry(polygon);
    properties.setShapeProperties(polygon);
    
def createPolyline(polylinedata, properties):
    polyline = PolylineI();
    polylinedata.setGeometry(polyline);
    properties.setShapeProperties(polyline);


