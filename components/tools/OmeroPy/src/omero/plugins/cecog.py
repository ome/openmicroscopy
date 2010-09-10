#!/usr/bin/env python
"""
   Methods for working with cecog

   Copyright 2010 University of Dundee, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import os
import re
import sys

from omero.cli import BaseControl, CLI, OMERODIR
from omero_ext.argparse import FileType

import omero
import omero.constants

from omero.rtypes import *


class CecogControl(BaseControl):
    """CeCog integration plugin.

Provides actions for prepairing data and otherwise
integrating with Cecog. See the Run_Cecog_4.1.py
script.
    """

    # [MetaMorph_PlateScanPackage]
    #regex_subdirectories = re.compile('(?=[^_]).*?(?P<D>\d+).*?')
    #regex_position = re.compile('P(?P<P>.+?)_')
    #continuous_frames = 1
    regex_token = re.compile(r'(?P<Token>.+)\.')
    regex_time = re.compile(r'T(?P<T>\d+)')
    regex_channel = re.compile(r'_C(?P<C>.+?)(_|$)')
    regex_zslice = re.compile(r'_Z(?P<Z>\d+)')

    def _configure(self, parser):
        sub = parser.sub()

        class Action(object):

            def __init__(this, name):
                func = getattr(self, name)
                this.parser = sub.add_parser(name, help=func.__doc__, description=func.__doc__)
                this.parser.set_defaults(func=func)

            def add_argument(this, *args, **kwargs):
                this.parser.add_argument(*args, **kwargs)
                return this

        merge = Action("merge")
        merge.add_argument("path", help="Path to image files")

        rois = Action("rois")
        rois.add_argument("-f", "--file", required=True, help="Details file to be parsed")
        rois.add_argument("-i", "--image", required=True, help="Image id which should have ids attached")

    ##
    ## Public methods
    ##
    def merge(self, args):
        """Uses PIL to read multiple planes from a local folder.

Planes are combined and uploaded to OMERO as new images with additional T, C, Z dimensions.

It should be run as a local script (not via scripting service) in order that it has
access to the local users file system. Therefore need EMAN2 or PIL installed locally.

Example usage:
will$ bin/omero cecog merge /Applications/CecogPackage/Data/Demo_data/0037/

Since this dir does not contain folders, this will upload images in '0037' into a Dataset called Demo_data
in a Project called 'Data'.

will$ bin/omero cecog merge /Applications/CecogPackage/Data/Demo_data/

Since this dir does contain folders, this will look for images in all subdirectories of 'Demo_data' and
upload images into a Dataset called Demo_data in a Project called 'Data'.

Images will be combined in Z, C and T according to the MetaMorph_PlateScanPackage naming convention.
E.g. tubulin_P0037_T00005_Cgfp_Z1_S1.tiff is Point 37, Timepoint 5, Channel gfp, Z 1. S?
see /Applications/CecogPackage/CecogAnalyzer.app/Contents/Resources/resources/naming_schemes.conf
"""
        """
        Processes the command args, makes project and dataset then calls uploadDirAsImages() to process and
        upload the images to OMERO.
        """
        path = args.path
        client = self.ctx.conn(args)
        queryService = client.sf.getQueryService()
        updateService = client.sf.getUpdateService()
        pixelsService = client.sf.getPixelsService()

        # if we don't have any folders in the 'dir' E.g. CecogPackage/Data/Demo_data/0037/
        # then 'Demo_data' becomes a dataset
        subDirs = []
        for f in os.listdir(path):
            fullpath = path + f
            # process folders in root dir:
            if os.path.isdir(fullpath):
                subDirs.append(fullpath)

        # get the dataset name and project name from path
        if len(subDirs) == 0:
            p = path[:-1]   # will remove the last folder
            p = os.path.dirname(p)
        else:
            if os.path.basename(path) == "":
                p = path[:-1]   # remove slash

        datasetName = os.path.basename(p)   # e.g. Demo_data
        p = p[:-1]
        p = os.path.dirname(p)
        projectName = os.path.basename(p)   # e.g. Data
        self.ctx.out("Putting images in Project: %s  Dataset: %s" % (projectName, datasetName))

        # create dataset
        dataset = omero.model.DatasetI()
        dataset.name = rstring(datasetName)
        dataset = updateService.saveAndReturnObject(dataset)
        # create project
        project = omero.model.ProjectI()
        project.name = rstring(projectName)
        project = updateService.saveAndReturnObject(project)
        # put dataset in project
        link = omero.model.ProjectDatasetLinkI()
        link.parent = omero.model.ProjectI(project.id.val, False)
        link.child = omero.model.DatasetI(dataset.id.val, False)
        updateService.saveAndReturnObject(link)

        if len(subDirs) > 0:
            for subDir in subDirs:
                self.ctx.out("Processing images in ", subDir)
                self.uploadDirAsImages(client.sf, queryService, updateService, pixelsService, subDir, dataset)

        # if there are no sub-directories, just put all the images in the dir
        else:
            self.ctx.out("Processing images in ", path)
            self.uploadDirAsImages(client.sf, queryService, updateService, pixelsService, path, dataset)

    def rois(self, args):
        """Parses an object_details text file, as generated by CeCog Analyzer and saves the data as ROIs on an Image in OMERO.

