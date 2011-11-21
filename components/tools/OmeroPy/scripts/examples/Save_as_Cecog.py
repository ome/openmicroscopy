"""
 components/tools/OmeroPy/scripts/omero/export_scripts/Save_as_Cecog.py 

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

This script takes a single image and saves it as single plane tiff files, named according to 
the MetaMorph_PlateScanPackage as used by Cecog Analyzer. 
These are zipped into a single file and attached to the image. 

    
@author  Will Moore &nbsp;&nbsp;&nbsp;&nbsp;
<a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
@version 3.0
<small>
(<b>Internal version:</b> $Revision: $Date: $)
</small>
@since 3.0-Beta4.2
 
"""

import numpy
import re
from numpy import zeros

import omero
import omero.scripts as scripts
import omero.constants
from omero.rtypes import *
import omero.util.script_utils as script_utils

from zipfile import ZipFile
import os

def compress(target, base):
    """Creates a ZIP recursively from a given base directory."""
    zip_file = ZipFile(target, 'w')
    try:
        for root, dirs, names in os.walk(base):
            for name in names:
                path = os.path.join(root, name)
                print "Compressing: %s" % path
                zip_file.write(path)
    finally:
        zip_file.close()
        

def split_image(client, imageId, dir, unformattedImageName = "tubulin_P037_T%05d_C%s_Z%d_S1.tif", dims = ('T', 'C', 'Z')):
    """
    ** NB: This method is implemented in script_utils. I have simply copied it here temporarily since I have made
    some edits that are not yet committed and updated on nightshade. **
    
    Splits the image into component planes, which are saved as local tiffs according to unformattedImageName.
    E.g. myLocalDir/tubulin_P037_T%05d_C%s_Z%d_S1.tif which will be formatted according to dims, E.g. ('T', 'C', 'Z')
    Channel will be formatted according to channel name, not index. 
    @param rawPixelsStore The rawPixelStore
    @param queryService
    @param c The C-Section to retrieve.
    @param t The T-Section to retrieve.
    @param imageName  the local location to save the image. 
    """

    unformattedImageName = os.path.join(dir, unformattedImageName)

    session = client.getSession()
    queryService = session.getQueryService()
    rawPixelsStore = session.createRawPixelsStore()
    pixelsService = session.getPixelsService()

    from numpy import zeros, int8, fromfunction
    try:
        from PIL import Image
    except:
        import Image

    query_string = "select p from Pixels p join fetch p.image as i join fetch p.pixelsType where i.id='%s'" % imageId
    pixels = queryService.findByQuery(query_string, None)
    sizeX = pixels.getSizeX().getValue()
    sizeY = pixels.getSizeY().getValue()
    sizeZ = pixels.getSizeZ().getValue()
    sizeC = pixels.getSizeC().getValue()
    sizeT = pixels.getSizeT().getValue()
    rawPixelsStore.setPixelsId(pixels.getId().getValue(), True)

    channelMap = {}
    cIndex = 0
    pixels = pixelsService.retrievePixDescription(pixels.id.val)    # load channels
    for c in pixels.iterateChannels():
        lc = c.getLogicalChannel()
        channelMap[cIndex] = lc.getName() and lc.getName().getValue() or str(cIndex)
        cIndex += 1

    def formatName(unformatted, z, c, t):
        # need to turn dims E.g. ('T', 'C', 'Z') into tuple, E.g. (t, c, z)
        dimMap = {'T': t, 'C':channelMap[c], 'Z': z}
        dd = tuple([dimMap[d] for d in dims])
        return unformatted % dd

    # cecog does this, but other formats may want to start at 0
    zStart = 1
    tStart = 1

    # loop through dimensions, saving planes as tiffs.
    for z in range(sizeZ):
        for c in range(sizeC):
            for t in range(sizeT):
                imageName = formatName(unformattedImageName, z+zStart, c, t+tStart)
                print "downloading plane z: %s c: %s t: %s  to  %s" % (z, c, t, imageName)
                plane = script_utils.downloadPlane(rawPixelsStore, pixels, z, c, t)
                print "plane dtype: %s min: %s max: %s" % (plane.dtype.name, plane.min(), plane.max())
                # need plane dtype to be int8 for conversion to tiff by PIL
                if plane.dtype.name == 'int16' or plane.dtype.name == 'uint16':
                    
                    minVal = plane.min()
                    maxVal = plane.max()
                    valRange = maxVal - minVal
                    scaled = (plane - minVal) * (float(255) / valRange)
                    convArray = zeros(plane.shape, dtype=int8)
                    convArray += scaled
                    print "using converted int8 plane: dtype: %s min: %s max: %s" % (convArray.dtype.name, convArray.min(), convArray.max())
                    i = Image.fromarray(convArray)
                else:
                    i = Image.fromarray(plane)
                i.save(imageName)
                
def save_as_cecog():
    """
    The main entry point of the script, as called by the client via the scripting service, passing the required parameters. 
    """
    
    client = scripts.client('Save_as_Cecog.py', """Script takes a single image and saves it as single plane tiff files, named according to 
    the MetaMorph_PlateScanPackage as used by Cecog Analyzer""", 
    
    scripts.Long("Image_ID", optional=False, grouping="1",
        description="The Image you want to Save As Cecog"),
        
    version = "4.2.1",
    authors = ["William Moore", "OME Team"],
    institutions = ["University of Dundee"],
    contact = "ome-users@lists.openmicroscopy.org.uk",
    )

    try:
        session = client.getSession()

        # process the list of args above.
        parameterMap = {}
        for key in client.getInputKeys():
            if client.getInput(key):
                parameterMap[key] = client.getInput(key).getValue()

        print parameterMap

        queryService = session.getQueryService()
        updateService = session.getUpdateService()
        rawFileStore = session.createRawFileStore()
        
        curr_dir = os.getcwd()
        tiff_dir = os.path.join(curr_dir, "cecogZip")
        os.mkdir(tiff_dir)
        
        imageId = parameterMap["Image_ID"]
        image = queryService.get("Image", imageId)
        
        print "Downloading tiffs to %s" % tiff_dir
        
        split_image(client, imageId, tiff_dir, unformattedImageName = "cecog_P001_T%05d_C%s_Z%d_S1.tif", dims = ('T', 'C', 'Z'))

        zip_file_name = "Image_%s_tiffs.zip" % imageId
        zip_file = os.path.join(curr_dir, zip_file_name)
        compress(zip_file, tiff_dir)
        
        fileAnnotation = None
        if os.path.exists(zip_file_name):
            fileAnnotation = script_utils.uploadAndAttachFile(queryService, updateService, rawFileStore, image, zip_file_name, mimetype="zip")
        
        if fileAnnotation:
            client.setOutput("Message", rstring("Cecog Zip Created"))
            client.setOutput("File_Annotation", robject(fileAnnotation))
        else:
            client.setOutput("Message", rstring("Save failed - see Errors"))
            
    finally:
        client.closeSession()


if __name__ == "__main__":
    save_as_cecog()