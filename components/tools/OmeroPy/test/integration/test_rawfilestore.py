#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Tests for the stateful RawFileStore service.

   Copyright 2010 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import omero
import unittest
import test.integration.library as lib

from omero.rtypes import rstring, rlong
from omero.util.concurrency import get_event

class TestRFS(lib.ITest):

    def file(self):
        ofile = omero.model.OriginalFileI()
        ofile.mimetype = rstring("application/octet-stream")
        ofile.name = rstring("test")
        ofile.path = rstring("/tmp/test")
        ofile.sha1 = rstring("")
        ofile.size = rlong(-1)
        ofile = self.update.saveAndReturnObject(ofile)
        return ofile

    def check_file(self, ofile):
        ofile = self.query.get("OriginalFile", ofile.id.val)
        self.assert_(ofile.size.val != -1)
        self.assert_(ofile.sha1.val != "")

    def testTicket1961Basic(self):
        ofile = self.file()
        rfs = self.client.sf.createRawFileStore()
        rfs.setFileId(ofile.id.val)
        rfs.write([0,1,2,3], 0, 4)
        rfs.close()
        self.check_file(ofile)

    def testTicket1961WithKillSession(self):
        ofile = self.file()
        grp = self.client.sf.getAdminService().getEventContext().groupName
        session = self.client.sf.getSessionService().createUserSession(1*1000, 10000, grp)
        properties = self.client.getPropertyMap()

        c = omero.client(properties)
        s = c.joinSession(session.uuid.val)

        rfs = s.createRawFileStore()
        rfs.setFileId(ofile.id.val)
        rfs.write([0,1,2,3], 0, 4)

        c.killSession()
        self.check_file(ofile)

    def testTicket2161Save(self):
        ofile = self.file()
        rfs = self.client.sf.createRawFileStore()
        rfs.setFileId(ofile.id.val)
        rfs.write([0,1,2,3], 0, 4)
        ofile = rfs.save()
        self.check_file(ofile)
        rfs.close()
        ofile2 = self.query.get("OriginalFile", ofile.id.val)
        self.assertEquals(ofile.details.updateEvent.id.val, ofile2.details.updateEvent.id.val)

if __name__ == '__main__':
    unittest.main()
