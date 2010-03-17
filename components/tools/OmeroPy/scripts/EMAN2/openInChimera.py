"""
 components/tools/OmeroPy/scripts/EMAN2/openInChimera.py 

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

This script is a client-side script that uses the scripting service (and EMAN2 on the server)
to export OMERO images as mrc images, which are then opened in Chimera on the client. 

Example usage, for exporting all the images in dataset ID: 901 as tif files to the given directory.
python openInChimera.py -h localhost -u root -p omero -i 151 -o /Users/will/Desktop/EMAN-export/tif/

	
@author  Will Moore &nbsp;&nbsp;&nbsp;&nbsp;
<a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
@version 3.0
<small>
(<b>Internal version:</b> $Revision: $Date: $)
</small>
@since 3.0-Beta4.2
 
"""

import omero
import getopt, sys, os, subprocess
import omero_api_Gateway_ice	# see http://tinyurl.com/icebuserror
import omero_api_IScript_ice
import omero_SharedResources_ice
from omero.rtypes import *
import omero.util.script_utils as scriptUtil

def run(commandArgs):
	
	# login details
	host = commandArgs["host"]
	user = commandArgs["username"]
	password = commandArgs["password"]
	
	client = omero.client(host)
	session = client.createSession(user, password)
	
	# create the services we need
	scriptService = session.getScriptService()
	rawFileStore = session.createRawFileStore()
	queryService = session.getQueryService()
	gateway = session.createGateway()
	
	imageId = None
	
	if "image" in commandArgs:
		imageId = long(commandArgs["image"])
	else:
		print "No image ID given"
		return
	
	# get the most recent (highest ID) original file with the correct script name
	scriptName = "saveImageAs.py"
	scriptFiles = queryService.findAllByQuery("from OriginalFile as o where o.name = '%s'" % scriptName, None)
	scriptId = max([s.getId().getValue() for s in scriptFiles])
	
	print "Running saveImageAs.py with script ID: " , scriptId
	imageIds = omero.rtypes.rlist([omero.rtypes.rint(imageId)])
	
	map = {
		"imageIds": imageIds,
		"extension": omero.rtypes.rstring("mrc")	# export as mrc file
	}  
	
	argMap = omero.rtypes.rmap(map)
	
	results = None
	try: 
		# TODO: this will be refactored 
		job = omero.model.ScriptJobI() 
		job.linkOriginalFile(omero.model.OriginalFileI(scriptId, False)) 
		processor = session.sharedResources().acquireProcessor(job, 10) 
		proc = processor.execute(argMap) 
		processor.setDetach(True)
		proc.wait()
		results = processor.getResults(proc).getValue()
	except omero.ResourceError, re: 
		print "Could not launch", re
		
	
	path = None
	if "path" in commandArgs:
		path = commandArgs["path"]
		
	fileNames = []	# need names for passing to chimera
	if "originalFileIds" in results:
		for r in results["originalFileIds"].getValue():
			# download the file from OMERO 
			fileId = r.getValue()
			originalFile = queryService.findByQuery("from OriginalFile as o where o.id = %s" % fileId, None)
			name = originalFile.getName().getValue()
			if path:
				name = os.path.join(path, name)
			print "Downloading to:", name
			filePath = scriptUtil.downloadFile(rawFileStore, originalFile, name)
			fileNames.append(filePath)		# if 'name' file already exists, filePath will be different 
			# This only deletes the DB row, not the data on disk! utils.cleanse.py removes files that are not in db. 
			gateway.deleteObject(originalFile)
	else:
		print "No OriginalFileIds returned by script"
		return
		
	# now we need to make a Chimera script to open the images (channels) and colour them! 
	scriptLines = []
	scriptLines.append("from chimera import openModels")
	scriptLines.append("from VolumeViewer import Volume")
	for fn in fileNames:
		scriptLines.append("openModels.open('%s')" % fn)
	scriptLines.append("colors = [(0.0, 0.0, 1.0, 1.0),(0.0, 1.0, 0.0, 1.0),(1.0, 0.0, 0.0, 1.0),(1.0, 0.0, 0.0, 1.0)]")
	scriptLines.append("for c, v in enumerate(openModels.list(modelTypes=[Volume])):")
	scriptLines.append("    v.set_parameters(surface_colors = [colors[c]])")
	scriptLines.append("    v.show()")
	
	print "\n".join(scriptLines)
	
	scriptName = "colourMaps.py"
	f = open(scriptName, 'w')		# will overwrite each time. 
	for line in scriptLines:
		f.write(line)
		f.write("\n")
	f.close()
	
	command = "chimera --script %s" % scriptName
	print command
	os.system(command)
	

def readCommandArgs():

	def usage():
		print "Usage: openInChimera.py --host host --username username --password password --image image --output output-path"
	try:
		opts, args = getopt.getopt(sys.argv[1:] ,"h:u:p:i:o:", 
			["host=", "username=", "password=", "image=", "output="])
	except getopt.GetoptError, err:          
		usage()                         
		sys.exit(2) 
	returnMap = {}                    
	for opt, arg in opts: 
		if opt in ("-h","--host"):
			returnMap["host"] = arg
		elif opt in ("-u","--username"): 
			returnMap["username"] = arg	
		elif opt in ("-p","--password"): 
			returnMap["password"] = arg	
		elif opt in ("-i","--image"): 
			returnMap["image"] = arg
		elif opt in ("-o","--output"):
			returnMap["path"] = arg
			
	#print returnMap	
	return returnMap

if __name__ == "__main__":	    
	commandArgs = readCommandArgs()
	run(commandArgs)
