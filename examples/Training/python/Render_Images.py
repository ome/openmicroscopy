#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2014 University of Dundee & Open Microscopy Environment.
#                    All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
#

"""
FOR TRAINING PURPOSES ONLY!
"""

from omero.gateway import BlitzGateway
from cStringIO import StringIO
try:
    from PIL import Image
except ImportError:
    import Image
from Connect_To_OMERO import USERNAME, PASSWORD, HOST, PORT


# Create a connection
# =================================================================
conn = BlitzGateway(USERNAME, PASSWORD, host=HOST, port=PORT)
conn.connect()


# Configuration
# =================================================================
imageId = 27544


# Get thumbnail
# =================================================================
# Thumbnail is created using the current rendering settings on the image
image = conn.getObject("Image", imageId)
img_data = image.getThumbnail()
renderedThumb = Image.open(StringIO(img_data))
# renderedThumb.show()           # shows a pop-up
renderedThumb.save("thumbnail.jpg")


# Get current settings
# =================================================================
print "Channel rendering settings:"
for ch in image.getChannels():
    # if no name, get emission wavelength or index
    print "Name: ", ch.getLabel()
    print "  Color:", ch.getColor().getHtml()
    print "  Active:", ch.isActive()
    print "  Levels:", ch.getWindowStart(), "-", ch.getWindowEnd()
print "isGreyscaleRenderingModel:", image.isGreyscaleRenderingModel()


# Show the saved rendering settings on this image
# =================================================================
print "Rendering Defs on Image:"
for rdef in image.getAllRenderingDefs():
    img_data = image.getThumbnail(rdefId = rdef['id'])
    print "   ID: %s (owner: %s %s)" % (rdef['id'],
                rdef['owner']['firstName'], rdef['owner']['lastName'])


# Render each channel as a separate greyscale image
# =================================================================
image.setGreyscaleRenderingModel()
sizeC = image.getSizeC()
z = image.getSizeZ() / 2
t = 0
for c in range(1, sizeC + 1):       # Channel index starts at 1
    channels = [c]                  # Turn on a single channel at a time
    image.setActiveChannels(channels)
    renderedImage = image.renderImage(z, t)
    # renderedImage.show()                        # popup (use for debug only)
    renderedImage.save("channel%s.jpg" % c)     # save in the current folder


# Turn 3 channels on, setting their colours
# =================================================================
image.setColorRenderingModel()
channels = [1, 2, 3]
colorList = ['F00', None, 'FFFF00']  # do not change colour of 2nd channel
image.setActiveChannels(channels, colors=colorList)
# max intensity projection 'intmean' for mean-intensity
image.setProjection('intmax')
renderedImage = image.renderImage(z, t)  # z and t are ignored for projections
# renderedImage.show()
renderedImage.save("all_channels.jpg")
image.setProjection('normal')               # turn off projection


# Turn 2 channels on, setting levels of the first one
# =================================================================
channels = [1, 2]
rangeList = [[100.0, 120.2], [None, None]]
image.setActiveChannels(channels, windows=rangeList)
# default compression is 0.9
renderedImage = image.renderImage(z, t, compression=0.5)
# renderedImage.show()
renderedImage.save("two_channels.jpg")


# Save the current rendering settings
# =================================================================
image.saveDefaults()


# Close connection:
# =================================================================
# When you are done, close the session to free up server resources.
conn._closeSession()
