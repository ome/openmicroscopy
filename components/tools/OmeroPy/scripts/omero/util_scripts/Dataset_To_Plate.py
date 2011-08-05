"""
 components/tools/OmeroPy/scripts/omero/util_scripts/Dataset_To_Plate.py

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

This script converts a Dataset of Images to a Plate, with one image per Well.

@author Will Moore
<a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
@version 4.3
<small>
(<b>Internal version:</b> $Revision: $Date: $)
</small>
@since 3.0-Beta4.3
"""

import omero.scripts as scripts
from omero.gateway import BlitzGateway
import omero
from omero.rtypes import *

def addImageToPlate(conn, image, plateId, column, row, removeFrom=None):
    """
    Add the Image to a Plate, creating a new well at the specified column and row
    NB - This will fail if there is already a well at that point
    """
    updateService = conn.getUpdateService()

    well = omero.model.WellI()
    well.plate = omero.model.PlateI(plateId, False)
    well.column = rint(column)
    well.row = rint(row)
    well = updateService.saveAndReturnObject(well)

    try:
        ws = omero.model.WellSampleI()
        ws.image = omero.model.ImageI(image.id, False)
        ws.well = well
        well.addWellSample(ws)
        updateService.saveObject(ws)
    except:
        print "Failed to add image to well sample"
        return False

    # remove from Datast
    if removeFrom is not None:
        links = list( image.getParentLinks(removeFrom.id) )
        print "     Removing image from %d Dataset: %s" % (len(links), removeFrom.name)
        for l in links:
            conn.deleteObjectDirect(l._obj)
    return True


def dataset_to_plate(conn, scriptParams, datasetId, screen):

    dataset = conn.getObject("Dataset", datasetId)
    if dataset is None:
        print "No dataset found for ID %s" % datasetId
        return

    updateService = conn.getUpdateService()

    # create Plate
    plate = omero.model.PlateI()
    plate.name = rstring(str(dataset.name))
    plate.columnNamingConvention = rstring(str(scriptParams["Column_Names"])) # 'letter' or 'number'
    plate.rowNamingConvention = rstring(str(scriptParams["Row_Names"]))
    plate = updateService.saveAndReturnObject(plate)

    if screen is not None:
        link = omero.model.ScreenPlateLinkI()
        link.parent = omero.model.ScreenI(screen.id, False)
        link.child = omero.model.PlateI(plate.id.val, False)
        updateService.saveObject(link)

    print "Moving images from Dataset: %d %s to Plate: %d %s" % (dataset.id, dataset.name, plate.id.val, plate.name.val)

    row = 0
    col = 0

    firstAxisIsRow = scriptParams["First_Axis"] == 'row'
    axisCount = scriptParams["First_Axis_Count"]

    # sort images by name
    images = list(dataset.listChildren())
    datasetImgCount = len(images)
    if "Filter_Names" in scriptParams:
        filterBy = scriptParams["Filter_Names"]
        print "Filtering images for names containing: %s" % filterBy
        images = [i for i in images if (i.getName().find(filterBy) >=0)]
    images.sort(key = lambda x: x.name.lower())

    # Do we try to remove images from Dataset and Delte Datset when/if empty?
    removeFrom = None
    removeDataset = "Remove_From_Dataset" in scriptParams and scriptParams["Remove_From_Dataset"]
    if removeDataset:
        removeFrom = dataset

    for image in images:
        print "    moving image: %d %s to row: %d, column: %d" % (image.id, image.name, row, col)
        added = addImageToPlate(conn, image, plate.id.val, col, row, removeFrom)
        # update row and column index
        if firstAxisIsRow:
            row += 1
            if row >= axisCount:
                row = 0
                col += 1
        else:
            col += 1
            if col >= axisCount:
                col = 0
                row += 1

    # if user wanted to delete dataset, AND it's empty we can delete dataset
    deleteDataset = False   # Turning this functionality off for now.
    deleteHandle = None
    if deleteDataset:
        if datasetImgCount == addedCount:
            dcs = list()
            print 'Deleting Dataset %d %s' % (dataset.id, dataset.name)
            options = None # {'/Image': 'KEEP'}    # don't delete the images!
            dcs.append(omero.api.delete.DeleteCommand("/Dataset", dataset.id, options))
            deleteHandle = conn.getDeleteService().queueDelete(dcs)
    return plate, deleteHandle

