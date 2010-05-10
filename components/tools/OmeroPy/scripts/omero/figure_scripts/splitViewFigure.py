"""
 components/tools/OmeroPy/scripts/splitViewFigure.py 

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

This script takes a number of images an makes a split view figure, one
image per row, displayed as a split view with merged image. 
    
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
import omero.util.figureUtil as figUtil
import omero.util.imageUtil as imgUtil
import omero.util.script_utils as scriptUtil
import omero
from omero.rtypes import *
import omero.gateway
import omero_api_Gateway_ice    # see http://tinyurl.com/icebuserror
#import omero.util.figureUtil as figUtil    # need to comment out for upload to work. But need import for script to work!!
import getopt, sys, os, subprocess
import Image, ImageDraw, ImageFont
import StringIO
from omero_sys_ParametersI import ParametersI
from datetime import date


JPEG = "image/jpeg"
PNG = "image/png"
formatExtensionMap = {JPEG:"jpg", PNG:"png"}

# keep track of log strings. 
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
    

def getSplitView(session, pixelIds, zStart, zEnd, splitIndexes, channelNames, colourChannels, mergedIndexes, 
        mergedColours, width=None, height=None, spacer = 12, algorithm = None, stepping = 1, scalebar = None, overlayColour=(255,255,255)):
    """ This method makes a figure of a number of images, arranged in rows with each row being the split-view
    of a single image. The channels are arranged left to right, with the combined image added on the right.
    The combined image is rendered according to current settings on the server, but it's channels will be
    turned on/off according to @mergedIndexes. 
    No text labels are added to the image at this stage. 
    
    The figure is returned as a PIL 'Image' 
    
    @ session    session for server access
    @ pixelIds        a list of the Ids for the pixels we want to display
    @ zStart        the start of Z-range for projection
    @ zEnd             the end of Z-range for projection
    @ splitIndexes     a list of the channel indexes to display. Same channels for each image/row
    @ channelNames         the Map of index:names to go above the columns for each split channel
    @ colourChannels     the colour to make each column/ channel
    @ mergedIndexes      list or set of channels in the merged image 
    @ mergedColours     index: colour dictionary of channels in the merged image
    @ width            the size in pixels to show each panel
    @ height        the size in pixels to show each panel
    @ spacer        the gap between images and around the figure. Doubled between rows. 
    """
    
    if algorithm is None:    # omero::constants::projection::ProjectionType
        algorithm = omero.constants.projection.ProjectionType.MAXIMUMINTENSITY
    timepoint = 0
    mode = "RGB"
    white = (255, 255, 255)
    
    # create a rendering engine
    re = session.createRenderingEngine()
    gateway = session.createGateway()
    
    rowPanels = []
    totalHeight = 0
    totalWidth = 0
    maxImageWidth = 0
    
    physicalSizeX = 0 
    
    log("Split View Rendering Log...")
    
    if zStart >-1 and zEnd >-1:
        alString = str(algorithm).replace("INTENSITY", " Intensity").capitalize()
        log("All images projected using '%s' projection with step size: %d  start: %d  end: %d" 
            % (alString, stepping, zStart+1, zEnd+1))
    else:
        log("Images show last-viewed Z-section")
    
    for row, pixelsId in enumerate(pixelIds):
        log("Rendering row %d" % (row+1))
        
        pixels = gateway.getPixels(pixelsId)
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
        if row == 0:    # set values for primary image
            physicalSizeX = physicalX
            physicalSizeY = physicalY
        else:            # compare primary image with current one
            if physicalSizeX != physicalX or physicalSizeY != physicalY:
                log(" WARNING: Images have different pixel lengths. Scales are not comparable.")
        
        log("  Image dimensions (pixels): x: %d  y: %d" % (sizeX, sizeY))
        maxImageWidth = max(maxImageWidth, sizeX)
        
        # set up rendering engine with the pixels
        re.lookupPixels(pixelsId)
        re.lookupRenderingDef(pixelsId)
        re.load()
        
        proStart = zStart
        proEnd = zEnd
        # make sure we're within Z range for projection. 
        if proEnd >= sizeZ:
            proEnd = sizeZ - 1
            if proStart > sizeZ:
                proStart = 0
            log(" WARNING: Current image has fewer Z-sections than the primary image.")
            
        # if we have an invalid z-range (start or end less than 0), show default Z only
        if proStart < 0 or proEnd < 0:
            proStart = re.getDefaultZ()
            proEnd = proStart
            log("  Display Z-section: %d" % (proEnd+1))
        else:
            log("  Projecting z range: %d - %d   (max Z is %d)" % (proStart+1, proEnd+1, sizeZ))
        
        # now get each channel in greyscale (or colour)
        # a list of renderedImages (data as Strings) for the split-view row
        renderedImages = []
        i = 0
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
                    if index in mergedIndexes:            # and this channel is in the combined image
                        rgba = tuple(mergedColours[index])
                        re.setRGBA(index, *rgba)        # set coloured 
                    else:
                        re.setRGBA(index,255,255,255,255)    # otherwise set white (max alpha)
                else:
                    re.setRGBA(index,255,255,255,255)    # if not colourChannels - channels are white
                info = (channelNames[index], re.getChannelWindowStart(index), re.getChannelWindowEnd(index))
                log("  Render channel: %s  start: %d  end: %d" % info)
            projection = re.renderProjectedCompressed(algorithm, timepoint, stepping, proStart, proEnd)
            renderedImages.append(projection)
            if index < sizeC:
                re.setActive(index, False)                # turn the channel off again!
    

        # turn on channels in mergedIndexes. 
        for i in mergedIndexes: 
            if i >= sizeC:
                channelMismatch = True
            else:
                rgba = mergedColours[i]
                re.setActive(i, True)
                re.setRGBA(i, *rgba)
                
        # get the combined image, using the existing rendering settings 
        channelsString = ", ".join([channelNames[i] for i in mergedIndexes])
        log("  Rendering merged channels: %s" % channelsString)
        overlay = re.renderProjectedCompressed(algorithm, timepoint, stepping, proStart, proEnd)
        
        if channelMismatch:
            log(" WARNING channel mismatch: The current image has fewer channels than the primary image.")
        
        
        # make a canvas for the row of splitview images...
        imageCount = len(renderedImages) + 1     # extra image for combined image
        canvasWidth = ((width + spacer) * imageCount) + spacer
        canvasHeight = spacer + height
        size = (canvasWidth, canvasHeight)
        canvas = Image.new(mode, size, white)        # create a canvas of appropriate width, height
    
        px = spacer
        py = spacer/2
        col = 0
        # paste the images in
        for img in renderedImages:
            im = Image.open(StringIO.StringIO(img))
            i = imgUtil.resizeImage(im, width, height)
            imgUtil.pasteImage(i, canvas, px, py)
            px = px + width + spacer
            col = col + 1
    
        # add combined image, after resizing and adding scale bar 
        i = Image.open(StringIO.StringIO(overlay))
        scaledImage = imgUtil.resizeImage(i, width, height)
        if scalebar:
            xIndent = spacer
            yIndent = xIndent
            zoom = imgUtil.getZoomFactor(i.size, width, height)     # if we've scaled to half size, zoom = 2
            sbar = float(scalebar) / zoom            # and the scale bar will be half size
            if not addScalebar(sbar, xIndent, yIndent, scaledImage, pixels, overlayColour):
                log("  Failed to add scale bar: Pixel size not defined or scale bar is too large.")
        imgUtil.pasteImage(scaledImage, canvas, px, py)
    
        totalWidth = max(totalWidth, canvasWidth)    # most should be same width anyway
        totalHeight = totalHeight + canvasHeight    # add together the heights of each row
        rowPanels.append(canvas)
        
    # make a figure to combine all split-view rows
    # each row has 1/2 spacer above and below the panels. Need extra 1/2 spacer top and bottom
    figureSize = (totalWidth, totalHeight+spacer)
    figureCanvas = Image.new(mode, figureSize, white)
    
    rowY = spacer/2
    for row in rowPanels:
        imgUtil.pasteImage(row, figureCanvas, 0, rowY)
        rowY = rowY + row.size[1]

    return figureCanvas
    


def makeSplitViewFigure(session, pixelIds, zStart, zEnd, splitIndexes, channelNames, colourChannels, 
                mergedIndexes, mergedColours, mergedNames, width, height, imageLabels = None, algorithm = None, stepping = 1, 
                scalebar=None, overlayColour=(255,255,255)):

    """ This method makes a figure of a number of images, arranged in rows with each row being the split-view
    of a single image. The channels are arranged left to right, with the combined image added on the right.
    The combined image is rendered according to current settings on the server, but it's channels will be
    turned on/off according to @mergedIndexes. 
    The colour of each channel turned white if colourChannels is false or the channel is not in the merged image.
    Otherwise channel is changed to mergedColours[i]
    Text is added at the top of the figure, to display channel names above each column, and the 
    combined image may have it's various channels named in coloured text. The optional imageLabels is a list 
    of string lists for naming the images at the left of the figure (Each image may have 0 or multiple labels).
    
    The figure is returned as a PIL 'Image' 
    
    @ session    session for server access
    @ pixelIds        a list of the Ids for the pixels we want to display
    @ zStart        the start of Z-range for projection
    @ zEnd             the end of Z-range for projection
    @ splitIndexes     a list of the channel indexes to display. Same channels for each image/row
    @ channelNames         map of index:name to go above the columns for each split channel
    @ colourChannels     true if split channels are 
    @ mergedIndexes        list (or set) of channels in the merged image 
    @ mergedColours     index: colour map of channels in the merged image
    @ mergedNames        if true, label with merged panel with channel names (otherwise, label "Merged")
    @ width             the width of primary image (all images zoomed to this height)
    @ height            the height of primary image
    @ imageLabels         optional list of string lists.
    @ algorithm            for projection MAXIMUMINTENSITY or MEANINTENSITY
    @ stepping            projection increment 
    """
    
    fontsize = 12
    if width > 500:
        fontsize = 48
    elif width > 400:
        fontsize = 36
    elif width > 300:
        fontsize = 24
    elif width > 200:
        fontsize = 16
        
    spacer = (width/25) + 2
    textGap = 3        # gap between text and image panels
    leftTextWidth = 0
    textHeight = 0
    

    # get the rendered splitview, with images surrounded on all sides by spacer
    sv = getSplitView(session, pixelIds, zStart, zEnd, splitIndexes, channelNames, colourChannels, 
            mergedIndexes, mergedColours, width, height, spacer, algorithm, stepping, scalebar, overlayColour)
    
    font = imgUtil.getFont(fontsize)
    mode = "RGB"
    white = (255, 255, 255)
    textHeight = font.getsize("Textq")[1]
    topSpacer = spacer + textHeight + textGap
    #textCanvas = Image.new(mode, (1,1), white)
    #textdraw = ImageDraw.Draw(textCanvas)
    #h = textdraw.textsize("Textq", font=font) [1]
    
    # if adding text to the left, write the text on horizontal canvas, then rotate to vertical (below)
    if imageLabels:
        # find max number of labels
        maxCount = 0 
        rowHeights = []
        for row in imageLabels:
            maxCount = max(maxCount, len(row))
        leftTextWidth = (textHeight + textGap) * maxCount
        size = (sv.size[1], leftTextWidth)    # make the canvas as wide as the panels height
        textCanvas = Image.new(mode, size, white)
        textdraw = ImageDraw.Draw(textCanvas)
        px = spacer
        imageLabels.reverse()
        for row in imageLabels:
            py = leftTextWidth - textGap # start at bottom
            for l, label in enumerate(row):
                py = py - textHeight    # find the top of this row
                w = textdraw.textsize(label, font=font) [0]
                inset = int((height - w) / 2)
                textdraw.text((px+inset, py), label, font=font, fill=(0,0,0))
                py = py - textGap    # add space between rows
            px = px + spacer + height         # spacer between each row
        
    
    topTextHeight = textHeight + textGap
    if (mergedNames):
        topTextHeight = ((textHeight) * len(mergedIndexes))
    # make a canvas big-enough to add text to the images. 
    canvasWidth = leftTextWidth + sv.size[0]
    canvasHeight = topTextHeight + sv.size[1]
    size = (canvasWidth, canvasHeight)
    canvas = Image.new(mode, size, white)        # create a canvas of appropriate width, height
    
    # add the split-view panel
    pasteX = leftTextWidth
    pasteY = topTextHeight
    imgUtil.pasteImage(sv, canvas, pasteX, pasteY)
    
    draw = ImageDraw.Draw(canvas)
    
    # add text to rows
    # want it to be vertical. Rotate and paste the text canvas from above
    if imageLabels:    
        textV = textCanvas.rotate(90)
        imgUtil.pasteImage(textV, canvas, spacer, topTextHeight)
    
    # add text to columns 
    px = spacer + leftTextWidth
    py = topTextHeight + spacer - (textHeight + textGap)    # edges of panels - rowHeight
    for index in splitIndexes:
        # calculate the position of the text, centered above the image
        w = font.getsize(channelNames[index]) [0]
        inset = int((width - w) / 2)
        # text is coloured if channel is grey AND in the merged image
        rgb = (0,0,0)
        if index in mergedIndexes:
            if not colourChannels:
                rgb = tuple(mergedColours[index])
                if rgb == (255,255,255):    # if white (unreadable), needs to be black! 
                    rgb = (0,0,0)
        draw.text((px+inset, py), channelNames[index], font=font, fill=rgb)
        px = px + width + spacer
    
    # add text for combined image
    if (mergedNames):
        mergedIndexes.reverse()
        for index in mergedIndexes:
            print index, channelNames[index]
            rgb = tuple(mergedColours[index])
            name = channelNames[index]
            combTextWidth = font.getsize(name)[0]
            inset = int((width - combTextWidth) / 2)
            draw.text((px + inset, py), name, font=font, fill=rgb)
            py = py - textHeight  
    else:
        combTextWidth = font.getsize("Merged")[0]
        inset = int((width - combTextWidth) / 2)
        px = px + inset
        draw.text((px, py), "Merged", font=font, fill=(0,0,0))
    
    return canvas
    

def splitViewFigure(session, commandArgs):    
    """
    Processes the arguments, populating defaults if necessary. Prints the details to log (fig-legend).
    Even handles missing arguments that are not optional (from when this ran from commandline with everything optional)
    then calls makeSplitViewFigure() to make the figure, attaches it to the Image as an 'originalFile' annotation,
    with fig-legend as the description. 
    
    @return: the id of the originalFileLink child. (ID object, not value) 
    """

    # create the services we're going to need
    metadataService = session.getMetadataService()
    queryService = session.getQueryService()
    updateService = session.getUpdateService()
    rawFileStore = session.createRawFileStore()
    
    log("Split-View figure created by OMERO on %s" % date.today())
    log("")
    
    pixelIds = []
    imageIds = []
    imageLabels = []
    imageNames = {}
    gateway = session.createGateway()
    omeroImage = None    # this is set as the first image, to link figure to

    # function for getting image labels.
    def getLabels(fullName, tagsList, pdList):
        name = fullName.split("/")[-1]
        return [name]
        
    # default function for getting labels is getName (or use datasets / tags)
    if "imageLabels" in commandArgs:
        if commandArgs["imageLabels"] == "DATASETS":
            def getDatasets(name, tagsList, pdList):
                return [dataset for project, dataset in pdList]
            getLabels = getDatasets
        elif commandArgs["imageLabels"] == "TAGS":
            def getTags(name, tagsList, pdList):
                return tagsList
            getLabels = getTags
            
    # process the list of images. If imageIds is not set, script can't run. 
    log("Image details:")
    if "imageIds" in commandArgs:
        for idCount, imageId in enumerate(commandArgs["imageIds"]):
            iId = long(imageId.getValue())
            imageIds.append(iId)
            image = gateway.getImage(iId)
            if idCount == 0:
                omeroImage = image        # remember the first image to attach figure to
            pixelIds.append(image.getPrimaryPixels().getId().getValue())
            imageNames[iId] = image.getName().getValue()
    
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
    pixels = gateway.getPixels(pixelsId)

    sizeX = pixels.getSizeX().getValue();
    sizeY = pixels.getSizeY().getValue();
    sizeZ = pixels.getSizeZ().getValue();
    sizeC = pixels.getSizeC().getValue();
        
    
    # set image dimensions
    zStart = -1
    zEnd = -1
    if "zStart" in commandArgs:
        zStart = commandArgs["zStart"]
    if "zEnd" in commandArgs:
        zEnd = commandArgs["zEnd"]
    
    width = sizeX
    if "width" in commandArgs:
        w = commandArgs["width"]
        try:
            width = int(w)
        except:
            log("Invalid width: %s Using default value: %d" % (str(w), sizeX))
    
    height = sizeY
    if "height" in commandArgs:
        h = commandArgs["height"]
        try:
            height = int(h)
        except:
            log("Invalid height: %s Using default value" % (str(h), sizeY))
            
    log("Image dimensions for all panels (pixels): width: %d  height: %d" % (width, height))
    
    # Make split-indexes list. If argument wasn't specified, include them all. 
    splitIndexes = []
    if "splitIndexes" in commandArgs:
        for index in commandArgs["splitIndexes"]:
            splitIndexes.append(index.getValue())
    else:
        for c in range(sizeC):
            splitIndexes = range(sizeC)
    
    # Make channel-names map. If argument wasn't specified, name by index
    channelNames = {}
    if "channelNames" in commandArgs:
        cNameMap = commandArgs["channelNames"]
        for c in cNameMap:
            index = int(c)
            channelNames[index] = cNameMap[c].getValue()
    else:
        for c in range(sizeC):
            channelNames[c] = str(c)            
                        
    mergedIndexes = []    # the channels in the combined image, 
    mergedColours = {}    
    if "mergedColours" in commandArgs:
        cColourMap = commandArgs["mergedColours"]
        for c in cColourMap:
            rgb = cColourMap[c].getValue()
            rgba = imgUtil.RGBIntToRGBA(rgb)
            mergedColours[int(c)] = rgba
            mergedIndexes.append(int(c))
        mergedIndexes.sort()
        print mergedIndexes
    else:
        mergedIndexes = range(sizeC)[1:]
        for c in mergedIndexes:    # make up some colours 
            if c%3 == 0:
                mergedColours[c] = (0,0,255,255)    # blue
            if c%3 == 1:
                mergedColours[c] = (0,255,0,255)    # green
            if c%3 == 2:
                mergedColours[c] = (255,0,0,255)    # red
    
    colourChannels = True
    if "splitPanelsGrey" in commandArgs and commandArgs["splitPanelsGrey"]:
        colourChannels = False
    
    algorithm = omero.constants.projection.ProjectionType.MAXIMUMINTENSITY
    if "algorithm" in commandArgs:
        a = commandArgs["algorithm"]
        if (a == "MEANINTENSITY"):
            algorithm = omero.constants.projection.ProjectionType.MEANINTENSITY
    
    stepping = 1
    if "stepping" in commandArgs:
        s = commandArgs["stepping"]
        if (0 < s < sizeZ):
            stepping = s
    
    scalebar = None
    if "scalebar" in commandArgs:
        sb = commandArgs["scalebar"]
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
    if "overlayColour" in commandArgs:
        overlayColour = imgUtil.RGBIntToRGB(commandArgs["overlayColour"])
        
    mergedNames = False
    if "mergedNames" in commandArgs:
        mergedNames = commandArgs["mergedNames"]
        
    fig = makeSplitViewFigure(session, pixelIds, zStart, zEnd, splitIndexes, channelNames, colourChannels, 
                        mergedIndexes, mergedColours, mergedNames, width, height, imageLabels, algorithm, stepping, scalebar, overlayColour)
                                                    
    #fig.show()        # bug-fixing only
    
    figLegend = "\n".join(logStrings)
    
    #print figLegend    # bug fixing only
    
    format = JPEG
    if "format" in commandArgs:
        if commandArgs["format"] in [PNG, "PNG", 'png']:
            format = PNG
            
    output = "splitViewFigure"
    if "figureName" in commandArgs:
        output = str(commandArgs["figureName"])
        
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
       
    labels = [rstring('Image_Name'), rstring('Datasets'), rstring('Tags')]
    algorithums = [rstring('Maximum_Intensity'),rstring('Mean_Intensity')]
    formats = [rstring('JPEG'),rstring('PNG')]
     
    client = scripts.client('splitViewFigure.py', 'Create a figure of split-view images.', 
    scripts.List("imageIds", "List of image IDs. Resulting figure will be attached to first image.", False),
    scripts.Int("zStart", "Projection range (if not specified or -1, use defaultZ only - no projection)", min=-1),
    scripts.Int("zEnd", "Projection range (if not specified or -1, use defaultZ only - no projection)", min=-1),
    scripts.Map("channelNames", "Map of index: channel name for all channels"),
    scripts.List("splitIndexes", "List of the channels in the split view"),
    scripts.Bool("splitPanelsGrey", "If true, all split panels are greyscale"),
    scripts.Map("mergedColours", "Map of index:int colours for each merged channel"),
    scripts.Bool("mergedNames", "If true, label the merged panel with channel names. Otherwise label with 'Merged'"),
    scripts.Int("width", "The max width of each image panel. Default is first image width", min=1),
    scripts.Int("height", "The max height of each image panel. Default is first image height", min=1),
    scripts.String("imageLabels", "Label images with Image name (default) or datasets or tags", values=labels),
    scripts.String("algorithm", "Algorithum for projection.", values=algorithums),
    scripts.Int("stepping", "The Z increment for projection. Default is 1", min=1),
    scripts.Int("scalebar", "Scale bar size in microns. Only shown if image has pixel-size info.", min=1),
    scripts.String("format", "Format to save image. E.g 'PNG'.", values=formats, default='JPEG'),
    scripts.String("figureName", "File name of the figure to save."),
    scripts.Int("overlayColour", "The colour of the scalebar. Default is white"),
    #scripts.Long("fileAnnotation").out()
    )  # script returns a file annotation
    
    session = client.getSession();
    gateway = session.createGateway();
    commandArgs = {"imageIds":client.getInput("imageIds").getValue()}
    
    # process the list of args above. 
    for key in client.getInputKeys():
        if client.getInput(key):
            commandArgs[key] = client.getInput(key).getValue()
    print commandArgs
    # call the main script, attaching resulting figure to Image. Returns the id of the originalFileLink child. (ID object, not value)
    fileAnnotation = splitViewFigure(session, commandArgs)
    # return this fileAnnotation to the client. 
    client.setOutput("File_Annotation", robject(fileAnnotation))
    
if __name__ == "__main__":
    runAsScript()
