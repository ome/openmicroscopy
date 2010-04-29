"""
 components/tools/OmeroPy/scripts/combineImages.py 

-----------------------------------------------------------------------------
  Copyright (C) 2006-2010 University of Dundee. All rights reserved.


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

This script takes a number of images and merges them to create additional C, T, Z dimensions. 
	
@author  Will Moore &nbsp;&nbsp;&nbsp;&nbsp;
<a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
@version 3.0
<small>
(<b>Internal version:</b> $Revision: $Date: $)
</small>
@since 3.0-Beta4.2
 
"""

import numpy

import omero
import omero.scripts as scripts
import omero.constants
from omero.rtypes import *
import omero_api_Gateway_ice	# see http://tinyurl.com/icebuserror
import omero.util.script_utils as scriptUtil

colourOptions = {"blue": (0,0,255,255), "green":(0,255,0,255), "red":(255,0,0,255), "white":(255,255,255,255)}

def getPlane(rawPixelStore, pixels, theZ, theC, theT):
	"""
	This method downloads the specified plane of the OMERO image and returns it as a numpy array. 
	
	@param session		The OMERO session
	@param imageId		The ID of the image to download
	@param pixels		The pixels object, with pixelsType
	@param imageName	The name of the image to write. If no path, saved in the current directory. 
	"""

	sizeX = pixels.getSizeX().getValue()
	sizeY = pixels.getSizeY().getValue()

	# get the plane
	pixelsId = pixels.getId().getValue()
	bypassOriginalFile = True
	rawPixelStore.setPixelsId(pixelsId, bypassOriginalFile)
	plane2D = scriptUtil.downloadPlane(rawPixelStore, pixels, theZ, theC, theT)
	
	plane2D.resize((sizeY, sizeX))		# not sure why we have to resize (y, x)
	return plane2D

