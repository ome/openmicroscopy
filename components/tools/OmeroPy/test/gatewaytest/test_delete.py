#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   gateway tests - Delete methods

   Copyright 2012-2013 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

   pytest fixtures used as defined in conftest.py:
   - gatewaywrapper

"""

import omero
from omero.gateway.scripts import dbhelpers
import time

class TestDelete (object):

    def _prepareObjectsToDelete (self, gateway, ns):
        """ creates a couple of annotations used in testDeleteObjects* """
        q = gateway.getQueryService()
        ids = [x.id.val for x in \
               q.findAllByQuery("from CommentAnnotation where ns='%s'" % ns, None)]
        assert len(ids) ==  0
        u = gateway.getUpdateService()
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
        assert len(ids) ==  2
        return ids

    def testDeleteObjectsUnwrapped (self, gatewaywrapper):
        """ tests async delete objects """
        ns = 'testDeleteObjects-'+str(time.time())
        gatewaywrapper.loginAsAuthor()
        ids = self._prepareObjectsToDelete(gatewaywrapper.gateway, ns)
        # This is the same as BlitzGateway.deleteObjects(), just unrolled here
        # for verbosity and to make sure the more generalistic code there
        # isn't to blame for any issue
        dcs = list()
        op = dict()
        for oid in ids:
            dcs.append(omero.cmd.Delete('/Annotation', long(oid), op))
        doall = omero.cmd.DoAll()
        doall.requests = dcs
        handle = gatewaywrapper.gateway.c.sf.submit(doall, gatewaywrapper.gateway.SERVICE_OPTS)
        gatewaywrapper.gateway._waitOnCmd(handle)
        handle.close()
        q = gatewaywrapper.gateway.getQueryService()
        ids = [x.id.val for x in \
               q.findAllByQuery("from CommentAnnotation where ns='%s'" % ns, None)]
        assert len(ids) ==  0

    def testDeleteObjects (self, gatewaywrapper):
        """ tests the call to deleteObjects """
        ns = 'testDeleteObjects-'+str(time.time())
        gatewaywrapper.loginAsAuthor()
        ids = self._prepareObjectsToDelete(gatewaywrapper.gateway, ns)
        handle = gatewaywrapper.gateway.deleteObjects('Annotation', ids)
        gatewaywrapper.gateway._waitOnCmd(handle)
        handle.close()
        q = gatewaywrapper.gateway.getQueryService()
        ids = [x.id.val for x in \
               q.findAllByQuery("from CommentAnnotation where ns='%s'" % ns, None)]
        assert len(ids) ==  0
