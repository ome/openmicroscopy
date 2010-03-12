"""
 components/tools/OmeroPy/scripts/EMAN2/ctf.py 

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

This script uses EMAN2 to perform CTF correction on images in OMERO. 
	
@author  Will Moore &nbsp;&nbsp;&nbsp;&nbsp;
<a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
@version 3.0
<small>
(<b>Internal version:</b> $Revision: $Date: $)
</small>
@since 3.0-Beta4.2
 
"""

from EMAN2 import *
import os

import numpy

import Image 	# for saving tiff locally. Maybe better to use EMAN2? 

import omero
import omero_api_Gateway_ice	# see http://tinyurl.com/icebuserror
import omero.scripts as scripts
from omero.rtypes import *
import omero.util.script_utils as scriptUtil

gateway = None
queryService= None
pixelsService= None
rawPixelStore= None
re= None
updateService= None
rawFileStore= None


# keep track of log strings. 
logStrings = []

def log(text):
	"""
	Adds the text to a list of logs. Compiled into figure legend at the end.
	"""
	#print text
	logStrings.append(text)
	

def uploadBdbsAsDataset(bdbContainer, datasetName, project = None):
	
	"""
	@param bdbContainer 	path to bdb (absolute OR from where we are running) OR this can be a list of image paths. 
	@param datasetName		name for new Dataset to put images in
	@param project			if specified, put the dataset into this project (omero.model.ProjectI)
	
	"""

	dbs = db_list_dicts('bdb:%s' % bdbContainer)
	
	print dbs
		
	d = EMData()
		
	dataset = omero.model.DatasetI()
	dataset.name = rstring(datasetName)
	dataset = gateway.saveAndReturnObject(dataset)
	if project:		# and put it in a new project
		link = omero.model.ProjectDatasetLinkI()
		link.parent = omero.model.ProjectI(project.id.val, False)
		link.child = omero.model.DatasetI(dataset.id.val, False)
		gateway.saveAndReturnObject(link)
	
	# use first image to get data-type (assume all the same!)
	dbpath = "bdb:particles#%s" % dbs[0]
	print "first image at: ", dbpath
	d.read_image(dbpath, 0)
	plane2D = EMNumPy.em2numpy(d)
	pType = plane2D.dtype.name
	print pType
	pixelsType = queryService.findByQuery("from PixelsType as p where p.value='%s'" % pType, None) # omero::model::PixelsType
	
	if pixelsType == None and pType.startswith("float"):
		# try 'float'
		pixelsType = queryService.findByQuery("from PixelsType as p where p.value='%s'" % "float", None) # omero::model::PixelsType
	if pixelsType == None:
		print "Unknown pixels type for: " % pType
		return
	else:
		print "Using pixels type ", pixelsType.getValue().getValue()
	
	namespace = omero.constants.namespaces.NSCOMPANIONFILE 
	fileName = "original_metadata.txt"
	
	# loop through all the images. 
	
	for db in dbs:
		dbpath = "bdb:particles#%s" % db 
		nimg = EMUtil.get_image_count(dbpath)	# eg images in bdb 'folder'
		print "Found %d images to import from: %s" % (nimg, dbpath)
		for i in range(nimg):
			newImageName = str(db)
			print "Importing image: %d" % i
			description = "Imported from EMAN2 bdb: %s" % dbpath
			print "importing from:" , dbpath
			d.read_image(dbpath, i)
			plane2D = EMNumPy.em2numpy(d)
			#print plane2D
			plane2Dlist = [plane2D]		# single plane image
		
			# maybe should move this method to script_utils, since it is also used by imagesFromRois.py, eman2omero.py
			image = scriptUtil.createNewImage(pixelsService, rawPixelStore, re, pixelsType, gateway, plane2Dlist, newImageName, description, dataset)
		
			f = open(fileName, 'w')		# will overwrite each time. 
			f.write("[GlobalMetadata]\n")
		
			# now add image attributes as "Original Metadata", sorted by key. 
			attributes = d.get_attr_dict()
			keyList = list(attributes.keys()) 	
			keyList.sort()
			for k in keyList:
				#print k, attributes[k]
				f.write("%s=%s\n" % (k, attributes[k]))
				if k == "ptcl_source_image":
					print "Add link to image named: ", attributes[k]
			f.close()
		
			scriptUtil.uploadAndAttachFile(queryService, updateService, rawFileStore, image, fileName, "text/plain", None, namespace)
		# delete temp file
		os.remove(fileName)

