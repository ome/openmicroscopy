#
#
#------------------------------------------------------------------------------
#  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
###
#
# Utility methods for deal with scripts.
#
# @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
# 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
# @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
# 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
# @version 3.0
# <small>
# (<b>Internal version:</b> $Revision: $Date: $)
# </small>
# @since 3.0-Beta4
#
import getopt, sys, os, subprocess
import numpy;
from struct import *

import PIL
from PIL import Image
import ImageDraw

import omero
import omero_Constants_ice
from omero.rtypes import *
import omero.util.pixelstypetopython as pixelstopython;

try: 
	import hashlib 
	hash_sha1 = hashlib.sha1 
except: 
	import sha 
	hash_sha1 = sha.new 

def drawTextOverlay(draw, x, y, text, colour='0xffffff'):
	draw.text((x, y), text, fill=colour)

def drawLineOverlay(draw, x0, y0, text, colour='0xffffff'):
	draw.text((x, y), text, fill=colour)

def rgbToRGBInt(red, green, blue):
	RGBInt = (red<<16)+(green<<8)+blue;
	return int(RGBInt);
	
def RGBToPIL(RGB):
	hexval = hex(int(RGB));
	return '#'+(6-len(hexval[2:]))*'0'+hexval[2:];

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

def rmdir_recursive(dir):
	for name in os.listdir(dir):
		full_name = os.path.join(dir, name)
		# on Windows, if we don't have write permission we can't remove
		# the file/directory either, so turn that on
		if not os.access(full_name, os.W_OK):
			os.chmod(full_name, 0600)
		if os.path.isdir(full_name):
			rmdir_recursive(full_name)
		else:
			os.remove(full_name)
	os.rmdir(dir)

def calcSha1(filename):
	fileHandle = open(filename)
	h = hash_sha1()
	h.update(fileHandle.read())
	hash = h.hexdigest()
	fileHandle.close()
	return hash;	

def getFormat(queryService, format):
	return queryService.findByQuery("from Format as f where f.value='"+format+"'", None)

def createFile(updateService, filename, format):
 	originalFile = omero.model.OriginalFileI();
	originalFile.setName(omero.rtypes.rstring(filename));
	originalFile.setPath(omero.rtypes.rstring(filename));
	originalFile.setFormat(format);
	originalFile.setSize(omero.rtypes.rlong(os.path.getsize(filename)));
	originalFile.setSha1(omero.rtypes.rstring(calcSha1(filename)));
	return updateService.saveAndReturnObject(originalFile);	
	
def uploadFile(rawFileStore, file):
	rawFileStore.setFileId(file.getId().getValue());
	fileSize = file.getSize().getValue();
	increment = 10000;
	cnt = 0;
	fileHandle = open(file.getName().getValue(), filename, 'rb');
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
	fileHandle.close();
	
def attachFileToImage(updateService, image, file, nameSpace):
	fa = omero.model.FileAnnotationI();
	fa.setFile(file);
	fa.setNs(omero.rtypes.rstring(nameSpace))
	l = omero.model.ImageAnnotationLinkI();
	l.setParent(image);
	l.setChild(fa);
	return updateService.saveAndReturnObject(l);

def addAnnotationToImage(updateService, image, annotation):
	l = omero.model.ImageAnnotationLinkI();
	l.setParent(image);
	l.setChild(annotation);
	return updateService.saveAndReturnObject(l);
	
def readFromOriginalFile(fileId):
	originalFileStore = session.createRawFileStore();
	iQuery = session.getQueryService();
	fileDetails = iQuery.findByQuery("from OriginalFile as o where o.id = " + str(fileId) , None);
	originalFileStore.setFileId(fileId);
	data = '';
	cnt = 0;
	maxBlockSize = 10000;
	fileSize = fileDetails.getSize().getValue();
	while(cnt<fileSize):
		blockSize = min(maxBlockSize, fileSize);
		block = originalFileStore.read(cnt, blockSize);
		data = data + block;
		cnt = cnt+blockSize;
	return data;

def readImageFile(gateway, pixels, pixelsId, channel, x, y):
	cRange = range(0,channel);
	stack = numpy.zeros((channel,x,y),dtype=(pixels.getPixelsType().getValue().getValue()));
	for c in cRange:
		plane = downloadPlane(gateway, pixels, pixelsId, x, y, 0, c, 0);
		stack[c,:,:]=plane;
	return stack;

def readFile(session, fileId, row, col):
	textBlock = readFromOriginalFile(fileId);
	arrayFromFile = numpy.fromstring(textBlock,sep=' ');
	return numpy.reshape(arrayFromFile, (row, col));
	
def calcSha1FromData(data):
	h = hash_sha1()
	h.update(data)
	hash = h.hexdigest()
	return hash;
	
