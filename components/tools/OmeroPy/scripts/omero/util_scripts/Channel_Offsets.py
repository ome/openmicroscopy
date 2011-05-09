"""
 components/tools/OmeroPy/scripts/omero/util_scripts/Channel_Offsets.py

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

This script creates new images from existing images, applying x, y, and z shifts
to each channel independently, as specified in the parameters.

@author  Will Moore &nbsp;&nbsp;&nbsp;&nbsp;
<a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
@version 3.0
<small>
(<b>Internal version:</b> $Revision: $Date: $)
</small>
@since 3.0-Beta4.3
 
"""

import omero
from omero.gateway import BlitzGateway
import omero.scripts as scripts
import omero_api_IRoi_ice
from omero.rtypes import *
import omero.util.script_utils as script_utils

import os
import numpy

import time


def newImageWithChannelOffsets(conn, imageId, channel_offsets):
    """
    Process a single image here: creating a new image and passing planes from
    original image to new image - applying offsets to each channel as we go.
    
    @param imageId:     Original image
    @param channel_offsets:     List of map for each channel {'index':index, 'x':x, 'y'y, 'z':z}
    """
    
    oldImage = conn.getObject("Image", imageId)
    # these dimensions don't change
    sizeZ = oldImage.getSizeZ()
    sizeC = oldImage.getSizeC()
    sizeT = oldImage.getSizeT()
    
    # setup the (z,c,t) list of planes we need
    zctList = []
    for z in range(sizeZ):
        for offset in channel_offsets:
            for t in range(sizeT):
                zOffset = offset['z']
                zctList.append( (z+zOffset, offset['index'], t) ) 

    # for convenience, make a map of channel:offsets
    offsetMap = {}
    for c in channel_offsets:
        offsetMap[c['index']] = {'x':c['x'], 'y': c['y'], 'z':c['z']}

    def offsetPlane(plane, x, y):
        """ Takes a numpy 2D array and returns the same plane offset by x and y, adding rows and columns of 0 values"""
        sizeY, sizeX = plane.shape
        dataType = plane.dtype
        if abs(x) > 0:  # shift x by cropping, creating a new array of columns and stacking horizontally
            newCols = zeros((sizeY,abs(x)), dataType)
            x1 = max(0, 0-x)
            x2 = min(sizeX, sizeX-x)
            crop = plane[0:sizeY, x1:x2]
            if x > 0:
                plane = hstack((newCols, crop))
            else:
                plane = hstack((crop, newCols))
        # shift y by cropping, creating a new array of rows and stacking vertically
        if abs(y) > 0:
            newRows = zeros((abs(y),sizeX), dataType)
            y1 = max(0, 0-y)
            y2 = min(sizeY, sizeY-y)
            crop = plane[y1:y2, 0:sizeX]
            if y > 0:
                plane = vstack((newRows, crop))
            else:
                plane = vstack((crop, newRows))
        return plane
        
    def offsetPlaneGen():
        planeGen = oldImage.getPrimaryPixels().getPlanes(zctList)
        for i, plane in enumerate(planeGen):
            z,c,t = zctList[i]
            offsets = offsetMap[c]
            yield offsetPlane(plane, offsets['x'], offsets['y'])
    
    desc = str(channel_offsets)
    serviceFactory = conn.c.sf  # make sure that script_utils creates a NEW rawPixelsStore
    i = script_utils.imageFromNumpySeq(serviceFactory, offsetPlaneGen(), "testOffsetImage", 
        sizeZ=sizeZ, sizeC=sizeC, sizeT=sizeT, description=desc)

def processImages(conn, scriptParams):
    """ Process the script params to make a list of channel_offsets, then iterate through
    the images creating a new image from each with the specified channel offsets """
    
    channel_offsets = []
    for i in range(1, 5):
        pName = "Channel_%s" % i
        if scriptParams[pName]:
            index = i-1     # UI channel index is 1-based - we want 0-based
            x = "Channel%s_X_shift"%i in scriptParams and scriptParams["Channel%s_X_shift"%i] or 0
            y = "Channel%s_Y_shift"%i in scriptParams and scriptParams["Channel%s_Y_shift"%i] or 0
            z = "Channel%s_Z_shift"%i in scriptParams and scriptParams["Channel%s_Z_shift"%i] or 0
            channel_offsets.append({'index':index, 'x':x, 'y':y, 'z':z})
    
    print channel_offsets
    
    # need to handle Datasets eventually - Just do images for now
    for iId in scriptParams['IDs']:
        newImageWithChannelOffsets(conn, iId, channel_offsets)
        
    
