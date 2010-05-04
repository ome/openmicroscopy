"""
 components/tools/OmeroPy/scripts/makemovie.py

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

Make movie takes a number of parameters and creates an movie from the
image with imageId supplied. This movie is uploaded back to the server and
attached to the original Image.

params:
	imageId: this id of the image to create the movie from
	output: The name of the output file, sans the extension
	zStart: The starting z-section to create the movie from
	zEnd: 	The final z-section
	tStart:	The starting timepoint to create the movie
	tEnd:	The final timepoint.
	channels: The list of channels to use in the movie(index, from 0)
	splitView: should we show the split view in the movie(not available yet)
	showTime: Show the average time of the aquisition of the channels in the frame.
	showPlaneInfo: Show the time and z-section of the current frame.
	fps:	The number of frames per second of the movie
	scalebar: The scalebar size in microns, if <=0 will not show scale bar.
	format:	The format of the movie to be created currently supports 'video/mpeg', 'video/quicktime'
	overlayColour: The colour of the overlays, scalebar, time, as int(RGB)
	fileAnnotation: The fileAnnotation id of the uploaded movie.

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

import omero.scripts as scripts
import omero
import getopt, sys, os, subprocess
import omero_api_Gateway_ice
import omero_api_IScript_ice
import numpy;
import omero.util.pixelstypetopython as pixelstypetopython
from struct import *
from omero.rtypes import *
import PIL
from PIL import Image
from PIL import ImageDraw
import omero_Constants_ice

try:
	import hashlib
	hash_sha1 = hashlib.sha1
except:
	import sha
	hash_sha1 = sha.new

MPEG = 'video/mpeg'
QT = 'video/quicktime'
WMV = 'video/wmv'
MPEG_NS = omero_Constants_ice._M_omero.constants.metadata.NSMOVIEMPEG;
QT_NS = omero_Constants_ice._M_omero.constants.metadata.NSMOVIEQT;
WMV_NS = omero_Constants_ice._M_omero.constants.metadata.NSMOVIEWMV;

formatNSMap = {MPEG:MPEG_NS, QT:QT_NS, WMV:WMV_NS};
formatExtensionMap = {MPEG:"avi", QT:"avi", WMV:"avi"};
OVERLAYCOLOUR = "#666666";

def getFormat(session, fmt):
	queryService = session.getQueryService();
	return queryService.findByQuery("from Format as f where f.value='"+fmt+"'", None)

def calcSha1(filename):
	fileHandle = open(filename)
	h = hash_sha1()
	h.update(fileHandle.read())
	hash = h.hexdigest()
	fileHandle.close()
	return hash;

def createFile(session, filename, format, ofilename=None):
	tempFile = omero.model.OriginalFileI();
	if(ofilename == None):
		ofilename = filename;
	tempFile.setName(omero.rtypes.rstring(ofilename));
	tempFile.setPath(omero.rtypes.rstring(ofilename));
	if(format==WMV):
		format=MPEG;
	tempFile.setFormat(getFormat(session, format));
	tempFile.setSize(omero.rtypes.rlong(os.path.getsize(filename)));
	tempFile.setSha1(omero.rtypes.rstring(calcSha1(filename)));
	updateService = session.getUpdateService();
	return updateService.saveAndReturnObject(tempFile);

def attachMovieToImage(client, session, image, file, format):
	updateService = session.getUpdateService();
	fa = omero.model.FileAnnotationI();
	fa.setFile(file);
	fa.setNs(omero.rtypes.rstring(formatNSMap[format]))
	l = omero.model.ImageAnnotationLinkI();
	l.setParent(image);
	l.setChild(fa);
	l = updateService.saveAndReturnObject(l);
	client.setOutput("fileAnnotation",l.getChild().getId());

def uploadMovie(client,session, image, output, format):
	filename = 'movie.'+formatExtensionMap[format];
	originalFilename = output+'.'+formatExtensionMap[format];
	file = createFile(session, filename, format, originalFilename);
	rawFileStore = session.createRawFileStore();
	rawFileStore.setFileId(file.getId().getValue());
	fileSize = file.getSize().getValue();
	increment = 10000;
	cnt = 0;
	fileHandle = open(filename, 'rb');
	done = 0
	while(done!=1):
		if(increment+cnt<fileSize):
			blockSize = increment;
		else:
			blockSize = fileSize-cnt;
			done = 1;
		fileHandle.seek(cnt);
		block = fileHandle.read(blockSize);
		rawFileStore.write(block, cnt, blockSize);
		cnt = cnt+blockSize;
	attachMovieToImage(client, session, image, file, format)


def downloadPlane(gateway, pixels, pixelsId, x, y, z, c, t):
	rawPlane = gateway.getPlane(pixelsId, z, c, t);
	convertType ='>'+str(x*y)+pixelstypetopython.toPython(pixels.getPixelsType().getValue().getValue());
	convertedPlane = unpack(convertType, rawPlane);
	remappedPlane = numpy.array(convertedPlane,dtype=(pixels.getPixelsType().getValue().getValue()));
	remappedPlane.resize(x,y);
	return remappedPlane;

def uploadPlane(gateway, newPixelsId, x, y, z, c, t, newPlane):
	byteSwappedPlane = newPlane.byteswap();
	convertedPlane = byteSwappedPlane.tostring();
	gateway.uploadPlane(newPixelsId, z, c, t, convertedPlane)

def macOSX():
	if ('darwin' in sys.platform):
		return 1;
	else:
		return 0;

def buildAVI(sizeX, sizeY, filelist, fps, output, format):
	program = 'mencoder'
	args = "";
	formatExtension = formatExtensionMap[format];
	if(format==WMV):
		args = ' mf://'+filelist+' -mf w='+str(sizeX)+':h='+str(sizeY)+':fps='+str(fps)+':type=jpg -ovc lavc -lavcopts vcodec=wmv2 -o movie.'+formatExtension;
	elif(format==QT):
		args = ' mf://'+filelist+' -mf w='+str(sizeX)+':h='+str(sizeY)+':fps='+str(fps)+':type=png -ovc lavc -lavcopts vcodec=mjpeg:vbitrate=800  -o movie.'+formatExtension;
	else:
		args = ' mf://'+filelist+' -mf w='+str(sizeX)+':h='+str(sizeY)+':fps='+str(fps)+':type=jpg -ovc lavc -lavcopts vcodec=mpeg4 -o movie.'+formatExtension;
	os.system(program+ args);

def rangeToStr(range):
	first = 1;
	string = "";
	for value in range:
		if(first==1):
			string = str(value);
			first = 0;
		else:
			string = string + ','+str(value)
	return string;

def rangeFromList(list, index):
	minValue = list[0][index];
	maxValue = list[0][index];
	for i in list:
		minValue = min(minValue, i[index]);
		maxValue = max(maxValue, i[index]);
	return range(minValue, maxValue+1);

def calculateAquisitionTime(session, pixelsId, cRange, tzList):
	queryService = session.getQueryService()
	print tzList
	tRange = rangeFromList(tzList, 0);
	zRange = rangeFromList(tzList, 1)
	query = "from PlaneInfo as Info where Info.theZ in ("+rangeToStr(zRange)+") and Info.theT in ("+rangeToStr(tRange)+") and Info.theC in ("+rangeToStr(cRange)+") and pixels.id='"+str(pixelsId)+"'"
	infoList = queryService.findAllByQuery(query,None)

	map = {}
	for info in infoList:
		if(info.deltaT==None):
			return None;
		key = "z:"+str(info.theZ.getValue())+"t:"+str(info.theT.getValue());
		if(map.has_key(key)):
			value = map.get(key);
			value = value+info.deltaT.getValue()
			map[key] = value;
		else:
			map[key] = info.deltaT.getValue()
	for key in map:
		map[key] = map[key]/len(cRange);
	return map;

def addScalebar(scalebar, image, pixels, commandArgs):
	draw = ImageDraw.Draw(image)
	if(pixels.getPhysicalSizeX()==None):
	   return image;
	pixelSizeX = pixels.getPhysicalSizeX().getValue()
	if(pixelSizeX<=0):
		return image;
	scaleBarY = pixels.getSizeY().getValue()-30;
	scaleBarX = pixels.getSizeX().getValue()-scalebar/pixelSizeX-20;
	scaleBarTextY = scaleBarY-15;
	scaleBarX2 = scaleBarX+scalebar/pixelSizeX;
	if(scaleBarX<=0 or scaleBarX2<=0 or scaleBarY<=0 or scaleBarX2>pixels.getSizeX().getValue()):
		return image;
	draw.line([(scaleBarX,scaleBarY), (scaleBarX2,scaleBarY)], fill=commandArgs["overlayColour"])
	draw.text(((scaleBarX+scaleBarX2)/2, scaleBarTextY), str(scalebar), fill=commandArgs["overlayColour"])
	return image;

def addPlaneInfo(z, t, pixels, image, commandArgs):
	draw = ImageDraw.Draw(image)
	planeInfoTextY = pixels.getSizeY().getValue()-60;
	textX = 20;
	if(planeInfoTextY<=0 or textX > pixels.getSizeX().getValue() or planeInfoTextY>pixels.getSizeY().getValue()):
		return image;
	planeCoord = "z:"+str(z+1)+" t:"+str(t+1);
	draw.text((textX, planeInfoTextY), planeCoord, fill=commandArgs["overlayColour"])
	return image;

def addTimePoints(time, pixels, image, commandArgs):
	draw = ImageDraw.Draw(image)
	textY = pixels.getSizeY().getValue()-45;
	textX = 20;
	if(textY<=0 or textX > pixels.getSizeX().getValue() or textY>pixels.getSizeY().getValue()):
		return image;
	draw.text((textX, textY), str(time), fill=commandArgs["overlayColour"])
	return image;

def getRenderingEngine(session, pixelsId, sizeC, cRange):
	renderingEngine = session.createRenderingEngine();
	renderingEngine.lookupPixels(pixelsId);
	if(renderingEngine.lookupRenderingDef(pixelsId)==0):
		renderingEngine.resetDefaults();
	renderingEngine.lookupRenderingDef(pixelsId);
	renderingEngine.load();
	if len(cRange) == 0:
		for channel in range(sizeC):
			renderingEngine.setActive(channel, 1)
	else:
		for channel in range(sizeC):
			renderingEngine.setActive(channel, 0)
		for channel in cRange:
			renderingEngine.setActive(channel, 1);
	return renderingEngine;

def getPlane(renderingEngine, z, t):
	planeDef = omero.romio.PlaneDef()
	planeDef.t = t;
	planeDef.z = z;
	planeDef.x = 0;
	planeDef.y = 0;
	planeDef.slice = 0;
	return renderingEngine.renderAsPackedInt(planeDef);

def inRange(low, high, max):
	if(low < 0 or low > high):
		return 0;
	if(high < 0 or high > max):
		return 0;
	return 1;

def validChannels(set, sizeC):
	if(len(set)==0):
		return 0;
	for val in set:
		if(val < 0 or val > sizeC):
			return 0;
	return 1;

def validColourRange(colour):
	if(colour >= 0 and colour < 0xffffff):
		return 1;
	return 0;

def RGBToPIL(RGB):
	hexval = hex(int(RGB));
	return '#'+(6-len(hexval[2:]))*'0'+hexval[2:];

def buildPlaneMapFromRanges(zRange, tRange):
	planeMap = [];
	for t in tRange:
		for z in zRange:
			planeMap.append([t,z]);
        return planeMap

def strToRange(key):
	splitKey = key.split('-');
	if(len(splitKey)==1):
		return range(int(splitKey[0]), int(splitKey[0])+1)
	return range(int(splitKey[0]), int(splitKey[1])+1);

def unrollPlaneMap(planeMap):
	unrolledPlaneMap = [];
	for tSet in planeMap:
		zValue = planeMap[tSet];
		for t in strToRange(tSet):
			for z in strToRange(zValue.getValue()):
				unrolledPlaneMap.append([int(t),int(z)]);
	return unrolledPlaneMap

def calculateRanges(sizeZ, sizeT, commandArgs):
	planeMap = {};
	if(commandArgs["planeMap"]=={}):
		if(commandArgs["zStart"]<0):
			commandArgs["zStart"] = 0;
		if(commandArgs["zEnd"]>sizeZ):
			if(sizeZ == 0):
				commandArgs["zEnd"] = 0;
			else:
				commandArgs["zEnd"] = sizeZ-1;
		if(commandArgs["tStart"]<0):
			commandArgs["tStart"] = 0;
		if(commandArgs["tEnd"]>sizeT):
			if(sizeT == 0):
				commandArgs["tEnd"] = 0;
			else:
				commandArgs["tEnd"] = sizeT-1;

		zRange = range(commandArgs["zStart"], commandArgs["zEnd"]+1);
		tRange = range(commandArgs["tStart"], commandArgs["tEnd"]+1);
		planeMap = buildPlaneMapFromRanges(zRange, tRange);
	else:
		map = commandArgs["planeMap"];
		planeMap = unrollPlaneMap(map);
	return planeMap;

def writeMovie(commandArgs, session):
	gateway = session.createGateway();
	scriptService = session.getScriptService();
	omeroImage = gateway.getImage(commandArgs["image"])
	pixelsList = gateway.getPixelsFromImage(commandArgs["image"])
	pixels = pixelsList[0];
	pixelsId = pixels.getId().getValue();

	sizeX = pixels.getSizeX().getValue();
	sizeY = pixels.getSizeY().getValue();
	sizeZ = pixels.getSizeZ().getValue();
	sizeC = pixels.getSizeC().getValue();
	sizeT = pixels.getSizeT().getValue();

	if(sizeX==None or sizeY==None or sizeZ==None or sizeT==None or sizeC==None):
		return;

	if(pixels.getPhysicalSizeX()==None):
		commandArgs["scalebar"]=0;

	xRange = range(0,sizeX);
	yRange = range(0,sizeY);
	cRange = commandArgs["channels"]
	if(validChannels(cRange, sizeC)==0):
		cRange = range(0, sizeC);

	tzList = calculateRanges(sizeZ, sizeT, commandArgs);

	timeMap = calculateAquisitionTime(session, pixelsId, cRange, tzList)
	if(timeMap==None):
		commandArgs["showTime"]=0;
	if(timeMap != None):
		if(len(timeMap)==0):
			commandArgs["showTime"]=0;

	pixelTypeString = pixels.getPixelsType().getValue().getValue();
	frameNo = 1;
	filelist='';
	renderingEngine = getRenderingEngine(session, pixelsId, sizeC, cRange)

	for tz in tzList:
		t = tz[0];
		z = tz[1];
		plane = getPlane(renderingEngine, z, t)
		planeImage = numpy.array(plane, dtype='uint32')
		planeImage = planeImage.byteswap();
		planeImage = planeImage.reshape(sizeX, sizeY);
		image = Image.frombuffer('RGBA',(sizeX,sizeY),planeImage.data,'raw','ARGB',0,1)
		if(commandArgs["format"]==QT):
			filename = str(frameNo)+'.png';
		else:
			filename = str(frameNo)+'.jpg';
		if(commandArgs["scalebar"]!=0):
			image = addScalebar(commandArgs["scalebar"], image, pixels, commandArgs);
		planeInfo = "z:"+str(z)+"t:"+str(t);
		if(commandArgs["showTime"]==1):
			time = timeMap[planeInfo]
			image = addTimePoints(time, pixels, image, commandArgs);
		if(commandArgs["showPlaneInfo"]==1):
			image = addPlaneInfo(z, t, pixels, image, commandArgs);
		if(commandArgs["format"]==QT):
			image.save(filename,"PNG")
		else:
			image.save(filename,"JPEG")
		if(frameNo==1):
			filelist = filename
		else:
			filelist = filelist+','+filename
		frameNo +=1;
	buildAVI(sizeX, sizeY, filelist, commandArgs["fps"], commandArgs["output"], commandArgs["format"]);
	uploadMovie(client, session, omeroImage, commandArgs["output"], commandArgs["format"])

if __name__ == "__main__":
	client = scripts.client('makemovie','MakeMovie creates a movie of the image and attaches it to the originating image.',\
	scripts.Long("imageId").inout(),\
	scripts.String("output").inout(),\
	scripts.Long("zStart").inout(),\
	scripts.Long("zEnd").inout(),\
	scripts.Long("tStart").inout(),\
	scripts.Long("tEnd").inout(),\
	scripts.Set("channels").inout(),\
	scripts.Bool("splitView").inout(),\
	scripts.Bool("showTime").inout(),\
	scripts.Bool("showPlaneInfo").inout(),\
	scripts.Long("fps").inout(),
	scripts.Long("scalebar").inout(),\
	scripts.String("format").inout(),\
	scripts.Long("overlayColour").inout(),\
	scripts.Map("planeMap", optional=True).inout(),\
	scripts.Long("fileAnnotation").out())

	session = client.getSession();
	gateway = session.createGateway();
	commandArgs = {
	"image":client.getInput("imageId").getValue(),
	"output":client.getInput("output").getValue(),
	"zStart":client.getInput("zStart").getValue(),
	"zEnd":client.getInput("zEnd").getValue(),
	"tStart":client.getInput("tStart").getValue(),
	"tEnd":client.getInput("tEnd").getValue(),
	"channels":client.getInput("channels").getValue(),
	"fps":client.getInput("fps").getValue(),
	"showTime":client.getInput("showTime").getValue(),
	"showPlaneInfo":client.getInput("showPlaneInfo").getValue(),
	"scalebar":client.getInput("scalebar").getValue(),
	"format":client.getInput("format").getValue()
	}

	planeMap = client.getInput("planeMap") # optional
	if planeMap is not None:
		commandArgs["planeMap"] = planeMap.getValue()
	else:
		commandArgs["planeMap"] = {}

	if(validColourRange(client.getInput("overlayColour").getValue())):
		commandArgs["overlayColour"] = RGBToPIL(client.getInput("overlayColour").getValue())
	else:
		commandArgs["overlayColour"] = OVERLAYCOLOUR;

	writeMovie(commandArgs, session)

