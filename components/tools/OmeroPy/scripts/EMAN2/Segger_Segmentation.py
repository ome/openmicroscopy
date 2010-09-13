"""
 components/tools/OmeroPy/scripts/EMAN2/Segger_Segmentation.py 

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

This script allows users to call Segger functionality within Chimera, without using GUI. 

An Image, identified by ID, is downloaded as an mrc file, using EMAN2. 
A python file, to specify functionality and parameters, is created by this script and passed to Chimera 
as a command line argument. 
Many thanks to Greg Pintilie for providing the chimera script. 

The results of the script (seg.file) can then be uploaded back to OMERO as an attachment to the Image.
    
@author  Will Moore &nbsp;&nbsp;&nbsp;&nbsp;
<a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
@version 3.0
<small>
(<b>Internal version:</b> $Revision: $Date: $)
</small>
@since 3.0-Beta4.2
 
"""
import os
import numpy

import omero
import omero_api_Gateway_ice    # see http://tinyurl.com/icebuserror
import omero.scripts as scripts
from omero.rtypes import *
import omero.util.script_utils as scriptUtil
    
from EMAN2 import *

def writePythonScript(scriptName, threshold, numit, sdev, targNRegs):
    
    output = open(scriptName, 'w')

    try:
        output.write(""" 
# This file is an input script to Chimera

# it can be run with --nogui, meaning that Chimera will not open a graphics
# window; instead the segmentation is done at the command line only

# to run it at a command line run:
# chimera --nogui <path of map to segment> segger_nogui.py

# make sure to edit the parameters detailed below as necessary...


import os.path 
import VolumeViewer
import Segger.segfile
from Segger.regions import Segmentation

def segment () :

    vlist = VolumeViewer.volume_list()

    if len ( vlist ) == 0 :
        print "no open models - there should be an open mrc map to segment"
        return

    v = vlist[0]
    print "Segmenting map:", v.name

    # for the name, strip the .map extension if there is one, and add .seg
    seg_name = os.path.splitext ( v.name )[0] + ".seg"
    seg = Segmentation ( seg_name, v)

    # there are 4 parameters that control the output:

    # the threshold to apply to the map - only voxels with density value above
    # this threshold will appear in the segmentation
    threshold = %s

    # the number of smoothing iterations - at each iteration, the number
    # of segmented regions typically decreases; hence the more iterations,
    # the fewer regions that will result
    numit = %s

    # the initial standard deviation of the smoothing filter
    # the higher this number, the more smoothing that occurs at each step, and
    # hence also the fewer the regions that will be obtained after each step
    sdev = %s

    # the number of regions we are aiming for
    # if, while smoothing and grouping, the number of regions becomes lower
    # than this number, the process stops
    # hence, if say, we are looking for 14 regions, a large number should be used
    # for numit (say 100), and set this variable to 14
    targNRegs = %s

    # first apply watershed
    seg.calculate_watershed_regions ( v, threshold )

    # then group the resulting regions by smoothing
    seg.smooth_and_group(numit, sdev, targNRegs)

    # now save the .seg file
    # take away the extension in the map name, and add a .seg
    seg_file = os.path.splitext ( v.data.path )[0] + ".seg"
    print "Saving segmentation:", seg_file
    Segger.segfile.write_segmentation ( seg, seg_file )


segment ()""" % (threshold, numit, sdev, targNRegs))
        
    finally:
        output.flush()
        output.close()
        
        
def downloadImage(queryService, rawPixelStore, imageId, imageName):
    """
    This method downloads the first 3D data of the OMERO image and saves it as a local image.
    
    @param session        The OMERO session
    @param imageId        The ID of the image to download
    @param imageName    The name of the image to write. If no path, saved in the current directory. 
    """
    
    # set up pixel-store and get the pixels object
    query_string = "select p from Pixels p join fetch p.image as i join fetch p.pixelsType where i.id='%d'" % imageId
    pixels = queryService.findByQuery(query_string, None)
    sizeX = pixels.getSizeX().getValue()
    sizeY = pixels.getSizeY().getValue()
    sizeZ = pixels.getSizeZ().getValue()
    sizeC = pixels.getSizeC().getValue()
    sizeT = pixels.getSizeT().getValue()
    bypassOriginalFile = True
    rawPixelStore.setPixelsId(pixels.getId().getValue(), bypassOriginalFile)
    em = EMData(sizeX,sizeY,sizeZ)
    pixelsType = pixels.pixelsType
    
    theC, theT = 0, 0
    for theZ in range(sizeZ):
        plane2D = scriptUtil.downloadPlane(rawPixelStore, pixels, theZ, theC, theT)
        try:    # method seems to depend which version of EMAN2 you have
            EMNumPy.numpy2em(plane2D, e)
        except:
            e = EMNumPy.numpy2em(plane2D)
        em.insert_clip(e,(0,0,theZ))
        
    em.write_image(imageName)
    
    
