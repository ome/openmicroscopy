#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
# Copyright (C) 2006-2013 University of Dundee & Open Microscopy Environment.
# All rights reserved.
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License along
# with this program; if not, write to the Free Software Foundation, Inc.,
# 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.

"""
Utility methods for dealing with scripts.
"""

import logging
import os
import warnings

from struct import unpack

import omero.clients
from omero.rtypes import rdouble
from omero.rtypes import rint
from omero.rtypes import rstring
from omero.rtypes import unwrap
import omero.util.pixelstypetopython as pixelstypetopython

try:
    import hashlib
    hash_sha1 = hashlib.sha1
except:
    import sha
    hash_sha1 = sha.new

# r,g,b,a colours for use in scripts.
COLOURS = {
    'Red': (255, 0, 0, 255),
    'Green': (0, 255, 0, 255),
    'Blue': (0, 0, 255, 255),
    'Yellow': (255, 255, 0, 255),
    'White': (255, 255, 255, 255), }

EXTRA_COLOURS = {
    'Violet': (238, 133, 238, 255),
    'Indigo': (79, 6, 132, 255),
    'Black': (0, 0, 0, 255),
    'Orange': (254, 200, 6, 255),
    'Gray': (130, 130, 130, 255), }

CSV_NS = 'text/csv'
CSV_FORMAT = 'text/csv'
SU_LOG = logging.getLogger("omero.util.script_utils")


def drawTextOverlay(draw, x, y, text, colour='0xffffff'):
    """
    Draw test on image.
    @param draw The PIL Draw class.
    @param x The x-coord to draw.
    @param y The y-coord to draw.
    @param text The text to render.
    @param colour The colour as a PIL colour string to draw the text in.
    """
    draw.text((x, y), text, fill=colour)


def drawLineOverlay(draw, x0, y0, x1, y1, colour='0xffffff'):
    """
    Draw line on image.
    @param draw The PIL Draw class.
    @param x0 The x0-coord of line.
    @param y0 The y0-coord of line.
    @param x1 The x1-coord of line.
    @param y1 The y1-coord of line.
    @param colour The colour as a PIL colour fill in the line.
    """
    draw.line([(x0, y0), (x1, y1)], fill=colour)


def rgbToRGBInt(red, green, blue):
    """
    Convert an R,G,B value to an int.
    @param R the Red value.
    @param G the Green value.
    @param B the Blue value.
    @return See above.
    """
    RGBInt = (red << 16) + (green << 8) + blue
    return int(RGBInt)


def RGBToPIL(RGB):
    """
    Convert an RGB value to a PIL colour value.
    @param RGB the RGB value.
    @return See above.
    """
    hexval = hex(int(RGB))
    return '#' + (6 - len(hexval[2:])) * '0' + hexval[2:]


def rangeToStr(range):
    """
    Map a range to a string of numbers
    @param range See above.
    @return See above.
    """
    first = 1
    string = ""
    for value in range:
        if(first == 1):
            string = str(value)
            first = 0
        else:
            string = string + ',' + str(value)
    return string


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
    return hash


def calcSha1FromData(data):
    """
    Calculate the Sha1 Hash from a data array
    @param data The data array.
    @return The Hash
    """
    h = hash_sha1()
    h.update(data)
    hash = h.hexdigest()
    return hash


def getFormat(queryService, format):
    return queryService.findByQuery(
        "from Format as f where f.value='" + format + "'", None)


def createFile(updateService, filename, mimetype=None, origFilePathName=None):
    """
    Creates an original file, saves it to the server and returns the result

    @param queryService:    The query service  E.g. session.getQueryService()
    @param updateService:   The update service E.g. session.getUpdateService()
    @param filename:        The file path and name (or name if in same folder).
                            String
    @param mimetype:        The mimetype (string) or Format object representing
                            the file format
    @param origFilePathName:       Optional path/name for the original file
    @return:                The saved OriginalFileI, as returned from the
                            server
    """

    originalFile = omero.model.OriginalFileI()
    if(origFilePathName is None):
        origFilePathName = filename
    path, name = os.path.split(origFilePathName)
    originalFile.setName(omero.rtypes.rstring(name))
    originalFile.setPath(omero.rtypes.rstring(path))
    # just in case we are passed a FormatI object
    try:
        v = mimetype.getValue()
        mt = v.getValue()
    except:
        # handle the string we expect
        mt = mimetype
    if mt:
        originalFile.mimetype = omero.rtypes.rstring(mt)
    originalFile.setSize(omero.rtypes.rlong(os.path.getsize(filename)))
    originalFile.setHash(omero.rtypes.rstring(calcSha1(filename)))
    return updateService.saveAndReturnObject(originalFile)


