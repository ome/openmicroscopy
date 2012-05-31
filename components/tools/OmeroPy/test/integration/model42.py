#!/usr/bin/env python

"""
   Integration test focused on the model changes in 4.2

   Copyright 2010 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import unittest
import integration.library as lib
from omero.rtypes import *


class TestModel42(lib.ITest):

    def testRoi(self):
        pix = self.pix()
        img = pix.getImage()
        img.name = rstring("testRoi")
        img.acquisitionDate = rtime(0)
        pix = self.update.saveAndReturnObject( pix )
        img = pix.getImage()
        roi = omero.model.ROII()
        roi.namespace = rstring(self.uuid())
        roi.linkImage(img)
        roi = self.update.saveAndReturnObject(roi)


class TestTicket2290(lib.ITest):

    def testEmptyArrays2290(self):
        pix = self.pix()
        img = pix.getImage()
        img.name = rstring("testRoi")
        img.acquisitionDate = rtime(0)
        pix = self.update.saveAndReturnObject( pix )
        img = pix.getImage()
        roi = omero.model.ROII()
        roi.linkImage(img)
        roi = self.update.saveAndReturnObject(roi)
        # Then resave
        roi = self.update.saveAndReturnObject(roi)

    def testNoArrays2290(self):
        pix = self.pix()
        img = pix.getImage()
        img.name = rstring("testRoi")
        img.acquisitionDate = rtime(0)
        pix = self.update.saveAndReturnObject( pix )
        img = pix.getImage()
        roi = omero.model.ROII()
        roi.linkImage(img)
        roi = self.update.saveAndReturnObject(roi)
        # Then resave
        roi = self.update.saveAndReturnObject(roi)

    def testRect2290(self):
        pix = self.pix()
        img = pix.getImage()
        img.name = rstring("testRoi")
        img.acquisitionDate = rtime(0)
        pix = self.update.saveAndReturnObject( pix )
        img = pix.getImage()
        roi = omero.model.ROII()
        roi.linkImage(img)
        rect = self.new_rectangle()
        roi.addShape(rect)
        roi = self.update.saveAndReturnObject(roi)

    def testRectB2290(self):
        pix = self.pix()
        img = pix.getImage()
        img.name = rstring("testRoi")
        img.acquisitionDate = rtime(0)
        pix = self.update.saveAndReturnObject( pix )
        img = pix.getImage()
        roi = omero.model.ROII()
        roi.linkImage(img)
        roi = self.update.saveAndReturnObject(roi)
        rect = self.new_rectangle()
        roi.addShape(rect)
        rect.roi = roi
        rect = self.update.saveAndReturnObject(rect)

    def testResave2290(self):
        pix = self.pix()
        img = pix.getImage()
        img.name = rstring("testRoi")
        img.acquisitionDate = rtime(0)
        pix = self.update.saveAndReturnObject( pix )
        img = pix.getImage()
        roi = omero.model.ROII()
        roi.linkImage(img)
        roi = self.update.saveAndReturnObject(roi)
        rect = self.new_rectangle()
        roi.addShape(rect)
        roi = self.update.saveAndReturnObject(roi)

if __name__ == '__main__':
    unittest.main()
