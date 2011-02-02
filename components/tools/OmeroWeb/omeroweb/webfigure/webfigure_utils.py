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
    
def getRoiShapes(roiService, imageId):
    
    rois = []     
    
    result = roiService.findByImage(imageId, None)
    
    for r in result.rois:
        roi = {}
        roi['id'] = r.getId().getValue()
        # go through all the shapes of the ROI
        shapes = []
        for s in r.copyShapes():
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
            except AttributeError: pass
            shapes.append(shape)
        roi['shapes'] = shapes
        rois.append(roi)
    
    return rois