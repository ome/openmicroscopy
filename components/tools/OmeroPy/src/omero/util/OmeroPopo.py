import math;
import numpy;
import omero.clients
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
# Create instance of data object this object wraps the basic OMERO types.
#
class DataObject():
    
    ##
    # Create instance.
    #
    def __init__(self):
        self.value = None;
                
     ##
     # Sets the {@link IObject}.
     # 
     # @param value The value to set.
     #
    def setValue(self, value):
        self.value = value;
    
    ##
    # get the id of the Dataobject.
    # @return See above.
    #
    def getId(self):
        return self.id;
        
    ##
    # Set the id of the data object
    # @param id See above.
    def setId(self, id):
        self.id = id;
  
    ##
    # Get the current object.
    # @return See above.
    #
    def asIObject(self):
        return self.value;

class ImageData(DataObject):
    
    ##
    # Create Instance
    #
    def __init__(self):
        DataObject.__init__(self)
        
    ##
    # Sets the name of the image.
    # 
    # @param name
    # The name of the image. Mustn't be <code>null</code>.
    #
    def setName(self, name):
        self.name = name

    ##
    # Returns the name of the image.
    # 
    # @return See above.
    #
    def getName(self):
        return self.name;
    
    ##
    # Sets the description of the image.
    # 
    # @param description
    #            The description of the image.
    #
    def setDescription(self, description):
        self.description = description;

    ##
    # Returns the description of the image.
    # 
    # @return See above.
    #
    def getDescription(self):
        return self.description

    
##
# This class stores the ROI Coordinate (Z,T).
#
class ROICoordinate:
    
    ##
    # Initialise the ROICoordinate.
    # @param z The z-section.
    # @param t The timepoint.
    def __init__(self, z = 0, t = 0):
        self.theZ = z;
        self.theT = t;
        self.ZBITSPLIT = 18;

    ##
    # Overload the equals operator 
    #    
    def __eq__(self, obj):
        if(self.theZ == obj.theZ and self.theT == obj.theT):
            return True;
        return False;
    
    ##
    # Overload the equals operator 
    #    
    def __ne__(self, obj):
        if(self.theZ != obj.theZ or self.theT != obj.theT):
            return True;
        return False;
    
    ##
    # Overload the lessthan or equals operator 
    #    
    def __lt__(self, obj):
        if(self.theT >= obj.theT):
            return False;
        if(self.theZ >= obj.theZ):
            return False;
        return True;
        
    ##
    # Overload the lessthan or equals operator 
    #    
    def __le__(self, obj):
        if(self.theT < obj.theT):
            return False;
        if(self.theZ < obj.theZ):
            return False;
        return True;
    
    ##
    # Overload the greater than equals operator 
    #    
    def __gt__(self, obj):
        if(self.theT <= obj.theT):
            return False;
        if(self.theZ <= obj.theZ):
            return False;
        return True;

    ##
    # Overload the greater than or equals operator 
    #    
    def __ge__(self, obj):
        if(self.theT < obj.theT):
            return False;
        if(self.theZ < obj.theZ):
            return False;
        return True;
    
    ##
    # Overload the hash operator 
    #    
    def __hash__(self):
        return self.theZ<<self.ZBITSPLIT+self.theT;     

    ##
    # Returns the timepoint.
    # 
    # @return See above.
    #
    def getTimepoint(self):
       return self.theT;
        
    ##
    # Returns the Z-Section.
    # 
    # @return See above.
    #
    def getZSection(self):
       return self.theZ;
    
    ##
    # Set the Z-Section of the Coordinate
    # @param z See above.
    #
    def setZSection(self, z):
        self.z = z;

    ##
    # Set the Timepoint of the Coordinate
    # @param t See above.
    #
    def setTimepoint(self, t):
        self.t = t;
        

