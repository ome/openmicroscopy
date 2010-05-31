"""
 components/tools/OmeroPy/scripts/EMAN2/Run_Spider_Procedure.py 

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

This script allows users to run a SPIDER procedure file (spf) 
on the OMERO server against a collection of images, and upload the results back to the server.

************************************************************************
    WARNING
Allowing users (non admin) to upload Spider scripts of their choice and 
run them with this script is a potential security risk, particularly if
this script has been installed by an admin and is run with admin permissions. 
For example, they may be able to delete files on the OMERO server
 (although only those with "dat" extension)
************************************************************************

The spf should already have been uploaded to the server as an Original File and the ID is
passed to this script. 

A list of OMERO image IDs (or dataset ID) defines which images we want to work with. These 
will be downloaded to the script folder as Spider images with the appropriate extension, E.g. "dat"

Spider is called by command line: E.g. "spider spf/dat @myProcedure"

The results of the script (Images) can then be uploaded back to OMERO in a new dataset (or same dataset)
    
@author  Will Moore &nbsp;&nbsp;&nbsp;&nbsp;
<a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
@version 3.0
<small>
(<b>Internal version:</b> $Revision: $Date: $)
</small>
@since 3.0-Beta4.2
 
"""

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

import os
import numpy

from Spider.Spiderarray import array2spider, spider2array, getSpiderHeader

import omero
import omero_api_Gateway_ice    # see http://tinyurl.com/icebuserror
import omero.scripts as scripts
from omero.rtypes import *
import omero.util.script_utils as scriptUtil
#from omero.util.script_utils import *


# keep track of log strings. 
logStrings = []

def log(text):
    """
    Adds the text to a list of logs. Compiled into figure legend at the end.
    """
    #print text
    logStrings.append(text)
    

def uploadImageToDataset(services, pixelsType, imageName, dataset=None, description=""):
    
    """
    Uploads a local Spider image to an OMERO dataset. Same function exists in spider2omero.py.
    
    @param services     Map of OMERO services
    @param pixelsType   The OMERO PixelsType object for new image.
    @param imageName    The local image path/name. Also used for new image name. 
    @param dataset      Dataset to put images in, if specified. omero.model.Dataset
    """
    
    gateway = services["gateway"]
    renderingEngine = services["renderingEngine"]
    queryService = services["queryService"]
    pixelsService = services["pixelsService"]
    rawPixelStore = services["rawPixelStore"]
    updateService = services["updateService"]
    rawFileStore = services["rawFileStore"]

    
    namespace = omero.constants.namespaces.NSCOMPANIONFILE 
    fileName = "original_metadata.txt"
    
    print "Importing image: %s" % imageName
    plane2D = spider2array(imageName)
    plane2Dlist = [plane2D]        # single plane image
    
    image = scriptUtil.createNewImage(pixelsService, rawPixelStore, renderingEngine, pixelsType, gateway, plane2Dlist, imageName, description, dataset)
    
    # header is a list of values corresponding to attributes 
    header = getSpiderHeader(imageName)
    
    # if we know the pixel size, set it in the new image
    if len(header) >= 38:
        physicalSizeX = header[38]
        physicalSizeY = header[38]
        pixels = image.getPrimaryPixels()
        pixels.setPhysicalSizeX(rdouble(physicalSizeX))
        pixels.setPhysicalSizeY(rdouble(physicalSizeY))
        gateway.saveObject(pixels)
    
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
    return image

        
def downloadImage(queryService, rawPixelStore, imageId, imageName):
    """
    This method downloads the first (only?) plane of the OMERO image and saves it as a local image.
    
    @param session        The OMERO session
    @param imageId        The ID of the image to download
    @param imageName    The name of the image to write. If no path, saved in the current directory. 
    """

    # get pixels with pixelsType
    query_string = "select p from Pixels p join fetch p.image i join fetch p.pixelsType pt where i.id='%d'" % imageId
    pixels = queryService.findByQuery(query_string, None)
    theX = pixels.getSizeX().getValue()
    theY = pixels.getSizeY().getValue()

    # get the plane
    theZ, theC, theT = (0,0,0)
    pixelsId = pixels.getId().getValue()
    bypassOriginalFile = True
    rawPixelStore.setPixelsId(pixelsId, bypassOriginalFile)
    plane2D = scriptUtil.downloadPlane(rawPixelStore, pixels, theZ, theC, theT)
    
    plane2D.resize((theY, theX))        # not sure why we have to resize (y, x)
    array2spider(plane2D, imageName)
    
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
    