def createFileFromData(session, filename, data, format):
 	tempFile = omero.model.OriginalFileI();
	tempFile.setName(omero.rtypes.rstring(filename));
	tempFile.setPath(omero.rtypes.rstring(filename));
	tempFile.setFormat(getFormat(session, format));
	tempFile.setSize(omero.rtypes.rlong(len(data)));
	tempFile.setSha1(omero.rtypes.rstring(calcSha1FromData(data)));
	updateService = session.getUpdateService();
	return updateService.saveAndReturnObject(tempFile);
	
def attachArrayToImage(session, image, file, nameSpace):
	updateService = session.getUpdateService();
	fa = omero.model.FileAnnotationI();
	fa.setFile(file);
	fa.setNs(omero.rtypes.rstring(nameSpace))
	l = omero.model.ImageAnnotationLinkI();
	l.setParent(image);
	l.setChild(fa);
	l = updateService.saveAndReturnObject(l);

def uploadArray(session, image, filename, data, format):
	file = createFileFromData(session, filename, data, format);
	rawFileStore = session.createRawFileStore();
	rawFileStore.setFileId(file.getId().getValue());
	fileSize = len(data);
	increment = 10000;
	cnt = 0;
	done = 0
	while(done!=1):
		if(increment+cnt<fileSize):
			blockSize = increment;
		else:
			blockSize = fileSize-cnt;
			done = 1;
		block = data[cnt:cnt+blockSize];
		rawFileStore.write(block, cnt, blockSize);
		cnt = cnt+blockSize;
	attachArrayToImage(session, image, file, CSV_NS)	

def arrayToCSV(data):
	size = data.shape;
	row = size[0];
	col = size[1];
	strdata ="";
	for r in range(0,row):
		for c in range(0, col):
			strdata = strdata + str(data[r,c])
			if(c<col-1):
				strdata = strdata+',';
		strdata = strdata + '\n';
	return strdata;    

def downloadPlane(rawPixelStore, pixels, z, c, t):
	rawPlane = rawPixelStore.getPlane(z, c, t);
	sizeX = pixels.getSizeX().getValue();
	sizeY = pixels.getSizeY().getValue();
	pixelType = pixels.getPixelsType().getValue().getValue();
	convertType ='>'+str(sizeX*sizeY)+pixelstypetopython.toPython(pixelType);
	convertedPlane = unpack(convertType, rawPlane);
	remappedPlane = numpy.array(convertedPlane, dtype=(pixelType));
	remappedPlane.resize(sizeX, sizeY);
	return remappedPlane;

def uploadPlane(rawPixelStore, plane, z, c, t):
	byteSwappedPlane = plane.byteswap();
	convertedPlane = byteSwappedPlane.tostring();
	rawPixelStore.setPlane(convertedPlane, z, c, t)

def getRenderingEngine(session, pixelsId):	
	renderingEngine = session.createRenderingEngine();
	renderingEngine.lookupPixels(pixelsId);
	if(renderingEngine.lookupRenderingDef(pixelsId)==0):
		renderingEngine.resetDefaults();
	renderingEngine.lookupRenderingDef(pixelsId);
	renderingEngine.load();
	return renderingEngine;

	
def createPlaneDef(z,t):
	planeDef = omero.romio.PlaneDef()
	planeDef.t = t;
	planeDef.z = z;
	planeDef.x = 0;
	planeDef.y = 0;
	planeDef.slice = 0;
	return planeDef;

def getPlaneAsPackedInt(renderingEngine, z, t):
	planeDef = createPlaneDef(z, t);
	return renderingEngine.renderAsPackedInt(planeDef);
	
def getRawPixelsStore(session, pixelsId):
	rawPixelsStore = session.createRawPixelsStore();
	rawPixelsStore.setPixelsId(pixelsId);
	return rawPixelsStore;

def getRawFileStore(session, fileId):
	rawFileStore = session.createRawFileStore();
	rawFileStore.setFileId(fileId);
	return rawFileStore;

def getPlaneInfo(iQuery, pixelsId, asOrderedList=0):
	query = "from PlaneInfo as Info where pixels.id='"+str(pixelsId)+"' orderby info.deltaT"
	infoList = queryService.findAllByQuery(query,None)

	if(asOrderedList==0):
		map = {}
		for info in infoList:
			key = "z:"+str(info.theZ.getValue())+"t:"+str(info.theT.getValue())+"c:"+str(info.theC.getValue());
			map[key] = info.deltaT.getValue();
		return map;	 
	else:
		return infoList;	

def IdentityFn(commandArgs):
	return commandArgs;

def parseInputs(client, session, processFn=IdentityFn):
	inputKeys = client.getInputKeys();
	commandArgs = {};
	for key in inputKeys:
		commandArgs[key]=client.getInput(key).getValue();
	return processFn(commandArgs);	
	