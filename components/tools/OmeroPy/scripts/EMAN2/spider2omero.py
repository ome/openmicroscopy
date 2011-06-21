"""
 components/tools/OmeroPy/scripts/EMAN2/spider2omero.py 

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

This script uses Spider to read images from a local file system and upload them to OMERO as new images.
Spider header data is attached to images as Original Metadata. 
It should be run as a local script (not via scripting service) in order that it has
access to the local users files. 
Therefore, you need to have Spider installed on the client where this script is run. 

Example usage:
$ python spider2omero.py -h localhost -u root -p omero -d /Users/will/Documents/dev/SPIDER/Reference_based
    
@author  Will Moore &nbsp;&nbsp;&nbsp;&nbsp;
<a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
@version 3.0
<small>
(<b>Internal version:</b> $Revision: $Date: $)
</small>
@since 3.0-Beta4.2
 
"""

from Spider.Spiderarray import spider2array, getSpiderHeader

import numpy
import getopt, sys, os

import omero
import omero.constants
from omero.rtypes import *
import omero.util.script_utils as scriptUtil

spiderHeaderMap = {
1 : "NSLICE",   # Number of slices (planes) in volume (=1 for an image)  In some ancient 2D images this may be -1) 
2:"NROW",       # Number of rows per slice. 
3:"IREC",       # Total number of records (including header records) in each image of a simple image or stacked image file.
5:"IFORM",  # File type specifier.
6:"IMAMI",  # Maximum/minimum flag = 0 when the file is created, and = 1 when the maximum, minimum, average, and standard deviation have been computed and stored into this header record (see following locations).
7:"FMAX",  # Maximum data value.
8:"FMIN",  # Minimum data value.
9:"AV",  # Average data value.
10:"SIG",  # Standard deviation of data. A value of -1.0 or 0.0 indicates that SIG has not been computed.
12:"NSAM",  # Number of pixels (samples) per line.
13:"LABREC",  # Number of records in file header (label).
14:"IANGLE",  # Flag that following three tilt angles are present.
15:"PHI",  # Tilt angle: phi (See note #2 below).
16:"THETA",  # Tilt angle: theta.
17:"GAMMA",  # Tilt angle: gamma (also called psi).
18:"XOFF",  # X translation.
19:"YOFF",  # Y translation.
20:"ZOFF",  # Z translation.
21:"SCALE",  # Scale factor.
22:"LABBYT",  # Total number of bytes in header.
23:"LENBYT",  # Record length in bytes.
24:"ISTACK",   # Position has a value of 0 in simple 2D or 3D (non-stack) files. In an "image stack" there is one overall stack header followed by a stack of images, in which each image has its own image header. A value of >0 in this position in the overall stack header indicates a stack of images. A value of <0 in this position in the overall stack header indicates an indexed stack of images and gives the maximum image number (MAXINDX) allowed in the index. 
26:"MAXIM",  # Position is only used in the overall header for a stacked image file. There, this position contains the number of the highest image currently used in the stack. This number is updated, if necessary, when an image is added or deleted from the stack.
27:"IMGNUM",  # Position is only used in a stacked image header. There, this position contains the number of the current image or zero if this image is unused.
28:"LASTINDX",  # Position is only used in overall header of indexed stacks. There, this position is the highest index location currently in use.
31:"KANGLE",  # Flag that additional rotation angles follow in header. 1 = one angle set is present, 2 = two additional angle sets. These rotation angles preceed any rotation stored in positions: 15..18.
32:"PHI1",  # Angle.
33:"THETA1",  # Angle.
34:"PSI1",  # Angle.
35:"PHI2",  # Angle.
36:"THETA2",  # Angle.
37:"PSI2",  # Angle.
38:"PIXSIZ",  # Pixel size (Angstroms).
39:"EV",  # Electron voltage used.
101:"PSI3",  # Projection angle: Psi (From 'PJ 3Q').
102:"THETA3",  # Projection angle: Theta (From 'PJ 3Q').
103:"PHI3",  # Projection angle: Phi (From 'PJ 3Q').
104:"LANGLE",   # If = 1 flag that Projection angles: PSI3, THETA3 & PHI3 are present in header.
212:"CDAT", # Character *11 Creation date e.g. 27-MAY-1999 
213:"CDAT",
214:"CDAT",
215:"CTIM",	#Character *8 Creation time e.g. 09:43:19
216:"CTIM",
#217-256 	CTIT	Character *160 Title
}
    
