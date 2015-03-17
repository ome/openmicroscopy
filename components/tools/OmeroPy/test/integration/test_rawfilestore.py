#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Tests for the stateful RawFileStore service.

   Copyright 2010-2014 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import omero
import library as lib
import pytest

from omero.rtypes import rstring, rlong


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

    @pytest.mark.broken(ticket="11534")
    def testTicket1961Basic(self):
        ofile = self.file()
        rfs = self.client.sf.createRawFileStore()
        rfs.setFileId(ofile.id.val)
        rfs.write([0, 1, 2, 3], 0, 4)
        rfs.close()
        self.check_file(ofile)

    @pytest.mark.broken(ticket="11534")
    def testTicket1961WithKillSession(self):
        ofile = self.file()
        grp = self.ctx.groupName
        session = self.client.sf.getSessionService().createUserSession(
            1 * 1000, 10000, grp)
        properties = self.client.getPropertyMap()

        c = omero.client(properties)
        s = c.joinSession(session.uuid.val)

        rfs = s.createRawFileStore()
        rfs.setFileId(ofile.id.val)
        rfs.write([0, 1, 2, 3], 0, 4)

        c.killSession()
        self.check_file(ofile)

    @pytest.mark.broken(ticket="11534")
    def testTicket2161Save(self):
        ofile = self.file()
        rfs = self.client.sf.createRawFileStore()
        rfs.setFileId(ofile.id.val)
        rfs.write([0, 1, 2, 3], 0, 4)
        ofile = rfs.save()
        self.check_file(ofile)
        rfs.close()
        ofile2 = self.query.get("OriginalFile", ofile.id.val)
        assert ofile.details.updateEvent.id.val \
            == ofile2.details.updateEvent.id.val

    @pytest.mark.broken(ticket="11534")
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

    def dummy_file(self):
        """
        Create an object of size 4
        """
        ofile = self.file(client=self.client)
        rfs = self.sf.createRawFileStore()
        try:
            rfs.setFileId(ofile.id.val)
            rfs.write("0123", 0, 4)
            ofile = rfs.save()
            assert 4 == ofile.size.val
            return ofile
        finally:
            rfs.close()

    def testNullSize11743(self):

        ofile = self.dummy_file()

        # Synthetically null the size
        ofile.size = None
        self.update.saveObject(ofile)

        # Assert the size is null
        ofile = self.query.get("OriginalFile", ofile.id.val)
        assert ofile.size is None

        # Show that the size can be loaded from the service
        rfs = self.sf.createRawFileStore()
        try:
            rfs.setFileId(ofile.id.val)
            assert 4 == rfs.size()
            rfs.write([], 0, 0)  # touch
            ofile = rfs.save()
            assert ofile.size is not None
        finally:
            rfs.close()

    def testGetFileId(self):
        ofile = self.dummy_file()
        rfs = self.sf.createRawFileStore()
        try:
            rfs.getFileId()
            rfs.setFileId(ofile.id.val)
            assert rfs.getFileId() == ofile.id.val
        except:
            rfs.close()
