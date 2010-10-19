"""
 components/tools/OmeroPy/scripts/EMAN2/Nonlinear_Anisotropic_Diffusion.py 

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

This script uses command-line functionality of IMOD to do Nonlinear Anisotropic Diffusion on 
an image, generating one or more images. 

Example command:
nad_eed_3d -i 3,6 -k 102 BB-rec.mrc BB-rec-nad.mrc

See http://bio3d.colorado.edu/imod/doc/man/nad_eed_3d.html

The results of the script (Images) can then be uploaded back to OMERO in the same dataset
    
@author  Will Moore &nbsp;&nbsp;&nbsp;&nbsp;
<a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
@version 3.0
<small>
(<b>Internal version:</b> $Revision: $Date: $)
</small>
@since 3.0-Beta4.2.1
 
"""

import os
import numpy

import omero
import omero_api_Gateway_ice    # see http://tinyurl.com/icebuserror
import omero.scripts as scripts
from omero.rtypes import *
import omero.util.script_utils as scriptUtil

from EMAN2 import *     # used for downloading OMERO to mrc and back


def uploadImageToDataset(services, localImage, dataset=None, description="", imageName=None):
    
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
    rawPixelsStore = services["rawPixelsStore"]
    updateService = services["updateService"]
    rawFileStore = services["rawFileStore"]

    if imageName == None:  imageName = localImage
    print "Importing image: %s" % imageName
    
    em = EMData()
    em.read_image(localImage)
    
    npArray = EMNumPy.em2numpy(em)
    if len(npArray.shape) < 3:
        plane2Dlist = [npArray]
    else:
        plane2Dlist = npArray
    
    pType = npArray.dtype.name
    pixelsType = queryService.findByQuery("from PixelsType as p where p.value='%s'" % pType, None) # omero::model::PixelsType
    
    if pixelsType == None and pType.startswith("float"):
        # try 'float'
        pixelsType = queryService.findByQuery("from PixelsType as p where p.value='%s'" % "float", None) # omero::model::PixelsType
    if pixelsType == None:
        print "Unknown pixels type for: " % pType
        return
    else:
        print "Using pixels type ", pixelsType.getValue().getValue()
    
    image = scriptUtil.createNewImage(pixelsService, rawPixelsStore, renderingEngine, pixelsType, gateway, plane2Dlist, imageName, description, dataset)
    
    return image
    
        
def downloadImage(queryService, rawPixelsStore, imageId, imageName):
    """
    This method downloads the first (only?) plane of the OMERO image and saves it as a local image.
    
    @param session        The OMERO session
    @param imageId        The ID of the image to download
    @param imageName    The name of the image to write. If no path, saved in the current directory. 
    """

    print "Downloading image ID: %s to local file: %s using EMAN2" % (imageId, imageName)
    
    # get pixels with pixelsType
    query_string = "select p from Pixels p join fetch p.image i join fetch p.pixelsType pt where i.id='%s'" % imageId
    pixels = queryService.findByQuery(query_string, None)
    sizeX = pixels.getSizeX().getValue()
    sizeY = pixels.getSizeY().getValue()
    sizeZ = pixels.getSizeZ().getValue()
    
    em = EMData(sizeX,sizeY,sizeZ)

    # get the planes
    pixelsId = pixels.getId().getValue()
    bypassOriginalFile = True
    rawPixelsStore.setPixelsId(pixelsId, bypassOriginalFile)
    theC, theT = (0,0)
    for theZ in range(sizeZ):
        plane2D = scriptUtil.downloadPlane(rawPixelsStore, pixels, theZ, theC, theT)
        e = EMNumPy.numpy2em(plane2D)
        em.insert_clip(e,(0,0,theZ))
    
    em.write_image(imageName)
    
    
