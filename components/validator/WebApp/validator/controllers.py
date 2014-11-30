#!/usr/bin/env python
# -*- coding: utf-8 -*-
# Copyright (C) 2007-2012 University of Dundee & Open Microscopy Environment.
# All Rights Reserved.
import turbogears
import cherrypy
from turbogears import controllers, expose
from time import gmtime, strftime
import os

from OmeValidator import XmlReport
# from model import *
# import logging
# log = logging.getLogger("validator.controllers")

# default upload dir to ./uploads
UPLOAD_DIR = cherrypy.config.get(
    "validator.uploads", os.path.join(os.getcwd(), "uploads"))
if not os.path.exists(UPLOAD_DIR):
    os.makedirs(UPLOAD_DIR)


class Root(controllers.Root):
    @expose(template="validator.templates.validator")
    def index(self):
        return dict()

    @expose("validator.templates.upload")
    def error(self, message):
        turbogears.flash("ERROR: %s " % message)
        raise turbogears.redirect("/")

    @expose()
    def upload(self, upload_file, **keywords):
        # check type of file (only XML and TIFF)
        sufix = upload_file.filename.split('.')
        size = len(sufix)
        if (sufix[size-1] != 'tif' and sufix[size-1] != 'tiff' and
                sufix[size-1] != 'xml' and sufix[size-1] != 'ome'):
            if upload_file.filename == '':
                msg = 'FileError: no file selected.'
            else:
                msg = ("FileError: File %s could not be uploaded because is"
                       " not OME-TIFF file." % upload_file.filename)
            raise turbogears.redirect("error", message=msg)

        time = strftime("%Y-%m-%d-%H-%M-%S_", gmtime())
        try:
            dirpath = os.getcwd()+'/'+UPLOAD_DIR+'/'+cherrypy.session.id + '/'
            if not os.path.exists(dirpath):
                os.makedirs(dirpath)
            # target file
            target_file_name = os.path.join(
                os.getcwd(), UPLOAD_DIR, cherrypy.session.id,
                time+upload_file.filename)
            # open file in binary mode for writing
            f = open(target_file_name, 'wb')
        except IOError:
            msg = "IOError: File %s could not be uploaded" \
                % upload_file.filename
            raise turbogears.redirect("error", message=msg)
        except:
            msg = "UnexpectedError: File could not be uploaded"
            raise turbogears.redirect("error", message=msg)
        else:
            try:
                # create buffer for reading
                bytes = upload_file.file.read(1024)
                while bytes:
                    f.write(bytes)
                    bytes = upload_file.file.read(1024)
            except IOError:
                msg = "IOError: File %s could not be written" \
                    % upload_file.filename
                raise turbogears.redirect("error", message=msg)
            except AttributeError:
                msg = "AttributeError: File %s could not be readed" \
                    % upload_file.filename
                raise turbogears.redirect("error", message=msg)
            except:
                msg = "UnexpectedError: File %s could not be read" \
                    % upload_file.filename
                raise turbogears.redirect("error", message=msg)
            if f:  # finally:
                f.close()

            # Session variable initialization (or recall if exists)
            cherrypy.session['filenames'] = cherrypy.session.get(
                'filenames', [])
            cherrypy.session['dirpath'] = cherrypy.session.get('dirpath', 0)

            # Variable assignment
            cherrypy.session.get(
                'filenames', []).append(time+upload_file.filename)
            cherrypy.session['dirpath'] = dirpath

            # Return the value to your template
            turbogears.flash("File uploaded successfully: %s."
                             % (time+upload_file.filename))
            raise turbogears.redirect("/fileslist")

    @expose(html="validator.templates.fileslist")
    def fileslist(self):
        filenames = cherrypy.session.get('filenames', [])
        if len(filenames) > 0:
            return dict(uploadlist=filenames)
        else:
            raise turbogears.redirect("/")

    @expose(html="validator.templates.result")
    def result(self, filename):
        # list from session
        filenames = cherrypy.session.get('filenames', [])
        if len(filenames) > 0:
            filepath = cherrypy.session.get('dirpath', 0) + filename
            if not os.path.isfile(filepath):
                msg = "UnexpectedError: File could valid"
                raise turbogears.redirect("error", message=msg)
            else:
                sufix = filename.split('.')
                size = len(sufix)

                if sufix[size-1] == 'tif' or sufix[size-1] == 'tiff':
                    result = XmlReport.validateTiff(filepath)
                else:
                    result = XmlReport.validateFile(filepath)

                # more
                xml_ns = ("http://www.openmicroscopy.org/XMLschemas/OME/FC/"
                          "ome.xsd")
                schema_ns = "http://www.openmicroscopy.org/Schemas/OME/%s"
                if result.theNamespace == xml_ns and result.isOmeTiff:
                    schema = xml_ns % 'ome.xsd (OME-TIFF variant)'
                elif result.theNamespace == xml_ns and not result.isOmeTiff:
                    schema = xml_ns % 'ome.xsd (Standard)'
                elif result.theNamespace == schema_ns % "2007-06":
                    schema = schema_ns % "2007-06 (Standard V2)"
                elif result.theNamespace == schema_ns % "2008-02":
                    schema = schema_ns % "2008-02 (Standard V2)"
                elif result.theNamespace == schema_ns % "2008-09":
                    schema = schema_ns % "2008-09 (Standard V1)"
                elif result.theNamespace == schema_ns % "2009-09":
                    schema = schema_ns % "2009-09 (Standard V1)"
                elif result.theNamespace == schema_ns % "2010-04":
                    schema = schema_ns % "2010-04 (Standard V1)"
                elif result.theNamespace == schema_ns % "2010-06":
                    schema = schema_ns % "2010-06 (Standard V1)"
                elif result.theNamespace == schema_ns % "2011-06":
                    schema = schema_ns % "2011-06 (Standard V1) - Pre-Release"
                else:
                    schema = ("No schema found - using " +
                              schema_ns % "2010-06 (Standard V1)")
                return dict(filepath=filename, result=result, schema=schema)
        else:
            raise turbogears.redirect("/")

    @expose()
    def default(self, name):
        return self.index()
