"""
 components/tools/OmeroPy/scripts/omero/util_scripts/Combine_Images.py 

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

This script takes a number of images (or Z-stacks) and merges them to create additional C, T, Z dimensions. 
    
@author  Will Moore &nbsp;&nbsp;&nbsp;&nbsp;
<a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
@version 3.0
<small>
(<b>Internal version:</b> $Revision: $Date: $)
</small>
@since 3.0-Beta4.2
 
"""

import numpy
import re
from numpy import zeros

import omero
import omero.scripts as scripts
import omero.constants
from omero.rtypes import *
import omero.util.script_utils as scriptUtil

COLOURS = scriptUtil.COLOURS

DEFAULT_T_REGEX = "_T"
DEFAULT_Z_REGEX = "_Z"
DEFAULT_C_REGEX = "_C"

channelRegexes = {DEFAULT_C_REGEX: r'_C(?P<C>.+?)(_|$)', 
        "C": r'C(?P<C>\w+?)',
        "_c": r'_c(?P<C>\w+?)', 
        "_w": r'_w(?P<C>\w+?)',
        "None (single channel)": False }
        
zRegexes = {DEFAULT_Z_REGEX: r'_Z(?P<Z>\d+)', 
        "Z": r'Z(?P<Z>\d+)',
        "_z": r'_z(?P<Z>\d+)',
        "None (single z section)": False }

timeRegexes = {DEFAULT_T_REGEX: r'_T(?P<T>\d+)', 
        "T": r'T(?P<T>\d+)',
        "_t": r'_t(?P<T>\d+)',
        "None (single time point)": False }


import time
startTime = 0

def printDuration(output=True):
    global startTime
    if startTime == 0:
        startTime = time.time()
    if output:
        print "Script timer = %s secs" % (time.time() - startTime)
    
    
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
    return plane2D


def manuallyAssignImages(parameterMap, imageIds, sourceZ):
    
    sizeZ = sourceZ
    sizeC = 1
    sizeT = 1
    
    dims = []
    dimSizes = [1,1,1]    # at least 1 in each dimension
    dimMap = {"C":"Size_C", "Z": "Size_Z", "T": "Size_T"}
    dimensionParams = ["Dimension_1", "Dimension_2", "Dimension_3"]
    
    for i, d in enumerate(dimensionParams):
        if d in parameterMap and len(parameterMap[d]) > 0:
            dim = parameterMap[d][0]     # First letter of 'Channel' or 'Time' or 'Z'
            dims.append(dim)
            if dim == "Z" and sourceZ > 1:
                continue
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
    
    imageMap = {}   # map of (z,c,t) : imageId
         
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
                # handle Z stacks...
                if sourceZ > 1:
                    print "Z-STACK ImageId: %s to c,t: %s, %s" % (imageIds[imageIndex], c,t)
                    for srcZ in range(sourceZ):
                        imageMap[(srcZ,c,t)] = (imageIds[imageIndex], srcZ)
                else:
                    print "Assign ImageId: %s to z,c,t: %s, %s, %s" % (imageIds[imageIndex], z,c,t)
                    imageMap[(z,c,t)] = (imageIds[imageIndex], 0)
                imageIndex += 1
                
    print "sizeZ: %s  sizeC: %s  sizeT: %s" % (sizeZ, sizeC, sizeT)
    
    return (sizeZ, sizeC, sizeT, imageMap)