##
# This class defines the python mapping of the ROIData object {See Pojos#ROIData} 
#
class ROIData(DataObject):
    
    ##
    # Create a new instance of an ROIData object.
    #
    def __init__(self):
        DataObject.__init__(self)
        self.roiShapes = {};
        
        
    ##
    # Set the imageId for the ROI.
    # @param imageId See above.
    #
    def setImage(self, image):
        self.image = image;
    
    ##
    # Get the image for the ROI.
    # @return See above.
    #
    def getImage(self):
        return self.image;
        
    ##
    # Add ShapeData object to ROIData.
    # @param shape See above.
    #
    def addShapeData(self, shape):
        coord = shape.getROICoordinate();
        shapeList = None;
        if(self.roiShapes.has_key(coord) == False):
            shapeList = list();
            self.roiShapes[coord] = shapeList;
        else:
            shapeList = self.roiShapes[coord];
        shapeList.append(shape);
    
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
        count = 0;
        for coord in self.roiShapes:
            list = self.roiShapes[coord];
            count = count + len(list);
        return count;

    ##
    # Returns the list of shapes on a given plane.
    # 
    # @param z The z-section.
    # @param t The timepoint.
    # @return See above.
    #
    def getShapes(self, z, t):
        return self.roiShapes[ROICoordinate(z,t)];
    
    ##
    # Returns the iterator of the collection of the map.
    # 
    # @return See above.
    #
    def getIterator(self):
        return self.roiShapes.iteritems();
        
    ##
    # Returns an iterator of the Shapes in the ROI in the range [start, end].
    # 
    # @param start The starting plane where the Shapes should reside.
    # @param end The final plane where the Shapes should reside.
    # @return See above.
    #
    def getShapesInRange(self, start, end):
        coordList = self.roiShapes.keys();
        coordList.sort();
        keyList = [];
        for coord in coordList:
            if(coord>=start and coord <= end):
                keyList.append(coord);
        return self.roiShapes.from_keys(keyList);

    ##
    # Returns the namespace of the ROI.
    #
    # @return see above.
    #
    def getNamespace(self):
        roi = self.asIObject();
        if(roi==None):
            raise Exception("No Roi specified.");
        ns = roi.getNs();
        if(ns!=None):
            return ns.getValue();
        return "";

    ##
    # Returns the keywords of the ROI.
    #
    # @return see above.
    #
    def getKeywords(self):
        roi = self.asIObject();
        if(roi==None):
            raise Exception("No Roi specified.");
        keywords = roi.getKeywords();
        if(keywords!=None):
            return keywords.getValue();
        return "";        

    ##
    # Set the namespace of the ROI.
    # @param namespace See above.
    #
    def setNamespace(self, namespace):
        roi = self.asIObject();
        if(roi==None):
            raise Exception("No Roi specified.");
        roi.setNs(rtypes.string(namespace));
    
    ##
    # Set the keywords of the ROI.
    # @param keywords See above.
    #
    def setKeywords(self, keywords):
        roi = self.asIObject();
        if(roi==None):
            raise Exception("No Roi specified.");
        roi.setKeywords(rtypes.string(keywords));
    
class ShapeData(DataObject):
    
    def __init__(self):
        DataObject.__init__(self)
        self.coord = ROICoordinate();
        self.text = None;
    
    ##
    # Returns the z-section.
    # 
    # @return See above.
    #
    def getZ(self):
        return self.coord.getZSection();
    
    ##
    # Set the z-section.
    # @param theZ See above.
    #
    def setZ(self, theZ):
        self.coord.setZSection(theZ);    

    ##
    # Returns the timepoint.
    # 
    # @return See above.
    #
    def getT(self):   
        return self.coord.getTimepoint();

    ##
    # Set the timepoint.
    # @param See above.
    #
    def setT(self, theT):  
        self.coord.setTimepoint(theT);
        
    ## 
    # Set the ROICoordinate for the ShapeData 
    # @param roiCoordinate See above.
    #
    def setROICoordinate(self, coord):
       self.coord = coord;
       
    ##
    # Get the ROICoordinate for the ShapeData 
    # @return See above.
    #
    def getROICoordinate(self):
        return self.coord;
    
    ##
    # Get the text for the Object
    # @return See above.
    def getText(self):
        return self.text;
    
    ## 
    # Set the text for the Obect. 
    # @param See above.
    def setText(self, text):
        self.text = text;

    ##
    # Get the affinetransform from the object, returned as a string matrix(m00 m01 m10 m11 m02 m12) 
    # see Java affinetransform toMatrix. 
    # @return see above.
    # 
    def getTransform(self):
        shape = self.asIObject();
        if(shape==None):
            raise Exception("No Shape specified.");
        transform = shape.getTransform();
        if(transform!=None):
            return transform.getValue();
        return "";
    
    ##
    # Transform the affine transform matrix from the string 'matrix(m00 m01 m10 m 11 m02 m12)' to a 
    # more appropriate numpy.array([m00 m01 m02], [m10 m11 m12]).
    #
    #
    def transformToMatrix(self, str):
        if(str==""):
            return numpy.matrix([[1,0,0],[0,1,0]])
        transformstr = str[str.find('(')+1:len(str)-1];
        values = transformstr.split(' ');
        b = numpy.matrix(values, dtype='double');
        t = numpy.matrix(numpy.zeros((3,3)));
        t[0,0] = b[0];
        t[0,1] = b[1];
        t[1,0] = b[2];
        t[1,1] = b[3];
        t[0,2] = b[4];
        t[1,2] = b[5];
        t[2,2] = 1;
        return t;
    
    ##
    # does the shape contain the point
    # @return see above.
    #
    def contains(self, point):
        return false;
        
    ##
    #
    #
    def containsPoints(self):
        return [];

