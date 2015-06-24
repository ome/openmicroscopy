#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Integration test for any ticket upto #1000

   Copyright 2008-2014 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import library as lib
import pytest
import omero
from omero.rtypes import rint, rlong, rstring

from test.integration.helpers import createTestImage

# Reused bits
params = omero.sys.Parameters()
params.theFilter = omero.sys.Filter()
params.theFilter.limit = rint(1)
params.theFilter.offset = rint(1)


class TestTicket1000(lib.ITest):

    def test711(self):
        exp = omero.model.ExperimenterI()
        exp.omeName = rstring("root")
        list = self.client.sf.getQueryService().findAllByExample(exp, None)
        assert 1 == len(list)

    def test843(self):
        with pytest.raises(omero.ValidationException):
            self.client.sf.getQueryService().get("Experimenter", -1)

    def test880(self):
        try:
            createTestImage(self.client.sf)
            i = self.client.sf.getQueryService().findAll(
                "Image", params.theFilter)[0]
            assert i is not None
            assert i.id is not None
            assert i.details is not None
        except omero.ValidationException:
            print " test880 - createTestImage has failed. "\
                  "This fixture method needs to be fixed."
        except IndexError:
            print " test880 - findAll has failed so assertions "\
                  "can't be checked. Is this a fail? "

    def test883WithoutClose(self):
        s = self.client.sf.createSearchService()
        s.onlyType("Image")
        s.byHqlQuery("select i from Image i", params)
        if s.hasNext():
            s.results()
        # s.close()

    def test883WithClose(self):
        s = self.client.sf.createSearchService()
        s.onlyType("Dataset")
        s.byHqlQuery("select d from Dataset d", params)
        if s.hasNext():
            s.results()
        s.close()

    def test883Upload(self):
        search = self.client.getSession().createSearchService()
        search.onlyType("OriginalFile")
        search.byHqlQuery(
            "select o from OriginalFile o where o.name = 'stderr'", params)
        if search.hasNext():
            ofile = search.next()
            tmpfile = self.tmpfile()
            self.client.download(ofile, tmpfile)
        else:
            print " test883Upload - no stderr found. Is this a fail? "

        search.close()

    success = "select i from Image i join i.annotationLinks links join "\
              "links.child ann where size(i.datasetLinks) > 0 and ann.id = :id"
    failing = "select i from Image i join i.annotationLinks links join "\
              "links.child ann where ann.id = :id and size(i.datasetLinks) > 0"

    # Both of these queries cause exceptions. Should the first succeed?
    def test985(self):
        prms = omero.sys.Parameters()
        prms.map = {}  # ParamMap
        prms.map["id"] = rlong(53)
        try:
            self.client.sf.getQueryService().findAllByQuery(
                TestTicket1000.success, prms)
        except omero.ValidationException:
            print " test985 - query has failed. Should this query pass? "

        with pytest.raises(omero.ValidationException):
            self.client.sf.getQueryService().findAllByQuery(
                TestTicket1000.failing, prms)

    # removed def test989(self):