def assignImagesByRegex(parameterMap, imageIds, queryService, sourceZ, idNameMap=None):
    
    c = None
    regex_channel = channelRegexes[parameterMap["Channel_Name_Pattern"]]
    if regex_channel: c = re.compile(regex_channel)
    
    t = None
    regex_t = timeRegexes[parameterMap["Time_Name_Pattern"]]
    if regex_t: t = re.compile(regex_t)
    
    z = None
    regex_z = zRegexes[parameterMap["Z_Name_Pattern"]]
    if regex_z: z = re.compile(regex_z)
    
    # other parameters we need to determine
    sizeZ = sourceZ
    sizeC = 1
    sizeT = 1
    zStart = None      # could be 0 or 1 ? 
    tStart = None
    
    imageMap = {}  # map of (z,c,t) : imageId
    channels = []
    
    if idNameMap == None:
        idNameMap = getImageNames(queryService, imageIds)
        
    # assign each (imageId,zPlane) to combined image (z,c,t) by name. 
    for iId in imageIds:
        name = idNameMap[iId]
        if t: tSearch = t.search(name)
        if c: cSearch = c.search(name)
        
        if t==None or tSearch == None: theT = 0
        else: theT = int(tSearch.group('T'))
        
        if c==None or cSearch == None: cName = "0"
        else: cName = cSearch.group('C')
        if cName in channels:
            theC = channels.index(cName)
        else:
            theC = len(channels)
            channels.append(cName)
        
        sizeT = max(sizeT, theT+1)
        if tStart == None: tStart = theT
        else: tStart = min(tStart, theT)
        
        # we have T and C now. Need to check if source images are Z stacks
        if sourceZ > 1:
            zStart = 0
            print "Image STACK ID: %s Name: %s is C: %s T: %s channelName: %s" % (iId, name, theC, theT, cName)
            for srcZ in range(sourceZ):
                imageMap[(srcZ,theC,theT)] = (iId, srcZ)
        else:
            if z: zSearch = z.search(name)

            if z==None or zSearch == None: theZ = 0
            else: theZ = int(zSearch.group('Z'))
        
            sizeZ = max(sizeZ, theZ+1)
            if zStart == None: zStart = theZ
            else: zStart = min(zStart, theZ)
            
            print "Image ID: %s Name: %s is Z: %s C: %s T: %s channelName: %s" % (iId, name, theZ, theC, theT, cName)
            imageMap[(theZ,theC,theT)] = (iId, 0)   # every plane comes from z=0 
    
    print "tStart:", tStart, "zStart:", zStart, "sizeT", sizeT, "sizeZ", sizeZ
    
    # if indexes were 1-based (or higher), need to shift indexes accordingly. 
    if tStart > 0 or zStart > 0:
        sizeT = sizeT-tStart
        sizeZ = sizeZ-zStart
        print "sizeT", sizeT, "sizeZ", sizeZ
        iMap = {}
        for key, value in imageMap.items():
            z, c, t = key
            iMap[(z-zStart, c, t-tStart)] = value
    else: iMap = imageMap
    
    cNames = {}
    for c, name in enumerate(channels):
        cNames[c] = name
    return (sizeZ, cNames, sizeT, iMap)


def getImageNames(queryService, imageIds):
    idString = ",".join([str(i) for i in imageIds])
    query_string = "select i from Image i where i.id in (%s)" % idString
    images = queryService.findAllByQuery(query_string, None)
    idMap = {}
    for i in images:
        iId = i.id.val
        name = i.name.val
        idMap[iId] = name
    return idMap
    

