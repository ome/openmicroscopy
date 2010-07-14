#
#
#------------------------------------------------------------------------------
#  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
#
#
#   This program is free software; you can redistribute it and/or modify
#  it under the terms of the GNU General Public License as published by
#  the Free Software Foundation; either version 2 of the License, or
#  (at your option) any later version.
#  This program is distributed in the hope that it will be useful,
#  but WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#  GNU General Public License for more details.
#
#  You should have received a copy of the GNU General Public License along
#  with this program; if not, write to the Free Software Foundation, Inc.,
#  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
#
#------------------------------------------------------------------------------
###
#
# Utility methods for deal with scripts.
#
# @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
#   <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
# @author   Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
#   <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
# @author   Will Moore &nbsp;&nbsp;&nbsp;&nbsp;
#   <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
# @version 3.0
# <small>
# (<b>Internal version:</b> $Revision: $Date: $)
# </small>
# @since 3.0-Beta4
#
import getopt, sys, os, subprocess
import numpy;
from struct import *

import omero.clients
import omero_Constants_ice
from omero.rtypes import *
import omero.util.pixelstypetopython as pixelstypetopython
from omero.util.OmeroPopo import EllipseData as EllipseData
from omero.util.OmeroPopo import RectData as RectData
from omero.util.OmeroPopo import MaskData as MaskData
from omero.util.OmeroPopo import WorkflowData as WorkflowData
from omero.util.OmeroPopo import ROIData as ROIData


try: 
    import hashlib 
    hash_sha1 = hashlib.sha1 
except: 
    import sha 
    hash_sha1 = sha.new 
    
# r,g,b,a colours for use in scripts. 
COLOURS = {'Red': (255,0,0,255), 'Green': (0,255,0,255), 'Blue': (0,0,255,255), 'Yellow': (255,255,0,255), 
    'White': (255,255,255,255), }
    
EXTRA_COLOURS = {'Violet': (238,133,238,255), 'Indigo': (79,6,132,255),
    'Black': (0,0,0,255), 'Orange': (254,200,6,255), 'Gray': (130,130,130,255),}

CSV_NS = 'text/csv';
CSV_FORMAT = 'text/csv';

def drawTextOverlay(draw, x, y, text, colour='0xffffff'):
    """
    Draw test on image.
    @param draw The PIL Draw class.
    @param x The x-coord to draw.
    @param y The y-coord to draw.
    @param text The text to render.
    @param colour The colour as a PIL colour string to draw the text in.
    """
    draw.text((x,y),text, fill=colour)

def drawLineOverlay(draw, x0, y0, x1, y1, colour='0xffffff'):
    """
    Draw line on image.
    @param draw The PIL Draw class.
    @param x0 The x0-coord of line.
    @param y0 The y0-coord of line.
    @param x1 The x1-coord of line.
    @param y1 The y1-coord of line.
    @param colour The colour as a PIL colour string to draw the text in.
    """
    draw.line([(x0, y0),(x1,y1)], text, fill=colour)

def rgbToRGBInt(red, green, blue):
    """
    Convert an R,G,B value to an int.
    @param R the Red value.
    @param G the Green value.
    @param B the Blue value.
    @return See above.
    """
    RGBInt = (red<<16)+(green<<8)+blue;
    return int(RGBInt);
    
def RGBToPIL(RGB):
    """
    Convert an RGB value to a PIL colour value.
    @param RGB the RGB value.
    @return See above.
    """
    hexval = hex(int(RGB));
    return '#'+(6-len(hexval[2:]))*'0'+hexval[2:];

def rangeToStr(range):
    """
    Map a range to a string of numbers
    @param range See above.
    @return See above.
    """
    first = 1;
    string = "";
    for value in range:
        if(first==1):
            string = str(value);
            first = 0;
        else:
            string = string + ','+str(value)
    return string;

