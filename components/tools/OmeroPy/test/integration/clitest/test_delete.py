#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2015 University of Dundee & Open Microscopy Environment.
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

import omero
from omero.plugins.delete import DeleteControl
from test.integration.clitest.cli import CLITest
from omero.rtypes import rstring
import pytest

object_types = ["Image", "Dataset", "Project", "Plate", "Screen"]
ordered = [True, False]


class TestDelete(CLITest):

    def setup_method(self, method):
        super(TestDelete, self).setup_method(method)
        self.cli.register("delete", DeleteControl, "TEST")
        self.args += ["delete"]

    def create_object(self, object_type):
        # create object
        if object_type == 'Dataset':
            new_object = omero.model.DatasetI()
        elif object_type == 'Project':
            new_object = omero.model.ProjectI()
        elif object_type == 'Plate':
            new_object = omero.model.PlateI()
        elif object_type == 'Screen':
            new_object = omero.model.ScreenI()
        elif object_type == 'Image':
            new_object = self.new_image()
        new_object.name = rstring("")
        new_object = self.update.saveAndReturnObject(new_object)

        # check object has been created
        found_object = self.query.get(object_type, new_object.id.val)
        assert found_object.id.val == new_object.id.val

        return new_object.id.val

    @pytest.mark.parametrize("object_type", object_types)
    def testDeleteMyData(self, object_type):
        oid = self.create_object(object_type)

        # Delete the object
        self.args += ['/%s:%s' % (object_type, oid)]
        self.cli.invoke(self.args, strict=True)

        # Check the object has been deleted
        assert not self.query.find(object_type, oid)

    @pytest.mark.parametrize('nimages', [1, 2])
    @pytest.mark.parametrize('arguments', ['image', 'fileset'])
    def testFileset(self, nimages, arguments):
        # 2 images sharing a fileset
        images = self.importMIF(nimages)
        img = self.query.get('Image', images[0].id.val)
        filesetId = img.fileset.id.val
        fileset = self.query.get('Fileset', filesetId)
        assert fileset is not None

        # Delete the fileset
        if arguments == 'fileset':
            self.args += ['Fileset:%s' % filesetId]
        else:
            ids = [str(i.id.val) for i in images]
            self.args += ['Image:' + ",".join(ids)]
        self.cli.invoke(self.args, strict=True)

        # Check the fileset and images have been deleted
        assert not self.query.find('Fileset', filesetId)
        for i in images:
            assert not self.query.find('Image', i.id.val)

    def testFilesetPartialFailing(self):
        images = self.importMIF(2)  # 2 images sharing a fileset

        # try to delete only one image
        self.args += ['/Image:%s' % images[0].id.val]
        self.cli.invoke(self.args, strict=True)

        # Check the images have not been deleted
        for i in images:
            assert self.query.get('Image', i.id.val) is not None

    def testFilesetAllImagesDeleteDataset(self):
        images = self.importMIF(2)  # 2 images sharing a fileset
        dataset_id = self.create_object('Dataset')  # ... in a dataset

        # put the images into the dataset
        for image in images:
            link = omero.model.DatasetImageLinkI()
            link.parent = omero.model.DatasetI(dataset_id, False)
            link.child = omero.model.ImageI(image.id.val, False)
            self.update.saveObject(link)

        # try to delete the dataset
        self.args += ['/Dataset:%s' % dataset_id]
        self.cli.invoke(self.args, strict=True)

        # check the dataset has been deleted
        assert not self.query.find('Dataset', dataset_id)

        # check the images have been deleted
        for image in images:
            assert not self.query.find('Image', image.id.val)

    # These tests try to exercise the various grouping possibilities
    # when passing multiple objects on the command line. In all of these
    # cases using the --ordered flag should make no difference

    @pytest.mark.parametrize('number', [1, 2, 3])
    @pytest.mark.parametrize("ordered", ordered)
    def testMultipleSimpleObjectsSameClass(self, number, ordered):
        dsets = [self.make_dataset() for i in range(number)]

        for d in dsets:
            self.args += ['Dataset:%s' % d.id.val]
        if ordered:
            self.args += ["--ordered"]
        self.cli.invoke(self.args, strict=True)

        # Check the objects have been deleted.
        for d in dsets:
            assert not self.query.find('Dataset', d.id.val)

    @pytest.mark.parametrize('number', [1, 2, 3])
    @pytest.mark.parametrize("ordered", ordered)
    def testMultipleSimpleObjectsTwoClassesSeparated(self, number, ordered):
        projs = [self.make_project() for i in range(number)]
        dsets = [self.make_dataset() for i in range(number)]

        for p in projs:
            self.args += ['Project:%s' % p.id.val]
        for d in dsets:
            self.args += ['Dataset:%s' % d.id.val]
        if ordered:
            self.args += ["--ordered"]
        self.cli.invoke(self.args, strict=True)

        # Check the objects have been deleted.
        for p in projs:
            assert not self.query.find('Project', p.id.val)
        for d in dsets:
            assert not self.query.find('Dataset', d.id.val)

    @pytest.mark.parametrize('number', [1, 2, 3])
    @pytest.mark.parametrize("ordered", ordered)
    def testMultipleSimpleObjectsTwoClassesInterlaced(self, number, ordered):
        projs = [self.make_project() for i in range(number)]
        dsets = [self.make_dataset() for i in range(number)]

        for i in range(number):
            self.args += ['Project:%s' % projs[i].id.val]
            self.args += ['Dataset:%s' % dsets[i].id.val]
        if ordered:
            self.args += ["--ordered"]
        self.cli.invoke(self.args, strict=True)

        # Check the objects have been deleted.
        for p in projs:
            assert not self.query.find('Project', p.id.val)
        for d in dsets:
            assert not self.query.find('Dataset', d.id.val)

    @pytest.mark.parametrize('form', ['/Dataset', ''])
    @pytest.mark.parametrize('number', [1, 2])
    @pytest.mark.parametrize("ordered", ordered)
    def testBasicSkipheadBothForms(self, form, number, ordered):
        proj = self.make_project()
        dset = self.make_dataset()
        imgs = [self.update.saveAndReturnObject(self.new_image())
                for i in range(number)]

        self.link(proj, dset)
        for i in imgs:
            self.link(dset, i)

        self.args += ['Project' + form + '/Image:%s' % proj.id.val]
        if ordered:
            self.args += ["--ordered"]
        self.cli.invoke(self.args, strict=True)

        # Check that only the images have been deleted.
        assert self.query.find('Project', proj.id.val)
        assert self.query.find('Dataset', dset.id.val)
        for i in imgs:
            assert not self.query.find('Image', i.id.val)

    @pytest.mark.parametrize('number', [1, 2, 3])
    @pytest.mark.parametrize("ordered", ordered)
    def testMultipleSkipheadsSameClass(self, number, ordered):
        projs = [self.make_project() for i in range(number)]
        dsets = [self.make_dataset() for i in range(number)]
        imgs = [self.update.saveAndReturnObject(self.new_image())
                for i in range(number)]

        for i in range(number):
            self.link(projs[i], dsets[i])
            self.link(dsets[i], imgs[i])

        for p in projs:
            self.args += ['Project/Dataset/Image:%s' % p.id.val]
        if ordered:
            self.args += ["--ordered"]
        self.cli.invoke(self.args, strict=True)

        # Check that only the images have been deleted.
        for p in projs:
            assert self.query.find('Project', p.id.val)
        for d in dsets:
            assert self.query.find('Dataset', d.id.val)
        for i in imgs:
            assert not self.query.find('Image', i.id.val)

    @pytest.mark.parametrize('number', [1, 2, 3])
    @pytest.mark.parametrize("ordered", ordered)
    def testMultipleSkipheadsPlusObjectsSeparated(self, number, ordered):
        projs = [self.make_project() for i in range(number)]
        dsets = [self.make_dataset() for i in range(number)]
        imgs = [self.update.saveAndReturnObject(self.new_image())
                for i in range(number)]

        for i in range(number):
            self.link(projs[i], dsets[i])
            self.link(dsets[i], imgs[i])

        ds = [self.make_dataset() for i in range(number)]

        for d in ds:
            self.args += ['Dataset:%s' % d.id.val]
        for p in projs:
            self.args += ['Project/Dataset/Image:%s' % p.id.val]
        if ordered:
            self.args += ["--ordered"]
        self.cli.invoke(self.args, strict=True)

        # Check that only the images and separate datasets have been deleted.
        for p in projs:
            assert self.query.find('Project', p.id.val)
        for d in dsets:
            assert self.query.find('Dataset', d.id.val)
        for i in imgs:
            assert not self.query.find('Image', i.id.val)
        for d in ds:
            assert not self.query.find('Dataset', d.id.val)

    @pytest.mark.parametrize('number', [1, 2, 3])
    @pytest.mark.parametrize("ordered", ordered)
    def testMultipleSkipheadsPlusObjectsInterlaced(self, number, ordered):
        projs = [self.make_project() for i in range(number)]
        dsets = [self.make_dataset() for i in range(number)]
        imgs = [self.update.saveAndReturnObject(self.new_image())
                for i in range(number)]

        for i in range(number):
            self.link(projs[i], dsets[i])
            self.link(dsets[i], imgs[i])

        ds = [self.make_dataset() for i in range(number)]

        for i in range(number):
            self.args += ['Project/Dataset/Image:%s' % projs[i].id.val]
            self.args += ['Dataset:%s' % ds[i].id.val]
        if ordered:
            self.args += ["--ordered"]
        self.cli.invoke(self.args, strict=True)

        # Check that only the images and separate datasets have been deleted.
        for p in projs:
            assert self.query.find('Project', p.id.val)
        for d in dsets:
            assert self.query.find('Dataset', d.id.val)
        for i in imgs:
            assert not self.query.find('Image', i.id.val)
        for d in ds:
            assert not self.query.find('Dataset', d.id.val)

    # Test dry-run option
    def testDryRun(self):
        img = self.update.saveAndReturnObject(self.new_image())

        self.args += ['Image:%s' % img.id.val]
        self.args += ['--dry-run']
        self.cli.invoke(self.args, strict=True)

        # Check that the image has not been deleted,
        assert self.query.find('Image', img.id.val)

    # These tests check the default exclusion of the annotations:
    # FileAnnotation, TagAnnotation and TermAnnotation

    def testDefaultExclusion(self):
        img = self.update.saveAndReturnObject(self.new_image())
        fa = self.make_file_annotation()
        tag = self.make_tag()

        self.link(img, fa)
        self.link(img, tag)

        self.args += ['Image:%s' % img.id.val]
        self.cli.invoke(self.args, strict=True)

        # Check that the image has been deleted,
        # but that both annotations have not been deleted.
        assert not self.query.find('Image', img.id.val)
        assert self.query.find('FileAnnotation', fa.id.val)
        assert self.query.find('TagAnnotation', tag.id.val)

    def testDefaultExclusionOverride(self):
        img = self.update.saveAndReturnObject(self.new_image())
        fa = self.make_file_annotation()
        tag = self.make_tag()

        self.link(img, fa)
        self.link(img, tag)

        self.args += ['Image:%s' % img.id.val]
        self.args += ['--include', 'Annotation']
        self.cli.invoke(self.args, strict=True)

        # Check that the image has been deleted,
        # and both annotations have been deleted.
        assert not self.query.find('Image', img.id.val)
        assert not self.query.find('FileAnnotation', fa.id.val)
        assert not self.query.find('TagAnnotation', tag.id.val)

    def testDefaultExclusionPartialOverride(self):
        img = self.update.saveAndReturnObject(self.new_image())
        fa = self.make_file_annotation()
        tag = self.make_tag()

        self.link(img, fa)
        self.link(img, tag)

        self.args += ['Image:%s' % img.id.val]
        self.args += ['--include', 'FileAnnotation']
        self.cli.invoke(self.args, strict=True)

        # Check that the image has been deleted,
        # and both annotations have been deleted.
        assert not self.query.find('Image', img.id.val)
        assert not self.query.find('FileAnnotation', fa.id.val)
        assert self.query.find('TagAnnotation', tag.id.val)

    def testSeparateAnnotationDelete(self):
        img = self.update.saveAndReturnObject(self.new_image())
        fa = self.make_file_annotation()
        fa2 = self.make_file_annotation()
        tag = self.make_tag()

        self.link(img, fa)
        self.link(img, tag)

        self.args += ['Image:%s' % img.id.val]
        self.args += ['Annotation:%s' % fa2.id.val]
        self.cli.invoke(self.args, strict=True)

        # Check that the image has been deleted and annotation, fa2,
        # but that both of the other annotations have not been deleted.
        assert not self.query.find('Image', img.id.val)
        assert self.query.find('FileAnnotation', fa.id.val)
        assert self.query.find('TagAnnotation', tag.id.val)
        assert not self.query.find('FileAnnotation', fa2.id.val)

    def testLinkedAnnotationDelete(self):
        img = self.update.saveAndReturnObject(self.new_image())
        fa = self.make_file_annotation()
        fa2 = self.make_file_annotation()

        self.link(img, fa)
        self.link(img, fa2)

        self.args += ['Image:%s' % img.id.val]
        self.args += ['Annotation:%s' % fa.id.val]
        self.cli.invoke(self.args, strict=True)

        # Check that the image has been deleted and annotation, fa,
        # but that the other annotation has not been deleted.
        assert not self.query.find('Image', img.id.val)
        assert not self.query.find('FileAnnotation', fa.id.val)
        assert self.query.find('FileAnnotation', fa2.id.val)

    def testLinkedAnnotationDeleteWithOverride(self):
        img = self.update.saveAndReturnObject(self.new_image())
        fa = self.make_file_annotation()
        fa2 = self.make_file_annotation()

        self.link(img, fa)
        self.link(img, fa2)

        self.args += ['Image:%s' % img.id.val]
        self.args += ['Annotation:%s' % fa.id.val]
        self.args += ['--include', 'FileAnnotation']
        self.cli.invoke(self.args, strict=True)

        # Check that the image and both annotations have been deleted.
        assert not self.query.find('Image', img.id.val)
        assert not self.query.find('FileAnnotation', fa.id.val)
        assert not self.query.find('FileAnnotation', fa2.id.val)