def makeSingleImage(services, parameterMap, imageIds, dataset, colourMap):
    """
    This takes the images specified by imageIds, sorts them in to Z,C,T dimensions according to parameters
    in the parameterMap, assembles them into a new Image, which is saved in dataset. 
    """
    
    if len(imageIds) == 0:
        return
        
    renderingEngine = services["renderingEngine"]
    queryService = services["queryService"]
    pixelsService = services["pixelsService"]
    rawPixelStore = services["rawPixelStore"]
    rawPixelStoreUpload = services["rawPixelStoreUpload"]
    updateService = services["updateService"]
    rawFileStore = services["rawFileStore"]
    containerService = services["containerService"]
    
    print "imageIds", len(imageIds)
    
    # Filter images by name if user has specified filter. 
    idNameMap = None
    if "Filter_Names" in parameterMap:
        filterString = parameterMap["Filter_Names"]
        if len(filterString) > 0:
            print "Filtering images for names containing '%s'" % filterString
            idNameMap = getImageNames(queryService, imageIds)
            imageIds = [i for i in imageIds if idNameMap[i].find(filterString) > -1]
            
    print "imageIds", len(imageIds)
    imageId = imageIds[0]
    
    # get pixels, with pixelsType, from the first image
    query_string = "select p from Pixels p join fetch p.image i join fetch p.pixelsType pt where i.id='%d'" % imageId
    pixels = queryService.findByQuery(query_string, None)
    pixelsType = pixels.getPixelsType()        # use the pixels type object we got from the first image. 

    # combined image will have same X and Y sizes...
    sizeX = pixels.getSizeX().getValue()
    sizeY = pixels.getSizeY().getValue()
    sourceZ = pixels.getSizeZ().getValue()    # if we have a Z stack, use this in new image (don't combine Z)
    
    # Now we need to find where our planes are coming from. 
    # imageMap is a map of destination:source, defined as (newX, newY, newZ):(imageId, z)
    if "Manually_Define_Dimensions" in parameterMap and parameterMap["Manually_Define_Dimensions"]:
        sizeZ, sizeC, sizeT, imageMap = manuallyAssignImages(parameterMap, imageIds, sourceZ)
        cNames = {}
    else:
        sizeZ, cNames, sizeT, imageMap = assignImagesByRegex(parameterMap, imageIds, queryService, sourceZ, idNameMap)
        sizeC = len(cNames)
    
    print "sizeZ: %s  sizeC: %s  sizeT: %s" % (sizeZ, sizeC, sizeT)
    
    if "Channel_Names" in parameterMap:
        for c, name in enumerate(parameterMap["Channel_Names"]):
            cNames[c] = name.getValue()
            
    
    imageName = "combinedImage"
    description = "created from image Ids: %s" % imageIds
    
    channelList = range(sizeC)
    iId = pixelsService.createImage(sizeX, sizeY, sizeZ, sizeT, channelList, pixelsType, imageName, description)
    image = containerService.getImages("Image", [iId.getValue()], None)[0]
    
    pixelsId = image.getPrimaryPixels().getId().getValue()
    rawPixelStoreUpload.setPixelsId(pixelsId, True)
    
    
    for theC in range(sizeC):
        minValue = 0
        maxValue = 0
        for theZ in range(sizeZ):
            for theT in range(sizeT):
                if (theZ, theC, theT) in imageMap:
                    imageId,planeZ = imageMap[(theZ, theC, theT)]
                    print "Getting plane from Image ID:" , imageId
                    query_string = "select p from Pixels p join fetch p.image i join fetch p.pixelsType pt where i.id='%d'" % imageId
                    pixels = queryService.findByQuery(query_string, None)
                    plane2D = getPlane(rawPixelStore, pixels, planeZ, 0, 0)
                else:
                    print "Creating blank plane for theZ, theC, theT", theZ, theC, theT
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
    
    # rename new channels
    pixels = renderingEngine.getPixels()    # has channels loaded - (getting Pixels from image doesn't)
    i = 0
    for c in pixels.iterateChannels():        # c is an instance of omero.model.ChannelI
        if i >= len(cNames): break
        lc = c.getLogicalChannel()            # returns omero.model.LogicalChannelI
        lc.setName(rstring(cNames[i]))
        updateService.saveObject(lc)
        i += 1
            
    # put the image in dataset, if specified. 
    if dataset:
        link = omero.model.DatasetImageLinkI()
        link.parent = omero.model.DatasetI(dataset.id.val, False)
        link.child = omero.model.ImageI(image.id.val, False)
        updateService.saveAndReturnObject(link)
    
    return image
    
def combineImages(session, parameterMap):
    
    # get the services we need 
    services = {}
    services["containerService"] = session.getContainerService()
    services["renderingEngine"] = session.createRenderingEngine()
    services["queryService"] = session.getQueryService()
    services["pixelsService"] = session.getPixelsService()
    services["rawPixelStore"] = session.createRawPixelsStore()
    services["rawPixelStoreUpload"] = session.createRawPixelsStore()
    services["updateService"] = session.getUpdateService()
    services["rawFileStore"] = session.createRawFileStore()
    
    queryService = services["queryService"]
    containerService = services["containerService"]
    
    colourMap = {}
    if "Channel_Colours" in parameterMap:
        for c, col in enumerate(parameterMap["Channel_Colours"]):
            colour = col.getValue()
            if colour in COLOURS:
                colourMap[c] = COLOURS[colour]
                
    # get the images IDs from list (in order) or dataset (sorted by name)
    imageIds = []
    outputImages = []
    
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
                dataset = queryService.get("Dataset", ds.id.val)
                print "Dataset", dataset.name.val
                break    # only use 1st dataset
        else:
            print "No Dataset found for Image ID: %s  Combined Image will not be put into dataset." % imageIds[0]
        newImg = makeSingleImage(services, parameterMap, imageIds, dataset, colourMap)
        outputImages.append(newImg)
    
    else:
        for dId in parameterMap["IDs"]:
            # TODO: This will only work on one dataset. Should process list! 
            datasetId = long(dId.getValue())
            
            images = containerService.getImages("Dataset", [datasetId], None)
            if images == None or len(images) == 0:
                print "No images found for Dataset ID: %s" % datasetId
                continue
            images.sort(key=lambda x:(x.getName().getValue()))
            imageIds = [i.getId().getValue() for i in images]
            dataset = queryService.get("Dataset", datasetId)
            newImg = makeSingleImage(services, parameterMap, imageIds, dataset, colourMap)
            outputImages.append(newImg)
            
    # try and close any stateful services     
    for s in services:
        try:
            s.close()
        except: pass
        
    return outputImages


