#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
OmeValidator.py

Created by Andrew Patterson on 2007-07-24.

#~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
#
# Copyright (C) 2002-2011 Open Microscopy Environment. All Rights Reserved.
#       Massachusetts Institute of Technology,
#       National Institutes of Health,
#       University of Dundee,
#       University of Wisconsin at Madison
#
#~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
"""

# Standard Imports
import logging
from xml.dom.minidom import getDOMImplementation
from StringIO import StringIO
from xml import sax
import os
from stat import ST_SIZE

"""
# Load schemas from configured directory
import cherrypy
# LocalDir for schemas
SCHEMA_DIR = cherrypy.config.get("validator.schema", \
os.path.join(os.getcwd(),"schema"))
def schemaFilePath(inFilename):
    return os.path.join(os.getcwd(), SCHEMA_DIR, inFilename)
"""


# Load schemas from current directory
def schemaFilePath(inFilename):
    return inFilename

# Try to load Image for Tiff Support
haveTiffSupport = True
try:
    import Image
except ImportError:
    # This exception means that we do not have tiff support loading
    haveTiffSupport = False

# Try to load lxml for XML Schema Validation Support
haveXsdSupport = True
try:
    from lxml import etree
except ImportError:
    # This exception means that we do not have lxml support loading
    haveXsdSupport = False

# Default logger configuration
logging.basicConfig()


# Class used to store each error or warning message
class ParseMessage(object):
    filename = None
    line = None
    column = None
    errortype = None
    context = None
    message = None

    def __init__(self, inFilename, inLine, inColumn, inErrorType, inContext,
                 inMessage):
        '''
        Populate the new message with the information
        '''
        self.filename = inFilename
        self.line = inLine
        self.column = inColumn
        self.errortype = inErrorType
        self.context = inContext
        self.message = inMessage

    def __str__(self):
        '''
        Convert the message to a string for printing
        '''
        return "File: %s Line: (%s, %s) Type: %s (%s) - %s\n" \
            % (self.filename, self.line, self.column, self.errortype,
               self.context, self.message)


# The valiadtion report - this stores the results and dose the analysis
class XmlReport(object):
    """
    True if the OME-XML document or fragment has been parsed by the report. If
    false no xml has been processed but the error and warning logs and other
    flags may still be used.
    """
    hasParsedXml = False

    """
    Whether or not OME-XML document or fragment contains a valid <xml> tag and
    a valid <OME> tag.
    """
    isOmeXml = None

    """
    Whether or not the OME-XML document or fragment passes XMLSchema
    validation.
    """
    isXsdValid = None

    """
    Whether or not the OME-XML document or fragment has unresolvable external
    identifiers.
    """
    hasUnresolvableIds = None

    """
    Whether or not the OME-XML document or fragment passes internal
    consistency checks that are implicit but are beyond the scope of XML
    schema validation.
    """
    isInternallyConsistent = None

    """
    Whether or not the OME-XML document or fragment is from an OME-TIFF.
    """
    isOmeTiff = None

    """
    Whether or not the binary data within the OME-TIFF is consistent with the
    <TiffData> block within the OME-XML document or fragment.
    """
    isOmeTiffConsistent = None

    """
    Whether or not the OME-XML document or fragment has <CustomAttribute>'s.
    """
    hasCustomAttributes = None

    """
    The namespace define in the OME element of the OME-XML document
    """
    theNamespace = None

    """
    The namespace define in the OME element of the OME-XML document
    """
    thePrefix = None

    """
    The file the schema for the namespace has been loaded from
    """
    theSchemaFile = None
    """
    Create the message lists for this instance of the report object
    """
    errorList = None
    warningList = None
    unresolvedList = None

    """
    Create variables used to check internal consistency
    """
    # count of TiffData elements founf in the XML
    omeTiffDataCount = None
    # count of image frames found in Tiff file
    tiffFileFrames = None

    # count of pixels
    omePixelsCount = None
    # count of planes
    omePlanesCount = None
    # count of Z * C * T
    ome5dPlaneCount = None
    # count of number of times a tiff data block has asked for "all available
    # frames" - value of more then 1 indicates an error
    theAllFrameCount = None

    def __init__(self):
        """
        Constructor - creates the error and warning logs
        """
        # construct the message lists
        self.errorList = list()
        self.warningList = list()
        self.unresolvedList = list()

        # build the blank dom
        self.theDom = None

    def __str__(self):
        '''
        Convert the report to a string
        '''
        out = str()
        errors = list()
        errors.append("Errors\n")
        errors.extend(self.errorList)
        errors.append("Warnings\n")
        errors.extend(self.warningList)
        errors.append("Unresolved\n")
        errors.extend(self.unresolvedList)
        for error in errors:
            out = out + str(error)

        out = out + "hasParsedXml : %s\n" % self.hasParsedXml

        if self.isOmeXml is not None:
            out = out + "isOmeXml : %s\n" % self.isOmeXml
        if self.isXsdValid is not None:
            out = out + "isXsdValid : %s\n" % self.isXsdValid
        if self.hasUnresolvableIds is not None:
            out = out + "hasUnresolvableIds : %s\n" % self.hasUnresolvableIds
        if self.isInternallyConsistent is not None:
            out = out + "isInternallyConsistent : %s\n" \
                % self.isInternallyConsistent
        if self.isOmeTiff is not None:
            out = out + "isOmeTiff : %s\n" % self.isOmeTiff
        if self.isOmeTiffConsistent is not None:
            out = out + "isOmeTiffConsistent : %s\n" \
                % self.isOmeTiffConsistent
        if self.hasCustomAttributes is not None:
            out = out + "hasCustomAttributes : %s\n" \
                % self.hasCustomAttributes
        if self.theNamespace is not None:
            out = out + "theNamespace : %s\n" % self.theNamespace
        return out

    def parse(self, inFile):
        """
        Parse - Main work function - Validates the XML, sets the flags and
        populates the error and warning logs
        """
        # mark xlm as having been parsed
        self.hasParsedXml = True

        # look at file for Ids, Refs, and namespaces
        self.scanForIdsAndNamespace(inFile)

        # check the xml is valid aginst it's schema
        self.validateAgainstSchema()

        if len(self.unresolvedList) != 0:
            self.hasUnresolvableIds = True

        if self.isXsdValid is True and len(self.errorList) == 0:
            self.isOmeXml = True

    def validateAgainstSchema(self):
        if not haveXsdSupport:
            self.errorList.append(ParseMessage(
                None, None, None, "XSD", None,
                " LXML support not available - no validation"))
            return

        # loading the OME schema to validate against
        schema = self.loadChoosenSchema()
        if schema is None:
            return

        # create an IO string for the xml string provided
        stringXml = StringIO(self.theDom.toxml())

        #
        # print self.theDom.toprettyxml()

        # building the document tree from the input xml
        try:
            document = etree.parse(stringXml)
        except etree.XMLSyntaxError:
            self.errorList.append(ParseMessage(
                None, None, None, "XmlSyntax", None,
                "Xml Syntax error while parsing XSD schema"))
            return

        # validating the documnet tree against the loaded schema
        # according to the docs this should not throw an exception - but it
        # does!
        try:
            schema.validate(document)
            self.isXsdValid = True
            for err in schema.error_log:
                self.isXsdValid = False
                self.errorList.append(ParseMessage(
                    None, err.line, None, "XSD", None, err.message))
            if self.isXsdValid:
                self.checkOldSchemas(document)
        except etree.XMLSchemaValidateError:
            self.isXsdValid = False
            self.errorList.append(ParseMessage(
                None, None, None, "XML", None,
                "Processing the XML data has generated an unspecified error"
                " in the XML sub-system. This is usually a result of an"
                " incorrect top level block. Please check the OME block is"
                " well-formed and that the schemaLocation is specified"
                " correctly. This may also be caused by a missing namespace"
                " prefix or incorrect xmlns attribute."))

    def checkOldSchemas(self, inDocument):
        for thePossibleSchema in [
                ["ome-2011-06-V1.xsd", "June 2011 V1"],
                ["ome-2010-06-V1.xsd", "June 2010 V1"],
                ["ome-2010-04-V1.xsd", "April 2010 V1"],
                ["ome-2009-09-V1.xsd", "September 2009 V1"],
                ["ome-2008-09-V1.xsd", "September 2008 V1"],
                ["ome-2008-02-V2.xsd", "February 2008 V2"],
                ["ome-2008-02-V1.xsd", "February 2008 V1"],
                ["ome-2007-06-V2.xsd", "September 2007 V2"],
                ["ome-2007-06-V1.xsd", "June 2007 V1"],
                ["ome-fc-tiff.xsd", "2003 - Tiff Variant"],
                ["ome-fc.xsd", "2003 - Standard version"]]:
            # skip current one
            if not thePossibleSchema[0] == self.theSchemaFile:
                # load each old schema
                try:
                    schema = etree.XMLSchema(etree.parse(
                        schemaFilePath(thePossibleSchema[0])))
                except:
                    # chosen schema failed to laod
                    self.errorList.append(ParseMessage(
                        None, None, None,
                        schemaFilePath(thePossibleSchema[0]), None,
                        "Validator Internal error: XSD schema file could not"
                        " be found [2]"))
                # try validation
                try:
                    schema.validate(inDocument)
                    err = schema.error_log.last_error
                    if not err:
                        # if valid then add info message "also valid under..."
                        self.warningList.append(ParseMessage(
                            None, None, None, "Info", None,
                            "File also valid under schema: " +
                            thePossibleSchema[1]))
                except etree.XMLSchemaValidateError:
                    err = False

    def loadChoosenSchema(self):
        # choose the schema source
        # assume the new schema
        self.theSchemaFile = "ome-2010-06-V1.xsd"
        # if old schema
        old_ns = "http://www.openmicroscopy.org/XMLschemas/OME/FC/ome.xsd"
        if self.theNamespace == old_ns:
            # check if used by tiff
            if self.isOmeTiff:
                # use special tiff version of old schema
                self.theSchemaFile = "ome-fc-tiff.xsd"
            else:
                # use normal version of old schema
                self.theSchemaFile = "ome-fc.xsd"
        else:
            schema_ns = "http://www.openmicroscopy.org/Schemas/OME/%s"
            if self.theNamespace == schema_ns % "2007-06":
                # use September 2007 schema
                self.theSchemaFile = "ome-2007-06-V2.xsd"
            else:
                if self.theNamespace == schema_ns % "2008-02":
                    # use February 2008 schema
                    self.theSchemaFile = "ome-2008-02-V2.xsd"
                else:
                    if self.theNamespace == schema_ns % "2008-09":
                        # use September 2008 schema
                        self.theSchemaFile = "ome-2008-09-V1.xsd"
                    else:
                        if self.theNamespace == schema_ns % "2009-09":
                            # use September 2009 schema
                            self.theSchemaFile = "ome-2009-09-V1.xsd"
                        else:
                            if self.theNamespace == schema_ns % "2010-04":
                                # use April 2010 schema
                                self.theSchemaFile = "ome-2010-04-V1.xsd"
                            else:
                                if self.theNamespace == schema_ns % "2010-06":
                                    # use June 2010 schema
                                    self.theSchemaFile = "ome-2010-06-V1.xsd"
                                else:
                                    if self.theNamespace == \
                                            schema_ns % "2011-06":
                                        # use June 2010 schema
                                        self.theSchemaFile = \
                                            "ome-2011-06-V1.xsd"

        # loading the OME schema to validate against
        try:
            schema = etree.XMLSchema(
                etree.parse(schemaFilePath(self.theSchemaFile)))
        except:
            # chosen schema failed to laod
            self.errorList.append(ParseMessage(
                None, None, None, "XSD", None,
                "Validator Internal error: XSD schema file could not be found"
                " [1]"))
            schema = None

        return schema

    def scanForIdsAndNamespace(self, inFile):
        '''
        Look through the Xml stream for the namespace and store all the ID
        tags
        This version looks at all the elements
        '''

        # locate the handler class to the parser
        handlerContent = ElementAggregator()
        handlerError = ParseErrorHandler()
        # parse the string - this applies the handler to each part of
        # the xml in turn
        try:
            sax.parse(inFile, handlerContent, handlerError)
            self.hasCustomAttributes = handlerContent.hasCustomAttributes
        except sax.SAXParseException:
            self.errorList.append(ParseMessage(
                None, None, None, "XmlError", None, "Parsing of XML failed"))
        self.errorList.extend(handlerContent.errorList)
        self.warningList.extend(handlerContent.warningList)
        self.errorList.extend(handlerError.errorList)
        self.warningList.extend(handlerError.warningList)

        # store the unresolved refrences
        for reference in handlerContent.references:
            if reference not in handlerContent.ids:
                self.unresolvedList.append(ParseMessage(
                    None, None, None, "UnresolvedID", None, reference))

        # store the internal counters
        self.omeTiffDataCount = handlerContent.omeTiffDataCount
        self.omePixelsCount = handlerContent.omePixelsCount
        self.omePlanesCount = handlerContent.omePlanesCount
        self.ome5dPlaneCount = handlerContent.ome5dPlaneCount
        self.omeTiffDataPlaneCount = handlerContent.omeTiffDataPlaneCount
        self.theAllFrameCount = handlerContent.theAllFrameCount

        # store the namespace
        self.theNamespace = handlerContent.theNamespace
        # store the dom
        self.theDom = handlerContent.dom

    def scanForFirstOmeNamespace(self, inXml):
        '''
        Look through the Xml stream for the namespace
        This version looks at as few elements as possible as it stops as soon
        as it has found the first ome element - If there is no ome element it
        will have read the entire file
        '''
        # locate the handler class to the parser
        handlerContent = NamespaceSearcher()
        handlerError = ParseErrorHandler()
        # parse the string - this applies the handler to each part of
        # the xml untill the OME node is found
        try:
            sax.parseString(inXml, handlerContent, handlerError)
        except sax.SAXParseException:
            self.errorList.append(ParseMessage(
                None, None, None, "XmlError", None, "Parsing of XML failed"))
        self.errorList.extend(handlerError.errorList)
        self.warningList.extend(handlerError.warningList)

        # store the namespace
        self.theNamespace = handlerContent.theNamespace

    def validateFile(klass, inFilename):
        """
        Opens an XML file and examines the header to see if it is using the
        OME Schema
        """
        # create the report
        theFileReport = klass()

        # Open the file
        try:
            theFile = open(inFilename, 'r')
            # check the file contains some data
            length = os.stat(inFilename)[ST_SIZE]
            if length == 0:
                theFileReport.errorList.append(ParseMessage(
                    inFilename, None, None, "IOFile", "",
                    "XML file was of zero length"))
        except IOError:
            theFileReport.errorList.append(ParseMessage(
                inFilename, None, None, "IOFile", "",
                "XML file could not be read"))
            return theFileReport

        # parse the file into the report and validate it
        theFileReport.parse(theFile)
        theFileReport.isOmeTiff = False
        # the report has now been populated
        return theFileReport
    validateFile = classmethod(validateFile)

    def validateTiff(klass, inFilename):
        """
        Opens a Tiff file and extracts the OME-XML part of the file
        """
        theTiffReport = klass()
        # check there is tiff file support
        if not haveTiffSupport:
            theTiffReport.isOmeTiff = False
            theTiffReport.errorList.append(ParseMessage(
                inFilename, None, None, "NoLibrary", "",
                "No Tiff library found - file could not be read"))
        else:
            # load the tiff image
            try:
                image = Image.open(inFilename)
            except IOError:
                theTiffReport.isOmeTiff = False
                theTiffReport.errorList.append(ParseMessage(
                    inFilename, None, None, "InvalidFile", "",
                    "Not recognised as Tiff format - file could not be read"))
            else:
                # check for the XML containing tag within the tiff
                if 270 not in image.tag.keys():
                    theTiffReport.isOmeTiff = False
                    theTiffReport.errorList.append(ParseMessage(
                        inFilename, None, None, "InvalidTiff", "",
                        "Tiff file did not containg an ImageDescription Tag"
                        " - no XML found"))
                else:
                    # read the xml from the tiff
                    theXml = image.tag[270]
                    theTiffReport.isOmeTiff = True
                    # create a file object to represent the xml string
                    theFileString = StringIO(theXml)
                    # parse the new string/file object into the report and
                    # validate it
                    theTiffReport.parse(theFileString)
                    theTiffReport.validateTiffImageData(image)
                    """
                    # print theXml
                    print "Tiff Frames    : %s" % theTiffReport.tiffFileFrames
                    print "Ome Frames     : %s" % \
