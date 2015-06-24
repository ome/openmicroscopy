#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
# ----------------------------------------------------------------------------
#  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
#
#
#   This program is free software; you can redistribute it and/or modify
#  it under the terms of the GNU General Public License as published by
#  the Free Software Foundation; either version 2 of the License, or
#  (at your option) any later version.
#  This program is distributed in the hope that it will be useful,
#  but WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#  GNU General Public License for more details.
#
#  You should have received a copy of the GNU General Public License along
#  with this program; if not, write to the Free Software Foundation, Inc.,
#  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
#
# ----------------------------------------------------------------------------
#
# Projection.py will project an image(OME::Pixels) using either maximum
# intensity, average intensity projections.
# @param pixelId the id of the pixels to project.
# @param channelSet the set of the channels of the pixels object to use.
# @param timeSet the set of the timePoints of the pixels object to use.
# @param zSectionSet the zsection of the pixels object to use.
# @param point1 the (x,y) of the (x,y)->(x1,y1) area of the pixels object to
# use.
# @param point2 the (x1,y1) of the (x,y)->(x1,y1) area of the pixels object to
# use.
# @param method  the method of projecting (maximum, average)
# @return new pixels object containing the projection.
#
import sys
import omero
import omero.cli
from numpy import zeros


class point:
    def __init__(self, x, y):
        self.x = x
        self.y = y


def projection():
    def __init__(self):
        self.AVERAGE = "average"
        self.MAX = "max"
        self.SUM = "sum"

    def __runScript(self):
        client = omero.script("projection", {})
        # using integer for pixels ID, as this seems the most reasonable
        # I know we discussed using pixels! (unloaded object, but I think
        # this could be more complex than required(?).
        pixelsID = client.getInput("pixelsID")
        channelSet = client.getInput("channelSet")
        timeSet = client.getInput("timeSet")
        zSectionSet = client.getInput("zSectionSet")

        # Not sure what a point object would be in the OME::System(?)
        point1 = point(
            client.getInput("point1.x"),
            client.getInput("point1.y"))
        point2 = point(
            client.getInput("point2.x"),
            client.getInput("point2.y"))

        width = point2.x - point1.x
        height = point2.y - point1.y

        # Get the method we're going to use of the pixels set.
        method = client.getInput("method")

        # method to create a new pixels from an pixels. This method will
        # create a new pixels object, with width, height, channelSet channels
        # and one timepoint and one zsection from the original image.
        # It will use the original image to set the bitdepth, channel info,
        # metadata.
        newPixelsID = client.copyPixels(pixelsID, [width, height, channelSet,
                                                   timeSet, 1])

        # Iterate original image, over the channelSet, timeSet, zSectionSet
        # and create the new plane based on the method
        for channel in channelSet:
            for time in timeSet:
                newPlaneData = zeros(width, height)
                for zSection in zSectionSet:

                    # I think that a mechanism for getting planes from the
                    # system, and writing values to it may be necessary. This
                    # method should return an array so it is simple to
                    # manipulate in python, (scripting should not really
                    # involve too much bit twiddling :)
                    planeData = client.getPixels(pixelsID, zSection, time,
                                                 channel, time)

                    # loop through the selection of the original image.
                    for x in range(point1.x, point2.x):
                        for y in range(point1.y, point2.y):

                            # The new image coords are just offset by point1
                            # values.
                            newImageX = x-point1.x
                            newImageY = y-point1.y

                            # Apply projection method.
                            if method == self.AVERAGE or method == self.SUM:
                                newPlaneData[newImageX][newImageY] += \
                                    planeData[x][y]
                            if method == self.MAX:
                                newPlaneData[newImageX][newImageY] = max(
                                    newPlaneData[newImageX][newImageY],
                                    planeData[x][y])
                # calculate mean for AVERAGE method.
                if method == self.AVERAGE:
                    for x in range(0, width-1):
                        for y in range(0, height-1):
                            newPlaneData[x][y] /= len(zSectionSet)

                # A method to set the plane of the newImage to the
                # newPlaneData
                client.setPlane(newPixelsID, newPlaneData, 1, time, channel,
                                time)

        # save the image?
        client.setOutput("newPixelsID", newPixelsID)
        sys.exit(0)

if __name__ == "__main__":
    projection().__runScript()
