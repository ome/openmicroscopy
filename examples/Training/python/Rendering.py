#!/usr/bin/env python
# 
# Copyright (c) 2011 University of Dundee. 
# 
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU Affero General Public License as
# published by the Free Software Foundation, either version 3 of the
# License, or (at your option) any later version.
# 
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Affero General Public License for more details.
# 
# You should have received a copy of the GNU Affero General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
# 
# Version: 1.0
#
# This script shows a simple connection to OMERO, printing details of the connection.
# NB: You will need to edit the config.py before running.
# 
# 
from omero.gateway import BlitzGateway
from omero.rtypes import *
from Connecting import USERNAME, PASSWORD, HOST, PORT
# create a connection
conn = BlitzGateway(USERNAME, PASSWORD, host=HOST, port=PORT)
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