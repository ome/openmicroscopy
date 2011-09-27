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
# This script gets plane data from OMERO images and saves them as local tiff files,
# using the PIL image library.
# If the pixel type has a larger range than can be handled by PIL (uint8) 
# then we scale the data to fit. 
# This shouldn't affect the quantitive analysis of the data.
# 
from numpy import zeros, uint8
from omero.gateway import BlitzGateway
from Connecting import USERNAME, PASSWORD, HOST, PORT
# create a connection
conn = BlitzGateway(USERNAME, PASSWORD, host=HOST, port=PORT)
conn.connect()
try:
    from PIL import Image
except ImportError:
    import Image
imageId = 401

# Save a plane (raw data) as tiff for analysis

image = conn.getObject("Image", imageId)                  # first plane of the image
pixels = image.getPrimaryPixels()
# make a note of min max pixel values for each channel
# so that we can scale all the planes from each channel to the same range
channelMinMax = []
for c in image.getChannels():
    minC = c.getWindowMin()
    maxC = c.getWindowMax()
    channelMinMax.append( (minC, maxC) )
print channelMinMax

# go through each channel (looping through Z and T not shown - go for mid-Z only)
theZ = image.getSizeZ()/2
theT = 0
cIndex = 0
for minMax in channelMinMax:
    plane = pixels.getPlane(theZ,cIndex,theT)
    print "dtype:", plane.dtype.name
    # need plane dtype to be uint8 (or int8) for conversion to tiff by PIL
    if plane.dtype.name not in ('uint8', 'int8'):      # we need to scale...
        minVal, maxVal = minMax
        valRange = maxVal - minVal
        scaled = (plane - minVal) * (float(255) / valRange)
        convArray = zeros(plane.shape, dtype=uint8)
        convArray += scaled
        print "using converted int8 plane: dtype: %s min: %s max: %s" % (convArray.dtype.name, convArray.min(), convArray.max())
        i = Image.fromarray(convArray)
    else:
        i = Image.fromarray(plane)
    i.save("tiffPlaneInt8%s.tiff" % cIndex)
    cIndex += 1
conn._closeSession()