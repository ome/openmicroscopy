from omero.gateway import BlitzGateway
from omero.rtypes import *
from omero.model import *
user = 'will'
pw = 'ome'
host = 'localhost'
conn = BlitzGateway(user, pw, host=host, port=4064)
conn.connect()
imageId = 101


# Render each channel as a separate plane

image = conn.getObject("Image", imageId)
sizeC = image.getSizeC()
z = image.getSizeZ() / 2
t = 0
for c in range(1, sizeC+1):     # Channel index starts at 1
    channels = [c]  
    image.setActiveChannels(channels)
    renderedImage = image.renderImage(z, t)
    #renderedImage.show()                        # popup (use for debug only)
    renderedImage.save("channel%s.jpg" % c)     # save in the current folder


# Turn 3 channels on, setting their colours

channels = [1,2,3]
colorList = ['F00', None, 'FFFF00']         # don't change colour of 2nd channel
image.setActiveChannels(channels, colors=colorList)
renderedImage = image.renderImage(z, t)
#renderedImage.show()
renderedImage.save("all_channels.jpg")


# Turn 2 channels on, setting levels of the first one

channels = [1,2]
rangeList = [(100.0, 120.2), (None, None)]
image.setActiveChannels(channels, windows=rangeList)
renderedImage = image.renderImage(z, t)
renderedImage.show()
renderedImage.save("two_channels.jpg")