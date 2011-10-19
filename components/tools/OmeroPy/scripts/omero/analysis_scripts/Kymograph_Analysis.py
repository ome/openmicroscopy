#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
 components/tools/OmeroPy/scripts/omero/analysis_scripts/Kymograph_Analysis.py

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

This script is the second Kymograph script, for analyzing lines drawn on
kymograph images that have been created by the 'Kymograph.py' Script. 


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
from omero.rtypes import *
import omero.scripts as scripts
import math
import os


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


def processImages(conn, scriptParams):

    fileAnns = []
    imageIds = scriptParams['IDs']
    for image in conn.getObjects("Image", imageIds):

        print "\nAnalysing Image: %s ID: %s" % (image.getName(), image.getId())
        
        if image.getSizeT() > 1:
            print "  This appears to be a time-lapse Image, not a kymograph"
            continue
        
        roiService = conn.getRoiService()
        result = roiService.findByImage(image.getId(), None)
        
        secsPerPixelY = image.getPixelSizeY()
        micronsPerPixelX = image.getPixelSizeX()
        if secsPerPixelY and micronsPerPixelX:
            micronsPerSec = micronsPerPixelX / secsPerPixelY
        else: micronsPerSec = None
        
        # for each line or polyline, create a row in csv table: y(t), x, dy(dt), dx, x/t (line), x/t (average)
        colNames = "\ny_start, x_start, y_end, x_end, dy, dx, x/y, average x/y, speed(um/sec)"
        tableData = ""
        for roi in result.rois:
            for s in roi.copyShapes():
                if type(s) == omero.model.LineI:
                    tableData += "\nLine"
                    x1 = s.getX1().getValue()
                    x2 = s.getX2().getValue()
                    y1 = s.getY1().getValue()
                    y2 = s.getY2().getValue()
                    dx = abs(x1-x2)
                    dy = abs(y1-y2)
                    dxPerY = float(dx)/dy
                    tableData += "\n"
                    tableData += ",".join([str(x) for x in (y1, x1, y2, x2, dy, dx, dxPerY, dxPerY, "")])
                    if micronsPerSec:
                        speed = dxPerY * micronsPerSec
                        tableData += "%s" % speed
            
                elif type(s) == omero.model.PolylineI:
                    tableData += "\nPolyline"
                    points = pointsStringToXYlist(s.getPoints().getValue())
                    xStart, yStart = points[0]
                    for i in range(1, len(points)):
                        x1, y1 = points[i-1]
                        x2, y2 = points[i]
                        dx = abs(x1-x2)
                        dy = abs(y1-y2)
                        dxPerY = float(dx)/dy
                        avXperY = abs(float(x2-xStart)/(y2-yStart))
                        tableData += "\n"
                        tableData += ",".join([str(x) for x in (y1, x1, y2, x2, dy, dx, dxPerY, avXperY, "")])
                        if micronsPerSec:
                            speed = dxPerY * micronsPerSec
                            tableData += "%s" % speed

        # write table data to csv...
        if len(tableData) > 0:
            tableString = "secsPerPixelY: %s" % secsPerPixelY
            tableString += '\nmicronsPerPixelX: %s' % micronsPerPixelX
            tableString += "\nmicronsPerSec: %s" % micronsPerSec
            tableString += "\n"
            tableString += colNames 
            tableString += tableData
            print tableString
            csvFileName = 'kymograph_velocities_%s.csv' % image.getId()
            csvFile = open(csvFileName, 'w')
            try:
                csvFile.write(tableString)
            finally:
                csvFile.close()

            fileAnn = conn.createFileAnnfromLocalFile(csvFileName, mimetype="text/csv", desc=None)
            fileAnns.append(fileAnn)
            image.linkAnnotation(fileAnn)
        else:
            print "Found NO lines or polylines to analyse for Image"

    return fileAnns
                        

if __name__ == "__main__":

    dataTypes = [rstring('Image')]

    client = scripts.client('Kymograph.py', """This script processes Images, which have Line or PolyLine ROIs to create kymographs.
Kymographs are created in the form of new OMERO Images, with single Z and T, same sizeC as input.""",

    scripts.String("Data_Type", optional=False, grouping="1",
        description="Choose source of images (only Image supported)", values=dataTypes, default="Image"),

    scripts.List("IDs", optional=False, grouping="2",
        description="List of Image IDs to process.").ofType(rlong(0)),

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

        fileAnns = processImages(conn, scriptParams)
        
        if len(fileAnns) == 1:
            client.setOutput("Message", rstring("Created analysis csv (Excel) file attached to kymograph"))
            client.setOutput("Kymograph_Analysis", robject(fileAnns[0]._obj))
        elif len(fileAnns) > 1:
            client.setOutput("Message", rstring("Created %s csv (Excel) files attached to kymographs" % len(fileAnns)))
        else:
            client.setOutput("Message", rstring("No Analysis files created. See 'Info' or 'Errror' for more details"))
        
    finally:
        client.closeSession()
