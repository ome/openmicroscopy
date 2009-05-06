import omero
import getopt, sys
import omero_api_Gateway_ice
import numpy;
import PixelsTypeToPython;
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

	xRange = range(commandArgs["x0"],commandArgs["x1"]);
	yRange = range(commandArgs["y0"],commandArgs["y1"]);
	zRange = range(commandArgs["zStart"],commandArgs["zEnd"]);
	tRange = range(commandArgs["tStart"],commandArgs["tEnd"]);
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
	
	newImageId = gateway.copyImage(image.getId().getValue(), sizeX,sizeY,1,sizeT, channels, 'projection');
	newImage = gateway.getImage(newImageId);
	newImage.setName(RStringI('projectedImage'));
	gateway.saveObject(newImage);
			
	newPixelsId = gateway.getPixelsFromImage(newImageId);
	newPixelsId = newPixelsId[0];
	for t in tRange:
		for c in cRange:
			newPlane = numpy.zeros([sizeX, sizeY], dtype=PixelsTypeToPython.toArray(pixels.getPixelsType().getValue().getValue()));
			for z in zRange:

				plane = downloadPlane(gateway, pixels, pixelsId, originalSizeX, originalSizeY, z, c, t);
				for x in xRange:
					for y in yRange:
						if(commandArgs["method"]=='mean' or commandArgs["method"] == 'sum'):
							newPlane[x-startX][y-startY] += plane[x][y];
						else
							newPlane[x-startX][y-startY] = max(newPlane[x-startX][y-startY],plane[x][y]);

			if(commandArgs["method"]=='mean'):
				newPlane /=sizeZ;
		
			uploadPlane(gateway, newPixelsId.getId().getValue(),sizeX, sizeY,0,c,t, newPlane);
	client.setOutput("newImageId", newImage.getId())
	
def downloadPlane(gateway, pixels, pixelsId, x, y, z, c, t):
	rawPlane = gateway.getPlane(pixelsId, z, c, t);
	convertType ='>'+str(x*y)+PixelsTypeToPython.toPython(pixels.getPixelsType().getValue().getValue());
	convertedPlane = unpack(convertType, rawPlane);
	remappedPlane = numpy.array(convertedPlane,dtype=PixelsTypeToPython.toArray(pixels.getPixelsType().getValue().getValue()));
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
	if("zStart" not in commandArgs)
		commandArgs["zStart"] = 0;
	if("zEnd" not in commandArgs)
		commandArgs["zEnd"] = sizeZ;
	if("tStart" not in commandArgs)
		commandArgs["tStart"] = 0;
	if("tEnd" not in commandArgs)
		commandArgs["tEnd"] = sizeT;
	if("x0" not in commandArgs)
		commandArgs["x0"] = 0;
	if("x1" not in commandArgs)
		commandArgs["x1"] = sizeX;
	if("y0" not in commandArgs)
		commandArgs["y0"] = 0;
	if("y1" not in commandArgs)
		commandArgs["y1"] = sizeY;
	return commandArgs;	
	if("channels" not in commandArgs)
		commandArgs["channels"] = range(0,sizeC);
	
	
client = scripts.client('Projection', scripts.Long("imageId").inout(), scripts.Long("newImageId").inout(), \
scripts.String("method").inout(),scripts.Long("zStart").optional(), scripts.Long("zEnd").optional(), \
scripts.Long("tStart").optional(), scripts.Long("tEnd").optional(), scripts.Long("x0").optional(), \
scripts.Long("y0").optional(), script.Long("x1").optional(), script.Long("y1").optional(), \
scripts.Set("channels").optional());
session = client.createSession();
commandArgs = parseInputs(client, session);
projection(client, session, commandArgs);

