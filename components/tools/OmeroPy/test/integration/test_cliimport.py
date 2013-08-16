#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2012 Glencoe Software, Inc. All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License along
# with this program; if not, write to the Free Software Foundation, Inc.,
# 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.

"""
Integration tests for bin/omero import
"""


import unittest
import test.integration.library as lib
import omero

from omero.rtypes import rstring
from omero.util.temp_files import create_path

try:
    from PIL import Image  # see ticket:2597
except ImportError:
    import Image  # see ticket:2597


class Fixture(object):

    def __init__(self, client, user):
        self.client = client
        self.user = user
        self.admin = self.client.sf.getAdminService()
        self.query = self.client.sf.getQueryService()
        self.update = self.client.sf.getUpdateService()
        self.context = self.admin.getEventContext()
        self.img = create_path("cliimportfixture.", ".png")
        i = Image.new(mode="1", size=[8,8])
        i.save(self.img)

    def dataset(self, name, ctx=None):
        dataset = omero.model.DatasetI()
        dataset.setName(rstring(name))
        dataset = self.update.saveAndReturnObject(dataset, ctx)
        return dataset

    def check_pix(self, pix_ids):
        if len(pix_ids) != 1:
            raise Exception("Expecting one pixel id: %s" % pix_ids)

    def load_pixels(self, pix_ids):
        self.check_pix(pix_ids)
        return self.query.get("Pixels", long(pix_ids[0]))

    def load_pixel_annotations(self, pix_ids):
        self.check_pix(pix_ids)
        return self.query.findAllByQuery("""select a from Image i
            join i.annotationLinks l
            join l.child a
            join i.pixels p
            where p.id = %s""" % pix_ids[0], None)

class TestCliImport(lib.ITest):

    def assertGroup(self, group, *objects):
        for obj in objects:
            self.assertEquals(group, obj.details.group.id.val)

    def cliimport(self, fixture, *extra_args):
        extra_args = [str(x) for x in extra_args]
        pix = self.import_image(filename=fixture.img, \
                client=fixture.client, \
                extra_args=extra_args)
        if not pix:
            raise Exception("No pixels found!")
        return pix

    def testBasic(self):
        fixture = Fixture(*self.new_client_and_user())
        pix = self.cliimport(fixture)

    def testDatasetTarget(self):
        fixture = Fixture(*self.new_client_and_user())
        dataset = fixture.dataset("testDatasetTarget")
        pix = self.cliimport(fixture, "-d", dataset.id.val)
        pix = fixture.load_pixels(pix)
        self.assertGroup(fixture.context.groupId, dataset, pix)

    def testTargetInDifferentGroup(self):
        fixture = Fixture(*self.new_client_and_user())
        group = self.new_group(experimenters=[fixture.user])
        fixture.admin.getEventContext() # Refresh
        dataset = fixture.dataset("testTargetInDifferentGroup", \
                {"omero.group": str(group.id.val)})
        try:
            fixture.query.find("Dataset", dataset.id.val)
            self.fail("secvio!")
        except:
            pass  # Good; should not find without call context

        self.assertGroup(group.id.val, dataset)
        pix = self.cliimport(fixture, "-d", dataset.id.val)

        fixture.client.sf.setSecurityContext(group)
        pix = fixture.load_pixels(pix)
        self.assertGroup(group.id.val, pix)

    def testAnnotationTextSimple(self):
        fixture = Fixture(*self.new_client_and_user())
        pix = self.cliimport(fixture, \
                "--annotation_ns=test", \
                "--annotation_text=test")
        ann = fixture.load_pixel_annotations(pix)
        self.assertEquals(1, len(ann))
        self.assertEquals("test", ann[0].ns.val)
        self.assertEquals("test", ann[0].textValue.val)

    def testAnnotationTextMultiple(self):
        fixture = Fixture(*self.new_client_and_user())
        pix = self.cliimport(fixture, \
                "--annotation_ns=test", \
                "--annotation_text=test", \
                "--annotation_ns=test", \
                "--annotation_text=test")
        ann = fixture.load_pixel_annotations(pix)
        self.assertEquals(2, len(ann))
        for x in ann:
            self.assertEquals("test", x.ns.val)
            self.assertEquals("test", x.textValue.val)

    def testAnnotationComment(self):
        fixture = Fixture(*self.new_client_and_user())
        comment = omero.model.CommentAnnotationI()
        comment.ns = rstring("test")
        comment.textValue = rstring("test")
        comment = fixture.update.saveAndReturnObject(comment)
        pix = self.cliimport(fixture, \
                "--annotation_link=%s" % comment.id.val)
        ann = fixture.load_pixel_annotations(pix)
        self.assertEquals(1, len(ann))
        self.assertEquals("test", ann[0].ns.val)
        self.assertEquals("test", ann[0].textValue.val)

if __name__ == '__main__':
    unittest.main()
