#!/usr/bin/env python

"""
   Integration test for any ticket upto #1000

   Copyright 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import test.integration.library as lib
import omero, tempfile, unittest

class TestTicket1000(lib.ITest):

    def test711(self):
        exp = omero.model.ExperimenterI()
        exp.omeName = omero.RString("root")
        list = self.client.sf.getQueryService().findAllByExample(exp, None)
        self.assertEquals(1, len(list))

    def test843(self):
        try:
            self.client.sf.getQueryService().get("Experimenter",-1)
            self.fail("should throw an exception")
        except omero.ValidationException, ve:
            pass

    def test880(self):
        i = self.client.sf.getQueryService().findAll("Image",None)[0]
        self.assert_(i != None)
        self.assert_(i.id != None)
        self.assert_(i.details != None)

    def test883WithoutClose(self):
        s = self.client.sf.createSearchService()
        s.onlyType("Image")
        s.byFullText("root")
        if s.hasNext():
            s.results()
        #s.close()

    def test883WithClose(self):
        s = self.client.sf.createSearchService()
        s.onlyType("Image")
        s.byFullText("root")
        if s.hasNext():
            s.results()
        s.close()

if __name__ == '__main__':
    unittest.main()