def uploadFile(rawFileStore, originalFile, filePath=None):
    """
    Uploads an OriginalFile to the server

    @param rawFileStore:    The Omero rawFileStore
    @param originalFile:    The OriginalFileI
    @param filePath:    Where to find the file to upload.
                        If None, use originalFile.getName().getValue()
    """
    rawFileStore.setFileId(originalFile.getId().getValue())
    fileSize = originalFile.getSize().getValue()
    increment = 10000
    cnt = 0
    if filePath is None:
        filePath = originalFile.getName().getValue()
    fileHandle = open(filePath, 'rb')
    done = 0
    while(done != 1):
        if(increment + cnt < fileSize):
            blockSize = increment
        else:
            blockSize = fileSize - cnt
            done = 1
        fileHandle.seek(cnt)
        block = fileHandle.read(blockSize)
        rawFileStore.write(block, cnt, blockSize)
        cnt = cnt + blockSize
    fileHandle.close()


def downloadFile(rawFileStore, originalFile, filePath=None):
    """
    Downloads an OriginalFile from the server.

    @param rawFileStore:    The Omero rawFileStore
    @param originalFile:    The OriginalFileI
    @param filePath:    Where to download the file.
                        If None, use originalFile.getName().getValue()
    """
    fileId = originalFile.getId().getValue()
    rawFileStore.setFileId(fileId)
    fileSize = originalFile.getSize().getValue()
    maxBlockSize = 10000
    cnt = 0
    if filePath is None:
        filePath = originalFile.getName().getValue()
    # don't overwrite. Add number before extension
    i = 1
    path, ext = filePath.rsplit(".", 1)
    while os.path.exists(filePath):
        filePath = "%s_%s.%s" % (path, i, ext)
        i += 1
    fileHandle = open(filePath, 'w')
    cnt = 0
    fileSize = originalFile.getSize().getValue()
    while(cnt < fileSize):
        blockSize = min(maxBlockSize, fileSize)
        block = rawFileStore.read(cnt, blockSize)
        cnt = cnt + blockSize
        fileHandle.write(block)
    fileHandle.close()
    return filePath


def attachFileToParent(updateService, parent, originalFile,
                       description=None, namespace=None):
    """
    Attaches the original file (file) to a Project, Dataset or Image (parent)

    @param updateService:       The update service
    @param parent:              A ProjectI, DatasetI or ImageI to attach
                                the file to
    @param originalFile:        The OriginalFileI to attach
    @param description:         Optional description for the file annotation.
                                String
    @param namespace:           Optional namespace for file annotataion. String
    @return:                    The saved and returned *AnnotationLinkI
                                (* = Project, Dataset or Image)
    """
    fa = omero.model.FileAnnotationI()
    fa.setFile(originalFile)
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
    # use unloaded object to avoid update conflicts
    parent = parent.__class__(parent.id.val, False)
    l.setParent(parent)
    l.setChild(fa)
    return updateService.saveAndReturnObject(l)


def uploadAndAttachFile(queryService, updateService, rawFileStore, parent,
                        localName, mimetype, description=None,
                        namespace=None, origFilePathName=None):
    """
    Uploads a local file to the server, as an Original File and attaches it to
    the parent (Project, Dataset or Image)

    @param queryService:    The query service
    @param updateService:   The update service
    @param rawFileStore:    The rawFileStore
    @param parent:          The ProjectI or DatasetI or ImageI to attach
                            file to
    @param localName:       Full Name (and path) of the file location
                            to upload. String
    @param mimetype:        The original file mimetype. E.g. "PNG". String
    @param description:     Optional description for the file annotation.
                            String
    @param namespace:       Namespace to set for the original file
    @param origFilePathName:    The /path/to/file/fileName.ext you want on the
                                server. If none, use output as name
    @return:                The originalFileLink child. (FileAnnotationI)
    """

    filename = localName
    if origFilePathName is None:
        origFilePathName = localName
    originalFile = createFile(
        updateService, filename, mimetype, origFilePathName)
    uploadFile(rawFileStore, originalFile, localName)
    fileLink = attachFileToParent(
        updateService, parent, originalFile, description, namespace)
    return fileLink.getChild()


def createLinkFileAnnotation(conn, localPath, parent, output="Output",
                             parenttype="Image", mimetype=None,
                             desc=None, ns=None, origFilePathAndName=None):
    """
    Uploads a local file to the server, as an Original File and attaches it to
    the parent (Project, Dataset or Image)

    @param conn:            The :class:`omero.gateway.BlitzGateway` connection.
    @param parent:          The ProjectI or DatasetI or ImageI to attach
                            file to
    @param localPath:       Full Name (and path) of the file location
                            to upload. String
    @param mimetype:        The original file mimetype. E.g. "PNG". String
    @param description:     Optional description for the file annotation.
                            String
    @param namespace:       Namespace to set for the original file
    @param
    @param origFilePathName:    The /path/to/file/fileName.ext you want on the
                                server. If none, use output as name
    @return:                The originalFileLink child (FileAnnotationI)
                            and a log message
    """
    if os.path.exists(localPath):
        fileAnnotation = conn.createFileAnnfromLocalFile(
            localPath, origFilePathAndName=origFilePathAndName,
            mimetype=mimetype, ns=ns, desc=desc)
        message = "%s created" % output
        if parent is not None:
            if parent.canAnnotate():
                parentClass = parent.OMERO_CLASS
                message += " and attached to %s%s %s." % (
                    parentClass[0].lower(), parentClass[1:], parent.getName())
                parent.linkAnnotation(fileAnnotation)
            else:
                message += " but could not be attached."
    else:
        message = "%s not created." % output
        fileAnnotation = None
    return fileAnnotation, message