def rmdir_recursive(dir):
    for name in os.listdir(dir):
        full_name = os.path.join(dir, name)
        # on Windows, if we don't have write permission we can't remove
        # the file/directory either, so turn that on
        if not os.access(full_name, os.W_OK):
            os.chmod(full_name, 0600)
        if os.path.isdir(full_name):
            rmdir_recursive(full_name)
        else:
            os.remove(full_name)
    os.rmdir(dir)

def calcSha1(filename):
    """
    Returns a hash of the file identified by filename
    
    @param  filename:   pathName of the file
    @return:            The hash of the file
    """
    
    fileHandle = open(filename)
    h = hash_sha1()
    h.update(fileHandle.read())
    hash = h.hexdigest()
    fileHandle.close()
    return hash;    

def calcSha1FromData(data):
    """
    Calculate the Sha1 Hash from a data array
    @param data The data array.
    @return The Hash
    """
    h = hash_sha1()
    h.update(data)
    hash = h.hexdigest()
    return hash;

def getFormat(queryService, format):
    return queryService.findByQuery("from Format as f where f.value='"+format+"'", None)


def createFile(updateService, filename, mimetype=None, origFilePathName=None):
    """
    Creates an original file, saves it to the server and returns the result
    
    @param queryService:    The query service  E.g. session.getQueryService()
    @param updateService:   The update service E.g. session.getUpdateService()
    @param filename:        The file path and name (or name if in same folder). String
    @param mimetype:        The mimetype (string) or Format object representing the file format
    @param origFilePathName:       Optional path/name for the original file
    @return:                The saved OriginalFileI, as returned from the server
    """
    
    originalFile = omero.model.OriginalFileI();
    if(origFilePathName == None):
        origFilePathName = filename;
    path, name = os.path.split(origFilePathName)
    originalFile.setName(omero.rtypes.rstring(name));
    originalFile.setPath(omero.rtypes.rstring(path));
    # just in case we are passed a FormatI object
    try:
        v = mimetype.getValue()
        mt = v.getValue()
    except:
        # handle the string we expect 
        mt = mimetype
    if mt:
        originalFile.mimetype = omero.rtypes.rstring(mt)
    originalFile.setSize(omero.rtypes.rlong(os.path.getsize(filename)));
    originalFile.setSha1(omero.rtypes.rstring(calcSha1(filename)));
    return updateService.saveAndReturnObject(originalFile); 
    

def uploadFile(rawFileStore, originalFile, filePath=None):
    """
    Uploads an OriginalFile to the server
    
    @param rawFileStore:    The Omero rawFileStore
    @param originalFile:    The OriginalFileI
    @param filePath:    Where to find the file to upload. If None, use originalFile.getName().getValue()
    """
    rawFileStore.setFileId(originalFile.getId().getValue());
    fileSize = originalFile.getSize().getValue();
    increment = 10000;
    cnt = 0;
    if filePath == None:
        filePath = originalFile.getName().getValue()
    fileHandle = open(filePath, 'rb');
    done = 0
    while(done!=1):
        if(increment+cnt<fileSize):
            blockSize = increment;
        else:
            blockSize = fileSize-cnt;
            done = 1;
        fileHandle.seek(cnt);
        block = fileHandle.read(blockSize);
        rawFileStore.write(block, cnt, blockSize);
        cnt = cnt+blockSize;
    fileHandle.close();


def downloadFile(rawFileStore, originalFile, filePath=None):
    """
    Downloads an OriginalFile from the server.
    
    @param rawFileStore:    The Omero rawFileStore
    @param originalFile:    The OriginalFileI
    @param filePath:    Where to download the file. If None, use originalFile.getName().getValue()
    """
    fileId = originalFile.getId().getValue()
    rawFileStore.setFileId(fileId)
    fileSize = originalFile.getSize().getValue()
    maxBlockSize = 10000
    cnt = 0
    if filePath == None:
        filePath = originalFile.getName().getValue()
    # don't overwrite. Add number before extension
    i = 1
    path, ext = filePath.rsplit(".", 1)
    while os.path.exists(filePath):
        filePath = "%s_%s.%s" % (path,i,ext)
        i +=1
    fileHandle = open(filePath, 'w')
    data = '';
    cnt = 0;
    fileSize = originalFile.getSize().getValue()
    while(cnt<fileSize):
        blockSize = min(maxBlockSize, fileSize)
        block = rawFileStore.read(cnt, blockSize)
        cnt = cnt+blockSize
        fileHandle.write(block)
    fileHandle.close()
    return filePath
    
    
