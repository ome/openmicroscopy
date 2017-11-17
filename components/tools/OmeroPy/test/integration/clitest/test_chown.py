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

# from omero.cli import NonZeroReturnCode

import omero
from omero.plugins.chown import ChownControl
from omero.testlib.cli import CLITest, RootCLITest
from test.integration.clitest.test_tag import AbstractTagTest
import pytest

object_types = ["Image", "Dataset", "Project", "Plate", "Screen"]
user_prefixes = ["", "User:", "Experimenter:"]
all_grps = {'omero.group': '-1'}
ordered = [True, False]


class TestChown(CLITest):

    DEFAULT_GROUP_OWNER = True

    def setup_method(self, method):
        super(TestChown, self).setup_method(method)
        self.cli.register("chown", ChownControl, "TEST")
        self.args += ["chown"]

    @pytest.mark.parametrize("object_type", object_types)
    @pytest.mark.parametrize("user_prefix", user_prefixes)
    def testChownBasicUsageWithId(self, object_type, user_prefix):
        oid = self.create_object(object_type)

        # create a user in the same group and transfer the object to the user
        client, user = self.new_client_and_user(group=self.group)
        self.args += ['%s%s' % (user_prefix, user.id.val),
                      '%s:%s' % (object_type, oid)]
        self.cli.invoke(self.args, strict=True)

        # check the object has been transferred
        obj = client.sf.getQueryService().get(object_type, oid, all_grps)
        assert obj.id.val == oid
        assert obj.details.owner.id.val == user.id.val

    def testChownBasicUsageTargetUser(self, simpleHierarchy):
        proj, dset, img = simpleHierarchy
        argument = "Experimenter"

        # create a new user in the same group
        # and transfer all owned by self.user to the new user
        client1, user1 = self.new_client_and_user(group=self.group)
        args_user = self.args + ['%s' % (user1.omeName.val),
                                 '%s:%s' % (argument, self.user.id.val)]
        self.cli.invoke(args_user, strict=True)

        # check that all objects have been transferred
        obj = self.query.get('Project', proj.id.val, all_grps)
        assert obj.id.val == proj.id.val
        assert obj.details.owner.id.val == user1.id.val
        obj = self.query.get('Dataset', dset.id.val, all_grps)
        assert obj.id.val == dset.id.val
        assert obj.details.owner.id.val == user1.id.val
        obj = self.query.get('Image', img.id.val, all_grps)
        assert obj.id.val == img.id.val
        assert obj.details.owner.id.val == user1.id.val

        # create dataset
        object_name = "Dataset"
        oid = self.create_object(object_name)

        # now create a second user (user2) in the same group
        # and transfer all owned by user1 and
        # the dataset (owned by self.user) to user2 in one step
        client2, user2 = self.new_client_and_user(group=self.group)
        self.args += ['%s' % (user2.omeName.val),
                      '%s:%s' % (object_name, oid),
                      '%s:%s' % (argument, user1.id.val)]
        self.cli.invoke(self.args, strict=True)

        # check that all objects of user1 have been transferred
        obj = self.query.get('Project', proj.id.val, all_grps)
        assert obj.id.val == proj.id.val
        assert obj.details.owner.id.val == user2.id.val
        obj = self.query.get('Dataset', dset.id.val, all_grps)
        assert obj.id.val == dset.id.val
        assert obj.details.owner.id.val == user2.id.val
        obj = self.query.get('Image', img.id.val, all_grps)
        assert obj.id.val == img.id.val
        assert obj.details.owner.id.val == user2.id.val
        # check that the separate dataset transferred too
        obj = self.query.get('Dataset', oid, all_grps)
        assert obj.id.val == oid
        assert obj.details.owner.id.val == user2.id.val

    @pytest.mark.parametrize("object_prefix", ["", "/"])
    @pytest.mark.parametrize("object_suffix", ["", "I"])
    def testChownBasicUsageWithName(self, object_prefix, object_suffix):
        object_name = "Dataset"
        oid = self.create_object(object_name)
        argument = object_prefix + object_name + object_suffix

        # create a user in the same group and transfer the object to the user
        client, user = self.new_client_and_user(group=self.group)
        self.args += ['%s' % (user.omeName.val), '%s:%s' % (argument, oid)]
        self.cli.invoke(self.args, strict=True)

        # check the object has been transferred
        obj = client.sf.getQueryService().get(object_name, oid, all_grps)
        assert obj.id.val == oid
        assert obj.details.owner.id.val == user.id.val

    def testChownDifferentGroup(self):
        oid = self.create_object("Image")

        # create user and try to transfer the object to the user
        client, user = self.new_client_and_user()
        self.args += ['%s' % user.id.val,
                      '%s:%s' % ("Image", oid)]
        self.cli.invoke(self.args, strict=True)

        # check the object still belongs to the original user
        obj = self.query.get("Image", oid, all_grps)
        assert obj.id.val == oid
        assert obj.details.owner.id.val == self.user.id.val

    @pytest.mark.parametrize('nimages', [1, 2])
    @pytest.mark.parametrize('arguments', ['image', 'fileset'])
    def testFileset(self, nimages, arguments):
        # 2 images sharing a fileset
        images = self.import_fake_file(nimages)
        img = self.query.get('Image', images[0].id.val)
        filesetId = img.fileset.id.val
        fileset = self.query.get('Fileset', filesetId)
        assert fileset is not None

        # create user and transfer the object to the user
        client, user = self.new_client_and_user(group=self.group)
        self.args += ['%s' % user.id.val]
        if arguments == 'fileset':
            self.args += ['%s:%s' % ('Fileset', filesetId)]
        else:
            ids = [str(i.id.val) for i in images]
            self.args += ['Image:' + ",".join(ids)]
        self.cli.invoke(self.args, strict=True)

        # Check the fileset and images have been chowned
        obj = self.query.get("Fileset", filesetId, all_grps)
        assert obj.id.val == filesetId
        assert obj.details.owner.id.val == user.id.val
        for i in images:
            obj = self.query.get('Image', i.id.val, all_grps)
            assert obj.id.val == i.id.val
            assert obj.details.owner.id.val == user.id.val

    def testFilesetPartialFailing(self):
        images = self.import_fake_file(2)  # 2 images sharing a fileset

        # Create user and try to transfer only one image to the user
        client, user = self.new_client_and_user(group=self.group)
        self.args += ['%s' % user.id.val]
        self.args += ['Image:%s' % images[0].id.val]
        self.cli.invoke(self.args, strict=True)

        # Check the images have not been transferred
        for i in images:
            obj = self.query.get('Image', i.id.val, all_grps)
            assert obj.id.val == i.id.val
            assert obj.details.owner.id.val == self.user.id.val

    def testFilesetAllImagesChownDataset(self):
        images = self.import_fake_file(2)  # 2 images sharing a fileset
        dataset_id = self.create_object('Dataset')  # ... in a dataset

        # put the images into the dataset
        for image in images:
            link = omero.model.DatasetImageLinkI()
            link.parent = omero.model.DatasetI(dataset_id, False)
            link.child = omero.model.ImageI(image.id.val, False)
            self.update.saveObject(link)

        # Create user and transfer the dataset to the user
        client, user = self.new_client_and_user(group=self.group)
        self.args += ['%s' % user.id.val]
        self.args += ['Dataset:%s' % dataset_id]
        self.cli.invoke(self.args, strict=True)

        # check the dataset has been transferred
        obj = self.query.get('Dataset', dataset_id, all_grps)
        assert obj.id.val == dataset_id
        assert obj.details.owner.id.val == user.id.val

        # check the images have been transferred
        for i in images:
            obj = self.query.get('Image', image.id.val, all_grps)
            assert obj.id.val == image.id.val
            assert obj.details.owner.id.val == user.id.val

    # These tests try to exercise the various grouping possibilities
    # when passing multiple objects on the command line. In all of these
    # cases using the --ordered flag should make no difference

    @pytest.mark.parametrize('number', [1, 2, 3])
    @pytest.mark.parametrize("ordered", ordered)
    def testMultipleSimpleObjectsSameClass(self, number, ordered):
        dsets = [self.make_dataset() for i in range(number)]

        # Create user and transfer the objects to the user
        client, user = self.new_client_and_user(group=self.group)
        self.args += ['%s' % user.id.val]
        for d in dsets:
            self.args += ['Dataset:%s' % d.id.val]
        if ordered:
            self.args += ["--ordered"]
        self.cli.invoke(self.args, strict=True)

        # Check the objects have been transferred
        for d in dsets:
            obj = self.query.get('Dataset', d.id.val, all_grps)
            assert obj.id.val == d.id.val
            assert obj.details.owner.id.val == user.id.val

    @pytest.mark.parametrize('number', [1, 2, 3])
    @pytest.mark.parametrize("ordered", ordered)
    def testMultipleSimpleObjectsTwoClassesSeparated(self, number, ordered):
        projs = [self.make_project() for i in range(number)]
        dsets = [self.make_dataset() for i in range(number)]

        # Create user and transfer the objects to the user
        client, user = self.new_client_and_user(group=self.group)
        self.args += ['%s' % user.id.val]
        for p in projs:
            self.args += ['Project:%s' % p.id.val]
        for d in dsets:
            self.args += ['Dataset:%s' % d.id.val]
        if ordered:
            self.args += ["--ordered"]
        self.cli.invoke(self.args, strict=True)

        # Check the objects have been transferred
        for p in projs:
            obj = self.query.get('Project', p.id.val, all_grps)
            assert obj.id.val == p.id.val
            assert obj.details.owner.id.val == user.id.val
        for d in dsets:
            obj = self.query.get('Dataset', d.id.val, all_grps)
            assert obj.id.val == d.id.val
            assert obj.details.owner.id.val == user.id.val

    @pytest.mark.parametrize('number', [1, 2, 3])
    @pytest.mark.parametrize("ordered", ordered)
    def testMultipleSimpleObjectsTwoClassesInterlaced(self, number, ordered):
        projs = [self.make_project() for i in range(number)]
        dsets = [self.make_dataset() for i in range(number)]

        # Create user and transfer the objects to the user
        client, user = self.new_client_and_user(group=self.group)
        self.args += ['%s' % user.id.val]
        for i in range(number):
            self.args += ['Project:%s' % projs[i].id.val]
            self.args += ['Dataset:%s' % dsets[i].id.val]
        if ordered:
            self.args += ["--ordered"]
        self.cli.invoke(self.args, strict=True)

        # Check the objects have been transferred
        for p in projs:
            obj = self.query.get('Project', p.id.val, all_grps)
            assert obj.id.val == p.id.val
            assert obj.details.owner.id.val == user.id.val
        for d in dsets:
            obj = self.query.get('Dataset', d.id.val, all_grps)
            assert obj.id.val == d.id.val
            assert obj.details.owner.id.val == user.id.val

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

        # Create user and transfer the objects to the user
        client, user = self.new_client_and_user(group=self.group)
        self.args += ['%s' % user.id.val]
        self.args += ['Project' + form + '/Image:%s' % proj.id.val]
        if ordered:
            self.args += ["--ordered"]
        self.cli.invoke(self.args, strict=True)

        # Check that only the images have been transferred
        # the Project and Dataset still belongs to the original user
        obj = self.query.get('Project', proj.id.val, all_grps)
        assert obj.id.val == proj.id.val
        assert obj.details.owner.id.val == self.user.id.val
        obj = self.query.get('Dataset', dset.id.val, all_grps)
        assert obj.id.val == dset.id.val
        assert obj.details.owner.id.val == self.user.id.val
        for i in imgs:
            obj = self.query.get('Image', i.id.val, all_grps)
            assert obj.id.val == i.id.val
            assert obj.details.owner.id.val == user.id.val

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

        # Create user and transfer the objects to the user
        client, user = self.new_client_and_user(group=self.group)
        self.args += ['%s' % user.id.val]
        for d in ds:
            self.args += ['Dataset:%s' % d.id.val]
        for p in projs:
            self.args += ['Project/Dataset/Image:%s' % p.id.val]
        if ordered:
            self.args += ["--ordered"]
        self.cli.invoke(self.args, strict=True)

        # Check that only the images and separate datasets
        # have been transferred
        for p in projs:
            obj = self.query.get('Project', p.id.val, all_grps)
            assert obj.id.val == p.id.val
            assert obj.details.owner.id.val == self.user.id.val
        for d in dsets:
            obj = self.query.get('Dataset', d.id.val, all_grps)
            assert obj.id.val == d.id.val
            assert obj.details.owner.id.val == self.user.id.val
        for i in imgs:
            obj = self.query.get('Image', i.id.val, all_grps)
            assert obj.id.val == i.id.val
            assert obj.details.owner.id.val == user.id.val
        for d in ds:
            obj = self.query.get('Dataset', d.id.val, all_grps)
            assert obj.id.val == d.id.val
            assert obj.details.owner.id.val == user.id.val

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

        # Create user and transfer the objects to the user
        client, user = self.new_client_and_user(group=self.group)
        self.args += ['%s' % user.id.val]
        for i in range(number):
            self.args += ['Project/Dataset/Image:%s' % projs[i].id.val]
            self.args += ['Dataset:%s' % ds[i].id.val]
        if ordered:
            self.args += ["--ordered"]
        self.cli.invoke(self.args, strict=True)

        # Check that only the images and separate datasets
        # have been transferred
        for p in projs:
            obj = self.query.get('Project', p.id.val, all_grps)
            assert obj.id.val == p.id.val
            assert obj.details.owner.id.val == self.user.id.val
        for d in dsets:
            obj = self.query.get('Dataset', d.id.val, all_grps)
            assert obj.id.val == d.id.val
            assert obj.details.owner.id.val == self.user.id.val
        for i in imgs:
            obj = self.query.get('Image', i.id.val, all_grps)
            assert obj.id.val == i.id.val
            assert obj.details.owner.id.val == user.id.val
        for d in ds:
            obj = self.query.get('Dataset', d.id.val, all_grps)
            assert obj.id.val == d.id.val
            assert obj.details.owner.id.val == user.id.val

    # Test dry-run option
    def testDryRun(self):
        img = self.update.saveAndReturnObject(self.new_image())

        # Create user and try to transfer image to the user
        client, user = self.new_client_and_user(group=self.group)
        self.args += ['%s' % user.id.val]
        self.args += ['Image:%s' % img.id.val]
        self.args += ['--dry-run']
        self.cli.invoke(self.args, strict=True)

        # Check the image has not been transferred
        assert self.query.find('Image', img.id.val)
        obj = self.query.get('Image', img.id.val, all_grps)
        assert obj.id.val == img.id.val
        assert obj.details.owner.id.val == self.user.id.val

    # Test combinations of include and exclude other than annotations
    def testExcludeNone(self, simpleHierarchy):
        proj, dset, img = simpleHierarchy

        # Create user and transfer objects to the user
        client, user = self.new_client_and_user(group=self.group)
        self.args += ['%s' % user.id.val]
        self.args += ['Project:%s' % proj.id.val]
        self.cli.invoke(self.args, strict=True)

        # Check that all objects have been transferred
        obj = self.query.get('Project', proj.id.val, all_grps)
        assert obj.id.val == proj.id.val
        assert obj.details.owner.id.val == user.id.val
        obj = self.query.get('Dataset', dset.id.val, all_grps)
        assert obj.id.val == dset.id.val
        assert obj.details.owner.id.val == user.id.val
        obj = self.query.get('Image', img.id.val, all_grps)
        assert obj.id.val == img.id.val
        assert obj.details.owner.id.val == user.id.val

    def testExcludeDataset(self, simpleHierarchy):
        proj, dset, img = simpleHierarchy

        # Create user and transfer objects to the user
        client, user = self.new_client_and_user(group=self.group)
        self.args += ['%s' % user.id.val]
        self.args += ['Project:%s' % proj.id.val]
        self.args += ['--exclude', 'Dataset']
        self.cli.invoke(self.args, strict=True)

        # Check that only the Project has been transferred
        obj = self.query.get('Project', proj.id.val, all_grps)
        assert obj.id.val == proj.id.val
        assert obj.details.owner.id.val == user.id.val
        obj = self.query.get('Dataset', dset.id.val, all_grps)
        assert obj.id.val == dset.id.val
        assert obj.details.owner.id.val == self.user.id.val
        obj = self.query.get('Image', img.id.val, all_grps)
        assert obj.id.val == img.id.val
        assert obj.details.owner.id.val == self.user.id.val

    def testExcludeImage(self, simpleHierarchy):
        proj, dset, img = simpleHierarchy

        # Create user and transfer objects to the user
        client, user = self.new_client_and_user(group=self.group)
        self.args += ['%s' % user.id.val]
        self.args += ['Project:%s' % proj.id.val]
        self.args += ['--exclude', 'Image']
        self.cli.invoke(self.args, strict=True)

        # Check that only Project & Dataset have been transferred
        obj = self.query.get('Project', proj.id.val, all_grps)
        assert obj.id.val == proj.id.val
        assert obj.details.owner.id.val == user.id.val
        obj = self.query.get('Dataset', dset.id.val, all_grps)
        assert obj.id.val == dset.id.val
        assert obj.details.owner.id.val == user.id.val
        obj = self.query.get('Image', img.id.val, all_grps)
        assert obj.id.val == img.id.val
        assert obj.details.owner.id.val == self.user.id.val

    def testExcludeOverridesInclude(self, simpleHierarchy):
        proj, dset, img = simpleHierarchy

        # Create user and transfer objects to the user
        client, user = self.new_client_and_user(group=self.group)
        self.args += ['%s' % user.id.val]
        self.args += ['Project:%s' % proj.id.val]
        self.args += ['--exclude', 'Dataset']
        self.args += ['--include', 'Image']
        self.cli.invoke(self.args, strict=True)

        # Check that only the Project has been transferred
        obj = self.query.get('Project', proj.id.val, all_grps)
        assert obj.id.val == proj.id.val
        assert obj.details.owner.id.val == user.id.val
        obj = self.query.get('Dataset', dset.id.val, all_grps)
        assert obj.id.val == dset.id.val
        assert obj.details.owner.id.val == self.user.id.val
        obj = self.query.get('Image', img.id.val, all_grps)
        assert obj.id.val == img.id.val
        assert obj.details.owner.id.val == self.user.id.val

    def testSeparateAnnotationTransfer(self):
        img = self.update.saveAndReturnObject(self.new_image())
        fa = self.make_file_annotation()
        fa2 = self.make_file_annotation()
        tag = self.make_tag()

        self.link(img, fa)
        self.link(img, tag)

        # Create user and transfer objects to the user
        client, user = self.new_client_and_user(group=self.group)
        self.args += ['%s' % user.id.val]
        self.args += ['Image:%s' % img.id.val]
        self.args += ['Annotation:%s' % fa2.id.val]
        self.cli.invoke(self.args, strict=True)

        # Check that the image and and all annotations
        # have been transferred, both linked and not linked
        obj = self.query.get('Image', img.id.val, all_grps)
        assert obj.id.val == img.id.val
        assert obj.details.owner.id.val == user.id.val
        obj = self.query.get('FileAnnotation', fa.id.val, all_grps)
        assert obj.id.val == fa.id.val
        assert obj.details.owner.id.val == user.id.val
        obj = self.query.get('TagAnnotation', tag.id.val, all_grps)
        assert obj.id.val == tag.id.val
        assert obj.details.owner.id.val == user.id.val
        obj = self.query.get('FileAnnotation', fa2.id.val, all_grps)
        assert obj.id.val == fa2.id.val
        assert obj.details.owner.id.val == user.id.val

    def testOutputWithElision(self, capfd):
        DATASETS = 8
        # Create several datasets
        ids = []
        for i in range(DATASETS):
            ids.append(self.make_dataset().id.val)
        ids = sorted(ids)
        assert len(ids) == DATASETS
        assert ids[-1] - ids[0] + 1 == DATASETS
        ids = [str(id) for id in ids]

        # Create user and transfer some of those datasets
        # to the user, mix up the order
        client, user = self.new_client_and_user(group=self.group)
        self.args += ['%s' % user.id.val]
        iids = [ids[5], ids[4], ids[0], ids[7], ids[2], ids[1]]
        self.args += ['Dataset:%s' % ",".join(iids)]
        self.cli.invoke(self.args, strict=True)
        o, e = capfd.readouterr()
        o = o.strip().split(" ")
        # The output should contain:...
        # ... the command and an ok at either end,...
        assert o[0] == "omero.cmd.Chown2"
        assert o[2] == "ok"
        type, oids = o[1].split(":")
        # ... the object type,...
        assert type == "Dataset"
        oids = oids.split(",")
        # ... the first three sequential ids elided,...
        assert oids[0] == ids[0]+"-"+ids[2]
        # ... the next two sequential ids, not elided,...
        assert oids[1] == ids[4]
        assert oids[2] == ids[5]
        # ... and the final separate single id.
        assert oids[3] == ids[7]


