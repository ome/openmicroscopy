"""
 components/tools/OmeroPy/scripts/omero/util_scripts/combineImages.py 

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

This script takes a number of images and merges them to create additional C, T, Z dimensions. 
    
@author  Will Moore &nbsp;&nbsp;&nbsp;&nbsp;
<a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
@version 3.0
<small>
(<b>Internal version:</b> $Revision: $Date: $)
</small>
@since 3.0-Beta4.2
 
"""

import numpy

import omero
import omero.scripts as scripts
import omero.constants
from omero.rtypes import *
import omero_api_Gateway_ice    # see http://tinyurl.com/icebuserror
import omero.util.script_utils as scriptUtil

COLOURS = scriptUtil.COLOURS

def getPlane(rawPixelStore, pixels, theZ, theC, theT):
    """
    This method downloads the specified plane of the OMERO image and returns it as a numpy array. 
    
    @param session        The OMERO session
    @param imageId        The ID of the image to download
    @param pixels        The pixels object, with pixelsType
    @param imageName    The name of the image to write. If no path, saved in the current directory. 
    """

    sizeX = pixels.getSizeX().getValue()
    sizeY = pixels.getSizeY().getValue()

    # get the plane
    pixelsId = pixels.getId().getValue()
    bypassOriginalFile = True
    rawPixelStore.setPixelsId(pixelsId, bypassOriginalFile)
    plane2D = scriptUtil.downloadPlane(rawPixelStore, pixels, theZ, theC, theT)
    
    plane2D.resize((sizeY, sizeX))        # not sure why we have to resize (y, x)
    return plane2D

def combineImages(session, parameterMap):
    
    # get the services we need 
    services = {}
    services["gateway"] = session.createGateway()
    services["renderingEngine"] = session.createRenderingEngine()
    services["queryService"] = session.getQueryService()
    services["pixelsService"] = session.getPixelsService()
    services["rawPixelStore"] = session.createRawPixelsStore()
    services["rawPixelStoreUpload"] = session.createRawPixelsStore()
    services["updateService"] = session.getUpdateService()
    services["rawFileStore"] = session.createRawFileStore()
    
    queryService = services["queryService"]
    gateway = services["gateway"]
    
    colourMap = {}
    if "Channel_Colours" in parameterMap:
        for c, col in enumerate(parameterMap["Channel_Colours"]):
            colour = col.getValue()
            if colour in COLOURS:
                colourMap[c] = COLOURS[colour]
                
    # get the images IDs from list (in order) or dataset (sorted by name)
    imageIds = []
    
    dataType = parameterMap["Data_Type"]
    if dataType == "Image":
        dataset = None
        for imageId in parameterMap["IDs"]:
            iId = long(imageId.getValue())
            imageIds.append(iId)
        # get dataset from first image
        query_string = "select i from Image i join fetch i.datasetLinks idl join fetch idl.parent where i.id in (%s)" % imageIds[0]
        image = queryService.findByQuery(query_string, None)
        if image:
            for link in image.iterateDatasetLinks():
                ds = link.parent
                dataset = gateway.getDataset(ds.id.val, True)
                print "Dataset", dataset.name.val
                break    # only use 1st dataset
        outputImage = makeSingleImage(services, parameterMap, imageIds, dataset, colourMap)
        return outputImage
    
    else:
        for dId in parameterMap["IDs"]:
            # TODO: This will only work on one dataset. Should process list! 
            datasetId = long(dId.getValue())
            
            images = gateway.getImages(omero.api.ContainerClass.Dataset, [datasetId])
            images.sort(key=lambda x:(x.getName().getValue()))
            for i in images:
                imageIds.append(i.getId().getValue())
            dataset = gateway.getDataset(datasetId, False)
            outputImage = makeSingleImage(services, parameterMap, imageIds, dataset, colourMap)
            
        return outputImage  # just return the last one
    
    