##
# Instance of the EllipseData Object
# 
class EllipseData(ShapeData):
    
    ##
    # Create instance of EllipseData Object
    # 
    def __init__(self, shape = None):
        ShapeData.__init__(self);
        if(shape==None):
            self.setValue(EllipseI());
            self.setCx(0);
            self.setCy(0);
            self.setRx(0);
            self.setRy(0);
        else:
            self.setValue(shape);
        
    ## 
    # Set the centre x coord of the Ellipse
    # @param cx See above.
    def setCx(self, cx):
        shape = self.asIObject();
        if(shape==None):
            raise Exception("No Shape specified.");
        shape.setCx(rdouble(cx));

    ## 
    # Get the centre x coord of the Ellipse
    # @return See Above. 
    def getCx(self):
        shape = self.asIObject();
        if(shape==None):
            raise Exception("No Shape specified.");
        cx = shape.getCx();
        if(cx==None):
            return 0;
        return cx.getValue();
        
    ## 
    # Set the centre y coord of the Ellipse
    # @param cy See above.
    def setCy(self, cy):
        shape = self.asIObject();
        if(shape==None):
            raise Exception("No Shape specified.");
        shape.setCy(rdouble(cy));

    ## 
    # Get the centre y coord of the Ellipse
    # @return See Above. 
    def getCy(self):
        shape = self.asIObject();
        if(shape==None):
            raise Exception("No Shape specified.");
        cy = shape.getCy();
        if(cy==None):
            return 0;
        return cy.getValue();

    ## 
    # Set the radius on the x-axis of the Ellipse
    # @param rx See above.
    def setRx(self, rx):
        shape = self.asIObject();
        if(shape==None):
            raise Exception("No Shape specified.");
        shape.setRx(rdouble(rx));

    ## 
    # Get the radius of the x-axis of the Ellipse
    # @return See Above. 
    def getRx(self):
        shape = self.asIObject();
        if(shape==None):
            raise Exception("No Shape specified.");
        rx = shape.getRx();
        if(rx==None):
            return 0;
        return rx.getValue();

    ## 
    # Set the radius on the y-axis of the Ellipse
    # @param rx See above.
    def setRy(self, ry):
        shape = self.asIObject();
        if(shape==None):
            raise Exception("No Shape specified.");
        shape.setRy(rdouble(ry));
        
    ## 
    # Get the radius of the y-axis of the Ellipse
    # @return See Above. 
    def getRy(self):
        shape = self.asIObject();
        if(shape==None):
            raise Exception("No Shape specified.");
        ry = shape.getRy();
        if(ry==None):
            return 0;
        return ry.getValue();
    
    def transformPoint(self, transform, point):
        p = numpy.matrix(point).transpose();
        return transform*p;
        
    def containsPoints(self):
        transform = self.transformToMatrix(self.getTransform());
        cx = self.getCx();
        cy = self.getCy();
        rx = self.getRx();
        ry = self.getRy();
        point = numpy.matrix((cx, cy, 1)).transpose();
        centre = transform*point;
        BL = numpy.matrix((cx-rx, cy+ry, 1)).transpose();
        BR = numpy.matrix((cx+rx, cy+ry, 1)).transpose();
        TL = numpy.matrix((cx-rx, cy-ry, 1)).transpose();
        TR = numpy.matrix((cx+rx, cy-ry, 1)).transpose();
        MajorAxisLeft = numpy.matrix((cx-rx, cy, 1)).transpose();
        MajorAxisRight = numpy.matrix((cx+rx, cy, 1)).transpose();
        MinorAxisTop = numpy.matrix((cx, cy-ry, 1)).transpose();
        MinorAxisBottom = numpy.matrix((cx, cy+ry, 1)).transpose();
        lb = transform*BL;
        rb = transform*BR;
        lt = transform*TL;
        rt = transform*TR;
        majl = transform*MajorAxisLeft;
        majr = transform*MajorAxisRight;
        mint = transform*MinorAxisTop;
        minb = transform*MinorAxisBottom;
        o = (majr[1]-majl[1]);
        a = (majr[0]-majl[0]);
        h = math.sqrt(o*o+a*a);
        majorAxisAngle = math.asin(o/h); 
        boundingBoxMinX = min(lb[0], rb[0], lt[0], rt[0]);
        boundingBoxMaxX = max(lb[0], rb[0], lt[0], rt[0]);
        boundingBoxMinY = min(lb[1], rb[1], lt[1], rt[1]);
        boundingBoxMaxY = max(lb[1], rb[1], lt[1], rt[1]);
        boundingBox = ((boundingBoxMinX, boundingBoxMinY), (boundingBoxMaxX, boundingBoxMaxY));
        centredBoundingBox = ((boundingBox[0][0]-centre[0],boundingBox[0][1]-centre[1]),(boundingBox[1][0]-centre[0],boundingBox[1][1]-centre[1]))
        points = {};
        xrange =  range(centredBoundingBox[0][0], centredBoundingBox[1][0])
        yrange = range(centredBoundingBox[0][1], centredBoundingBox[1][1])
        for x in xrange:
            for y in yrange:
                newX = x*math.cos(majorAxisAngle)+y*math.sin(majorAxisAngle);
                newY = -x*math.sin(majorAxisAngle)+y*math.cos(majorAxisAngle);
                val = (newX*newX)/(rx*rx)+ (newY*newY)/(ry*ry);
                if(val <= 1):
                    points[(x,y)]=1;
        return points;              
    