def attachFileToParent(updateService, parent, originalFile, description=None, namespace=None):
    """
    Attaches the original file (file) to a Project, Dataset or Image (parent) 
    
    @param updateService:       The update service
    @param parent:              A ProjectI, DatasetI or ImageI to attach the file to
    @param originalFile:        The OriginalFileI to attach
    @param description:         Optional description for the file annotation. String
    @param namespace:           Optional namespace for file annotataion. String
    @return:                The saved and returned *AnnotationLinkI (* = Project, Dataset or Image)
    """
    fa = omero.model.FileAnnotationI();
    fa.setFile(originalFile);
    if description:
        fa.setDescription(omero.rtypes.rstring(description))
    if namespace:
        fa.setNs(omero.rtypes.rstring(namespace))
    if type(parent) == omero.model.DatasetI:
        l = omero.model.DatasetAnnotationLinkI()
    elif type(parent) == omero.model.ProjectI:
        l = omero.model.ProjectAnnotationLinkI()
    elif type(parent) == omero.model.ImageI:
        l = omero.model.ImageAnnotationLinkI()
    else:
        return
    parent = parent.__class__(parent.id.val, False)     # use unloaded object to avoid update conflicts
    l.setParent(parent);
    l.setChild(fa);
    return updateService.saveAndReturnObject(l);


def uploadAndAttachFile(queryService, updateService, rawFileStore, parent, localName, mimetype, description=None, namespace=None, origFilePathName=None):
    """
    Uploads a local file to the server, as an Original File and attaches it to the 
    parent (Project, Dataset or Image)
    
    @param queryService:    The query service
    @param updateService:   The update service
    @param rawFileStore:    The rawFileStore
    @param parent:          The ProjectI or DatasetI or ImageI to attach file to
    @param localName:       Full Name (and path) of the file location to upload. String
    @param mimetype:        The original file mimetype. E.g. "PNG". String
    @param description:     Optional description for the file annotation. String
    @param namespace:       Namespace to set for the original file
    @param origFilePathName:    The /path/to/file/fileName.ext you want on the server. If none, use output as name
    @return:                The originalFileLink child. (FileAnnotationI)
    """
    
    filename = localName
    if origFilePathName == None:
        origFilePathName = localName
    originalFile = createFile(updateService, filename, mimetype, origFilePathName);
    uploadFile(rawFileStore, originalFile, localName)
    fileLink = attachFileToParent(updateService, parent, originalFile, description, namespace)
    return fileLink.getChild()
    
    
def addAnnotationToImage(updateService, image, annotation):
    """
    Add the annotation to an image.
    @param updateService The update service to create the annotation link.
    @param image The ImageI object that should be annotated.
    @param annotation The annotation object
    @return The new annotationlink object
    """
    l = omero.model.ImageAnnotationLinkI();
    l.setParent(image);
    l.setChild(annotation);
    return updateService.saveAndReturnObject(l);
    
def readFromOriginalFile(rawFileService, iQuery, fileId, maxBlockSize = 10000):
    """
    Read the OriginalFile with fileId and return it as a string.
    @param rawFileService The RawFileService service to read the originalfile.
    @param iQuery The Query Service.
    @param fileId The id of the originalFile object.
    @param maxBlockSize The block size of each read.
    @return The OriginalFile object contents as a string
    """
    fileDetails = iQuery.findByQuery("from OriginalFile as o where o.id = " + str(fileId) , None);
    rawFileService.setFileId(fileId);
    data = '';
    cnt = 0;
    fileSize = fileDetails.getSize().getValue();
    while(cnt<fileSize):
        blockSize = min(maxBlockSize, fileSize);
        block = rawFileService.read(cnt, blockSize);
        data = data + block;
        cnt = cnt+blockSize;
    return data;