theTiffReport.omeTiffDataCount
                    print "Ome Pixels     : %s" % theTiffReport.omePixelsCount
                    print "Ome Planes     : %s" % theTiffReport.omePlanesCount
                    print "Ome 5dPlane    : %s" % \
theTiffReport.ome5dPlaneCount
                    print "Ome TiffPlane  : %s" % \
theTiffReport.omeTiffDataPlaneCount
                    print "Ome AllFrame   : %s" % \
theTiffReport.theAllFrameCount
                    """

        return theTiffReport
    validateTiff = classmethod(validateTiff)

    def validateTiffImageData(self, inImage, ):
        """
        Examines the tiff image data to compare with
        """

        """ code to look at the list of tiff image dimensions
        theTiffWidth = None
        theTiffHeight = None
        try:
            #theTiffWidth = int(inImage.tag[256])
            print inImage.tag[256]
        except KeyError:
            pass
        except ValueError:
            pass
        try:
            theTiffHeight = int(inImage.tag[257])
        except KeyError:
            pass
        except ValueError:
            pass
        """

        # look for frames
        self.tiffFileFrames = 0
        try:
            while True:
                inImage.seek(self.tiffFileFrames)
                self.tiffFileFrames = self.tiffFileFrames + 1
        except EOFError:
            inImage.seek(0)
            pass

        self.isOmeTiffConsistent = True

        # compare with values from xml and tiff
        if self.tiffFileFrames > self.ome5dPlaneCount:
            self.warningList.append(ParseMessage(
                None, None, None, "TIFF",
                ("Frames %s needing %s"
                 % (self.tiffFileFrames, self.ome5dPlaneCount)),
                "Extra frames are present in this Tiff file"))

        if self.tiffFileFrames < self.ome5dPlaneCount:
            self.warningList.append(ParseMessage(
                None, None, None, "TIFF",
                ("Frames %s out of %s"
                 % (self.tiffFileFrames, self.ome5dPlaneCount)),
                "Not all possible frames are present in this Tiff file"))

        # compare with values from xml TiffData and tiff
        totalTiffDataFrames = (self.omeTiffDataPlaneCount +
                               (self.tiffFileFrames * self.theAllFrameCount))
        if self.tiffFileFrames > totalTiffDataFrames:
            self.warningList.append(ParseMessage(
                None, None, None, "TIFF",
                ("Frames %s referenced %s"
                 % (self.tiffFileFrames, totalTiffDataFrames)),
                "Unreferenced frames are present in this Tiff file"))

        if self.tiffFileFrames < totalTiffDataFrames:
            self.errorList.append(ParseMessage(
                None, None, None, "TIFF",
                ("Frames %s out of %s"
                 % (self.tiffFileFrames, totalTiffDataFrames)),
                "Not all required frames are present in this Tiff file"))
            self.isOmeTiffConsistent = False


# Used by sax parser to handle errors when processing Elements
class ParseErrorHandler(sax.ErrorHandler):
    def __init__(self):
        self.errorList = list()
        self.warningList = list()

    def error(self, exception):
        # Called when the parser encounters a recoverable error.
        # If this method does not raise an exception, parsing may continue,
        # but further document information should not be expected by the
        # application. Allowing the parser to continue may allow additional
        # errors to be discovered in the input document.
        self.errorList.append(ParseMessage(
            exception.getPublicId(), exception.getLineNumber(),
            exception.getColumnNumber(), "XmlError",
            exception.getPublicId(), exception.getMessage()))

    def fatalError(self, exception):
        # Called when the parser encounters an error it cannot recover from;
        # parsing is expected to terminate when this method returns.
        self.errorList.append(ParseMessage(
            exception.getPublicId(), exception.getLineNumber(),
            exception.getColumnNumber(), "XmlFatalError",
            exception.getPublicId(), exception.getMessage()))

    def warning(self, exception):
        # Called when the parser presents minor warning information to the
        # application.
        self.warningList.append(ParseMessage(
            exception.getPublicId(), exception.getLineNumber(),
            exception.getColumnNumber(), "XmlWarning",
            exception.getPublicId(), exception.getMessage()))


# Used to process all Elements by sax parser
class ElementAggregator(sax.ContentHandler):
    inBinData = False

    def __init__(self):
        self.errorList = list()
        self.warningList = list()

    def startDocument(self):
        '''
        Initialise this object at the start of the document
        '''
        self.ids = list()
        self.references = list()
        self.theNamespace = None
        self.inBinDataContent = False
        self.shortFormXml = ""
        # internal check counters
        self.skipCount = 0
        self.omeTiffDataCount = 0
        self.omePixelsCount = 0
        self.omePlanesCount = 0
        self.ome5dPlaneCount = 0
        self.omeTiffDataPlaneCount = 0
        self.theAllFrameCount = 0
        self.hasCustomAttributes = False

        # Setup the DOM chunk
        impl = getDOMImplementation()
        self.dom = impl.createDocument(None, None, None)
        self.stack = list()

    def startElement(self, name, attribs):
        '''
        Examine each element in turn and harvest any useful information
        '''
        # pull the namespace out of the OME element
        if name[-3:] == "OME":
            if name[-4:] == ":OME":
                # a prefex is being used
                self.thePrefix = name[:-4]
                try:
                    self.theNamespace = attribs.getValue(
                        "xmlns" + ':' + self.thePrefix)
                except KeyError:
                    self.theNamespace = ""
            else:
                try:
                    self.theNamespace = attribs.getValue("xmlns")
                except KeyError:
                    self.theNamespace = ""

        # save the ID in any elements encountered
        if name[-16:] == "CustomAttributes":
            self.hasCustomAttributes = True

        # save the ID in any elements encountered
        if name[-3:] == "Ref":
            try:
                # If a Ref element then save in the refrences
                self.references.append(attribs.getValue("ID"))
            except KeyError:
                pass
        else:
            if name[-6:] == "Leader":
                try:
                    # in Leader - ID is actually a Reference
                    self.references.append(attribs.getValue("ID"))
                except KeyError:
                    pass
            else:
                if name[-7:] == "Contact":
                    try:
                        # in Contact - ID is actually a Reference
                        self.references.append(attribs.getValue("ID"))
                    except KeyError:
                        pass
                else:
                    if name[-5:] == "Image":
                        try:
                            # in Image - ID is an ID
                            # and DefaultPixels is a Reference
                            self.references.append(
                                attribs.getValue("DefaultPixels"))
                        except KeyError:
                            pass
                        try:
                            # and AcquiredPixels is a Reference
                            self.references.append(
                                attribs.getValue("AcquiredPixels"))
                        except KeyError:
                            pass
                    if name[-16:] == "ChannelComponent":
                        try:
                            # in ChannelComponent - Pixels is a Reference
                            self.references.append(attribs.getValue("Pixels"))
                        except KeyError:
                            pass
                    if name[-14:] == "LogicalChannel":
                        try:
                            # in LogicalChannel ID is an ID
                            # and SecondaryEmissionFilter is a Reference
                            self.references.append(
                                attribs.getValue("SecondaryEmissionFilter"))
                        except KeyError:
                            pass
                        try:
                            # and SecondaryExcitationFilter is a Reference
                            self.references.append(
                                attribs.getValue("SecondaryExcitationFilter"))
                        except KeyError:
                            pass

                    try:
                        # If any other element then save in the ids
                        self.ids.append(attribs.getValue("ID"))
                    except KeyError:
                        pass

        if name[-7:] == "BinData":
            self.inBinData = True

        if name[-8:] == "TiffData":
            self.omeTiffDataCount = self.omeTiffDataCount + 1
            # record the number of planes used by the TiffData block
            if "NumPlanes" in attribs:
                # use the number of planes specified
                self.omeTiffDataPlaneCount = self.omeTiffDataPlaneCount + \
                    int(attribs.getValue("NumPlanes"))
                if self.theAllFrameCount > 0:
                    self.errorList.append(ParseMessage(
                        None, None, None, "OME", "",
                        "Inconsistent use of TiffData element [Type 1]"))

            else:
                if "IFD" in attribs:
                    # use one frame
                    self.omeTiffDataPlaneCount = self.omeTiffDataPlaneCount \
                        + 1
                    if self.theAllFrameCount > 0:
                        self.errorList.append(ParseMessage(
                            None, None, None, "OME", "",
                            "Inconsistent use of TiffData element [Type 2]"))
                else:
                    # use all the frames in the tiff
                    self.theAllFrameCount = self.theAllFrameCount + 1
                    if ((self.theAllFrameCount > 1) or
                            (self.omeTiffDataPlaneCount > 0)):
                        self.errorList.append(ParseMessage(
                            None, None, None, "OME", "",
                            "Inconsistent use of TiffData element [Type 3]"))

            """
            IFD: Gives the IFD(s) for which this element is applicable. \
