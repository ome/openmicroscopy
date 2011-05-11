"""
 components/tools/OmeroPy/scripts/omero/util_scripts/Images_From_ROIs.py

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
#import omero_api_IRoi_ice
from omero.gateway import BlitzGateway
from omero.rtypes import *
import omero.util.script_utils as script_utils

import os
import numpy

import time
startTime = 0

def printDuration(output=True):
    global startTime
    if startTime == 0:
        startTime = time.time()
    if output:
        print "Script timer = %s secs" % (time.time() - startTime)

def getRectangles(conn, imageId):
    """ Returns a list of (x, y, width, height, zStart, zStop, tStart, tStop) of each rectange ROI in the image """
    
    rois = []
    
    roiService = conn.getRoiService()
    result = roiService.findByImage(imageId, None)
    
    for roi in result.rois:
        zStart = None
        zEnd = 0
        tStart = None
        tEnd = 0
        x = None
        for shape in roi.copyShapes():
            if type(shape) == omero.model.RectI:
                # check t range and z range for every rectangle
                t = shape.getTheT().getValue()
                z = shape.getTheZ().getValue()
                if tStart is None:  tStart = t
                if zStart is None:  zStart = z
                tStart = min(t, tStart)
                tEnd = max(t, tEnd)
                zStart = min(z, zStart)
                zEnd = max(z, zEnd)
                if x is None:   # get x, y, width, height for first rect only
                    x = int(shape.getX().getValue())
                    y = int(shape.getY().getValue())
                    width = int(shape.getWidth().getValue())
                    height = int(shape.getHeight().getValue())
        # if we have found any rectangles at all...
        if zStart is not None:
            rois.append((x, y, width, height, zStart, zEnd, tStart, tEnd))

    return rois

def processImage(conn, imageId, parameterMap, dataset=None):
    """
    Process an image.
    If imageStack is True, we make a Z-stack using one tile from each ROI (c=0)
    Otherwise, we create a 5D image representing the ROI "cropping" the original image
    Image is put in a dataset if specified.
    """

    imageStack = parameterMap['Make_Image_Stack']
    containerName = parameterMap['Container_Name']

    image = conn.getObject("Image", imageId)
    if image is None:
        return

    parentDataset = image.getParent()
    parentProject = parentDataset.getParent()

    imageName = image.getName()
    updateService = conn.getUpdateService()

    pixels = image.getPrimaryPixels()
    # note pixel sizes (if available) to set for the new images
    physicalSizeX = pixels.getPhysicalSizeX()
    physicalSizeY = pixels.getPhysicalSizeY()

    rois = getRectangles(conn, imageId)     # x, y, w, h, zStart, zEnd, tStart, tEnd
    print "rois"
    print rois

    # if making a single stack image...
    if imageStack:
        print "\nMaking Image stack from ROIs of Image:", imageId
        print "physicalSize X, Y:  %s, %s" % (physicalSizeX, physicalSizeY)
        plane2Dlist = []
        # use width and height from first roi to make sure that all are the same.
        x,y,width,height, z1, z2, t1, t2 = rois[0]

        def tileGen():
            # list a tile from each ROI and create a generator of 2D planes
            zctTileList = []
            c = 0   # assume single channel image - Electron Microscopy use case
            for r in rois:
                x,y,w,h, z1,z2,t1,t2 = r
                tile = (x, y, width, height)
                zctTileList.append((z1, c, t1, tile))
            for t in pixels.getTiles(zctTileList):
                yield t

        newImageName = "%s_%s" % (os.path.basename(imageName), containerName)

        description = "Image from ROIS on parent Image:\n  Name: %s\n  Image ID: %d" % (imageName, imageId)
        print description
        image = conn.createImageFromNumpySeq(tileGen(), newImageName,
            sizeZ=len(rois), sizeC=1, sizeT=1, description=description, dataset=dataset)

        return image

    # ...otherwise, we're going to make a new 5D image per ROI
    else:
        iIds = []
        for r in rois:
            x,y,w,h, z1,z2,t1,t2 = r
            print "  ROI x: %s y: %s w: %s h: %s z1: %s z2: %s t1: %s t2: %s" % (x, y, w, h,z1,z2,t1,t2)

            # need a tile generator to get all the planes within the ROI
            sizeZ = z2-z1 + 1
            sizeT = t2-t1 + 1
            sizeC = image.getSizeC()
            zctTileList = []
            tile = (x, y, w, h)
            print "zctTileList..."
            for z in range(z1, z2+1):
                for c in range(sizeC):
                    for t in range(t1, t2+1):
                        zctTileList.append((z, c, t, tile))
            def tileGen():
                for i, t in enumerate(pixels.getTiles(zctTileList)):
                    yield t

            print "sizeZ, sizeC, sizeT", sizeZ, sizeC, sizeT
            description = "Created from image:\n  Name: %s\n  Image ID: %d \n x: %d y: %d" % (imageName, imageId, x, y)
            serviceFactory = conn.c.sf  # make sure that script_utils creates a NEW rawPixelsStore
            newI = conn.createImageFromNumpySeq(tileGen(), imageName,
                sizeZ=sizeZ, sizeC=sizeC, sizeT=sizeT, description=description)
            #pixels = image.getPrimaryPixels()
            #pixels.setPhysicalSizeX(rdouble(physicalSizeX))
            #pixels.setPhysicalSizeY(rdouble(physicalSizeY))
            #updateService.saveObject(pixels)
            iIds.append(newI.getId())

        if len(iIds) > 0:
            # create a new dataset for new images
            datasetName = containerName
            print "\nMaking Dataset '%s' of Images from ROIs of Image: %s" % (datasetName, imageId)
            print "physicalSize X, Y:  %s, %s" % (physicalSizeX, physicalSizeY)
            dataset = omero.model.DatasetI()
            dataset.name = rstring(datasetName)
            desc = "Images in this Dataset are from ROIs of parent Image:\n  Name: %s\n  Image ID: %d" % (imageName, imageId)
            dataset.description = rstring(desc)
            dataset = updateService.saveAndReturnObject(dataset)
            for iid in iIds:
                link = omero.model.DatasetImageLinkI()
                link.parent = omero.model.DatasetI(dataset.id.val, False)
                link.child = omero.model.ImageI(iid, False)
                updateService.saveObject(link)
            if parentProject:        # and put it in the current project
                link = omero.model.ProjectDatasetLinkI()
                link.parent = omero.model.ProjectI(parentProject.getId(), False)
                link.child = omero.model.DatasetI(dataset.id.val, False)
                updateService.saveAndReturnObject(link)
            return dataset

def makeImagesFromRois(conn, parameterMap):
    """
    Processes the list of Image_IDs, either making a new image-stack or a new dataset from each image,
    with new image planes coming from the regions in Rectangular ROIs on the parent images. 
    """
    
    imageIds = []
    dataType = parameterMap["Data_Type"]
    ids = parameterMap["IDs"]
    imageStack = parameterMap['Make_Image_Stack']
    
    newIds = []
    if dataType == 'Image':
        for iId in ids:
            new = processImage(conn, iId, parameterMap, dataset=None)
            if new is not None:
                newIds.append(new.getId())
            
    else:
        for dsId in ids:
            ds = conn.getObject("Dataset", dsId)
            for i in ds.listChildren():
                new = processImage(conn, i.getId(), parameterMap, dataset=None)
                if new is not None:
                    newIds.append(new.getId())

    plural = (len(newIds) == 1) and "." or "s."
    if imageStack:
        message = "Created %s new Image%s Refresh Dataset to view" % (len(newIds), plural)
    else:
        message = "Created %s new Dataset%s Refresh Project to view" % (len(newIds), plural)
    return message

def runAsScript():
    """
    The main entry point of the script, as called by the client via the scripting service, passing the required parameters. 
    """
    printDuration(False)    # start timer
    dataTypes = [rstring('Dataset'),rstring('Image')]
    
    client = scripts.client('Images_From_ROIs.py', """Create new Images from the regions defined by Rectangle ROIs on other Images.
