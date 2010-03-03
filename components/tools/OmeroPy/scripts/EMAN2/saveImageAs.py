"""
 components/tools/OmeroPy/scripts/EMAN2/saveImageAs.py 

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

This script uses EMAN2 to convert images in OMERO into mrc files. 
	
@author  Will Moore &nbsp;&nbsp;&nbsp;&nbsp;
<a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
@version 3.0
<small>
(<b>Internal version:</b> $Revision: $Date: $)
</small>
@since 3.0-Beta4.2
 
"""

from EMAN2 import *

import numpy

import omero
import omero.scripts as scripts
from omero.rtypes import *
import omero.util.script_utils as scriptUtil

# would really like to get a list of the supported file types for saving, 
# as listed here: http://blake.bcm.edu/emanwiki/EMAN2ImageFormats
# but the closest I can find is EMAN2.cvs/lib/pyemtbx/imagetypes.py
# which has statements like:  IMAGE_MRC = EMUtil.ImageType.IMAGE_MRC

# maybe use   EMUtil.get_image_ext_type(outtype)

filetypes = {"hdf": None, 
	"mrc": "MRC", 
	"spi": None, # To read the overall image header in a stacked spider file, use image_index = -1.
	"img": None, # Imagic. seperate header and data file, cannot store multiple 3D images in one file. Regional I/O is only available for 2D.
	"dm3": "Gatan", 
	"tiff": "image/tiff",
	"tif": "image/tiff", 	# 8bit or 16bit per pixel
	"pgm": "PGM",  #8 bits per pixel
	"pif": None,	# images in PIF stack are homogenous. PIF doesn't currently work.
	"vtk": None,
	"png": "image/png",	# lossless data compression, 8 bit or 16 bit per pixel
	"img": None,	# seperate header and data file
	"icos": None,
	"emim": None, # images in stack are homogenous
	"dm2": "Gatan", 	# Gatan
	"am": "Amira",	# Amira
	"xplor": None,	# XPLOR 8 bytes integer, 12.5E float format
	"em": None,
	"v4l": None,	# Acquires images from the V4L2 interface in real-time(video4linux).
	"jpg": "image/jpeg",
	"jpeg": "image/jpeg", # lossy data compression
	"fts": None,  # common file format in astronomy
	"lst": None,  # ASCII file contains a list of image file names and numbers. Used in EMAN1 to avoid large files. Not commonly used in EMAN2
	"lsx": None,	# Optomized version of LST
	}
	
	
# keep track of log strings. 
logStrings = []

def log(text):
	"""
	Adds the text to a list of logs. Compiled into figure legend at the end.
	"""
	#print text
	logStrings.append(text)


def getPlane(queryService, rawPixelStore, imageId, pixels, theZ):
	"""
	This method downloads the first (only?) plane of the OMERO image and saves it as a local image.
	
	@param session		The OMERO session
	@param imageId		The ID of the image to download
	@param pixels		The pixels object, with pixelsType
	@param imageName	The name of the image to write. If no path, saved in the current directory. 
	"""

	theX = pixels.getSizeX().getValue()
	theY = pixels.getSizeY().getValue()

	# get the plane
	theC, theT = (0, 0)
	pixelsId = pixels.getId().getValue()
	bypassOriginalFile = True
	rawPixelStore.setPixelsId(pixelsId, bypassOriginalFile)
	plane2D = scriptUtil.downloadPlane(rawPixelStore, pixels, theZ, theC, theT)
	
	plane2D.resize((theY, theX))		# not sure why we have to resize (y, x)
	
	return plane2D


def saveImageAs(session, parameterMap):
	
	# get the services we need 
	queryService = session.getQueryService()
	updateService = session.getUpdateService()
	rawFileStore = session.createRawFileStore()
	rawPixelStore = session.createRawPixelsStore()
	
	imageIds = []
	
	if "imageIds" in parameterMap:
		for idCount, imageId in enumerate(parameterMap["imageIds"]):
			iId = long(imageId.getValue())
			imageIds.append(iId)
	else:
		print "No images"
		return
		
	extension = None
	format = None
	if "extension" in parameterMap:
		extension = parameterMap["extension"]
		if extension in filetypes:
			format = filetypes[extension]
			print "Saving all images as .%s files. Format: %s" % (extension, filetypes[extension])
		else:
			print "Invalid extension: %s (not supported by EMAN2). Will attempt to get extensions from image names." % extension
			extension = None
	else:
		print "No extension specified. Will attempt get extensions from image names."
	
	gateway = session.createGateway()
	for imageId in imageIds:
		
		image = gateway.getImage(imageId)
		imageName = image.getName().getValue()
		if (extension == None) or (extension not in filetypes):
			# try to get extension from image name
			lastDotIndex = imageName.rfind(".")		# .rpartition(sep)
			if lastDotIndex >= 0:
				extension = imageName[lastDotIndex+1:]
				if extension in filetypes:
					format = filetypes[extension]
		
		if (extension == None) or (extension not in filetypes):
			print "File extension from image invalid. Could not export image ID: %d  Name: %s  Extension: %s" % (imageId, imageName, extension)
			continue
			
			
		if not imageName.endswith(".%s" % extension):
			imageName = "%s.%s" % (imageName, extension)
		#print "Preparing to save image: %s" % imageName
		figLegend = ""
	
		# get pixels, with pixelsType
		query_string = "select p from Pixels p join fetch p.image i join fetch p.pixelsType pt where i.id='%d'" % imageId
		pixels = queryService.findByQuery(query_string, None)
		
		xSize = pixels.getSizeX().getValue()
		ySize = pixels.getSizeY().getValue()
		zSize = pixels.getSizeZ().getValue()

		# prepare rawPixelStore
		theC, theT = (0, 0)
		pixelsId = pixels.getId().getValue()
		bypassOriginalFile = True
		rawPixelStore.setPixelsId(pixelsId, bypassOriginalFile)

		e = EMData()
		em = EMData(xSize,ySize,zSize)
		
		for z in range(zSize):
			# get each plane and add to EMData 
			#print "Downloading plane: %d" % z
			plane2D = scriptUtil.downloadPlane(rawPixelStore, pixels, z, theC, theT)
			plane2D.resize((ySize, xSize))		# not sure why we have to resize (y, x)
			EMNumPy.numpy2em(plane2D, e)
			em.insert_clip(e,(0,0,z))
			
		em.write_image(imageName)
		
		if format == None:
			format = ""		# upload method will pick generic format. 
		print "Uploading image: %s to server with file type: %s" % (imageName, format)
		
		# attach to image
		fileId = scriptUtil.uploadAndAttachFile(queryService, updateService, rawFileStore, image, imageName, format, figLegend)	 


def runAsScript():
	"""
	The main entry point of the script, as called by the client via the scripting service, passing the required parameters. 
	"""
	client = scripts.client('saveImageAs.py', 'Use EMAN2 to save an image as mrc etc.', 
	scripts.List("imageIds").inout(),		# List of image IDs. 
	scripts.String("extension", optional=True).inout())	# File type/extension. E.g. "mrc". If not given, will try to use extension of each image name
	
	session = client.getSession()
	
	# process the list of args above. 
	parameterMap = {}
	for key in client.getInputKeys():
		if client.getInput(key):
			parameterMap[key] = client.getInput(key).getValue()
	
	saveImageAs(session, parameterMap)		# might return None if failed. 
	
	
if __name__ == "__main__":
	runAsScript()
	
