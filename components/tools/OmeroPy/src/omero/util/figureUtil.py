#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
 components/tools/OmeroPy/src/omero/util/figureUitl.py

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

A collection of utility methods used by Figure scripts for producing 
publication type of figures. 

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

try:
    from PIL import Image, ImageDraw, ImageFont # see ticket:2597
except ImportError:
    import Image, ImageDraw, ImageFont # see ticket:2597

WHITE = (255,255,255)

SECS_MILLIS = "SECS_MILLIS"
SECS = "SECS"
MINS = "MINS"
HOURS = "HOURS"
MINS_SECS = "MINS_SECS"
HOURS_MINS = "HOURS_MINS"
HOURS_MINS_SECS = "HOURS_MINS_SECS"
HOURS_MINS_SECS_MILLIS = "HOURS_MINS_SECS_MILLIS"
TIME_UNITS = [SECS_MILLIS, SECS, MINS, HOURS, MINS_SECS, HOURS_MINS, HOURS_MINS_SECS, HOURS_MINS_SECS_MILLIS]

def getDatasetsProjectsFromImages(queryService, imageIds):
    """ 
    Query returns a map where each key is the imageId and the value is a list of (projectName, datasetName) tuples. 
    If the image does not have a Dataset AND Project, the map will hold an empty list for that imageId. 
    
    @param queryService:     The Omero query service
    @param imageIds:        A list of image IDs. [long]
    @return:            A map imageId:[(projectName, datasetName)]
    """
    ids = ",".join([str(i) for i in imageIds])
    
    query_string = "select i from Image i join fetch i.datasetLinks idl join fetch idl.parent d join fetch d.projectLinks pl join fetch pl.parent where i.id in (%s)" % ids

    images = queryService.findAllByQuery(query_string, None)
    results = {}
    
    for i in images:    # order of images not same as imageIds
        pdList = []
        imageId = i.getId().getValue()
        for link in i.iterateDatasetLinks():
            dataset = link.parent
            dName = dataset.getName().getValue()
            if dataset.sizeOfProjectLinks() == 0:
                pdList.append(("", dName))
            for dpLink in dataset.iterateProjectLinks():
                project = dpLink.parent
                pName = project.getName().getValue()
                pdList.append((pName, dName))
        results[imageId] = pdList
    
    # make sure the map contains all the imageIds
    for iId in imageIds:
        if iId not in results:
            results[iId] = []
    return results
    

def getTagsFromImages(metadataService, imageIds):
    """ 
    Query returns a map of key = imageId, value = [tagNames] for the image
    
    @param metadataService:        The Omero metadata service
    @param imageIds:            A list of image IDs. [long]
    @return:                A map of imageId:[tagName]
    """
    
    types = ["ome.model.annotations.TagAnnotation"]
    annotations = metadataService.loadAnnotations("Image", imageIds, types, None, None)
    
    tagsMap = {}
    for i in imageIds:
        annots = annotations[i]
        tags = [a.getTextValue().getValue() for a in annots]
        tagsMap[i] = tags
    return tagsMap
    
    
def getTimes(queryService, pixelsId, tIndexes, theZ=None, theC=None):
    """
    Get the time in seconds (float) for the first plane (C = 0 & Z = 0) at 
    each time-point for the defined pixels.
    Returns a map of tIndex: timeInSecs
    
    @param queryService:    The Omero queryService
    @param pixelsId:        The ID of the pixels object. long
    @param tIndexes:        List of time indexes. [int]
    @param theZ:        The Z plane index. Default is 0
    @param theC:        The Channel index. Default is 0
    @return:            A map of tIndex: timeInSecs
    """
    if theZ == None:
        theZ = 0
    if theC == None:
        theC = 0
    indexes = ",".join([str(t) for t in tIndexes])
    query = "from PlaneInfo as Info where Info.theT in (%s) and Info.theZ in (%d) and Info.theC in (%d) and pixels.id='%d'" % (indexes, theZ, theC, pixelsId)
    infoList = queryService.findAllByQuery(query,None)
    timeMap = {}
    for info in infoList:
        tIndex = info.theT.getValue()
        time = info.deltaT.getValue() 
        timeMap[tIndex] = time
    return timeMap    
    
    