def getObjects(conn, params):
    """
    Get the objects specified by the script parameters.
    Assume the parameters contain the keys IDs and Data_Type

    @param conn:            The :class:`omero.gateway.BlitzGateway` connection.
    @param params:          The script parameters
    @return:                The valid objects and a log message
    """

    dataType = params["Data_Type"]
    ids = params["IDs"]
    objects = list(conn.getObjects(dataType, ids))

    message = ""
    if not objects:
        message += "No %s%s found. " % (dataType[0].lower(), dataType[1:])
    else:
        if not len(objects) == len(ids):
            message += "Found %s out of %s %s%s(s). " % (
                len(objects), len(ids), dataType[0].lower(), dataType[1:])

        # Sort the objects according to the order of IDs
        idMap = dict([(o.id, o) for o in objects])
        objects = [idMap[i] for i in ids if i in idMap]

    return objects, message


def addAnnotationToImage(updateService, image, annotation):
    """
    Add the annotation to an image.
    @param updateService The update service to create the annotation link.
    @param image The ImageI object that should be annotated.
    @param annotation The annotation object
    @return The new annotationlink object
    """
    l = omero.model.ImageAnnotationLinkI()
    l.setParent(image)
    l.setChild(annotation)
    return updateService.saveAndReturnObject(l)


def readFromOriginalFile(rawFileService, iQuery, fileId, maxBlockSize=10000):
    """
    Read the OriginalFile with fileId and return it as a string.
    @param rawFileService The RawFileService service to read the originalfile.
    @param iQuery The Query Service.
    @param fileId The id of the originalFile object.
    @param maxBlockSize The block size of each read.
    @return The OriginalFile object contents as a string
    """
    fileDetails = iQuery.findByQuery(
        "from OriginalFile as o where o.id = " + str(fileId), None)
    rawFileService.setFileId(fileId)
    data = ''
    cnt = 0
    fileSize = fileDetails.getSize().getValue()
    while(cnt < fileSize):
        blockSize = min(maxBlockSize, fileSize)
        block = rawFileService.read(cnt, blockSize)
        data = data + block
        cnt = cnt + blockSize
    return data[0:fileSize]


def readFileAsArray(rawFileService, iQuery, fileId, row, col, separator=' '):
    """
    Read an OriginalFile with id and column separator
    and return it as an array.
    @param rawFileService The RawFileService service to read the originalfile.
    @param iQuery The Query Service.
    @param fileId The id of the originalFile object.
    @param row The number of rows in the file.
    @param col The number of columns in the file.
    @param sep the column separator.
    @return The file as an NumPy array.
    """
    from numpy import fromstring, reshape
    textBlock = readFromOriginalFile(rawFileService, iQuery, fileId)
    arrayFromFile = fromstring(textBlock, sep=separator)
    return reshape(arrayFromFile, (row, col))


def readFlimImageFile(rawPixelsStore, pixels):
    """
    Read the RawImageFlimFile with fileId and return it as an array [c, x, y]
    @param rawPixelsStore The rawPixelStore service to get the image.
    @param pixels The pixels of the image.
    @return The Contents of the image for z = 0, t = 0, all channels;
    """
    from numpy import zeros
    sizeC = pixels.getSizeC().getValue()
    sizeX = pixels.getSizeX().getValue()
    sizeY = pixels.getSizeY().getValue()
    id = pixels.getId().getValue()
    pixelsType = pixels.getPixelsType().getValue().getValue()
    rawPixelsStore.setPixelsId(id, False)
    cRange = range(0, sizeC)
    stack = zeros(
        (sizeC, sizeX, sizeY), dtype=pixelstypetopython.toNumpy(pixelsType))
    for c in cRange:
        plane = downloadPlane(rawPixelsStore, pixels, 0, c, 0)
        stack[c, :, :] = plane
    return stack


def downloadPlane(rawPixelsStore, pixels, z, c, t):
    """
    Download the plane [z,c,t] for image pixels.
    Pixels must have pixelsType loaded.
    N.B. The rawPixelsStore must have already been initialised by setPixelsId()
    @param rawPixelsStore The rawPixelStore service to get the image.
    @param pixels The pixels of the image.
    @param z The Z-Section to retrieve.
    @param c The C-Section to retrieve.
    @param t The T-Section to retrieve.
    @return The Plane of the image for z, c, t
    """
    from numpy import array
    rawPlane = rawPixelsStore.getPlane(z, c, t)
    sizeX = pixels.getSizeX().getValue()
    sizeY = pixels.getSizeY().getValue()
    pixelType = pixels.getPixelsType().getValue().getValue()
    convertType = '>' + str(sizeX * sizeY) + \
        pixelstypetopython.toPython(pixelType)
    convertedPlane = unpack(convertType, rawPlane)
    numpyType = pixelstypetopython.toNumpy(pixelType)
    remappedPlane = array(convertedPlane, numpyType)
    remappedPlane.resize(sizeY, sizeX)
    return remappedPlane


