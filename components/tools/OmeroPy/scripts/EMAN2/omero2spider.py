"""
 components/tools/OmeroPy/scripts/EMAN2/omero2spider.py 

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

This script gets images from an OMERO server and uses Spider to write the images locally to disk.

It should be run as a client-side script (not scripting service) since it needs access to 
the file-system to write the result files. 
This means that you need Spider installed on the client. 

Example usage (downloading images from dataset ID 901 as .dat files to the specified folder)
wjm:EMAN2 will$ python omero2spider.py -h localhost -u root -p omero -d 901 -o /Users/will/Desktop/Spider-export/
    
@author  Will Moore &nbsp;&nbsp;&nbsp;&nbsp;
<a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
@version 3.0
<small>
(<b>Internal version:</b> $Revision: $Date: $)
</small>
@since 3.0-Beta4.2
 
"""

from Spider.Spiderarray import array2spider

from numpy import zeros
import getopt, sys, os

import omero
import omero.constants
from omero.rtypes import *
import omero.util.script_utils as scriptUtil
import omero.util.pixelstypetopython as pixelstypetopython

# supported file types
filetypes = ["dat", "spi"]

def omeroToEm(commandArgs):
    
    # log-in 
    client = omero.client(commandArgs["host"])
    session = client.createSession(commandArgs["username"], commandArgs["password"])
    
    # get the services we need 
    queryService = session.getQueryService()
    updateService = session.getUpdateService()
    rawFileStore = session.createRawFileStore()
    rawPixelStore = session.createRawPixelsStore()
    containerService = session.getContainerService()
    
    images = []
    
    if "image" in commandArgs:
        iId = long(commandArgs["image"])
        i = containerService.getImages("Image", [iId], None)[0]
        images.append(i)
    elif "dataset" in commandArgs:
        dIds = [long(commandArgs["dataset"])]
        images = containerService.getImages("Dataset", dIds, None)
    else:
        print "No image or dataset ID given"
        return
        
    path = None
    if "path" in commandArgs:
        path = commandArgs["path"]
        if not os.path.exists(path):
            print "Given path: %s not found. Saving images in current directory." % path
            path = None
        
    extension = "dat"   # default
    format = None
    if "extension" in commandArgs:
        ext = commandArgs["extension"]
        if ext in filetypes:
            extension = ext
            print "Saving all images as .%s files." % extension
        else:
            print "Invalid extension: %s (not supported by Spider). Using %s" % (ext, extension)
            
    
    for image in images:
        iName = image.getName().getValue()
        imageName = os.path.basename(iName) # make sure no dir separators in name. 
        imageId = image.getId().getValue()
        
        if not imageName.endswith(".%s" % extension):
            imageName = "%s.%s" % (imageName, extension)
            
        if path:
            imageName = os.path.join(path,imageName)
            
        i = 1   # don't overwrite. Add number before extension
        dirName, ext = imageName.rsplit(".", 1)
        while os.path.exists(imageName):
            imageName = "%s_%s.%s" % (dirName,i,ext)
            i +=1
            
        print "Preparing to save image: %s" % imageName
        figLegend = ""
    
        # get pixels, with pixelsType
        query_string = "select p from Pixels p join fetch p.image i join fetch p.pixelsType pt where i.id='%d'" % imageId
        pixels = queryService.findByQuery(query_string, None)
        ptype = pixels.pixelsType.getValue().getValue()
        
        sizeX = pixels.getSizeX().getValue()
        sizeY = pixels.getSizeY().getValue()
        sizeZ = pixels.getSizeZ().getValue()

        # prepare rawPixelStore
        theC, theT = (0, 0)
        pixelsId = pixels.getId().getValue()
        bypassOriginalFile = True
        rawPixelStore.setPixelsId(pixelsId, bypassOriginalFile)
        
        if sizeZ == 1:
            plane2D = scriptUtil.downloadPlane(rawPixelStore, pixels, 0, theC, theT)
            array2spider(plane2D, imageName)
        else:   
            numpyType = pixelstypetopython.toNumpy(ptype)
            array3D = zeros( (sizeZ, sizeY, sizeX), dtype=numpyType )  
            for z in range(sizeZ):
                # get each plane and add to 3D array
                plane2D = scriptUtil.downloadPlane(rawPixelStore, pixels, z, theC, theT)
                array3D[z] = plane2D
            array2spider(array3D, imageName)
            

def readCommandArgs():
    host = ""
    username = ""
    password = ""
    bdb = ""
    
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
    omeroToEm(commandArgs)