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
from omero.rtypes import wrap, unwrap
import time


class TestDelete (object):

    def _prepareObjectsToDelete(self, gateway, ns):
        """ creates a couple of annotations used in testDeleteObjects* """
        q = gateway.getQueryService()
        ids = [x.id.val for x in q.findAllByQuery(
               "from CommentAnnotation where ns='%s'" % ns, None)]
        assert len(ids) == 0
        u = gateway.getUpdateService()
        ann = omero.gateway.CommentAnnotationWrapper()
        ann.setNs(ns)
        ann.setValue('foo')
        u.saveObject(ann._obj)
        ann = omero.gateway.CommentAnnotationWrapper()
        ann.setNs(ns)
        ann.setValue('')
        u.saveObject(ann._obj)
        ids = [x.id.val for x in q.findAllByQuery(
               "from CommentAnnotation where ns='%s'" % ns, None)]
        assert len(ids) == 2
        return ids

    def testDeleteObjectsUnwrapped(self, gatewaywrapper):
        """ tests async delete objects """
        ns = 'testDeleteObjects-'+str(time.time())
        gatewaywrapper.loginAsAuthor()
        ids = self._prepareObjectsToDelete(gatewaywrapper.gateway, ns)
        # This is the same as BlitzGateway.deleteObjects(), just unrolled here
        # for verbosity and to make sure the more generalistic code there
        # isn't to blame for any issue
        command = omero.cmd.Delete2(targetObjects={'Annotation': ids})
        doall = omero.cmd.DoAll()
        doall.requests = [command]
        handle = gatewaywrapper.gateway.c.sf.submit(
            doall, gatewaywrapper.gateway.SERVICE_OPTS)
        gatewaywrapper.gateway._waitOnCmd(handle)
        handle.close()
        q = gatewaywrapper.gateway.getQueryService()
        ids = [x.id.val for x in q.findAllByQuery(
               "from CommentAnnotation where ns='%s'" % ns, None)]
        assert len(ids) == 0

    def testDeleteObjects(self, gatewaywrapper):
        """ tests the call to deleteObjects """
        ns = 'testDeleteObjects-'+str(time.time())
        gatewaywrapper.loginAsAuthor()
        ids = self._prepareObjectsToDelete(gatewaywrapper.gateway, ns)
        handle = gatewaywrapper.gateway.deleteObjects('Annotation', ids)
        gatewaywrapper.gateway._waitOnCmd(handle)
        handle.close()
        q = gatewaywrapper.gateway.getQueryService()
        ids = [x.id.val for x in q.findAllByQuery(
               "from CommentAnnotation where ns='%s'" % ns, None)]
        assert len(ids) == 0

    def testDeleteAnnotatedFileAnnotation(self, gatewaywrapper):
        """ See trac:11939 """
        ns = 'testDeleteObjects-' + str(time.time())
        gatewaywrapper.loginAsAuthor()
        us = gatewaywrapper.gateway.getUpdateService()
        qs = gatewaywrapper.gateway.getQueryService()

        tag = omero.model.TagAnnotationI()
        tag.setNs(wrap(ns))
        tag.setTextValue(wrap('tag'))
        tag = us.saveAndReturnObject(tag)

        project = omero.model.ProjectI()
        project.setName(wrap('project'))
        project = us.saveAndReturnObject(project)
        pid = unwrap(project.getId())

        ofile = omero.model.OriginalFileI()
        ofile.setName(wrap('filename'))
        ofile.setPath(wrap('filepath'))
        ofile = us.saveAndReturnObject(ofile)
        oid = unwrap(ofile.getId())

        tagAnnLink = omero.model.OriginalFileAnnotationLinkI()
        tagAnnLink.link(omero.model.OriginalFileI(oid, False), tag)
        tagAnnLink = us.saveAndReturnObject(tagAnnLink)

        fileAnn = omero.model.FileAnnotationI()
        fileAnn.setFile(omero.model.OriginalFileI(oid, False))
        fileAnn.setNs(wrap(ns))
        fileAnn.setDescription(wrap('file attachment'))

        fileAnnLink = omero.model.ProjectAnnotationLinkI()
        fileAnnLink.link(omero.model.ProjectI(pid, False), fileAnn)
        fileAnnLink = us.saveAndReturnObject(fileAnnLink)

        faid = unwrap(fileAnnLink.getChild().getId())

        # Delete the file
        handle = gatewaywrapper.gateway.deleteObjects(
            'OriginalFile', [oid], True, True)
        gatewaywrapper.gateway._waitOnCmd(handle)
        handle.close()

        assert qs.find('OriginalFile', oid) is None
        assert qs.find('FileAnnotation', faid) is None