def readFileAsArray(rawFileService, iQuery, fileId, row, col, separator = ' '):
    """
    Read an OriginalFile with id and column separator and return it as an array.
    @param rawFileService The RawFileService service to read the originalfile.
    @param iQuery The Query Service.
    @param fileId The id of the originalFile object.
    @param row The number of rows in the file.
    @param col The number of columns in the file.
    @param sep the column separator.
    @return The file as an NumPy array.
    """
    textBlock = readFromOriginalFile(rawFileService, iQuery, fileId);
    arrayFromFile = numpy.fromstring(textBlock,sep = separator);
    return numpy.reshape(arrayFromFile, (row, col));

def readFlimImageFile(rawPixelsStore, pixels):
    """
    Read the RawImageFlimFile with fileId and return it as an array [c, x, y]
    @param rawPixelsStore The rawPixelStore service to get the image.
    @param pixels The pixels of the image.
    @return The Contents of the image for z = 0, t = 0, all channels;
    """
    sizeC = pixels.getSizeC().getValue();
    sizeX = pixels.getSizeX().getValue();
    sizeY = pixels.getSizeY().getValue();
    id = pixels.getId().getValue();
    pixelsType = pixels.getPixelsType().getValue().getValue();
    rawPixelsStore.setPixelsId(id , False);
    cRange = range(0, sizeC);
    stack = numpy.zeros((sizeC, sizeX, sizeY),dtype=pixelstypetopython.toNumpy(pixelsType));
    for c in cRange:
        plane = downloadPlane(rawPixelsStore, pixels, 0, c, 0);
        stack[c,:,:]=plane;
    return stack;
    
def downloadPlane(rawPixelsStore, pixels, z, c, t):
    """
    Download the plane [z,c,t] for image pixels.
    @param rawPixelsStore The rawPixelStore service to get the image.
    @param pixels The pixels of the image.
    @param z The Z-Section to retrieve.
    @param c The C-Section to retrieve.
    @param t The T-Section to retrieve.
    @return The Plane of the image for z, c, t
    """
    rawPlane = rawPixelsStore.getPlane(z, c, t);
    sizeX = pixels.getSizeX().getValue();
    sizeY = pixels.getSizeY().getValue();
    pixelsId = pixels.getId().getValue();
    pixelType = pixels.getPixelsType().getValue().getValue();
    convertType ='>'+str(sizeX*sizeY)+pixelstypetopython.toPython(pixelType);
    convertedPlane = unpack(convertType, rawPlane);
    numpyType = pixelstypetopython.toNumpy(pixelType)
    remappedPlane = numpy.array(convertedPlane, numpyType);
    remappedPlane.resize(sizeY, sizeX);
    return remappedPlane;

def createFileFromData(updateService, queryService, filename, data):
    """
    Create a file from the data of type format, setting sha1, ..
    @param updateService The updateService to create the annotation link.
    @param filename The name of the file.
    @param data The data to save.
    @param format The Format of the file.
    @return The newly created OriginalFile.
    """
    tempFile = omero.model.OriginalFileI();
    tempFile.setName(omero.rtypes.rstring(filename));
    tempFile.setPath(omero.rtypes.rstring(filename));
    tempFile.setMimetype(omero.rtypes.rstring(CSV_FORMAT));
    tempFile.setSize(omero.rtypes.rlong(len(data)));
    tempFile.setSha1(omero.rtypes.rstring(calcSha1FromData(data)));
    return updateService.saveAndReturnObject(tempFile);
    
def attachArrayToImage(updateService, image, file, nameSpace):
    """
    Attach an array, stored as a csv file to an image.
    @param updateService The updateService to create the annotation link.
    @param image The image to attach the data to.
    @param filename The name of the file.
    @param namespace The namespace of the file.
    """
    fa = omero.model.FileAnnotationI();
    fa.setFile(file);
    fa.setNs(omero.rtypes.rstring(nameSpace))
    l = omero.model.ImageAnnotationLinkI();
    l.setParent(image);
    l.setChild(fa);
    l = updateService.saveAndReturnObject(l);