class TestTagChown(AbstractTagTest):

    DEFAULT_GROUP_OWNER = True

    def setup_method(self, method):
        super(AbstractTagTest, self).setup_method(method)
        self.cli.register("chown", ChownControl, "TEST")
        self.args += ["chown"]
        # Create two tags sets with two tags each, one in common
        tag_name = self.uuid()
        self.tag_ids = self.create_tags(3, tag_name)
        self.ts1_id = self.create_tagset(self.tag_ids[:2], tag_name)
        self.ts2_id = self.create_tagset(self.tag_ids[1:], tag_name)

    def teardown_method(self, method):
        pass

    def testChownOneTagSetWithUniqueTags(self):
        # Create user and transfer one tagset to the user
        client, user = self.new_client_and_user(group=self.group)
        self.args += ['%s' % user.id.val]
        self.args += ['TagAnnotation:%s' % self.ts1_id]
        self.args += ['--report']
        self.cli.invoke(self.args, strict=True)

        # Check the tagset and the tag unique to the tagset
        # transfer. Check that the tag shared with the other
        # tagset and the other tagset with its unique tag
        # still belong to original owner.
        obj = self.query.get('TagAnnotation', self.ts1_id, all_grps)
        assert obj.id.val == self.ts1_id
        assert obj.details.owner.id.val == user.id.val
        obj = self.query.get('TagAnnotation', self.tag_ids[0], all_grps)
        assert obj.id.val == self.tag_ids[0]
        assert obj.details.owner.id.val == user.id.val
        obj = self.query.get('TagAnnotation', self.tag_ids[1], all_grps)
        assert obj.id.val == self.tag_ids[1]
        assert obj.details.owner.id.val == self.user.id.val
        obj = self.query.get('TagAnnotation', self.tag_ids[2], all_grps)
        assert obj.id.val == self.tag_ids[2]
        assert obj.details.owner.id.val == self.user.id.val
        obj = self.query.get('TagAnnotation', self.ts2_id, all_grps)
        assert obj.id.val == self.ts2_id
        assert obj.details.owner.id.val == self.user.id.val