##
# Instance of the Mask Object
# 
class MaskData(ShapeData):
    
    ##
    # Create instance of MaskData Object
    # 
    def __init__(self, maskShape=None):
        ShapeData.__init__(self);
        if(maskShape==None):
            self.setValue(MaskI());
            self.setX(0);
            self.setY(0);
            self.setWidth(0);
            self.setHeight(0);
            self.setMask(None);
        else:
            self.setValue(maskShape);
            
    ## 
    # Set the x coord of the Mask
    # @param x See above.
    def setX(self, x):
        shape = self.asIObject();
        if(shape==None):
            raise Exception("No Shape specified.");
        shape.setX(rdouble(x));

    ## 
    # Get the x coord of the Mask
    # @return See Above. 
    def getX(self):
        shape = self.asIObject();
        if(shape==None):
            raise Exception("No Shape specified.");
        x = shape.getX();
        if(x==None):
            return 0;
        return x.getValue();
   
    ## 
    # Set the y coord of the Mask
    # @param y See above.
    def setY(self, y):
        shape = self.asIObject();
        if(shape==None):
            raise Exception("No Shape specified.");
        shape.setY(rdouble(y));

    ## 
    # Get the y coord of the Mask
    # @return See Above. 
    def getY(self):
        shape = self.asIObject();
        if(shape==None):
            raise Exception("No Shape specified.");
        y = shape.getY();
        if(y==None):
            return 0;
        return y.getValue();
    ## 
    # Set the width the Mask
    # @param width See above.
    def setWidth(self, width):
        shape = self.asIObject();
        if(shape==None):
            raise Exception("No Shape specified.");
        shape.setWidth(rdouble(width));

    ## 
    # Get the width of the Mask
    # @return See Above. 
    def getWidth(self):
        shape = self.asIObject();
        if(shape==None):
            raise Exception("No Shape specified.");
        width = shape.getWidth();
        if(width==None):
            return 0;
        return width.getValue();
    ## 
    # Set the height of the Mask
    # @param height See above.
    def setHeight(self, height):
        shape = self.asIObject();
        if(shape==None):
            raise Exception("No Shape specified.");
        shape.setHeight(rdouble(height));
        
    ## 
    # Get the height of the Mask
    # @return See Above. 
    def getHeight(self):
        shape = self.asIObject();
        if(shape==None):
            raise Exception("No Shape specified.");
        height = shape.getHeight();
        if(height==None):
            return 0;
        return height.getValue();
    ## 
    # Set the bitmask of the Mask
    # @param See Above. 
    def setMask(self, mask):
        shape = self.asIObject();
        if(shape==None):
            raise Exception("No Shape specified.");
        shape.setMask(mask);
    
    ## 
    # Get the bitmask of the Mask
    # @return See Above. 
    def getMask(self):
        shape = self.asIObject();
        if(shape==None):
            raise Exception("No Shape specified.");
        mask = shape.getMask();
        if(x==None):
            return 0;
        return mask.getValue();        


