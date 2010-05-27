"""
 components/tools/OmeroPy/scripts/EMAN2/eman2omero.py 

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

This script uses EMAN2 to read images from an EMAN2 bdb and upload them to OMERO as new images.
It should be run as a local script (not via scripting service) in order that it has
access to the local users bdb repository. 
Therefore, you need to have EMAN2 installed on the client where this script is run. 
The bdb repository should be specified as described on http://blake.bcm.edu/emanwiki/Eman2DataStorage 

The way you specify an image inside one of these databases is any of:
For a database in the local directory: bdb:dbname
For a database in another directory referenced to the current one: bdb:../local/path#dbname
For a database at an absolute path: bdb:/absolute/path/to/directory#dbname

Example usage:
wjm:EMAN2 will$ python eman2omero.py -h localhost -u root -p omero -b /Users/will/Documents/EM-data/EMAN2-tutorial/eman_demo/raw_data/
This will upload raw images (not in bdb) that are in the /raw_data/ folder, 
and will also upload images from bdb that are in subfolders of /raw_data/, e.g. /raw_data/particles#1160_ptcls

wjm:EMAN2 will$ python eman2omero.py -h localhost -u root -p omero -b /Users/will/Documents/EM-data/EMAN2-tutorial/eman_demo/raw_data/particles#1160_ptcls
Uploads the images in the 1160_ptcls bdb to a dataset called "1160_ptcls"
    
@author  Will Moore &nbsp;&nbsp;&nbsp;&nbsp;
<a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
@version 3.0
<small>
(<b>Internal version:</b> $Revision: $Date: $)
</small>
@since 3.0-Beta4.2
 
"""

from EMAN2 import *

import numpy
import getopt, sys, os

import omero
import omero.constants
from omero.rtypes import *
import omero_api_Gateway_ice    # see http://tinyurl.com/icebuserror
import omero.util.script_utils as scriptUtil

# declare the global services here, so we don't have to pass them around so much
gateway = None
queryService= None
pixelsService= None
rawPixelStore= None
re= None
updateService= None
rawFileStore= None

demo = True
newImageMap = {}    # map of imported images. EMAN-ID : OMERO-ID

# map between extension of particle sets bdb ("data" or "flipped" or "filtered") and original particle bdb extensions
particleSetExtMap = {"data": "ptcls", "flipped":"flip", "filtered":"wiener"}
    
