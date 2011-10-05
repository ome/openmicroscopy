#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
 components/tools/OmeroPy/scripts/omero/analysis_scripts/Kymograph.py

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

This script processes Images, which have Line or PolyLine ROIs to create kymographs.
Kymographs are created in the form of new OMERO Images, single Z and T, same sizeC as input.


@author Will Moore
<a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
@version 4.3.2
<small>
(<b>Internal version:</b> $Revision: $Date: $)
</small>
@since 3.0-Beta4.3.2
"""

from omero.gateway import BlitzGateway
import omero
from omero.rtypes import *
import omero.scripts as scripts
from cStringIO import StringIO
import math
from numpy import *
try:
    from PIL import Image
except ImportError:
    import Image


def pointsStringToXYlist(string):
    """
    Method for converting the string returned from omero.model.ShapeI.getPoints()
    into list of (x,y) points.
    E.g: "points[309,427, 366,503, 190,491] points1[309,427, 366,503, 190,491] points2[309,427, 366,503, 190,491]"
    """
    pointLists = string.strip().split("points")
    if len(pointLists) < 2:
        logger.error("Unrecognised ROI shape 'points' string: %s" % string)
        return ""
    firstList = pointLists[1]
    xyList = []
    for xy in firstList.strip(" []").split(", "):
        x, y = xy.split(",")
        xyList.append( ( int( x.strip() ), int(y.strip() ) ) )
    return xyList
    
def numpyToImage(plane, cMinMax):
    """
    Converts the numpy plane to a PIL Image, scaling to cMinMax (minVal, maxVal) and changing data type if needed.
    Need plane dtype to be uint8 (or int8) for conversion to tiff by PIL
    """
    if plane.dtype.name not in ('uint8', 'int8'):      # we need to scale...
        minVal, maxVal = cMinMax
        valRange = maxVal - minVal
        scaled = (plane - minVal) * (float(255) / valRange)
        convArray = zeros(plane.shape, dtype=uint8)
        convArray += scaled
        #print "using converted int8 plane: dtype: %s min: %s max: %s" % (convArray.dtype.name, convArray.min(), convArray.max())
        return Image.fromarray(convArray)
    return Image.fromarray(plane)

    
def getLineData(pixels, x1,y1,x2,y2, cMinMax, lineW=2, theZ=0, theC=0, theT=0):
    """
    Grabs pixel data covering the specified line, and rotates it horizontally so that x1,y1 is to the left.
    Uses PIL to handle rotating and interpolating the data. Converts to numpy to PIL and back (may change dtype.)
    cMinMax is the (min, max) values of Channel pixel data, to use for scaling numpy to PIL. 
    """
    
    centreX = (x1+x2)/2
    centreY = (y1+y2)/2
    lineX = x2-x1
    lineY = y2-y1

    rads = math.atan(float(lineX)/lineY)
    print "Leaning over by degrees:", math.degrees(rads)

    # How much extra Height do we need, top and bottom?
    extraH = abs(math.sin(rads) * lineW)
    print "Need extraH", extraH
    bottom = max(y1,y2) + extraH/2
    top = min(y1,y2) - extraH/2

    # How much extra width do we need, left and right?
    extraW = abs(math.cos(rads) * lineW)
    print "Need extraW", extraW
    left = min(x1,x2) - extraW
    right = max(x1,x2) + extraW

    # What's the larger area we need?
    x = int(left)
    y = int(top)
    w = int(right - left)
    h = int(bottom - top)
    tile = (x, y, w, h)
    plane = pixels.getTile(theZ, theC, theT, tile)
    pil = numpyToImage(plane, cMinMax)
    #pil.show()

    # Now need to rotate so that x1,y1 is horizontally to the left of x2,y2
    toRotate = 90 - math.degrees(rads)
    print "To straighten:", toRotate

    if x1 > x2:
        toRotate += 180
    print "To rotate fully:", toRotate
    rotated = pil.rotate(toRotate, expand=True)  # filter=Image.BICUBIC see http://www.ncbi.nlm.nih.gov/pmc/articles/PMC2172449/
    #rotated.show()

    # finally we need to crop to the length of the line
    length = int(math.sqrt(math.pow(lineX, 2) + math.pow(lineY, 2)))
    rotW, rotH = rotated.size
    cropX = (rotW - length)/2
    cropX2 = cropX + length
    cropY = (rotH - lineW)/2
    cropY2 = cropY + lineW
    print "Cropping: cropX, cropY, cropX2, cropY2 ", cropX, cropY, cropX2, cropY2
    cropped = rotated.crop( (cropX, cropY, cropX2, cropY2))
    #cropped.show()
    return asarray(cropped)


def polyLineKymograph(conn, image, points, theZ, channelMinMax, lineWidth, dataset):
    """
    Creates a new kymograph Image from a single polyLine (list of points)
    
    @param points:  list of (x,y)
    @param theZ:    Assume all Time-points on a single Z section
    """
    pixels = image.getPrimaryPixels()
    sizeC = image.getSizeC()
    sizeT = image.getSizeT()

    def planeGen():
        """ Final image is single Z and T. Each plane is rows of T-slices """
        for theC in range(sizeC):
            tRows = []
            cMinMax = channelMinMax[theC]
            for theT in range(sizeT):
                # make a row by joining each line of polyline
                lineData = []
                for l in range(len(points)-1):
                    x1, y1 = points[l]
                    x2, y2 = points[l+1]
                    ld = getLineData(pixels, x1,y1,x2,y2, cMinMax, lineWidth, theZ, theC, theT)
                    print " Line data: Z,C,T, shape",theZ,theC,theT,ld.shape 
                    lineData.append(ld)
                rowData = hstack(lineData)
                print "Row data: shape", rowData.shape
                tRows.append( rowData )
            
            cData = vstack(tRows)
            print "Channel Data: shape", cData.shape
            yield cData

    desc = "Kymograph generated from Image ID: %s with each timepoint being %s vertical pixels" % (image.getId(), lineWidth)
    newImg = conn.createImageFromNumpySeq(planeGen(), "kymograph", 1, sizeC, 1, description=desc, dataset=dataset)
    return newImg

def linesKymograph(conn, image, lines, channelMinMax, lineWidth, dataset):
    """
    Creates a new kymograph Image from one or more lines.
    If one line, use this for every time point.
    If multiple lines, use the first one for length and all the remaining ones for x1,y1 and direction,
    making all subsequent lines the same length as the first. 
    """
    
    if len(lines) == 1:
        pixels = image.getPrimaryPixels()
        sizeC = image.getSizeC()
        sizeT = image.getSizeT()
        l = lines[0]
        theZ = l['theZ']
        x1,y1,x2,y2 = l['x1'], l['y1'], l['x2'], l['y2']
        
        def planeGen():
            """ Final image is single Z and T. Each plane is rows of T-slices """
            for theC in range(sizeC):
                tRows = []
                cMinMax = channelMinMax[theC]
                for theT in range(sizeT):
                    # make a row by joining each line of polyline
                    rowData = getLineData(pixels, x1,y1,x2,y2, cMinMax, lineWidth, theZ, theC, theT)
                    tRows.append( rowData )
                yield vstack(tRows)
        
        desc = "Kymograph generated from Image ID: %s with each timepoint being %s vertical pixels" % (image.getId(), lineWidth)
        newImg = conn.createImageFromNumpySeq(planeGen(), "kymograph", 1, sizeC, 1, description=desc, dataset=dataset)
        return newImg
    

def processImages(conn, scriptParams):
    
    lineWidth = scriptParams['Line_Width']
    imageIds = scriptParams['IDs']
    newKymographs = []
    for image in conn.getObjects("Image", imageIds):
        
        newImages = []      # kymographs derived from the current image.
        cNames = []
        colors = []
        for ch in image.getChannels():
            cNames.append(ch.getLabel())
            colors.append(ch.getColor().getRGB())

        sizeT = image.getSizeT()
        sizeX = image.getSizeX()
        sizeY = image.getSizeY()
        sizeZ = image.getSizeZ()
        sizeC = image.getSizeC()
        pixels = image.getPrimaryPixels()

        dataset = image.getDataset()

        channelMinMax = []
        for c in image.getChannels():
            minC = c.getWindowMin()
            maxC = c.getWindowMax()
            channelMinMax.append((minC, maxC))

        roiService = conn.getRoiService()
        result = roiService.findByImage(image.getId(), None)
        for roi in result.rois:
            lines = []
            for s in roi.copyShapes():
                theZ = s.getTheZ() and s.getTheZ().getValue() or 0
                theT = s.getTheT() and s.getTheT().getValue() or 0
                # TODO: Add some filter of shapes. E.g. text? / 'lines' only etc.
                if type(s) == omero.model.LineI:
                    x1 = s.getX1().getValue()
                    x2 = s.getX2().getValue()
                    y1 = s.getY1().getValue()
                    y2 = s.getY2().getValue()
                    lines.append({'theT':theT, 'theZ':theZ, 'x1':x1, 'y1':y1, 'x2':x2, 'y2':y2})
            
                elif type(s) == omero.model.PolylineI:
                    points = pointsStringToXYlist(s.getPoints().getValue())
                    newImg = polyLineKymograph(conn, image, points, theZ, channelMinMax, lineWidth, dataset)
                    newImages.append(newImg)
                    # TODO: set new channel names, colors, pixel sizes. 
                    break       # only interested in the first polyline

            print lines
            if len(lines) > 0:
                newImg = linesKymograph(conn, image, lines, channelMinMax, lineWidth, dataset)
                newImages.append(newImg)
                lines = []
        
        # Save channel names and colors for each new image
        for img in newImages:
            print "Applying channel Names:", cNames, " Colors:", colors
            for i, c in enumerate(img.getChannels()):
                lc = c.getLogicalChannel()
                lc.setName(cNames[i])
                lc.save()
                r, g, b = colors[i]
                # need to reload channels to avoid optimistic lock on update
                cObj = conn.getQueryService().get("Channel", c.id)
                cObj.red = omero.rtypes.rint(r)
                cObj.green = omero.rtypes.rint(g)
                cObj.blue = omero.rtypes.rint(b)
                cObj.alpha = omero.rtypes.rint(255)
                conn.getUpdateService().saveObject(cObj)
            img.resetRDefs()  # reset based on colors above
        newKymographs.extend(newImages)
    
    return newKymographs

if __name__ == "__main__":

    dataTypes = [rstring('Image')]

    client = scripts.client('Kymograph.py', """This script processes Images, which have Line or PolyLine ROIs to create kymographs.
