"""
 components/tools/OmeroPy/scripts/cecog/cecog2omero.py 

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

This script uses EMAN2 or PIL to read multiple planes from a local folder, combine and upload them to OMERO 
as new images with additional T, C, Z dimensions.
It should be run as a local script (not via scripting service) in order that it has
access to the local users file system. Therefore need EMAN2 or PIL installed locally. 

Example usage:
will$ python cecog2omero.py -h localhost -u root -p omero -d /Applications/CecogPackage/Data/Demo_data/0037/
Since this dir does not contain folders, this will upload images in '0037' into a Dataset called Demo_data 
in a Project called 'Data'. 

will$ python cecog2omero.py -h localhost -u root -p omero -d /Applications/CecogPackage/Data/Demo_data/
Since this dir does contain folders, this will look for images in all subdirectories of 'Demo_data' and
upload images into a Dataset called Demo_data in a Project called 'Data'.

Images will be combined in Z, C and T according to the MetaMorph_PlateScanPackage naming convention. 
E.g. tubulin_P0037_T00005_Cgfp_Z1_S1.tiff is Point 37, Timepoint 5, Channel gfp, Z 1. S? 
see /Applications/CecogPackage/CecogAnalyzer.app/Contents/Resources/resources/naming_schemes.conf 
    
@author  Will Moore &nbsp;&nbsp;&nbsp;&nbsp;
<a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
@version 3.0
<small>
(<b>Internal version:</b> $Revision: $Date: $)
</small>
@since 3.0-Beta4.2
 
"""

import numpy
from numpy import zeros
import getopt, sys, os

import omero
import omero.constants
from omero.rtypes import *
import omero_api_Gateway_ice    # see http://tinyurl.com/icebuserror
import omero.util.script_utils as scriptUtil
import re

from EMAN2 import EMData, EMNumPy

try:
    import Image
except:
    from PIL import Image

newImageMap = {}

# [MetaMorph_PlateScanPackage]
#regex_subdirectories = '(?=[^_]).*?(?P<D>\d+).*?'
regex_token = r'(?P<Token>.+)\.'
#regex_position = 'P(?P<P>.+?)_'
regex_time = r'T(?P<T>\d+)'
regex_channel = r'_C(?P<C>.+?)(_|$)'
regex_zslice = r'_Z(?P<Z>\d+)'
#continuous_frames = 1


def getPlaneFromLocalImage(imagePath):
    """
    Don't use this method! - Causes "Segmentation Fault"
    TODO: Fix this so it can be used as an alternative to PIL for reading Images -> numpy
    """
    d = EMData()
    d.read_image(imagePath)
    plane2D = EMNumPy.em2numpy(d)
    return plane2D
    

def getPlaneFromImage(imagePath):
    """
    Reads a local image (E.g. single plane tiff) and returns it as a numpy 2D array.
    
    @param imagePath   Path to image. 
    """
    i = Image.open(imagePath)
    a = numpy.asarray(i)
    return a
                
                
