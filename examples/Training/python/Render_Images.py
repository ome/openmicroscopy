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
from Parse_OMERO_Properties import USERNAME, PASSWORD, HOST, PORT, imageId

"""
start-code
"""

# Create a connection
# ===================
conn = BlitzGateway(USERNAME, PASSWORD, host=HOST, port=PORT)
conn.connect()


# Get thumbnail
# =============
# Thumbnail is created using the current rendering settings on the image
print imageId
image = conn.getObject("Image", imageId)
img_data = image.getThumbnail()
rendered_thumb = Image.open(StringIO(img_data))
# renderedThumb.show()           # shows a pop-up
rendered_thumb.save("thumbnail.jpg")


# Get current settings
# ====================
print "Channel rendering settings:"
for ch in image.getChannels():
    # if no name, get emission wavelength or index
    print "Name: ", ch.getLabel()
    print "  Color:", ch.getColor().getHtml()
    print "  Active:", ch.isActive()
    print "  Levels:", ch.getWindowStart(), "-", ch.getWindowEnd()
print "isGreyscaleRenderingModel:", image.isGreyscaleRenderingModel()
print "Default Z/T positions:"
print "    Z = %s, T = %s" % (image.getDefaultZ(), image.getDefaultT())


# Show the saved rendering settings on this image
# ===============================================
print "Rendering Defs on Image:"
for rdef in image.getAllRenderingDefs():
    img_data = image.getThumbnail(rdefId=rdef['id'])
    print "   ID: %s (owner: %s %s)" % (
        rdef['id'], rdef['owner']['firstName'], rdef['owner']['lastName'])


# Render each channel as a separate greyscale image
# =================================================
image.setGreyscaleRenderingModel()
size_c = image.getSizeC()
z = image.getSizeZ() / 2
t = 0
for c in range(1, size_c + 1):       # Channel index starts at 1
    channels = [c]                  # Turn on a single channel at a time
    image.setActiveChannels(channels)
    rendered_image = image.renderImage(z, t)
    # renderedImage.show()                        # popup (use for debug only)
    rendered_image.save("channel%s.jpg" % c)     # save in the current folder


# Turn 3 channels on, setting their colors
# ========================================
image.setColorRenderingModel()
channels = [1, 2, 3]
color_list = ['F00', None, 'FFFF00']  # do not change color of 2nd channel
image.setActiveChannels(channels, colors=color_list)
# max intensity projection 'intmean' for mean-intensity
image.setProjection('intmax')
rendered_image = image.renderImage(z, t)  # z and t are ignored for projections
# renderedImage.show()
rendered_image.save("all_channels.jpg")
image.setProjection('normal')               # turn off projection


# Turn 2 channels on, setting levels of the first one
# ===================================================
channels = [1, 2]
range_list = [[100.0, 120.2], [None, None]]
image.setActiveChannels(channels, windows=range_list)
# Set default Z and T. These will be used as defaults for further rendering
image.setDefaultZ(0)
image.setDefaultT(0)
# default compression is 0.9
rendered_image = image.renderImage(z=None, t=None, compression=0.5)
rendered_image.show()
rendered_image.save("two_channels.jpg")


# Save the current rendering settings and default Z/T
# ===================================================
image.saveDefaults()


# Reset to settings at import time, and optionally save
# =====================================================
image.resetDefaults(save=True)


# Close connection
# ================
# When you are done, close the session to free up server resources.
conn.close()
