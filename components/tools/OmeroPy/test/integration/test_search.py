#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Integration test for search testing

   Copyright 2010 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import unittest
import test.integration.library as lib
import omero
import datetime, time

class TestSearch(lib.ITest):

    def test2541(self):
        """
        Search for private data from another user
        """
        group = self.new_group(perms="rw----")
        owner = self.new_client(group)
        searcher = self.new_client(group)
        uuid = self.uuid().replace("-", "")
        tag = omero.model.TagAnnotationI()
        tag.ns = omero.rtypes.rstring(uuid)
        tag = self.update.saveAndReturnObject(tag)
        self.root.sf.getUpdateService().indexObject(tag)
        q = searcher.sf.getQueryService()
        r = q.findAllByFullText("TagAnnotation", uuid, None)
        self.assertEquals(0, len(r))

    def test3164Private(self):
        group = self.new_group(perms="rw----")
        owner = self.new_client(group)
        searcher = self.new_client(group)
        self._3164(owner, owner)

    def test3164ReadOnlySelf(self):
        group = self.new_group(perms="rwr---")
        owner = self.new_client(group)
        self._3164(owner, owner)

    def test3164ReadOnlyOther(self):
        group = self.new_group(perms="rwr---")
        owner = self.new_client(group)
        searcher = self.new_client(group)
        self._3164(owner, searcher)

    def test3164CollabSelf(self):
        group = self.new_group(perms="rwrw--")
        owner = self.new_client(group)
        self._3164(owner, owner)

    def test3164CollabOther(self):
        group = self.new_group(perms="rwrw--")
        owner = self.new_client(group)
        searcher = self.new_client(group)
        self._3164(owner, searcher)

    def test3721Ordering(self):
        """
        Creates two tags and checks that boosting
        works properly on their namespaces.

        tag1^10 OR tag2^1 should return a list
        with tag1 first, and the reverse should
        hold as well.
        """
        tags = list()
        for x in range(2):
            tag = omero.model.TagAnnotationI()
            tag.ns = omero.rtypes.rstring(self.uuid())
            tag = self.update.saveAndReturnObject(tag)
            self.index(tag)
            tags.append(tag.ns.val)

        search = self.client.sf.createSearchService()
        search.onlyType("TagAnnotation")

        # Sanity check
        for tag in tags:
            search.byFullText(tag)
            res = search.results()
            self.assertEquals(tag, res[0].ns.val)

        boost_query = "%s^10 OR %s^1"

        # Boosted
        search.byFullText(boost_query % tuple(tags))
        res = search.results()
        self.assertEquals(tags[0], res[0].ns.val)
        self.assertEquals(tags[1], res[1].ns.val)

        # Reversed
        search.byFullText(boost_query % tuple(reversed(tags)))
        res = search.results()
        self.assertEquals(tags[0], res[1].ns.val)
        self.assertEquals(tags[1], res[0].ns.val)

    #
    # Helpers
    #
    def _3164(self, owner, searcher):

        images = list()
        for i in range(0,5):
            img = omero.model.ImageI()
            img.name = omero.rtypes.rstring("search_test_%i.tif" % i)
            img.acquisitionDate = omero.rtypes.rtime(0)
            tag = omero.model.TagAnnotationI()
            tag.textValue = omero.rtypes.rstring("tag %i" % i)
            img.linkAnnotation( tag )

            images.append(owner.sf.getUpdateService().saveAndReturnObject( img ))
            self.index(images[-1])

        p = omero.sys.Parameters()
        p.map = {}
        p.map["oids"] = omero.rtypes.rlist(im.id for im in images)

        sql = "select im from Image im "\
                "where im.id in (:oids) " \
                "order by im.id asc"
        res = owner.sf.getQueryService().findAllByQuery(sql, p)
        self.assertEquals(5, len(res))

        #Searching
        texts = ("*earch", "*h", "search tif", "search",\
                 "test", "tag", "t*", "search_test",\
                 "s .tif", ".tif", "tif", "*tif")

        BROKEN = ("*test*.tif", "search*tif", "s*.tif", "*.tif")

        search = searcher.sf.createSearchService()
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

    def test8692(self):
        # Test that group admin and system admins can
        # find items in non-private groups.

        all = {"omero.group": "-1"}
        msg = "%s: Count not find ann=%s as %s in %s"

        for x in ("rw", "rwr", "rwra", "rwrw"):
            p = x.ljust(6, "-")
            g = self.new_group(perms=p)
            u = self.new_client(group=g)
            a = self.new_client(group=g, admin=True)

            uuid = self.uuid().replace("-","")

            # Create a comment as the user
            t = omero.model.CommentAnnotationI()
            t.setTextValue(omero.rtypes.rstring(uuid))
            t = u.sf.getUpdateService().saveAndReturnObject(t)
            self.root.sf.getUpdateService().indexObject(t) # Index

            # And try to read it back as the leader and the admin
            for sf, who in ((a.sf, "grp-admin"), (self.root.sf, "sys-admin")):

                # First see if IQuery.findAllByFullText works
                # Note: it's necessary to pass {"omero.group":"-1"}
                q = sf.getQueryService()
                t = q.findAllByFullText("CommentAnnotation", uuid, None, all)
                if not t or len(t) != 1:
                    self.fail(msg % ("IQueryPrx", uuid, who, x))

                # Then see if search also works via SearchPrx
                # Note: it's necessary to pass {"omero.group":"-1"}
                # during hasNext and next/results
                s = sf.createSearchService()
                s.onlyType("CommentAnnotation")
                s.byFullText(uuid)
                if not s.hasNext(all) or len(s.results(all)) != 1:
                    self.fail(msg % ("SearchPrx", uuid, who, x))

    def test8846(self):
        # Wildcard search

        client = self.new_client()
        query = client.sf.getQueryService()
        update = client.sf.getUpdateService()

        uuid = self.uuid().replace("-", "")
        cann = omero.model.CommentAnnotationI()
        cann.textValue = omero.rtypes.rstring(uuid)
        cann = update.saveAndReturnObject(cann)
        self.root.sf.getUpdateService().indexObject(cann)

        rv = query.findAllByFullText( \
                "CommentAnnotation", "%s" % uuid, None)
        #"CommentAnnotation", "%s*" % uuid[0:6], None)
        self.assertEquals(cann.id.val, rv[0].id.val)


if __name__ == '__main__':
    unittest.main()
