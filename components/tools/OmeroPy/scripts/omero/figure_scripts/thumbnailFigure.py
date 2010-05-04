"""
 components/tools/OmeroPy/scripts/thumbnailFigure.py

-----------------------------------------------------------------------------
  Copyright (C) 2006-2009 University of Dundee. All rights reserved.


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

This script displays a bunch of thumbnails from OMERO as a jpg or png, saved
back to the server as a FileAnnotation attached to the parent dataset or project. 

@author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
<a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
@author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
@author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
@version 3.0
<small>
(<b>Internal version:</b> $Revision: $Date: $)
</small>
@since 3.0-Beta4.1
 
"""
import omero.scripts as scripts
import omero.util.script_utils as scriptUtil
from omero.rtypes import *
import omero.gateway
import omero_api_Gateway_ice    # see http://tinyurl.com/icebuserror
import omero.util.imageUtil as imgUtil
import Image, ImageDraw, ImageFont
from datetime import date
    

WHITE = (255, 255, 255)

JPEG = "image/jpeg"
PNG = "image/png"
formatExtensionMap = {JPEG:"jpg", PNG:"png"};

logLines = []    # make a log / legend of the figure
def log(text):
    """ Adds lines of text to the logLines list, so they can be collected into a figure legend. """
    # print text
    logLines.append(text)
    
    
def paintDatasetCanvas(session, images, title, tagIds=None, showUntagged = False, colCount = 10, length = 100):
    """
        Paints and returns a canvas of thumbnails from images, laid out in a set number of columns. 
        Title and date-range of the images is printed above the thumbnails,
        to the left and right, respectively. 
        
        @param session:         OMERO service factory. omero.api.ServiceFactoryPrx
        @param images:        list of Images. [omero.model.ImageI]
        @param title:        title to display at top of figure. String
        @param tagIds:        Optional to sort thumbnails by tag. [long]
        @param colCount:    max number of columns to lay out thumbnails 
        @param length:        length of longest side of thumbnails 
        @fontsize     size of font for title
    """
    
    mode = "RGB"
    figCanvas = None
    spacing = length/40 + 2
    
    gateway = session.createGateway()        # requires import omero_api_Gateway_ice
    thumbnailStore = session.createThumbnailStore()        # returns  omero.api.ThumbnailStorePrx
    metadataService = session.getMetadataService()
    
    if len(images) == 0:
        return None
    timestampMin = images[0].getAcquisitionDate().getValue()        # timestamp as 'long' millisecs
    timestampMax = timestampMin
    
    dsImageIds = []
    imagePixelMap = {}
    imageNames = {}
    
    # sort the images by name
    images.sort(key=lambda x:(x.getName().getValue().lower()))
    
    for image in images:
        imageId = image.getId().getValue()
        pixelId = image.getPrimaryPixels().getId().getValue()
        name = image.getName().getValue()
        dsImageIds.append(imageId)        # make a list of image-IDs
        imagePixelMap[imageId] = pixelId    # and a map of image-ID: pixel-ID
        imageNames[imageId] = name
        timestampMin = min(timestampMin, image.getAcquisitionDate().getValue())
        timestampMax = max(timestampMax, image.getAcquisitionDate().getValue())
    
    # set-up fonts
    fontsize = length/7 + 5
    font = imgUtil.getFont(fontsize)
    textHeight = font.getsize("Textq")[1]
    topSpacer = spacing + textHeight
    leftSpacer = spacing + textHeight
    
    tagPanes = []
    maxWidth = 0
    totalHeight = topSpacer
    
    # if we have a list of tags, then sort images by tag 
    if tagIds:
        log(" Sorting images by tags")
        tagNames = {}
        taggedImages = {}    # a map of tagId: list-of-image-Ids
        for tagId in tagIds:
            taggedImages[tagId] = []
        
        # look for images that have a tag
        types = ["ome.model.annotations.TagAnnotation"]
        annotations = metadataService.loadAnnotations("Image", dsImageIds, types, None, None)
        #filter images by annotation...
        for imageId, tags in annotations.items():
            for tag in tags:
                tagId = tag.getId().getValue()
                if tagId in tagIds:        # if image has multiple tags, it will be display more than once
                    taggedImages[tagId].append(imageId)        # add the image id to the appropriate list
                    if imageId in dsImageIds:
                        dsImageIds.remove(imageId)                # remember which we've picked already
                    if tagId not in tagNames.keys():
                        tagNames[tagId] = tag.getTextValue().getValue()        # make a dict of tag-names
        
        # if we want to show remaining images in dataset (not picked by tag)...
        if showUntagged:
            tagIds.append("noTag")
            taggedImages["noTag"] = [untaggedId for untaggedId in dsImageIds]
            tagNames["noTag"] = "Untagged"
        
        # print results and convert image-id to pixel-id
        # make a canvas for each tag
        for tagId in tagIds:
            if tagId not in tagNames.keys():    # no images with this tag
                continue
            leftLabel = tagNames[tagId]
            log(" Tag: %s  (contains %d images)" % (leftLabel, len(taggedImages[tagId])))
            pixelIds = []
            for imageId in taggedImages[tagId]:
                log("  Name: %s  ID: %d" % (imageNames[imageId], imageId))
                pixelIds.append(imagePixelMap[imageId])
            tagCanvas = imgUtil.paintThumbnailGrid(thumbnailStore, length, spacing, pixelIds, colCount, leftLabel=leftLabel)
            tagPanes.append(tagCanvas)
            maxWidth = max(maxWidth, tagCanvas.size[0])
            totalHeight += tagCanvas.size[1]
    
    else:
        leftSpacer = spacing
        pixelIds = []
        for imageId in dsImageIds:
            log("  Name: %s  ID: %d" % (imageNames[imageId], imageId))
            pixelIds.append(imagePixelMap[imageId])
        figCanvas = imgUtil.paintThumbnailGrid(thumbnailStore, length, spacing, pixelIds, colCount)
        tagPanes.append(figCanvas)
        maxWidth = max(maxWidth, figCanvas.size[0])
        totalHeight += figCanvas.size[1]
    
    # paste them into a single canvas
    size = (maxWidth, totalHeight)
    fullCanvas = Image.new(mode, size, WHITE)
    pX = 0
    pY = topSpacer
    for pane in tagPanes:
        imgUtil.pasteImage(pane, fullCanvas, pX, pY)
        pY += pane.size[1]
        
    # create dates for the image timestamps. If dates are not the same, show first - last. 
    firstdate = date.fromtimestamp(timestampMin/1000)
    lastdate = date.fromtimestamp(timestampMax/1000)
    figureDate = str(firstdate)
    if firstdate != lastdate:
        figureDate = "%s - %s" % (firstdate, lastdate)

    draw = ImageDraw.Draw(fullCanvas)
    dateWidth = draw.textsize(figureDate, font=font) [0]
    titleWidth = draw.textsize(title, font=font) [0]
    dateY = spacing
    dateX = fullCanvas.size[0] - spacing - dateWidth
    draw.text((leftSpacer, dateY), title, font=font, fill=(0,0,0))        # title
    if (leftSpacer+titleWidth) < dateX:            # if there's enough space...
        draw.text((dateX, dateY), figureDate, font=font, fill=(0,0,0))    # add date 
    
    return fullCanvas
    
    
