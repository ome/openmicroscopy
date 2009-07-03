"""
 components/tools/OmeroPy/scripts/Projection.py 

-----------------------------------------------------------------------------
  Copyright (C) 2006-2009 University of Dundee. All rights reserved.


  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
  
  You should have received a copy of the GNU General Public License along
  with this program; if not, write to the Free Software Foundation, Inc.,
  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.

------------------------------------------------------------------------------

Project the image and create a new image from the projection.  

params:
	imageId: this id of the image to create the projection from
	zStart: The starting z-section to create the projection from
	zEnd: 	The final z-section
	tStart:	The starting timepoint to create the projection
	tEnd:	The final timepoint.
	channels: The list of channels to use in the projection(index, from 0)
	x0:		The start of the cropping area (x0,y0)
	y0:		The start of the cropping area (x0,y0)
	x1:		The end of the cropping area (x1,y1)
	y1:		The end of the cropping area (x1,y1)
	method: The projection method (sum, mean or max) 
	newImageId: The id of the newly created image.

@author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
@author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
@version 3.0
<small>
(<b>Internal version:</b> $Revision: $Date: $)
</small>
@since 3.0-Beta4.1

"""

import omero
import omero.scripts as scripts
import getopt, sys
import omero_api_Gateway_ice
import numpy;
import omero.util.pixelstypetopython as pixelstypetopython
from struct import *
from omero.rtypes import *

def projection(client, session, commandArgs):
	gateway = session.createGateway();
	pixelsList = gateway.getPixelsFromImage(commandArgs["imageId"])
	pixels = pixelsList[0];
	pixelsId = pixels.getId().getValue();
	originalSizeX = pixels.getSizeX().getValue();
	originalSizeY = pixels.getSizeX().getValue();
	sizeC = pixels.getSizeC().getValue();
	sizeT = pixels.getSizeT().getValue();

	xRange = range(commandArgs["x0"], commandArgs["x1"]);
	yRange = range(commandArgs["y0"], commandArgs["y1"]);
	zRange = range(commandArgs["zStart"], commandArgs["zEnd"]);
	tRange = range(commandArgs["tStart"], commandArgs["tEnd"]);
	cRange = commandArgs["channels"];
	sizeX = len(xRange);
	sizeY = len(yRange);
	sizeC = len(cRange);
	sizeZ = len(zRange);
	sizeT = len(tRange);
	startX = commandArgs["x0"];
	startY = commandArgs["y0"];
	
	channels = [];
	for c in cRange:
		channels.append(int(c)) 
	
	image = pixels.getImage();		
	
	newImageId = gateway.copyImage(image.getId().getValue(), sizeX,sizeY,sizeT,1, channels, 'projection');
	newImage = gateway.getImage(newImageId);
	newImage.setName(RStringI('projectedImage'));
	gateway.saveObject(newImage);
			
	newPixelsId = gateway.getPixelsFromImage(newImageId);
	newPixelsId = newPixelsId[0];
	for t in tRange:
		for c in cRange:
			newPlane = numpy.zeros([sizeX, sizeY], dtype=pixelstypetopython.toArray(pixels.getPixelsType().getValue().getValue()));
			for z in zRange:

				plane = downloadPlane(gateway, pixels, pixelsId, originalSizeX, originalSizeY, z, c, t);
				for x in xRange:
					for y in yRange:
						if(commandArgs["method"]=='mean' or commandArgs["method"] == 'sum'):
							newPlane[x-startX][y-startY] += plane[x][y];
						else:
							newPlane[x-startX][y-startY] = max(newPlane[x-startX][y-startY],plane[x][y]);

			if(commandArgs["method"]=='mean'):
				newPlane /=sizeZ;
		
			uploadPlane(gateway, newPixelsId.getId().getValue(),sizeX, sizeY,0,c,t, newPlane);
	client.setOutput("newImageId", newImage.getId())
	
def downloadPlane(gateway, pixels, pixelsId, x, y, z, c, t):
	rawPlane = gateway.getPlane(pixelsId, z, c, t);
	convertType ='>'+str(x*y)+pixelstypetopython.toPython(pixels.getPixelsType().getValue().getValue());
	convertedPlane = unpack(convertType, rawPlane);
	remappedPlane = numpy.array(convertedPlane,dtype=pixelstypetopython.toArray(pixels.getPixelsType().getValue().getValue()));
	remappedPlane.resize(x,y);
	return remappedPlane;

def uploadPlane(gateway, newPixelsId, x, y, z, c, t, newPlane):
	byteSwappedPlane = newPlane.byteswap();
	convertedPlane = byteSwappedPlane.tostring();
	gateway.uploadPlane(newPixelsId, z, c, t, convertedPlane)
		
def parseInputs(client, session):
	gateway = session.createGateway();
	pixelsList = gateway.getPixelsFromImage(commandArgs["imageId"])
	pixels = pixelsList[0];
	pixelsId = pixels.getId().getValue();
	sizeX = pixels.getSizeX().getValue();
	sizeY = pixels.getSizeY().getValue();
	sizeZ = pixels.getSizeZ().getValue();
	sizeC = pixels.getSizeC().getValue();
	sizeT = pixels.getSizeT().getValue();

	inputKeys = client.getInputKeys();
	commandArgs = {};
	for key in inputKeys:
		commandArgs[key]=client.getInput(key).getValue();
	if("zStart" not in commandArgs):
		commandArgs["zStart"] = 0;
	if("zEnd" not in commandArgs):
		commandArgs["zEnd"] = sizeZ;
	if("tStart" not in commandArgs):
		commandArgs["tStart"] = 0;
	if("tEnd" not in commandArgs):
		commandArgs["tEnd"] = sizeT;
	if("x0" not in commandArgs):
		commandArgs["x0"] = 0;
	if("x1" not in commandArgs):
		commandArgs["x1"] = sizeX;
	if("y0" not in commandArgs):
		commandArgs["y0"] = 0;
	if("y1" not in commandArgs):
		commandArgs["y1"] = sizeY;
	if("channels" not in commandArgs):
		commandArgs["channels"] = range(0,sizeC);
	return commandArgs;	

	
client = scripts.client('projection', 'Project the image and create a new image from the projection.', scripts.Long("imageId").inout(), 
scripts.Long("newImageId").inout(), scripts.String("method").inout(),scripts.Long("zStart").inout(), scripts.Long("zEnd").inout(), \
scripts.Long("tStart").inout(), scripts.Long("tEnd").inout(), scripts.Long("x0").inout(), \
scripts.Long("y0").inout(), scripts.Long("x1").inout(), scripts.Long("y1").inout(), scripts.Set("channels").inout());
session = client.createSession();
commandArgs = parseInputs(client, session);
projection(client, session, commandArgs);

