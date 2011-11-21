"""
 components/tools/OmeroPy/scripts/EMAN2/export2em.py 

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
to export OMERO images as EM images. 
The EM images are uploaded to the server as OriginalFiles by the server-side script
and the list of IDs are returned to this client script, which then downloads 
the EM images and deletes them from the server. 

Example usage, for exporting all the images in dataset ID: 901 as tif files to the given directory.
python export2em.py -h localhost -u root -p omero -d 901 -e tif -o /Users/will/Desktop/EMAN-export/tif/

Example, for exporting a single image, by image-id to the current folder, in mrc format
python export2em.py -h localhost -u root -p omero -i 235 -e mrc


The list of file formats supported by EMAN2 is listed on the EMAN2 web-site
http://blake.bcm.edu/emanwiki/EMAN2ImageFormats
    
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
from omero.rtypes import *
import omero.scripts
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
    updateService = session.getUpdateService()
    containerService = session.getContainerService()
    
    ids = []
    d = omero.api.ContainerClass.Dataset
    
    if "image" in commandArgs:
        iId = long(commandArgs["image"])
        ids.append(iId)
    elif "dataset" in commandArgs:
        dIds = [long(commandArgs["dataset"])]
        images = containerService.getImages("Dataset", dIds, None)
        for i in images:
            ids.append(i.getId().getValue())
    else:
        print "No image or dataset ID given"
        return
        
    if len(ids) == 0:
        print "No images found"
        return 
    
    # get the most recent (highest ID) original file with the correct script name
    scriptName = "Save_Image_As_Em.py"
    scriptId = -1
    for s in scriptService.getScripts():
        if s.getName().getValue() == scriptName:
            scriptId = max(scriptId, s.getId().getValue())
    
    print "Running script %s with ID: %s" % (scriptName, scriptId)
    
    imageIds = omero.rtypes.rlist([omero.rtypes.rlong(iId) for iId in ids])
    
    map = {
        "Image_IDs": imageIds,
    }  
    
    if "extension" in commandArgs:
        map["Extension"] = omero.rtypes.rstring(commandArgs["extension"])
    
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
        
    if "Original_Files" in results:
        for r in results["Original_Files"].getValue():
            # download the file from OMERO 
            f = r.getValue()    # unloaded originalfile
            fileId = f.getId().getValue()
            originalFile = queryService.findByQuery("from OriginalFile as o where o.id = %s" % fileId, None)
            name = originalFile.getName().getValue()
            if path:
                name = os.path.join(path, name)
            filePath = scriptUtil.downloadFile(rawFileStore, originalFile, name)
            print "Saved file at:", filePath
            # This only deletes the DB row, not the data on disk! utils.cleanse.py removes files that are not in db. 
            updateService.deleteObject(originalFile)
    else:
        print "No files generated by %s script on the server" % scriptName
            

def readCommandArgs():

    def usage():
        print "Usage: uploadscript --host host --username username --password password --dataset dataset --image image --extension ext --output output-path"
    try:
        opts, args = getopt.getopt(sys.argv[1:] ,"h:u:p:d:i:e:o:", 
            ["host=", "username=", "password=","dataset=", "image=", "extension=", "output="])
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
        elif opt in ("-d","--dataset"):
            returnMap["dataset"] = arg
        elif opt in ("-e","--extension"):
            returnMap["extension"] = arg
        elif opt in ("-o","--output"):
            returnMap["path"] = arg
            
    #print returnMap    
    return returnMap

if __name__ == "__main__":        
    commandArgs = readCommandArgs()
    run(commandArgs)
