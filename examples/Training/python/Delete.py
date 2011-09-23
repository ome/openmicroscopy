from omero.gateway import BlitzGateway
from omero.rtypes import *
from omero.model import *
user = 'will'
pw = 'ome'
host = 'localhost'
conn = BlitzGateway(user, pw, host=host, port=4064)
conn.connect()
# Let's set up an Image to delete (NB this image has no Pixel data etc)
image = omero.model.ImageI()
image.setName(omero.rtypes.rstring("test_delete"))
image.setAcquisitionDate(omero.rtypes.rtime(2000000))
image = conn.getUpdateService().saveAndReturnObject(image)
imageId = image.getId().getValue()
# OR, you could put the ID of an image you want to delte here:
#imageId = 101


# Delete Image

# You can delete a number of objects of the same type at the same time. In this case 'Image'

obj_ids = [imageId]
# use deleteChildren=True if you are E.g. deleting a Dataset and you want to delete Images.
conn.deleteObjects("Image", obj_ids, deleteAnns=True, deleteChildren=False)