def uploadArray(rawFileStore, updateService, queryService, image, filename, array):
    """
    Upload the data to the server, creating the OriginalFile Object and attaching it to the image.
    @param rawFileStore The rawFileStore used to create the file.
    @param updateService The updateService to create the annotation link.
    @param image The image to attach the data to.
    @param filename The name of the file.
    @param data The data to save.
    """
    data = arrayToCSV(array);
    file = createFileFromData(updateService, queryService, filename, data);
    rawFileStore.setFileId(file.getId().getValue());
    fileSize = len(data);
    increment = 10000;
    cnt = 0;
    done = 0
    while(done!=1):
        if(increment+cnt<fileSize):
            blockSize = increment;
        else:
            blockSize = fileSize-cnt;
            done = 1;
        block = data[cnt:cnt+blockSize];
        rawFileStore.write(block, cnt, blockSize);
        cnt = cnt+blockSize;
    attachArrayToImage(updateService, image, file, CSV_NS)    

def arrayToCSV(data):
    """
    Convert the numpy array data to a csv file.
    @param data the Numpy Array
    @return The CSV string.
    """
    size = data.shape;
    row = size[0];
    col = size[1];
    strdata ="";
    for r in range(0,row):
        for c in range(0, col):
            strdata = strdata + str(data[r,c])
            if(c<col-1):
                strdata = strdata+',';
        strdata = strdata + '\n';
    return strdata;    


def uploadPlane(rawPixelsStore, plane, z, c, t):
    """
    Upload the plane to the server attching it to the current z,c,t of the already instantiated rawPixelStore.
    @param rawPixelsStore The rawPixelStore which is already pointing to the data.
    @param plane The data to upload
    @param z The Z-Section of the plane.
    @param c The C-Section of the plane.
    @param t The T-Section of the plane.
    """
    byteSwappedPlane = plane.byteswap();
    convertedPlane = byteSwappedPlane.tostring();
    rawPixelsStore.setPlane(convertedPlane, z, c, t)


def uploadPlaneByRow(rawPixelsStore, plane, z, c, t):
    """
    Upload the plane to the server one row at a time,
    attching it to the current z,c,t of the already instantiated rawPixelStore.
    @param rawPixelsStore The rawPixelStore which is already pointing to the data.
    @param plane The data to upload
    @param z The Z-Section of the plane.
    @param c The C-Section of the plane.
    @param t The T-Section of the plane.
    """
    byteSwappedPlane = plane.byteswap()
    
    rowCount, colCount = plane.shape
    for y in range(rowCount):
        row = byteSwappedPlane[y:y+1, :]		# slice y axis into rows
        convertedRow = row.tostring()
        rawPixelsStore.setRow(convertedRow, y, z, c, t)


def getRenderingEngine(session, pixelsId):  
    """
    Create the renderingEngine for the pixelsId.
    @param session The current session to create the renderingEngine from.
    @return The renderingEngine Service for the pixels.
    """
    renderingEngine = session.createRenderingEngine();
    renderingEngine.lookupPixels(pixelsId);
    if(renderingEngine.lookupRenderingDef(pixelsId)==0):
        renderingEngine.resetDefaults();
    renderingEngine.lookupRenderingDef(pixelsId);
    renderingEngine.load();
    return renderingEngine;

    
def createPlaneDef(z,t):
    """
    Create the plane rendering def, for z,t
    @param Z the Z-Section
    @param T The T-Point.
    @return The RenderingDef Object.
    """
    planeDef = omero.romio.PlaneDef()
    planeDef.t = t;
    planeDef.z = z;
    planeDef.x = 0;
    planeDef.y = 0;
    planeDef.slice = 0;
    return planeDef;

def getPlaneAsPackedInt(renderingEngine, z, t):
    """
    Get the rendered Image of the plane for the z, t with the default channels.
    @param renderingEngine The already instantiated renderEngine.
    @param z The Z-section.
    @param t The Timepoint.
    """
    planeDef = createPlaneDef(z, t);
    return renderingEngine.renderAsPackedInt(planeDef);
    
