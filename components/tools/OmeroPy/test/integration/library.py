#!/usr/bin/env python

"""
   Library for integration tests

   Copyright 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import unittest
import omero
import tempfile
import traceback
import exceptions

class ITest(unittest.TestCase):

    def setUp(self):
        self.client = omero.client()
        self.client.createSession()
        self.tmpfiles = []
        rootpass = self.client.getProperty("omero.rootpass")
        if rootpass:
            self.root = omero.client()
            self.root.createSession("root",rootpass)
        else:
            self.root = None

    def tmpfile(self):
        tmpfile = tempfile.NamedTemporaryFile(mode='w+t')
        self.tmpfiles.append(tmpfile)
        return tmpfile

    def tearDown(self):
        failure = False
        try:
            self.client.closeSession()
        except:
            traceback.print_exc()
            failure = True
        if self.root:
            try:
                self.root.closeSession()
            except:
                traceback.print_exc()
                failure = True
        for tmpfile in self.tmpfiles:
            try:
                tmpfile.close()
            except:
                print "Error closing:"+tmpfile
        if failure:
           raise exceptions.Exception("Exception on client.closeSession")