def makeThumbnailFigure(client, session, commandArgs):    
    """
    Makes the figure using the parameters in @commandArgs, attaches the figure to the 
    parent Project/Dataset, and returns the file-annotation ID
    
    @ returns        Returns the id of the originalFileLink child. (ID object, not value)
    """
        
    log("Thumbnail figure created by OMERO")
    log("")
    
    gateway = session.createGateway()

    parent = None        # figure will be attached to this object 

    datasetIds = []
    if "datasetIds" in commandArgs:
        for datasetId in commandArgs["datasetIds"]:
            dId = long(datasetId.getValue())
            datasetIds.append(dId)
            
        if "parentId" in commandArgs:
            pId = commandArgs["parentId"]
            if pId >0:
                parent = gateway.getProjects([pId], False)[0]
                if parent:
                    log("Figure will be linked to Project: %s" % parent.getName().getValue())
        if parent == None:
            parent = gateway.getDataset(datasetIds[0], False)
            if parent:
                log("Figure will be linked to Dataset: %s" % parent.getName().getValue())
            
    imageIds = []
    # if no datasets are given, use image Ids instead...
    if len(datasetIds) == 0:
        if "imageIds" in commandArgs:
            for imageId in commandArgs["imageIds"]:
                iId = long(imageId.getValue())
                imageIds.append(iId)
        if "parentId" in commandArgs:
            pId = commandArgs["parentId"]
            if pId >0:
                parent = gateway.getDataset(pId, False)
                if parent:
                    log("Figure will be linked to Dataset: %s" % parent.getName().getValue())
        if parent == None:
            parent = gateway.getImage(imageIds[0])
            if parent:
                log("Figure will be linked to Image: %s" % parent.getName().getValue())

    tagIds = []
    if "tagIds" in commandArgs:
        for tagId in commandArgs["tagIds"]:
            tagIds.append(tagId.getValue())
    if len(tagIds) == 0:
        tagIds = None
        
    showUntagged = False
    if (tagIds):
        if "showUntaggedImages" in commandArgs:
            if commandArgs["showUntaggedImages"]:
                showUntagged = True
        
    thumbSize = 100
    if "thumbSize" in commandArgs:
        thumbSize = commandArgs["thumbSize"]
    
    maxColumns = 10
    if "maxColumns" in commandArgs:
        maxColumns = commandArgs["maxColumns"]
        

    figHeight = 0
    figWidth = 0
    dsCanvases = []
    

    for datasetId in datasetIds:
        datasetName = gateway.getDataset(datasetId, False).getName().getValue()
        images = gateway.getImages(omero.api.ContainerClass.Dataset, [datasetId])
        log("Dataset: %s     ID: %d     Images: %d" % (datasetName, datasetId, len(images)))
        dsCanvas = paintDatasetCanvas(session, images, datasetName, tagIds, showUntagged, length=thumbSize, colCount=maxColumns)
        if dsCanvas == None:
            continue
        dsCanvases.append(dsCanvas)
        figHeight += dsCanvas.size[1]
        figWidth = max(figWidth, dsCanvas.size[0])
        
    if len(datasetIds) == 0:
        images = []
        for imageId in imageIds:
            images.append(gateway.getImage(imageId))
        imageCanvas = paintDatasetCanvas(session, images, "Selected Images", tagIds, showUntagged, length=thumbSize, colCount=maxColumns)
        dsCanvases.append(imageCanvas)
        figHeight += imageCanvas.size[1]
        figWidth = max(figWidth, imageCanvas.size[0])
    
    figure = Image.new("RGB", (figWidth, figHeight), WHITE)
    y = 0
    for ds in dsCanvases:
        imgUtil.pasteImage(ds, figure, 0, y)
        y += ds.size[1]
    
    
    #figure.show()    # bug fixing only 
    
    log("")
    figLegend = "\n".join(logLines)
    
    #print figLegend    # bug fixing only
    
    format = JPEG
    if "format" in commandArgs:
        if commandArgs["format"] in [PNG, "PNG", "png"]:
            format = PNG
            
    output = "thumbnailFigure"
    if "figureName" in commandArgs:
        output = str(commandArgs["figureName"])
        
    if format == PNG:
        output = output + ".png"
        figure.save(output, "PNG")
    else:
        output = output + ".jpg"
        figure.save(output)
    
    queryService = session.getQueryService()
    updateService = session.getUpdateService()
    rawFileStore = session.createRawFileStore()
    
    # uploads the file to the server, attaching it to the 'parent' Project/Dataset as an OriginalFile annotation,
    # with the figLegend as the description. Returns the id of the originalFileLink child. (ID object, not value)
    fileId = scriptUtil.uploadAndAttachFile(queryService, updateService, rawFileStore, parent, output, format, figLegend)
    
    return fileId
        

