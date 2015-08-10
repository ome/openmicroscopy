#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2013-2015 University of Dundee & Open Microscopy Environment.
# All rights reserved.
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

import pytest

import omero
import omero.gateway
from omero.constants.namespaces import NSBULKANNOTATIONS
from omero.gateway import BlitzGateway
from omero.plugins.metadata import Metadata, MetadataControl
from omero.rtypes import rdouble, unwrap
from test.integration.clitest.cli import CLITest


class MetadataTestBase(CLITest):

    def setup_method(self, method):
        super(MetadataTestBase, self).setup_method(method)
        self.name = self.uuid()
        self.image = self.importSingleImage(
            GlobalMetadata={'gmd-' + self.name: 'gmd-' + self.name})

        conn = BlitzGateway(client_obj=self.client)
        self.imageid = unwrap(self.image.getId())
        assert type(self.imageid) == long
        wrapper = conn.getObject("Image", self.imageid)
        self.md = Metadata(wrapper)

    def create_annotations(self, obj):
        tag = self.new_tag('tag-' + self.name)
        fa = self.make_file_annotation(
            'file-' + self.name, format="OMERO.tables", ns=NSBULKANNOTATIONS)
        self.link(obj, tag)
        self.link(obj, fa)
        return tag, fa

    def create_roi(self, img):
        roi = omero.model.RoiI()
        point = omero.model.PointI()
        point.setCx(rdouble(1))
        point.setCy(rdouble(2))
        roi.addShape(point)
        roi.setImage(img)
        roi = self.client.getSession().getUpdateService().saveAndReturnObject(
            roi)
        return roi

    def create_hierarchy(self, img):
        dataset1 = self.new_dataset(name='dataset1-' + self.name)
        dataset2 = self.new_dataset(name='dataset2-' + self.name)
        project1 = self.new_project(name='project1-' + self.name)
        self.link(project1, dataset1)
        self.link(dataset1, img)
        self.link(dataset2, img)
        return dataset1, dataset2, project1


class TestMetadata(MetadataTestBase):
    """
    Test the get methods in Metadata

    Some tests are combined for efficiency (import is relatively slow)
    """

    def test_get_identifiers(self):
        assert self.md.get_type() == "Image"
        assert self.md.get_id() == self.imageid
        assert self.md.get_name() == "Image:%d" % self.imageid

    @pytest.mark.parametrize('parents', [False, True])
    def test_get_parents(self, parents):
        if parents:
            dataset1, dataset2, project1 = self.create_hierarchy(self.image)
            expected = set((unwrap(dataset1.getName()),
                           unwrap(dataset2.getName())))

            assert self.md.get_parent().getName() in expected
            assert set(p.getName() for p in self.md.get_parents()) == expected
        else:
            assert self.md.get_parent() is None
            assert self.md.get_parents() == []

    @pytest.mark.parametrize('nrois', [0, 1])
    def test_get_roi_count(self, nrois):
        if nrois:
            self.create_roi(self.image)
        assert self.md.get_roi_count() == nrois

    def test_get_original(self):
        origmd = self.md.get_original()
        assert len(origmd) == 3
        assert origmd[0] is None
        assert len(origmd[1]) == 2
        assert len(origmd[2]) == 0

    @pytest.mark.parametrize('annotations', [False, True])
    def test_get_bulkanns(self, annotations):
        if annotations:
            tag, fa = self.create_annotations(self.image)

        bulkanns = self.md.get_bulkanns()

        if annotations:
            assert len(bulkanns) == 1
            b0 = bulkanns[0]
            assert isinstance(b0.obj_wrapper,
                              omero.gateway.FileAnnotationWrapper)
            assert b0.getFileName() == unwrap(fa.getFile().getName())
        else:
            assert bulkanns == []

    @pytest.mark.parametrize('annotations', [False, True])
    def test_get_allanns(self, annotations):
        if annotations:
            tag, fa = self.create_annotations(self.image)

        allanns = self.md.get_allanns()

        if annotations:
            assert len(allanns) == 2
            a0 = allanns[0]
            a1 = allanns[1]
            if isinstance(a0.obj_wrapper,
                          omero.gateway.FileAnnotationWrapper):
                assert isinstance(a1.obj_wrapper,
                                  omero.gateway.TagAnnotationWrapper)
                assert a0.getName() == unwrap(fa.getFile().getName())
                assert a1.getFileName() == unwrap(tag.getName())
            else:
                assert isinstance(a1.obj_wrapper,
                                  omero.gateway.FileAnnotationWrapper)
                assert isinstance(a0.obj_wrapper,
                                  omero.gateway.TagAnnotationWrapper)
                assert a1.getFileName() == unwrap(fa.getFile().getName())
                assert a0.getName() == unwrap(tag.getName())
        else:
            assert allanns == []


class TestMetadataControl(MetadataTestBase):

    def setup_method(self, method):
        super(TestMetadataControl, self).setup_method(method)
        self.cli.register("metadata", MetadataControl, "TEST")
        self.args += ["metadata"]

    # def test_summary(self, capfd):

    def testOriginal(self, capfd):
        uuid = self.uuid()
        img = self.importSingleImage(GlobalMetadata={uuid: uuid})
        prx = "Image:%s" % img.id.val
        self.args += ["original", prx]
        self.cli.invoke(self.args, strict=True)
        o, e = capfd.readouterr()
        assert uuid in o

    # def test_bulkanns(self, capfd):

    # def test_measures(self, capfd):

    # def test_mapanns(self, capfd):

    # def test_allands(self, capfd):

    # def test_populate(self, capfd):
