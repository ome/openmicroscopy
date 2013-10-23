#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Tests for the stateful RawFileStore service.

   Copyright 2010 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import omero
import test.integration.library as lib
import pytest

from omero.rtypes import rstring, rlong
from omero.util.concurrency import get_event

class TestRFS(lib.ITest):

    def file(self, client=None):
        if client is None:
            client = self.client
        update = client.sf.getUpdateService()
        ofile = omero.model.OriginalFileI()
        ofile.mimetype = rstring("application/octet-stream")
        ofile.name = rstring("test")
        ofile.path = rstring("/tmp/test")
        ofile.hash = rstring("")
        ofile.size = rlong(-1)
        ofile = update.saveAndReturnObject(ofile)
        return ofile

    def check_file(self, ofile, client=None):
        if client is None:
            client = self.client
        query = client.sf.getQueryService()
        ofile = query.get("OriginalFile", ofile.id.val)
        assert ofile.size.val != -1
        assert ofile.hash.val != ""

    @pytest.mark.xfail(reason="see ticket 11534")
    def testTicket1961Basic(self):
        ofile = self.file()
        rfs = self.client.sf.createRawFileStore()
        rfs.setFileId(ofile.id.val)
        rfs.write([0,1,2,3], 0, 4)
        rfs.close()
        self.check_file(ofile)

    @pytest.mark.xfail(reason="see ticket 11534")
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

    @pytest.mark.xfail(reason="see ticket 11534")
    def testTicket2161Save(self):
        ofile = self.file()
        rfs = self.client.sf.createRawFileStore()
        rfs.setFileId(ofile.id.val)
        rfs.write([0,1,2,3], 0, 4)
        ofile = rfs.save()
        self.check_file(ofile)
        rfs.close()
        ofile2 = self.query.get("OriginalFile", ofile.id.val)
        assert ofile.details.updateEvent.id.val ==  ofile2.details.updateEvent.id.val

    @pytest.mark.xfail(reason="see ticket 11534")
    def testNoWrite(self):

        group = self.new_group(perms="rwr---")
        client1 = self.new_client(group=group)
        client2 = self.new_client(group=group)

        ofile = self.file(client=client1)
        rfs = client1.sf.createRawFileStore()
        rfs.setFileId(ofile.id.val)
        rfs.write("0123", 0, 4)
        rfs.close()
        self.check_file(ofile, client=client1)

        rfs = client2.sf.createRawFileStore()
        rfs.setFileId(ofile.id.val)
        try:
            rfs.write("3210", 0, 4)
            assert False, "Require security vio"
        except:
            pass
        rfs.close()
        self.check_file(ofile, client=client2)

        rfs = client1.sf.createRawFileStore()
        rfs.setFileId(ofile.id.val)
        buf = rfs.read(0, 4)
        rfs.close()
        assert "0123" == buf