def getRawPixelsStore(session, pixelsId):
    """
    Get the rawPixelsStore for the Image with pixelsId
    @param pixelsId The pixelsId of the object to retrieve.
    @return The rawPixelsStore service.
    """
    rawPixelsStore = session.createRawPixelsStore();
    rawPixelsStore.setPixelsId(pixelsId);
    return rawPixelsStore;

def getRawFileStore(session, fileId):
    """
    Get the rawFileStore for the file with fileId
    @param fileId The fileId of the object to retrieve.
    @return The rawFileStore service.
    """
    rawFileStore = session.createRawFileStore();
    rawFileStore.setFileId(fileId);
    return rawFileStore;

def getPlaneInfo(iQuery, pixelsId, asOrderedList = True):
    """
    Get the plane info for the pixels object returning it in order of z,t,c
    @param iQuery The query service.
    @param pixelsId The pixels for Id.
    @param asOrderedList 
    @return list of planeInfoTimes or map["z:t:c:]
    """
    query = "from PlaneInfo as Info where pixels.id='"+str(pixelsId)+"' orderby info.deltaT"
    infoList = queryService.findAllByQuery(query,None)

    if(asOrderedList):
        map = {}
        for info in infoList:
            key = "z:"+str(info.theZ.getValue())+"t:"+str(info.theT.getValue())+"c:"+str(info.theC.getValue());
            map[key] = info.deltaT.getValue();
        return map;  
    else:
        return infoList;    

def IdentityFn(commandArgs):
    return commandArgs;


def resetRenderingSettings(renderingEngine, pixelsId, cIndex, minValue, maxValue, rgba=None):
    """
    Simply resests the rendering settings for a pixel set, according to the min and max values
    The rendering engine does NOT have to be primed with pixelsId, as that is handled by this method. 
    
    @param renderingEngine        The OMERO rendering engine
    @param pixelsId        The Pixels ID
    @param minValue        Minimum value of rendering window
    @param maxValue        Maximum value of rendering window
    @param rgba            Option to set the colour of the channel. (r,g,b,a) tuple. 
    """
    
    renderingEngine.lookupPixels(pixelsId)
    if not renderingEngine.lookupRenderingDef(pixelsId):
        renderingEngine.resetDefaults()  
        if rgba == None:
            rgba=(255,255,255,255)  # probably don't want E.g. single channel image to be blue!   
    
    if not renderingEngine.lookupRenderingDef(pixelsId):
        raise "Still No Rendering Def"
    
    renderingEngine.load()
    renderingEngine.setChannelWindow(cIndex, float(minValue), float(maxValue))
    if rgba:
        red, green, blue, alpha = rgba
        renderingEngine.setRGBA(cIndex, red, green, blue, alpha)
    renderingEngine.saveCurrentSettings()


