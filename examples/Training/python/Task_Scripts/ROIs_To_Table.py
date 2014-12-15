#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2011 University of Dundee & Open Microscopy Environment.
#                    All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
#

import omero.scripts as scripts
from random import random
import math
from numpy import array
from omero.gateway import BlitzGateway
import omero
from omero.rtypes import rstring, rdouble, rlong


def processData(conn, scriptParams):
    """
    For each Dataset, process each Image adding the length of each ROI line to
    an OMERO.table.
    Also calculate the average of all lines for each Image and add this as a
    Double Annotation on Image.
    """

    datasetIds = scriptParams['IDs']
    for dataset in conn.getObjects("Dataset", datasetIds):

        # first create our table...
        # columns we want are: imageId, roiId, shapeId, theZ, theT,
        # lineLength, shapetext.
        columns = [
            omero.grid.LongColumn('imageId', '', []),
            omero.grid.RoiColumn('roidId', '', []),
            omero.grid.LongColumn('shapeId', '', []),
            omero.grid.LongColumn('theZ', '', []),
            omero.grid.LongColumn('theT', '', []),
            omero.grid.DoubleColumn('lineLength', '', []),
            omero.grid.StringColumn('shapeText', '', 64, [])
            ]
        # create and initialize the table
        table = conn.c.sf.sharedResources().newTable(
            1, "LineLengths%s" % str(random()))
        table.initialize(columns)

        # make a local array of our data (add it to table in one go)
        imageIds = []
        roiIds = []
        shapeIds = []
        theZs = []
        theTs = []
        lineLengths = []
        shapeTexts = []
        roiService = conn.getRoiService()
        lengthsForImage = []
        imgCount = 0
        for image in dataset.listChildren():
            imgCount += 1
            result = roiService.findByImage(image.getId(), None)
            for roi in result.rois:
                for s in roi.copyShapes():
                    if type(s) == omero.model.LineI:
                        imageIds.append(image.getId())
                        roiIds.append(roi.getId().getValue())
                        shapeIds.append(s.getId().getValue())
                        theZs.append(s.getTheZ().getValue())
                        theTs.append(s.getTheT().getValue())
                        x1 = s.getX1().getValue()
                        x2 = s.getX2().getValue()
                        y1 = s.getY1().getValue()
                        y2 = s.getY2().getValue()
                        x = x1 - x2
                        y = y1 - y2
                        length = math.sqrt(math.pow(x, 2) + math.pow(y, 2))
                        lineLengths.append(length)
                        lengthsForImage.append(length)
                        if s.getTextValue():
                            shapeTexts.append(s.getTextValue().getValue())
                        else:
                            shapeTexts.append("")
            if len(lengthsForImage) == 0:
                print "No lines found on Image:", image.getName()
                continue
            imgAverage = sum(lengthsForImage) / len(lengthsForImage)
            print ("Average length of line for Image: %s is %s"
                   % (image.getName(), imgAverage))

            # Add the average as an annotation on each image.
            lengthAnn = omero.model.DoubleAnnotationI()
            lengthAnn.setDoubleValue(rdouble(imgAverage))
            lengthAnn.setNs(
                rstring("imperial.training.demo.lineLengthAverage"))
            link = omero.model.ImageAnnotationLinkI()
            link.setParent(omero.model.ImageI(image.getId(), False))
            link.setChild(lengthAnn)
            conn.getUpdateService().saveAndReturnObject(link)
            lengthsForImage = []    # reset for next image.

        # Prepare data for adding to OMERO table.
        data = [
            omero.grid.LongColumn('imageId', '', imageIds),
            omero.grid.RoiColumn('roidId', '', roiIds),
            omero.grid.LongColumn('shapeId', '', shapeIds),
            omero.grid.LongColumn('theZ', '', theZs),
            omero.grid.LongColumn('theT', '', theTs),
            omero.grid.DoubleColumn('lineLength', '', lineLengths),
            omero.grid.StringColumn('shapeText', '', 64, shapeTexts),
            ]
        table.addData(data)

        # get the table as an original file & attach this data to Dataset
        orig_file = table.getOriginalFile()
        fileAnn = omero.model.FileAnnotationI()
        fileAnn.setFile(orig_file)
        link = omero.model.DatasetAnnotationLinkI()
        link.setParent(omero.model.DatasetI(dataset.getId(), False))
        link.setChild(fileAnn)
        # conn.getUpdateService().saveAndReturnObject(link)

        a = array(lineLengths)
        print "std", a.std()
        print "mean", a.mean()
        print "max", a.max()
        print "min", a.min()

        # lets retrieve all the lines that are longer than 2 standard
        # deviations above mean
        limit = a.mean() + (2 * a.std())
        print "Retrieving all lines longer than: ", limit
        rowCount = table.getNumberOfRows()
        queryRows = table.getWhereList(
            "lineLength > %s" % limit, variables={}, start=0, stop=rowCount,
            step=0)
        if len(queryRows) == 0:
            print "No lines found"
        else:
            data = table.readCoordinates(queryRows)
            for col in data.columns:
                print "Query Results for Column: ", col.name
                for v in col.values:
                    print "   ", v

        # Assume we only have 1 dataset. This will return after the first
        # dataset
        message = ("Counted %s images, %s lines. Mean length: %s, std: %s"
                   % (imgCount, len(shapeIds), a.mean(), a.std()))
        return message


def runAsScript():
    """
    The main entry point of the script, as called by the client via the
    scripting service, passing the required parameters.
    """

    dataTypes = [rstring('Dataset')]

    client = scripts.client(
        'Shapes_To_Table.py',
        ("This script processes images, measuring the length of ROI Lines and"
         " saving the results to an OMERO.table."),

        scripts.String(
            "Data_Type", optional=False, grouping="1",
            description="Choose source of images (only Dataset supported)",
            values=dataTypes, default="Dataset"),

        scripts.List(
            "IDs", optional=False, grouping="2",
            description="List of Dataset IDs to convert to new"
            " Plates.").ofType(rlong(0)),

        version="4.3.2",
        authors=["William Moore", "OME Team"],
        institutions=["University of Dundee"],
        contact="ome-users@lists.openmicroscopy.org.uk",
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

        # process images in Datasets
        message = processData(conn, scriptParams)
        if message is not None:
            client.setOutput("Message", rstring(message))
        else:
            client.setOutput("Message", rstring("No datasets found"))
    finally:
        client.closeSession()

if __name__ == "__main__":
    runAsScript()