def uploadBdbAsDataset(infile, dataset):
    
    """
    @param infile       path to bdb (absolute OR from where we are running) OR this can be a list of image paths. 
    @param dataset      Dataset to put images in (omero.model.DatasetI)
    """

    imageList = None
    # particleExt will be "ptcls" or "flip" or "wiener" if we are importing original particles
    # particleExt will be "data" or "flipped" or "filtered" if we are importing sets particles
    particleExt = None
    nimg = 0
    try:
        nimg = EMUtil.get_image_count(infile)    # eg images in bdb 'folder'
        particleExt = infile.split("_")[-1]
        print "Found %d %s images to import from: %s to dataset: %s" % (nimg, particleExt, infile, dataset.name.val)
    except:
        nimg = len(infile)    # OK, we're probably dealing with a list
        imageList = infile
        print "Importing %d images to dataset: %s" % (nimg, dataset.name.val)
    
    if nimg == 0:
        return
        
    d = EMData()
    # use first image to get data-type (assume all the same!)
    if imageList:
        d.read_image(imageList[0])
    else:
        d.read_image(infile, 0)
    plane2D = EMNumPy.em2numpy(d)
    pType = plane2D.dtype.name
    print pType
    pixelsType = queryService.findByQuery("from PixelsType as p where p.value='%s'" % pType, None) # omero::model::PixelsType
    
    if pixelsType == None and pType.startswith("float"):
        # try 'float'
        pixelsType = queryService.findByQuery("from PixelsType as p where p.value='%s'" % "float", None) # omero::model::PixelsType
    if pixelsType == None:
        print "Unknown pixels type for: " % pType
        return
    else:
        print "Using pixels type ", pixelsType.getValue().getValue()
    
    namespace = omero.constants.namespaces.NSCOMPANIONFILE 
    fileName = "original_metadata.txt"
    
    # loop through all the images.
    if demo: nimg = min(2, nimg) 
    for i in range(nimg):
        description = "Imported from EMAN2 bdb: %s" % infile
        newImageName = ""
        if imageList:
            h, newImageName = os.path.split(imageList[i])
            print "\nReading image:", imageList[i]
            d.read_image(imageList[i])
        else:
            newImageName = "%d" % i
            print "\nReading image: %s / %s" % (i, nimg)
            d.read_image(infile, i)
        plane2D = EMNumPy.em2numpy(d)
        #display(d)
        #plane2D *= 100     # temporary hack to avoid rendering problem with small numbers. 
        #planeMin = int(plane2D.min())
        #plane2D -= planeMin     # make min = 0
        #print plane2D
        plane2Dlist = [plane2D]        # single plane image
        
        # test attributes for source image link
        attributes = d.get_attr_dict()
        particleSource = ""
        if "ptcl_source_image" in attributes:
            parentName = attributes["ptcl_source_image"]
            newImageName = parentName   # name the particle after it's parent
            description = description + "\nSource Image: %s" % parentName
            particleSource += parentName
            if parentName in newImageMap:
                #print "Add link to image named: ", parentName
                # simply add to description, since we don't have Image-Image links yet
                description = description + "\nSource Image ID: %s" % newImageMap[parentName]
        if "ptcl_source_coord" in attributes:
            try:
                x, y = attributes["ptcl_source_coord"]
                particleSource = "%s.%d.%d" % (particleSource, x, y)
                xCoord = float(x)
                yCoord = float(y)
                description = description + "\nSource Coordinates: %.1f, %.1f" % (xCoord, yCoord)
            except: pass
        
        print "particleSource", particleSource
        # if this particle has been imported already, simple put it in the dataset...
        if "data_path" in attributes:
            if particleExt in particleSetExtMap:    # E.g. "data" 
                originalParticleExt = particleSetExtMap[particleExt]    # E.g. "ptcls"
                particleSource += ".%s" % originalParticleExt
                if particleSource in newImageMap:
                    print particleSource, "already imported..."
                    particleId = newImageMap[particleSource]
            
                    link = omero.model.DatasetImageLinkI()
                    link.parent = omero.model.DatasetI(dataset.id.val, False)
                    link.child = omero.model.ImageI(particleId, False)
                    gateway.saveAndReturnObject(link)
                    continue
        
        # create new Image from numpy data.
        print "Creating image in OMERO and uploading data..."
        image = scriptUtil.createNewImage(pixelsService, rawPixelStore, re, pixelsType, gateway, plane2Dlist, newImageName, description, dataset)
        imageId = image.getId().getValue()
        
        # if we know the pixel size, set it in the new image
        if "apix_x" in attributes:
            physicalSizeX = float(attributes["apix_x"])
            print "physicalSizeX" , physicalSizeX
            if "apix_y" in attributes:
                physicalSizeY = float(attributes["apix_y"])
                print "physicalSizeY" , physicalSizeY
            else:
                physicalSizeY = physicalSizeX
            pixels = image.getPrimaryPixels()
            pixels.setPhysicalSizeX(rdouble(physicalSizeX))
            pixels.setPhysicalSizeY(rdouble(physicalSizeY))
            gateway.saveObject(pixels)
             
        # make a map of name: imageId, for creating image links
        if particleSource:
            particleSource += ".%s" % particleExt
            print particleSource, "added to map"
            newImageMap[particleSource] = imageId
        else:
            print newImageName, "added to map"
            newImageMap[newImageName] = imageId
        
        f = open(fileName, 'w')        # will overwrite each time. 
        f.write("[GlobalMetadata]\n")
        
        # now add image attributes as "Original Metadata", sorted by key. 
        keyList = list(attributes.keys())    
        keyList.sort()
        for k in keyList:
            #print k, attributes[k]
            f.write("%s=%s\n" % (k, attributes[k]))
        f.close()
        
        scriptUtil.uploadAndAttachFile(queryService, updateService, rawFileStore, image, fileName, "text/plain", None, namespace)
    # delete temp file
    if os.path.exists(fileName):    os.remove(fileName)
    
