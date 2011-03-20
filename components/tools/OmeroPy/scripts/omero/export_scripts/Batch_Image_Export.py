"""
 components/tools/OmeroPy/scripts/omero/export_scripts/Batch_Image_Export.py 

-----------------------------------------------------------------------------
  Copyright (C) 2006-2011 University of Dundee. All rights reserved.


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

This script takes a number of images and saves individual image planes in a zip
file for download. 

@author Will Moore
<a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
@version 4.3
<small>
(<b>Internal version:</b> $Revision: $Date: $)
</small>
@since 3.0-Beta4.3
"""

import omero.scripts as scripts
import omero.util.script_utils as script_utils
import omero
from omero.rtypes import *
import os
import StringIO

from zipfile import ZipFile

try:
    from PIL import Image, ImageDraw # see ticket:2597
except ImportError:
    import Image, ImageDraw # see ticket:2597


# keep track of log strings. 
logStrings = []

def log(text):
    """
    Adds the text to a list of logs. Compiled into text file at the end.
    """
    print text
    logStrings.append(text)
    

def compress(target, base):
    """Creates a ZIP recursively from a given base directory."""
    zip_file = ZipFile(target, 'w')
    try:
        for root, dirs, names in os.walk(base):
            for name in names:
                path = os.path.join(root, name)
                print "Compressing: %s" % path
                zip_file.write(path)
    finally:
        zip_file.close()
        

def renderImage(renderingEngine, zRange, t=0, channel=None, greyscale=False):
    """
    Generates a rendered image of the specified plane, with various active channels.
    Simply activates the appropriate channels and renders plane (no other rendering adjustments).
    Returns a PIL image of the rendered plane.
    
    @param renderingEngine:     Rendering Engine should already be initialised with the correct pixels etc
    @param zRange:              Tuple of (zIndex,) OR (zStart, zStop) for projection
    @param t:                   T index
    @param channel:             Active channel index. If None, use current rendering settings
    @param greyscale:           If true, all visible channels will be greyscale
    """
    
    if channel != None:
        pixels = renderingEngine.getPixels()
        for i in range(pixels.getSizeC().getValue()):
            renderingEngine.setActive(i, i == channel)
            if greyscale:
                renderingEngine.setRGBA(i,255,255,255,255)

    if len(zRange) == 1:
        planeDef = omero.romio.PlaneDef()
        planeDef.z = long(zRange[0])
        planeDef.t = long(t)
        img = renderingEngine.renderCompressed(planeDef)    # compressed String
    else:
        algorithm = omero.constants.projection.ProjectionType.MAXIMUMINTENSITY
        stepping = 1
        img = renderingEngine.renderProjectedCompressed(algorithm, t, stepping, zRange[0], zRange[1])
        
    pilImage = Image.open(StringIO.StringIO(img))
    #pilImage.show()
    return pilImage
    

def savePlane(renderingEngine, originalName, format, cName, zRange, t=0, channel=None, greyscale=False, imgWidth=None):
    """
    Renders and saves an image to disk.
    
    @param renderingEngine:     Rendering Engine should already be initialised with the correct pixels etc
    @param imgName:             The name or path to save to disk, with extension. E.g. imgDir/image01_DAPI_T01_Z01.png
    @param zRange:              Tuple of (zIndex,) OR (zStart, zStop) for projection
    @param t:                   T index
    @param channel:             Active channel index. If None, use current rendering settings
    @param greyscale:           If true, all visible channels will be greyscale 
    @param imgWidth:            Resize image to this width if specified.
    """
    
    print ""
    print "savePlane.."
    print "originalName", originalName
    print "format", format
    print "cName", cName
    print "zRange", zRange
    print "t", t
    print "channel", channel
    print "greyscale", greyscale
    print "imgWidth", imgWidth
    
    
    image = renderImage(renderingEngine, zRange, t, channel, greyscale)
    if imgWidth:
        w, h = image.size()
        newH = (float(imgW) / w ) * h
        image = image.resize((imgWidth, newH))
        
    if format == "PNG":
        imgName = makeImageName(originalName, cName, zRange, t, "png")
        image.save(imgName, "PNG")
    else:
        imgName = makeImageName(originalName, cName, zRange, t, "jpg")
        image.save(imgName)
        
        
def makeImageName(originalName, cName, zRange, t, extension):
    """ 
    Produces the name for the saved image.
    E.g. imported/myImage.dv -> myImage_DAPI_z13_t01.png
    """
    name = originalName.split("/")[-1]
    name = name.rsplit(".",1)[0]
    if len(zRange) == 2:
        z = "%02d-%02d" % (zRange[0], zRange[1])
    else:
        z = "%02d" % zRange[0]
    imgName = "images/%s_%s_z%s_t%02d.%s" % (name, cName, z, t, extension)
    print imgName
    return imgName
    
    
