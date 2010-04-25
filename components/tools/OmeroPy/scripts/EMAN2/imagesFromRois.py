"""
 components/tools/OmeroPy/scripts/EMAN2/imagesFromRois.py 

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

This script gets all the Rectangles from a particular image, then creates new images with 
the regions within the ROIs, and saves them back to the server.
    
@author  Will Moore &nbsp;&nbsp;&nbsp;&nbsp;
<a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
@version 3.0
<small>
(<b>Internal version:</b> $Revision: $Date: $)
</small>
@since 3.0-Beta4.2
 
"""

import omero
import omero.scripts as scripts
import omero_api_Gateway_ice
import omero_api_IRoi_ice
from omero.rtypes import *
import omero.util.script_utils as scriptUtil

import os
import numpy


def getRectangles(session, imageId):
    """ Returns (x, y, width, height) of each rectange ROI in the image """
    
    rectangles = []
    shapes = []        # string set. 
    
    roiService = session.getRoiService()
    result = roiService.findByImage(imageId, None)
    
    rectCount = 0
    for roi in result.rois:
        for shape in roi.copyShapes():
            if type(shape) == omero.model.RectI:
                x = shape.getX().getValue()
                y = shape.getY().getValue()
                width = shape.getWidth().getValue()
                height = shape.getHeight().getValue()
                rectangles.append((int(x), int(y), int(width), int(height)))
                continue
    return rectangles


def getImagePlane(session, imageId):

    queryService = session.getQueryService()
    rawPixelStore = session.createRawPixelsStore()

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
    
    plane2D.resize((theY, theX))        # not sure why we have to resize (y, x)
    return plane2D
    

def resetRenderingSettings(re, pixelsId, minValue, maxValue):
    
    re.lookupPixels(pixelsId)
    if not re.lookupRenderingDef(pixelsId):
    #    print "No Rendering Def"
        re.resetDefaults()    
    
    if not re.lookupRenderingDef(pixelsId):
    #    print "Still No Rendering Def"
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
    
    channelList = [0]  # omero::sys::IntList
    
    sizeZ, sizeT = (len(plane2Dlist),1)
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
        if plane2D.size > 1000000:
            scriptUtil.uploadPlaneByRow(rawPixelStore, plane2D, theZ, theC, theT)
        else:
            scriptUtil.uploadPlane(rawPixelStore, plane2D, theZ, theC, theT)

    resetRenderingSettings(re, pixelsId, minValue, maxValue)
    
    if dataset:
        link = omero.model.DatasetImageLinkI()
        link.parent = omero.model.DatasetI(dataset.id.val, False)
        link.child = omero.model.ImageI(image.id.val, False)
        gateway.saveAndReturnObject(link)
        #gateway.attachImageToDataset(dataset, image)