def runAsScript():

    dataTypes = [rstring('Dataset'),rstring('Image')]

    client = scripts.client('Channel_Offsets.py', """Create new Images from existing images,
applying an x, y and z shift to each channel independently. """,

    scripts.String("Data_Type", optional=False, grouping="1",
        description="Pick Images by 'Image' ID or by the ID of their 'Dataset'", values=dataTypes, default="Image"),

    scripts.List("IDs", optional=False, grouping="2",
        description="List of Dataset IDs or Image IDs to process.").ofType(rlong(0)),

    scripts.Bool("Channel_1", grouping="3", default=True,
        description="Choose to include this channel in the output image"),

    scripts.Int("Channel1_X_shift", grouping="3.1", default=0,
        description="Number of pixels to shift this channel in the X direction. (negative to shift left)"),

    scripts.Int("Channel1_Y_shift", grouping="3.2", default=0,
        description="Number of pixels to shift this channel in the Y direction. (negative to shift up)"),

    scripts.Int("Channel1_Z_shift", grouping="3.3", default=0,
        description="Offset channel by a number of Z-sections"),

    scripts.Bool("Channel_2", grouping="4", default=True,
        description="Choose to include this channel in the output image"),

    scripts.Int("Channel2_X_shift", grouping="4.1", default=0,
        description="Number of pixels to shift this channel in the X direction. (negative to shift left)"),

    scripts.Int("Channel2_Y_shift", grouping="4.2", default=0,
        description="Number of pixels to shift this channel in the Y direction. (negative to shift up)"),

    scripts.Int("Channel2_Z_shift", grouping="4.3", default=0,
        description="Offset channel by a number of Z-sections"),

    scripts.Bool("Channel_3", grouping="5", default=True,
        description="Choose to include this channel in the output image"),

    scripts.Int("Channel3_X_shift", grouping="5.1", default=0,
        description="Number of pixels to shift this channel in the X direction. (negative to shift left)"),

    scripts.Int("Channel3_Y_shift", grouping="5.2", default=0,
        description="Number of pixels to shift this channel in the Y direction. (negative to shift up)"),

    scripts.Int("Channel3_Z_shift", grouping="5.3", default=0,
        description="Offset channel by a number of Z-sections"),

    scripts.Bool("Channel_4", grouping="6", default=True,
        description="Choose to include this channel in the output image"),

    scripts.Int("Channel4_X_shift", grouping="6.1", default=0,
        description="Number of pixels to shift this channel in the X direction. (negative to shift left)"),

    scripts.Int("Channel4_Y_shift", grouping="6.2", default=0,
        description="Number of pixels to shift this channel in the Y direction. (negative to shift up)"),

    scripts.Int("Channel4_Z_shift", grouping="6.3", default=0,
        description="Offset channel by a number of Z-sections"),

    scripts.String("New_Dataset_Name", grouping="7",
        description="If you want the new image(s) in a new Dataset, put name here"),

    version = "4.2.0",
    authors = ["William Moore", "OME Team"],
    institutions = ["University of Dundee"],
    contact = "ome-users@lists.openmicroscopy.org.uk",
    )
    
    try:
        session = client.getSession();

        # process the list of args above.
        scriptParams = {}
        for key in client.getInputKeys():
            if client.getInput(key):
                scriptParams[key] = unwrap( client.getInput(key) )

        print scriptParams
        
        # wrap client to use the Blitz Gateway
        conn = BlitzGateway(client_obj=client)
        
        processImages(conn, scriptParams)

        client.setOutput("Message", rstring("We're done!"))
    finally:
        client.closeSession()

if __name__ == "__main__":
    runAsScript()