"""
 components/tools/OmeroPy/scripts/Movie_ROI_Figure.py

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
from omero.model import ImageI
from omero.rtypes import *      # includes wrap()
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

COLOURS = scriptUtil.COLOURS
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


def getTimeIndexes(timePoints, maxFrames):
    """ 
    If we want to display a number of timepoints (e.g. 11), without exceeding maxFrames (e.g. 5), 
    need to pick a selection of t-indexes e.g. 0, 2, 4, 7, 10
    This method returns the list of indexes. NB - Not used at present - but might be needed. """
    frames = min(maxFrames, timePoints)
    intervalCount = frames-1
    smallestInterval = (timePoints-1)/intervalCount
    # make a list of intervals, making the last intervals bigger if needed
    intervals = [smallestInterval] * intervalCount
    extra = (timePoints-1) % intervalCount
    for e in range(extra):
        lastIndex = -(e+1)
        intervals[lastIndex] += 1
    # convert the list of intervals into indexes. 
    indexes = []
    time = 0
    indexes.append(time)
    for i in range(frames-1):
        time += intervals[i]
        indexes.append(time)
    return indexes
        
    
def getROImovieView    (re, queryService, pixels, timeShapeMap, mergedIndexes, mergedColours, roiWidth, 
        roiHeight, roiZoom, spacer = 12, algorithm=None, stepping = 1, fontsize=24, maxColumns=None, showRoiDuration=False):

    """ This takes a ROI rectangle from an image and makes a movie canvas of the region in the ROI, zoomed 
        by a defined factor. 
    """
    
    mode = "RGB"
    white = (255, 255, 255)    
    
    sizeX = pixels.getSizeX().getValue()
    sizeY = pixels.getSizeY().getValue()
    sizeZ = pixels.getSizeZ().getValue()
    sizeC = pixels.getSizeC().getValue()
    sizeT = pixels.getSizeT().getValue()
    
    if pixels.getPhysicalSizeX():
        physicalX = pixels.getPhysicalSizeX().getValue()
    else:
        physicalX = 0 
    if pixels.getPhysicalSizeY():
        physicalY = pixels.getPhysicalSizeY().getValue()
    else:
        physicalY = 0
    log("  Pixel size (um): x: %s  y: %s" % (str(physicalX), str(physicalY)))
    log("  Image dimensions (pixels): x: %d  y: %d" % (sizeX, sizeY))
    
    log(" Projecting Movie Frame ROIs...")

    # set up rendering engine with the pixels
    pixelsId = pixels.getId().getValue()
    re.lookupPixels(pixelsId)
    if not re.lookupRenderingDef(pixelsId):
        re.resetDefaults()
    if not re.lookupRenderingDef(pixelsId):
        raise "Failed to lookup Rendering Def"
    re.load()
    
    # now get each channel in greyscale (or colour)
    # a list of renderedImages (data as Strings) for the split-view row
    renderedImages = []
    panelWidth = 0
    channelMismatch = False
    # first, turn off all channels in pixels
    for i in range(sizeC): 
        re.setActive(i, False)        
            
    # turn on channels in mergedIndexes. 
    for i in mergedIndexes: 
        if i >= sizeC or i < 0:
            channelMismatch = True
        else:
            print "Turning on channel:", i
            re.setActive(i, True)
            if i in mergedColours:
                rgba = mergedColours[i]
                print "Setting rgba", rgba
                re.setRGBA(i, *rgba)
                
    # get the combined image, using the existing rendering settings 
    channelsString = ", ".join([str(i) for i in mergedIndexes])
    log("  Rendering Movie channels: %s" % channelsString)

    timeIndexes = list(timeShapeMap.keys())
    timeIndexes.sort()
    
    if showRoiDuration:
        log(" Timepoints shown are ROI duration, not from start of movie")
    timeLabels = figUtil.getTimeLabels(queryService, pixelsId, timeIndexes, sizeT, None, showRoiDuration)
    # The last value of the list will be the Units used to display time
    print "Time label units are:", timeLabels[-1]
    
    fullFirstFrame = None
    for t, timepoint in enumerate(timeIndexes):
        roiX, roiY, proStart, proEnd = timeShapeMap[timepoint]
        box = (roiX, roiY, int(roiX+roiWidth), int(roiY+roiHeight))
        log("  Time-index: %d Time-label: %s  Projecting z range: %d - %d (max Z is %d) of region x: %s y: %s" % (timepoint+1, timeLabels[t], proStart+1, proEnd+1, sizeZ, roiX, roiY))
        
        merged = re.renderProjectedCompressed(algorithm, timepoint, stepping, proStart, proEnd)
        fullMergedImage = Image.open(StringIO.StringIO(merged))
        if fullFirstFrame == None:
            fullFirstFrame = fullMergedImage
        roiMergedImage = fullMergedImage.crop(box)
        roiMergedImage.load()    # make sure this is not just a lazy copy of the full image
        if roiZoom is not 1:
            newSize = (int(roiWidth*roiZoom), int(roiHeight*roiZoom))
            roiMergedImage = roiMergedImage.resize(newSize)
        panelWidth = roiMergedImage.size[0]
        renderedImages.append(roiMergedImage)
        
    if channelMismatch:
        log(" WARNING channel mismatch: The current image has fewer channels than the primary image.")

    # now assemble the roi split-view canvas, with space above for text
    colCount = len(renderedImages)
    rowCount = 1
    if maxColumns:
        rowCount = colCount / maxColumns
        if (colCount % maxColumns) > 0: 
            rowCount += 1
        colCount = maxColumns
    font = imgUtil.getFont(fontsize)
    textHeight = font.getsize("Textq")[1]
    canvasWidth = ((panelWidth + spacer) * colCount) - spacer    # no spaces around panels
    rowHeight = renderedImages[0].size[1] + spacer + textHeight
    canvasHeight = rowHeight * rowCount
    size = (canvasWidth, canvasHeight)
    canvas = Image.new(mode, size, white)        # create a canvas of appropriate width, height
    
    
    px = 0
    textY = spacer/2
    panelY = textHeight + spacer
    # paste the images in, with time labels
    draw = ImageDraw.Draw(canvas)
    
    col = 0
    for i, img in enumerate(renderedImages):
        label = timeLabels[i]
        indent = (panelWidth - (font.getsize(label)[0])) / 2
        draw.text((px+indent, textY), label, font=font, fill=(0,0,0))
        imgUtil.pasteImage(img, canvas, px, panelY)
        if col == (colCount - 1):
            col = 0
            px = 0
            textY += rowHeight
            panelY += rowHeight
        else:
            col += 1
            px = px + panelWidth + spacer
    
    # return the roi splitview canvas, as well as the full merged image
    return (canvas, fullFirstFrame, textHeight + spacer)
    

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
    """ 
    Returns (x, y, width, height, timeShapeMap) of the all rectanges in the first ROI of the image where 
    timeShapeMap is a map of tIndex: (x,y,zMin,zMax) 
    x, y, Width and Height are from the first rectangle (assumed that all are same size!)
    """
    
    shapes = []        # string set. 
    
    result = roiService.findByImage(imageId, None)
    
    roiText = roiLabel.lower()
    roiCount = 0
    rectCount = 0
    foundLabelledRoi = False
    
    for roi in result.rois:
        timeShapeMap = {} # map of tIndex: (x,y,zMin,zMax) for a single roi
        for shape in roi.copyShapes():
            if type(shape) == omero.model.RectI:
                t = shape.getTheT().getValue()
                z = shape.getTheZ().getValue()
                x = int(shape.getX().getValue())
                y = int(shape.getY().getValue())
                text = shape.getTextValue() and shape.getTextValue().getValue() or None
                
                # build a map of tIndex: (x,y,zMin,zMax)
                if t in timeShapeMap:
                    xx, yy, minZ, maxZ = timeShapeMap[t]
                    tzMin = min(minZ, z)
                    tzMax = max(maxZ, z)
                    timeShapeMap[t] = (x,y,tzMin,tzMax)
                else:
                    timeShapeMap[t] = (x,y,z,z)
                    
                # get ranges for whole ROI
                if rectCount == 0:
                    width = shape.getWidth().getValue()
                    height = shape.getHeight().getValue()
                    x1 = x
                    y1 = y
                rectCount += 1
                if text != None and text.lower() == roiText:
                    foundLabelledRoi = True
        # will return after the first ROI that matches text
        if foundLabelledRoi:
            return (int(x1), int(y1), int(width), int(height), timeShapeMap)
        else:
            if rectCount > 0:
                roiCount += 1
            rectCount = 0    # try another ROI
    
    # if we got here without finding an ROI that matched, simply return any ROI we have (last one)
    if roiCount > 0:
        return (int(x1), int(y1), int(width), int(height), timeShapeMap)
    
    
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
    
    
def getSplitView(session, imageIds, pixelIds, mergedIndexes, 
        mergedColours, width, height, imageLabels, spacer, algorithm, stepping, scalebar, 
        overlayColour, roiZoom, maxColumns, showRoiDuration, roiLabel):
    """ This method makes a figure of a number of images, arranged in rows with each row being the split-view
    of a single image. The channels are arranged left to right, with the combined image added on the right.
    The combined image is rendered according to current settings on the server, but it's channels will be
    turned on/off according to @mergedIndexes. 
    
    The figure is returned as a PIL 'Image' 
    
    @ session            session for server access
    @ pixelIds            a list of the Ids for the pixels we want to display
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
    for iid in imageIds:
        rect = getRectangle(roiService, iid, roiLabel)
        if rect is not None: break

    if rect is None:
        log("Found no images with rectangle ROIs")
        return
    x, y, roiWidth, roiHeight, timeShapeMap = rect
    
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
    
    for row, pixelsId in enumerate(pixelIds):
        log("Rendering row %d" % (row))
        
        if showLabelsAboveEveryRow:    showTopLabels = True
        else: showTopLabels = (row == 0)    # only show top labels for first row
        
        # need to get the roi dimensions from the server
        imageId = imageIds[row]
        roi = getRectangle(roiService, imageId, roiLabel)
        if roi == None:
            log("No Rectangle ROI found for this image")
            del imageLabels[row]    # remove the corresponding labels
            continue
        roiX, roiY, roiWidth, roiHeight, timeShapeMap = roi
        
        pixels = queryService.get("Pixels", pixelsId)
        sizeX = pixels.getSizeX().getValue()
        sizeY = pixels.getSizeY().getValue()
        
        
        # work out if any additional zoom is needed (if the full-sized image is different size from primary image)
        fullSize =  (sizeX, sizeY)
        imageZoom = imgUtil.getZoomFactor(fullSize, width, height)
        if imageZoom != 1.0:
            log("  Scaling down the full-size image by a factor of %F" % imageZoom)
        
        log("  ROI location (top-left of first frame) x: %d  y: %d  and size width: %d  height: %d" % (roiX, roiY, roiWidth, roiHeight))
        # get the split pane and full merged image
        roiSplitPane, fullMergedImage, topSpacer = getROImovieView    (re, queryService, pixels, timeShapeMap, mergedIndexes, mergedColours,
                 roiWidth, roiHeight, roiZoom, spacer, algorithm, stepping, fontsize, maxColumns, showRoiDuration)
                
            
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
    dataType = commandArgs["Data_Type"]
    ids = commandArgs["IDs"]
    images = containerService.getImages(dataType, ids, None)

    for idCount, image in enumerate(images):
        iId = image.getId().getValue()
        imageIds.append(iId)
        if idCount == 0:
            omeroImage = image        # remember the first image to attach figure to
        pixelIds.append(image.getPrimaryPixels().getId().getValue())
        imageNames[iId] = image.getName().getValue()

    if len(imageIds) == 0:
        print "No image IDs specified."
        return
            
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
        
    # the channels in the combined image,
    if "Merged_Channels" in commandArgs:
        mergedIndexes = [c-1 for c in commandArgs["Merged_Channels"]]  # convert to 0-based
    else:
        mergedIndexes = range(sizeC) # show all
    mergedIndexes.reverse()
        
    mergedColours = {}    # if no colours added, use existing rendering settings.
    # Actually, nicer to always use existing rendering settings.
    #if "Merged_Colours" in commandArgs:
    #    for i, c in enumerate(commandArgs["Merged_Colours"]):
    #        if c in COLOURS:
    #            mergedColours[i] = COLOURS[c]
    
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
    if "Scalebar_Colour" in commandArgs:
        if commandArgs["Scalebar_Colour"] in OVERLAY_COLOURS: 
            r,g,b,a = OVERLAY_COLOURS[commandArgs["Scalebar_Colour"]]
            overlayColour = (r,g,b)
    
    roiZoom = None
    if "Roi_Zoom" in commandArgs:
        roiZoom = float(commandArgs["Roi_Zoom"])
        if roiZoom == 0:
            roiZoom = None
            
    maxColumns = None
    if "Max_Columns" in commandArgs:
        maxColumns = commandArgs["Max_Columns"]
        
    showRoiDuration = False
    if "Show_ROI_Duration" in commandArgs:
        showRoiDuration = commandArgs["Show_ROI_Duration"]
    
    roiLabel = "FigureROI"
    if "Roi_Selection_Label" in commandArgs:
        roiLabel = commandArgs["Roi_Selection_Label"]
                
    spacer = (width/50) + 2
    
    print "showRoiDuration", showRoiDuration
    fig = getSplitView(session, imageIds, pixelIds, mergedIndexes, 
            mergedColours, width, height, imageLabels, spacer, algorithm, stepping, scalebar, overlayColour, roiZoom, 
            maxColumns, showRoiDuration, roiLabel)
                                                    
    #fig.show()        # bug-fixing only

    if fig is None:
        log("\nNo Figure produced - probably due to NO rectangle ROIs being found for the specified images")
        return
    figLegend = "\n".join(logStrings)
    
    #print figLegend    # bug fixing only
    
    format = JPEG
    if "Format" in commandArgs:
        if commandArgs["Format"] == "PNG":
            format = PNG
            
    output = "movieROIFigure"
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
    return (fileAnnotation, omeroImage)

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
    
    client = scripts.client('Movie_ROI_Figure.py', """Create a figure of movie frames from ROI region of image.
See http://www.openmicroscopy.org/site/support/omero4/getting-started/tutorial/exporting-figures""",

    scripts.String("Data_Type", optional=False, grouping="01",
        description="The data you want to work with.", values=dataTypes, default="Image"),

    scripts.List("IDs", optional=False, grouping="02",
        description="List of Image IDs").ofType(rlong(0)),
    
    #scripts.List("Merged_Colours", grouping="03",
    #    description="A list of colours to apply to merged channels.", values=cOptions),
         
    scripts.List("Merged_Channels", grouping="03",
        description="A list of channel indexes to display, starting at 1. E.g. 1, 2, 3").ofType(rint(0)),
        
    scripts.Float("Roi_Zoom", grouping="04", default=1,
        description="How much to zoom the ROI. E.g. x 2. If 0 then ROI panel will zoom to same size as main image"),
    
    scripts.Int("Max_Columns", grouping="04.1", default=10,
        description="The maximum number of columns in the figure, for ROI-movie frames.", min=1),
    
    scripts.Bool("Resize_Images", grouping="05", default=True,
        description="Images are shown full-size by default, but can be resized below"),
        
    scripts.Int("Width", grouping="05.1",
        description="Max width of each image panel in pixels", min=1), 
          
    scripts.Int("Height", grouping="05.2",
        description="The max height of each image panel in pixels", min=1),
             
    scripts.String("Image_Labels", grouping="06",
        description="Label images with the Image Name or Datasets or Tags", values=labels), 
    
    scripts.Bool("Show_ROI_Duration", grouping="06.1",
        description="If true, times shown as duration from first timepoint of the ROI, otherwise use movie timestamp."),
        
    scripts.Int("Scalebar", grouping="07",
        description="Scale bar size in microns. Only shown if image has pixel-size info.", min=1),
        
    scripts.String("Scalebar_Colour", grouping="07.1",
        description="The colour of the scalebar and ROI outline.",default='White',values=oColours),
    
    scripts.String("Roi_Selection_Label", grouping="08",
        description=roiLabel),
        
    scripts.String("Algorithm", grouping="09",
        description="Algorithum for projection, if ROI spans several Z sections.", values=algorithums),

    scripts.String("Figure_Name", grouping="10",
        description="File name of the figure to save.", default='movieROIFigure'),

    scripts.String("Format", grouping="10.1",
        description="Format to save figure.", values=formats, default='JPEG'),
    
    version = "4.3.0",
    authors = ["William Moore", "OME Team"],
    institutions = ["University of Dundee"],
    contact = "ome-users@lists.openmicroscopy.org.uk",
    )
    
    try:
        session = client.getSession();
        commandArgs = {}

        # process the list of args above. 
        for key in client.getInputKeys():
            if client.getInput(key):
                commandArgs[key] = unwrap(client.getInput(key))

        print commandArgs
        # call the main script, attaching resulting figure to Image. Returns the id of the originalFileLink child. (ID object, not value)
        result = roiFigure(session, commandArgs)
        if result is not None:
            fileAnnotation, image = result
            # return this fileAnnotation to the client. 
            if fileAnnotation:
                client.setOutput("Message", rstring("ROI Movie Figure Attached to Image: %s" % image.name.val))
                client.setOutput("File_Annotation", robject(fileAnnotation))
        else:
            client.setOutput("Message", rstring("No Figure Produced. See Error or Info for details"))

    finally: client.closeSession()

if __name__ == "__main__":
    runAsScript()
