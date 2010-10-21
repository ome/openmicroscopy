#!/usr/bin/env python

"""
   Integration test for search testing

   Copyright 2010 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import unittest
import integration.library as lib
import omero
import datetime, time

class TestSearch(lib.ITest):

    def test3164(self):
        images = list()
        for i in range(0,5):
            img = omero.model.ImageI()
            img.name = omero.rtypes.rstring("search_test_%i.tif" % i)
            img.acquisitionDate = omero.rtypes.rtime(0)
            tag = omero.model.TagAnnotationI()
            tag.textValue = omero.rtypes.rstring("tag %i" % i)
            img.linkAnnotation( tag )

            images.append(self.client.sf.getUpdateService().saveAndReturnObject( img ))
            self.index(images[-1])

        p = omero.sys.Parameters()
        p.map = {}
        p.map["oids"] = omero.rtypes.rlist(im.id for im in images)

        sql = "select im from Image im "\
                "where im.id in (:oids) " \
                "order by im.id asc"
        res = self.client.sf.getQueryService().findAllByQuery(sql, p)
        self.assertEquals(5, len(res))

        #Searching
        texts = ("*earch", "*h", "search tif", "search",\
                 "test", "tag", "t*", "search_test",\
                 "*test*.tif", "search*tif", "s .tif",\
                 ".tif", "tif", "*tif",\
                 "s*.tif", "*.tif")

        search = self.client.sf.createSearchService()
        search.onlyType('Image')
        search.addOrderByAsc("name")
        search.setAllowLeadingWildcard(True)

        failed = {}
        for text in texts:
            search.byFullText(str(text))
            if search.hasNext():
                sz = len(search.results())
            else:
                sz = 0
            if 5 != sz:
                failed[text] = sz

        msg = ""
        for k in sorted(failed):
            msg += """\nFAILED: `%s` returned %s""" % (k, failed[k])

        if msg:
            self.fail("%s\n" % msg)


if __name__ == '__main__':
    unittest.main()