def getPixelsType(queryService, imageName):
    """
    Get the OMERO pixelsType object appropriate for the image named. 
    """
    plane2D = spider2array(imageName)
    pType = plane2D.dtype.name
    pixelsType = queryService.findByQuery("from PixelsType as p where p.value='%s'" % pType, None) # omero::model::PixelsType
    
    if pixelsType == None and pType.startswith("float"):
        # try 'float'
        pixelsType = queryService.findByQuery("from PixelsType as p where p.value='%s'" % "float", None) # omero::model::PixelsType
    if pixelsType == None:
        print "Unknown pixels type for: " % pType
        return
    else:
        print "Using pixels type ", pixelsType.getValue().getValue()
    return pixelsType
    
    
def runSegger(session, parameterMap):
    """
    This is where the action happens.
    We get the parameters, download the image, write segger_nogui.py file and 
    pass this to Chimera from command line. 
    Then look for the generated .seg file and attach it to the image. 
    """
    
    # create services we need 
    gateway = session.createGateway()
    queryService = session.getQueryService()
    rawPixelStore = session.createRawPixelsStore()
    rawFileStore = session.createRawFileStore()
    updateService = session.getUpdateService()
    
    # required parameters
    imageId = long(parameterMap["Image_ID"])
    image = gateway.getImage(imageId)
    threshold = parameterMap["Threshold"]
    
    # optional parameters
    numit = 3
    if "Smoothing_Steps" in parameterMap:
        numit = parameterMap["Smoothing_Steps"]
    sdev = 1
    if "Standard_Deviation" in parameterMap:
        sdev = parameterMap["Standard_Deviation"]
    targNRegs = 1   # not sure what this default should be
    if "Target_Region_Count" in parameterMap:
        targNRegs = parameterMap["Target_Region_Count"]
    
    # local file names - indicate parameters
    name = "thr%.2fss%ssd%strc%s" % (threshold, numit, sdev, targNRegs)
    inputName = "%s.mrc" % name
    outputName = "%s.seg" % name
    
    downloadImage(queryService, rawPixelStore, imageId, inputName)
    if not os.path.exists(inputName):
        print "Failed to download image as %s" % inputName
    
    # write out a python file...
    scriptName = "segger_nogui.py"
    writePythonScript(scriptName, threshold, numit, sdev, targNRegs)
    if not os.path.exists(scriptName):
        print "Failed to write script file as %s" % scriptName
    
    chimeraCmd = "chimera --nogui %s %s" % (inputName, scriptName)
    print chimeraCmd
    
    os.system(chimeraCmd)
        
    # upload segger file
    if not os.path.exists(outputName):
        print "Segger file not created by Chimera from Input Image ID: %s" % imageId
    else:
        origFile = scriptUtil.uploadAndAttachFile(queryService, updateService, rawFileStore, image, outputName, "application/octet-stream")
        return origFile


def runAsScript():
    """
    The main entry point of the script, as called by the client via the scripting service, passing the required parameters. 
    """
    
    client = scripts.client('Segger_Segmentation.py', """Run Segger segmentation on an EM-map/Image in OMERO
See http://people.csail.mit.edu/gdp/segger/docs_segmenting.html""", 
    scripts.Long("Image_ID", optional=False, grouping="1",
        description="The OMERO Image we want to segment"),
        
    scripts.Float("Threshold", optional=False, grouping="2",
        description="The threshold to apply - only voxels above this threshold will appear in the segmentation"), 
        
    scripts.Int("Smoothing_Steps", grouping="3", 
        description="""The number of smoothing iterations - at each iteration, the number
of segmented regions typically decreases; hence the more iterations,
the fewer regions that will result""", default=3),

    scripts.Int("Standard_Deviation", grouping="4",
        description="""The initial standard deviation of the smoothing filter
the higher this number, the more smoothing that occurs at each step, and
hence also the fewer the regions that will be obtained after each step""", default=1),

    scripts.Int("Target_Region_Count", grouping="5",
        description="""The number of regions we are aiming for.
If, while smoothing and grouping, the number of regions becomes lower
than this number, the process stops.""", default=1),
    )
    
    try:
        session = client.getSession()
    
        # process the list of args above. 
        parameterMap = {}
        for key in client.getInputKeys():
            if client.getInput(key):
                parameterMap[key] = client.getInput(key).getValue()
        
        print parameterMap
        
        origFile = runSegger(session, parameterMap)
        
        if origFile == None:
            client.setOutput("Message", rstring("No segmentation file created. See Error"))
        else:
            client.setOutput("Message", rstring("Segger file attached to Image"))
            client.setOutput("Segger_File", robject(origFile))
            
    finally: client.closeSession()
    
if __name__ == "__main__":
    runAsScript()