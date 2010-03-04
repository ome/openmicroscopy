"""
 components/tools/OmeroPy/scripts/EMAN2/eman2omero.py 

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

This script uses EMAN2 to read images from an EMAN2 bdb and upload them to OMERO as new images.
It should be run as a local script (not via scripting service) in order that it has
access to the local users bdb repository. 
The bdb repository should be specified as described on http://blake.bcm.edu/emanwiki/Eman2DataStorage 

The way you specify an image inside one of these databases is any of:
For a database in the local directory: bdb:dbname
For a database in another directory referenced to the current one: bdb:../local/path#dbname
For a database at an absolute path: bdb:/absolute/path/to/directory#dbname
	
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
import getopt, sys, os

import omero
import omero.constants
from omero.rtypes import *
import omero_api_Gateway_ice	# see http://tinyurl.com/icebuserror
import omero.util.script_utils as scriptUtil

gateway = None
queryService= None
pixelsService= None
rawPixelStore= None
re= None
updateService= None
rawFileStore= None

def resetRenderingSettings(re, pixelsId, minValue, maxValue):
	
	re.lookupPixels(pixelsId)
	if not re.lookupRenderingDef(pixelsId):
	#	print "No Rendering Def"
		re.resetDefaults()	
	
	if not re.lookupRenderingDef(pixelsId):
	#	print "Still No Rendering Def"
		pass
	
	re.load()
	
	re.setChannelWindow(0, float(minValue), float(maxValue))
	re.saveCurrentSettings()


def createNewImage(pixelsService, rawPixelStore, re, pixelsType, gateway, plane2Dlist, imageName, description, dataset=None):
	
	# all planes in plane2Dlist should be same shape. Render according to first plane. 
	shape = plane2Dlist[0].shape
	sizeY, sizeX = shape
	minValue = plane2Dlist[0].min()
	maxValue = plane2Dlist[0].max()
	print "min-max", minValue, maxValue
	
	channelList = [0]  # omero::sys::IntList
	
	sizeZ, sizeT = (len(plane2Dlist),1)
	#print sizeX, sizeY, sizeZ, sizeT, channelList, pixelsType, imageName, description
	iId = pixelsService.createImage(sizeX, sizeY, sizeZ, sizeT, channelList, pixelsType, imageName, description)
	imageId = iId.getValue()
	
	image = gateway.getImage(imageId)
	pixelsId = image.getPrimaryPixels().getId().getValue()
	pixelsService.setChannelGlobalMinMax(pixelsId, 0, float(minValue), float(maxValue))
	
	# upload plane data
	pixelsId = image.getPrimaryPixels().getId().getValue()
	rawPixelStore.setPixelsId(pixelsId, True)
	theC, theT = (0,0)
	for theZ, plane2D in enumerate(plane2Dlist):
		scriptUtil.uploadPlane(rawPixelStore, plane2D, theZ, theC, theT)

	resetRenderingSettings(re, pixelsId, minValue, maxValue)
	
	if dataset:
		link = omero.model.DatasetImageLinkI()
		link.parent = omero.model.DatasetI(dataset.id.val, False)
		link.child = omero.model.ImageI(image.id.val, False)
		gateway.saveAndReturnObject(link)
		#gateway.attachImageToDataset(dataset, image)
		
	return image
	
def uploadBdbAsDataset(infile, datasetName, project = None):
	
	"""
	@param infile 			path to bdb. Either absolute, or from where we are running
	@param datasetName		name for new Dataset to put images in
	@param project			if specified, put the dataset into this project (omero.model.ProjectI)
	
	"""
	
	nimg = EMUtil.get_image_count(infile)	# eg images in bdb 'folder'
	print "Found %d images to import from %s" % (nimg, infile)
	
	if nimg == 0:
		return
		
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
	d.read_image(infile, 0)
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
	description = "Imported from EMAN2 bdb: %s" % infile
	for i in range(5):
		print "Importing image: %d" % i
		newImageName = "%d" % i
		d.read_image(infile, i)
		plane2D = EMNumPy.em2numpy(d)
		#print plane2D
		plane2Dlist = [plane2D]		# single plane image
		
		# maybe should move this method to script_utils, since it is also used by imagesFromRois.py
		image = createNewImage(pixelsService, rawPixelStore, re, pixelsType, gateway, plane2Dlist, newImageName, description, dataset)
		
		f = open(fileName, 'w')		# will overwrite each time. 
		f.write("[GlobalMetadata]\n")
		
		# now add image attributes as "Original Metadata"
		for attr, value in d.get_attr_dict().items():
			# print attr, value
			f.write("%s=%s\n" % (attr, value))
			if attr == "ptcl_source_image":
				print "Add link to image named: ", value
		f.close()
		
		scriptUtil.uploadAndAttachFile(queryService, updateService, rawFileStore, image, fileName, "text/plain", None, namespace)
	# delete temp file
	os.remove(fileName)

def emanToOmero(commandArgs):
	#print commandArgs
	client = omero.client(commandArgs["host"])
	session = client.createSession(commandArgs["username"], commandArgs["password"])
	
	#global blitzcon
	#blitzcon = client_wrapper(commandArgs["username"], commandArgs["password"], host=commandArgs["host"], port=4063)
	#blitzcon.connect()
	#queryService = blitzcon.getQueryService()
	
	global gateway
	global re
	global queryService
	global pixelsService
	global rawPixelStore
	global updateService
	global rawFileStore
	
	gateway = session.createGateway()
	re = session.createRenderingEngine()
	queryService = session.getQueryService()
	pixelsService = session.getPixelsService()
	rawPixelStore = session.createRawPixelsStore()
	updateService = session.getUpdateService()
	rawFileStore = session.createRawFileStore()
	
	path = commandArgs["bdb"]
		
		
	# code from e2bdb.py
	dbpath = path
	if dbpath.lower()[:4]!="bdb:" : dbpath="bdb:"+dbpath
	if '#' in dbpath :
		#if len(args)>1 : print "\n",path,":"
		dbpath,dbs=dbpath.rsplit("#",1)
		dbpath+="#"
		dbs=[dbs]
	else:
		if not '#' in dbpath and dbpath[-1]!='/' : dbpath+='#'			
		#if len(args)>1 : print "\n",path[:-1],":"
		dbs=db_list_dicts(dbpath)
		
	# get a name for the project 
	head,tail = os.path.split(path)
	projectName = tail
	if projectName == "":
		projectName = head
	# create project
	project = omero.model.ProjectI()
	project.name = rstring(projectName)
	project = gateway.saveAndReturnObject(project)
	
	print dbs
	
	for db in dbs:
		infile = dbpath + db
		datasetName = db
		uploadBdbAsDataset(infile, datasetName, project)
		
	print path
	if os.path.isdir(path):
		for f in os.listdir(path):
			fullpath = path + f
			if f != "EMAN2DB" and os.path.isdir(fullpath):
				print fullpath
				dbpath = fullpath
				if dbpath.lower()[:4]!="bdb:" : dbpath="bdb:"+dbpath
				if '#' in dbpath :
					#if len(args)>1 : print "\n",path,":"
					dbpath,dbs=dbpath.rsplit("#",1)
					dbpath+="#"
					dbs=[dbs]
				else:
					if not '#' in dbpath and dbpath[-1]!='/' : dbpath+='#'			
					#if len(args)>1 : print "\n",path[:-1],":"
					dbs=db_list_dicts(dbpath)
					
				print "    " , dbs

				for db in dbs:
					infile = dbpath + db
					datasetName = db
					uploadBdbAsDataset(infile, datasetName, project)
	

def readCommandArgs():
	host = ""
	username = ""
	password = ""
	bdb = ""
	
	def usage():
		print "Usage: uploadscript --host host --username username --password password --bdb bdb"
	try:
		opts, args = getopt.getopt(sys.argv[1:] ,"h:u:p:b:", ["host=", "username=", "password=","bdb="])
	except getopt.GetoptError, err:          
		usage()                         
		sys.exit(2)                     
	for opt, arg in opts: 
		if opt in ("-h","--host"):
			host = arg;
		elif opt in ("-u","--username"): 
			username = arg;	
		elif opt in ("-p","--password"): 
			password = arg;	
		elif opt in ("-b","--bdb"): 
			bdb = arg;	
	returnMap = {"host":host, "username":username, "password":password, "bdb":bdb}
	return returnMap

if __name__ == "__main__":	    
	commandArgs = readCommandArgs()
	emanToOmero(commandArgs)
	
	