def getPlaneFromImage(imagePath, rgbIndex=None):
    """
    Reads a local image (E.g. single plane tiff)
    and returns it as a numpy 2D array.

    @param imagePath   Path to image.
    """
    from numpy import asarray
    try:
        from PIL import Image  # see ticket:2597
    except ImportError:
        import Image  # see ticket:2597

    i = Image.open(imagePath)
    a = asarray(i)
    if rgbIndex is None:
        return a
    else:
        return a[:, :, rgbIndex]


def uploadDirAsImages(sf, queryService, updateService,
                      pixelsService, path, dataset=None):
    """
    Reads all the images in the directory specified by 'path' and
    uploads them to OMERO as a single
    multi-dimensional image, placed in the specified 'dataset'
    Uses regex to determine the Z, C, T position of each image by name,
    and therefore determines sizeZ, sizeC, sizeT of the new Image.

    @param path     the path to the directory containing images.
    @param dataset  the OMERO dataset, if we want to put images somewhere.
                    omero.model.DatasetI
    """

    import re
    from numpy import zeros

    regex_token = re.compile(r'(?P<Token>.+)\.')
    regex_time = re.compile(r'T(?P<T>\d+)')
    regex_channel = re.compile(r'_C(?P<C>.+?)(_|$)')
    regex_zslice = re.compile(r'_Z(?P<Z>\d+)')

    # assume 1 image in this folder for now.
    # Make a single map of all images. key is (z,c,t). Value is image path.
    imageMap = {}
    channelSet = set()
    tokens = []

    # other parameters we need to determine
    sizeZ = 1
    sizeC = 1
    sizeT = 1
    zStart = 1      # could be 0 or 1 ?
    tStart = 1

    fullpath = None

    rgb = False
    # process the names and populate our imagemap
    for f in os.listdir(path):
        fullpath = os.path.join(path, f)
        tSearch = regex_time.search(f)
        cSearch = regex_channel.search(f)
        zSearch = regex_zslice.search(f)
        tokSearch = regex_token.search(f)

        if f.endswith(".jpg"):
            rgb = True

        if tSearch is None:
            theT = 0
        else:
            theT = int(tSearch.group('T'))

        if cSearch is None:
            cName = "0"
        else:
            cName = cSearch.group('C')

        if zSearch is None:
            theZ = 0
        else:
            theZ = int(zSearch.group('Z'))

        channelSet.add(cName)
        sizeZ = max(sizeZ, theZ)
        zStart = min(zStart, theZ)
        sizeT = max(sizeT, theT)
        tStart = min(tStart, theT)
        if tokSearch is not None:
            tokens.append(tokSearch.group('Token'))
        imageMap[(theZ, cName, theT)] = fullpath

    colourMap = {}
    if not rgb:
        channels = list(channelSet)
        # see if we can guess what colour the channels should be, based on
        # name.
        for i, c in enumerate(channels):
            if c == 'rfp':
                colourMap[i] = COLOURS["Red"]
            if c == 'gfp':
                colourMap[i] = COLOURS["Green"]
    else:
        channels = ("red", "green", "blue")
        colourMap[0] = COLOURS["Red"]
        colourMap[1] = COLOURS["Green"]
        colourMap[2] = COLOURS["Blue"]

    sizeC = len(channels)

    # use the common stem as the image name
    imageName = os.path.commonprefix(tokens).strip('0T_')
    description = "Imported from images in %s" % path
    SU_LOG.info("Creating image: %s" % imageName)

    # use the last image to get X, Y sizes and pixel type
    if rgb:
        plane = getPlaneFromImage(fullpath, 0)
    else:
        plane = getPlaneFromImage(fullpath)
    pType = plane.dtype.name
    # look up the PixelsType object from DB
    # omero::model::PixelsType
    pixelsType = queryService.findByQuery(
        "from PixelsType as p where p.value='%s'" % pType, None)
    if pixelsType is None and pType.startswith("float"):    # e.g. float32
        # omero::model::PixelsType
        pixelsType = queryService.findByQuery(
            "from PixelsType as p where p.value='%s'" % "float", None)
    if pixelsType is None:
        SU_LOG.warn("Unknown pixels type for: %s" % pType)
        return
    sizeY, sizeX = plane.shape

    SU_LOG.debug("sizeX: %s  sizeY: %s sizeZ: %s  sizeC: %s  sizeT: %s"
                 % (sizeX, sizeY, sizeZ, sizeC, sizeT))

    # code below here is very similar to combineImages.py
    # create an image in OMERO and populate the planes with numpy 2D arrays
    channelList = range(sizeC)
    imageId = pixelsService.createImage(
        sizeX, sizeY, sizeZ, sizeT, channelList,
        pixelsType, imageName, description)
    params = omero.sys.ParametersI()
    params.addId(imageId)
    pixelsId = queryService.projection(
        "select p.id from Image i join i.pixels p where i.id = :id",
        params)[0][0].val

    rawPixelStore = sf.createRawPixelsStore()
    rawPixelStore.setPixelsId(pixelsId, True)
    try:
        for theC in range(sizeC):
            minValue = 0
            maxValue = 0
            for theZ in range(sizeZ):
                zIndex = theZ + zStart
                for theT in range(sizeT):
                    tIndex = theT + tStart
                    if rgb:
                        c = "0"
                    else:
                        c = channels[theC]
                    if (zIndex, c, tIndex) in imageMap:
                        imagePath = imageMap[(zIndex, c, tIndex)]
                        if rgb:
                            SU_LOG.debug(
                                "Getting rgb plane from: %s" % imagePath)
                            plane2D = getPlaneFromImage(imagePath, theC)
                        else:
                            SU_LOG.debug("Getting plane from: %s" % imagePath)
                            plane2D = getPlaneFromImage(imagePath)
                    else:
                        SU_LOG.debug(
                            "Creating blank plane for .",
                            theZ, channels[theC], theT)
                        plane2D = zeros((sizeY, sizeX))
                    SU_LOG.debug(
                        "Uploading plane: theZ: %s, theC: %s, theT: %s"
                        % (theZ, theC, theT))

                    uploadPlane(rawPixelStore, plane2D, theZ, theC, theT)
                    minValue = min(minValue, plane2D.min())
                    maxValue = max(maxValue, plane2D.max())
            pixelsService.setChannelGlobalMinMax(
                pixelsId, theC, float(minValue), float(maxValue))
            rgba = None
            if theC in colourMap:
                rgba = colourMap[theC]
            try:
                renderingEngine = sf.createRenderingEngine()
                resetRenderingSettings(
                    renderingEngine, pixelsId, theC, minValue, maxValue, rgba)
            finally:
                renderingEngine.close()
    finally:
        rawPixelStore.close()

    # add channel names
    pixels = pixelsService.retrievePixDescription(pixelsId)
    i = 0
    # c is an instance of omero.model.ChannelI
    for c in pixels.iterateChannels():
        # returns omero.model.LogicalChannelI
        lc = c.getLogicalChannel()
        lc.setName(rstring(channels[i]))
        updateService.saveObject(lc)
        i += 1

    # put the image in dataset, if specified.
    if dataset:
        link = omero.model.DatasetImageLinkI()
        link.parent = omero.model.DatasetI(dataset.id.val, False)
        link.child = omero.model.ImageI(imageId, False)
        updateService.saveAndReturnObject(link)

    return imageId