def makeSingleImage(services, parameterMap, imageIds, dataset, colourMap):
    
    if len(imageIds) == 0:
        return
        
    gateway = services["gateway"]
    renderingEngine = services["renderingEngine"]
    queryService = services["queryService"]
    pixelsService = services["pixelsService"]
    rawPixelStore = services["rawPixelStore"]
    rawPixelStoreUpload = services["rawPixelStoreUpload"]
    updateService = services["updateService"]
    rawFileStore = services["rawFileStore"]
    
    dims = []
    
    sizeZ = 1
    sizeC = 1
    sizeT = 1
    
    dimSizes = [1,1,1]    # at least 1 in each dimension
    dimMap = {"C":"Size_C", "Z": "Size_Z", "T": "Size_T"}

    
    for i, d in enumerate(["Dimension_1", "Dimension_2", "Dimension_3"]):
        if d in parameterMap and len(parameterMap[d]) > 0:
            dim = parameterMap[d][0]     # First letter of 'Channel' or 'Time' or 'Z'
            dims.append(dim)
            sizeParam = dimMap[dim]
            if sizeParam in parameterMap:
                dimSizes[i]= parameterMap[sizeParam]
            else:
                print "calculate size of dim:", d, dim
                print "existing dim sizes:", dimSizes
                dimSizes[i] = len(imageIds) / (dimSizes[0] * dimSizes[1] * dimSizes[2])
                print "new dim sizes:", dimSizes
    
    print "dims", dims
    print "dimSizes", dimSizes
    
    imageIndex = 0
    
    imageMap = {}
        
    print ""
    for dim3 in range(dimSizes[2]):
        for dim2 in range(dimSizes[1]):
            for dim1 in range(dimSizes[0]):
                if imageIndex >= len(imageIds):
                    break
                z,c,t = (0,0,0)
                ddd = (dim1, dim2, dim3)
                # bit of a hack, but this somehow does my head in!!
                for i, d in enumerate(dims):
                    if d == "C": 
                        c = ddd[i]
                        sizeC = max(sizeC, c+1)
                    elif d == "T": 
                        t = ddd[i]
                        sizeT = max(sizeT, t+1)
                    elif d == "Z": 
                        z = ddd[i]
                        sizeZ = max(sizeZ, z+1)
                coords = (z,c,t)
                print "Assign ImageId: %s to z,c,t: %s" % (imageIds[imageIndex], coords)
                imageMap[(z,c,t)] = imageIds[imageIndex]
                imageIndex += 1
                
    print "sizeZ: %s  sizeC: %s  sizeT: %s" % (sizeZ, sizeC, sizeT)
    
    imageId = imageIds[0]
    
    
    # get pixels, with pixelsType, from the first image
    query_string = "select p from Pixels p join fetch p.image i join fetch p.pixelsType pt where i.id='%d'" % imageId
    pixels = queryService.findByQuery(query_string, None)
    pixelsType = pixels.getPixelsType()        # use the pixels type object we got from the first image. 
    
    sizeX = pixels.getSizeX().getValue()
    sizeY = pixels.getSizeY().getValue()
    
    imageName = "combinedImage"
    description = "created from image Ids: %s" % imageIds
    
    channelList = range(sizeC)
    iId = pixelsService.createImage(sizeX, sizeY, sizeZ, sizeT, channelList, pixelsType, imageName, description)
    image = gateway.getImage(iId.getValue())
    
    pixelsId = image.getPrimaryPixels().getId().getValue()
    rawPixelStoreUpload.setPixelsId(pixelsId, True)
    
    
    for theC in range(sizeC):
        minValue = 0
        maxValue = 0
        for theZ in range(sizeZ):
            for theT in range(sizeT):
                if (theZ, theC, theT) in imageMap:
                    imageId = imageMap[(theZ, theC, theT)]
                    print "Getting plane from Image ID:" , imageId
                    query_string = "select p from Pixels p join fetch p.image i join fetch p.pixelsType pt where i.id='%d'" % imageId
                    pixels = queryService.findByQuery(query_string, None)
                    plane2D = getPlane(rawPixelStore, pixels, 0, 0, 0)    # just get first plane of each image (for now)
                else:
                    print "Creating blank plane."
                    plane2D = zeros((sizeY, sizeX))
                print "Uploading plane: theZ: %s, theC: %s, theT: %s" % (theZ, theC, theT)
                scriptUtil.uploadPlaneByRow(rawPixelStoreUpload, plane2D, theZ, theC, theT)
                minValue = min(minValue, plane2D.min())
                maxValue = max(maxValue, plane2D.max())
        print "Setting the min, max ", minValue, maxValue
        pixelsService.setChannelGlobalMinMax(pixelsId, theC, float(minValue), float(maxValue))
        rgba = COLOURS["White"]
        if theC in colourMap:
            rgba = colourMap[theC]
            print "Setting the Channel colour:", rgba
        scriptUtil.resetRenderingSettings(renderingEngine, pixelsId, theC, minValue, maxValue, rgba)
        
    if "Channel_Names" in parameterMap:
        cNames = []
        for name in parameterMap["Channel_Names"]:
            cNames.append(name.getValue())
            
        pixels = gateway.getPixels(pixelsId)
        i = 0
        for c in pixels.iterateChannels():        # c is an instance of omero.model.ChannelI
            if i >= len(cNames): break
            lc = c.getLogicalChannel()            # returns omero.model.LogicalChannelI
            lc.setName(rstring(cNames[i]))
            gateway.saveObject(lc)
            i += 1
            
    # put the image in dataset, if specified. 
    if dataset:
        link = omero.model.DatasetImageLinkI()
        link.parent = omero.model.DatasetI(dataset.id.val, False)
        link.child = omero.model.ImageI(image.id.val, False)
        gateway.saveAndReturnObject(link)
    
    return image