##
# Instance of the RectangleData object
# 
class RectData(ShapeData):
    
    ##
    # Create instance of MaskData Object
    # 
    def __init__(self, rectShape=None):
        ShapeData.__init__(self);
        if(rectShape==None):
            self.setValue(RectI());
            self.setX(0);
            self.setY(0);
            self.setWidth(0);
            self.setHeight(0);
            self.setMask(None);
        else:
            self.setValue(rectShape);
            
    ## 
    # Set the x coord of the Rectangle
    # @param x See above.
    def setX(self, x):
        shape = self.asIObject();
        if(shape==None):
            raise Exception("No Shape specified.");
        shape.setX(rdouble(x));

    ## 
    # Get the x coord of the Rectangle
    # @return See Above. 
    def getX(self):
        shape = self.asIObject();
        if(shape==None):
            raise Exception("No Shape specified.");
        x = shape.getX();
        if(x==None):
            return 0;
        return x.getValue();
   
    ## 
    # Set the y coord of the Rectangle
    # @param y See above.
    def setY(self, y):
        shape = self.asIObject();
        if(shape==None):
            raise Exception("No Shape specified.");
        shape.setY(rdouble(y));

    ## 
    # Get the y coord of the Rectangle
    # @return See Above. 
    def getY(self):
        shape = self.asIObject();
        if(shape==None):
            raise Exception("No Shape specified.");
        y = shape.getY();
        if(y==None):
            return 0;
        return y.getValue();
    ## 
    # Set the width the Rectangle
    # @param width See above.
    def setWidth(self, width):
        shape = self.asIObject();
        if(shape==None):
            raise Exception("No Shape specified.");
        shape.setWidth(rdouble(width));

    ## 
    # Get the width of the Rectangle
    # @return See Above. 
    def getWidth(self):
        shape = self.asIObject();
        if(shape==None):
            raise Exception("No Shape specified.");
        width = shape.getWidth();
        if(width==None):
            return 0;
        return width.getValue();
    ## 
    # Set the height of the Rectangle
    # @param height See above.
    def setHeight(self, height):
        shape = self.asIObject();
        if(shape==None):
            raise Exception("No Shape specified.");
        shape.setHeight(rdouble(height));
        
    ## 
    # Get the height of the Rectangle
    # @return See Above. 
    def getHeight(self):
        shape = self.asIObject();
        if(shape==None):
            raise Exception("No Shape specified.");
        height = shape.getHeight();
        if(height==None):
            return 0;
        return height.getValue();
        
    def containsPoints(self):
        points = {};
        offsetX = self.getX();
        offsetY = self.getY();
        for x in range(self.getWidth()):
            for y in range(self.getHeight()):
                points[(offsetX+x,offsetY+y)] = 1;
        return points;
