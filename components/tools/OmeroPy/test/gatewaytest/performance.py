#!/usr/bin/env python

"""
   gateway tests - Testing speed of using Blitz gateway for various queries - particularly checking whether lazy loading
   can hurt performance vv loading graphs.

"""

import exceptions
import unittest
import omero
import time

import gatewaytest.library as lib

from omero.rtypes import rstring, rlong

class PerformanceTest (lib.GTest):

    def setUp (self):
        super(PerformanceTest, self).setUp()
        self.loginAsAuthor()
        self.TESTIMG = self.getTestImage()


    def testListFileAnnotations(self):
        """ testListFileAnnotations: test speed of getObjects('FileAnnotation') vv listFileAnnotations() """

        updateService = self.gateway.getUpdateService()

        def createFileAnnotation(name, ns):
            originalFile = omero.model.OriginalFileI()
            originalFile.setName(rstring(name))
            originalFile.setPath(rstring(name))
            originalFile.setSize(rlong(0))
            originalFile.setSha1(rstring("Foo"))
            originalFile = updateService.saveAndReturnObject(originalFile)
            fa = omero.model.FileAnnotationI()
            fa.setFile(originalFile)
            fa.setNs(rstring(ns))
            fa = updateService.saveAndReturnObject(fa)
            return fa.id.val

        ns = "omero.gatewaytest.PerformanceTest.testListFileAnnotations"
        fileCount = 250

        fileAnnIds = [createFileAnnotation("testListFileAnnotations%s"%i, ns) for i in range(fileCount)]

        # test speed of listFileAnnotations
        startTime = time.time()
        fileCount = 0
        fileAnns = self.gateway.listFileAnnotations(toInclude=[ns])
        for fa in fileAnns:
            name = fa.getFileName()
            fileCount +=1
        t1 = time.time() - startTime
        print "listFileAnnotations for %d files = %s secs" % (fileCount, t1)    # Typically 1.4 secs

        # test speed of getOjbects("Annotation") - lazy loading file names
        startTime = time.time()
        fileCount = 0
        fileAnns = self.gateway.getObjects("FileAnnotation", attributes={'ns':ns})
        for fa in fileAnns:
            name = fa.getFileName()
            fileCount +=1
        t2 = time.time() - startTime
        print "getObjects, lazy loading file names for %d files = %s secs" % (fileCount, t2) # Typically 2.8 secs

        # test speed of getOjbects("Annotation") - NO loading file names
        startTime = time.time()
        fileCount = 0
        fileAnns = self.gateway.getObjects("FileAnnotation", attributes={'ns':ns})
        for fa in fileAnns:
            fid = fa.getId()
            fileCount +=1
        t3 = time.time() - startTime
        print "getObjects, NO file names for %d files = %s secs" % (fileCount, t3)      # Typically 0.4 secs

        self.assertTrue(t1 < t2, "Blitz listFileAnnotations() should be faster than getObjects('FileAnnotation')")
        self.assertTrue(t3 < t2, "Blitz getObjects('FileAnnotation') should be faster without fa.getFileName()")
        self.assertTrue(t3 < t1, "Blitz getting unloaded 'FileAnnotation' should be faster than listFileAnnotations()")

        # now delete what we have created
        self.gateway.deleteObjects("Annotation", fileAnnIds)


if __name__ == '__main__':
    unittest.main()
