"""
 components/tools/OmeroPy/scripts/omero/figure_scripts/Thumbnail_Figure.py

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
from omero.gateway import BlitzGateway
from omero.rtypes import *
import omero.util.imageUtil as imgUtil
from datetime import date

try:
    from PIL import Image, ImageDraw # see ticket:2597
except ImportError:
    import Image, ImageDraw # see ticket:2597

WHITE = (255, 255, 255)

logLines = []    # make a log / legend of the figure
def log(text):
    """ Adds lines of text to the logLines list, so they can be collected into a figure legend. """
    print text
    logLines.append(text)
    
    
def paintDatasetCanvas(conn, images, title, tagIds=None, showUntagged = False, colCount = 10, length = 100):
    """
        Paints and returns a canvas of thumbnails from images, laid out in a set number of columns. 
        Title and date-range of the images is printed above the thumbnails,
        to the left and right, respectively. 
        
        @param conn:        Blitz connection
        @param imageIds:    Image IDs
        @param title:       title to display at top of figure. String
        @param tagIds:      Optional to sort thumbnails by tag. [long]
        @param colCount:    Max number of columns to lay out thumbnails 
        @param length:      Length of longest side of thumbnails
    """
    
    mode = "RGB"
    figCanvas = None
    spacing = length/40 + 2
    
    thumbnailStore = conn.createThumbnailStore()        # returns  omero.api.ThumbnailStorePrx
    metadataService = conn.getMetadataService()
    
    if len(images) == 0:
        return None
    timestampMin = images[0].getDate()   # datetime
    timestampMax = timestampMin
    
    dsImageIds = []
    imagePixelMap = {}
    imageNames = {}
    
    # sort the images by name
    images.sort(key=lambda x:(x.getName().lower()))
    
    for image in images:
        imageId = image.getId()
        pixelId = image.getPrimaryPixels().getId()
        name = image.getName()
        dsImageIds.append(imageId)        # make a list of image-IDs
        imagePixelMap[imageId] = pixelId    # and a map of image-ID: pixel-ID
        imageNames[imageId] = name
        timestampMin = min(timestampMin, image.getDate())
        timestampMax = max(timestampMax, image.getDate())
    
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
            print 'pixelIds', pixelIds
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
    firstdate = timestampMin
    lastdate = timestampMax
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
    
    
def makeThumbnailFigure(conn, scriptParams):    
    """
    Makes the figure using the parameters in @scriptParams, attaches the figure to the 
    parent Project/Dataset, and returns the file-annotation ID
    
    @ returns        Returns the id of the originalFileLink child. (ID object, not value)
    """
        
    log("Thumbnail figure created by OMERO")
    log("")

    parent = None        # figure will be attached to this object 

    imageIds = []
    datasetIds = []
    
    dataType = scriptParams["Data_Type"]
    if dataType == "Image":
        imageIds = scriptParams["IDs"]
        if "Parent_ID" in scriptParams and len(imageIds) > 1:
            pId = scriptParams["Parent_ID"]
            parent = conn.getObject("Dataset", pId)
            if parent:
                log("Figure will be linked to Dataset: %s" % parent.getName())
        if parent == None:
            parent = conn.getObject("Image", imageIds[0])
            if parent:
                log("Figure will be linked to Image: %s" % parent.getName())
                
    else:   # Dataset
        datasetIds = scriptParams["IDs"]
        if "Parent_ID" in scriptParams and len(datasetIds) > 1:
            pId = scriptParams["Parent_ID"]
            parent = conn.getObject("Project", pId)
            if parent:
                log("Figure will be linked to Project: %s" % parent.getName().getValue())
        if parent == None:
            parent = conn.getObject("Dataset", datasetIds[0])
            if parent:
                log("Figure will be linked to Dataset: %s" % parent.getName())
    
    if len(imageIds) == 0 and len(datasetIds) == 0:
        print "No image IDs or dataset IDs found"       
    
    tagIds = []
    if "Tag_IDs" in scriptParams:
        tagIds = scriptParams['Tag_IDs']
    if len(tagIds) == 0:
        tagIds = None
        
    showUntagged = False
    if (tagIds):
        showUntagged = scriptParams["Show_Untagged_Images"]

    thumbSize = scriptParams["Thumbnail_Size"]
    maxColumns = scriptParams["Max_Columns"]

    figHeight = 0
    figWidth = 0
    dsCanvases = []

    for datasetId in datasetIds:
        dataset = conn.getObject("Dataset", datasetId)
        if dataset == None:
            log("No dataset found for ID: %s" % datasetId)
            continue
        datasetName = dataset.getName()
        images = list(dataset.listChildren())
        log("Dataset: %s     ID: %d" % (datasetName, datasetId))
        dsCanvas = paintDatasetCanvas(conn, images, datasetName, tagIds, showUntagged, length=thumbSize, colCount=maxColumns)
        if dsCanvas == None:
            continue
        dsCanvases.append(dsCanvas)
        figHeight += dsCanvas.size[1]
        figWidth = max(figWidth, dsCanvas.size[0])
        
    if len(datasetIds) == 0:
        images = list(conn.getObjects("Image", imageIds))
        imageCanvas = paintDatasetCanvas(conn, images, "", tagIds, showUntagged, length=thumbSize, colCount=maxColumns)
        dsCanvases.append(imageCanvas)
        figHeight += imageCanvas.size[1]
        figWidth = max(figWidth, imageCanvas.size[0])
    
    if len(dsCanvases) == 0:
        return None
    figure = Image.new("RGB", (figWidth, figHeight), WHITE)
    y = 0
    for ds in dsCanvases:
        imgUtil.pasteImage(ds, figure, 0, y)
        y += ds.size[1]
    
    
    log("")
    figLegend = "\n".join(logLines)
    
    format = scriptParams["Format"]
    output = scriptParams["Figure_Name"]
        
    if format == 'PNG':
        output = output + ".png"
        figure.save(output, "PNG")
        mimetype = "image/png"
    else:
        output = output + ".jpg"
        figure.save(output)
        mimetype = "image/jpeg"

    fa = conn.createFileAnnfromLocalFile(output, origFilePathAndName=None, mimetype=mimetype, ns=None, desc=figLegend)
    parent.linkAnnotation(fa)
    
    return fa._obj
        

def runAsScript():
    """
    The main entry point of the script. Gets the parameters from the scripting service, makes the figure and 
    returns the output to the client. 
    def __init__(self, name, optional = False, out = False, description = None, type = None, min = None, max = None, values = None)
    """
        
    formats = [rstring('JPEG'),rstring('PNG')]
    dataTypes = [rstring('Dataset'),rstring('Image')]
    
    client = scripts.client('Thumbnail_Figure.py', """Export a figure of thumbnails, optionally sorted by tag.
