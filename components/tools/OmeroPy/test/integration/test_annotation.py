#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2013-2014 University of Dundee & Open Microscopy Environment.
# All Rights Reserved. Use is subject to license terms supplied in LICENSE.txt
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
   Integration test for adding annotations to Project.
"""

import library as lib
import omero
import omero.scripts
from omero.rtypes import rstring, rbool, rtime, rlong, rdouble
import random
from datetime import datetime


class TestFigureExportScripts(lib.ITest):

    def testAddAnnotations(self):

        # root session is root.sf
        session = self.root.sf
        updateService = session.getUpdateService()
        queryService = session.getQueryService()

        # create project
        parent = self.make_project(name="Annotations Test", client=self.root)

        xml = "<testXml><testElement><annotation>\
            Text</annotation></testElement></testXml>"
        doubleVal = random.random()
        timeVal = datetime.now().microsecond
        addTag(
            updateService, parent, "Test-Tag",
            ns="test/omero/tag/ns",
            description=None)
        addComment(
            updateService, parent, "Test-Comment",
            ns="test/omero/comment/ns",
            description=None)
        addXmlAnnotation(
            updateService, parent, xml,
            ns="test/omero/xml/ns",
            description="Test xml annotation description")
        addBooleanAnnotation(
            updateService, parent, True,
            ns="test/omero/boolean/ns",
            description="True if True, otherwise False")
        addDoubleAnnotation(
            updateService, parent, doubleVal,
            ns="test/omero/double/ns",
            description="Random number!")
        addLongAnnotation(
            updateService, parent, 123456,
            ns="test/omero/long/ns",
            description=None)
        addTermAnnotation(
            updateService, parent, "Metaphase",
            ns="test/omero/term/ns",
            description="Metaphase is part of mitosis")
        addTimestampAnnotation(
            updateService, parent, timeVal,
            ns="test/omero/timestamp/ns",
            description=None)

        annValues = {"test/omero/tag/ns": ["Test-Tag", "getTextValue"],
                     "test/omero/comment/ns": ["Test-Comment", "getTextValue"],
                     "test/omero/xml/ns": [xml, "getTextValue"],
                     "test/omero/boolean/ns": [True, "getBoolValue"],
                     "test/omero/double/ns": [doubleVal, "getDoubleValue"],
                     "test/omero/long/ns": [123456, "getLongValue"],
                     "test/omero/term/ns": ["Metaphase", "getTermValue"],
                     "test/omero/timestamp/ns": [timeVal, "getTimeValue"]}

        # retrieve the annotations and check values.
        p = omero.sys.Parameters()
        p.map = {}
        p.map["pid"] = parent.getId()
        query = "select l from ProjectAnnotationLink as l join\
            fetch l.child as a where l.parent.id=:pid and a.ns=:ns"
        for ns, values in annValues.items():
            p.map["ns"] = rstring(ns)
            # only 1 of each namespace
            link = queryService.findByQuery(query, p)
            valueMethod = getattr(link.child, values[1])
            assert values[0] == valueMethod().getValue(),\
                "Annotation %s value %s not equal to set value %s"\
                % (link.child.__class__, valueMethod().getValue(), values[0])


def saveAndLinkAnnotation(
        updateService, parent, annotation, ns=None, description=None):
    """
    Saves the Annotation and Links it to a Project, Dataset or Image

    """

    if ns:
        annotation.setNs(rstring(ns))
    if description:
        annotation.setDescription(rstring(description))
    annotation = updateService.saveAndReturnObject(annotation)
    if type(parent) == omero.model.DatasetI:
        l = omero.model.DatasetAnnotationLinkI()
    elif type(parent) == omero.model.ProjectI:
        l = omero.model.ProjectAnnotationLinkI()
    elif type(parent) == omero.model.ImageI:
        l = omero.model.ImageAnnotationLinkI()
    else:
        return
    parent = parent.__class__(parent.id.val, False)
    l.setParent(parent)
    l.setChild(annotation)
    return updateService.saveAndReturnObject(l)


# Text Annotations
def addTag(
        updateService, parent, text, ns=None, description=None):
    """ Adds a Tag. """
    child = omero.model.TagAnnotationI()
    child.setTextValue(rstring(text))
    saveAndLinkAnnotation(updateService, parent, child, ns, description)


def addComment(
        updateService, parent, text, ns=None, description=None):
    """ Adds a Comment. """
    child = omero.model.CommentAnnotationI()
    child.setTextValue(rstring(text))
    saveAndLinkAnnotation(updateService, parent, child, ns, description)


def addXmlAnnotation(
        updateService, parent, xmlText, ns=None, description=None):
    """ Adds XML Annotation. """
    child = omero.model.XmlAnnotationI()
    child.setTextValue(rstring(xmlText))
    saveAndLinkAnnotation(updateService, parent, child, ns, description)


# Basic Annotations
def addBooleanAnnotation(
        updateService, parent, boolean, ns=None, description=None):
    """ Adds a Boolean Annotation. """
    child = omero.model.BooleanAnnotationI()
    child.setBoolValue(rbool(boolean))
    saveAndLinkAnnotation(updateService, parent, child, ns, description)


def addDoubleAnnotation(
        updateService, parent, double, ns=None, description=None):
    """ Adds a Double Annotation. """
    child = omero.model.DoubleAnnotationI()
    child.setDoubleValue(rdouble(double))
    saveAndLinkAnnotation(updateService, parent, child, ns, description)


def addLongAnnotation(
        updateService, parent, longValue, ns=None, description=None):
    """ Adds a Long Annotation. """
    child = omero.model.LongAnnotationI()
    child.setLongValue(rlong(longValue))
    saveAndLinkAnnotation(updateService, parent, child, ns, description)


def addTermAnnotation(
        updateService, parent, term, ns=None, description=None):
    """ Adds a Term Annotation. """
    child = omero.model.TermAnnotationI()
    child.setTermValue(rstring(term))
    saveAndLinkAnnotation(updateService, parent, child, ns, description)


def addTimestampAnnotation(
        updateService, parent, timeValue, ns=None, description=None):
    """ Adds a Timestamp Annotation. """
    child = omero.model.TimestampAnnotationI()
    child.setTimeValue(rtime(timeValue))
    saveAndLinkAnnotation(updateService, parent, child, ns, description)