def runNAD(session, parameterMap):
    """
    This is where the action happens.
    We get the parameters, download the images, run nad_eed_3d from command line. 
    Then we get the output from the folder where the results should be, and upload these
    images into a new (or same) dataset. 
    """
    
    # create services we need 
    services = {}
    services["gateway"] = session.createGateway()
    services["renderingEngine"] = session.createRenderingEngine()
    services["queryService"] = session.getQueryService()
    services["pixelsService"] = session.getPixelsService()
    services["rawPixelsStore"] = session.createRawPixelsStore()
    services["updateService"] = session.getUpdateService()
    services["rawFileStore"] = session.createRawFileStore()
    
    queryService = services["queryService"]
    gateway = services["gateway"]
    rawFileStore = services["rawFileStore"]
    rawPixelsStore = services["rawPixelsStore"]
    
    imageId = parameterMap["Image_ID"]
    
    # get the dataset from the first image
    dataset = None
    query_string = "select i from Image i join fetch i.datasetLinks idl join fetch idl.parent where i.id=%s" % imageId
    image = queryService.findByQuery(query_string, None)
    if image:
        for link in image.iterateDatasetLinks():
            dataset = link.parent
            print "Dataset", dataset.name.val
            break    # only use 1st Dataset
    
    
    # build the command for nad_eed_3d
    iters = [str(i.getValue())  for i in parameterMap["Iterations"]]
    iters.sort()    # in case user entered out-of-order.
    i = ",".join(iters)
    k = parameterMap["K_Value"]
    
    inputImage = "input.mrc"
    outputImage = "output-nad.mrc"  # will produce output-nad.mrc-006, output-nad.mrc-008 etc for each value of i
    
    nadCommand = "nad_eed_3d -i %s -k %s %s %s" % (i, k, inputImage, outputImage)
    
    # download the image from OMERO, using EMAN2 to write the mrc
    downloadImage(queryService, rawPixelsStore, imageId, inputImage)
    
    # run the command, generating mrc images named wrt outputImage
    print "Attempting to run IMOD with command \n%s" % nadCommand
    os.system(nadCommand)
        
    url = "http://bio3d.colorado.edu/imod/doc/man/nad_eed_3d.html"
    description = "Created from Image ID: %s with the IMOD command: \n%s\nSee: %s" % (imageId, nadCommand, url)
    
    path = os.getcwd()
    outputImages = []
    for fileName in os.listdir(path):
        if fileName.startswith(outputImage):
            image = uploadImageToDataset(services, fileName, dataset, description)
            outputImages.append(image)

    outputImages.sort(key=lambda image: image.getName().getValue())     # sort by name
    return (outputImages, dataset)
    

def runAsScript():
    """
    The main entry point of the script, as called by the client via the scripting service, passing the required parameters. 
    Calls the Spider command line.
    
    """
    
    client = scripts.client('Nonlinear_Anisotropic_Diffusion.py', """Nonlinear Anisotropic Diffusion against Images on OMERO.
Uses command line functionality from IMOD. See http://bio3d.colorado.edu/imod/doc/man/nad_eed_3d.html""", 
    scripts.Long("Image_ID", optional=False, grouping="1",
        description="ID of the image you want to work with."),
    scripts.Float("K_Value", optional=False, grouping="2",
        description="The K value, or lambda to apply to the processing."),
    scripts.List("Iterations", optional=False, grouping="3",
        description="The iteration number(s) to write a result.").ofType(rint(0)),
    )
    
    try:
        session = client.getSession()
    
        # process the list of args above. 
        parameterMap = {}
        for key in client.getInputKeys():
            if client.getInput(key):
                parameterMap[key] = client.getInput(key).getValue()
    
        images, dataset = runNAD(session, parameterMap)
        
        if len(images) == 0:
            client.setOutput("Message", rstring("Script failed. IMOD not installed on server? See Errors"))
        else:
            print [i.getName().getValue() for i in images]
            client.setOutput("Message", rstring("Script created %s images in current Dataset" % len(images)))
            for i in images:    # return all the images 
                client.setOutput("Image_%s" % i.getId().getValue(),robject(i))
            if dataset:
                client.setOutput("Dataset",robject(dataset))
    finally:
        client.closeSession()

if __name__ == "__main__":
    runAsScript()