def uploadDirAsImages(services, path, dataset = None):
    """
    Reads all the images in the directory specified by 'path' and uploads them to OMERO as a single
    multi-dimensional image, placed in the specified 'dataset'
    Uses regex to determine the Z, C, T position of each image by name, 
    and therefore determines sizeZ, sizeC, sizeT of the new Image. 
    
    @param path     the path to the directory containing images. 
    @param dataset  the OMERO dataset, if we want to put images somewhere. omero.model.DatasetI
    """
    queryService = services["queryService"]
    gateway = services["gateway"]
    renderingEngine = services["renderingEngine"]
    pixelsService = services["pixelsService"]
    rawPixelStore = services["rawPixelStore"]
    
    t = re.compile(regex_time)
    c = re.compile(regex_channel)
    z = re.compile(regex_zslice)
    token = re.compile(regex_token)
    
    # assume 1 image in this folder for now. 
    # Make a single map of all images. key is (z,c,t). Value is image path.
    imageMap = {}        
    channelSet = set()
    tokens = []
    
    # other parameters we need to determine
    sizeZ = 1
    sizeC = 1
    sizeT = 1
    zStart = 1      # could be 0 or 1 ? 
    tStart = 1
    
    fullpath = None
    
    # process the names and populate our imagemap
    for f in os.listdir(path):
        tSearch = t.search(f)
        cSearch = c.search(f)
        zSearch = z.search(f)
        tokSearch = token.search(f)
        if tSearch == None or cSearch == None or zSearch == None: continue
        cName = cSearch.group('C')
        theT = int(tSearch.group('T'))
        theZ = int(zSearch.group('Z'))
        channelSet.add(cName)
        sizeZ = max(sizeZ, theZ)
        zStart = min(zStart, theZ)
        sizeT = max(sizeT, theT)
        tStart = min(tStart, theT)
        fullpath = os.path.join(path, f)
        if tokSearch != None: tokens.append(tokSearch.group('Token'))
        # print fullpath, theZ, cName, theT
        imageMap[(theZ,cName,theT)] = fullpath 
    
    channels = list(channelSet)
    sizeC = len(channels)
    
    # use the common stem as the image name
    imageName =  os.path.commonprefix(tokens).strip('0T_')
    description = "Imported from images in %s" % path
    print "Creating image: ", imageName
    
    # see if we can guess what colour the channels should be, based on name. 
    colourMap = {}
    for i, c in enumerate(channels):
        if c == 'rfp': colourMap[i] = (255,0,0,255)
        if c == 'gfp': colourMap[i] = (0,255,0,255)
        
    # use the last image to get X, Y sizes and pixel type
    plane = getPlaneFromImage(fullpath)
    pType = plane.dtype.name
    # look up the PixelsType object from DB
    pixelsType = queryService.findByQuery("from PixelsType as p where p.value='%s'" % pType, None) # omero::model::PixelsType
    if pixelsType == None and pType.startswith("float"):    # e.g. float32
        pixelsType = queryService.findByQuery("from PixelsType as p where p.value='%s'" % "float", None) # omero::model::PixelsType
    if pixelsType == None:
        print "Unknown pixels type for: " % pType
        return
    sizeY, sizeX = plane.shape
    
    print "sizeX: %s  sizeY: %s sizeZ: %s  sizeC: %s  sizeT: %s" % (sizeX, sizeY, sizeZ, sizeC, sizeT)
    #print zStart, tStart
    
    # code below here is very similar to combineImages.py
    # create an image in OMERO and populate the planes with numpy 2D arrays
    channelList = range(sizeC)
    iId = pixelsService.createImage(sizeX, sizeY, sizeZ, sizeT, channelList, pixelsType, imageName, description)
    image = gateway.getImage(iId.getValue())
    
    pixelsId = image.getPrimaryPixels().getId().getValue()
    rawPixelStore.setPixelsId(pixelsId, True)
    
    
    for theC in range(sizeC):
        minValue = 0
        maxValue = 0
        for theZ in range(sizeZ):
            zIndex = theZ + zStart
            for theT in range(sizeT):
                tIndex = theT + tStart
                if (zIndex, channels[theC], tIndex) in imageMap:
                    imagePath = imageMap[(zIndex, channels[theC], tIndex)]
                    print "Getting plane from:" , imagePath
                    plane2D = getPlaneFromImage(imagePath)
                else:
                    print "Creating blank plane for .", theZ, channels[theC], theT
                    plane2D = zeros((sizeY, sizeX))
                print "Uploading plane: theZ: %s, theC: %s, theT: %s" % (theZ, theC, theT)
                #scriptUtil.uploadPlaneByRow(rawPixelStore, plane2D, theZ, theC, theT)
                scriptUtil.uploadPlane(rawPixelStore, plane2D, theZ, theC, theT)
                minValue = min(minValue, plane2D.min())
                maxValue = max(maxValue, plane2D.max())
        #print "Setting the min, max ", minValue, maxValue
        pixelsService.setChannelGlobalMinMax(pixelsId, theC, float(minValue), float(maxValue))
        rgba = None
        if theC in colourMap:
            rgba = colourMap[theC]
        scriptUtil.resetRenderingSettings(renderingEngine, pixelsId, theC, minValue, maxValue, rgba)
        
    # add channel names 
    pixels = gateway.getPixels(pixelsId)
    i = 0
    for c in pixels.iterateChannels():        # c is an instance of omero.model.ChannelI
        lc = c.getLogicalChannel()            # returns omero.model.LogicalChannelI
        lc.setName(rstring(channels[i]))
        gateway.saveObject(lc)
        i += 1
            
    # put the image in dataset, if specified. 
    if dataset:
        link = omero.model.DatasetImageLinkI()
        link.parent = omero.model.DatasetI(dataset.id.val, False)
        link.child = omero.model.ImageI(image.id.val, False)
        gateway.saveAndReturnObject(link)


