#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
 examples/ScriptingService/Edit_Descriptions.py

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

This script demonstrates a number of features that must be included in OMERO
scripts, as well as some that improve the usability etc.
 * Definition of script name, description and parameters
 * Inputs designed to be auto-populated based on currently selected objects in
   OMERO.insight client
 * Outputs designed to display a useful message and provide link to Image in
   Insight (Datasets handled too)
 * Using print for logging to stdout


@author  Will Moore &nbsp;&nbsp;&nbsp;&nbsp;
<a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
@version 3.0
<small>
(<b>Internal version:</b> $Revision: $Date: $)
</small>
@since 3.0-Beta4.2

"""

from omero.gateway import BlitzGateway
from omero.rtypes import rlong, rstring, robject
import omero.scripts as scripts


def editDescriptions(conn, scriptParams):
    """
    Does the main work of the script, setting Description for each Image

    @param conn:             Blitz Gateway connection wrapper
    @param scriptParams:     A map of the input parameters
    """
    # we know scriptParams will have "Data_Type" and "IDs" since these
    # parameters are not optional
    dataType = scriptParams["Data_Type"]
    ids = scriptParams["IDs"]

    # dataType is 'Dataset' or 'Image' so we can use it directly in
    # getObjects()
    obs = conn.getObjects(dataType, ids)    # generator of images or datasets
    objects = list(obs)

    if len(objects) == 0:
        print "No %ss found for specified IDs" % dataType
        return

    images = []

    if dataType == 'Dataset':
        for ds in objects:
            print "Processing Images from Dataset: %s" % ds.getName()
            imgs = list(ds.listChildren())
            images.extend(imgs)
    else:
        print "Processing Images identified by ID"
        images = objects

    # for optional parameters - need to test if present
    newDescription = "No description specified"
    if "New_Description" in scriptParams:
        newDescription = scriptParams["New_Description"]

    print "Editing images with this description: \n%s\n" % newDescription

    # keep track of what we've edited, to provide feedback message
    editedImgIds = []
    for i in images:
        print "   Editing image ID: %d Name: %s" % (i.id, i.name)
        i.setDescription(newDescription)
        i.save()
        editedImgIds.append(i.id)

    # use this in Output_Message
    return editedImgIds


if __name__ == "__main__":
    """
    The main entry point of the script, as called by the client via the
    scripting service, passing the required parameters.
    """

    dataTypes = [rstring('Dataset'), rstring('Image')]

    # Here we define the script name and description.
    # Good practice to put url here to give users more guidance on how to run
    # your script.
    client = scripts.client(
        'Edit_Descriptions.py',
        ("Edits the descriptions of multiple Images, either specified via"
         " Image IDs or by the Dataset IDs.\nSee"
         " http://www.openmicroscopy.org/site/support/omero5.2/developers/"
         "scripts/user-guide.html for the tutorial that uses this script."),

        scripts.String(
            "Data_Type", optional=False, grouping="1",
            description="The data you want to work with.", values=dataTypes,
            default="Dataset"),

        scripts.List(
            "IDs", optional=False, grouping="2",
            description="List of Dataset IDs or Image IDs").ofType(rlong(0)),

        scripts.String(
            "New_Description", grouping="3",
            description="The new description to set for each Image in the"
            " Dataset"),
    )

    try:
        # process the list of args above. Not scrictly necessary, but useful
        # for more complex scripts
        scriptParams = {}
        for key in client.getInputKeys():
            if client.getInput(key):
                # unwrap rtypes to String, Integer etc
                scriptParams[key] = client.getInput(key, unwrap=True)

        print scriptParams    # handy to have inputs in the std-out log

        # wrap client to use the Blitz Gateway
        conn = BlitzGateway(client_obj=client)

        # do the editing...
        editedImgIds = editDescriptions(conn, scriptParams)

        # now handle the result, displaying message and returning image if
        # appropriate
        if editedImgIds is None:
            message = "Script failed. See 'error' or 'info' for more details"
        else:
            if len(editedImgIds) == 1:
                # image-wrapper
                img = conn.getObject("Image", editedImgIds[0])
                message = "One Image edited: %s" % img.getName()
                # omero.model object
                omeroImage = img._obj
                # Insight will display 'View' link to image
                client.setOutput("Edited Image", robject(omeroImage))
            elif len(editedImgIds) > 1:
                message = "%s Images edited" % len(editedImgIds)
            else:
                message = ("No images edited. See 'error' or 'info' for more"
                           " details")
                # Insight will display the 'Message' parameter
        client.setOutput("Message", rstring(message))
    finally:
        client.closeSession()