def createNewImage(pixelsService, rawPixelStore, renderingEngine, pixelsType, gateway, plane2Dlist, imageName, description, dataset=None):
    """
    Creates a new single-channel, single-timepoint image from the list of 2D numpy arrays in plane2Dlist 
    with each numpy 2D plane becoming a Z-section.
    
    @param pixelsService        The OMERO pixelsService
    @param rawPixelStore        The OMERO rawPixelsStore
    @param renderingEngine        The OMERO renderingEngine
    @param pixelsType            The pixelsType object     omero::model::PixelsType
    @param gateway                The OMERO gateway service
    @param plane2Dlist            A list of numpy 2D arrays, corresponding to Z-planes of new image. 
    @param imageName            Name of new image
    @param description            Description for the new image
    @param dataset                If specified, put the image in this dataset. omero.model.Dataset object
    
    @return The new OMERO image: omero.model.ImageI 
    """
    theC, theT = (0,0)
    
    # all planes in plane2Dlist should be same shape.
    shape = plane2Dlist[0].shape
    sizeY, sizeX = shape
    minValue = plane2Dlist[0].min()
    maxValue = plane2Dlist[0].max()
    
    # get some other dimensions and create the image.
    channelList = [theC]  # omero::sys::IntList
    sizeZ, sizeT = (len(plane2Dlist),1)
    iId = pixelsService.createImage(sizeX, sizeY, sizeZ, sizeT, channelList, pixelsType, imageName, description)
    imageId = iId.getValue()
    image = gateway.getImage(imageId)
    
    # upload plane data
    pixelsId = image.getPrimaryPixels().getId().getValue()
    rawPixelStore.setPixelsId(pixelsId, True)
    for theZ, plane2D in enumerate(plane2Dlist):
        minValue = min(minValue, plane2D.min())
        maxValue = max(maxValue, plane2D.max())
        if plane2D.size > 1000000:
            uploadPlaneByRow(rawPixelStore, plane2D, theZ, theC, theT)
        else:
            uploadPlane(rawPixelStore, plane2D, theZ, theC, theT)
    pixelsService.setChannelGlobalMinMax(pixelsId, theC, float(minValue), float(maxValue))
    resetRenderingSettings(renderingEngine, pixelsId, theC, minValue, maxValue)
    
    # put the image in dataset, if specified. 
    if dataset:
        link = omero.model.DatasetImageLinkI()
        link.parent = omero.model.DatasetI(dataset.id.val, False)
        link.child = omero.model.ImageI(image.id.val, False)
        gateway.saveAndReturnObject(link)
        
    return image


def parseInputs(client, session, processFn=IdentityFn):
    """
    parse the inputs from the client object and map it to some other form, values may be transformed by function.
    @param client The client object
    @param session The current session.
    @param processFn A function to transform data to some other form.
    @return Parsed inputs as defined by ProcessFn.
    """
    inputKeys = client.getInputKeys();
    commandArgs = {};
    for key in inputKeys:
        commandArgs[key]=client.getInput(key).getValue();
    return processFn(commandArgs);  


def getROIFromImage(iROIService, imageId, namespace=None):
    """
    Get the ROI from the server for the image with the namespace 
    @param iROIService The iROIService object
    @param imageId The imageId to retreive ROI from.
    @param namespace The namespace of the ROI.
    @return See above.
    """    
    roiOpts = ROIOptions(); 
    if(namespace!=None):
        roiOpts.namespace = namespace;
    return iROIService.findByImage(imageId, roiOpts);
  
def toCSV(list):
    """
    Convert a list to a Comma Separated Value string.
    @param list The list to convert.
    @return See above.
    """
    lenList = len(list);
    cnt = 0;
    str = "";
    for item in list:
        str = str + item;
        if(cnt < lenList-1):
              str = str + ",";
        cnt = cnt +1;
    return str;
  
def toList(csvString):
    """
    Convert a csv string to a list of strings
    @param csvString The CSV string to convert.
    @return See above.
    """
    list = csvString.split(',');
    for index in range(len(list)):
        list[index] = list[index].strip();
    return list;
  
def registerNamespace(iQuery, iUpdate, namespace, keywords):
    """
    Register a workflow with the server, if the workflow does not exist create it and returns it,
    otherwise it returns the already created workflow.
    @param iQuery The query service.
    @param iUpdate The update service.
    @param namespace The namespace of the workflow.
    @param keywords The keywords associated with the workflow.
    @return see above.
    """
    workflow = iQuery.findByQuery("from Namespace as n where n.name = '" + namespace+"'", None);
    workflowData = None;
    if(workflow!=None):
        return;
    workflowData = WorkflowData();
    workflowData.setNamespace(namespace);
    workflowData.setKeywords(keywords);
    workflow = iUpdate.saveAndReturnObject(workflowData.asIObject());
    return WorkflowData(workflow);

def findROIByImage(roiService, image, namespace):
    roiOptions = omero.api.RoiOptions();
    roiOptions.namespace = omero.rtypes.rstring(namespace);
    results = roiService.findByImage(image, roiOptions);
    roiList = [];
    for roi in results.rois:
        roiList.append(ROIData(roi));
    return roiList;