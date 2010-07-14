"""
 components/tools/OmeroPy/scripts/omero/figure_scripts/Movie_Figure.py

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

Script produces a figure of a movie, showing panels of different frames.
Saves the figure as a jpg or png attached to the first image in the figure. 

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
import omero
from omero.rtypes import *
import omero.gateway
import omero_api_Gateway_ice    # see http://tinyurl.com/icebuserror
# import util.figureUtil as figUtil    # need to comment out for upload to work. But need import for script to work!!
import getopt, sys, os, subprocess
try:
    import Image, ImageDraw, ImageFont
except ImportError:
    from PIL import Image, ImageDraw, ImageFont
import StringIO
from omero_sys_ParametersI import ParametersI
from datetime import date
    
WHITE = (255, 255, 255)
COLOURS = scriptUtil.COLOURS    # name:(rgba) map
OVERLAY_COLOURS = dict(COLOURS, **scriptUtil.EXTRA_COLOURS)

JPEG = "image/jpeg"
PNG = "image/png"

logLines = []    # make a log / legend of the figure
def log(text):
    print text
    logLines.append(text)
    

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
    
        
def getImageFrames(session, pixelIds, tIndexes, zStart, zEnd, width, height, spacer, 
            algorithm, stepping, scalebar, overlayColour, timeUnits):
    
    """
    Makes a canvas showing an image per row with multiple columns showing 
    frames from each image/movie. Labels obove each frame to show the time-stamp of that frame in the 
    specified units. 
    
    @param session        The OMERO session
    @param pixelIds        A list of the Pixel IDs for the images in the figure
    @param tIndexes        A list of tIndexes to display frames from
    @param zStart        Projection Z-start
    @param zEnd            Projection Z-end
    @param width        Maximum width of panels
    @param height        Max height of panels
    @param spacer        Space between panels
    @param algorithm    Projection algorithm e.g. "MAXIMUMINTENSITY"
    @param stepping        Projecttion z-step
    @param scalebar        A number of microns for scale-bar
    @param overlayColour     Colour of the scale-bar as tuple (255,255,255)
    @param timeUnits    A string such as "SECS"
    """
    
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
    
    for row, pixelsId in enumerate(pixelIds):
        log("Rendering row %d" % (row))
        
        pixels = gateway.getPixels(pixelsId)
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
        
        for time in tIndexes:
            if time >= sizeT:
                log(" WARNING: This image does not have Time frame: %d. (max is %d)" % (time+1, sizeT))     
            else: 
                projection = re.renderProjectedCompressed(algorithm, time, stepping, proStart, proEnd)
                # create images and resize, add to list
                image = Image.open(StringIO.StringIO(projection))
                resizedImage = imgUtil.resizeImage(image, width, height)
                renderedImages.append(resizedImage)

        
        # make a canvas for the row of splitview images...(will add time labels above each row)
        font = imgUtil.getFont(width/12)
        fontHeight = font.getsize("Textq")[1]
        canvasWidth = ((width + spacer) * len(renderedImages)) + spacer
        canvasHeight = spacer/2 + fontHeight + spacer + height
        size = (canvasWidth, canvasHeight)
        canvas = Image.new(mode, size, white)        # create a canvas of appropriate width, height
        
        # add text labels
        queryService = session.getQueryService()
        textX = spacer
        textY = spacer/4
        timeLabels = figUtil.getTimeLabels(queryService, pixelsId, tIndexes, sizeT, timeUnits)
        for t, tIndex in enumerate(tIndexes):
            if tIndex >= sizeT:
                continue
            time = timeLabels[t]
            textW = font.getsize(time)[0]
            inset = (width - textW) / 2
            textdraw = ImageDraw.Draw(canvas)
            textdraw.text((textX+inset, textY), time, font=font, fill=(0,0,0))
            textX += width + spacer
    
        # add scale bar to last frame...
        if scalebar:
            scaledImage = renderedImages[-1]
            xIndent = spacer
            yIndent = xIndent
            zoom = imgUtil.getZoomFactor(scaledImage.size, width, height)     # if we've scaled to half size, zoom = 2
            sbar = float(scalebar) / zoom            # and the scale bar will be half size
            if not addScalebar(sbar, xIndent, yIndent, scaledImage, pixels, overlayColour):
                log("  Failed to add scale bar: Pixel size not defined or scale bar is too large.")
                
        px = spacer
        py = spacer + fontHeight
        # paste the images in
        for img in renderedImages:
            imgUtil.pasteImage(img, canvas, px, py)
            px = px + width + spacer
    
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
    
    
def createMovieFigure(session, pixelIds, tIndexes, zStart, zEnd, width, height, spacer, 
                            algorithm, stepping, scalebar, overlayColour, timeUnits, imageLabels):
    """
    Makes the complete Movie figure: A canvas showing an image per row with multiple columns showing 
    frames from each image/movie. Labels obove each frame to show the time-stamp of that frame in the 
    specified units and labels on the left name each image. 
    
    @param session        The OMERO session
    @param pixelIds        A list of the Pixel IDs for the images in the figure
    @param tIndexes        A list of tIndexes to display frames from
    @param zStart        Projection Z-start
    @param zEnd            Projection Z-end
    @param width        Maximum width of panels
    @param height        Max height of panels
    @param spacer        Space between panels
    @param algorithm    Projection algorithm e.g. "MAXIMUMINTENSITY"
    @param stepping        Projecttion z-step
    @param scalebar        A number of microns for scale-bar
    @param overlayColour     Colour of the scale-bar as tuple (255,255,255)
    @param timeUnits    A string such as "SECS"
    @param imageLabels    A list of lists, corresponding to pixelIds, for labelling each image with one or more strings.
    """

    panelCanvas = getImageFrames(session, pixelIds, tIndexes, zStart, zEnd, width, height, spacer, 
                            algorithm, stepping, scalebar, overlayColour, timeUnits)
                    
    # add lables to row...
    mode = "RGB"
    white = (255,255,255)
    font = imgUtil.getFont(width/12)
    textHeight = font.getsize("Sampleq")[1]
    textGap = spacer /2
    rowSpacing = panelCanvas.size[1]/len(pixelIds)
    
    # find max number of labels
    maxCount = 0 
    rowHeights = []
    for row in imageLabels:
        maxCount = max(maxCount, len(row))
    leftTextWidth = (textHeight + textGap) * maxCount
    size = (panelCanvas.size[1], leftTextWidth)    # make the canvas as wide as the panels height
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
        px = px + rowSpacing         # 2 spacers between each row
        

    # make a canvas big-enough to add text to the images. 
    canvasWidth = leftTextWidth + panelCanvas.size[0]
    canvasHeight = panelCanvas.size[1]
    size = (canvasWidth, canvasHeight)
    canvas = Image.new(mode, size, white)        # create a canvas of appropriate width, height
    
    # add the panels to the canvas 
    pasteX = leftTextWidth
    pasteY = 0
    imgUtil.pasteImage(panelCanvas, canvas, pasteX, pasteY)
    
    # add text to rows
    # want it to be vertical. Rotate and paste the text canvas from above
    if imageLabels:    
        textV = textCanvas.rotate(90)
        imgUtil.pasteImage(textV, canvas, spacer/2, 0)
            
    return canvas
    
    
def movieFigure(session, commandArgs):    
    """
    Makes the figure using the parameters in @commandArgs, attaches the figure to the 
    parent Project/Dataset, and returns the file-annotation ID
    
    @param session        The OMERO session
    @param commandArgs    Map of parameters for the script
    @ returns            Returns the id of the originalFileLink child. (ID object, not value)
    """
    
    # create the services we're going to need. 
    metadataService = session.getMetadataService()
    queryService = session.getQueryService()
    updateService = session.getUpdateService()
    rawFileStore = session.createRawFileStore()
    
    log("Movie figure created by OMERO on %s" % date.today())
    log("")
    
    timeLabels = {"SECS_MILLIS": "seconds",
                "SECS": "seconds",
                "MINS": "minutes",
                "HOURS": "hours",
                "MINS_SECS": "mins:secs",
                "HOURS_MINS": "hours:mins"}
    timeUnits = "SECS"
    if "timeUnits" in commandArgs:
        timeUnits = commandArgs["timeUnits"]
    if timeUnits not in timeLabels.keys():
        timeUnits = "SECS"
    log("Time units are in %s" % timeLabels[timeUnits])
    
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
    if "Image_IDs" in commandArgs:
        for idCount, imageId in enumerate(commandArgs["Image_IDs"]):
            iId = long(imageId.getValue())
            image = gateway.getImage(iId)
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
    pixels = gateway.getPixels(pixelsId)
                
    sizeX = pixels.getSizeX().getValue()
    sizeY = pixels.getSizeY().getValue()
    sizeZ = pixels.getSizeZ().getValue()
    sizeC = pixels.getSizeC().getValue()

    tIndexes = []
    if "T_Indexes" in commandArgs:
        for t in commandArgs["T_Indexes"]:
            tIndexes.append(t.getValue())
            
    zStart = -1
    zEnd = -1
    if "Z_Start" in commandArgs:
        zStart = commandArgs["Z_Start"]
    if "Z_End" in commandArgs:
        zEnd = commandArgs["Z_End"]
    
    width = sizeX
    if "Width" in commandArgs:
        width = commandArgs["Width"]
    
    height = sizeY
    if "Height" in commandArgs:
        height = commandArgs["Height"]
    
    spacer = (width/25) + 2
    
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
    if "Scalebar_Size" in commandArgs:
        sb = commandArgs["Scalebar_Size"]
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
        r,g,b,a = OVERLAY_COLOURS[commandArgs["Scalebar_Colour"]]
        overlayColour = (r,g,b)
                
    figure = createMovieFigure(session, pixelIds, tIndexes, zStart, zEnd, width, height, spacer, 
                            algorithm, stepping, scalebar, overlayColour, timeUnits, imageLabels)
    
    #figure.show()
    
    log("")
    figLegend = "\n".join(logLines)
    
    #print figLegend    # bug fixing only
    
    format = JPEG
    if "Format" in commandArgs:
        if commandArgs["Format"] == "PNG":
            format = PNG
            
    output = "movieFigure"
    if "Figure_Name" in commandArgs:
        output = str(commandArgs["Figure_Name"])
        
    if format == PNG:
        output = output + ".png"
        figure.save(output, "PNG")
    else:
        output = output + ".jpg"
        figure.save(output)
    

    fileAnnotation = scriptUtil.uploadAndAttachFile(queryService, updateService, rawFileStore, omeroImage, output, format, figLegend)
    return (fileAnnotation, omeroImage)    

def runAsScript():
    """
    The main entry point of the script. Gets the parameters from the scripting service, makes the figure and 
    returns the output to the client. 
    """
        
    labels = [rstring('Image Name'), rstring('Datasets'), rstring('Tags')]
    algorithums = [rstring('Maximum Intensity'),rstring('Mean Intensity')]
    tunits =  [rstring("SECS"), rstring("MINS"), rstring("HOURS"), rstring("MINS SECS"), rstring("HOURS MINS")]
    formats = [rstring('JPEG'),rstring('PNG')]
    ckeys = COLOURS.keys()
    ckeys.sort()
    cOptions = wrap(ckeys)
    oColours = wrap(OVERLAY_COLOURS.keys())
    
    client = scripts.client('Movie_Figure.py', 'Export a figure of a movie. See http://trac.openmicroscopy.org.uk/shoola/wiki/FigureExport', 
    scripts.List("Image_IDs", grouping="01", 
        description="List of image IDs. Resulting figure will be attached to first image.", optional=False).ofType(rlong(0)),
    scripts.List("T_Indexes", grouping="02", 
        description="The time frames to display in the figure for each image").ofType(rint(0)),
    scripts.String("Image_Labels", grouping="03", 
        description="Label images with Image name (default) or datasets or tags", values=labels),
    scripts.Int("Width", grouping="06", 
        description="The max width of each image panel. Default is first image width", min=1),
    scripts.Int("Height", grouping="07", 
        description="The max height of each image panel. Default is first image height", min=1),
    scripts.Bool("Z_Projection", grouping="08", default=True),
    scripts.Int("Z_Start", grouping="08.1", 
        description="Projection range (if not specified, use defaultZ only - no projection)", min=0),
    scripts.Int("Z_End", grouping="08.2", 
        description="Projection range (if not specified or, use defaultZ only - no projection)", min=0),
    scripts.String("Algorithm", grouping="08.3", 
        description="Algorithum for projection.", values=algorithums),
    scripts.Int("Stepping", grouping="08.4", 
        description="The Z increment for projection.", default=1, min=1),
    scripts.Bool("Show_Scalebar", grouping="10", default=True),
    scripts.Int("Scalebar_Size", grouping="10.1",
        description="Scale bar size in microns. Only shown if image has pixel-size info.", min=1),
    scripts.String("Scalebar_Colour", grouping="10.2",
        description="The colour of the scalebar.",default='White',values=oColours),
    scripts.String("Format", grouping="11",
        description="Format to save image.", values=formats),
    scripts.String("Figure_Name", grouping="12",
        description="File name of the figure to save."),
    scripts.String("Time_Units", grouping="13",
        description="The units to use for time display", values=tunits),
    
    version = "4.2.0",
    authors = ["William Moore", "OME Team"],
    institutions = ["University of Dundee"],
    contact = "ome-users@lists.openmicroscopy.org.uk",
    ) 
    
    try:
        session = client.getSession();
        gateway = session.createGateway();
        commandArgs = {}
    
        for key in client.getInputKeys():
            if client.getInput(key):
                commandArgs[key] = client.getInput(key).getValue()
                
        print commandArgs
        
        # Makes the figure and attaches it to Image. Returns the id of the originalFileLink child. (ID object, not value)
        fileAnnotation, image = movieFigure(session, commandArgs)
        if fileAnnotation:
            client.setOutput("Message", rstring("Movie Figure Attached to Image: %s" % image.name.val))
            client.setOutput("File_Annotation", robject(fileAnnotation))
    except: raise
    finally: client.closeSession()
    
if __name__ == "__main__":
    runAsScript()