def savePlanesForImage(renderingEngine, originalName, pixelsId, sizeC, splitCs, mergedCs, channelNames=None,
        zRange=None, tRange=None, greyscale=False, imgWidth=None, projectZ=False, format="PNG"):
    """
    Saves all the required planes for a single image, either as individual planes or projection.
    
    @param renderingEngine:     Rendering Engine, NOT initialised. 
    @param queryService:        OMERO query service
    @param imageId:             Image ID
    @param zRange:              Tuple: (zStart, zStop). If None, use default Zindex
    @param tRange:              Tuple: (tStart, tStop). If None, use default Tindex
    @param greyscale:           If true, all visible channels will be greyscale 
    @param imgWidth:            Resize image to this width if specified.
    @param projectZ:            If true, project over Z range.
    """
    
    channels = []
    if mergedCs:
        channels.append(None)   # render merged first with current rendering settings
    if splitCs:
        for i in range(sizeC):
            channels.append(i)
        

    # set up rendering engine with the pixels
    renderingEngine.lookupPixels(pixelsId)
    if not renderingEngine.lookupRenderingDef(pixelsId):
        renderingEngine.resetDefaults()
    if not renderingEngine.lookupRenderingDef(pixelsId):
        raise "Failed to lookup Rendering Def"
    renderingEngine.load()
    
    if tRange == None:
        tIndexes = [renderingEngine.getDefaultT()]
    else:
        if len(tRange) > 1:
            tIndexes = range(tRange[0], tRange[1])
        else:
            tIndexes = [tRange[0]]
    
    cName = 'merged'
    for c in channels:
        if c != None:
            if channelNames != None and c < len(channelNames):
                cName = channelNames[c]
            else:
                cName = "c%02d" % c
        for t in tIndexes:
            if zRange == None:
                defaultZ = renderingEngine.getDefaultZ()
                savePlane(renderingEngine, originalName, format, cName, (defaultZ,), t, c, greyscale, imgWidth)
            elif projectZ:
                savePlane(renderingEngine, originalName, format, cName, zRange, t, c, greyscale, imgWidth)
            else:
                if len(zRange) > 1:
                    for z in range(zRange[0], zRange[1]):
                        savePlane(renderingEngine, originalName, format, cName, (z,), t, c, greyscale, imgWidth)
                else:
                    savePlane(renderingEngine, originalName, format, cName, zRange, t, c, greyscale, imgWidth)


def batchImageExport(session, scriptParams):
    
    # for params with default values, we can get the value directly
    splitCs = scriptParams["Export_Individual_Channels"]
    mergedCs = scriptParams["Export_Merged_Image"] 
    greyscale = scriptParams["Individual_Channels_Grey"]
    dataType = scriptParams["Data_Type"]
    ids = scriptParams["IDs"]
    
    if (not splitCs) and (not mergedCs):
        print "Not chosen to save Individual Channels OR Merged Image"
        return
        
    # check if we have these params
    channelNames = []
    if "Channel_Names" in scriptParams:
        channelNames = scriptParams["Channel_Names"]
    imgWidth = None
    if "Image_Width" in scriptParams:
        imgWidth = scriptParams["Image_Width"]
    
    
    # functions used below for each imaage.
    def getZrange(sizeZ, scriptParams):
        zRange = None
        if "Choose_Z_Section" in scriptParams:
            zChoice = scriptParams["Choose_Z_Section"]
            if zChoice == 'ALL Z planes':
                zRange = (0, sizeZ)
            elif "OR_specify_Z_index" in scriptParams:
                zIndex = scriptParams["OR_specify_Z_index"]
                zIndex = min(zIndex, sizeZ-1)
                zRange = (zIndex,)
            elif "OR_specify_Z_start_AND..." in scriptParams and "...specify_Z_end" in scriptParams:
                zStart = scriptParams["OR_specify_Z_start_AND..."]
                zEnd = scriptParams["...specify_Z_end"]
                zRange = (min(sizeZ-1,zStart), min(sizeZ-1,zEnd) )
        return zRange
    
    def getTrange(sizeT, scriptParams):
        tRange = None
        if "Choose_T_Section" in scriptParams:
            tChoice = scriptParams["Choose_T_Section"]
            if tChoice == 'ALL T planes':
                tRange = (0, sizeT)
            elif "OR_specify_T_index" in scriptParams:
                tIndex = scriptParams["OR_specify_T_index"]
                tIndex = min(tIndex, sizeT-1)
                tRange = (tIndex,)
            elif "OR_specify_t_start_AND..." in scriptParams and "...specify_T_end" in scriptParams:
                tStart = scriptParams["OR_specify_T_start_AND..."]
                tEnd = scriptParams["...specify_T_end"]
                tRange = (min(sizeT-1,tStart), min(sizeT-1,tEnd) )
        return tRange
    
    # images to export
    images = containerService.getImages(dataType, ids, None)
    print "Processing %s images" % len(images)
    
    # services we need
    renderingEngine = session.createRenderingEngine()
    containerService = session.getContainerService()
    
    # somewhere to put images
    curr_dir = os.getcwd()
    exp_dir = os.path.join(curr_dir, "images")
    os.mkdir(exp_dir)
    
    # do the saving to disk
    for img in images:
        pixelsId = img.getPrimaryPixels().getId().getValue()
        originalName = img.getName().getValue()
        print "\nSaving image", originalName
        sizeC = img.getPrimaryPixels().getSizeC().getValue()
        sizeZ = img.getPrimaryPixels().getSizeZ().getValue()
        sizeT = img.getPrimaryPixels().getSizeT().getValue()
        zRange = getZrange(sizeZ, scriptParams)
        tRange = getTrange(sizeT, scriptParams)
        print "zRange", zRange
        print "tRange", tRange
        savePlanesForImage(renderingEngine, originalName, pixelsId, sizeC, splitCs, mergedCs, channelNames,
            zRange, tRange, greyscale, imgWidth, projectZ=False, format="PNG")
    
    renderingEngine.close() 

    # zip up image folder
    zip_file_name = "exported_images.zip"
    zip_file = os.path.join(curr_dir, zip_file_name)
    compress(zip_file, exp_dir)
    
    queryService = session.getQueryService()
    updateService = session.getUpdateService()
    rawFileStore = session.createRawFileStore()
    image = images[0]
    
    fileAnnotation = None
    if os.path.exists(zip_file_name):
        print "Attaching zip to image... %s" % image.getName().getValue(), image.getId().getValue()
        fileAnnotation = script_utils.uploadAndAttachFile(queryService, updateService, rawFileStore, image, zip_file_name, mimetype="zip")
        return fileAnnotation

