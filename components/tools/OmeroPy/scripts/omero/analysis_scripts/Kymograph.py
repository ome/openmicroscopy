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
@version 4.3.3
<small>
(<b>Internal version:</b> $Revision: $Date: $)
</small>
@since 3.0-Beta4.3.3
"""

from omero.gateway import BlitzGateway
import omero
from omero.util.imageUtil import getLineData
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


def polyLineKymograph(conn, scriptParams, image, polylines, lineWidth, dataset):
    """
    Creates a new kymograph Image from one or more polylines.
    
    @param polylines:       map of theT: {theZ:theZ, points: list of (x,y)}
    """
    pixels = image.getPrimaryPixels()
    sizeC = image.getSizeC()
    sizeT = image.getSizeT()
    
    use_all_times = "Use_All_Timepoints" in scriptParams and scriptParams['Use_All_Timepoints'] is True
    if len(polylines) == 1:
        use_all_times = True

    # for now, assume we're using ALL timepoints
    # need the first shape
    firstShape = None
    for t in range(sizeT):
        if t in polylines:
            firstShape = polylines[t]
            break
    
    print "\nCreating Kymograph image from 'polyline' ROI. First polyline:", firstShape
    
    def planeGen():
        """ Final image is single Z and T. Each plane is rows of T-slices """
        for theC in range(sizeC):
            shape = firstShape
            tRows = []
            for theT in range(sizeT):
                # update shape if specified for this timepoint
                if theT in polylines:
                    shape = polylines[theT]
                elif not use_all_times:
                    continue
                lineData = []
                points = shape['points']
                theZ = shape['theZ']
                for l in range(len(points)-1):
                    x1, y1 = points[l]
                    x2, y2 = points[l+1]
                    ld = getLineData(pixels, x1,y1,x2,y2, lineWidth, theZ, theC, theT)
                    lineData.append(ld)
                rowData = hstack(lineData)
                tRows.append( rowData )
            
            # have to handle any mismatch in line lengths by padding shorter rows
            longest = max([row_array.shape[1] for row_array in tRows])
            for t in range(len(tRows)):
                t_row = tRows[t]
                row_height, row_length = t_row.shape
                if row_length < longest:
                    padding = longest - row_length
                    pad_data = zeros( (row_height,padding), dtype=t_row.dtype)
                    tRows[t] = hstack([t_row, pad_data])
            cData = vstack(tRows)
            yield cData

    desc = "Kymograph generated from Image ID: %s, polyline: %s" % (image.getId(), firstShape['points'])
    desc += "\nwith each timepoint being %s vertical pixels" % lineWidth
    newImg = conn.createImageFromNumpySeq(planeGen(), "kymograph", 1, sizeC, 1, description=desc, dataset=dataset)
    return newImg


def linesKymograph(conn, scriptParams, image, lines, lineWidth, dataset):
    """
    Creates a new kymograph Image from one or more lines.
    If one line, use this for every time point.
    If multiple lines, use the first one for length and all the remaining ones for x1,y1 and direction,
    making all subsequent lines the same length as the first. 
    """
    
    pixels = image.getPrimaryPixels()
    sizeC = image.getSizeC()
    sizeT = image.getSizeT()

    use_all_times = "Use_All_Timepoints" in scriptParams and scriptParams['Use_All_Timepoints'] is True
    if len(lines) == 1:
        use_all_times = True

    # need the first shape - Going to make all lines this length
    firstLine = None
    for t in range(sizeT):
        if t in lines:
            firstLine = lines[t]
            break
    
    print "\nCreating Kymograph image from 'line' ROI. First line:", firstLine

    def planeGen():
        """ Final image is single Z and T. Each plane is rows of T-slices """
        for theC in range(sizeC):
            shape = firstLine
            r_length = None           # set this for first line
            tRows = []
            for theT in range(sizeT):
                if theT in lines:
                    shape = lines[theT]
                elif not use_all_times:
                    continue
                theZ = shape['theZ']
                x1,y1,x2,y2 = shape['x1'], shape['y1'], shape['x2'], shape['y2']
                rowData = getLineData(pixels, x1,y1,x2,y2, lineWidth, theZ, theC, theT)
                # if the row is too long, crop - if it's too short, pad
                row_height, row_length = rowData.shape
                if r_length is None:  r_length = row_length
                if row_length < r_length:
                    padding = r_length - row_length
                    pad_data = zeros( (row_height,padding), dtype=rowData.dtype)
                    rowData = hstack([rowData, pad_data])
                elif row_length > r_length:
                    rowData = rowData[:, 0:r_length]
                tRows.append( rowData )
            yield vstack(tRows)
    
    desc = "Kymograph generated from Image ID: %s, line: %s" % (image.getId(), firstLine)
    desc += "\nwith each timepoint being %s vertical pixels" % lineWidth
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

        roiService = conn.getRoiService()
        result = roiService.findByImage(image.getId(), None)
        
        # kymograph strategy - Using Line and Polyline ROIs:
        # NB: Use ALL time points unless >1 shape AND 'use_all_timepoints' = False
        # If > 1 shape per time-point (per ROI), pick one!
        # 1 - Single line. Use this shape for all time points
        # 2 - Many lines. Use the first one to fix length. Subsequent lines to update start and direction
        # 3 - Single polyline. Use this shape for all time points
        # 4 - Many polylines. Use the first one to fix length. 
        for roi in result.rois:
            lines = {}          # map of theT: line
            polylines = {}      # map of theT: polyline
            for s in roi.copyShapes():
                theZ = s.getTheZ() and s.getTheZ().getValue() or 0
                theT = s.getTheT() and s.getTheT().getValue() or 0
                # TODO: Add some filter of shapes. E.g. text? / 'lines' only etc.
                if type(s) == omero.model.LineI:
                    x1 = s.getX1().getValue()
                    x2 = s.getX2().getValue()
                    y1 = s.getY1().getValue()
                    y2 = s.getY2().getValue()
                    lines[theT] = {'theZ':theZ, 'x1':x1, 'y1':y1, 'x2':x2, 'y2':y2}
            
                elif type(s) == omero.model.PolylineI:
                    points = pointsStringToXYlist(s.getPoints().getValue())
                    polylines[theT] = {'theZ':theZ, 'points': points}


            if len(lines) > 0:
                newImg = linesKymograph(conn, scriptParams, image, lines, lineWidth, dataset)
                newImages.append(newImg)
                lines = []
            elif len(polylines) > 0:
                newImg = polyLineKymograph(conn, scriptParams, image, polylines, lineWidth, dataset)
                newImages.append(newImg)
            else:
                print "ROI: %s had no lines or polylines" % roi.getId().getValue()
        
        
        # look-up the interval for each time-point
        tInterval = None
        infos = list (pixels.copyPlaneInfo(theC=0, theT=sizeT-1, theZ=0))
        if len(infos) > 0:
            duration = infos[0].deltaT
            print "duration", duration
            tInterval = duration/(sizeT-1)
        elif pixels.timeIncrement is not None:
            print "pixels.timeIncrement", pixels.timeIncrement
            tInterval = pixels.timeIncrement
        elif "Time_Increment" in scriptParams:
            tInterval = scriptParams["Time_Increment"]
        
        pixel_size = None
        if pixels.physicalSizeX is not None:
            pixel_size = pixels.physicalSizeX
        elif "Pixel_Size" in scriptParams:
            pixel_size = scriptParams['Pixel_Size']
        
        
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
            
            # If we know pixel sizes, set them on the new image
            if pixel_size is not None or tInterval is not None:
                px = conn.getQueryService().get("Pixels", img.getPixelsId())
                if pixel_size is not None:
                    px.setPhysicalSizeX(rdouble(pixel_size))
                if tInterval is not None:
                    t_per_pixel = tInterval / lineWidth
                    px.setPhysicalSizeY(rdouble(t_per_pixel))
                conn.getUpdateService().saveObject(px)
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

    scripts.Int("Line_Width", optional=False, grouping="3", default=4,
        description="Width in pixels of each time slice", min=1),
    
    scripts.Bool("Use_All_Timepoints", grouping="4", default=True,
        description="Use every timepoint in the kymograph. If False, only use timepoints with ROI-shapes"),

    scripts.Float("Time_Increment", grouping="5",
        description="If source movie has no time info, specify increment per time point (secs)"),

    scripts.Float("Pixel_Size", grouping="6",
        description="If source movie has no Pixel size info, specify pixel size (microns)"),

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
