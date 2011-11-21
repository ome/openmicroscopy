"""
 components/tools/OmeroPy/scripts/omero/figure_scripts/ROI_Split_Figure.py

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

This script takes a number of images and displays regions defined by their ROIs as
zoomed panels beside the images.

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
import omero.util.imageUtil as imgUtil
import omero.util.figureUtil as figUtil
import omero.util.script_utils as scriptUtil
from omero.rtypes import *
# import util.figureUtil as figUtil    # need to comment out for upload to work. But need import for script to work!!
import getopt, sys, os, subprocess
import StringIO
from omero_sys_ParametersI import ParametersI
from datetime import date
    
try:
    from PIL import Image, ImageDraw # see ticket:2597
except ImportError:
    import Image, ImageDraw # see ticket:2597

JPEG = "image/jpeg"
PNG = "image/png"

WHITE = (255,255,255)
COLOURS = scriptUtil.COLOURS    # name:(rgba) map
OVERLAY_COLOURS = dict(COLOURS, **scriptUtil.EXTRA_COLOURS)

logStrings = []
def log(text):
    """
    Adds the text to a list of logs. Compiled into figure legend at the end.
    """
    print text
    logStrings.append(text)    


def addScalebar(scalebar, xIndent, yIndent, image, pixels, colour):
    """ adds a scalebar at the bottom right of an image, No text. 
    
    @scalebar         length of scalebar in microns 
    @xIndent        indent from the right of the image
    @yIndent         indent from the bottom of the image
    @image            the PIL image to add scalebar to. 
    @pixels         the pixels object
    @colour         colour of the overlay as r,g,b tuple
    """
    draw = ImageDraw.Draw(image)
    if pixels.getPhysicalSizeX() == None:
        return False
    pixelSizeX = pixels.getPhysicalSizeX().getValue()
    if pixelSizeX <= 0:
        return False
    iWidth, iHeight = image.size
    lineThickness = (iHeight//100) + 1
    scaleBarY = iHeight - yIndent
    scaleBarX = iWidth - scalebar//pixelSizeX - xIndent
    scaleBarX2 = iWidth - xIndent
    if scaleBarX<=0 or scaleBarX2<=0 or scaleBarY<=0 or scaleBarX2>iWidth:
        return False
    for l in range(lineThickness):
        draw.line([(scaleBarX,scaleBarY), (scaleBarX2,scaleBarY)], fill=colour)
        scaleBarY -= 1
    return True


def getROIsplitView    (re, pixels, zStart, zEnd, splitIndexes, channelNames, mergedNames, colourChannels, mergedIndexes, mergedColours, 
            roiX, roiY, roiWidth, roiHeight, roiZoom, tIndex, spacer, algorithm, stepping, fontsize, showTopLabels):
    """ This takes a ROI rectangle from an image and makes a split view canvas of the region in the ROI, zoomed 
        by a defined factor. 
        
    @param    re        The OMERO rendering engine. 
    """
    
    if algorithm is None:    # omero::constants::projection::ProjectionType
        algorithm = omero.constants.projection.ProjectionType.MAXIMUMINTENSITY
    mode = "RGB"
    white = (255, 255, 255)    
    
    sizeX = pixels.getSizeX().getValue()
    sizeY = pixels.getSizeY().getValue()
    sizeZ = pixels.getSizeZ().getValue()
    sizeC = pixels.getSizeC().getValue()
    
    if pixels.getPhysicalSizeX():
        physicalX = pixels.getPhysicalSizeX().getValue()
    else:
        physicalX = 0 
    if pixels.getPhysicalSizeY():
        physicalY = pixels.getPhysicalSizeY().getValue()
    else:
        physicalY = 0
    log("  Pixel size (um): x: %.3f  y: %.3f" % (physicalX, physicalY))
    log("  Image dimensions (pixels): x: %d  y: %d" % (sizeX, sizeY))
    
    log(" Projecting ROIs...")
    proStart = zStart
    proEnd = zEnd
    # make sure we're within Z range for projection. 
    if proEnd >= sizeZ:
        proEnd = sizeZ - 1
        if proStart > sizeZ:
            proStart = 0
        log(" WARNING: Current image has fewer Z-sections than the primary image projection.")
    if proStart < 0:
        proStart = 0
    log("  Projecting z range: %d - %d   (max Z is %d)" % (proStart+1, proEnd+1, sizeZ))
    # set up rendering engine with the pixels
    pixelsId = pixels.getId().getValue()
    re.lookupPixels(pixelsId)
    if not re.lookupRenderingDef(pixelsId):
        re.resetDefaults()
    if not re.lookupRenderingDef(pixelsId):
        raise "Failed to lookup Rendering Def"
    re.load()
    
    # if we are missing some merged colours, get them from rendering engine. 
    for index in mergedIndexes:
        if index not in mergedColours:
            color = tuple(re.getRGBA(index))
            mergedColours[index] = color
            print "Adding colour to index", color, index 
    
    # now get each channel in greyscale (or colour)
    # a list of renderedImages (data as Strings) for the split-view row
    renderedImages = []
    panelWidth = 0
    channelMismatch = False
    # first, turn off all channels in pixels
    for i in range(sizeC): 
        re.setActive(i, False)
        
    # for each channel in the splitview...
    for index in splitIndexes:
        if index >= sizeC:
            channelMismatch = True        # can't turn channel on - simply render black square! 
        else:
            re.setActive(index, True)                # turn channel on
            if colourChannels:                            # if split channels are coloured...
                if index in mergedColours:            # and this channel is in the combined image
                    rgba = tuple(mergedColours[index])
                    re.setRGBA(index, *rgba)        # set coloured 
                else:
                    re.setRGBA(index,255,255,255,255)
            else:
                re.setRGBA(index,255,255,255,255)    # if not colourChannels - channels are white
            info = (channelNames[index], re.getChannelWindowStart(index), re.getChannelWindowEnd(index))
            log("  Render channel: %s  start: %d  end: %d" % info)
            box = (roiX, roiY, roiX+roiWidth, roiY+roiHeight)
            if proStart == proEnd:
                # if it's a single plane, we can render a region (region not supported with projection)
                planeDef = omero.romio.PlaneDef()
                planeDef.z = long(proStart)
                planeDef.t = long(tIndex)
                regionDef = omero.romio.RegionDef()
                regionDef.x = roiX
                regionDef.y = roiY
                regionDef.width = roiWidth
                regionDef.height = roiHeight
                planeDef.region = regionDef
                rPlane = re.renderCompressed(planeDef)
                roiImage = Image.open(StringIO.StringIO(rPlane))
            else:
                projection = re.renderProjectedCompressed(algorithm, tIndex, stepping, proStart, proEnd)
                fullImage = Image.open(StringIO.StringIO(projection))
                roiImage = fullImage.crop(box)
                roiImage.load()        # hoping that when we zoom, don't zoom fullImage
            if roiZoom is not 1:
                newSize = (int(roiWidth*roiZoom), int(roiHeight*roiZoom))
                roiImage = roiImage.resize(newSize)
            renderedImages.append(roiImage)
            panelWidth = roiImage.size[0]
            re.setActive(index, False)                # turn the channel off again!
            
            
    # turn on channels in mergedIndexes.
    for i in mergedIndexes: 
        if i >= sizeC:
            channelMismatch = True
        else:
            re.setActive(i, True)
            if i in mergedColours:
                rgba = mergedColours[i]
                re.setRGBA(i, *rgba)
                
    # get the combined image, using the existing rendering settings 
    channelsString = ", ".join([str(i) for i in mergedIndexes])
    log("  Rendering merged channels: %s" % channelsString)
    if proStart != proEnd:
        merged = re.renderProjectedCompressed(algorithm, tIndex, stepping, proStart, proEnd)
    else:
        planeDef = omero.romio.PlaneDef()
        planeDef.z = proStart
        planeDef.t = tIndex
        merged = re.renderCompressed(planeDef)
    fullMergedImage = Image.open(StringIO.StringIO(merged))
    roiMergedImage = fullMergedImage.crop(box)
    roiMergedImage.load()    # make sure this is not just a lazy copy of the full image
    if roiZoom is not 1:
        newSize = (int(roiWidth*roiZoom), int(roiHeight*roiZoom))
        roiMergedImage = roiMergedImage.resize(newSize)
        
    if channelMismatch:
        log(" WARNING channel mismatch: The current image has fewer channels than the primary image.")
            
    # now assemble the roi split-view canvas
    font = imgUtil.getFont(fontsize)
    textHeight = font.getsize("Textq")[1]
    topSpacer = 0
    if showTopLabels: 
        if mergedNames:
            topSpacer = (textHeight * len(mergedIndexes)) + spacer
        else:
            topSpacer = textHeight + spacer
    imageCount = len(renderedImages) + 1     # extra image for merged image
    canvasWidth = ((panelWidth + spacer) * imageCount) - spacer    # no spaces around panels
    canvasHeight = renderedImages[0].size[1] + topSpacer
    size = (canvasWidth, canvasHeight)
    canvas = Image.new(mode, size, white)        # create a canvas of appropriate width, height
    
    px = 0
    textY = topSpacer - textHeight - spacer/2
    panelY = topSpacer
    # paste the split images in, with channel labels
    draw = ImageDraw.Draw(canvas)
    print "mergedColours", mergedColours
    for i, index in enumerate(splitIndexes):
        label = channelNames[index]
        indent = (panelWidth - (font.getsize(label)[0])) / 2
        # text is coloured if channel is not coloured AND in the merged image
        rgb = (0,0,0)
        if index in mergedColours:
            if not colourChannels:
                rgb = tuple(mergedColours[index])
                if rgb == (255,255,255,255):    # if white (unreadable), needs to be black! 
                    rgb = (0,0,0)
        if showTopLabels: draw.text((px+indent, textY), label, font=font, fill=rgb)
        if i < len(renderedImages):
            imgUtil.pasteImage(renderedImages[i], canvas, px, panelY)
        px = px + panelWidth + spacer
    # and the merged image
    if showTopLabels:
        #indent = (panelWidth - (font.getsize("Merged")[0])) / 2
        #draw.text((px+indent, textY), "Merged", font=font, fill=(0,0,0))
        if (mergedNames):
            for index in mergedIndexes:
                if index in mergedColours: 
                    rgb = tuple(mergedColours[index])
                    if rgb == (255,255,255,255): rgb = (0,0,0)
                else: rgb = (0,0,0) 
                if index in channelNames: name = channelNames[index]
                else: name = str(index) 
                combTextWidth = font.getsize(name)[0]
                inset = int((panelWidth - combTextWidth) / 2)
                draw.text((px + inset, textY), name, font=font, fill=rgb)
                textY = textY - textHeight  
        else:
            combTextWidth = font.getsize("Merged")[0]
            inset = int((panelWidth - combTextWidth) / 2)
            draw.text((px + inset, textY), "Merged", font=font, fill=(0,0,0))
    imgUtil.pasteImage(roiMergedImage, canvas, px, panelY)
    
    # return the roi splitview canvas, as well as the full merged image
    return (canvas, fullMergedImage, panelY)

def drawRectangle(image, roiX, roiY, roiX2, roiY2, colour, stroke=1):
    roiDraw = ImageDraw.Draw(image)
    for s in range(stroke):
        roiBox = (roiX, roiY, roiX2, roiY2)
        roiDraw.rectangle(roiBox, outline = colour)
        roiX +=1
        roiY +=1
        roiX2 -=1
        roiY2 -=1

def getRectangle(roiService, imageId, roiLabel):
    """ Returns (x, y, width, height, zMin, zMax, tMin, tMax) of the first rectange in the image that has @roiLabel as text """
    
    shapes = []        # string set. 
    
    result = roiService.findByImage(imageId, None)
    
    roiText = roiLabel.lower()
    roiCount = 0
    rectCount = 0
    foundLabelledRoi = False
    
    for roi in result.rois:
        roiCount += 1
        # go through all the shapes of the ROI
        for shape in roi.copyShapes():
            if type(shape) == omero.model.RectI:
                t = shape.getTheT().getValue()
                z = shape.getTheZ().getValue()
                x = shape.getX().getValue()
                y = shape.getY().getValue()
                tv = shape.getTextValue()
                if tv != None:  text = tv.getValue()
                else:           text = ""
                    
                # get ranges for whole ROI
                if rectCount == 0:
                    zMin = z
                    zMax = zMin
                    tMin = t
                    tMax = tMin
                    width = shape.getWidth().getValue()
                    height = shape.getHeight().getValue()
                else:
                    zMin = min(zMin, z)
                    zMax = max(zMax, z)
                    tMin = min(tMin, t)
                    tMax = max(tMax, t)
                rectCount += 1
                if text != None and text.lower() == roiText:
                    foundLabelledRoi = True
        if foundLabelledRoi:
            return (int(x), int(y), int(width), int(height), int(zMin), int(zMax), int(tMin), int(tMax))
        else:
            rectCount = 0    # try another ROI
            
    # if we got here without finding an ROI that matched, simply return any ROI we have (last one)
    if roiCount > 0:
        return (int(x), int(y), int(width), int(height), int(zMin), int(zMax), int(tMin), int(tMax))
                
                
def getVerticalLabels(labels, font, textGap):
    """ Returns an image with the labels written vertically with the given font, black on white background """
    
    maxWidth = 0
    height = 0
    textHeight = font.getsize("testq")[1]
    for label in labels:
        maxWidth = max(maxWidth, font.getsize(label)[0])
        if height > 0: height += textGap
        height += textHeight
    size = (maxWidth, height)
    textCanvas = Image.new("RGB", size, WHITE)
    textdraw = ImageDraw.Draw(textCanvas)
    py = 0
    for label in labels:
        indent = (maxWidth - font.getsize(label)[0]) / 2
        textdraw.text((indent, py), label, font=font, fill=(0,0,0))
        py += textHeight + textGap
    return textCanvas.rotate(90)
    
    
def getSplitView(session, imageIds, pixelIds, splitIndexes, channelNames, mergedNames, colourChannels, mergedIndexes, 
        mergedColours, width, height, imageLabels, spacer, algorithm, stepping, scalebar, 
        overlayColour, roiZoom, roiLabel):
    """ This method makes a figure of a number of images, arranged in rows with each row being the split-view
    of a single image. The channels are arranged left to right, with the combined image added on the right.
    The combined image is rendered according to current settings on the server, but it's channels will be
    turned on/off according to @mergedIndexes. 
    
    The figure is returned as a PIL 'Image' 
    
    @ session            session for server access
    @ pixelIds            a list of the Ids for the pixels we want to display
    @ splitIndexes         a list of the channel indexes to display. Same channels for each image/row
    @ channelNames         the Map of index:names for all channels
    @ zStart            the start of Z-range for projection
    @ zEnd                 the end of Z-range for projection
    @ colourChannels     the colour to make each column/ channel
    @ mergedIndexes      list or set of channels in the merged image 
    @ mergedColours     index: colour dictionary of channels in the merged image
    @ width            the size in pixels to show each panel
    @ height        the size in pixels to show each panel
    @ spacer        the gap between images and around the figure. Doubled between rows. 
    """
    
    roiService = session.getRoiService()
    re = session.createRenderingEngine()
    queryService = session.getQueryService()    # only needed for movie
    
    # establish dimensions and roiZoom for the primary image
    # getTheseValues from the server
    rect = getRectangle(roiService, imageIds[0], roiLabel)
    if rect == None:
        raise("No ROI found for the first image.")
    roiX, roiY, roiWidth, roiHeight, yMin, yMax, tMin, tMax = rect
    
    roiOutline = ((max(width, height)) / 200 ) + 1
    
    if roiZoom == None:
        # get the pixels for priamry image. 
        pixels = queryService.get("Pixels", pixelIds[0])
        sizeY = pixels.getSizeY().getValue()
    
        roiZoom = float(height) / float(roiHeight)
        log("ROI zoom set by primary image is %F X" % roiZoom)
    else:
        log("ROI zoom: %F X" % roiZoom)
    
    textGap = spacer/3
    fontsize = 12
    if width > 500:
        fontsize = 48
    elif width > 400:
        fontsize = 36
    elif width > 300:
        fontsize = 24
    elif width > 200:
        fontsize = 16
    font = imgUtil.getFont(fontsize)
    textHeight = font.getsize("Textq")[1]
    maxCount = 0
    for row in imageLabels:
        maxCount = max(maxCount, len(row))
    leftTextWidth = (textHeight + textGap) * maxCount + spacer
    
    maxSplitPanelWidth = 0
    totalcanvasHeight = 0
    mergedImages = []
    roiSplitPanes = []
    topSpacers = []         # space for labels above each row
    
    showLabelsAboveEveryRow = False
    invalidImages = []      # note any image row indexes that don't have ROIs. 
    
    for row, pixelsId in enumerate(pixelIds):
        log("Rendering row %d" % (row))
        
        if showLabelsAboveEveryRow:    showTopLabels = True
        else: showTopLabels = (row == 0)    # only show top labels for first row
        
        # need to get the roi dimensions from the server
        imageId = imageIds[row]
        roi = getRectangle(roiService, imageId, roiLabel)
        if roi == None:
            log("No Rectangle ROI found for this image")
            invalidImages.append(row)
            continue
        roiX, roiY, roiWidth, roiHeight, zMin, zMax, tStart, tEnd = roi
        
        pixels = queryService.get("Pixels", pixelsId)
        sizeX = pixels.getSizeX().getValue()
        sizeY = pixels.getSizeY().getValue()
        
        zStart = zMin
        zEnd = zMax
        
        # work out if any additional zoom is needed (if the full-sized image is different size from primary image)
        fullSize =  (sizeX, sizeY)
        imageZoom = imgUtil.getZoomFactor(fullSize, width, height)
        if imageZoom != 1.0:
            log("  Scaling down the full-size image by a factor of %F" % imageZoom)
        
        log("  ROI location (top-left) x: %d  y: %d  and size width: %d  height: %d" % (roiX, roiY, roiWidth, roiHeight))
        log("  ROI time: %d - %d   zRange: %d - %d" % (tStart+1, tEnd+1, zStart+1, zEnd+1))
        # get the split pane and full merged image
        roiSplitPane, fullMergedImage, topSpacer = getROIsplitView    (re, pixels, zStart, zEnd, splitIndexes, channelNames, 
            mergedNames, colourChannels, mergedIndexes, mergedColours, roiX, roiY, roiWidth, roiHeight, roiZoom, tStart, spacer, algorithm, 
            stepping, fontsize, showTopLabels)
            
        
        # and now zoom the full-sized merged image, add scalebar 
        mergedImage = imgUtil.resizeImage(fullMergedImage, width, height)
        if scalebar:
            xIndent = spacer
            yIndent = xIndent
            sbar = float(scalebar) / imageZoom            # and the scale bar will be half size
            if not addScalebar(sbar, xIndent, yIndent, mergedImage, pixels, overlayColour):
                log("  Failed to add scale bar: Pixel size not defined or scale bar is too large.")
                
        # draw ROI onto mergedImage...
        # recalculate roi if the image has been zoomed
        x = roiX / imageZoom
        y = roiY / imageZoom
        roiX2 = (roiX + roiWidth) / imageZoom
        roiY2 = (roiY + roiHeight) / imageZoom
        drawRectangle(mergedImage, x, y, roiX2, roiY2, overlayColour, roiOutline)
        
        # note the maxWidth of zoomed panels and total height for row
        maxSplitPanelWidth = max(maxSplitPanelWidth, roiSplitPane.size[0])
        totalcanvasHeight += spacer + max(height+topSpacer, roiSplitPane.size[1])
        
        mergedImages.append(mergedImage)
        roiSplitPanes.append(roiSplitPane)
        topSpacers.append(topSpacer)
    
    # remove the labels for the invalid images (without ROIs)
    invalidImages.reverse()
    for row in invalidImages:
        del imageLabels[row]
        
    # make a figure to combine all split-view rows
    # each row has 1/2 spacer above and below the panels. Need extra 1/2 spacer top and bottom
    canvasWidth = leftTextWidth + width + spacer + maxSplitPanelWidth + spacer    # 
    figureSize = (canvasWidth, totalcanvasHeight + spacer)
    figureCanvas = Image.new("RGB", figureSize, (255,255,255))
    
    rowY = spacer
    for row, image in enumerate(mergedImages):
        labelCanvas = getVerticalLabels(imageLabels[row], font, textGap)
        vOffset = (image.size[1] - labelCanvas.size[1]) / 2
        imgUtil.pasteImage(labelCanvas, figureCanvas, spacer/2, rowY+topSpacers[row]+ vOffset)
        imgUtil.pasteImage(image, figureCanvas, leftTextWidth, rowY+topSpacers[row])
        x = leftTextWidth + width + spacer
        imgUtil.pasteImage(roiSplitPanes[row], figureCanvas, x, rowY)
        rowY = rowY + max(image.size[1]+topSpacers[row], roiSplitPanes[row].size[1])+ spacer

    return figureCanvas
            

def roiFigure(session, commandArgs):    
    """
        This processes the script parameters, adding defaults if needed. 
        Then calls a method to make the figure, and finally uploads and attaches this to the primary image.
        
        @param: session        The OMERO session
        @param: commandArgs        Map of String:Object parameters for the script. 
                                Objects are not rtypes, since getValue() was called when the map was processed below. 
                                But, list and map objects may contain rtypes (need to call getValue())
        
        @return:     the id of the originalFileLink child. (ID object, not value) 
    """
    
    # create the services we're going to need. 
    metadataService = session.getMetadataService()
    queryService = session.getQueryService()
    updateService = session.getUpdateService()
    rawFileStore = session.createRawFileStore()
    containerService = session.getContainerService()
    
    log("ROI figure created by OMERO on %s" % date.today())
    log("")
    
    pixelIds = []
    imageIds = []
    imageLabels = []
    imageNames = {}
    omeroImage = None    # this is set as the first image, to link figure to

    # function for getting image labels.
    def getLabels(fullName, tagsList, pdList):
        name = fullName.split("/")[-1]
        return [name]
        
    # default function for getting labels is getName (or use datasets / tags)
    if "Image_Labels" in commandArgs:
        if commandArgs["Image_Labels"] == "Datasets":
            def getDatasets(name, tagsList, pdList):
                return [dataset for project, dataset in pdList]
            getLabels = getDatasets
        elif commandArgs["Image_Labels"] == "Tags":
            def getTags(name, tagsList, pdList):
                return tagsList
            getLabels = getTags
            
    # process the list of images. If imageIds is not set, script can't run. 
    log("Image details:")
    for idCount, imageId in enumerate(commandArgs["IDs"]):
        iId = long(imageId.getValue())
        image = containerService.getImages("Image", [iId], None)[0]
        if image == None:
            print "Image not found for ID:", iId
            continue
        imageIds.append(iId)
        if idCount == 0:
            omeroImage = image        # remember the first image to attach figure to
        pixelIds.append(image.getPrimaryPixels().getId().getValue())
        imageNames[iId] = image.getName().getValue()
    
    if len(imageIds) == 0:
        print "No image IDs specified."    
            
    pdMap = figUtil.getDatasetsProjectsFromImages(queryService, imageIds)    # a map of imageId : list of (project, dataset) names. 
    tagMap = figUtil.getTagsFromImages(metadataService, imageIds)
    # Build a legend entry for each image
    for iId in imageIds:
        name = imageNames[iId]
        imageDate = image.getAcquisitionDate().getValue()
        tagsList = tagMap[iId]
        pdList = pdMap[iId]
        
        tags = ", ".join(tagsList)
        pdString = ", ".join(["%s/%s" % pd for pd in pdList])
        log(" Image: %s  ID: %d" % (name, iId))
        log("  Date: %s" % date.fromtimestamp(imageDate/1000))
        log("  Tags: %s" % tags)
        log("  Project/Datasets: %s" % pdString)
        
        imageLabels.append(getLabels(name, tagsList, pdList))
    
    # use the first image to define dimensions, channel colours etc. 
    pixelsId = pixelIds[0]
    pixels = queryService.get("Pixels", pixelsId)

    sizeX = pixels.getSizeX().getValue();
    sizeY = pixels.getSizeY().getValue();
    sizeZ = pixels.getSizeZ().getValue();
    sizeC = pixels.getSizeC().getValue();

    
    width = sizeX
    if "Width" in commandArgs:
        w = commandArgs["Width"]
        try:
            width = int(w)
        except:
            log("Invalid width: %s Using default value: %d" % (str(w), sizeX))
    
    height = sizeY
    if "Height" in commandArgs:
        h = commandArgs["Height"]
        try:
            height = int(h)
        except:
            log("Invalid height: %s Using default value" % (str(h), sizeY))
            
    log("Image dimensions for all panels (pixels): width: %d  height: %d" % (width, height))
        
                        
    mergedIndexes = []    # the channels in the combined image, 
    mergedColours = {}    
    if "Merged_Colours" in commandArgs:
        cColourMap = commandArgs["Merged_Colours"]
        for c in cColourMap:
            rgb = cColourMap[c].getValue()
            rgba = imgUtil.RGBIntToRGBA(rgb)
            mergedColours[int(c)] = rgba
            mergedIndexes.append(int(c))
        mergedIndexes.sort()
    # make sure we have some merged channels
    if len(mergedIndexes) == 0:
        mergedIndexes = range(sizeC)
    mergedIndexes.reverse()
    
    mergedNames = False
    if "Merged_Names" in commandArgs:
        mergedNames = commandArgs["Merged_Names"]
        
    # Make channel-names map. If argument wasn't specified, name by index
    channelNames = {}
    if "Channel_Names" in commandArgs:
        cNameMap = commandArgs["Channel_Names"]
        for c in range(sizeC):
            if str(c) in cNameMap:
                channelNames[c] = cNameMap[str(c)].getValue()
            else: 
                channelNames[c] = str(c)
    else:
        for c in range(sizeC):
            channelNames[c] = str(c)
    
    # Make split-indexes list. If argument wasn't specified, include them all. 
    splitIndexes = []
    if "Split_Indexes" in commandArgs:
        for index in commandArgs["Split_Indexes"]:
            splitIndexes.append(index.getValue())
    else:
        for c in range(sizeC):
            splitIndexes = range(sizeC)
            
    colourChannels = True
    if "Split_Panels_Grey" in commandArgs and commandArgs["Split_Panels_Grey"]:
        colourChannels = False
    
    algorithm = omero.constants.projection.ProjectionType.MAXIMUMINTENSITY
    if "Algorithm" in commandArgs:
        a = commandArgs["Algorithm"]
        if (a == "Mean Intensity"):
            algorithm = omero.constants.projection.ProjectionType.MEANINTENSITY
    
    stepping = 1
    if "Stepping" in commandArgs:
        s = commandArgs["Stepping"]
        if (0 < s < sizeZ):
            stepping = s
    
    scalebar = None
    if "Scalebar" in commandArgs:
        sb = commandArgs["Scalebar"]
        try:
            scalebar = int(sb)
            if scalebar <= 0:
                scalebar = None
            else:
                log("Scalebar is %d microns" % scalebar)
        except:
            log("Invalid value for scalebar: %s" % str(sb))
            scalebar = None
    
    overlayColour = (255,255,255)
    if "Overlay_Colour" in commandArgs:
        r,g,b,a = OVERLAY_COLOURS[commandArgs["Overlay_Colour"]]
        overlayColour = (r,g,b)
    
    roiZoom = None
    if "ROI_Zoom" in commandArgs:
        roiZoom = float(commandArgs["ROI_Zoom"])
        if roiZoom == 0:
            roiZoom = None
    
    roiLabel = "FigureROI"
    if "ROI_Label" in commandArgs:
        roiLabel = commandArgs["ROI_Label"]
        
    spacer = (width/50) + 2
    
    fig = getSplitView(session, imageIds, pixelIds, splitIndexes, channelNames, mergedNames, colourChannels, mergedIndexes, 
            mergedColours, width, height, imageLabels, spacer, algorithm, stepping, scalebar, overlayColour, roiZoom, roiLabel)
    
    if fig == None:        # e.g. No ROIs found
        return                                                
    #fig.show()        # bug-fixing only
    
    log("")
    figLegend = "\n".join(logStrings)
    
    #print figLegend    # bug fixing only
    
    format = JPEG
    if "Format" in commandArgs:
        if commandArgs["Format"] == "PNG":
            format = PNG
            
    output = "roiFigure"
    if "Figure_Name" in commandArgs:
        output = str(commandArgs["Figure_Name"])
        
    if format == PNG:
        output = output + ".png"
        fig.save(output, "PNG")
    else:
        output = output + ".jpg"
        fig.save(output)
    
    # Use util method to upload the figure 'output' to the server, attaching it to the omeroImage, adding the 
    # figLegend as the fileAnnotation description. 
    # Returns the id of the originalFileLink child. (ID object, not value)
    fileAnnotation = scriptUtil.uploadAndAttachFile(queryService, updateService, rawFileStore, omeroImage, output, format, figLegend)
    return fileAnnotation

def runAsScript():
    """
    The main entry point of the script, as called by the client via the scripting service, passing the required parameters. 
    """
     
    dataTypes = [rstring('Image')]
    labels = [rstring('Image Name'), rstring('Datasets'), rstring('Tags')]
    algorithums = [rstring('Maximum Intensity'),rstring('Mean Intensity')]
    roiLabel = """Specify an ROI to pick by specifying it's shape label. 'FigureROI' by default,
              (not case sensitive). If matching ROI not found, use any ROI."""
    formats = [rstring('JPEG'),rstring('PNG')]
    ckeys = COLOURS.keys()
    ckeys.sort()
    cOptions = wrap(ckeys)
    oColours = wrap(OVERLAY_COLOURS.keys())
    
    client = scripts.client('ROI_Split_Figure.py', """Create a figure of an ROI region as separate zoomed split-channel panels.
