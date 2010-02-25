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
# @version 3.0
# <small>
# (<b>Internal version:</b> $Revision: $Date: $)
# </small>
# @since 3.0-Beta4
#
import getopt, sys, os, subprocess
import numpy;
from struct import *

import PIL
from PIL import Image
import ImageDraw

import omero
import omero_Constants_ice
from omero.rtypes import *
import omero.util.pixelstypetopython as pixelstypetopython

try: 
    import hashlib 
    hash_sha1 = hashlib.sha1 
except: 
    import sha 
    hash_sha1 = sha.new 


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


def createFile(updateService, filename, format, ofilename=None):
    """
    Creates an original file, saves it to the server and returns the result
    
    @param queryService:    The query service  E.g. session.getQueryService()
    @param updateService:   The update service E.g. session.getUpdateService()
    @param filename:        The file path and name (or name if in same folder). String
    @param format:          The Format object representing the file format
    @param ofileName:       Optional name for the original file
    @return:                The saved OriginalFileI, as returned from the server
    """
    
    originalFile = omero.model.OriginalFileI();
    if(ofilename == None):
        ofilename = filename;
    originalFile.setName(omero.rtypes.rstring(ofilename));
    originalFile.setPath(omero.rtypes.rstring(ofilename));
    originalFile.setFormat(format);
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
    l.setParent(parent);
    l.setChild(fa);
    return updateService.saveAndReturnObject(l);


def uploadAndAttachFile(queryService, updateService, rawFileStore, parent, output, format, description=None):
    """
    Uploads a local file to the server, as an Original File and attaches it to the 
    parent (Project, Dataset or Image)
    
    @param queryService:    The query service
    @param updateService:   The update service
    @param rawFileStore:    The rawFileStore
    @param parent:          The ProjectI or DatasetI or ImageI to attach file to
    @param output:          Full Name (and path) of the file to upload. String
    @param format:          The format. E.g. "image/png". String
    @param description:     Optional description for the file annotation. String
    @return:            The id of the originalFileLink child. (ID object, not value)
    """
    
    filename = output
    originalFilename = output
    fileformat = getFormat(queryService, format)
    originalFile = createFile(updateService, filename, fileformat, originalFilename);
    uploadFile(rawFileStore, originalFile, originalFilename)
    fileLink = attachFileToParent(updateService, parent, originalFile, description)
    return fileLink.getChild().getId()
    
    
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
    remappedPlane.resize(sizeX, sizeY);
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
    tempFile.setFormat(getFormat(queryService, CSV_FORMAT));
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