def runAsScript():
    """
    The main entry point of the script, as called by the client via the scripting service, passing the required parameters. 
    """
    
    ckeys = COLOURS.keys()
    ckeys.sort()
    cOptions = wrap(ckeys)
    dataTypes = [rstring('Dataset'),rstring('Image')]
    firstDim = [rstring('Time'),rstring('Channel'),rstring('Z')]
    extraDims = [rstring(''),rstring('Time'),rstring('Channel'),rstring('Z')]
    
    client = scripts.client('combineImages.py', 'Combine several single-plane images into one with greater Z, C, T dimensions.', 
    scripts.String("Data_Type", optional=False, grouping="1",
        description="Use all the images in specified 'Datasets' or choose individual 'Images'.", values=dataTypes, default="Dataset"),
    scripts.List("IDs", optional=False, grouping="2",
        description="List of Dataset IDs or Image IDs to combine.").ofType(rlong(0)),
    scripts.String("Dimension_1", optional=False, description="The first Dimension to change", values=firstDim), 
    scripts.String("Dimension_2", description="The second Dimension to change", values=extraDims, default=""), 
    scripts.String("Dimension_3", description="The third Dimension to change", values=extraDims, default=""), 
    scripts.Int("Size_Z", description="Number of Z planes in new image", min=1),
    scripts.Int("Size_C", description="Number of channels in new image", min=1),
    scripts.Int("Size_T", description="Number of time-points in new image", min=1),
    scripts.List("Channel_Colours", description="List of Colours for channels.", values=cOptions),
    scripts.List("Channel_Names", description="List of Names for channels in the new image."))
    
    session = client.getSession()
    
    # process the list of args above. 
    parameterMap = {}
    for key in client.getInputKeys():
        if client.getInput(key):
            parameterMap[key] = client.getInput(key).getValue()
    
    print parameterMap
    image = combineImages(session, parameterMap)        
    
    client.setOutput("Message", rstring("Script Ran OK. New Image created ID: %s" % image.id.val))
    client.setOutput("Combined_Image",robject(image))
    
if __name__ == "__main__":
    runAsScript()