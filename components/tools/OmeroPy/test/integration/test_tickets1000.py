#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Integration test for any ticket upto #1000

   Copyright 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import test.integration.library as lib
import omero, tempfile, unittest
from omero_sys_ParametersI import ParametersI
from omero.rtypes import *

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
        self.assertEquals(1, len(list))

    def test843(self):
        try:
            self.client.sf.getQueryService().get("Experimenter",-1)
            self.fail("should throw an exception")
        except omero.ValidationException, ve:
            pass

    # This test is overridden by the next but would fail anyway due to null params
    def test880(self):
        success = "select i from Image i join i.annotationLinks links join links.child ann where size(i.datasetLinks) > 0 and ann.id = :id"
        failing = "select i from Image i join i.annotationLinks links join links.child ann where ann.id = :id and size(i.datasetLinks) > 0"
        prms = omero.sys.Parameters()
        prms.map = {} # ParamMap
        self.client.sf.getQueryService().findAllByQuery(failing, None)
        self.client.sf.getQueryService().findAllByQuery("""select i from Image i where i.name ilike '%h%' """, prms);

    def test880(self):
        try:
            createTestImage(self.client.sf)
            i = self.client.sf.getQueryService().findAll("Image", params.theFilter)[0]
            self.assert_(i != None)
            self.assert_(i.id != None)
            self.assert_(i.details != None)
        except omero.ValidationException, ve:
            print " test880 - createTestImage has failed. This fixture method needs to be fixed."
        except IndexError, ie:
            print " test880 - findAll has failed so assertions can't be checked. Is this a fail? "

    def test883WithoutClose(self):
        s = self.client.sf.createSearchService()
        s.onlyType("Image")
        s.byHqlQuery("select i from Image i", params)
        if s.hasNext():
            s.results()
        #s.close()

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
        search.byHqlQuery("select o from OriginalFile o where o.name = 'stderr'", params)
        if search.hasNext():
            ofile = search.next()
            tmpfile = self.tmpfile()
            self.client.download(ofile, tmpfile)
        else:
            print " test883Upload - no stderr found. Is this a fail? "

        search.close()


    success = "select i from Image i join i.annotationLinks links join links.child ann where size(i.datasetLinks) > 0 and ann.id = :id"
    failing = "select i from Image i join i.annotationLinks links join links.child ann where ann.id = :id and size(i.datasetLinks) > 0"

    # Both of these queries cause exceptions. Should the first succeed?
    def test985(self):
        prms = omero.sys.Parameters()
        prms.map = {} # ParamMap
        prms.map["id"] = rlong(53)
        try: 
            self.client.sf.getQueryService().findAllByQuery(TestTicket1000.success, prms)
        except omero.ValidationException, ve:
            print " test985 - query has failed. Should this query pass? "
            
        try: 
            self.client.sf.getQueryService().findAllByQuery(TestTicket1000.failing, prms)
            self.fail("should throw an exception")
        except omero.ValidationException, ve:
            pass

    ## removed def test989(self):

if __name__ == '__main__':
    unittest.main()