def downloadImage(queryService, rawPixelStore, imageId, imageName):
	"""
	This method downloads the first (only?) plane of the OMERO image and saves it as a local image.
	
	@param session		The OMERO session
	@param imageId		The ID of the image to download
	@param imageName	The name of the image to write. If no path, saved in the current directory. 
	"""

	# get pixels with pixelsType
	query_string = "select p from Pixels p join fetch p.image i join fetch p.pixelsType pt where i.id='%d'" % imageId
	pixels = queryService.findByQuery(query_string, None)
	theX = pixels.getSizeX().getValue()
	theY = pixels.getSizeY().getValue()

	# get the plane
	theZ, theC, theT = (0,0,0)
	pixelsId = pixels.getId().getValue()
	bypassOriginalFile = True
	rawPixelStore.setPixelsId(pixelsId, bypassOriginalFile)
	plane2D = scriptUtil.downloadPlane(rawPixelStore, pixels, theZ, theC, theT)
	
	plane2D.resize((theY, theX))		# not sure why we have to resize (y, x)
	p = Image.fromarray(plane2D)
	#p.show()
	p.save(imageName)
	
	return (theX, theY)
	

def runCtf(session, parameterMap):
	
	global gateway
	global re
	global queryService
	global pixelsService
	global rawPixelStore
	global updateService
	global rawFileStore
	
	# get the services we need, as global variables for all methods.  
	gateway = session.createGateway()
	re = session.createRenderingEngine()
	queryService = session.getQueryService()
	pixelsService = session.getPixelsService()
	rawPixelStore = session.createRawPixelsStore()
	updateService = session.getUpdateService()
	rawFileStore = session.createRawFileStore()
	
	imageIds = []
	
	if "imageIds" in parameterMap:
		for imageId in parameterMap["imageIds"]:
			iId = long(imageId.getValue())
			imageIds.append(iId)
	
	filenames = []
	
	fileExt = ".tiff"
	
	for imageId in imageIds:
		
		tempName = "%d%s" % (imageId, fileExt)
		downloadImage(queryService, rawPixelStore, imageId, tempName)
		filenames.append(tempName)
		
	voltage = parameterMap["voltage"]
	cs = parameterMap["cs"]
	apix = parameterMap["apix"]
	
	# use command line to run the ctf...
	ctf_command = "e2ctf.py *%s --voltage=%s --apix=%s --cs=%s --autofit" % (fileExt, voltage, apix, cs)
	write_command = "e2ctf.py *%s --phaseflip --wiener" % fileExt
	
	os.system(ctf_command)
	os.system(write_command)
	
	# hopefully by this point, we have a folder called 'particles' containing a bdb for each image.
	datasetName = "ctf-corrected" 
	uploadBdbsAsDataset('particles', datasetName, project = None)


def runAsScript():
	"""
	The main entry point of the script, as called by the client via the scripting service, passing the required parameters. 
	"""
	client = scripts.client('ctf.py', 'Use EMAN2 to calculate CTF correction on images.', 
	scripts.List("imageIds").inout(),	# List of image IDs.
	scripts.Long("voltage").inout(),	# Voltage in Kv 
	scripts.Long("cs").inout(),			# Coefficient of Spherical abherration
	scripts.Long("apix").inout())		# Angstroms per pixel. If not specified, try to use OMERO metadata! 
	
	session = client.getSession()
	
	# process the list of args above. 
	parameterMap = {}
	for key in client.getInputKeys():
		if client.getInput(key):
			parameterMap[key] = client.getInput(key).getValue()
	
	runCtf(session, parameterMap)
	
	# test
if __name__ == "__main__":
	runAsScript()
	