Text file is of the form:

frame	objID	classLabel	className	centerX	centerY	mean	        sd
1	    10  	6       	lateana	    1119	41	    76.8253796095	54.9305640673


Example usage:
bin/omero cecog rois -f Data/Demo_output/analyzed/0037/statistics/P0037__object_details.txt -i 502
"""
        """
        Processes the command args, parses the object_details.txt file and creates ROIs on the image specified in OMERO
        """
        filePath = args.file
        imageId = args.image
        if not os.path.exists(filePath):
            self.ctx.die(654, "Could find the object_details file at %s" % filePath)

        client = self.ctx.conn(args)
        object_details = open(filePath, 'r')

        for line in object_details:
            parseObject(updateService, imageId, line)

        object_details.close()


    ##
    ## Internal methods
    ##
    def getPlaneFromImage(self, imagePath, rgbIndex=None):
        """
        Reads a local image (E.g. single plane tiff) and returns it as a numpy 2D array.

        @param imagePath   Path to image.
        """
        import numpy
        try:
             from PIL import Image # see ticket:2597
        except ImportError:
             import Image # see ticket:2597

        i = Image.open(imagePath)
        a = numpy.asarray(i)
        if rgbIndex == None:
            return a
        else:
            return a[:, :, rgbIndex]

    def uploadDirAsImages(self, sf, queryService, updateService, pixelsService, path, dataset = None):
        """
        Reads all the images in the directory specified by 'path' and uploads them to OMERO as a single
        multi-dimensional image, placed in the specified 'dataset'
        Uses regex to determine the Z, C, T position of each image by name,
        and therefore determines sizeZ, sizeC, sizeT of the new Image.

        @param path     the path to the directory containing images.
        @param dataset  the OMERO dataset, if we want to put images somewhere. omero.model.DatasetI
        """
        import omero.util.script_utils as scriptUtil

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
            tSearch = self.regex_time.search(f)
            cSearch = self.regex_channel.search(f)
            zSearch = self.regex_zslice.search(f)
            tokSearch = self.regex_token.search(f)

            if f.endswith(".jpg"):
                rgb = True

            if tSearch == None:
                theT = 0
            else:
                theT = int(tSearch.group('T'))

            if cSearch == None:
                cName = "0"
            else:
                cName = cSearch.group('C')

            if zSearch == None:
                theZ = 0
            else:
                theZ = int(zSearch.group('Z'))

            channelSet.add(cName)
            sizeZ = max(sizeZ, theZ)
            zStart = min(zStart, theZ)
            sizeT = max(sizeT, theT)
            tStart = min(tStart, theT)
            if tokSearch != None:
                tokens.append(tokSearch.group('Token'))
            imageMap[(theZ, cName, theT)] = fullpath

        colourMap = {}
        if not rgb:
            channels = list(channelSet)
            # see if we can guess what colour the channels should be, based on name.
            for i, c in enumerate(channels):
                if c == 'rfp':
                    colourMap[i] = (255, 0, 0, 255)
                if c == 'gfp':
                    colourMap[i] = (0, 255, 0, 255)
        else:
            channels = ("red", "green", "blue")
            colourMap[0] = (255, 0, 0, 255)
            colourMap[1] = (0, 255, 0, 255)
            colourMap[2] = (0, 0, 255, 255)

        sizeC = len(channels)

        # use the common stem as the image name
        imageName = os.path.commonprefix(tokens).strip('0T_')
        description = "Imported from images in %s" % path
        self.ctx.out("Creating image: ", imageName)

        # use the last image to get X, Y sizes and pixel type
        if rgb:
            plane = self.getPlaneFromImage(fullpath, 0)
        else:
            plane = self.getPlaneFromImage(fullpath)
        pType = plane.dtype.name
        # look up the PixelsType object from DB
        pixelsType = queryService.findByQuery(\
            "from PixelsType as p where p.value='%s'" % pType, None) # omero::model::PixelsType
        if pixelsType == None and pType.startswith("float"):    # e.g. float32
            pixelsType = queryService.findByQuery(\
                "from PixelsType as p where p.value='%s'" % "float", None) # omero::model::PixelsType
        if pixelsType == None:
            self.ctx.die(502, "Unknown pixels type for: " % pType)
        sizeY, sizeX = plane.shape

        self.ctx.out("sizeX: %s  sizeY: %s sizeZ: %s  sizeC: %s  sizeT: %s" % (sizeX, sizeY, sizeZ, sizeC, sizeT))

        # code below here is very similar to combineImages.py
        # create an image in OMERO and populate the planes with numpy 2D arrays
        channelList = range(sizeC)
        imageId = pixelsService.createImage(sizeX, sizeY, sizeZ, sizeT, channelList, pixelsType, imageName, description)
        params = omero.sys.ParametersI()
        params.addId(imageId)
        pixelsId = queryService.projection(\
            "select p.id from Image i join i.pixels p where i.id = :id",\
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
                            self.ctx.out("Getting rgb plane from:", imagePath)
                            if rgb:
                                plane2D = self.getPlaneFromImage(imagePath, theC)
                                self.ctx.out(plane2D.shape)
                            else:
                                print imagePath
                                plane2D = self.getPlaneFromImage(imagePath)
                        else:
                            self.ctx.out("Creating blank plane for .", theZ, channels[theC], theT)
                            plane2D = zeros((sizeY, sizeX))
                        self.ctx.out("Uploading plane: theZ: %s, theC: %s, theT: %s" % (theZ, theC, theT))
                        #scriptUtil.uploadPlaneByRow(rawPixelStore, plane2D, theZ, theC, theT)
                        scriptUtil.uploadPlane(rawPixelStore, plane2D, theZ, theC, theT)
                        minValue = min(minValue, plane2D.min())
                        maxValue = max(maxValue, plane2D.max())
                pixelsService.setChannelGlobalMinMax(pixelsId, theC, float(minValue), float(maxValue))
                rgba = None
                if theC in colourMap:
                    rgba = colourMap[theC]
                try:
                    renderingEngine = sf.createRenderingEngine()
                    scriptUtil.resetRenderingSettings(renderingEngine, pixelsId, theC, minValue, maxValue, rgba)
                finally:
                    renderingEngine.close()
        finally:
            rawPixelStore.close()

        # add channel names
        pixels = pixelsService.retrievePixDescription(pixelsId)
        i = 0
        for c in pixels.iterateChannels():        # c is an instance of omero.model.ChannelI
            lc = c.getLogicalChannel()            # returns omero.model.LogicalChannelI
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

    def addRoi(self, updateService, imageId, x, y, theT, theZ, roiText=None):
        """
        Adds a Rectangle (particle) to the current OMERO image, at point x, y.
        Uses the self.image (OMERO image) and self.updateService
        """

        # create an ROI, add the point and save
        roi = omero.model.RoiI()
        roi.setImage(omero.model.ImageI(imageId, False))
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

    def parseObject(self, updateService, imageId, line):

        theZ = 0
        theT = None
        x = None
        y = None
        try:
            frame, objID, classLabel, className, centerX, centerY, mean, sd = line.split("\t")
            theT = long(frame)-1
            x = float(centerX)
            y = float(centerY)
        except:
            # line wasn't a data object
            pass

        if theT and x and y:
            self.ctx.out("Adding point '%s' to frome: %s, x: %s, y: %s" % (className, theT, x, y))
            self.addRoi(updateService, imageId, x, y, theT, theZ, className)

try:
    register("cecog", CecogControl, CecogControl.__doc__)
except NameError:
    if __name__ == "__main__":
        cli = CLI()
        cli.register("cecog", CecogControl, CecogControl.__doc__)
        cli.invoke(sys.argv[1:])