def formatTime(seconds, timeUnits):
    """
    Returns a string formatting of the time (in seconds)
    according to the chosen timeUnits: "SECS_MILLIS", "SECS", "MINS", "HOURS", "MINS_SECS", "HOURS_MINS", HOURS_MINS_SECS, HOURS_MINS_SECS_MILLIS
    
    @param seconds:        Time in seconds. float or int
    @param timeUnits:    A string denoting the format. One of the choices above. 
    @return:        A string, such as "10" or "3:20"    
    """
    neg = False
    if seconds < 0: 
        seconds = seconds * -1
        neg = True
    label = None
    if timeUnits == "SECS_MILLIS":
        label = "%.2f" % seconds
    elif timeUnits == "SECS":
        label = "%d" % int(round(seconds))
    elif timeUnits == "MINS":
        mins = float(seconds) / float(60)
        label = "%d" % int(round(mins))
    elif timeUnits == "HOURS":
        hrs = float(seconds) / float(3600)
        label = "%d" % int(round(hrs))
    elif timeUnits == "MINS_SECS":
        mins = seconds / 60
        secs = round(seconds % 60)
        label = "%d:%02d" % (mins, secs)
    elif timeUnits == "HOURS_MINS":
        hrs = seconds / 3600
        mins = round((seconds % 3600)/60)
        label = "%d:%02d" % (hrs, mins)
    elif timeUnits == "HOURS_MINS_SECS":
        hrs = seconds / 3600
        mins = (seconds % 3600)/60
        secs = round(seconds % (3600 * 60))
        label = "%d:%02d:%02d" % (hrs, mins, secs)
    elif timeUnits == "HOURS_MINS_SECS_MILLIS":
        hrs = seconds / 3600
        mins = (seconds % 3600)/60
        secs = (seconds % (3600 * 60))
        label = "%d:%02d:%05.2f" % (hrs, mins, secs)
    else:
        label = "%.2f sec" % seconds
    return neg and "-%s"%label or label
    

def getTimeLabels(queryService, pixelsId, tIndexes, sizeT, timeUnits = None, showRoiDuration = False):
    """
    Returns a list of time labels e.g. "10", "20" for the first plane at 
    each t-index (C=0 and Z=0). If no planeInfo is available, returns plane number/total e.g "3/10"
    If time units are not specified, the most suitable units are chosen based on the max time. 
    The list of label returned includes the timeUnits as the last string in the list, in case you didn't specify it. 
    
    @param queryService:        The Omero query service
    @param pixelsId:            The ID of the pixels you want info for
    @param tIndexes:            List of t-index to get the times for. Assumed to be in t order. 
    @param sizeT:                The T dimension size of the pixels. Used if no plane info
    @param timeUnits:        Format choice of "SECS", "MINS", "HOURS", "MINS_SECS", "HOURS_MINS". String
    @param showRoiDuration:        if true, times shown are from the start of the ROI frames, otherwise use movie timestamp.
    @return:                A list of strings, ordered same as tIndexes
    """
    secondsMap = getTimes(queryService, pixelsId, tIndexes)
    
    if timeUnits == None and len(secondsMap) > 0:
        maxSecs = max(secondsMap.values())
        if maxSecs > 3600: timeUnits = HOURS_MINS
        elif maxSecs > 60: timeUnits = MINS_SECS
        else: timeUnits = SECS_MILLIS
    
    labels = []
    for t in tIndexes:
        if t in secondsMap:
            seconds = secondsMap[t]
            if showRoiDuration:
                seconds = seconds - secondsMap[tIndexes[0]]
            labels.append(formatTime(seconds,timeUnits))
        else:
            labels.append("%d/%d" % (t+1, sizeT))
            
    labels.append(timeUnits)
    return labels

def addScalebar(scalebar, xIndent, yIndent, image, pixels, colour):
    """
    Adds a scalebar at the bottom right of an image, No text.

    @param scalebar     length of scalebar in microns
    @param xIndent      indent from the right of the image
    @param yIndent      indent from the bottom of the image
    @param image        the PIL image to add scalebar to
    @param pixels       the pixels object
    @param colour       colour of the overlay as r,g,b tuple
    """
    draw = ImageDraw.Draw(image)
    if pixels.getPhysicalSizeX() == None:
        return False, "  Failed to add scale bar: Pixel size not defined."
    pixelSizeX = pixels.getPhysicalSizeX().getValue()
    if pixelSizeX <= 0:
        return False, "  Failed to add scale bar: Pixel size not defined."
    iWidth, iHeight = image.size
    lineThickness = (iHeight//100) + 1
    scaleBarY = iHeight - yIndent
    scaleBarX = iWidth - scalebar//pixelSizeX - xIndent
    scaleBarX2 = iWidth - xIndent
    if scaleBarX<=0 or scaleBarX2<=0 or scaleBarY<=0 or scaleBarX2>iWidth:
        return False, "  Failed to add scale bar: Scale bar is too large."
    for l in range(lineThickness):
        draw.line([(scaleBarX,scaleBarY), (scaleBarX2,scaleBarY)], fill=colour)
        scaleBarY -= 1
    return True,  "  Scalebar added to the image."

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
