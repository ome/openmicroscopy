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
from test.integration.clitest.test_tag import AbstractTagTest
import pytest

object_types = ["Image", "Dataset", "Project", "Plate", "Screen"]
model = ["", "I"]
ordered = [True, False]


class TestDelete(CLITest):

    def setup_method(self, method):
        super(TestDelete, self).setup_method(method)
        self.cli.register("delete", DeleteControl, "TEST")
        self.args += ["delete"]

    @pytest.mark.parametrize("model", model)
    @pytest.mark.parametrize("object_type", object_types)
    def testDeleteMyData(self, object_type, model):
        oid = self.create_object(object_type)

        # Delete the object
        self.args += ['/%s%s:%s' % (object_type, model, oid)]
        self.cli.invoke(self.args, strict=True)

        # Check the object has been deleted
        assert not self.query.find(object_type, oid)

    @pytest.mark.parametrize('nimages', [1, 2])
    @pytest.mark.parametrize('arguments', ['image', 'fileset'])
    def testFileset(self, nimages, arguments):
        # 2 images sharing a fileset
        images = self.import_mif(nimages)
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
        images = self.import_mif(2)  # 2 images sharing a fileset

        # try to delete only one image
        self.args += ['/Image:%s' % images[0].id.val]
        self.cli.invoke(self.args, strict=True)

        # Check the images have not been deleted
        for i in images:
            assert self.query.get('Image', i.id.val) is not None

    def testFilesetAllImagesDeleteDataset(self):
        images = self.import_mif(2)  # 2 images sharing a fileset
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

    # Test combinations of include and exclude other than annotations
    def testExcludeNone(self, simpleHierarchy):
        proj, dset, img = simpleHierarchy

        self.args += ['Project:%s' % proj.id.val]
        self.cli.invoke(self.args, strict=True)

        # Check that everything has been deleted.
        assert not self.query.find('Project', proj.id.val)
        assert not self.query.find('Dataset', dset.id.val)
        assert not self.query.find('Image', img.id.val)

    def testExcludeDataset(self, simpleHierarchy):
        proj, dset, img = simpleHierarchy

        self.args += ['Project:%s' % proj.id.val]
        self.args += ['--exclude', 'Dataset']
        self.cli.invoke(self.args, strict=True)

        # Check that only the Project has been deleted.
        assert not self.query.find('Project', proj.id.val)
        assert self.query.find('Dataset', dset.id.val)
        assert self.query.find('Image', img.id.val)

    def testExcludeImage(self, simpleHierarchy):
        proj, dset, img = simpleHierarchy

        self.args += ['Project:%s' % proj.id.val]
        self.args += ['--exclude', 'Image']
        self.cli.invoke(self.args, strict=True)

        # Check that only Project & Dataset have been deleted.
        assert not self.query.find('Project', proj.id.val)
        assert not self.query.find('Dataset', dset.id.val)
        assert self.query.find('Image', img.id.val)

    def testExcludeOverridesInclude(self, simpleHierarchy):
        proj, dset, img = simpleHierarchy

        self.args += ['Project:%s' % proj.id.val]
        self.args += ['--exclude', 'Dataset']
        self.args += ['--include', 'Image']
        self.cli.invoke(self.args, strict=True)

        # Check that only the Project has been deleted.
        assert not self.query.find('Project', proj.id.val)
        assert self.query.find('Dataset', dset.id.val)
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

    @pytest.mark.parametrize('number', [1, 2, 3])
    def testInputWithElisionDefault(self, number, capfd):
        # Create several datasets
        ids = []
        for i in range(number):
            ids.append(self.make_dataset().id.val)
        ids = sorted(ids)
        assert len(ids) == number
        assert ids[-1] - ids[0] + 1 == number
        # Try to delete the datasets, defaults to --dry-run for elision
        self.args += ['Dataset:%s' % str(ids[0]) + "-" + str(ids[number-1])]
        self.cli.invoke(self.args, strict=True)
        # Check that the Datasets were not deleted
        for did in ids:
            assert self.query.find('Dataset', did)

    def testInputWithElisionForce(self, capfd):
        DATASETS = 3

        # Create several datasets
        ids = []
        for i in range(DATASETS):
            ids.append(self.make_dataset().id.val)
        ids = sorted(ids)
        assert len(ids) == DATASETS
        assert ids[-1] - ids[0] + 1 == DATASETS

        # Delete the datasets using --force flag
        self.args += ['Dataset:%s' % str(ids[0]) + "-" + str(ids[2])]
        self.args += ['--force']
        self.cli.invoke(self.args, strict=True)

        # Check that the Datasets were deleted
        for did in ids:
            assert not self.query.find('Dataset', did)

    def testElisionDefaultFailForce(self):
        DATASETS = 3
        ids = []

        # Create a Dataset as me
        dat_my1 = self.make_dataset()
        ids.append(dat_my1.id.val)
        # Create a Dataset as another user
        client, user = self.new_client_and_user(group=self.group)
        dat2 = self.make_dataset(client=client)
        ids.append(dat2.id.val)
        # Create another Dataset as me
        dat_my3 = self.make_dataset()
        ids.append(dat_my3.id.val)
        ids = sorted(ids)
        assert len(ids) == DATASETS
        assert ids[-1] - ids[0] + 1 == DATASETS
        # Try to delete the Datasets with --force
        self.args += ['Dataset:%s' % str(ids[0]) + "-" + str(ids[2])]
        self.args += ['--force']
        self.cli.invoke(self.args, strict=True)

        # Check that the Datasets were not deleted
        # because the user does not have permission to delete the dat2
        assert self.query.find('Dataset', ids[0])
        assert client.sf.getQueryService().find("Dataset", ids[1])
        assert self.query.find('Dataset', ids[2])

    def testOutputWithElision(self, capfd):
        IMAGES = 8
        # Import several images
        ids = []
        for i in range(IMAGES):
            ids.append(self.import_single_image().getId().getValue())
        ids = sorted(ids)
        assert len(ids) == IMAGES
        assert ids[-1] - ids[0] + 1 == IMAGES
        ids = [str(id) for id in ids]
        # Now delete some of those images, mix up the order
        iids = [ids[5], ids[4], ids[0], ids[7], ids[2], ids[1]]
        self.args += ['Image:%s' % ",".join(iids)]
        self.cli.invoke(self.args, strict=True)
        o, e = capfd.readouterr()
        o = o.strip().split(" ")
        # The output should contain:...
        # ... the command and an ok at either end,...
        assert o[0] == "omero.cmd.Delete2"
        assert o[2] == "ok"
        type, oids = o[1].split(":")
        # ... the object type,...
        assert type == "Image"
        oids = oids.split(",")
        # ... the first three sequential ids elided,...
        assert oids[0] == ids[0]+"-"+ids[2]
        # ... the next two sequential ids, not elided,...
        assert oids[1] == ids[4]
        assert oids[2] == ids[5]
        # ... and the final separate single id.
        assert oids[3] == ids[7]