def runAsScript():
    """
    The main entry point of the script, as called by the client via the scripting service, passing the required parameters. 
    """
    printDuration(False)    # start timer
    
    ckeys = COLOURS.keys()
    ckeys.sort()
    cOptions = [rstring(col) for col in ckeys]
    dataTypes = [rstring('Dataset'),rstring('Image')]
    firstDim = [rstring('Time'),rstring('Channel'),rstring('Z')]
    extraDims = [rstring(''),rstring('Time'),rstring('Channel'),rstring('Z')]
    channelRegs = [rstring(r) for r in channelRegexes.keys()]
    zRegs = [rstring(r) for r in zRegexes.keys()]
    tRegs = [rstring(r) for r in timeRegexes.keys()]
    
    client = scripts.client('Combine_Images.py', """Combine several single-plane images (or Z-stacks) into one with 
greater Z, C, T dimensions.
See http://www.openmicroscopy.org/site/support/omero4/getting-started/tutorial/running-util-scripts""", 
    
    scripts.String("Data_Type", optional=False, grouping="1",
        description="Use all the images in specified 'Datasets' or choose individual 'Images'.", values=dataTypes, default="Image"),
        
    scripts.List("IDs", optional=False, grouping="2",
        description="List of Dataset IDs or Image IDs to combine.").ofType(rlong(0)),
        
    scripts.String("Filter_Names", grouping="2.1",
        description="Filter the images by names that contain this value"),
    
    scripts.Bool("Auto_Define_Dimensions", grouping="3", default=True,
        description="""Choose new dimensions with respect to the order of the input images. See URL above."""),
        
    scripts.String("Channel_Name_Pattern", grouping="3.1", default=DEFAULT_C_REGEX, values=channelRegs,
        description="""Auto-pick images by channel in the image name"""),

    scripts.String("Z_Name_Pattern", grouping="3.2", default=DEFAULT_Z_REGEX, values=zRegs,
        description="""Auto-pick images by Z-index in the image name"""),
    
    scripts.String("Time_Name_Pattern", grouping="3.3", default=DEFAULT_T_REGEX, values=tRegs,
        description="""Auto-pick images by T-index in the image name"""),
    
    scripts.Bool("Manually_Define_Dimensions", grouping="4", default=False,
        description="""Choose new dimensions with respect to the order of the input images. See URL above."""),
    
    scripts.String("Dimension_1", grouping="4.1",
        description="The first Dimension to change", values=firstDim), 
    
    scripts.String("Dimension_2", grouping="4.2", values=extraDims, default="",
        description="The second Dimension to change. Only specify this if combining multiple dimensions."), 
    
    scripts.String("Dimension_3", grouping="4.3",values=extraDims, default="",
        description="The third Dimension to change. Only specify this if combining multiple dimensions."), 
    
    scripts.Int("Size_Z", grouping="4.4",
        description="Number of Z planes in new image", min=1),
    
    scripts.Int("Size_C", grouping="4.5",
        description="Number of channels in new image", min=1),
    
    scripts.Int("Size_T", grouping="4.6",
        description="Number of time-points in new image", min=1),
    
    scripts.List("Channel_Colours", grouping="7",
        description="List of Colours for channels.", default="White", values=cOptions).ofType(rstring("")),
    
    scripts.List("Channel_Names", grouping="8",
        description="List of Names for channels in the new image."),
        
    version = "4.2.0",
    authors = ["William Moore", "OME Team"],
    institutions = ["University of Dundee"],
    contact = "ome-users@lists.openmicroscopy.org.uk",
    )

    try:
        session = client.getSession()

        # process the list of args above.
        parameterMap = {}
        for key in client.getInputKeys():
            if client.getInput(key):
                parameterMap[key] = client.getInput(key).getValue()

        print parameterMap

        # create the combined image
        images = combineImages(session, parameterMap)

        if len(images) == 1:
            client.setOutput("Message", rstring("Script Ran OK. New Image created ID: %s" % images[0].id.val))
            client.setOutput("Combined_Image",robject(images[0]))
        elif len(images) > 1:
            client.setOutput("Message", rstring("Script Ran OK. %d images created" % len(images) ))
            client.setOutput("First_Image",robject(images[0]))
        else:
            client.setOutput("Message", rstring("No images created."))
            print "No images created."
    finally:
        client.closeSession()
        printDuration()

if __name__ == "__main__":
    runAsScript()