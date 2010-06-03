"""
 components/tools/OmeroPy/scripts/EMAN2/Save_Image_As_Em.py 

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
import omero_api_Gateway_ice    # see http://tinyurl.com/icebuserror
import omero.scripts as scripts
from omero.rtypes import *      # includes wrap
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
    
    
# keep track of log strings. 
logStrings = []

def log(text):
    """
    Adds the text to a list of logs. Compiled into figure legend at the end.
    """
    #print text
    logStrings.append(text)


def saveImageAs(session, parameterMap):
    
    # get the services we need 
    queryService = session.getQueryService()
    updateService = session.getUpdateService()
    rawFileStore = session.createRawFileStore()
    rawPixelStore = session.createRawPixelsStore()
    
    imageIds = []
    
    if "Image_IDs" in parameterMap:
        for idCount, imageId in enumerate(parameterMap["Image_IDs"]):
            iId = long(imageId.getValue())
            imageIds.append(iId)
    else:
        print "No images"
        return
        
    cIndexes = None
    theT = 0
    if "Channel_Index" in parameterMap:
        cIndexes = [parameterMap["Channel_Index"]]
        
    extension = None
    format = None
    if "Extension" in parameterMap:
        extension = parameterMap["Extension"]
        if extension in filetypes:
            format = filetypes[extension]
            print "Saving all images as .%s files. Format: %s" % (extension, filetypes[extension])
        else:
            print "Invalid extension: %s (not supported by EMAN2). Will attempt to get extensions from image names." % extension
            extension = None
    else:
        print "No extension specified. Will attempt get extensions from image names."
    
    gateway = session.createGateway()
    originalFiles = []
    
    for imageId in imageIds:
        
        image = gateway.getImage(imageId)
        n = image.getName().getValue()
        imageName = os.path.basename(n)     # make sure we don't have path as name.
        if imageName == "":
            imageName = os.path.basename(n[:-1])
        
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
        print "Preparing to save image: %s" % imageName
        figLegend = ""
    
        # get pixels, with pixelsType
        query_string = "select p from Pixels p join fetch p.image i join fetch p.pixelsType pt where i.id='%d'" % imageId
        pixels = queryService.findByQuery(query_string, None)
        
        xSize = pixels.getSizeX().getValue()
        ySize = pixels.getSizeY().getValue()
        zSize = pixels.getSizeZ().getValue()
        cSize = pixels.getSizeC().getValue()
        
        if pixels.getPhysicalSizeX() == None:    physicalSizeX = 1.0
        else:    physicalSizeX = pixels.getPhysicalSizeX().getValue()
        if pixels.getPhysicalSizeY() == None:    physicalSizeY = 1.0
        else:    physicalSizeY = pixels.getPhysicalSizeY().getValue()
        if pixels.getPhysicalSizeZ() == None:    physicalSizeZ = 1.0
        else:    physicalSizeZ = pixels.getPhysicalSizeZ().getValue()
        
        
        if cIndexes == None:
            cIndexes = range(cSize)

        # prepare rawPixelStore
        pixelsId = pixels.getId().getValue()
        bypassOriginalFile = True
        rawPixelStore.setPixelsId(pixelsId, bypassOriginalFile)
        
        # export an EM image for every channel
        for theC in cIndexes:
            e = EMData()
            em = EMData(xSize,ySize,zSize)
            # if the physical size was in microns (in OMERO) now it's in Angstroms! 
            em.set_attr('apix_x', physicalSizeX)
            em.set_attr('apix_y', physicalSizeY)
            em.set_attr('apix_z', physicalSizeZ)
            
            if theC > 0:
                saveName = imageName.replace(extension, "%s.%s" % (theC, extension))
            else: saveName = imageName
            for z in range(zSize):
                # get each plane and add to EMData 
                #print "Downloading plane: %d" % z
                plane2D = scriptUtil.downloadPlane(rawPixelStore, pixels, z, theC, theT)
                EMNumPy.numpy2em(plane2D, e)
                em.insert_clip(e,(0,0,z))
            
            em.write_image(saveName)
        
            if format == None:
                format = ""        # upload method will pick generic format. 
            print "Uploading image: %s to server with file type: %s" % (saveName, format)
        
            # attach to image
            #fileId = scriptUtil.uploadAndAttachFile(queryService, updateService, rawFileStore, image, imageName, format, figLegend)     
        
            # want to return the image to client, without attaching it to anything the server
            # still need to upload it...
        
            fileformat = scriptUtil.getFormat(queryService, format)
            if fileformat == None:        # if we didn't find a matching format in the DB, use a generic format. 
                fileformat = scriptUtil.getFormat(queryService, "text/plain")
            originalFile = scriptUtil.createFile(updateService, saveName, fileformat, saveName)
            scriptUtil.uploadFile(rawFileStore, originalFile, saveName)
        
            print "File uploaded with ID:", originalFile.getId().getValue()
            originalFiles.append(originalFile)
    
    return originalFiles
    

def runAsScript():
    """
    The main entry point of the script, as called by the client via the scripting service, passing the required parameters. 
    """
    client = scripts.client('Save_Image_As_Em.py', """Use EMAN2 to save an image as mrc etc.
See http://trac.openmicroscopy.org.uk/omero/wiki/EmPreviewFunctionality""", 
    scripts.List("Image_IDs", optional=False, description="List of image IDs.").ofType(rlong(0)), 
    scripts.Int("Channel_Index",description="If images are multi-channel, specify a channel to save"),
    scripts.String("Extension", description="File type/extension. E.g. 'mrc'. If not given, will try to use extension of each image name"),
    )
    
    try:
        session = client.getSession()
    
        # process the list of args above. 
        parameterMap = {}
        for key in client.getInputKeys():
            if client.getInput(key):
                parameterMap[key] = client.getInput(key).getValue()
    
        originalFiles = saveImageAs(session, parameterMap)        # might return None if failed. 
    
        # Return a single list for other scripts to use
        client.setOutput("Original_Files", wrap(originalFiles))
        # But also return individual objects so Insight can offer 'Download' for each
        for i, o in enumerate(originalFiles):
            client.setOutput("Original_File%s"%i, omero.rtypes.robject(o))
    
    except: raise
    finally: client.closeSession()
    
if __name__ == "__main__":
    runAsScript()
    