def datasets_to_plates(conn, scriptParams):

    updateService = conn.getUpdateService()

    # these must be Dataset IDs
    IDs = scriptParams["IDs"]

    # find or create Screen if specified
    screen = None
    if "Screen" in scriptParams and len(scriptParams["Screen"]) > 0:
        s = scriptParams["Screen"]
        # see if this is ID of existing screen
        try:
            screenId = long(s)
            screen = conn.getObject("Screen", screenId)
        except ValueError:
            pass
        # if not, create one
        if screen is None:
            screen = omero.model.ScreenI()
            screen.name = rstring(s)
            screen = updateService.saveAndReturnObject(screen)

    plates = []
    deletes = []
    for datasetId in IDs:
        plate, deleteHandle = dataset_to_plate(conn, scriptParams, datasetId, screen)
        if plate is not None:
            plates.append(plate)
        if deleteHandle is not None:
            deletes.append(deleteHandle)

    # wait for any deletes to finish
    for handle in deletes:
        cb = omero.callbacks.DeleteCallbackI(conn.c, handle)
        while True: # ms
            if cb.block(100) is None:
                print "Waiting for delete"
            else:
                break
        err = handle.errors()
        if err > 0:
            print "Delete error", err
        else:
            print "Delete OK"

    if screen is not None:
        return [screen]
    else:
        return plates


def runAsScript():
    """
    The main entry point of the script, as called by the client via the scripting service, passing the required parameters.
    """

    dataTypes = [rstring('Dataset')]
    firstAxis = [rstring('column'), rstring('row')]
    rowColNaming = [rstring('letter'), rstring('number')]

    client = scripts.client('Dataset_To_Plate.py', """Take a Dataset of Images and put them in a new Plate,
arranging them into rows or columns as desired.
Optionally add the Plate to a new or existing Screen.
See http://www.openmicroscopy.org/site/support/omero4/getting-started/tutorial/running-util-scripts""",

    scripts.String("Data_Type", optional=False, grouping="1",
        description="Choose source of images (only Dataset supported)", values=dataTypes, default="Dataset"),

    scripts.List("IDs", optional=False, grouping="2",
        description="List of Dataset IDs to convert to new Plates.").ofType(rlong(0)),

    scripts.String("Filter_Names", grouping="2.1",
        description="Filter the images by names that contain this value"),

    scripts.String("First_Axis", grouping="3", optional=False, default='column', values=firstAxis,
        description="""Arrange images accross 'column' first or down 'row'"""),

    scripts.Int("First_Axis_Count", grouping="3.1", optional=False, default=12,
        description="Number of Rows or Columns in the 'First Axis'", min=1),

    scripts.String("Column_Names", grouping="4", optional=False, default='number', values=rowColNaming,
        description="""Name plate columns with 'number' or 'letter'"""),

    scripts.String("Row_Names", grouping="5", optional=False, default='letter', values=rowColNaming,
        description="""Name plate rows with 'number' or 'letter'"""),

    scripts.String("Screen", grouping="6",
        description="""Option: put Plate(s) in a Screen. Enter Name of new screen or ID of existing screen"""),

    scripts.Bool("Remove_From_Dataset", grouping="7", default=True,
        description="""Remove Images from Dataset as they are added to Plate"""),

    version = "4.3.2",
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

        # convert Dataset(s) to Plate(s). Returns new plates or screen
        newObjs = datasets_to_plates(conn, scriptParams)

        if len(newObjs) == 1:
            name = newObjs[0].name.val
            dType = newObjs[0].__class__.__name__[:-1]
            client.setOutput("Message", rstring("Script Ran OK. New %s created: %s" % (dType, name)))
            client.setOutput("New_Object",robject(newObjs[0]))
        elif len(newObjs) > 1:
            client.setOutput("Message", rstring("Script Ran OK. %d Plates created" % len(newObjs) ))
            client.setOutput("New_Object",robject(newObjs[0]))  # return the first one
        else:
            client.setOutput("Message", rstring("No plates created. See 'Error' or 'Info' for details"))
    finally:
        client.closeSession()

if __name__ == "__main__":
    runAsScript()