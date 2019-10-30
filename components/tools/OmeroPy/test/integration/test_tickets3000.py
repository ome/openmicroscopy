#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Integration tests for tickets between 2000 and 2999.

   Copyright 2010-2014 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""
from builtins import str
from builtins import range
import omero
from omero.testlib import ITest
import pytest
import Ice

from omero.rtypes import rint, rlong, rstring


class TestTickets3000(ITest):

    def test2396(self):
        # create image
        img = self.make_image(name='test2396-img-%s' % self.uuid())

        format = "txt"
        binary = "12345678910"
        oFile = omero.model.OriginalFileI()
        oFile.setName(rstring(str("txt-name")))
        oFile.setPath(rstring(str("txt-name")))
        oFile.setSize(rlong(len(binary)))
        oFile.setHash(rstring("pending"))
        oFile.setMimetype(rstring(str(format)))

        of = self.update.saveAndReturnObject(oFile)

        store = self.client.sf.createRawFileStore()
        store.setFileId(of.id.val)
        store.write(binary, 0, 0)
        of = store.save()  # See ticket:1501
        store.close()

        fa = omero.model.FileAnnotationI()
        fa.setFile(of)

        self.link(img, fa)

        # Alternatively, unload the file
        of = self.update.saveAndReturnObject(oFile)
        of.unload()

        store = self.client.sf.createRawFileStore()
        store.setFileId(of.id.val)
        store.write(binary, 0, 0)
        # Don't capture from save, but will be saved anyway.
        store.close()

        fa = omero.model.FileAnnotationI()
        fa.setFile(of)

        self.link(img, fa)

    def test2547(self):
        admin = self.root.sf.getAdminService()
        user = self.new_user()
        grps = admin.containedGroups(user.id.val)
        assert 2 == len(grps)
        non_user = [x for x in grps if x.id.val != 1][0]
        grp = self.new_group()
        admin.addGroups(user, [grp])
        admin.removeGroups(user, [non_user])
        admin.lookupExperimenters()

    def test2628(self):
        q = self.root.sf.getQueryService()
        sql = "select s.uuid "\
              "from EventLog evl join evl.event ev join ev.session s"

        # This was never supported
        with pytest.raises(
                (Ice.UnmarshalOutOfBoundsException, Ice.UnknownUserException,
                 Ice.UnknownLocalException)):
            q.findAllByQuery(sql, None)

        p1 = omero.sys.Parameters()
        f1 = omero.sys.Filter()
        f1.limit = rint(100)
        p1.theFilter = f1

        # Nor was this
        with pytest.raises((Ice.UnknownUserException,
                            Ice.UnknownLocalException)):
            q.findAllByQuery(sql, p1)

        # Only IQuery.projection can return non-IObject types
        q.projection(sql, p1)

    def test2952(self):
        la = omero.model.LongAnnotationI()
        la.longValue = rlong(123456789)
        proj = self.new_project(name="test_ticket_2952")
        proj.linkAnnotation(la)
        proj = self.update.saveAndReturnObject(proj)
        self.index(proj)

        search = self.client.sf.createSearchService()
        search.onlyType("Project")
        s = str(la.longValue.val)
        search.byFullText(s)
        res = search.results()

        assert proj.id.val in [x.id.val for x in res]

    def test2762(self):
        """
        Test that the page (limit/offset) settings on a ParametersI
        are properly handled by IQuery.findAllByFullText
        """

        uuid = self.uuid().replace("-", "")
        projs = []
        for x in range(15):
            ta = omero.model.TagAnnotationI()
            ta.setNs(rstring(uuid))
            proj = self.new_project(name="test_ticket_2762")
            proj.linkAnnotation(ta)
            proj = self.update.saveAndReturnObject(proj)
            projs.append(proj)
            self.index(proj)

        results = self.query.findAllByFullText("Project", uuid, None)
        assert len(projs) == len(results)

        params = omero.sys.ParametersI()
        params.page(0, 10)
        results = self.query.findAllByFullText("Project", uuid, params)
        assert 10 == len(results)
