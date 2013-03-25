#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   gateway tests - Delete methods

   Copyright 2012 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import unittest
import omero
import gatewaytest.library as lib
from omero.gateway.scripts import dbhelpers
import time

class DeleteTest (lib.GTest):
    def setUp (self):
        super(DeleteTest, self).setUp(skipTestDB=True)

    def _prepareObjectsToDelete (self, ns):
        """ creates a couple of annotations used in testDeleteObjects* """
        q = self.gateway.getQueryService()
        ids = [x.id.val for x in \
               q.findAllByQuery("from CommentAnnotation where ns='%s'" % ns, None)]
        self.assertEqual(len(ids), 0)
        u = self.gateway.getUpdateService()
        ann = omero.gateway.CommentAnnotationWrapper()
        ann.setNs(ns)
        ann.setValue('foo')
        u.saveObject(ann._obj)
        ann = omero.gateway.CommentAnnotationWrapper()
        ann.setNs(ns)
        ann.setValue('')
        u.saveObject(ann._obj)
        ids = [x.id.val for x in \
               q.findAllByQuery("from CommentAnnotation where ns='%s'" % ns, None)]
        self.assertEqual(len(ids), 2)
        return ids

    def testDeleteObjectsUnwrapped (self):
        """ tests async delete objects """
        ns = 'testDeleteObjects-'+str(time.time())
        self.loginAsAuthor()
        ids = self._prepareObjectsToDelete(ns)
        # This is the same as BlitzGateway.deleteObjects(), just unrolled here
        # for verbosity and to make sure the more generalistic code there
        # isn't to blame for any issue
        dcs = list()
        op = dict()
        for oid in ids:
            dcs.append(omero.cmd.Delete('/Annotation', long(oid), op))
        doall = omero.cmd.DoAll()
        doall.requests = dcs
        handle = self.gateway.c.sf.submit(doall, self.gateway.SERVICE_OPTS)
        self.gateway._waitOnCmd(handle)
        handle.close()
        q = self.gateway.getQueryService()
        ids = [x.id.val for x in \
               q.findAllByQuery("from CommentAnnotation where ns='%s'" % ns, None)]
        self.assertEqual(len(ids), 0)

    def testDeleteObjects (self):
        """ tests the call to deleteObjects """
        ns = 'testDeleteObjects-'+str(time.time())
        self.loginAsAuthor()
        ids = self._prepareObjectsToDelete(ns)
        handle = self.gateway.deleteObjects('Annotation', ids)
        self.gateway._waitOnCmd(handle)
        handle.close()
        q = self.gateway.getQueryService()
        ids = [x.id.val for x in \
               q.findAllByQuery("from CommentAnnotation where ns='%s'" % ns, None)]
        self.assertEqual(len(ids), 0)
