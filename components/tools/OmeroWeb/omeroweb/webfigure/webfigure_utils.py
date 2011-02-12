import omero
from omero.model import RectI, EllipseI, LineI, PointI, PolygonI

def stringToSvg(string):
    """
    Method for converting the string returned from omero.model.ShapeI.getPoints()
    into an SVG for display on web.
    E.g: "points[309,427, 366,503, 190,491] points1[309,427, 366,503, 190,491] points2[309,427, 366,503, 190,491]"
    To: M 309 427 L 366 503 L 190 491 z
    """
    
    firstList = string.strip().split("points")[1]
    nums = firstList.strip("[]").replace(", ", " L").replace(",", " ")
    return "M" + nums + "z"


def rgb_int2css(rgbint):
    """
    converts a bin int number into css colour, E.g. -1006567680 to '#00ff00'
    """
    r,g,b = (rgbint // 256 // 256 % 256, rgbint // 256 % 256, rgbint % 256)
    return "#%02x%02x%02x" % (r,g,b)    # format hex
      
def getRoiShapes(roiService, imageId):
    
    rois = []     
    
    result = roiService.findByImage(imageId, None)
    
    for r in result.rois:
        roi = {}
        roi['id'] = r.getId().getValue()
        # go through all the shapes of the ROI
        shapes = []
        for s in r.copyShapes():
            print type(s)
            shape = {}
            shape['id'] = s.getId().getValue()
            shape['theT'] = s.getTheT().getValue()
            shape['theZ'] = s.getTheZ().getValue()
            if type(s) == omero.model.RectI:
                shape['type'] = 'Rectangle'
                shape['x'] = s.getX().getValue()
                shape['y'] = s.getY().getValue()
                shape['width'] = s.getWidth().getValue()
                shape['height'] = s.getHeight().getValue()
            if type(s) == omero.model.MaskI:
                shape['type'] = 'Mask'
                shape['x'] = s.getX().getValue()
                shape['y'] = s.getY().getValue()
                shape['width'] = s.getWidth().getValue()
                shape['height'] = s.getHeight().getValue()
                # TODO: support for mask
                print s.getPixels()
                print s.getBytes()
            elif type(s) == omero.model.EllipseI:
                shape['type'] = 'Ellipse'
                shape['cx'] = s.getCx().getValue()
                shape['cy'] = s.getCy().getValue()
                shape['rx'] = s.getRx().getValue()
                shape['ry'] = s.getRy().getValue()
            elif type(s) == omero.model.PolylineI:
                shape['type'] = 'PolyLine'
                shape['points'] = stringToSvg(s.getPoints().getValue())
            elif type(s) == omero.model.LineI:
                shape['type'] = 'Line'
                shape['x1'] = s.getX1().getValue()
                shape['x2'] = s.getX2().getValue()
                shape['y1'] = s.getY1().getValue()
                shape['y2'] = s.getY2().getValue()
            elif type(s) == omero.model.PointI:
                shape['type'] = 'Point'
                shape['cx'] = s.getCx().getValue()
                shape['cy'] = s.getCy().getValue()
            elif type(s) == omero.model.PolygonI:
                shape['type'] = 'Polygon'
                shape['points'] = stringToSvg(s.getPoints().getValue())
            try:
                if s.getTextValue() and s.getTextValue().getValue():
                    shape['textValue'] = s.getTextValue().getValue()
                    # only populate json with font styles if we have some text
                    if s.getFontSize() and s.getFontSize().getValue():
                        shape['fontSize'] = s.getFontSize().getValue()
                    if s.getFontStyle() and s.getFontStyle().getValue():
                        shape['fontStyle'] = s.getFontStyle().getValue()
                    if s.getFontFamily() and s.getFontFamily().getValue():
                        shape['fontFamily'] = s.getFontFamily().getValue()
            except AttributeError: pass
            if s.getTransform():
                t = s.getTransform().getValue()
                if t and t != 'none':
                    shape['transform'] = t
            if s.getFillColor() and s.getFillColor().getValue():
                shape['fillColor'] = rgb_int2css(s.getFillColor().getValue())
            if s.getStrokeColor() and s.getStrokeColor().getValue():
                shape['strokeColor'] = rgb_int2css(s.getStrokeColor().getValue())
            if s.getStrokeWidth() and s.getStrokeWidth().getValue():
                shape['strokeWidth'] = s.getStrokeWidth().getValue()
            shapes.append(shape)
            # sort shapes by Z, then T. 
            shapes.sort(key=lambda x: "%03d%03d"% (x['theZ'],x['theT']) );
        roi['shapes'] = shapes
        rois.append(roi)
    
    return rois