def uploadCecogObjectDetails(updateService, imageId, filePath):
    """
    Parses a single line of cecog output and saves as a roi.

    Adds a Rectangle (particle) to the current OMERO image, at point x, y.
    Uses the self.image (OMERO image) and self.updateService
    """

    objects = {}
    roi_ids = []

    import fileinput
    for line in fileinput.input([filePath]):

        theT = None
        x = None
        y = None

        parts = line.split("\t")
        names = ("frame", "objID", "primaryClassLabel", "primaryClassName",
                 "centerX", "centerY", "mean", "sd", "secondaryClassabel",
                 "secondaryClassName", "secondaryMean", "secondarySd")
        values = {}
        for idx, name in enumerate(names):
            if len(parts) >= idx:
                values[name] = parts[idx]

        frame = values["frame"]
        try:
            frame = long(frame)
        except ValueError:
            SU_LOG.debug("Non-roi line: %s " % line)
            continue

        theT = frame - 1
        objID = values["objID"]
        className = values["primaryClassName"]
        x = float(values["centerX"])
        y = float(values["centerY"])

        description = ""
        for name in names:
            description += ("%s=%s\n" % (name, values.get(name, "(missing)")))

        if theT and x and y:
            SU_LOG.debug(
                "Adding point '%s' to frame: %s, x: %s, y: %s"
                % (className, theT, x, y))
            try:
                shapes = objects[objID]
            except KeyError:
                shapes = []
                objects[objID] = shapes
            shapes.append((theT, className, x, y, values, description))

    for object, shapes in objects.items():

        # create an ROI, add the point and save
        roi = omero.model.RoiI()
        roi.setImage(omero.model.ImageI(imageId, False))
        roi.setDescription(omero.rtypes.rstring("objID: %s" % object))

        # create and save a point
        for shape in shapes:

            theT, className, x, y, values, description = shape

            point = omero.model.PointI()
            point.cx = rdouble(x)
            point.cy = rdouble(y)
            point.theT = rint(theT)
            point.theZ = rint(0)  # Workaround for shoola:ticket:1596
            if className:
                point.setTextValue(rstring(className))    # for display only

            # link the point to the ROI and save it
            roi.addShape(point)

        roi = updateService.saveAndReturnObject(point)
        roi_ids.append(roi.id.val)

    return roi_ids