def combineImages(session, parameterMap):
	
	# get the services we need 
	gateway = session.createGateway()
	renderingEngine = session.createRenderingEngine()
	queryService = session.getQueryService()
	pixelsService = session.getPixelsService()
	rawPixelStore = session.createRawPixelsStore()
	rawPixelStoreUpload = session.createRawPixelsStore()
	updateService = session.getUpdateService()
	rawFileStore = session.createRawFileStore()
	
	# get the images IDs from list (in order) or dataset (sorted by name)
	dataset = None
	imageIds = []
	if "imageIds" in parameterMap:
		for imageId in parameterMap["imageIds"]:
			iId = long(imageId.getValue())
			imageIds.append(iId)
		# get dataset from first image
		query_string = "select i from Image i join fetch i.datasetLinks idl join fetch idl.parent where i.id in (%s)" % imageIds[0]
		image = queryService.findByQuery(query_string, None)
		if image:
			for link in image.iterateDatasetLinks():
				ds = link.parent
				dataset = gateway.getDataset(ds.id.val, True)
				print "Dataset", dataset.name.val
				break	# only use 1st dataset
	
	elif "datasetId" in parameterMap:
		datasetId = parameterMap["datasetId"]
		images = gateway.getImages(omero.api.ContainerClass.Dataset, [datasetId])
		images.sort(key=lambda x:(x.getName().getValue()))
		for i in images:
			imageIds.append(i.getId().getValue())
		dataset = gateway.getDataset(datasetId, False)
			
	if len(imageIds) == 0:
		return
		
	colourMap = {}
	if "colours" in parameterMap:
		for c, col in enumerate(parameterMap["colours"]):
			colour = col.getValue()
			if colour in colourOptions:
				colourMap[c] = colourOptions[colour]
		
	dimOrder = parameterMap["dimensionOrder"]
	dims = []
	nDims = len(dimOrder)
	
	sizeZ = 1
	sizeC = 1
	sizeT = 1
	
	dimSizes = [1,1,1]	# at least 1 in each dimension
	dimMap = {"C":"sizeC", "Z": "sizeZ", "T": "sizeT"}
	for d, dim in enumerate(dimOrder):
		dims.append(dim)
		size = dimMap[dim]
		if size in parameterMap:
			dimSizes[d]= parameterMap[size]
		else:
			print "calculate size of dim:", d, dim
			print "existing dim sizes:", dimSizes
			dimSizes[d] = len(imageIds) / (dimSizes[0] * dimSizes[1] * dimSizes[2])
			print "new dim sizes:", dimSizes
	
	if len(dimSizes) < nDims - 1:
		print "Not enough dimensions specified."
		return
	
	print dims
	print dimSizes
	
	imageIndex = 0
	
	imageMap = {}
		
	print ""
	for dim3 in range(dimSizes[2]):
		for dim2 in range(dimSizes[1]):
			for dim1 in range(dimSizes[0]):
				if imageIndex >= len(imageIds):
					break
				z,c,t = (0,0,0)
				ddd = (dim1, dim2, dim3)
				# bit of a hack, but this somehow does my head in!!
				for i, d in enumerate(dims):
					if d == "C": 
						c = ddd[i]
						sizeC = max(sizeC, c+1)
					elif d == "T": 
						t = ddd[i]
						sizeT = max(sizeT, t+1)
					elif d == "Z": 
						z = ddd[i]
						sizeZ = max(sizeZ, z+1)
				coords = (z,c,t)
				print "Assign ImageId: %s to z,c,t: %s" % (imageIds[imageIndex], coords)
				imageMap[(z,c,t)] = imageIds[imageIndex]
				imageIndex += 1
				
	print "sizeZ: %s  sizeC: %s  sizeT: %s" % (sizeZ, sizeC, sizeT)
	
	imageId = imageIds[0]
	
	
	# get pixels, with pixelsType, from the first image
	query_string = "select p from Pixels p join fetch p.image i join fetch p.pixelsType pt where i.id='%d'" % imageId
	pixels = queryService.findByQuery(query_string, None)
	pixelsType = pixels.getPixelsType()		# use the pixels type object we got from the first image. 
	
	sizeX = pixels.getSizeX().getValue()
	sizeY = pixels.getSizeY().getValue()
	
	imageName = "combinedImage"
	description = "created from image Ids: %s" % imageIds
	
	channelList = range(sizeC)
	iId = pixelsService.createImage(sizeX, sizeY, sizeZ, sizeT, channelList, pixelsType, imageName, description)
	image = gateway.getImage(iId.getValue())
	
	pixelsId = image.getPrimaryPixels().getId().getValue()
	rawPixelStoreUpload.setPixelsId(pixelsId, True)
	
	
	for theC in range(sizeC):
		minValue = 0
		maxValue = 0
		for theZ in range(sizeZ):
			for theT in range(sizeT):
				if (theZ, theC, theT) in imageMap:
					imageId = imageMap[(theZ, theC, theT)]
					print "Getting plane from Image ID:" , imageId
					query_string = "select p from Pixels p join fetch p.image i join fetch p.pixelsType pt where i.id='%d'" % imageId
					pixels = queryService.findByQuery(query_string, None)
					plane2D = getPlane(rawPixelStore, pixels, 0, 0, 0)	# just get first plane of each image (for now)
				else:
					print "Creating blank plane."
					plane2D = zeros((sizeY, sizeX))
				print "Uploading plane: theZ: %s, theC: %s, theT: %s" % (theZ, theC, theT)
				scriptUtil.uploadPlaneByRow(rawPixelStoreUpload, plane2D, theZ, theC, theT)
				minValue = min(minValue, plane2D.min())
				maxValue = max(maxValue, plane2D.max())
		print "Setting the min, max ", minValue, maxValue
		pixelsService.setChannelGlobalMinMax(pixelsId, theC, float(minValue), float(maxValue))
		rgba = colourOptions["white"]
		if theC in colourMap:
			rgba = colourMap[theC]
		scriptUtil.resetRenderingSettings(renderingEngine, pixelsId, theC, minValue, maxValue, rgba)
		
	if "channelNames" in parameterMap:
		cNames = []
		for name in parameterMap["channelNames"]:
			cNames.append(name.getValue())
			
		pixels = gateway.getPixels(pixelsId)
		i = 0
		for c in pixels.iterateChannels():        # c is an instance of omero.model.ChannelI
			if i >= len(cNames): break
			lc = c.getLogicalChannel()            # returns omero.model.LogicalChannelI
			lc.setName(rstring(cNames[i]))
			gateway.saveObject(lc)
			i += 1
			
	# put the image in dataset, if specified. 
	if dataset:
		link = omero.model.DatasetImageLinkI()
		link.parent = omero.model.DatasetI(dataset.id.val, False)
		link.child = omero.model.ImageI(image.id.val, False)
		gateway.saveAndReturnObject(link)
	

def runAsScript():
	"""
	The main entry point of the script, as called by the client via the scripting service, passing the required parameters. 
	"""
	client = scripts.client('combineImages.py', 'Combine multiple images into one with greater Z, C, T dimensions.', 
	scripts.List("imageIds", optional=True).inout(),	# use these images OR images from datasetId
	scripts.Long("datasetId", optional=True).inout(),	# use images in this dataset OR images from imageIds
	scripts.String("dimensionOrder").inout(),		# Dimensions in oder, E.g. "T" or "Z" or "ZC" or "ZCT" etc. 
	scripts.Long("sizeZ", optional=True).inout(),	# number of Z planes in new image. Only needed if combining multiple dimensions
	scripts.Long("sizeC", optional=True).inout(),	# number of channels in new image. Only needed if combining multiple dimensions
	scripts.Long("sizeT", optional=True).inout(),	# number of T points in new image. Only needed if combining multiple dimensions
	scripts.List("colours", optional=True).inout(),	# Colours for channels. Options are 'blue' 'green' 'red' 'white'
	scripts.List("channelNames", optional=True).inout())	# Names for channels in the new image. 
	
	session = client.getSession()
	
	# process the list of args above. 
	parameterMap = {}
	for key in client.getInputKeys():
		if client.getInput(key):
			parameterMap[key] = client.getInput(key).getValue()
	
	combineImages(session, parameterMap)		
	
	
if __name__ == "__main__":
	runAsScript()