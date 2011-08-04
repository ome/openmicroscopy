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

def addImageToPlate(conn, imageId, plateId, column, row):
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

    ws = omero.model.WellSampleI()
    ws.image = omero.model.ImageI(imageId, False)
    ws.well = well
    well.addWellSample(ws)
    updateService.saveObject(ws)


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
    images.sort(key = lambda x: x.name.lower())

    for image in images:
        print "   moving image: %d %s to row: %d, column: %d" % (image.id, image.name, row, col)
        addImageToPlate(conn, image.id, plate.id.val, col, row)
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

    return plate

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
    for datasetId in IDs:
        plate = dataset_to_plate(conn, scriptParams, datasetId, screen)
        if plate is not None:
            plates.append(plate)

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
        description="""Arrange images accross 'row' first or down 'column'"""),

    scripts.Int("First_Axis_Count", grouping="3.1", optional=False, default=12,
        description="Number of Rows or Columns in the 'First Axis'", min=1),

    scripts.String("Column_Names", grouping="4", optional=False, default='number', values=rowColNaming,
        description="""Name plate columns with 'number' or 'letter'"""),

    scripts.String("Row_Names", grouping="5", optional=False, default='letter', values=rowColNaming,
        description="""Name plate rows with 'number' or 'letter'"""),

    scripts.String("Screen", grouping="6",
        description="""Option: put Plate(s) in a Screen. Enter Name of new screen or ID of existing screen"""),

    scripts.Bool("Delete_Datasets", grouping="7", default=False,
        description="""After placing Images in Plates, delete the Dataset"""),

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

        # convert Dataset(s) to Plate(s)
        plates = datasets_to_plates(conn, scriptParams)

        if len(plates) == 1:
            client.setOutput("Message", rstring("Script Ran OK. New Plate created ID: %s" % plates[0].id.val))
            client.setOutput("New_Plate",robject(plates[0]))
        elif len(plates) > 1:
            client.setOutput("Message", rstring("Script Ran OK. %d plates created" % len(plates) ))
        else:
            client.setOutput("Message", rstring("No plates created. See 'Error' or 'Info' for details"))
    finally:
        client.closeSession()

if __name__ == "__main__":
    runAsScript()