def runSpf(session, parameterMap):
    """
    This is where the action happens.
    We get the parameters, download the images, run Spider file from command line. 
    Then we get the output from the folder where the results should be, and upload these
    images into a new dataset. 
    """
    # create services we need 
    services = {}
    services["gateway"] = session.createGateway()
    services["renderingEngine"] = session.createRenderingEngine()
    services["queryService"] = session.getQueryService()
    services["pixelsService"] = session.getPixelsService()
    services["rawPixelStore"] = session.createRawPixelsStore()
    services["updateService"] = session.getUpdateService()
    services["rawFileStore"] = session.createRawFileStore()
    
    queryService = services["queryService"]
    gateway = services["gateway"]
    rawFileStore = services["rawFileStore"]
    rawPixelStore = services["rawPixelStore"]
    
    imageIds = []
    
    if "imageIds" in parameterMap:
        for imageId in parameterMap["imageIds"]:
            iId = long(imageId.getValue())
            imageIds.append(iId)
    
    elif "datasetId" in parameterMap:
        datasetId = parameterMap["datasetId"]
        images = gateway.getImages(omero.api.ContainerClass.Dataset, [datasetId])
        for i in images:
            imageIds.append(i.getId().getValue())
            
    if len(imageIds) == 0:
        return
        
        
    # get the project from the first image
    project = None
    dataset = None
    imageId = imageIds[0]
    query_string = "select i from Image i join fetch i.datasetLinks idl join fetch idl.parent d join fetch d.projectLinks pl join fetch pl.parent where i.id in (%s)" % imageId
    image = queryService.findByQuery(query_string, None)
    if image:
        for link in image.iterateDatasetLinks():
            dataset = link.parent
            print "Dataset", dataset.name.val
            for dpLink in dataset.iterateProjectLinks():
                project = dpLink.parent
                print "Project", project.name.val
                break # only use 1st Project
            break    # only use 1st Dataset
    
    if "newDatasetName" in parameterMap:
        # make a dataset for images
        dataset = omero.model.DatasetI()
        dataset.name = rstring(parameterMap["newDatasetName"])
        dataset = gateway.saveAndReturnObject(dataset)
        if project:        # and put it in the same project
            link = omero.model.ProjectDatasetLinkI()
            link.parent = omero.model.ProjectI(project.id.val, False)
            link.child = omero.model.DatasetI(dataset.id.val, False)
            gateway.saveAndReturnObject(link)
    
    
    
    fileExt = "dat"
        
    inputName = "input"
    if "inputName" in parameterMap:
        inputName = parameterMap["inputName"]
    outputName = "output"
    if "outputName" in parameterMap:
        outputName = parameterMap["outputName"]
            
    # download the procdure file
    spfName = "procedure.spf"
    spfFileId = parameterMap["spfFileId"]
    annotation = queryService.get('FileAnnotation', spfFileId)
    origFileId = annotation.file.id.val
    originalFile = queryService.findByQuery("from OriginalFile as o where o.id = %s" % origFileId, None)
    scriptUtil.downloadFile(rawFileStore, originalFile, filePath=spfName)
    # run command. E.g. spider spf/dat @bat01
    spfCommand = "spider spf/%s @procedure" % fileExt
    
    spfText = open(spfName, 'r').read()
    # for each image, download it, run the spider command and upload result to OMERO
    inputImage = "%s.%s" % (inputName, fileExt)
    outputImage = "%s.%s" % (outputName, fileExt)
    pixelsType = None   # set by first image result - assume all the same
    for i, imageId in enumerate(imageIds):
        downloadImage(queryService, rawPixelStore, imageId, inputImage)
        os.system(spfCommand)
        if pixelsType == None:      pixelsType = getPixelsType(queryService, outputImage)
        description = "Created from Image ID: %s with the Spider Procedure\n%s" % (imageId, spfText)
        image = uploadImageToDataset(services, pixelsType, outputImage, dataset, description)
        # attach Spf to new image
        scriptUtil.attachFileToParent(services["updateService"], image, originalFile)

def runAsScript():
    """
    The main entry point of the script, as called by the client via the scripting service, passing the required parameters. 
    Calls the Spider command line.
    
    """
    client = scripts.client('Run_Spider_Procedure.py', """Run a Spider Procedure File against Images on OMERO.
See http://trac.openmicroscopy.org.uk/omero/wiki/EmPreviewFunctionality""", 
    scripts.List("imageIds", optional=True).inout(),    # List of image IDs. Use this OR datasetId
    scripts.Long("datasetId", optional=True).inout(),    # Dataset Id. Use this OR imageIds
    scripts.Long("spfFileId").inout(),         # the FileAnnotation-ID of the Spider Procedure File
    scripts.String("newDatasetName", optional=True).inout(),     # Make a dataset to put results. Otherwise put in same as input images.
    scripts.String("inputName", optional=True).inout(),     # The name of the input image at the start of the spf file. Default is "input"
    scripts.String("outputName", optional=True).inout())    # The name of the output image at the end of the spf file. Default is "output"
    
    session = client.getSession()
    
    # process the list of args above. 
    parameterMap = {}
    for key in client.getInputKeys():
        if client.getInput(key):
            parameterMap[key] = client.getInput(key).getValue()
    
    runSpf(session, parameterMap)
    
if __name__ == "__main__":
    runAsScript()