Kymographs are created in the form of new OMERO Images, with single Z and T, same sizeC as input.""",

    scripts.String("Data_Type", optional=False, grouping="1",
        description="Choose source of images (only Image supported)", values=dataTypes, default="Image"),

    scripts.List("IDs", optional=False, grouping="2",
        description="List of Image IDs to process.").ofType(rlong(0)),

    scripts.Int("Line_Width", optional=False, grouping="3", default=10,
        description="Width in pixels of each time slice", min=1),

    version = "4.3.3",
    authors = ["William Moore", "OME Team"],
    institutions = ["University of Dundee"],
    contact = "ome-users@lists.openmicroscopy.org.uk",
    )

    try:
        # process the list of args above.
        scriptParams = {}
        for key in client.getInputKeys():
            if client.getInput(key):
                scriptParams[key] = client.getInput(key, unwrap=True)

        print scriptParams

        # wrap client to use the Blitz Gateway
        conn = BlitzGateway(client_obj=client)

        newImages = processImages(conn, scriptParams)

        if len(newImages) == 1:
            client.setOutput("Message", rstring("Script Ran OK. Created a Kymograph Image"))
            client.setOutput("New_Image",robject(newImages[0]._obj))
        elif len(newImages) > 1:
            client.setOutput("Message", rstring("Script Ran OK. %d Kymographs created" % len(newImages) ))
            client.setOutput("First_Image",robject(newImages[0]._obj))  # return the first one
        else:
            client.setOutput("Message", rstring("No kymographs created. See 'Error' or 'Info' for details"))
    finally:
        client.closeSession()