def makeImagesFromRois(session, parameterMap):
    
    imageIds = []
    if "imageIds" in parameterMap:
        for idCount, imageId in enumerate(parameterMap["imageIds"]):
            iId = long(imageId.getValue())
            imageIds.append(iId)
    
    if len(imageIds) == 0:
        print "No image-IDs in list."
        return
        
    queryService = session.getQueryService()
    pixelsService = session.getPixelsService()
    rawPixelStore = session.createRawPixelsStore()
    gateway = session.createGateway()
    re = session.createRenderingEngine()
    
    # get the project and dataset from the first image...
    imageId = imageIds[0]
    query_string = "select i from Image i join fetch i.datasetLinks idl join fetch idl.parent d join fetch d.projectLinks pl join fetch pl.parent where i.id in (%s)" % imageId
    image = queryService.findByQuery(query_string, None)
    project = None
    dataset = None
    if image:
        for link in image.iterateDatasetLinks():
            ds = link.parent
            dataset = gateway.getDataset(ds.id.val, True)
            print "Dataset", dataset.name.val
            for dpLink in ds.iterateProjectLinks():
                project = dpLink.parent
                print "Project", project.name.val
                break # only use 1st Project
            break    # only use 1st
    
    containerName = 'particles'
    if "containerName" in parameterMap:
        containerName = parameterMap["containerName"]
    
    
    for imageId in imageIds:
    
        imageName = gateway.getImage(imageId).getName().getValue()
    
        pixels = gateway.getPixelsFromImage(imageId)[0]
        physicalSizeX = pixels.getPhysicalSizeX().getValue()
        physicalSizeY = pixels.getPhysicalSizeY().getValue()
    
        # get plane of image
        plane2D = getImagePlane(session, imageId)
    
        # get ROI Rectangles, as (x, y, width, height)
        rects = getRectangles(session, imageId)
    
        pType = plane2D.dtype.name
        pixelsType = queryService.findByQuery("from PixelsType as p where p.value='%s'" % pType, None) # omero::model::PixelsType
        
        
        # if making a single particle-stack image...
        if ("makeParticleStack" in parameterMap) and (parameterMap["makeParticleStack"]):
        
            plane2Dlist = []
            # use width and height from first rectangle to make sure that all are the same. 
            x,y,width,height = rects[0]    
            for r in rects:
                x,y,w,h = r
                x2 = x+width
                y2 = y+height
                plane2Dlist.append(plane2D[y:y2, x:x2])
            
            newImageName = "%s_%s" % (os.path.basename(imageName), containerName)
        
            description = "Particles from image:\n Image Name: %s\n Image ID: %d" % (imageName, imageId)
            image = scriptUtil.createNewImage(pixelsService, rawPixelStore, re, pixelsType, gateway, plane2Dlist, newImageName, description, dataset)
        
            pixels = image.getPrimaryPixels()
            pixels.setPhysicalSizeX(rdouble(physicalSizeX))
            pixels.setPhysicalSizeY(rdouble(physicalSizeY))
            gateway.saveObject(pixels)
    
        # ..else, make an image for each ROI (all in one dataset?)
        else:
            # create a new dataset for new images
            datasetName = "%s_%s" % (os.path.basename(imageName), containerName)    # e.g. myImage.mrc_particles
            dataset = omero.model.DatasetI()
            dataset.name = rstring(datasetName)
            dataset = gateway.saveAndReturnObject(dataset)
            if project:        # and put it in the current project
                link = omero.model.ProjectDatasetLinkI()
                link.parent = omero.model.ProjectI(project.id.val, False)
                link.child = omero.model.DatasetI(dataset.id.val, False)
                gateway.saveAndReturnObject(link)
    
            for r in rects:
                x,y,w,h = r
                x2 = x+w
                y2 = y+h
                array = plane2D[y:y2, x:x2]     # slice the ROI rectangle data out of the whole image-plane 2D array
            
                description = "Created from image:\n Image Name: %s\n Image ID: %d \n x: %d y: %d" % (imageName, imageId, x, y)
                image = scriptUtil.createNewImage(pixelsService, rawPixelStore, re, pixelsType, gateway, [array], "particle", description, dataset)
            
                pixels = image.getPrimaryPixels()
                pixels.setPhysicalSizeX(rdouble(physicalSizeX))
                pixels.setPhysicalSizeY(rdouble(physicalSizeY))
                gateway.saveObject(pixels)

def runAsScript():
    """
    The main entry point of the script, as called by the client via the scripting service, passing the required parameters. 
    """
    client = scripts.client('imagesFromRois.py', 'Create new images from the regions defined by Rectangle ROIs of another image', 
    scripts.List("imageIds").inout(),            # List of image IDs, with Rectangle ROIs.
    scripts.String("containerName", optional=True).inout(),    #     New Dataset name (in the same project as image) or Image-stack name
    scripts.Bool("makeParticleStack", optional=True).inout())
    
    session = client.getSession();
    
    # process the list of args above. 
    parameterMap = {}
    for key in client.getInputKeys():
        if client.getInput(key):
            parameterMap[key] = client.getInput(key).getValue()
    
    makeImagesFromRois(session, parameterMap)
    

if __name__ == "__main__":
    runAsScript()