class TestChownRoot(RootCLITest):

    def setup_method(self, method):
        super(TestChownRoot, self).setup_method(method)
        self.cli.register("chown", ChownControl, "TEST")
        self.args += ["chown"]

    def testChownBasicUsageWithId(self):
        new_image = self.new_image()
        new_image = self.update.saveAndReturnObject(new_image)
        iid = new_image.id.val

        # create a new group which the root is not a member of
        # and a user in the same group and transfer the image to the user
        group = self.new_group()
        client, user = self.new_client_and_user(group=group)
        self.args += ['%s%s' % ("User:", user.id.val),
                      '%s:%s' % ("Image", new_image.id.val)]
        self.cli.invoke(self.args, strict=True)

        # check the object has been transferred
        obj = client.sf.getQueryService().get("Image", iid, all_grps)
        assert obj.id.val == new_image.id.val
        assert obj.details.owner.id.val == user.id.val


class TestChownNonGroupOwner(CLITest):

    def setup_method(self, method):
        super(TestChownNonGroupOwner, self).setup_method(method)
        self.cli.register("chown", ChownControl, "TEST")
        self.args += ["chown"]

    def testChownBasicUsageWithId(self):
        new_dataset = self.new_dataset()
        new_dataset = self.update.saveAndReturnObject(new_dataset)
        iid = new_dataset.id.val

        # create a user in the same group and
        # attempt to transfer image to the user
        client, user = self.new_client_and_user(group=self.group)
        self.args += ['%s%s' % ("User:", user.id.val),
                      '%s:%s' % ("Dataset", new_dataset.id.val)]
        self.cli.invoke(self.args, strict=True)

        # check the object still belongs to the original user
        obj = self.query.get("Dataset", iid, all_grps)
        assert obj.id.val == iid
        assert obj.details.owner.id.val == self.user.id.val