def split_image(client, imageId, dir,
                unformattedImageName="tubulin_P037_T%05d_C%s_Z%d_S1.tif",
                dims=('T', 'C', 'Z')):
    """
    Splits the image into component planes,
    which are saved as local tiffs according to unformattedImageName.
    E.g. myLocalDir/tubulin_P037_T%05d_C%s_Z%d_S1.tif
    which will be formatted according to dims, E.g. ('T', 'C', 'Z')
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

    try:
        from PIL import Image   # see ticket:2597
    except:
        import Image        # see ticket:2597

    query_string = "select p from Pixels p join fetch p.image as i join fetch " \
                   "p.pixelsType where i.id='%s'" % imageId
    pixels = queryService.findByQuery(query_string, None)
    sizeZ = pixels.getSizeZ().getValue()
    sizeC = pixels.getSizeC().getValue()
    sizeT = pixels.getSizeT().getValue()
    rawPixelsStore.setPixelsId(pixels.getId().getValue(), True)

    channelMap = {}
    cIndex = 0
    pixels = pixelsService.retrievePixDescription(
        pixels.id.val)    # load channels
    for c in pixels.iterateChannels():
        lc = c.getLogicalChannel()
        channelMap[
            cIndex] = lc.getName() and lc.getName().getValue() or str(cIndex)
        cIndex += 1

    def formatName(unformatted, z, c, t):
        # need to turn dims E.g. ('T', 'C', 'Z') into tuple, E.g. (t, c, z)
        dimMap = {'T': t, 'C': channelMap[c], 'Z': z}
        dd = tuple([dimMap[d] for d in dims])
        return unformatted % dd

    # cecog does this, but other formats may want to start at 0
    zStart = 1
    tStart = 1

    # loop through dimensions, saving planes as tiffs.
    for z in range(sizeZ):
        for c in range(sizeC):
            for t in range(sizeT):
                imageName = formatName(
                    unformattedImageName, z + zStart, c, t + tStart)
                SU_LOG.debug(
                    "downloading plane z: %s c: %s t: %s  to  %s"
                    % (z, c, t, imageName))
                plane = downloadPlane(rawPixelsStore, pixels, z, c, t)
                i = Image.fromarray(plane)
                i.save(imageName)


def createFileFromData(updateService, queryService, filename, data):
    """
    Create a file from the data of type format, setting sha1, ..
    @param updateService The updateService to create the annotation link.
    @param filename The name of the file.
    @param data The data to save.
    @param format The Format of the file.
    @return The newly created OriginalFile.
    """
    tempFile = omero.model.OriginalFileI()
    tempFile.setName(omero.rtypes.rstring(filename))
    tempFile.setPath(omero.rtypes.rstring(filename))
    tempFile.setMimetype(omero.rtypes.rstring(CSV_FORMAT))
    tempFile.setSize(omero.rtypes.rlong(len(data)))
    tempFile.setHash(omero.rtypes.rstring(calcSha1FromData(data)))
    return updateService.saveAndReturnObject(tempFile)


def attachArrayToImage(updateService, image, file, nameSpace):
    """
    Attach an array, stored as a csv file to an image. Returns the annotation.
    @param updateService The updateService to create the annotation link.
    @param image The image to attach the data to.
    @param filename The name of the file.
    @param namespace The namespace of the file.
    @return
    """
    fa = omero.model.FileAnnotationI()
    fa.setFile(file)
    fa.setNs(omero.rtypes.rstring(nameSpace))
    l = omero.model.ImageAnnotationLinkI()
    l.setParent(image)
    l.setChild(fa)
    l = updateService.saveAndReturnObject(l)
    return l.getChild()


def uploadArray(rawFileStore, updateService, queryService, image,
                filename, namespace, array):
    """
    Upload the data to the server, creating the OriginalFile Object
    and attaching it to the image.
    @param rawFileStore The rawFileStore used to create the file.
    @param updateService The updateService to create the annotation link.
    @param image The image to attach the data to.
    @param filename The name of the file.
    @param namespace The name space associated to the annotation.
    @param data The data to save.
    @return The newly created file.
    """
    data = arrayToCSV(array)
    file = createFileFromData(updateService, queryService, filename, data)
    rawFileStore.setFileId(file.getId().getValue())
    fileSize = len(data)
    increment = 10000
    cnt = 0
    done = 0
    while(done != 1):
        if(increment + cnt < fileSize):
            blockSize = increment
        else:
            blockSize = fileSize - cnt
            done = 1
        block = data[cnt:cnt + blockSize]
        rawFileStore.write(block, cnt, blockSize)
        cnt = cnt + blockSize
    return attachArrayToImage(updateService, image, file, namespace)


def arrayToCSV(data):
    """
    Convert the numpy array data to a csv file.
    @param data the Numpy Array
    @return The CSV string.
    """
    size = data.shape
    row = size[0]
    col = size[1]
    strdata = ""
    for r in range(0, row):
        for c in range(0, col):
            strdata = strdata + str(data[r, c])
            if(c < col - 1):
                strdata = strdata + ','
        strdata = strdata + '\n'
    return strdata


def uploadPlane(rawPixelsStore, plane, z, c, t):
    """
    Upload the plane to the server attching it to the current z,c,t
    of the already instantiated rawPixelStore.
    @param rawPixelsStore The rawPixelStore which is already pointing
                        to the data.
    @param plane The data to upload
    @param z The Z-Section of the plane.
    @param c The C-Section of the plane.
    @param t The T-Section of the plane.
    """
    byteSwappedPlane = plane.byteswap()
    convertedPlane = byteSwappedPlane.tostring()
    rawPixelsStore.setPlane(convertedPlane, z, c, t)


def uploadPlaneByRow(rawPixelsStore, plane, z, c, t):
    """
    Upload the plane to the server one row at a time,
    attching it to the current z,c,t of the already instantiated rawPixelStore.
    @param rawPixelsStore The rawPixelStore which is already pointing
                          to the data.
    @param plane The data to upload
    @param z The Z-Section of the plane.
    @param c The C-Section of the plane.
    @param t The T-Section of the plane.
    """
    byteSwappedPlane = plane.byteswap()

    rowCount, colCount = plane.shape
    for y in range(rowCount):
        row = byteSwappedPlane[y:y+1, :]        # slice y axis into rows
        convertedRow = row.tostring()
        rawPixelsStore.setRow(convertedRow, y, z, c, t)


def getRenderingEngine(session, pixelsId):
    """
    Create the renderingEngine for the pixelsId.
    @param session The current session to create the renderingEngine from.
    @return The renderingEngine Service for the pixels.
    """
    renderingEngine = session.createRenderingEngine()
    renderingEngine.lookupPixels(pixelsId)
    if(renderingEngine.lookupRenderingDef(pixelsId) == 0):
        renderingEngine.resetDefaultSettings(True)
    renderingEngine.lookupRenderingDef(pixelsId)
    renderingEngine.load()
    return renderingEngine


def createPlaneDef(z, t):
    """
    Create the plane rendering def, for z,t
    @param Z the Z-Section
    @param T The T-Point.
    @return The RenderingDef Object.
    """
    planeDef = omero.romio.PlaneDef()
    planeDef.t = t
    planeDef.z = z
    planeDef.x = 0
    planeDef.y = 0
    planeDef.slice = 0
    return planeDef


def getPlaneAsPackedInt(renderingEngine, z, t):
    """
    Get the rendered Image of the plane for the z, t with the default channels.
    @param renderingEngine The already instantiated renderEngine.
    @param z The Z-section.
    @param t The Timepoint.
    """
    planeDef = createPlaneDef(z, t)
    return renderingEngine.renderAsPackedInt(planeDef)


def getRawPixelsStore(session, pixelsId):
    """
    Get the rawPixelsStore for the Image with pixelsId
    @param pixelsId The pixelsId of the object to retrieve.
    @return The rawPixelsStore service.
    """
    rawPixelsStore = session.createRawPixelsStore()
    rawPixelsStore.setPixelsId(pixelsId)
    return rawPixelsStore


def getRawFileStore(session, fileId):
    """
    Get the rawFileStore for the file with fileId
    @param fileId The fileId of the object to retrieve.
    @return The rawFileStore service.
    """
    rawFileStore = session.createRawFileStore()
    rawFileStore.setFileId(fileId)
    return rawFileStore


def getPlaneInfo(iQuery, pixelsId, asOrderedList=True):
    """
    Get the plane info for the pixels object returning it in order of z,t,c
    @param iQuery The query service.
    @param pixelsId The pixels for Id.
    @param asOrderedList
    @return list of planeInfoTimes or map[z:t:c:]
    """
    query = "from PlaneInfo as Info where pixels.id='" + \
        str(pixelsId) + "' orderby info.deltaT"
    infoList = iQuery.findAllByQuery(query, None)

    if(asOrderedList):
        map = {}
        for info in infoList:
            key = "z:" + str(info.theZ.getValue()) + "t:" + \
                str(info.theT.getValue()) + "c:" + str(info.theC.getValue())
            map[key] = info.deltaT.getValue()
        return map
    else:
        return infoList


def IdentityFn(commandArgs):
    return commandArgs


def resetRenderingSettings(renderingEngine, pixelsId, cIndex,
                           minValue, maxValue, rgba=None):
    """
    Simply resests the rendering settings for a pixel set,
    according to the min and max values
    The rendering engine does NOT have to be primed with pixelsId,
    as that is handled by this method.

    @param renderingEngine        The OMERO rendering engine
    @param pixelsId        The Pixels ID
    @param minValue        Minimum value of rendering window
    @param maxValue        Maximum value of rendering window
    @param rgba            Option to set the colour of the channel.
                           (r,g,b,a) tuple.
    """

    renderingEngine.lookupPixels(pixelsId)
    if not renderingEngine.lookupRenderingDef(pixelsId):
        renderingEngine.resetDefaultSettings(True)
        if rgba is None:
            # probably don't want E.g. single channel image to be blue!
            rgba = COLOURS["White"]

    if not renderingEngine.lookupRenderingDef(pixelsId):
        raise Exception("Still No Rendering Def")

    renderingEngine.load()
    renderingEngine.setChannelWindow(cIndex, float(minValue), float(maxValue))
    if rgba:
        red, green, blue, alpha = rgba
        renderingEngine.setRGBA(cIndex, red, green, blue, alpha)
    renderingEngine.saveCurrentSettings()


def createNewImage(session, plane2Dlist, imageName, description, dataset=None):
    """
    Creates a new single-channel, single-timepoint image from the list of 2D
    numpy arrays in plane2Dlist with each numpy 2D plane becoming a Z-section.

    @param session          An OMERO service factory or equivalent
                            with getQueryService() etc.
    @param plane2Dlist      A list of numpy 2D arrays,
                            corresponding to Z-planes of new image.
    @param imageName        Name of new image
    @param description      Description for the new image
    @param dataset          If specified, put the image in this dataset.
                            omero.model.Dataset object

    @return The new OMERO image: omero.model.ImageI
    """
    queryService = session.getQueryService()
    pixelsService = session.getPixelsService()
    rawPixelStore = session.createRawPixelsStore()
    renderingEngine = session.createRenderingEngine()
    containerService = session.getContainerService()

    pType = plane2Dlist[0].dtype.name
    # omero::model::PixelsType
    pixelsType = queryService.findByQuery(
        "from PixelsType as p where p.value='%s'" % pType, None)

    theC, theT = (0, 0)

    # all planes in plane2Dlist should be same shape.
    shape = plane2Dlist[0].shape
    sizeY, sizeX = shape
    minValue = plane2Dlist[0].min()
    maxValue = plane2Dlist[0].max()

    # get some other dimensions and create the image.
    channelList = [theC]  # omero::sys::IntList
    sizeZ, sizeT = (len(plane2Dlist), 1)
    iId = pixelsService.createImage(
        sizeX, sizeY, sizeZ, sizeT, channelList,
        pixelsType, imageName, description)
    imageId = iId.getValue()
    image = containerService.getImages("Image", [imageId], None)[0]

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
    pixelsService.setChannelGlobalMinMax(
        pixelsId, theC, float(minValue), float(maxValue))
    resetRenderingSettings(renderingEngine, pixelsId, theC, minValue, maxValue)

    # put the image in dataset, if specified.
    if dataset:
        link = omero.model.DatasetImageLinkI()
        link.parent = omero.model.DatasetI(dataset.id.val, False)
        link.child = omero.model.ImageI(image.id.val, False)
        session.getUpdateService().saveObject(link)

    renderingEngine.close()
    rawPixelStore.close()
    return image


def parseInputs(client, session=None, processFn=IdentityFn):
    """
    parse the inputs from the client object and map it to some other form,
    values may be transformed by function.
    @param client The client object
    @param session The current session (deprecated).
    @param processFn A function to transform data to some other form.
    @return Parsed inputs as defined by ProcessFn.
    """
    if session:
        warnings.warn(
            "argument `session' is no longer required and may be removed from "
            "future versions of OMERO", DeprecationWarning)
    return processFn(client.getInputs(unwrap=True))


def getROIFromImage(iROIService, imageId):
    """
    Get the ROI from the server for the image with the namespace
    @param iROIService The iROIService object
    @param imageId The imageId to retreive ROI from.
    @param namespace The namespace of the ROI.
    @return See above.
    """
    roiOpts = omero.api.RoiOptions()
    return iROIService.findByImage(imageId, roiOpts)


def toCSV(list):
    """
    Convert a list to a Comma Separated Value string.
    @param list The list to convert.
    @return See above.
    """
    lenList = len(list)
    cnt = 0
    str = ""
    for item in list:
        str = str + item
        if(cnt < lenList - 1):
            str = str + ","
        cnt = cnt + 1
    return str


def toList(csvString):
    """
    Convert a csv string to a list of strings
    @param csvString The CSV string to convert.
    @return See above.
    """
    list = csvString.split(',')
    for index in range(len(list)):
        list[index] = list[index].strip()
    return list


def registerNamespace(iQuery, iUpdate, namespace, keywords):
    """
    Register a workflow with the server,
    if the workflow does not exist create it and returns it,
    otherwise it returns the already created workflow.
    @param iQuery The query service.
    @param iUpdate The update service.
    @param namespace The namespace of the workflow.
    @param keywords The keywords associated with the workflow.
    @return see above.
    """
    from omero.util.OmeroPopo import WorkflowData as WorkflowData

    # Support rstring and str namespaces
    namespace = unwrap(namespace)
    keywords = unwrap(keywords)

    workflow = iQuery.findByQuery(
        "from Namespace as n where n.name = '" + namespace + "'", None)
    workflowData = WorkflowData()
    if(workflow is not None):
        workflowData = WorkflowData(workflow)
    else:
        workflowData.setNamespace(namespace)
    splitKeywords = keywords.split(',')

    SU_LOG.debug(workflowData.asIObject())
    for keyword in splitKeywords:
        workflowData.addKeyword(keyword)
    SU_LOG.debug(workflowData.asIObject())
    workflow = iUpdate.saveAndReturnObject(workflowData.asIObject())
    return WorkflowData(workflow)


def findROIByImage(roiService, image, namespace):
    """
    Finds the ROI with the given namespace linked to the image.
    Returns a collection of ROIs.
    @param roiService The ROI service.
    @param image The image the ROIs are linked to .
    @param namespace The namespace of the ROI.
    @return see above.
    """
    from omero.util.OmeroPopo import ROIData as ROIData
    roiOptions = omero.api.RoiOptions()
    roiOptions.namespace = omero.rtypes.rstring(namespace)
    results = roiService.findByImage(image, roiOptions)
    roiList = []
    for roi in results.rois:
        roiList.append(ROIData(roi))
    return roiList
