#!/usr/bin/env python

"""
   gateway tests - Testing various methods on a Big image when renderingEngine.load() etc throws MissingPyramidException

"""

import exceptions
import unittest
import omero
import time

import gatewaytest.library as lib


class PyramidTest (lib.GTest):

    def setUp (self):
        super(PyramidTest, self).setUp()
        self.loginAsAuthor()
        self.TESTIMG = self.getTestImage()


    def testThrowException(self):
        """ test that image._prepareRE() throws MissingPyramidException """
        
        image = self.TESTIMG
        image._conn.createRenderingEngine = lambda: MockRenderingEngine()

        try:
            image._prepareRE()
            self.assertTrue(False, "_prepareRE should have thrown an exception")
        except omero.ConcurrencyException, ce:
            print "Handling MissingPyramidException with backoff: %s secs" % (ce.backOff/1000)


    def testPrepareRenderingEngine(self):
        """ We need image._prepareRenderingEngine() to raise MissingPyramidException"""

        image = self.TESTIMG
        image._conn.createRenderingEngine = lambda: MockRenderingEngine()
        
        try:
            image._prepareRenderingEngine()
            self.assertTrue(False, "_prepareRenderingEngine() should have thrown an exception")
        except omero.ConcurrencyException, ce:
            print "Handling MissingPyramidException with backoff: %s secs" % (ce.backOff/1000)


    def XtestGetChannels(self):
        """ Missing Pyramid shouldn't stop us from getting Channel Info """
        
        image = self.TESTIMG
        image._conn.createRenderingEngine = lambda: MockRenderingEngine()
        
        channels = image.getChannels()
        for c in channels:
            print c.getLabel()
            

class MockRenderingEngine(object):
    """ Should throw on re.load() """
    
    def lookupPixels(self, id):
        pass
    
    def lookupRenderingDef(self, id):
        pass
    
    def loadRenderingDef(self, id):
        pass
    
    def resetDefaults(self):
        pass

    def getRenderingDefId(self):
        return 1

    def load(self):
        e = omero.ConcurrencyException("MOCK MissingPyramidException")
        e.backOff = (3 * 60 * 60 * 1000) + (20 * 60 * 1000) + (45 * 1000) # 3 hours
        raise e
    
if __name__ == '__main__':
    unittest.main()
