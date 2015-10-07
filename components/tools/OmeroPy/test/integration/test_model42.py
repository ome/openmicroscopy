#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Integration test focused on the model changes in 4.2

   Copyright 2010-2014 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import library as lib
import omero

from omero.rtypes import rstring


class TestModel42(lib.ITest):

    def testNs(self):
        ns = omero.model.NamespaceI()
        ns.name = rstring(self.uuid())
        ns.keywords = ["a", "b"]
        ns = self.root.sf.getUpdateService().saveAndReturnObject(ns)

    def testRoi(self):
        img = self.new_image("testRoi")
        roi = omero.model.RoiI()
        roi.namespaces = [self.uuid(), self.uuid()]
        roi.keywords = [["a", "b"], ["c", "d"]]
        roi.image = img
        roi = self.update.saveAndReturnObject(roi)


class TestTicket2290(lib.ITest):

    def testEmptyArrays2290(self):
        img = self.new_image("testRoi")
        roi = omero.model.RoiI()
        roi.image = img
        roi.keywords = []
        roi.namespaces = []
        roi = self.update.saveAndReturnObject(roi)
        # Then resave
        roi = self.update.saveAndReturnObject(roi)

    def testNoArrays2290(self):
        img = self.new_image("testRoi")
        roi = omero.model.RoiI()
        roi.image = img
        roi = self.update.saveAndReturnObject(roi)
        # Then resave
        roi = self.update.saveAndReturnObject(roi)

    def testRect2290(self):
        img = self.new_image("testRoi")
        roi = omero.model.RoiI()
        roi.namespaces = [self.uuid(), self.uuid()]
        roi.keywords = [["a", "b"], ["c", "d"]]
        roi.image = img
        rect = omero.model.RectangleI()
        roi.addShape(rect)
        roi = self.update.saveAndReturnObject(roi)

    def testRectB2290(self):
        img = self.new_image("testRoi")
        roi = omero.model.RoiI()
        roi.namespaces = [self.uuid(), self.uuid()]
        roi.keywords = [["a", "b"], ["c", "d"]]
        roi.image = img
        roi = self.update.saveAndReturnObject(roi)
        rect = omero.model.RectangleI()
        roi.addShape(rect)
        rect.roi = roi
        rect = self.update.saveAndReturnObject(rect)

    def testResave2290(self):
        img = self.new_image("testRoi")
        roi = omero.model.RoiI()
        roi.namespaces = [self.uuid(), self.uuid()]
        roi.keywords = [["a", "b"], ["c", "d"]]
        roi.image = img
        roi = self.update.saveAndReturnObject(roi)
        rect = omero.model.RectangleI()
        roi.addShape(rect)
        roi = self.update.saveAndReturnObject(roi)