NB: OMERO.insight client provides a nicer UI for this script under 'Publishing Options'
See https://www.openmicroscopy.org/site/support/omero4/getting-started/tutorial/exporting-figures""",

    # provide 'Data_Type' and 'IDs' parameters so that Insight auto-populates with currently selected images.
    scripts.String("Data_Type", optional=False, grouping="01",
        description="The data you want to work with.", values=dataTypes, default="Image"),

    scripts.List("IDs", optional=False, grouping="02",
        description="List of Dataset IDs or Image IDs").ofType(rlong(0)),

    scripts.Map("Channel_Names", grouping="03", description="Map of index: channel name for All channels"),
    scripts.Bool("Merged_Names", grouping="04", description="If true, label the merged panel with channel names. Otherwise label with 'Merged'"),
    scripts.List("Split_Indexes", grouping="05", description="List of the channels in the split view panels"),
    scripts.Bool("Split_Panels_Grey", grouping="06", description="If true, all split panels are greyscale"),
    scripts.Map("Merged_Colours", grouping="07", description="Map of index:int colours for each merged channel. Otherwise use existing colour settings"),
    scripts.Int("Width", grouping="08", description="Max width of each image panel", min=1),   
    scripts.Int("Height", grouping="09", description="The max height of each image panel", min=1),
    scripts.String("Image_Labels", grouping="10", description="Label images with the Image's Name or it's Datasets or Tags", values=labels),               
    scripts.String("Algorithm", grouping="11", description="Algorithum for projection.", values=algorithums),
    scripts.Int("Stepping", grouping="12", description="The Z-plane increment for projection. Default is 1", min=1),
    scripts.Int("Scalebar", grouping="13", description="Scale bar size in microns. Only shown if image has pixel-size info.", min=1),
    scripts.String("Format", grouping="14", description="Format to save image. E.g 'PNG'.", values=formats, default='JPEG'),
    scripts.String("Figure_Name", grouping="15", description="File name of the figure to save."),
    scripts.String("Overlay_Colour", grouping="16", description="The colour of the scalebar.",default='White',values=oColours),
    scripts.Float("ROI_Zoom", grouping="17", description="How much to zoom the ROI. E.g. x 2. If 0 then zoom roi panel to fit", min=0),
    scripts.String("ROI_Label", grouping="18", description=roiLabel),
    
    version = "4.3.0",
    authors = ["William Moore", "OME Team"],
    institutions = ["University of Dundee"],
    contact = "ome-users@lists.openmicroscopy.org.uk",
    )
    try:
        session = client.getSession()
        commandArgs = {}
    
        # process the list of args above. 
        for key in client.getInputKeys():
            if client.getInput(key):
                commandArgs[key] = client.getInput(key).getValue()
    
        print commandArgs
        # call the main script, attaching resulting figure to Image. Returns the id of the originalFileLink child. (ID object, not value)
        fileAnnotation = roiFigure(session, commandArgs)
        # return this fileAnnotation to the client. 
        if fileAnnotation:
            client.setOutput("Message", rstring("ROI Split Figure Created"))
            client.setOutput("File_Annotation", robject(fileAnnotation))
    finally:
        client.closeSession()

if __name__ == "__main__":
    runAsScript()
