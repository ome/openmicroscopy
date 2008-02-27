#!/usr/bin/python
#
#------------------------------------------------------------------------------
#  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
#
#
# 	This program is free software; you can redistribute it and/or modify
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
#------------------------------------------------------------------------------
#
# Projection.py will project an image(OME::Pixels) using either maximum 
# intensity, average intensity projections.  
# @param pixelId the id of the pixels to project.
# @param channelSet the set of the channels of the pixels object to use.
# @param timeSet the set of the timePoints of the pixels object to use.
# @param zSectionSet the zsection of the pixels object to use.
# @param point1 the (x,y) of the (x,y)->(x1,y1) area of the pixels object to use.
# @param point2 the (x1,y1) of the (x,y)->(x1,y1) area of the pixels object to use.
# @param method  the method of projecting (maximum, average)
# @return new pixels object containing the projection.
#
import omero, omero.scripts as s
#import getRawPlane
class GetRawPlane:
    def getRawPlane(self, *args):
        return [1]
getRawPlane = GetRawPlane()
def zeros(w,h):
    return [0]

def Projection():
 		AVERAGE="average"
		MAX="max"
		SUM="sum"

		client = s.client("","",s.String("method"), s.Long("pixelsID"), s.Set("channelSet"), s.Set("timeSet"), s.Set("zSectionSet"), \
                    s.Long("point1"), s.Long("point2"), s.Long("newPixelsID").out())
		session = client.createSession()
		rawPixelsStore = session.createRawPixelsStore()
		queryService = session.getQueryService()
		# using integer for pixels ID, as this seems the most reasonable
		# I know we discussed using pixels! (unloaded object, but I think
		# this could be more complex than required(?).
		pixelsID = client.getInput("pixelsID").val
		channelSet = client.getInput("channelSet").val
		timeSet = client.getInput("timeSet").val
		zSectionSet = client.getInput("zSectionSet").val

		# Not sure what a point object would be in the OME::System(?)
		point1 = client.getInput("point1").val # Unwrapping internals
		point2 = client.getInput("point2").val

		areaWidth = point2.x -point1.x
		areaHeight = point2.y-point1.y

		# Get the method we're going to use of the pixels set.
		method = client.getInput("method")

		# method to create a new pixels from an pixels. This method will create
		# a new pixels object, with width, height, channelSet channels and 
		# one timepoint and one zsection from the original image. 
		# It will use the original image to set the bitdepth, channel info, meta-
		# data. 
		# TODO : Need a method to complete this, either in the client object, or 
		# via another service.
		#newPixelsID = client.copyPixels(pixelsID, [width, height, channelSet, timeSet, 1])
		
		# Iterate original image, over the channelSet, timeSet, zSectionSet and create 
		# the new plane based on the method 
		for channel in channelSet:
			for time in timeSet:
				newPlaneData = zeros(areaWidth, areaHeight)
				for zSection in zSectionSet:
					
					# I think that a mechanism for getting planes from the system, and writing 
					# values to it may be necessary. This method should return an array so it
					# is simple to manipulate in python, (scripting should not really involve
					# too much bit twiddling :)
					planeData = getRawPlane.getRawPlane(rawPixelsStore, queryService, pixelsID, zSection, channel, time)
					
					# loop through the selection of the original image.
					for x in range(point1.x, point2.x):
						for y in range(point1.y, point2.y):
						
							# The new image coords are just offset by point1 values.
							newImageX = x-point1.x
							newImageY = y-point1.y
						
							# Apply projection method.
							if method == AVERAGE or method == SUM:
								newPlaneData[newImageX][newImageY] += planeData[x][y];
							if method == MAX:
								newPlaneData[newImageX][newImageY] = max(newPlaneData[newImageX][newImageY], planeData[x][y])		
				# calculate mean for AVERAGE method.
				if method == AVERAGE:
					for x in range(0, areaWidth):
						for y in range(0, areaHeight):
							newPlaneData[x][y] /= len(zSectionSet)
				
				# A method to set the plane of the newImage to the newPlaneData
				# TODO : 
				#client.setPlane(newPixelsID, newPlaneData, 1, time, channel, time);
					
		# save the image?
		client.setOutput("newPixelsID", omero.RLong(-1))

if __name__ == "__main__":
    Projection()