def uploadImageToDataset(services, pixelsType, imageArray, imageName, dataset=None):
    
    """
    Uploads a local Spider image to an OMERO dataset. Same function exists in spider2omero.py.
    
    @param services     Map of OMERO services
    @param pixelsType   The OMERO PixelsType object for new image.
    @param imageArray   Numpy array of pixel data - 2D
    @param imageName    The local file, for getting image header info
    @param dataset      Dataset to put images in, if specified. omero.model.Dataset
    """

    session = services["session"]
    queryService = services["queryService"]
    updateService = services["updateService"]
    rawFileStore = services["rawFileStore"]
    
    namespace = omero.constants.namespaces.NSCOMPANIONFILE 
    fileName = omero.constants.annotation.file.ORIGINALMETADATA
    
    print "Importing image: %s" % imageName
    description = ""
    if len(imageArray.shape) > 2:
        plane2Dlist = imageArray    # 3D array already. TODO: Need to check that volume is not mirrored (Z in correct order)
    else:
        plane2Dlist = [imageArray]  # single plane image
    
    name = os.path.basename(imageName)
    image = scriptUtil.createNewImage(session, plane2Dlist, name, description, dataset)
    
    # header is a list of values corresponding to attributes 
    header = getSpiderHeader(imageName)
    
    # if we know the pixel size, set it in the new image
    if len(header) >= 38:
        physicalSizeX = header[38]
        physicalSizeY = header[38]
        pixels = image.getPrimaryPixels()
        pixels.setPhysicalSizeX(rdouble(physicalSizeX))
        pixels.setPhysicalSizeY(rdouble(physicalSizeY))
        updateService.saveObject(pixels)
    
    # make a temp text file. 
    f = open(fileName, 'w')
    f.write("[GlobalMetadata]\n")

    # now add image attributes as "Original Metadata", sorted by key. 
    for i, h in enumerate(header):
        if i in spiderHeaderMap:
            f.write("%s=%s\n" % (spiderHeaderMap[i], h))
            
    f.close()

    scriptUtil.uploadAndAttachFile(queryService, updateService, rawFileStore, image, fileName, "text/plain", None, namespace)
    # delete temp file
    os.remove(fileName)


def getPixelsType(queryService, numpyArray):
    """
    Get the OMERO pixelsType object appropriate for the numpy array data-type 
    """
    pType = numpyArray.dtype.name
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

def createDataset(updateService, project, datasetName):
    # create dataset
    dataset = omero.model.DatasetI()
    dataset.name = rstring(datasetName)
    dataset = updateService.saveAndReturnObject(dataset)
    # put dataset in project 
    link = omero.model.ProjectDatasetLinkI()
    link.parent = omero.model.ProjectI(project.id.val, False)
    link.child = omero.model.DatasetI(dataset.id.val, False)
    updateService.saveAndReturnObject(link)
    return dataset

def spiderToOmero(commandArgs):
    #print commandArgs
    client = omero.client(commandArgs["host"])
    session = client.createSession(commandArgs["username"], commandArgs["password"])
    
    # create the services we need
    services = {}
    services["session"] = session
    services["queryService"] = session.getQueryService()
    services["updateService"] = session.getUpdateService()
    services["rawFileStore"] = session.createRawFileStore()
    
    queryService = services["queryService"]
    updateService = services["updateService"]
    
    # get a name for the project 
    path = commandArgs["dir"]
    projectName = path
    print "Importing into Project:", projectName
    
    # create project
    project = omero.model.ProjectI()
    project.name = rstring(projectName)
    project = updateService.saveAndReturnObject(project)
    
    arg = path
    
    #processDir(services, path, path)
    def visit(arg, dirname, names):
        rootpath = arg
        datasetName = dirname.replace(rootpath, "")
        pixelsType = None   # set by first image result - assume all the same
        dataset = None
        for n in names:
            fullname = os.path.join(dirname, n)
            imageArray = None
            try:
                # attempt to read pixel data. Only way to tell if this is a Spider image? 
                imageArray = spider2array(fullname)
            except:
                print "."
            if imageArray != None:
                #print "   " , fullname, " IS an image"
                if pixelsType == None:   
                    pixelsType = getPixelsType(queryService, imageArray)
                if dataset == None:     
                    print "Dataset" , datasetName
                    dataset = createDataset(updateService, project, datasetName)
                uploadImageToDataset(services, pixelsType, imageArray, fullname, dataset)
        
    os.path.walk(path, visit, arg)
                    

def readCommandArgs():
    host = ""
    username = ""
    password = ""
    bdb = ""
    
    def usage():
        print "Usage: uploadscript --host host --username username --password password --dir dir"
    try:
        opts, args = getopt.getopt(sys.argv[1:] ,"h:u:p:d:", ["host=", "username=", "password=","dir="])
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
        elif opt in ("-d","--dir"): 
            returnMap["dir"] = arg 
    return returnMap

if __name__ == "__main__":        
    commandArgs = readCommandArgs()
    spiderToOmero(commandArgs)
    
    