def importMicrographs(path, datasetName="raw_data"):
    """ Imports all the image files in the given directory that can be read by EMAN2 """
    imageList = []
    i = EMData()
    for f in os.listdir(path):
        fullpath = path + f
        # ignore folders in root dir:
        if not os.path.isdir(fullpath):    # e.g. 'particles' folder
            try:
                i.read_image(fullpath, 0, True)    # header only 
                #print " is an image"
                imageList.append(fullpath)
            except:
                print "."
                
    print "Uploading image list: "
    print imageList          
    
    dataset = omero.model.DatasetI()
    dataset.name = rstring(datasetName)
    dataset = gateway.saveAndReturnObject(dataset)
    if project:        # and put it in a new project
        link = omero.model.ProjectDatasetLinkI()
        link.parent = omero.model.ProjectI(project.id.val, False)
        link.child = omero.model.DatasetI(dataset.id.val, False)
        gateway.saveAndReturnObject(link)  
    uploadBdbAsDataset(imageList, dataset)
                

def importParticles(dbpath, bdbExt=None, datasetName=None, project=None):
    """ Imports particles from bdbs in the specified folder into a new dataset """
    if dbpath.lower()[:4]!="bdb:" : dbpath="bdb:"+dbpath
    if not '#' in dbpath and dbpath[-1]!='/' : dbpath+='#'            
    #if len(args)>1 : print "\n",path[:-1],":"
    dbs=db_list_dicts(dbpath)
    
    dataset = None
    # create dataset
    if datasetName:
        dataset = createDataset(datasetName, project)
    
    # import selected folders
    print "Uploading bdbs ", dbs
    for db in dbs:
        if bdbExt == None or db.endswith(bdbExt):
            infile = dbpath + db
            if datasetName == None: dataset = createDataset(db, project)
            uploadBdbAsDataset(infile, dataset)


def createDataset(datasetName, project=None):
    dataset = omero.model.DatasetI()
    dataset.name = rstring(datasetName)
    dataset = gateway.saveAndReturnObject(dataset)
    if project:        # and put it in a new project
        link = omero.model.ProjectDatasetLinkI()
        link.parent = omero.model.ProjectI(project.id.val, False)
        link.child = omero.model.DatasetI(dataset.id.val, False)
        gateway.saveAndReturnObject(link)
    return dataset


