"""
 components/tools/OmeroPy/scripts/omero/export_scripts/Make_Movie.py

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

Make movie takes a number of parameters and creates an movie from the
image with imageId supplied. This movie is uploaded back to the server and
attached to the original Image.

params:
    imageId: this id of the image to create the movie from
    output: The name of the output file, sans the extension
    zStart: The starting z-section to create the movie from
    zEnd:     The final z-section
    tStart:    The starting timepoint to create the movie
    tEnd:    The final timepoint.
    channels: The list of channels to use in the movie(index, from 0)
    splitView: should we show the split view in the movie(not available yet)
    showTime: Show the average time of the aquisition of the channels in the frame.
    showPlaneInfo: Show the time and z-section of the current frame.
    fps:    The number of frames per second of the movie
    scalebar: The scalebar size in microns, if <=0 will not show scale bar.
    format:    The format of the movie to be created currently supports 'video/mpeg', 'video/quicktime'
    overlayColour: The colour of the overlays, scalebar, time, as int(RGB)
    fileAnnotation: The fileAnnotation id of the uploaded movie.

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
import omero
import omero.min # Constants etc.
import getopt, sys, os, subprocess
import numpy
import omero.util.pixelstypetopython as pixelstypetopython
from struct import *
from omero.rtypes import wrap, rstring, rlong, rint, robject
from omero.gateway import BlitzGateway

from cStringIO import StringIO

try:
    from PIL import Image, ImageDraw # see ticket:2597
except ImportError:
    import Image, ImageDraw # see ticket:2597

COLOURS = scriptUtil.COLOURS;
COLOURS.update(scriptUtil.EXTRA_COLOURS)    # name:(rgba) map

MPEG = 'MPEG'
QT = 'Quicktime'
WMV = 'WMV'
MOVIE_NS = omero.constants.metadata.NSMOVIE
formatNSMap = {MPEG:MOVIE_NS, QT:MOVIE_NS, WMV:MOVIE_NS}
formatExtensionMap = {MPEG:"avi", QT:"avi", WMV:"avi"}
formatMap = {MPEG:"avi", QT:"avi", WMV:"avi"}
formatMimetypes = {MPEG:"video/mpeg", QT:"video/quicktime", WMV:"video/x-ms-wmv"}
OVERLAYCOLOUR = "#666666"
    

logLines = []    # make a log / legend of the figure
def log(text):
    """ Adds lines of text to the logLines list, so they can be collected into a figure legend. """
    print text
    logLines.append(text)


def downloadPlane(gateway, pixels, pixelsId, x, y, z, c, t):
    """ Retrieves the selected plane """
    rawPlane = gateway.getPlane(pixelsId, z, c, t)
    convertType ='>'+str(x*y)+pixelstypetopython.toPython(pixels.getPixelsType().getValue())
    convertedPlane = unpack(convertType, rawPlane)
    remappedPlane = numpy.array(convertedPlane,dtype=(pixels.getPixelsType().getValue()))
    remappedPlane.resize(x,y)
    return remappedPlane

def uploadPlane(gateway, newPixelsId, x, y, z, c, t, newPlane):
    """Uploads the specified plane. """
    byteSwappedPlane = newPlane.byteswap()
    convertedPlane = byteSwappedPlane.tostring()
    gateway.uploadPlane(newPixelsId, z, c, t, convertedPlane)

def macOSX():
    """ Identifies if the Operating System is Mac or not."""
    if ('darwin' in sys.platform):
        return 1
    else:
        return 0

def buildAVI(sizeX, sizeY, filelist, fps, movieName, format):
    """ Encodes. """
    program = 'mencoder'
    args = ""
    if(format==WMV):
        args = ' mf://'+filelist+' -mf w='+str(sizeX)+':h='+str(sizeY)+':fps='+str(fps)+':type=jpg -ovc lavc -lavcopts vcodec=wmv2 -o %s'% movieName
    elif(format==QT):
        args = ' mf://'+filelist+' -mf w='+str(sizeX)+':h='+str(sizeY)+':fps='+str(fps)+':type=png -ovc lavc -lavcopts vcodec=mjpeg:vbitrate=800  -o %s'% movieName
    else:
        args = ' mf://'+filelist+' -mf w='+str(sizeX)+':h='+str(sizeY)+':fps='+str(fps)+':type=jpg -ovc lavc -lavcopts vcodec=mpeg4 -o %s'% movieName
    log(args)
    os.system(program+ args)

def rangeFromList(list, index):
    minValue = list[0][index]
    maxValue = list[0][index]
    for i in list:
        minValue = min(minValue, i[index])
        maxValue = max(maxValue, i[index])
    return range(minValue, maxValue+1)

def calculateAquisitionTime(conn, pixelsId, cList, tzList):
    """ Loads the plane information. """
    queryService = conn.getQueryService()
    
    tRange = ",".join([str(i) for i in rangeFromList(tzList, 0)])
    zRange = ",".join([str(i) for i in rangeFromList(tzList, 1)])
    cRange = ",".join([str(i) for i in cList])
    query = "from PlaneInfo as Info where Info.theZ in (%s) and Info.theT in (%s) and Info.theC in (%s) and pixels.id='%s'" % (zRange, tRange, cRange, pixelsId)
    infoList = queryService.findAllByQuery(query,None)

    map = {}
    for info in infoList:
        if(info.deltaT==None):
            return None
        key = "z:"+str(info.theZ.getValue())+"t:"+str(info.theT.getValue())
        if(map.has_key(key)):
            value = map.get(key)
            value = value+info.deltaT.getValue()
            map[key] = value
        else:
            map[key] = info.deltaT.getValue()
    for key in map:
        map[key] = map[key]/len(cRange)
    return map

def addScalebar(scalebar, image, pixels, commandArgs):
    """ Adds the scalebar. """
    image_w, image_h = image.size
    draw = ImageDraw.Draw(image)
    if (pixels.getPhysicalSizeX()==None):
       return image
    pixelSizeX = pixels.getPhysicalSizeX()
    if(pixelSizeX<=0):
        return image
    scaleBarY = image_h-30
    scaleBarX = image_w-scalebar/pixelSizeX-20
    scaleBarTextY = scaleBarY-15
    scaleBarX2 = scaleBarX+scalebar/pixelSizeX
    if (scaleBarX<=0 or scaleBarX2<=0 or scaleBarY<=0 or scaleBarX2>image_w):
        return image
    draw.line([(scaleBarX,scaleBarY), (scaleBarX2,scaleBarY)], fill=commandArgs["Overlay_Colour"])
    draw.text(((scaleBarX+scaleBarX2)/2, scaleBarTextY), str(scalebar), fill=commandArgs["Overlay_Colour"])
    return image

def addPlaneInfo(z, t, pixels, image, colour):
    """ Displays the plane information. """
    image_w, image_h = image.size
    draw = ImageDraw.Draw(image)
    planeInfoTextY = image_h-60
    textX = 20
    if(planeInfoTextY<=0 or textX > image_w or planeInfoTextY>image_h):
        return image
    planeCoord = "z:"+str(z+1)+" t:"+str(t+1)
    draw.text((textX, planeInfoTextY), planeCoord, fill=colour)
    return image

def addTimePoints(time, pixels, image, colour):
    """ Displays the time-points. """
    image_w, image_h = image.size
    draw = ImageDraw.Draw(image)
    textY = image_h-45
    textX = 20
    if(textY<=0 or textX > image_w or textY>image_h):
        return image
    draw.text((textX, textY), str(time), fill=colour)
    return image

def getRenderingEngine(conn, pixelsId, sizeC, cRange):
    """ Initializes the rendering engine for the specified pixels set. """
    renderingEngine = conn.createRenderingEngine()
    renderingEngine.lookupPixels(pixelsId)
    if(renderingEngine.lookupRenderingDef(pixelsId)==0):
        renderingEngine.resetDefaults()
    renderingEngine.lookupRenderingDef(pixelsId)
    renderingEngine.load()
    if len(cRange) == 0:
        for channel in range(sizeC):
            renderingEngine.setActive(channel, 1)
    else:
        for channel in range(sizeC):
            renderingEngine.setActive(channel, 0)
        for channel in cRange:
            renderingEngine.setActive(channel, 1)
    return renderingEngine

def getPlane(renderingEngine, z, t):
    """ Retrieves the specified XY-plane. """
    planeDef = omero.romio.PlaneDef()
    planeDef.t = t
    planeDef.z = z
    planeDef.x = 0
    planeDef.y = 0
    planeDef.slice = 0
    return renderingEngine.renderAsPackedInt(planeDef)

def inRange(low, high, max):
    """ Determines if the passed values are in the range. """
    if(low < 0 or low > high):
        return 0
    if(high < 0 or high > max):
        return 0
    return 1

def validChannels(set, sizeC):
    """ Determines the channels are valid """
    if(len(set)==0):
        return False
    for val in set:
        if(val < 0 or val > sizeC):
            return False
    return True

def validColourRange(colour):
    """ Checks if the passed value is valid. """
    if(colour >= 0 and colour < 0xffffff):
        return 1
    return 0

def buildPlaneMapFromRanges(zRange, tRange):
    """ Determines the plane to load. """
    planeMap = []
    for t in tRange:
        for z in zRange:
            planeMap.append([t,z])
    return planeMap

def strToRange(key):
    splitKey = key.split('-')
    if(len(splitKey)==1):
        return range(int(splitKey[0]), int(splitKey[0])+1)
    return range(int(splitKey[0]), int(splitKey[1])+1)

def unrollPlaneMap(planeMap):
    unrolledPlaneMap = []
    for tSet in planeMap:
        zValue = planeMap[tSet]
        for t in strToRange(tSet):
            for z in strToRange(zValue.getValue()):
                unrolledPlaneMap.append([int(t),int(z)])
    return unrolledPlaneMap

def calculateRanges(sizeZ, sizeT, commandArgs):
    """ Determines the plane to load. """
    planeMap = {}
    if "Plane_Map" not in commandArgs:
        zStart = 0
        zEnd = sizeZ
        if "Z_Start" in commandArgs and commandArgs["Z_Start"] >= 0 and commandArgs["Z_Start"] < sizeZ:
            zStart = commandArgs["Z_Start"]
        if "Z_End" in commandArgs and commandArgs["Z_End"] >= 0 and commandArgs["Z_End"] < sizeZ and commandArgs["Z_End"] >= zStart:
            zEnd = commandArgs["Z_End"]+1
        tStart = 0
        tEnd = sizeT-1
        if "T_Start" in commandArgs and commandArgs["T_Start"] >= 0 and commandArgs["T_Start"] < sizeT:
            tStart = commandArgs["T_Start"]
        if "T_End" in commandArgs and commandArgs["T_End"] >= 0 and commandArgs["T_End"] < sizeT and commandArgs["T_End"] >= tStart:
            tEnd = commandArgs["T_End"]+1
        if(zEnd==zStart):
            zEnd=zEnd+1;
        if(tEnd==tStart):
            tEnd=tEnd+1;

        zRange = range(zStart, zEnd)
        tRange = range(tStart, tEnd)
        planeMap = buildPlaneMapFromRanges(zRange, tRange)
    else:
        map = commandArgs["Plane_Map"]
        planeMap = unrollPlaneMap(map)
    return planeMap


def reshape_to_fit(image, sizeX, sizeY, bg=(0,0,0)):
    """
    Make the PIL image fit the sizeX and sizeY dimensions by scaling as necessary
    and then padding with background.
    Used for watermark and intro & outro slides.
    """
    image_w, image_h = image.size
    if (image_w, image_h) == (sizeX, sizeY):
        return image
    # scale
    print "scale...from ", image.size, " to ", sizeX, sizeY
    ratio = min(float(sizeX) / image_w, float(sizeY) / image_h)
    image = image.resize(map(lambda x: x*ratio, image.size), Image.ANTIALIAS)
    print ratio, image.size
    # paste
    bg = Image.new("RGBA", (sizeX, sizeY), (0,0,0))     # black bg
    ovlpos = (sizeX-image.size[0]) / 2, (sizeY-image.size[1]) / 2
    print "ovlpos", ovlpos
    bg.paste(image, ovlpos)
    return bg


def write_intro_end_slides(conn, commandArgs, orig_file_id, duration, sizeX, sizeY):
    """
    Uses an original file (jpeg or png) to add frames to the movie.
    Scales and pads to fit sizeX, sizeY.

    @param orig_file_id:    Original File (png or jpeg) ID
    @param duration:        Duration of intro / end (secs)
    @param sizeX:           Width of the exported movie
    @param sizeY:           Height of the exported movie
    @return:                List of file names to add to mencoder list
    """

    slide_filenames = []
    fps = commandArgs["FPS"]
    format = commandArgs["Format"]

    # get Original File as Image
    slide_file = conn.getObject("OriginalFile", orig_file_id)
    slide_data = "".join( slide_file.getFileInChunks() )
    i = StringIO(slide_data)
    slide = Image.open(i)
    slide = reshape_to_fit(slide, sizeX, sizeY)

    # write the file once
    if format==QT:
        filename = 'slide_%s.png' % orig_file_id
        slide.save(filename,"PNG")
    else:
        filename = 'slide_%s.jpg' % orig_file_id
        slide.save(filename,"JPEG")
    # control duration by adding the filename multiple times
    for i in range(duration * fps):
        slide_filenames.append(filename)

    return slide_filenames


def prepareWatermark(conn, commandArgs, sizeX, sizeY):
    """
    Read Original File (png or jpeg) to use as watermark,
    scale if needed to fit movie (sizeX, sizeY) and return

    @return:        PIL Image to use as watermark.
    """

    wm_orig_file = commandArgs["Watermark"]
    # get Original File as Image
    wm_file = conn.getObject("OriginalFile", wm_orig_file.id.val)
    wm_data = "".join( wm_file.getFileInChunks() )
    i = StringIO(wm_data)
    wm = Image.open(i)
    wm_w, wm_h = wm.size
    # only resize watermark if too big
    if wm_w > sizeX or wm_h > sizeY:
        wm = reshape_to_fit(wm, sizeX, sizeY)
    #wm = wm.convert("L")
    return wm


def pasteWatermark(image, watermark):
    """ Paste the watermark onto the bottom left corner of the image. Return image """

    wm_w, wm_h = watermark.size
    w, h = image.size
    wmpos = 0, h - wm_h
    image.paste(watermark, wmpos, watermark)
    return image


def writeMovie(commandArgs, conn):
    """
    Makes the movie.
    
    @ returns        Returns the file annotation
    """
    log("Movie created by OMERO")
    log("")

    message=""

    sopts = conn.CONFIG['SERVICE_OPTS'] or {}
    sopts['omero.group'] = '-1'
    conn.CONFIG['SERVICE_OPTS'] = sopts
    session = conn.c.sf
    gateway = conn
    scriptService = session.getScriptService()
    queryService = session.getQueryService()
    updateService = session.getUpdateService()
    rawFileStore = session.createRawFileStore()

    # Get the images
    images, logMessage = scriptUtil.getObjects(conn, commandArgs)
    message += logMessage
    if not images:
        return None, message
    omeroImage = images[0] # Get the first valid image (should be expanded to process the list)
    
    if commandArgs["RenderingDef_ID"] >= 0:
        omeroImage._prepareRenderingEngine(rdid=commandArgs["RenderingDef_ID"])
    pixels = omeroImage.getPrimaryPixels()
    pixelsId = pixels.getId()

    sizeX = pixels.getSizeX()
    sizeY = pixels.getSizeY()
    sizeZ = pixels.getSizeZ()
    sizeC = pixels.getSizeC()
    sizeT = pixels.getSizeT()

    if (sizeX==None or sizeY==None or sizeZ==None or sizeT==None or sizeC==None):
        return

    if (pixels.getPhysicalSizeX()==None):
        commandArgs["Scalebar"]=0

    cRange = range(0, sizeC)
    if "Channels" in commandArgs and validChannels(commandArgs["Channels"], sizeC):
        cRange = commandArgs["Channels"]

    tzList = calculateRanges(sizeZ, sizeT, commandArgs)

    timeMap = calculateAquisitionTime(conn, pixelsId, cRange, tzList)
    if (timeMap==None):
        commandArgs["Show_Time"]=False
    if (timeMap != None):
        if (len(timeMap)==0):
            commandArgs["Show_Time"]=False

    pixelTypeString = pixels.getPixelsType().getValue()
    frameNo = 1
    omeroImage.setActiveChannels(map(lambda x: x+1, cRange))
    renderingEngine = omeroImage._re

    overlayColour = (255,255,255)
    if "Overlay_Colour" in commandArgs:
        r,g,b,a = COLOURS[commandArgs["Overlay_Colour"]]
        overlayColour = (r,g,b)

    canvasColour = tuple(COLOURS[commandArgs["Canvas_Colour"]][:3])
    mw = commandArgs["Min_Width"]
    if mw < sizeX:
        mw = sizeX
    mh = commandArgs["Min_Height"]
    if mh < sizeY:
        mh = sizeY
    ovlpos = None
    canvas = None
    if sizeX < mw or sizeY < mh:
        ovlpos = ((mw-sizeX) / 2, (mh-sizeY) / 2)
        canvas = Image.new("RGBA", (mw,mh), canvasColour)
        
    format = commandArgs["Format"]
    fileNames = []

    # add intro...
    if "Intro_Slide" in commandArgs and commandArgs["Intro_Slide"].id:
        intro_duration = commandArgs["Intro_Duration"]
        intro_fileId = commandArgs["Intro_Slide"].id.val
        intro_filenames = write_intro_end_slides(conn, commandArgs, intro_fileId, intro_duration, mw, mh)
        fileNames.extend(intro_filenames)

    # prepare watermark
    if "Watermark" in commandArgs and commandArgs["Watermark"].id:
        watermark = prepareWatermark(conn, commandArgs, mw, mh)

    # add movie frames...
    for tz in tzList:
        t = tz[0]
        z = tz[1]
        plane = getPlane(renderingEngine, z, t)
        planeImage = numpy.array(plane, dtype='uint32')
        planeImage = planeImage.byteswap()
        planeImage = planeImage.reshape(sizeX, sizeY)
        image = Image.frombuffer('RGBA',(sizeX,sizeY),planeImage.data,'raw','ARGB',0,1)
        if ovlpos is not None:
            image2 = canvas.copy()
            image2.paste(image, ovlpos, image)
            image = image2
        
        if "Scalebar" in commandArgs and commandArgs["Scalebar"]:
            image = addScalebar(commandArgs["Scalebar"], image, pixels, commandArgs)
        planeInfo = "z:"+str(z)+"t:"+str(t)
        if "Show_Time" in commandArgs and commandArgs["Show_Time"]:
            time = timeMap[planeInfo]
            image = addTimePoints(time, pixels, image, overlayColour)
        if "Show_Plane_Info" in commandArgs and commandArgs["Show_Plane_Info"]:
            image = addPlaneInfo(z, t, pixels, image, overlayColour)
        if "Watermark" in commandArgs and commandArgs["Watermark"].id:
            image = pasteWatermark(image, watermark)
        if format==QT:
            filename = str(frameNo)+'.png'
            image.save(filename,"PNG")
        else:
            filename = str(frameNo)+'.jpg'
            image.save(filename,"JPEG")
        fileNames.append(filename)
        frameNo +=1

    # add exit frames... "outro"
    # add intro...
    if "Ending_Slide" in commandArgs and commandArgs["Ending_Slide"].id:
        end_duration = commandArgs["Ending_Duration"]
        end_fileId = commandArgs["Ending_Slide"].id.val
        end_filenames = write_intro_end_slides(conn, commandArgs, end_fileId, end_duration, mw, mh)
        fileNames.extend(end_filenames)

    filelist= ",".join(fileNames)
        
    ext = formatMap[format]
    movieName = "Movie"
    if "Movie_Name" in commandArgs:
        movieName = commandArgs["Movie_Name"]
    if not movieName.endswith(".%s" % ext):
        movieName = "%s.%s" % (movieName, ext)
        
    framesPerSec = 2
    if "FPS" in commandArgs:
        framesPerSec = commandArgs["FPS"]
    buildAVI(mw, mh, filelist, framesPerSec, movieName, format)
    figLegend = "\n".join(logLines)
    mimetype = formatMimetypes[format]

    if not commandArgs["Do_Link"]:
        originalFile = scriptUtil.createFile(updateService, movieName, mimetype, movieName);
        scriptUtil.uploadFile(rawFileStore, originalFile, movieName)
        return originalFile, message
    
    namespace = omero.constants.namespaces.NSCREATED+"/omero/export_scripts/Make_Movie"
    fileAnnotation, annMessage = scriptUtil.createLinkFileAnnotation(conn, movieName, omeroImage,
        output="Movie", ns=namespace, mimetype=mimetype)
    message += annMessage
    return fileAnnotation, message

def runAsScript():
    """
    The main entry point of the script. Gets the parameters from the scripting service, makes the figure and 
    returns the output to the client. 
    def __init__(self, name, optional = False, out = False, description = None, type = None, min = None, max = None, values = None)
    """
    formats = wrap(formatMap.keys())    # wrap each key in it's rtype
    ckeys = COLOURS.keys()
    ckeys = ckeys;
    ckeys.sort()
    cOptions = wrap(ckeys)
    dataTypes= [rstring("Image")]
    
    client = scripts.client('Make_Movie','MakeMovie creates a movie of the image and attaches it to the originating image.',
    scripts.String("Data_Type", optional=False, grouping="1", description="Choose Images via their 'Image' IDs.", values=dataTypes, default="Image"),
    scripts.List("IDs", optional=False, grouping="1", description="List of Image IDs to process.").ofType(rlong(0)),
    scripts.Long("RenderingDef_ID", description="The Rendering Definitions for the Image.", default=-1, optional=True, grouping="1"),
    scripts.String("Movie_Name", description="The name of the movie", grouping="2"),
    scripts.Int("Z_Start", description="Projection range (if not specified, use defaultZ only - no projection)", min=0, default=0, grouping="3.1"),
    scripts.Int("Z_End", description="Projection range (if not specified or, use defaultZ only - no projection)", min=0, grouping="3.2"),
    scripts.Int("T_Start", description="The first time-point", min=0, default=0, grouping="4.1"),
    scripts.Int("T_End", description="The last time-point", min=0, grouping="4.2"),
    scripts.List("Channels", description="The selected channels", grouping="5").ofType(rint(0)),
    scripts.Bool("Show_Time", description="If true, display the time.", default=True, grouping="6"),
    scripts.Bool("Show_Plane_Info", description="If true, display the information about the plane e.g. Exposure Time.", default=True, grouping="7"),
    scripts.Int("FPS", description="Frames Per Second.", default=2, grouping="8"),
    scripts.Int("Scalebar", description="Scale bar size in microns. Only shown if image has pixel-size info.", min=1, grouping="9"),
    scripts.String("Format", description="Format to save movie", values=formats, default=QT, grouping="10"),
    scripts.String("Overlay_Colour", description="The colour of the scalebar.",default='White',values=cOptions, grouping="11"),
    scripts.String("Canvas_Colour", description="The background colour when using minimum size.",default='Black',values=cOptions),
    scripts.Int("Min_Width", description="Minimum width for output movie.", default=-1),
    scripts.Int("Min_Height", description="Minimum height for output movie.", default=-1),
    scripts.Map("Plane_Map", description="Specify the individual planes (instead of using T_Start, T_End, Z_Start and Z_End)", grouping="12"),
    scripts.Object("Watermark", description="Specifiy a watermark as an Original File (png or jpeg)", 
            default=omero.model.OriginalFileI()),
    scripts.Object("Intro_Slide", description="Specifiy an Intro slide as an Original File (png or jpeg)",
            default=omero.model.OriginalFileI()),
    scripts.Int("Intro_Duration", default=3, description="Duration of Intro in seconds. Default is 3 secs."),
    scripts.Object("Ending_Slide", description="Specifiy a finishing slide as an Original File, (png or jpeg)",
            default=omero.model.OriginalFileI()),
    scripts.Int("Ending_Duration", default=3, description="Duration of finishing slide in seconds. Default is 3 secs."),
    scripts.Bool("Do_Link", description="If true, creates a FileAnnotation with the OriginalFile holding the movie and links it to the Image.", default=True),

    version = "4.2.0",
    authors = ["Donald MacDonald", "OME Team"],
    institutions = ["University of Dundee"],
    contact = "ome-users@lists.openmicroscopy.org.uk",
    )

    try:
        conn = BlitzGateway(client_obj=client)
        commandArgs = {}

        for key in client.getInputKeys():
            if client.getInput(key):
                commandArgs[key] = client.getInput(key,unwrap=True)
        print commandArgs
        
        fileAnnotation, message = writeMovie(commandArgs, conn)
        
        # return this fileAnnotation to the client. 
        client.setOutput("Message", rstring(message))
        if fileAnnotation is not None:
            client.setOutput("File_Annotation", robject(fileAnnotation._obj))
    finally:
        client.closeSession()

if __name__ == "__main__":
     runAsScript()