Designed to work with single-plane images (Z=1 T=1) with multiple ROIs per image. 
If you choose to make an image stack from all the ROIs, this script
assumes that all the ROIs on each Image are the same size.""",

    scripts.String("Data_Type", optional=False, grouping="1",
        description="Choose Images via their 'Dataset' or directly by 'Image' IDs.", values=dataTypes, default="Image"),
        
    scripts.List("IDs", optional=False, grouping="2",
        description="List of Dataset IDs or Image IDs to process.").ofType(rlong(0)),
        
    scripts.String("Container_Name", optional=False, grouping="3",
        description="New Dataset name or Image name (if 'Make_Image_Stack')", default="From_ROIs"),
        
    scripts.Bool("Make_Image_Stack", grouping="4", default=False,
        description="If true, make a single Image (stack) from all the ROIs of each parent Image"),
        
    version = "4.2.0",
    authors = ["William Moore", "OME Team"],
    institutions = ["University of Dundee"],
    contact = "ome-users@lists.openmicroscopy.org.uk",
    )

    try:

        # process the list of args above.
        parameterMap = {}
        for key in client.getInputKeys():
            if client.getInput(key):
                parameterMap[key] = unwrap( client.getInput(key).getValue() )

        print parameterMap

        # create a wrapper so we can use the Blitz Gateway.
        conn = BlitzGateway(client_obj=client)

        message = makeImagesFromRois(conn, parameterMap)

        if message:
            client.setOutput("Message", rstring(message))
        else:
            client.setOutput("Message", rstring("Script Failed. See 'error' or 'info'"))

    finally:
        client.closeSession()
        printDuration()

if __name__ == "__main__":
    runAsScript()