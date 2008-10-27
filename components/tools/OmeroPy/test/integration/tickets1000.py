#!/usr/bin/env python

"""
   Integration test for any ticket upto #1000

   Copyright 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import test.integration.library as lib
import omero, tempfile, unittest
from omero.rtypes import *

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

    def test880(self):
        i = self.client.sf.getQueryService().findAll("Image", params.theFilter)[0]
        self.assert_(i != None)
        self.assert_(i.id != None)
        self.assert_(i.details != None)

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
        search.byHqlQuery("select o from OriginalFile o where o.name = 'stderr'", params.theFilter)
        if search.hasNext():
            ofile = search.next()
        else:
            self.fail(""" "stderr" not found in index """)

        tmpfile = self.tmpfile()
        self.client.download(ofile, tmpfile.name)
        search.close()


    success = "select i from Image i join i.annotationLinks links join links.child ann where size(i.datasetLinks) > 0 and ann.id = :id"
    failing = "select i from Image i join i.annotationLinks links join links.child ann where ann.id = :id and size(i.datasetLinks) > 0"

    def test985(self):
        prms = omero.sys.Parameters()
        prms.map = {} # ParamMap
        prms.map["id"] = rlong(53)
        self.client.sf.getQueryService().findAllByQuery(TestTicket1000.success, prms);
        self.client.sf.getQueryService().findAllByQuery(TestTicket1000.failing, prms);

    def test989(self):

        try:
           d = self.client.sf.getQueryService().findAllByQuery("select d from Dataset d where name = 'ticket989'",None)[0]
        except:
            self.fail("No dataset named ticket989 found")

        pixelsIds = list()
        pojos = self.client.sf.getPojosService()
        p = omero.sys.Parameters()
        p.map = {}
        p.map[omero.constants.POJOEXPERIMENTER] = rlong(0)

        for e in pojos.getImages("Dataset",[d.id.val],  p.map):
            for px in e.pixels:
                pixelsIds.append(px.id.val)

        self.assert_(len(pixelsIds)>0)

        tb = self.client.sf.createThumbnailStore()
        th = tb.getThumbnailSet(rint(120), rint(120), pixelsIds)
        print th.items()
        for key in th:
            tb.setPixelsId(key)
            print th[key]
            print tb.getThumbnailDirect(rint(120),rint(120))

if __name__ == '__main__':
    unittest.main()
