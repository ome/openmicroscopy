from omero.gateway import BlitzGateway
from omero.rtypes import *
from omero.model import *
user = 'will'
pw = 'ome'
host = 'localhost'
conn = BlitzGateway(user, pw, host=host, port=4064)
conn.connect()
imageId = 101


# Load the 'Original Metadata' for the image

image = conn.getObject("Image", imageId)
om = image.loadOriginalMetadata()
if om is not None:
    print "original_metadata"
    print "    File Annotation ID:", om[0].getId()
    print "global_metadata"
    for keyValue in om[1]:
        if len(keyValue) > 1:
            print "   ", keyValue[0], keyValue[1]
        else:
            print "   ", keyValue[0], "NOT FOUND"
    print "series_metadata"
    for keyValue in om[2]:
        if len(keyValue) > 1:
            print "   ", keyValue[0], keyValue[1]
        else:
            print "   ", keyValue[0], "NOT FOUND" 


