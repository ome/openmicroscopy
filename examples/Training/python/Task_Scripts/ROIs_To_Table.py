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


def process_data(conn, script_params):
    """
    For each Dataset, process each Image adding the length of each ROI line to
    an OMERO.table.
    Also calculate the average of all lines for each Image and add this as a
    Double Annotation on Image.
    """

    dataset_ids = script_params['IDs']
    for dataset in conn.getObjects("Dataset", dataset_ids):

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
        image_ids = []
        roi_ids = []
        shape_ids = []
        the_z_s = []
        the_t_s = []
        line_lengths = []
        shape_texts = []
        roi_service = conn.getRoiService()
        lengths_for_image = []
        img_count = 0
        for image in dataset.listChildren():
            img_count += 1
            result = roi_service.findByImage(image.getId(), None)
            for roi in result.rois:
                for s in roi.copyShapes():
                    if type(s) == omero.model.LineI:
                        image_ids.append(image.getId())
                        roi_ids.append(roi.getId().getValue())
                        shape_ids.append(s.getId().getValue())
                        the_z_s.append(s.getTheZ().getValue())
                        the_t_s.append(s.getTheT().getValue())
                        x1 = s.getX1().getValue()
                        x2 = s.getX2().getValue()
                        y1 = s.getY1().getValue()
                        y2 = s.getY2().getValue()
                        x = x1 - x2
                        y = y1 - y2
                        length = math.sqrt(math.pow(x, 2) + math.pow(y, 2))
                        line_lengths.append(length)
                        lengths_for_image.append(length)
                        if s.getTextValue():
                            shape_texts.append(s.getTextValue().getValue())
                        else:
                            shape_texts.append("")
            if len(lengths_for_image) == 0:
                print "No lines found on Image:", image.getName()
                continue
            img_average = sum(lengths_for_image) / len(lengths_for_image)
            print ("Average length of line for Image: %s is %s"
                   % (image.getName(), img_average))

            # Add the average as an annotation on each image.
            length_ann = omero.model.DoubleAnnotationI()
            length_ann.setDoubleValue(rdouble(img_average))
            length_ann.setNs(
                rstring("imperial.training.demo.lineLengthAverage"))
            link = omero.model.ImageAnnotationLinkI()
            link.setParent(omero.model.ImageI(image.getId(), False))
            link.setChild(length_ann)
            conn.getUpdateService().saveAndReturnObject(link)
            lengths_for_image = []    # reset for next image.

        # Prepare data for adding to OMERO table.
        data = [
            omero.grid.LongColumn('imageId', '', image_ids),
            omero.grid.RoiColumn('roidId', '', roi_ids),
            omero.grid.LongColumn('shapeId', '', shape_ids),
            omero.grid.LongColumn('theZ', '', the_z_s),
            omero.grid.LongColumn('theT', '', the_t_s),
            omero.grid.DoubleColumn('lineLength', '', line_lengths),
            omero.grid.StringColumn('shapeText', '', 64, shape_texts),
            ]
        table.addData(data)

        # get the table as an original file & attach this data to Dataset
        orig_file = table.getOriginalFile()
        file_ann = omero.model.FileAnnotationI()
        file_ann.setFile(orig_file)
        link = omero.model.DatasetAnnotationLinkI()
        link.setParent(omero.model.DatasetI(dataset.getId(), False))
        link.setChild(file_ann)
        # conn.getUpdateService().saveAndReturnObject(link)

        a = array(line_lengths)
        print "std", a.std()
        print "mean", a.mean()
        print "max", a.max()
        print "min", a.min()

        # lets retrieve all the lines that are longer than 2 standard
        # deviations above mean
        limit = a.mean() + (2 * a.std())
        print "Retrieving all lines longer than: ", limit
        row_count = table.getNumberOfRows()
        query_rows = table.getWhereList(
            "lineLength > %s" % limit, variables={}, start=0, stop=row_count,
            step=0)
        if len(query_rows) == 0:
            print "No lines found"
        else:
            data = table.readCoordinates(query_rows)
            for col in data.columns:
                print "Query Results for Column: ", col.name
                for v in col.values:
                    print "   ", v

        # Assume we only have 1 dataset. This will return after the first
        # dataset
        message = ("Counted %s images, %s lines. Mean length: %s, std: %s"
                   % (img_count, len(shape_ids), a.mean(), a.std()))
        return message


def run_as_script():
    """
    The main entry point of the script, as called by the client via the
    scripting service, passing the required parameters.
    """

    data_types = [rstring('Dataset')]

    client = scripts.client(
        'Shapes_To_Table.py',
        ("This script processes images, measuring the length of ROI Lines and"
         " saving the results to an OMERO.table."),

        scripts.String(
            "Data_Type", optional=False, grouping="1",
            description="Choose source of images (only Dataset supported)",
            values=data_types, default="Dataset"),

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
        script_params = {}
        for key in client.getInputKeys():
            if client.getInput(key):
                script_params[key] = client.getInput(key, unwrap=True)

        print script_params

        # wrap client to use the Blitz Gateway
        conn = BlitzGateway(client_obj=client)

        # process images in Datasets
        message = process_data(conn, script_params)
        if message is not None:
            client.setOutput("Message", rstring(message))
        else:
            client.setOutput("Message", rstring("No datasets found"))
    finally:
        conn.close()

if __name__ == "__main__":
    run_as_script()