def emanToOmero(commandArgs):
    #print commandArgs
    client = omero.client(commandArgs["host"])
    session = client.createSession(commandArgs["username"], commandArgs["password"])
    
    global gateway
    global re
    global queryService
    global pixelsService
    global rawPixelStore
    global updateService
    global rawFileStore
    
    gateway = session.createGateway()
    re = session.createRenderingEngine()
    queryService = session.getQueryService()
    pixelsService = session.getPixelsService()
    rawPixelStore = session.createRawPixelsStore()
    updateService = session.getUpdateService()
    rawFileStore = session.createRawFileStore()
    
    path = commandArgs["bdb"]
    
    # get a name for the project 
    head,tail = os.path.split(path)
    projectName = tail
    if projectName == "":
        projectName = head
    # create project
    project = omero.model.ProjectI()
    project.name = rstring(projectName)
    project = gateway.saveAndReturnObject(project)
    
    # import the micrographs in the root folder  
    #importMicrographs(path)
    
    # import particles into 3 datasets, "particles", "ctf", "wiener"
    importParticles(path + "particles", bdbExt="ptcls", datasetName="particles", project=project)
    importParticles(path + "particles", bdbExt="ctf_flip", datasetName="ctf", project=project)
    importParticles(path + "particles", bdbExt="ctf_wiener", datasetName="wiener", project=project)
    
    # make a dataset from each particle set (only do '...filtered' particle sets)
    importParticles(path + "sets", bdbExt="filtered", project=project)
    
    
    return
    
    # ignore directories that don't have interesting images in 
    utilDirs = ["e2boxercache", "EMAN2DB"]
    # process directories in root dir. 
    imageList = []        # put any image names here
    dbs = []            # and any bdbs here
    if os.path.isdir(path):
        for f in os.listdir(path):
            fullpath = path + f
            #print fullpath
            # process folders in root dir:
            if f not in utilDirs and os.path.isdir(fullpath):    # e.g. 'particles' folder
                dbpath = fullpath
                if dbpath.lower()[:4]!="bdb:" : dbpath="bdb:"+dbpath
                if '#' in dbpath :
                    #if len(args)>1 : print "\n",path,":"
                    dbpath,db=dbpath.rsplit("#",1)
                    dbpath+="#"
                    dbs=[db]
                else:
                    if not '#' in dbpath and dbpath[-1]!='/' : dbpath+='#'            
                    #if len(args)>1 : print "\n",path[:-1],":"
                    dbs=db_list_dicts(dbpath)
            
            # process files in root dir:
            else:
                i = EMData()
                try:
                    i.read_image(fullpath, 0, True)    # header only 
                    #print " is an image"
                    imageList.append(fullpath)
                except:
                    print "."
                    
    # first, upload any root images    
    print "Uploading image list: "
    print imageList     
    print dbs
    return        
    #uploadBdbAsDataset(imageList, "raw_data", project)
    
    # and now do any bdbs in root folder subdirectories
    print "Uploading bdbs "
    for db in dbs:
        infile = dbpath + db
        datasetName = db
        uploadBdbAsDataset(infile, datasetName, project)
    
    # code below handles cases where we are starting at a bdb (not a directory)
    # code from e2bdb.py
    dbpath = path
    if dbpath.lower()[:4]!="bdb:" : dbpath="bdb:"+dbpath
    if '#' in dbpath :
        #if len(args)>1 : print "\n",path,":"
        dbpath,dbs=dbpath.rsplit("#",1)
        dbpath+="#"
        dbs=[dbs]
    else:
        if not '#' in dbpath and dbpath[-1]!='/' : dbpath+='#'            
        #if len(args)>1 : print "\n",path[:-1],":"
        dbs=db_list_dicts(dbpath)
    
    print "List of dbs in root directory:"
    print dbs        # may not be any dbs in root (e.g. spr root directory has no dbs)
    for db in dbs:
        infile = dbpath + db
        datasetName = db
        uploadBdbAsDataset(infile, datasetName, project)
    

def readCommandArgs():
    host = ""
    username = ""
    password = ""
    bdb = ""
    
    def usage():
        print "Usage: uploadscript --host host --username username --password password --bdb bdb"
    try:
        opts, args = getopt.getopt(sys.argv[1:] ,"h:u:p:b:", ["host=", "username=", "password=","bdb="])
    except getopt.GetoptError, err:          
        usage()                         
        sys.exit(2)                     
    for opt, arg in opts: 
        if opt in ("-h","--host"):
            host = arg;
        elif opt in ("-u","--username"): 
            username = arg;    
        elif opt in ("-p","--password"): 
            password = arg;    
        elif opt in ("-b","--bdb"): 
            bdb = arg;    
    returnMap = {"host":host, "username":username, "password":password, "bdb":bdb}
    return returnMap, args

if __name__ == "__main__":        
    commandArgs, args = readCommandArgs()
    #global demo
    demo = "demo" in args
    emanToOmero(commandArgs)
    
    
