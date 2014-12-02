#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   gateway tests - Testing speed of using Blitz gateway for various queries
                   particularly checking whether lazy loading can hurt
                   performance vv loading graphs.

   Copyright (C) 2012 University of Dundee & Open Microscopy Environment.
                      All Rights Reserved.
   Copyright 2013 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

   pytest fixtures used as defined in conftest.py:
   - gatewaywrapper

"""

import pytest
import omero
import time

from omero.rtypes import rstring, rlong


class TestPerformance (object):

    @pytest.mark.broken(ticket="11494")
    def testListFileAnnotations(self, gatewaywrapper):
        """
        testListFileAnnotations: test speed of getObjects('FileAnnotation') vv
        listFileAnnotations()
        """
        gatewaywrapper.loginAsAuthor()
        updateService = gatewaywrapper.gateway.getUpdateService()

        def createFileAnnotation(name, ns):
            originalFile = omero.model.OriginalFileI()
            originalFile.setName(rstring(name))
            originalFile.setPath(rstring(name))
            originalFile.setSize(rlong(0))
            originalFile.setHash(rstring("Foo"))
            originalFile = updateService.saveAndReturnObject(originalFile)
            fa = omero.model.FileAnnotationI()
            fa.setFile(originalFile)
            fa.setNs(rstring(ns))
            fa = updateService.saveAndReturnObject(fa)
            return fa.id.val

        ns = "omero.gatewaytest.PerformanceTest.testListFileAnnotations"
        fileCount = 250

        fileAnnIds = [createFileAnnotation("testListFileAnnotations%s" % i,
                      ns) for i in range(fileCount)]

        # test speed of listFileAnnotations
        startTime = time.time()
        fileCount = 0
        fileAnns = gatewaywrapper.gateway.listFileAnnotations(toInclude=[ns])
        for fa in fileAnns:
            fa.getFileName()
            fileCount += 1
        t1 = time.time() - startTime
        print "listFileAnnotations for %d files = %s secs" % (fileCount, t1)
        # Typically 1.4 secs

        # test speed of getOjbects("Annotation") - lazy loading file names
        startTime = time.time()
        fileCount = 0
        fileAnns = gatewaywrapper.gateway.getObjects(
            "FileAnnotation", attributes={'ns': ns})
        for fa in fileAnns:
            fa.getFileName()
            fileCount += 1
        t2 = time.time() - startTime
        print "getObjects, lazy loading file names for %d files = %s secs" \
            % (fileCount, t2)  # Typically 2.8 secs

        # test speed of getOjbects("Annotation") - NO loading file names
        startTime = time.time()
        fileCount = 0
        fileAnns = gatewaywrapper.gateway.getObjects(
            "FileAnnotation", attributes={'ns': ns})
        for fa in fileAnns:
            fa.getId()
            fileCount += 1
        t3 = time.time() - startTime
        print "getObjects, NO file names for %d files = %s secs" \
            % (fileCount, t3)  # Typically 0.4 secs

        assert t1 < t2, "Blitz listFileAnnotations() should be faster " \
            "than getObjects('FileAnnotation')"
        assert t3 < t2, "Blitz getObjects('FileAnnotation') should be " \
            "faster without fa.getFileName()"
        assert t3 < t1, "Blitz getting unloaded 'FileAnnotation' should be" \
            " faster than listFileAnnotations()"

        # now delete what we have created
        handle = gatewaywrapper.gateway.deleteObjects("Annotation", fileAnnIds)
        gatewaywrapper.waitOnCmd(gatewaywrapper.gateway.c, handle)
