#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Integration tests for tickets between 2000 and 2999
   a running server.

   Copyright 2010 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""
import unittest, time
import test.integration.library as lib
from omero.rtypes import *

class TestTickets3000(lib.ITest):

    def test2396(self):
        uuid = self.uuid()

        # create image
        img = self.new_image()
        img.setName(rstring('test2396-img-%s' % (uuid)))
        img = self.update.saveAndReturnObject(img)
        img.unload()

        format = "txt"
        binary = "12345678910"
        oFile = omero.model.OriginalFileI()
        oFile.setName(rstring(str("txt-name")));
        oFile.setPath(rstring(str("txt-name")));
        oFile.setSize(rlong(len(binary)));
        oFile.setSha1(rstring("pending"));
        oFile.setMimetype(rstring(str(format)));

        of = self.update.saveAndReturnObject(oFile);

        store = self.client.sf.createRawFileStore()
        store.setFileId(of.id.val);
        store.write(binary, 0, 0)
        of = store.save() # See ticket:1501
        store.close()

        fa = omero.model.FileAnnotationI()
        fa.setFile(of)
        l_ia = omero.model.ImageAnnotationLinkI()
        l_ia.setParent(img)
        l_ia.setChild(fa)
        self.update.saveObject(l_ia)

        # Alternatively, unload the file
        of = self.update.saveAndReturnObject(oFile);
        of.unload()

        store = self.client.sf.createRawFileStore()
        store.setFileId(of.id.val);
        store.write(binary, 0, 0)
        # Don't capture from save, but will be saved anyway.
        store.close()

        fa = omero.model.FileAnnotationI()
        fa.setFile(of)
        l_ia = omero.model.ImageAnnotationLinkI()
        l_ia.setParent(img)
        l_ia.setChild(fa)
        self.update.saveObject(l_ia)

    def test2547(self):
        admin = self.root.sf.getAdminService()
        user = self.new_user()
        grps = admin.containedGroups(user.id.val)
        self.assertEquals(2, len(grps))
        non_user = [x for x in grps if x.id.val != 1][0]
        grp = self.new_group()
        admin.addGroups(user, [grp])
        admin.removeGroups(user, [non_user])
        admin.lookupExperimenters()

    def test2628(self):
        q = self.root.sf.getQueryService()
        sql = "select s.uuid "\
              "from EventLog evl join evl.event ev join ev.session s"

        """
          File "/Users/ola/Dev/omero/dist/lib/python/omero_api_IQuery_ice.py", line 138, in findAllByQuery
            return _M_omero.api.IQuery._op_findAllByQuery.invoke(self, ((query, params), _ctx))
        Ice.UnmarshalOutOfBoundsException: exception ::Ice::UnmarshalOutOfBoundsException
        {
            reason =
        }
        """
        # This was never supported
        self.assertRaises(Ice.UnmarshalOutOfBoundsException, q.findAllByQuery, sql, None)

        """
          File "/Users/ola/Dev/omero/dist/lib/python/omero_api_IQuery_ice.py", line 138, in findAllByQuery
            return _M_omero.api.IQuery._op_findAllByQuery.invoke(self, ((query, params), _ctx))
        Ice.UnknownUserException: exception ::Ice::UnknownUserException
        {
            unknown = unknown exception type `'
        }
        """
        p1 = omero.sys.Parameters()
        f1 = omero.sys.Filter()
        f1.limit = rint(100)
        p1.theFilter = f1

        # Nor was this
        self.assertRaises(Ice.UnknownUserException, q.findAllByQuery, sql, p1)

        # Only IQuery.projection can return non-IObject types
        q.projection(sql, p1)

    def test2952(self):

        la = omero.model.LongAnnotationI()
        la.longValue = rlong(123456789)
        la = self.client.sf.getUpdateService().saveAndReturnObject(la)
        self.index(la)

        search = self.client.sf.createSearchService()
        search.onlyType("LongAnnotation")
        s = "%s" % la.longValue.val
        search.byFullText(s)
        res = search.results()

        self.assert_( la.id.val in [x.id.val for x in res] )

    def test2762(self):
        """
        Test that the page (limit/offset) settings on a ParametersI
        are properly handled by IQuery.findAllByFullText
        """

        uuid = self.uuid().replace("-","")
        tas = []
        for x in range(15):
            ta = omero.model.TagAnnotationI()
            ta.setNs(rstring(uuid))
            ta = self.update.saveAndReturnObject(ta)
            tas.append(ta)
            self.root.sf.getUpdateService().indexObject(ta)

        results = self.query.findAllByFullText("TagAnnotation", uuid, None)
        self.assertEquals(len(tas), len(results))

        params = omero.sys.ParametersI()
        params.page(0, 10)
        results = self.query.findAllByFullText("TagAnnotation", uuid, params)
        self.assertEquals(10, len(results))


if __name__ == '__main__':
    unittest.main()