class TestTagDelete(AbstractTagTest):

    def setup_method(self, method):
        super(AbstractTagTest, self).setup_method(method)
        self.cli.register("delete", DeleteControl, "TEST")
        self.args += ["delete"]
        # Create two tags sets with two tags each, one in common
        tag_name = self.uuid()
        self.tag_ids = self.create_tags(3, tag_name)
        self.ts1_id = self.create_tagset(self.tag_ids[:2], tag_name)
        self.ts2_id = self.create_tagset(self.tag_ids[1:], tag_name)

    def teardown_method(self, method):
        pass

    def testDeleteOneTagSetNotTags(self):
        # try to delete one tag set
        self.args += ['TagAnnotation:%s' % self.ts1_id]
        self.args += ['--report']
        self.cli.invoke(self.args, strict=True)

        assert not self.query.find('TagAnnotation', self.ts1_id)
        assert self.query.find('TagAnnotation', self.tag_ids[0])
        assert self.query.find('TagAnnotation', self.ts2_id)
        assert self.query.find('TagAnnotation', self.tag_ids[1])
        assert self.query.find('TagAnnotation', self.tag_ids[2])

    def testDeleteTwoTagSetsNotTags(self):
        # try to delete both tag sets
        self.args += ['TagAnnotation:%s,%s' % (self.ts1_id, self.ts2_id)]
        self.args += ['--report']
        self.cli.invoke(self.args, strict=True)

        assert not self.query.find('TagAnnotation', self.ts1_id)
        assert self.query.find('TagAnnotation', self.tag_ids[0])
        assert not self.query.find('TagAnnotation', self.ts2_id)
        assert self.query.find('TagAnnotation', self.tag_ids[1])
        assert self.query.find('TagAnnotation', self.tag_ids[2])

    def testDeleteOneTagSetIncludingTags(self):
        # try to delete one tag set with tags
        self.args += ['TagAnnotation:%s' % self.ts1_id]
        self.args += ['--include', 'TagAnnotation']
        self.args += ['--report']
        self.cli.invoke(self.args, strict=True)

        assert not self.query.find('TagAnnotation', self.ts1_id)
        assert not self.query.find('TagAnnotation', self.tag_ids[0])
        assert self.query.find('TagAnnotation', self.ts2_id)
        assert not self.query.find('TagAnnotation', self.tag_ids[1])
        assert self.query.find('TagAnnotation', self.tag_ids[2])

    def testDeleteTwoTagSetsIncludingTags(self):
        # try to delete both tag sets with tags
        self.args += ['TagAnnotation:%s,%s' % (self.ts1_id, self.ts2_id)]
        self.args += ['--include', 'TagAnnotation']
        self.args += ['--report']
        self.cli.invoke(self.args, strict=True)

        assert not self.query.find('TagAnnotation', self.ts1_id)
        assert not self.query.find('TagAnnotation', self.tag_ids[0])
        assert not self.query.find('TagAnnotation', self.ts2_id)
        assert not self.query.find('TagAnnotation', self.tag_ids[1])
        assert not self.query.find('TagAnnotation', self.tag_ids[2])
