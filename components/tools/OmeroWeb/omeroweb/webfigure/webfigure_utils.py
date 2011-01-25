import omero
from omero.model import RectI, EllipseI, LineI, PointI
def getRoiShapes(roiService, imageId):
    
    rois = []     
    
    result = roiService.findByImage(imageId, None)
    
    for r in result.rois:
        roi = {}
        roi['id'] = r.getId().getValue()
        # go through all the shapes of the ROI
        for s in r.copyShapes():
            shapes = []
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
                if s.getTextValue():
                    shape['textValue'] = s.getTextValue().getValue()
            elif type(s) == omero.model.EllipseI:
                shape['type'] = 'Ellipse'
                shape['cx'] = s.getCx().getValue()
                shape['cy'] = s.getCy().getValue()
                shape['rx'] = s.getRx().getValue()
                shape['ry'] = s.getRy().getValue()
                if s.getTextValue():
                    shape['textValue'] = s.getTextValue().getValue()
            elif type(s) == omero.model.LineI:
                shape['type'] = 'Line'
                shape['x1'] = s.getX1().getValue()
                shape['x2'] = s.getX2().getValue()
                shape['y1'] = s.getY1().getValue()
                shape['y2'] = s.getY2().getValue()
                if s.getTextValue():
                    shape['textValue'] = s.getTextValue().getValue()
            elif type(s) == omero.model.PointI:
                shape['type'] = 'Point'
                shape['cx'] = s.getCx().getValue()
                shape['cy'] = s.getCy().getValue()
                if s.getTextValue():
                    shape['textValue'] = s.getTextValue().getValue()
            shapes.append(shape)
        roi['shapes'] = shapes
        rois.append(roi)
    
    return rois