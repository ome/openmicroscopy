"""
 examples/ScriptingService/Edit_Descriptions.py

-----------------------------------------------------------------------------
  Copyright (C) 2006-2010 University of Dundee. All rights reserved.


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

This script demonstrates the minimum framework of a script that can be run by the 
scripting service on an OMERO server. 
It defines a name, description and parameter list for the script. 
    
@author  Will Moore &nbsp;&nbsp;&nbsp;&nbsp;
<a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
@version 3.0
<small>
(<b>Internal version:</b> $Revision: $Date: $)
</small>
@since 3.0-Beta4.2
 
"""

import omero
from omero.gateway import BlitzGateway
from omero.rtypes import *
import omero.scripts as scripts

import time
startTime = 0

def printDuration():
    global startTime
    if startTime == 0:
        startTime = time.time()
    print "script time = %s secs" % (time.time() - startTime)
    
def editDescriptions(conn, parameterMap):
    """
    Does the main work of the script, setting Description for each Image in a Dataset
    
    @param conn             Blitz Gateway connection wrapper
    @param parameterMap     A map of the input parameters
    """
    # we know parameterMap will have "Dataset_ID" since this parameter is not optional
    datasetId = parameterMap["Dataset_ID"]
    
    # for optional parameters - need to test if present
    newDescription = "No description specified"  
    if "New_Description" in parameterMap:
        newDescription = parameterMap["New_Description"]

    dataset = conn.getObject('Dataset', datasetId)
    print "Dataset:", dataset.getName()

    for image in dataset.listChildren():
        image.setDescription(newDescription)
        image.save()
        
    # use this in Output_Message
    return dataset.getName()


if __name__ == "__main__":
    """
    The main entry point of the script, as called by the client via the scripting service, passing the required parameters. 
    """
    printDuration()
    client = scripts.client('Edit_Descriptions.py', 'Edits the descriptions for all the images in a given Dataset.', 
    scripts.Long("Dataset_ID", optional=False, description="Specifiy the Dataset by ID"),
    scripts.String("New_Description", description="The new description to set for each Image in the Dataset"),
    )
    
    session = client.getSession()
    
    try:
        # process the list of args above. Not scrictly necessary, but useful for more complex scripts
        parameterMap = {}
        for key in client.getInputKeys():
            if client.getInput(key):
                parameterMap[key] = client.getInput(key, unwrap=True) # unwrap rtypes to String, Integer etc

        # wrap client to use the Blitz Gateway
        conn = BlitzGateway(client_obj=client)

        # do the editing and handle the result
        datasetName = editDescriptions(conn, parameterMap)
        if datasetName:
            ouputMessage = "Script Ran OK on Dataset: %s" % datasetName
        else:
            ouputMessage = "Script failed. See error message"
        
        client.setOutput("Message", rstring(ouputMessage))
    finally:
        client.closeSession()
        printDuration()
    