def runScript():
    """
    The main entry point of the script, as called by the client via the scripting service, passing the required parameters. 
    """
       
    dataTypes = [rstring('Dataset'),rstring('Image')]
    formats = [rstring('JPEG'),rstring('PNG')]
    defaultZoption = 'Default-Z (last-viewed)'
    zChoices = [rstring(defaultZoption),
        rstring('ALL Z planes'),
        rstring('Other (see below)')]
    defaultToption = 'Default-T (last-viewed)'
    tChoices = [rstring(defaultToption),
        rstring('ALL T planes'),
        rstring('Other (see below)')]
     
    client = scripts.client('Batch_Image_Export.py', """Save multiple images as jpegs, pngs etc in a zip
file available for download as a batch export. 
See http://www.openmicroscopy.org/site/support/omero4/getting-started/tutorial/running-util-scripts""", 

    scripts.String("Data_Type", optional=False, grouping="1",
        description="The data you want to work with.", values=dataTypes, default="Image"),

    scripts.List("IDs", optional=False, grouping="2",
        description="List of Dataset IDs or Image IDs").ofType(rlong(0)),
        
    scripts.Bool("Export_Individual_Channels", grouping="3", 
        description="Save individual channels as separate images", default=True),
    
    scripts.Bool("Individual_Channels_Grey", grouping="3.1", 
        description="If true, all individual channel images will be greyscale", default=False),
    
    scripts.List("Channel_Names", grouping="3.2", 
        description="Names for saving individual channel images"),
    
    scripts.Bool("Export_Merged_Image", grouping="4", 
        description="Save merged image, using current rendering settings", default=True),
    
    scripts.String("Choose_Z_Section", grouping="5",
        description="Default Z is last viewed Z for each image, OR choose Z below.", values=zChoices, default=defaultZoption),
    
    scripts.Int("OR_specify_Z_index", grouping="5.1",
        description="Choose a specific Z-index to export", min=0),
    
    scripts.Int("OR_specify_Z_start_AND...", grouping="5.2",
        description="Choose a specific Z-index to export", min=0),
    
    scripts.Int("...specify_Z_end", grouping="5.3",
        description="Choose a specific Z-index to export", min=0),
    
    scripts.String("Choose_T_Section", grouping="6",
        description="Default T is last viewed T for each image, OR choose T below.", values=tChoices, default=defaultToption),
    
    scripts.Int("OR_specify_T_index", grouping="6.1",
        description="Choose a specific T-index to export", min=0),
    
    scripts.Int("OR_specify_T_start_AND...", grouping="6.2",
        description="Choose a specific T-index to export", min=0),
    
    scripts.Int("...specify_T_end", grouping="6.3",
        description="Choose a specific T-index to export", min=0),
        
    scripts.Int("Image_Width", grouping="7", 
        description="The max width of each image panel. Default is actual size", min=1),

    scripts.String("Format", grouping="8", 
        description="Format to save image", values=formats, default='JPEG'),
    
    version = "4.3.0",
    authors = ["William Moore", "OME Team"],
    institutions = ["University of Dundee"],
    contact = "ome-users@lists.openmicroscopy.org.uk",
    ) 
    
    session = client.getSession()
    scriptParams = {}

    # process the list of args above. 
    for key in client.getInputKeys():
        if client.getInput(key):
            scriptParams[key] = unwrap(client.getInput(key))
    print scriptParams
    # call the main script
    fileAnnotation = batchImageExport(session, scriptParams)
    # return this fileAnnotation to the client. 
    if fileAnnotation:
        client.setOutput("Message", rstring("Batch Export zip created and attached"))
        client.setOutput("File_Annotation", robject(fileAnnotation))
    

if __name__ == "__main__":
    runScript()
