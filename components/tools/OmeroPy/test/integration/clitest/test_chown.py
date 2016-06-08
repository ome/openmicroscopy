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
from test.integration.clitest.cli import CLITest, RootCLITest
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

    def testChownBasicUsageWithName(self):
        oid = self.create_object("Image")

        # create a user in the same group and transfer the object to the user
        client, user = self.new_client_and_user(group=self.group)
        self.args += ['%s' % (user.omeName.val),
                      '%s:%s' % ("Image", oid)]
        self.cli.invoke(self.args, strict=True)

        # check the object has been transferred
        obj = client.sf.getQueryService().get("Image", oid, all_grps)
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
        images = self.importMIF(nimages)
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
        images = self.importMIF(2)  # 2 images sharing a fileset

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
        images = self.importMIF(2)  # 2 images sharing a fileset
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