def runAsScript():
    """
    The main entry point of the script. Gets the parameters from the scripting service, makes the figure and 
    returns the output to the client. 
    def __init__(self, name, optional = False, out = False, description = None, type = None, min = None, max = None, values = None)
    """

    def makeParam(paramClass, name, description=None, optional=True, min=None, max=None, values=None):
        param = paramClass(name, optional, description=description, min=min, max=max, values=values)
        return param

    client = scripts.client('thumbnailFigure.py', 'Export a figure of thumbnails, optionally sorted by tag.',
    makeParam(scripts.List, "datasetIds", "List of dataset IDs. Use this OR imageIds to specify images"),
    makeParam(scripts.List, "imageIds", "List of image IDs. Use this OR datasetIds"),
    makeParam(scripts.List, "tagIds", "Group thumbnails by these tags."),
    makeParam(scripts.Bool, "showUntaggedImages", "If true (and you're sorting by tagIds) also show images without the specified tags"),
    makeParam(scripts.Long, "parentId", "Attach figure to this Project (if datasetIds above) or Dataset if imageIds. If not specifed, attach figure to first dataset or image."),
    makeParam(scripts.Long, "thumbSize", "The dimension of each thumbnail. Default is 100", True, 10, 250),
    makeParam(scripts.Long, "maxColumns", "The max number of thumbnail columns. Default is 10", min=1),
    makeParam(scripts.String, "format", "Format to save image. E.g 'PNG'. Default is JPEG"),
    makeParam(scripts.String, "figureName", "File name of figure to create"),
    makeParam(scripts.Long, "fileAnnotation", "Script returns a file annotation").out())

    session = client.getSession()
    commandArgs = {}
    
    for key in client.getInputKeys():
        if client.getInput(key):
            commandArgs[key] = client.getInput(key).getValue()
    print commandArgs
    # Makes the figure and attaches it to Project/Dataset. Returns the id of the originalFileLink child. (ID object, not value)
    fileId = makeThumbnailFigure(client, session, commandArgs)
    client.setOutput("fileAnnotation",fileId)
    
    
if __name__ == "__main__":
    runAsScript()