def cecogToOmero(commandArgs):
    """
    Processes the command args, makes project and dataset then calls uploadDirAsImages() to process and 
    upload the images to OMERO. 
    
    @param commandArgs      Map of command args. 
    """
    #print commandArgs
    path = commandArgs["dir"]
    
    client = omero.client(commandArgs["host"])
    session = client.createSession(commandArgs["username"], commandArgs["password"])
    
    # create the services we need 
    services = {}
    services["gateway"] = session.createGateway()
    services["renderingEngine"] = session.createRenderingEngine()
    services["queryService"] = session.getQueryService()
    services["pixelsService"] = session.getPixelsService()
    services["rawPixelStore"] = session.createRawPixelsStore()
    services["updateService"] = session.getUpdateService()
    
    gateway = services["gateway"]
    
    # if we don't have any folders in the 'dir' E.g. CecogPackage/Data/Demo_data/0037/
    # then 'Demo_data' becomes a dataset 
    subDirs = []
    for f in os.listdir(path):
        fullpath = path + f
        # process folders in root dir:
        if os.path.isdir(fullpath):
            subDirs.append(fullpath)
    
    # get the dataset name and project name from path
    if len(subDirs) == 0:
        p = path[:-1]   # will remove the last folder
        p = os.path.dirname(p)
    else:
        if os.path.basename(path) == "":
            p = path[:-1]   # remove slash
        
    datasetName = os.path.basename(p)   # e.g. Demo_data
    p = p[:-1]
    p = os.path.dirname(p)
    projectName = os.path.basename(p)   # e.g. Data
    print "Putting images in Project: %s  Dataset: %s" % (projectName, datasetName)
    
    # create dataset
    dataset = omero.model.DatasetI()
    dataset.name = rstring(datasetName)
    dataset = gateway.saveAndReturnObject(dataset)
    # create project
    project = omero.model.ProjectI()
    project.name = rstring(projectName)
    project = gateway.saveAndReturnObject(project)
    # put dataset in project 
    link = omero.model.ProjectDatasetLinkI()
    link.parent = omero.model.ProjectI(project.id.val, False)
    link.child = omero.model.DatasetI(dataset.id.val, False)
    gateway.saveAndReturnObject(link)
    
    if len(subDirs) > 0:
        for subDir in subDirs:
            print "Processing images in ", subDir
            uploadDirAsImages(services, subDir, dataset)
    
    # if there are no sub-directories, just put all the images in the dir
    else:
        print "Processing images in ", path
        uploadDirAsImages(services, path, dataset)
    
    
def readCommandArgs():
    
    def usage():
        print "Usage: python cecog2omero.py --host host --username username --password password --dir dir"
    try:
        opts, args = getopt.getopt(sys.argv[1:] ,"h:u:p:d:", ["host=", "username=", "password=","dir="])
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
        elif opt in ("-d","--dir"): 
            returnMap["dir"] = arg
    return returnMap

if __name__ == "__main__":        
    commandArgs = readCommandArgs()
    cecogToOmero(commandArgs)
    
    
