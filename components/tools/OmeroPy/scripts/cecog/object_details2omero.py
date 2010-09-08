"""
 components/tools/OmeroPy/scripts/cecog/cecog2omero.py 

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

This script uses EMAN2 or PIL to read multiple planes from a local folder, combine and upload them to OMERO 
as new images with additional T, C, Z dimensions.
It should be run as a local script (not via scripting service) in order that it has
access to the local users file system. Therefore need EMAN2 or PIL installed locally. 

Example usage:
will$ python cecog2omero.py -h localhost -u root -p omero -d /Applications/CecogPackage/Data/Demo_data/0037/
Since this dir does not contain folders, this will upload images in '0037' into a Dataset called Demo_data 
in a Project called 'Data'. 

will$ python cecog2omero.py -h localhost -u root -p omero -d /Applications/CecogPackage/Data/Demo_data/
Since this dir does contain folders, this will look for images in all subdirectories of 'Demo_data' and
upload images into a Dataset called Demo_data in a Project called 'Data'.

Images will be combined in Z, C and T according to the MetaMorph_PlateScanPackage naming convention. 
E.g. tubulin_P0037_T00005_Cgfp_Z1_S1.tiff is Point 37, Timepoint 5, Channel gfp, Z 1. S? 
see /Applications/CecogPackage/CecogAnalyzer.app/Contents/Resources/resources/naming_schemes.conf 
    
@author  Will Moore &nbsp;&nbsp;&nbsp;&nbsp;
<a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
@version 3.0
<small>
(<b>Internal version:</b> $Revision: $Date: $)
</small>
@since 3.0-Beta4.2
 
"""

import getopt, sys, os

import omero
import omero.constants
from omero.rtypes import *
import omero_api_Gateway_ice    # see http://tinyurl.com/icebuserror
import omero.util.script_utils as scriptUtil


def addRoi(updateService, image, x, y, theT, theZ, roiText=None):
    """
    Adds a Rectangle (particle) to the current OMERO image, at point x, y. 
    Uses the self.image (OMERO image) and self.updateService
    """
    
    # create an ROI, add the point and save
    roi = omero.model.RoiI()
    roi.setImage(image)
    r = updateService.saveAndReturnObject(roi)
    

    # create and save a point
    point = omero.model.PointI()
    point.cx = rdouble(x)
    point.cy = rdouble(y)
    point.theZ = rint(theT)
    point.theT = rint(theZ)
    if roiText:
        point.textValue = rstring(roiText)    # for display only

    # link the point to the ROI and save it 
    point.setRoi(r)
    r.addShape(point)    
    updateService.saveAndReturnObject(point)


def parseObject(updateService, image, line):
    
    theZ = 0
    theT = None
    x = None
    y = None
    try:
        frame,objID,classLabel,className,centerX,centerY,mean,sd = line.split("\t")
        theT = long(frame)-1
        x = float(centerX)
        y = float(centerY)
    except:
        # line wasn't a data object
        pass
        
    if theT and x and y:
        print "Adding point '%s' to frome: %s, x: %s, y: %s" % (className, theT, x, y)
        addRoi(updateService, image, x, y, theT, theZ, className)


def cecogToOmero(commandArgs):
    """
    Processes the command args, parses the object_details.txt file and creates ROIs on the image specified in OMERO
    
    @param commandArgs      Map of command args. 
    """
    #print commandArgs
    filePath = commandArgs["file"]
    if not os.path.exists(filePath):
        print "Could find the object_details file at %s" % filePath
        return
    
    client = omero.client(commandArgs["host"])
    session = client.createSession(commandArgs["username"], commandArgs["password"])
    
    # create the services we need 
    services = {}
    gateway = session.createGateway()
    updateService = session.getUpdateService()
    
    imageId = commandArgs["image"]
    image = gateway.getImage(long(imageId))
    
    object_details = open(filePath, 'r')
    
    for line in object_details:
        parseObject(updateService, image, line)
    
    object_details.close()
    
    
def readCommandArgs():
    
    def usage():
        print "Usage: python cecog2omero.py --host host --username username --password password --file object_details.txt --image imageId"
    try:
        opts, args = getopt.getopt(sys.argv[1:] ,"h:u:p:f:i:", ["host=", "username=", "password=","file=", "image="])
    except getopt.GetoptError, err:          
        usage()                         
        sys.exit(2)    
    returnMap = {}                 
    for opt, arg in opts: 
        if opt in ("-h","--host"):
            returnMap["host"] = arg
        elif opt in ("-u","--username"): 
            returnMap["username"] = arg    
        elif opt in ("-p","--password"): 
            returnMap["password"] = arg    
        elif opt in ("-f","--file"): 
            returnMap["file"] = arg
        elif opt in ("-i","--image"): 
            returnMap["image"] = arg
    return returnMap

if __name__ == "__main__":        
    commandArgs = readCommandArgs()
    cecogToOmero(commandArgs)
    