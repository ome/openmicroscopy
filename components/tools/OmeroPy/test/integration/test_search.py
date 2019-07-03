#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
   Integration test for search testing

   Copyright 2010-2014 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

from omero.testlib import ITest
import pytest
import omero
import os


class TestSearch(ITest):

    def test2541(self):
        """
        Search for private data from another user
        """
        group = self.new_group(perms="rw----")
        searcher = self.new_client(group)
        uuid = self.uuid().replace("-", "")
        tag = omero.model.TagAnnotationI()
        tag.ns = omero.rtypes.rstring(uuid)
        proj = self.new_project(name="test_search")
        proj.linkAnnotation(tag)
        proj = self.update.saveAndReturnObject(proj)
        self.index(proj)
        q = searcher.sf.getQueryService()
        r = q.findAllByFullText("TagAnnotation", uuid, None)
        assert 0 == len(r)

    def test3164Private(self):
        group = self.new_group(perms="rw----")
        owner = self.new_client(group)
        self._3164_setup(owner)
        failed = self._3164_search(owner)
        self._3164_assert(failed)

    def test3164ReadOnlySelf(self):
        group = self.new_group(perms="rwr---")
        owner = self.new_client(group)
        self._3164_setup(owner)
        failed = self._3164_search(owner)
        self._3164_assert(failed)

    def test3164ReadOnlyOther(self):
        group = self.new_group(perms="rwr---")
        owner = self.new_client(group)
        searcher = self.new_client(group)
        self._3164_setup(owner)
        failed = self._3164_search(searcher)
        self._3164_assert(failed)

    def test3164CollabSelf(self):
        group = self.new_group(perms="rwrw--")
        owner = self.new_client(group)
        self._3164_setup(owner)
        failed = self._3164_search(owner)
        self._3164_assert(failed)

    def test3164CollabOther(self):
        group = self.new_group(perms="rwrw--")
        owner = self.new_client(group)
        searcher = self.new_client(group)
        self._3164_setup(owner)
        failed = self._3164_search(searcher)
        self._3164_assert(failed)

    def test3721Ordering(self):
        """
        Creates two tags and checks that boosting
        works properly on their namespaces.

        tag1^10 OR tag2^1 should return a list
        with tag1 first, and the reverse should
        hold as well.
        """
        proj_by_ns = {}
        for x in range(2):
            ns = self.uuid()
            tag = omero.model.TagAnnotationI()
            tag.ns = omero.rtypes.rstring(ns)
            proj = self.new_project(name="test_search")
            proj.linkAnnotation(tag)
            proj = self.update.saveAndReturnObject(proj)
            proj_by_ns[ns] = proj
            self.index(proj)
        all_ns = proj_by_ns.keys()

        search = self.client.sf.createSearchService()
        search.onlyType("Project")

        # Sanity check
        for ns in all_ns:
            search.byFullText(ns)
            res = search.results()
            assert proj_by_ns[ns].id.val == res[0].id.val

        boost_query = "%s^10 OR %s^1"

        # Boosted
        search.byFullText(boost_query % tuple(all_ns))
        res = search.results()
        assert proj_by_ns[all_ns[0]].id.val == res[0].id.val
        assert proj_by_ns[all_ns[1]].id.val == res[1].id.val

        # Reversed
        search.byFullText(boost_query % tuple(reversed(all_ns)))
        res = search.results()
        assert proj_by_ns[all_ns[0]].id.val == res[1].id.val
        assert proj_by_ns[all_ns[1]].id.val == res[0].id.val

    #
    # Helpers
    #
    def _3164_setup(self, owner):

        images = list()
        for i in range(0, 5):
            img = self.new_image(name="search_test_%i.tif" % i)
            tag = omero.model.TagAnnotationI()
            tag.textValue = omero.rtypes.rstring("tag %i" % i)
            img.linkAnnotation(tag)

            images.append(owner.sf.getUpdateService().saveAndReturnObject(img))
            self.index(images[-1])

        p = omero.sys.ParametersI()
        p.addIds(im.id for im in images)

        hql = "FROM Image WHERE id IN (:ids)"
        res = owner.sf.getQueryService().findAllByQuery(hql, p)
        assert 5 == len(res)

    def _3164_search(self, searcher):
        texts = ("*earch", "*rch", "search tif", "search",
                 "test", "tag", "ta*", "search_test",
                 "s .tif", ".tif", "tif", "*tif")

        # Commented out to pass flake8.
        # BROKEN = ("*test*.tif", "search*tif", "s*.tif", "*.tif")

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
        if failed:
            print "%i fails" % len(failed)
        return failed

    def _3164_assert(self, failed):
        msg = ""
        for k in sorted(failed):
            msg += """\nFAILED: `%s` returned %s""" % (k, failed[k])

        if msg:
            assert False, "%s\n" % msg

    def test8692(self):
        # Test that group admin and system admins can
        # find items in non-private groups.

        all = {"omero.group": "-1"}
        msg = "%s: Count not find ann=%s as %s in %s"

        for x in ("rw", "rwr", "rwra", "rwrw"):
            p = x.ljust(6, "-")
            g = self.new_group(perms=p)
            u = self.new_client(group=g)
            a = self.new_client(group=g, owner=True)

            uuid = self.uuid().replace("-", "")

            # Create a comment as the user
            t = omero.model.CommentAnnotationI()
            t.setTextValue(omero.rtypes.rstring(uuid))
            proj = self.new_project(name="test_search")
            proj.linkAnnotation(t)
            proj = u.sf.getUpdateService().saveAndReturnObject(proj)
            self.index(proj)  # Index

            # And try to read it back as the leader and the admin
            for sf, who in ((a.sf, "grp-admin"), (self.root.sf, "sys-admin")):

                # First see if IQuery.findAllByFullText works
                # Note: it's necessary to pass {"omero.group":"-1"}
                q = sf.getQueryService()
                t = q.findAllByFullText("Project", uuid, None, all)
                if not t or len(t) != 1:
                    assert False, msg % ("IQueryPrx", uuid, who, x)

                # Then see if search also works via SearchPrx
                # Note: it's necessary to pass {"omero.group":"-1"}
                # during hasNext and next/results
                s = sf.createSearchService()
                s.onlyType("Project")
                s.byFullText(uuid)
                if not s.hasNext(all) or len(s.results(all)) != 1:
                    assert False, msg % ("SearchPrx", uuid, who, x)

    def test8846(self):
        # Wildcard search

        client = self.new_client()
        query = client.sf.getQueryService()
        update = client.sf.getUpdateService()

        uuid = self.uuid().replace("-", "")
        cann = omero.model.CommentAnnotationI()
        cann.textValue = omero.rtypes.rstring(uuid)
        proj = self.new_project(name="test_search")
        proj.linkAnnotation(cann)
        proj = update.saveAndReturnObject(proj)
        self.index(proj)

        rv = query.findAllByFullText(
            "Project", "%s" % uuid, None)
        # "CommentAnnotation", "%s*" % uuid[0:6], None)
        assert proj.id.val == rv[0].id.val

    def simple_uuid(self):
        uuid = self.uuid()
        uuid = uuid.replace("-", "")
        uuid = "t" + self.uuid().replace("-", "")[0:8]
        return uuid

    def testFilename(self):
        client = self.new_client()
        uuid = self.simple_uuid()
        images = self.import_fake_file(name=uuid + ';', client=client)
        image = images[0]
        self.index(image)
        search = client.sf.createSearchService()
        search.onlyType("Image")
        search.setAllowLeadingWildcard(True)

        def supported(x):
            search.byFullText(x)
            assert search.hasNext(), "None found for " + x
            assert [image.id.val] == [i.id.val for i in search.results()]
            assert not search.hasNext()

        def unsupported(x):
            search.byFullText(x)
            assert not search.hasNext(), "Found for %s!" % x

        for x, m in (
                (".fake", supported),
                ("fake", supported),
                ("%s*" % uuid[:-1], supported),
                (uuid, supported),
                #
                ("*.fake", unsupported),
                ("%s*.fake" % uuid[:-1], unsupported)):

            m(x)

        search.close()

    def attached_image(self, uuid, client, path, mimetype):
        _ = omero.rtypes.rstring
        images = self.import_fake_file(name=uuid, client=client)
        image = images[0]
        ofile = omero.model.OriginalFileI()
        ofile.mimetype = _(mimetype)
        ofile.path = _(os.path.dirname(path))
        ofile.name = _(os.path.basename(path))
        ofile = client.upload(path, ofile=ofile)
        fa = omero.model.FileAnnotationI()
        fa.file = ofile.proxy()
        self.link(image, fa, client=client)
        self.index(image)
        return image

    def test_csv_attachment(self, tmpdir):
        uuid = self.simple_uuid()
        client = self.new_client()
        filename = "%s.csv" % uuid
        csv = tmpdir.join(filename)
        csv.write("Header1,Header2\nGFP\n100.0\n")
        image = self.attached_image(
            uuid, client, str(csv), "text/csv")

        search = client.sf.createSearchService()
        try:
            search.onlyType("Image")
            search.byFullText("GFP")
            assert search.hasNext()
            assert [image.id.val] == \
                [x.id.val for x in search.results()]
        finally:
            search.close()

    def test_txt_attachment(self, tmpdir):
        uuid = self.simple_uuid()
        client = self.new_client()
        filename = "weird attachment.txt"
        txt = tmpdir.join(filename)
        txt.write("crazy")
        image = self.attached_image(
            uuid, client, str(txt), "text/plain")
        self.index(image)

        search = client.sf.createSearchService()
        try:
            for t in ("Image", "Annotation"):
                search.onlyType("Image")
                for x in ("crazy", "weird"):
                    search.byFullText(x)
                    assert search.hasNext()
                    assert [image.id.val] == \
                           [x.id.val for x in search.results()]
        finally:
            search.close()

    def test_word_portions(self):
        word = "onomatopoeia"
        client = self.new_client()
        tag = omero.model.TagAnnotationI()
        tag.textValue = omero.rtypes.rstring(word)
        proj = self.new_project(name="test_search")
        proj.linkAnnotation(tag)
        proj = client.sf.getUpdateService().saveAndReturnObject(proj)
        self.index(proj)

        search = client.sf.createSearchService()
        search.onlyType("Project")

        try:
            for idx in range(len(word) - 1, 6, -1):
                base = word[0:idx]
                for pattern in ("%s*", "%s~0.1"):
                    q = pattern % base
                    search.byFullText(q)
                    assert search.hasNext(), "Nothing for " + q
                    assert [proj.id.val] == \
                           [x.id.val for x in search.results()]

        finally:
            search.close()

    def test_empty_query_string(self):
        client = self.new_client()

        search = client.sf.createSearchService()
        search.onlyType("Image")

        try:
            search.byLuceneQueryBuilder("", "", "", "", "%")
        finally:
            search.close()

    @pytest.mark.parametrize("test", (
        "very small", "very-small", "very_small",
        "small very",
        # TODO: "small-very", "small_very", <-- these do NOT work
    ))
    @pytest.mark.parametrize("name", (
        "very-small", "very_small", "very small",
    ))
    def test_hyphen_underscore(self, name, test):
        client = self.new_client()
        proj = self.make_project(name, client=client)
        self.index(proj)

        search = client.sf.createSearchService()
        search.onlyType("Project")

        try:
            search.byLuceneQueryBuilder("", "", "", "", test)
            assert search.hasNext()
            assert proj.id.val in [
                x.id.val for x in search.results()
            ]
        finally:
            search.close()

    def test_map_annotations(self):
        client = self.new_client()
        key = "k" + self.simple_uuid()
        val = "v" + self.simple_uuid()
        ann = omero.model.MapAnnotationI()
        ann.setMapValue([
            omero.model.NamedValue(key, val)
        ])
        proj = self.new_project(name="test_map_annotations")
        proj.linkAnnotation(ann)
        proj = client.sf.getUpdateService().saveAndReturnObject(proj)
        self.index(proj)

        search = client.sf.createSearchService()
        search.onlyType("Project")

        try:
            for txt in (key, val,
                        "%s:%s" % (key, val),
                        "has_key:%s" % key):
                search.byFullText(txt)
                assert search.hasNext(), txt
                assert proj.id.val in [
                    x.id.val for x in search.results()
                ], txt

            # The value should not be found as the key
            # Nor should the inverse v/k pair return a result.
            for txt in ("%s:%s" % (val, key),
                        "%s:%s" % ("has_key", val)):

                search.byFullText(txt)
                assert not search.hasNext(), txt

        finally:
            search.close()
