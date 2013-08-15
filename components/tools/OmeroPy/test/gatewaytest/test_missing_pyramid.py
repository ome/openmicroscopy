#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   gateway tests - Testing various methods on a Big image when renderingEngine.load() etc throws MissingPyramidException

   Copyright 2013 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import omero
import time
import pytest


class TestPyramid (object):

    @pytest.fixture(autouse=True)
    def setUp (self, author_testimg_generated):
        self.image = author_testimg_generated

    def testThrowException(self):
        """ test that image._prepareRE() throws MissingPyramidException """
        self.image._conn.createRenderingEngine = lambda: MockRenderingEngine()

        try:
            self.image._prepareRE()
            assert False, "_prepareRE should have thrown an exception"
        except omero.ConcurrencyException, ce:
            print "Handling MissingPyramidException with backoff: %s secs" % (ce.backOff/1000)


    def testPrepareRenderingEngine(self):
        """ We need image._prepareRenderingEngine() to raise MissingPyramidException"""
        self.image._conn.createRenderingEngine = lambda: MockRenderingEngine()
        
        try:
            self.image._prepareRenderingEngine()
            assert False, "_prepareRenderingEngine() should have thrown an exception"
        except omero.ConcurrencyException, ce:
            print "Handling MissingPyramidException with backoff: %s secs" % (ce.backOff/1000)


    def testGetChannels(self):
        """ Missing Pyramid shouldn't stop us from getting Channel Info """
        self.image._conn.createRenderingEngine = lambda: MockRenderingEngine()

        channels = self.image.getChannels()
        for c in channels:
            print c.getLabel()
            

class MockRenderingEngine(object):
    """ Should throw on re.load() """
    
    def lookupPixels(self, id, ctx=None):
        pass
    
    def lookupRenderingDef(self, id, ctx=None):
        pass
    
    def loadRenderingDef(self, id, ctx=None):
        pass
    
    def resetDefaults(self, ctx=None):
        pass

    def getRenderingDefId(self, ctx=None):
        return 1

    def load(self, ctx=None):
        e = omero.ConcurrencyException("MOCK MissingPyramidException")
        e.backOff = (3 * 60 * 60 * 1000) + (20 * 60 * 1000) + (45 * 1000) # 3 hours
        raise e
    