Indexed from 0. Default is 0 (the first IFD).
            FirstZ: Gives the Z position of the image plane at the specified \
IFD. Indexed from 0. Default is 0 (the first Z position).
            FirstT: Gives the T position of the image plane at the specified \
IFD. Indexed from 0. Default is 0 (the first T position).
            FirstC: Gives the C position of the image plane at the specified \
IFD. Indexed from 0. Default is 0 (the first C position).
            NumPlanes: Gives the number of IFDs affected. Dimension order of \
IFDs is given by the enclosing Pixels element's DimensionOrder attribute.
                       Default is the number of IFDs in the TIFF file, \
unless an IFD is specified, in which case the default is 1.
            """

        if name[-6:] == "Pixels":
            self.omePixelsCount = self.omePixelsCount + 1
            try:
                # total up planes needed from Z, C and T
                theZ = int(attribs.getValue("SizeZ"))
                theC = int(attribs.getValue("SizeC"))
                theT = int(attribs.getValue("SizeT"))
                # print "Z: %s, C: %s, T: %s" % (theZ, theC, theT)
                self.ome5dPlaneCount = self.ome5dPlaneCount + \
                    (theZ * theC * theT)
            except KeyError:
                pass
            except ValueError:
                pass

        if name[-5:] == "Plane":
            self.omePlanesCount = self.omePlanesCount + 1

        if name[-12:] == "ManufactSpec":
            try:
                # look for the serial number
                theSerial = int(attribs.getValue("SerialNumber"))
            except KeyError:
                pass
            except ValueError:
                pass
            if theSerial is None or len(theSerial) == 0:
                self.warningList.append(ParseMessage(
                    None, None, None, "OME", "",
                    "Missing or empty SerialNumber in ManufactSpec"))

        self.domify(name, attribs)

    def endElement(self, name):
        newElement = self.stack.pop()
        length = len(self.stack)
        if length == 0:
            self.dom.appendChild(newElement)
        else:
            self.stack[-1].appendChild(newElement)
        self.clear()

    def domify(self, name, attribs):
        newElement = self.dom.createElement(name)
        for (attr, value) in attribs.items():
            newAttribute = self.dom.createAttribute(attr)
            newAttribute.value = value
            newElement.setAttributeNode(newAttribute)
        self.stack.append(newElement)

    def characters(self, content):
        if not self.inBinData:
            # Strip trailing and/or leading whitespace, "\n", "\r", etc.
            content = content.strip().strip('\n\r')
            if len(content) > 0 and len(self.stack) > 0:
                textNode = self.dom.createTextNode(content)
                self.stack[-1].appendChild(textNode)

    def clear(self):
        self.inBinData = False


# Used to process the Elements until a namespace is found by sax parser
class NamespaceSearcher(sax.ContentHandler):
    def startDocument(self):
        '''
        Initialise this object at the start of the document
        '''
        self.theNamespace = None

    def startElement(self, name, attribs):
        '''
        Examine each element in turn and check of the main OME element
        '''
        # pull the namespace out of the OME element
        if name[-3:] == "OME":
            if name[-4:] == ":OME":
                # a prefex is being used
                self.thePrefix = name[:-4]
                try:
                    # pull the namespace for prefix out of the OME element
                    self.theNamespace = attribs.getValue(
                        "xmlns" + ':' + self.thePrefix)
                except KeyError:
                    # assume default namespace
                    self.theNamespace = ""
            else:
                try:
                    # pull the namespace out of the OME element
                    self.theNamespace = attribs.getValue("xmlns")
                except KeyError:
                    self.theNamespace = ""
            # finally:
                # the OME node has been found (even if it does not have a
                # namespace) so stop parsing the file
                # TODO

# Test code below this line #


if __name__ == '__main__':
    for aFilename in [
            "samples/completesamplenopre.xml",
            "samples/completesample.xml",
            "samples/completesamplenoenc.xml",
            "samples/sdub.ome",
            "samples/sdub-fix.ome",
            "samples/sdub-fix-pre.ome",
            "samples/tiny.ome", "samples/broke.ome",
            "samples/tiny2008-02-V1.ome",
            "samples/tiny2008-09-V1.ome",
            "samples/tiny2009-09-V1.ome",
            "samples/fail2009-09-V1.ome"]:

        print "============ XML file %s ============ " % aFilename
        print XmlReport.validateFile(aFilename)

    for aFilename in [
            "samples/4d2wOME.tif", "samples/4d2wOME-fixed.tif",
            "samples/4d2wOME-fixed-updated.tif", "samples/blank.tif",
            "samples/ome.xsd", "samples/4d2wOME-EdExtra.tif"]:
        print "============ XML file %s ============ " % aFilename
        print XmlReport.validateTiff(aFilename)

    print "============"

###
