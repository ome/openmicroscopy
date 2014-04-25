#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2010-2014 Glencoe Software, Inc. All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
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
   Integration test focused on the model changes in 4.2

"""

import test.integration.library as lib
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
        rect = omero.model.RectI()
        roi.addShape(rect)
        roi = self.update.saveAndReturnObject(roi)

    def testRectB2290(self):
        img = self.new_image("testRoi")
        roi = omero.model.RoiI()
        roi.namespaces = [self.uuid(), self.uuid()]
        roi.keywords = [["a", "b"], ["c", "d"]]
        roi.image = img
        roi = self.update.saveAndReturnObject(roi)
        rect = omero.model.RectI()
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
        rect = omero.model.RectI()
        roi.addShape(rect)
        roi = self.update.saveAndReturnObject(roi)
