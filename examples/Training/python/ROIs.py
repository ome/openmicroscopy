from omero.gateway import BlitzGateway
from omero.rtypes import *
from omero.model import *
user = 'will'
pw = 'ome'
host = 'localhost'
imageId = 101
conn = BlitzGateway(user, pw, host=host, port=4064)
conn.connect()
updateService = conn.getUpdateService()
x = 50
y = 200
width = 3
height = 2

# Create ROI. 

# We are using the core Python API and omero.model objects here, since ROIs are not
# yet supported in the Python Blitz Gateway.

#In this example, we create an ROI with a rectangular shape and attach it to an image. 

image = conn.getObject("Image", imageId)
theZ = image.getSizeZ()/2
theT = 0
print "Adding a rectangle at theZ: %s, theT: %s, X: %s, Y: %s, width: %s, height: %s" % (theZ,theT,x,y,width,height)
# create an ROI, link it to Image
roi = omero.model.RoiI()
roi.setImage(image._obj)    # use the omero.model.ImageI that underlies the 'image' wrapper
r = updateService.saveAndReturnObject(roi) 
# create and save a rectangle shape
rect = omero.model.RectI()
rect.x = rdouble(x)
rect.y = rdouble(y)
rect.width = rdouble(width)
rect.height = rdouble(height)
rect.theZ = rint(theZ)
rect.theT = rint(theT)
# link the rectangle to the ROI and save it 
rect.setRoi(r)
r.addShape(rect)    
sh = updateService.saveAndReturnObject(rect)


#Retrieve ROIs linked to an Image.

roiService = conn.getRoiService()
result = roiService.findByImage(imageId, None)
for roi in result.rois:
    print "ROI:"
    for shape in roi.copyShapes():
        print "  Shape:", shape.__class__.__name__ # E.g. omero.model.RectI
        theZ = shape.getTheZ().getValue()
        theT = shape.getTheT().getValue()
        x = int(shape.getX().getValue())
        y = int(shape.getY().getValue())
        width = int(shape.getWidth().getValue())
        height = int(shape.getHeight().getValue())
        print "  at theZ: %s, theT: %s, X: %s, Y: %s, width: %s, height: %s" % (theZ,theT,x,y,width,height)
        tile = (x, y, width, height)
        print image.getPrimaryPixels().getTile(theZ,0,theT, tile)