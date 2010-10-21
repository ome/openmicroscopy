#!/usr/bin/env python

"""
   Integration test for delete testing

   Copyright 2010 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import unittest
import integration.library as lib
import omero
import datetime, time

class TestSearch(lib.ITest):

    def testBasicUsage(self):
        images = list()
        for i in range(0,5):
            img = omero.model.ImageI()
            img.name = omero.rtypes.rstring("search_test_%i.tif" % i)
            img.acquisitionDate = omero.rtypes.rtime(0)
            tag = omero.model.TagAnnotationI()
            tag.textValue = omero.rtypes.rstring("tag %i" % i)
            img.linkAnnotation( tag )

            images.append(self.client.sf.getUpdateService().saveAndReturnObject( img ))
        

        img = self.client.sf.getUpdateService().saveAndReturnObject( img )
        
        p = omero.sys.Parameters()
        p.map = {}
        p.map["oids"] = omero.rtypes.rlist(im.id for im in images)
        
        sql = "select im from Image im "\
                "where im.id in (:oids) " \
                "order by im.id asc"
        res = self.client.sf.getQueryService().findAllByQuery(sql, p)
        self.assertEquals(5, len(res))
        
        #Searching
        text = "*.tif"
         
        search = self.client.sf.createSearchService()
        search.onlyType('Image')
        search.addOrderByAsc("name")
        if created:
            search.onlyCreatedBetween(created[0], created[1]);
        if text:
           search.setAllowLeadingWildcard(True)
           search.byFullText(str(text))
        if search.hasNext():
            self.assertEquals(5, len(search.results()))
            #for e in search.results():
            #    print e.id.val
        else:
            raise ValueError('No images found')

    