NB: OMERO.insight client provides a nicer UI for this script under 'Publishing Options'
See https://www.openmicroscopy.org/site/support/omero4/getting-started/tutorial/exporting-figures""",

        scripts.String("Data_Type", optional=False, grouping="1",
            description="The data you want to work with.", values=dataTypes, default="Dataset"),

        scripts.List("IDs", optional=False, grouping="2",
            description="List of Dataset IDs or Image IDs").ofType(rlong(0)),

        scripts.List("Tag_IDs", grouping="3",
            description="Group thumbnails by these tags."),

        scripts.Bool("Show_Untagged_Images", grouping="3.1", default=False,
            description="If true (and you're sorting by tagIds) also show images without the specified tags"),

        scripts.Long("Parent_ID", grouping="4",
            description="Attach figure to this Project (if datasetIds above) or Dataset if imageIds. If not specifed, attach figure to first dataset or image."),
            # this will be ignored if only a single ID in list - attach to that object instead. 

        scripts.Int("Thumbnail_Size", grouping="5", min=10, max=250, default=100,
            description="The dimension of each thumbnail. Default is 100"),

        scripts.Int("Max_Columns", grouping="5.1", min=1, default=10,
            description="The max number of thumbnail columns. Default is 10"),

        scripts.String("Format", grouping="6",
            description="Format to save image.", values=formats, default="JPEG"),

        scripts.String("Figure_Name", grouping="6.1", default='Thumbnail_Figure',
            description="File name of figure to create"),

        version = "4.3.0",
        authors = ["William Moore", "OME Team"],
        institutions = ["University of Dundee"],
        contact = "ome-users@lists.openmicroscopy.org.uk",
        )

    try:
        session = client.getSession()
        commandArgs = {}
        
        conn = BlitzGateway(client_obj=client)

        for key in client.getInputKeys():
            if client.getInput(key):
                commandArgs[key] = unwrap(client.getInput(key))
        print commandArgs

        # Makes the figure and attaches it to Project/Dataset. Returns FileAnnotationI object
        fileAnnotation = makeThumbnailFigure(conn, commandArgs)
        if fileAnnotation:
            print fileAnnotation
            client.setOutput("Message", rstring("Thumbnail-Figure Created"))
            client.setOutput("File_Annotation", robject(fileAnnotation))
        else:
            client.setOutput("Message", rstring("Thumbnail-Figure Failed. See error or info"))
    finally:
        client.closeSession()

if __name__ == "__main__":
    runAsScript()
