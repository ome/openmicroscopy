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
import omero.scripts
import getopt, sys, os, subprocess
from omero.rtypes import *
import omero.util.script_utils as scriptUtil

    
def getColours(re, sizeC, pixelsId):
    colours = []
    re.lookupPixels(pixelsId)
    re.lookupRenderingDef(pixelsId)
    re.load()
    for c in range(sizeC):
        colours.append(re.getRGBA(c))
    return colours
        
        
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
    re = session.createRenderingEngine()
    updateService = session.getUpdateService()
    
    imageId = None
    
    if "image" in commandArgs:
        imageId = long(commandArgs["image"])
    else:
        print "No image ID given"
        return
    
    # get the most recent (highest ID) script with the correct name
    scriptName = "/EMAN2/Save_Image_As_Em.py"
    scriptId = scriptService.getScriptID(scriptName)
    
    print "Running %s with script ID: %s" % (scriptName, scriptId)
    imageIds = omero.rtypes.rlist([omero.rtypes.rlong(imageId)])
    
    map = {
        "Image_IDs": imageIds,
        "Extension": omero.rtypes.rstring("mrc")    # export as mrc file
    }
    
    results = None
    
    proc = scriptService.runScript(scriptId, map, None)
    try:
        cb = omero.scripts.ProcessCallbackI(client, proc)
        while not cb.block(1000): # ms.
            pass
        cb.close()
        results = proc.getResults(0)    # ms
    finally:
        proc.close(False)
        
    
    path = None
    if "path" in commandArgs:
        path = commandArgs["path"]
    
    
    fileNames = []    # need names for passing to chimera
    if "Original_Files" in results:
        for r in results["Original_Files"].getValue():
            # download the file from OMERO 
            f = r.getValue()
            fileId = f.getId().getValue()
            print "Downloading Original File ID:", fileId
            originalFile = queryService.findByQuery("from OriginalFile as o where o.id = %s" % fileId, None)
            name = originalFile.getName().getValue()
            if path:
                name = os.path.join(path, name)
            filePath = scriptUtil.downloadFile(rawFileStore, originalFile, name)
            print "   file saved to:", filePath
            fileNames.append(filePath)        # if 'name' file already exists, filePath will be different 
            # This only deletes the DB row, not the data on disk! utils.cleanse.py removes files that are not in db. 
            updateService.deleteObject(originalFile)
    else:
        print "No OriginalFileIds returned by script"
        if 'stdout' in results:
            origFile = results['stdout'].getValue()
            fileId = origFile.getId().getValue()
            print "\n******** Script: %s generated StdOut in file:%s  *******" % (scriptName, fileId)
            print scriptUtil.readFromOriginalFile(rawFileStore, queryService, fileId)
        if 'stderr' in results:
            origFile = results['stderr'].getValue()
            fileId = origFile.getId().getValue()
            print "\n******** Script: %s generated StdErr in file:%s  *******" % (scriptName, fileId)
            print scriptUtil.readFromOriginalFile(rawFileStore, queryService, fileId)
        return
        

    # need to get colours for each channel, to pass to chimera. Chimera uses [(0.0, 0.0, 1.0, 1.0),(0.0, 1.0, 0.0, 1.0)]
    # This returns e.g. [[0, 0, 255, 255], [0, 255, 0, 255], [255, 0, 0, 255], [255, 0, 0, 255]] but seems to work OK. 
    pixels = containerService.getImages('Image', [imageId], None)[0].getPrimaryPixels()
    pixelsId = pixels.getId().getValue()
    sizeC = pixels.getSizeC().getValue()
    colours = getColours(re, sizeC, pixelsId)
    
    # now we need to make a Chimera script to open the images (channels) and colour them! 
    scriptLines = []
    scriptLines.append("from chimera import openModels")
    scriptLines.append("from VolumeViewer import Volume")
    for fn in fileNames:
        scriptLines.append("openModels.open('%s')" % fn)
    scriptLines.append("colors = %s" % colours)        # the colours list is rendered suitably as a string 
    scriptLines.append("for c, v in enumerate(openModels.list(modelTypes=[Volume])):")
    scriptLines.append("    v.set_parameters(surface_colors = [colors[c]])")
    scriptLines.append("    v.show()")
    
    print "\n".join(scriptLines)
    
    scriptName = "colourMaps.py"
    f = open(scriptName, 'w')        # will overwrite each time. 
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
