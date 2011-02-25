"""
 components/tools/OmeroPy/scripts/EMAN2/omero2em.py 

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

This script gets images from an OMERO server and uses EMAN2 to write the images to disk 
in a variety of EM formats (see filetypes below)

It should be run as a client-side script (not scripting service) since it needs access to 
the file-system to write the result files. 
This means that you need EMAN2 installed on the client. 

Example usage (downloading images from dataset ID 901 as tiffs to the specified folder)
wjm:EMAN2 will$ python omero2em.py -h localhost -u root -p omero -d 901 -e tif -o /Users/will/Desktop/EMAN-export/ctf/
    
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
import omero.util.script_utils as scriptUtil


# map of EMAN2-supported extensins, with the corresponding OMERO file-types (or None if no match)
filetypes = {"hdf": None, 
    "mrc": "MRC", 
    "spi": None, # To read the overall image header in a stacked spider file, use image_index = -1.
    "img": None, # Imagic. seperate header and data file, cannot store multiple 3D images in one file. Regional I/O is only available for 2D.
    "dm3": "Gatan", 
    "tiff": "image/tiff",
    "tif": "image/tiff",     # 8bit or 16bit per pixel
    "pgm": "PGM",  #8 bits per pixel
    "pif": None,    # images in PIF stack are homogenous. PIF doesn't currently work.
    "vtk": None,
    "png": "image/png",    # lossless data compression, 8 bit or 16 bit per pixel
    "img": None,    # seperate header and data file
    "icos": None,
    "emim": None, # images in stack are homogenous
    "dm2": "Gatan",     # Gatan
    "am": "Amira",    # Amira
    "xplor": None,    # XPLOR 8 bytes integer, 12.5E float format
    "em": None,
    "v4l": None,    # Acquires images from the V4L2 interface in real-time(video4linux).
    "jpg": "image/jpeg",
    "jpeg": "image/jpeg", # lossy data compression
    "fts": None,  # common file format in astronomy
    "lst": None,  # ASCII file contains a list of image file names and numbers. Used in EMAN1 to avoid large files. Not commonly used in EMAN2
    "lsx": None,    # Optomized version of LST
    }


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
        
    extension = None
    format = None
    if "extension" in commandArgs:
        extension = commandArgs["extension"]
        if extension in filetypes:
            format = filetypes[extension]
            print "Saving all images as .%s files. Format: %s" % (extension, filetypes[extension])
        else:
            print "Invalid extension: %s (not supported by EMAN2). Will attempt to get extensions from image names." % extension
            extension = None
    else:
        print "No extension specified. Will attempt get extensions from image names."
    
    for image in images:
        iName = image.getName().getValue()
        imageName = os.path.basename(iName) # make sure no dir separators in name. 
        imageId = image.getId().getValue()
        if (extension == None) or (extension not in filetypes):
            # try to get extension from image name
            lastDotIndex = imageName.rfind(".")        # .rpartition(sep)
            if lastDotIndex >= 0:
                extension = imageName[lastDotIndex+1:]
                if extension in filetypes:
                    format = filetypes[extension]
        
        if (extension == None) or (extension not in filetypes):
            print "File extension from image invalid. Could not export image ID: %d  Name: %s  Extension: %s" % (imageId, imageName, extension)
            continue
            
            
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
            EMNumPy.numpy2em(plane2D, e)
            em.insert_clip(e,(0,0,z))
            
        em.write_image(imageName)
        

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
    
    
