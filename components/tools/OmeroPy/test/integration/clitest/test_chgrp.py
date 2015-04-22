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

import omero
from omero.cli import NonZeroReturnCode
from omero.plugins.chgrp import ChgrpControl
from test.integration.clitest.cli import CLITest, RootCLITest
from omero.rtypes import rstring
import pytest

object_types = ["Image", "Dataset", "Project", "Plate", "Screen"]
permissions = ["rw----", "rwr---", "rwra--", "rwrw--"]
group_prefixes = ["", "Group:", "ExperimenterGroup:"]
ordered = [True, False]
all_grps = {'omero.group': '-1'}


class TestChgrp(CLITest):

    @classmethod
    def setup_class(cls):
        super(TestChgrp, cls).setup_class()
        exp = cls.sf.getAdminService().getExperimenter(cls.user.id.val)
        cls.target_groups = {}
        for p in permissions:
            cls.target_groups[p] = cls.new_group([exp], p)

    def setup_method(self, method):
        super(TestChgrp, self).setup_method(method)
        self.cli.register("chgrp", ChgrpControl, "TEST")
        self.args += ["chgrp"]

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
    @pytest.mark.parametrize("target_group_perms", permissions)
    @pytest.mark.parametrize("group_prefix", group_prefixes)
    def testChgrpMyData(self, object_type, target_group_perms, group_prefix):
        oid = self.create_object(object_type)

        # create a new group and move the object to the new group
        target_group = self.target_groups[target_group_perms]
        self.args += ['%s%s' % (group_prefix, target_group.id.val),
                      '/%s:%s' % (object_type, oid)]
        self.cli.invoke(self.args, strict=True)

        # check the object has been moved
        new_object = self.query.get(object_type, oid,
                                    {'omero.group': str(target_group.id.val)})
        assert new_object.id.val == oid

    def testNonMember(self):
        iid = self.create_object("Image")

        # create a new group which the current user is not member of
        group = self.new_group()

        # try to move the image to the new group
        self.args += ['%s' % group.id.val, '/Image:%s' % iid]
        with pytest.raises(NonZeroReturnCode):
            self.cli.invoke(self.args, strict=True)

    def testGroupName(self):
        iid = self.create_object("Image")

        # try to move the image to the new group
        target_group = self.target_groups['rw----']
        self.args += ['%s' % target_group.name.val, '/Image:%s' % iid]
        self.cli.invoke(self.args, strict=True)

        # check the image has been moved
        img = self.query.get("Image", iid, {'omero.group': '-1'})
        assert img.id.val == iid

    def testFileset(self):
        # 2 images sharing a fileset
        images = self.importMIF(2)
        img = self.query.get('Image', images[0].id.val)
        filesetId = img.fileset.id.val
        fileset = self.query.get('Fileset', filesetId)
        assert fileset is not None

        # move the fileset to the new group
        target_group = self.target_groups['rw----']
        self.args += ['%s' % target_group.id.val, '/Fileset:%s' % filesetId]
        self.cli.invoke(self.args, strict=True)

        # check the image has been moved
        ctx = {'omero.group': '-1'}  # query across groups
        for i in images:
            img = self.query.get('Image', i.id.val, ctx)
            assert img.details.group.id.val == target_group.id.val

    def testFilesetPartialFailing(self):
        images = self.importMIF(2)  # 2 images sharing a fileset

        # try to move only one image to the new group
        target_group = self.target_groups['rw----']
        self.args += ['%s' % target_group.id.val,
                      '/Image:%s' % images[0].id.val]
        self.cli.invoke(self.args, strict=True)

        # check the images are still in the current group
        gid = self.sf.getAdminService().getEventContext().groupId
        ctx = {'omero.group': '-1'}  # query across groups
        for i in images:
            img = self.query.get('Image', i.id.val, ctx)
            assert img.details.group.id.val == gid

    def testFilesetOneImage(self):
        images = self.importMIF(1)  # One image in a fileset

        # try to move only one image to the new group
        target_group = self.target_groups['rw----']
        self.args += ['%s' % target_group.id.val,
                      '/Image:%s' % images[0].id.val]
        self.cli.invoke(self.args, strict=True)

        # check the image has been moved
        ctx = {'omero.group': '-1'}  # query across groups
        for i in images:
            img = self.query.get('Image', i.id.val, ctx)
            assert img.details.group.id.val == target_group.id.val

    def testFilesetAllImagesMoveImages(self):
        images = self.importMIF(2)  # 2 images sharing a fileset

        # try to move both the images to the new group
        target_group = self.target_groups['rw----']
        self.args += ['%s' % target_group.id.val,
                      'Image:%s,%s' % (images[0].id.val, images[1].id.val)]
        self.cli.invoke(self.args, strict=True)

        # check the images have been moved
        ctx = {'omero.group': '-1'}  # query across groups
        for i in images:
            img = self.query.get('Image', i.id.val, ctx)
            assert img.details.group.id.val == target_group.id.val

    def testFilesetAllImagesMoveDataset(self):
        images = self.importMIF(2)  # 2 images sharing a fileset
        dataset_id = self.create_object('Dataset')  # ... in a dataset

        # put the images into the dataset
        for image in images:
            link = omero.model.DatasetImageLinkI()
            link.parent = omero.model.DatasetI(dataset_id, False)
            link.child = omero.model.ImageI(image.id.val, False)
            self.update.saveObject(link)

        # try to move the dataset to the new group
        target_group = self.target_groups['rw----']
        self.args += ['%s' % target_group.id.val, 'Dataset:%s' % dataset_id]
        self.cli.invoke(self.args, strict=True)

        # query across groups
        ctx = {'omero.group': '-1'}

        # check the dataset been moved
        ds = self.query.get('Dataset', dataset_id, ctx)
        assert ds.details.group.id.val == target_group.id.val

        # check the images have been moved
        for i in images:
            img = self.query.get('Image', i.id.val, ctx)
            assert img.details.group.id.val == target_group.id.val

    @pytest.mark.parametrize("group_prefix", group_prefixes)
    def testNonExistingGroupId(self, group_prefix):
        self.args += ['%s-1' % group_prefix, '/Image:1']
        with pytest.raises(NonZeroReturnCode):
            self.cli.invoke(self.args, strict=True)

    def testNonExistingGroupName(self):
        self.args += [self.uuid(), '/Image:1']
        with pytest.raises(NonZeroReturnCode):
                self.cli.invoke(self.args, strict=True)

    # These tests try to exercise the various grouping possibilities
    # when passing multiple objects on the command line. In all of these
    # cases using the --ordered flag should make no difference

    @pytest.mark.parametrize('number', [1, 2, 3])
    @pytest.mark.parametrize("ordered", ordered)
    def testMultipleSimpleObjectsSameClass(self, number, ordered):
        dsets = [self.make_dataset() for i in range(number)]

        # try to move the objects to the new group
        target_group = self.target_groups['rw----']
        self.args += ['%s' % target_group.name.val]
        for d in dsets:
            self.args += ['Dataset:%s' % d.id.val]
        if ordered:
            self.args += ["--ordered"]
        self.cli.invoke(self.args, strict=True)

        # Check the objects have been moved.
        for d in dsets:
            dset = self.query.get('Dataset', d.id.val, all_grps)
            assert dset.details.group.id.val == target_group.id.val

    @pytest.mark.parametrize('number', [1, 2, 3])
    @pytest.mark.parametrize("ordered", ordered)
    def testMultipleSimpleObjectsTwoClassesSeparated(self, number, ordered):
        projs = [self.make_project() for i in range(number)]
        dsets = [self.make_dataset() for i in range(number)]

        # try to move the objects to the new group
        target_group = self.target_groups['rw----']
        self.args += ['%s' % target_group.name.val]
        for p in projs:
            self.args += ['Project:%s' % p.id.val]
        for d in dsets:
            self.args += ['Dataset:%s' % d.id.val]
        if ordered:
            self.args += ["--ordered"]
        self.cli.invoke(self.args, strict=True)

        # Check the objects have been moved.
        for p in projs:
            proj = self.query.get('Project', p.id.val, all_grps)
            assert proj.details.group.id.val == target_group.id.val
        for d in dsets:
            dset = self.query.get('Dataset', d.id.val, all_grps)
            assert dset.details.group.id.val == target_group.id.val

    @pytest.mark.parametrize('number', [1, 2, 3])
    @pytest.mark.parametrize("ordered", ordered)
    def testMultipleSimpleObjectsTwoClassesInterlaced(self, number, ordered):
        projs = [self.make_project() for i in range(number)]
        dsets = [self.make_dataset() for i in range(number)]

        # try to move the objects to the new group
        target_group = self.target_groups['rw----']
        self.args += ['%s' % target_group.name.val]
        for i in range(number):
            self.args += ['Project:%s' % projs[i].id.val]
            self.args += ['Dataset:%s' % dsets[i].id.val]
        if ordered:
            self.args += ["--ordered"]
        self.cli.invoke(self.args, strict=True)

        # Check the objects have been moved.
        for p in projs:
            proj = self.query.get('Project', p.id.val, all_grps)
            assert proj.details.group.id.val == target_group.id.val
        for d in dsets:
            dset = self.query.get('Dataset', d.id.val, all_grps)
            assert dset.details.group.id.val == target_group.id.val

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

        # try to move the objects to the new group
        target_group = self.target_groups['rw----']
        self.args += ['%s' % target_group.name.val]
        self.args += ['Project' + form + '/Image:%s' % proj.id.val]
        if ordered:
            self.args += ["--ordered"]
        self.cli.invoke(self.args, strict=True)

        # Check that only the images have been moved.
        gid = self.sf.getAdminService().getEventContext().groupId
        p = self.query.get('Project', proj.id.val, all_grps)
        assert p.details.group.id.val == gid
        d = self.query.get('Dataset', dset.id.val, all_grps)
        assert d.details.group.id.val == gid
        for i in imgs:
            img = self.query.get('Image', i.id.val, all_grps)
            assert img.details.group.id.val == target_group.id.val

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

        # try to move the objects to the new group
        target_group = self.target_groups['rw----']
        self.args += ['%s' % target_group.name.val]
        for p in projs:
            self.args += ['Project/Dataset/Image:%s' % p.id.val]
        if ordered:
            self.args += ["--ordered"]
        self.cli.invoke(self.args, strict=True)

        # Check that only the images have been moved.
        gid = self.sf.getAdminService().getEventContext().groupId
        for p in projs:
            proj = self.query.get('Project', p.id.val, all_grps)
            assert proj.details.group.id.val == gid
        for d in dsets:
            dset = self.query.get('Dataset', d.id.val, all_grps)
            assert dset.details.group.id.val == gid
        for i in imgs:
            img = self.query.get('Image', i.id.val, all_grps)
            assert img.details.group.id.val == target_group.id.val

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

        # try to move the objects to the new group
        target_group = self.target_groups['rw----']
        self.args += ['%s' % target_group.name.val]
        for d in ds:
            self.args += ['Dataset:%s' % d.id.val]
        for p in projs:
            self.args += ['Project/Dataset/Image:%s' % p.id.val]
        if ordered:
            self.args += ["--ordered"]
        self.cli.invoke(self.args, strict=True)

        # Check that only the images and separate datasets have been moved.
        gid = self.sf.getAdminService().getEventContext().groupId
        for p in projs:
            proj = self.query.get('Project', p.id.val, all_grps)
            assert proj.details.group.id.val == gid
        for d in dsets:
            dset = self.query.get('Dataset', d.id.val, all_grps)
            assert dset.details.group.id.val == gid
        for i in imgs:
            img = self.query.get('Image', i.id.val, all_grps)
            assert img.details.group.id.val == target_group.id.val
        for d in ds:
            dset = self.query.get('Dataset', d.id.val, all_grps)
            assert dset.details.group.id.val == target_group.id.val

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

        # try to move the objects to the new group
        target_group = self.target_groups['rw----']
        self.args += ['%s' % target_group.name.val]
        for i in range(number):
            self.args += ['Project/Dataset/Image:%s' % projs[i].id.val]
            self.args += ['Dataset:%s' % ds[i].id.val]
        if ordered:
            self.args += ["--ordered"]
        self.cli.invoke(self.args, strict=True)

        # Check that only the images and separate datasets have been moved.
        gid = self.sf.getAdminService().getEventContext().groupId
        for p in projs:
            proj = self.query.get('Project', p.id.val, all_grps)
            assert proj.details.group.id.val == gid
        for d in dsets:
            dset = self.query.get('Dataset', d.id.val, all_grps)
            assert dset.details.group.id.val == gid
        for i in imgs:
            img = self.query.get('Image', i.id.val, all_grps)
            assert img.details.group.id.val == target_group.id.val
        for d in ds:
            dset = self.query.get('Dataset', d.id.val, all_grps)
            assert dset.details.group.id.val == target_group.id.val


class TestChgrpRoot(RootCLITest):

    def setup_method(self, method):
        super(TestChgrpRoot, self).setup_method(method)
        self.cli.register("chgrp", ChgrpControl, "TEST")
        self.args += ["chgrp"]

    def testNonMember(self):
        new_image = self.new_image()
        new_image = self.update.saveAndReturnObject(new_image)

        # create a new group which the current root user is not member of
        group = self.new_group()

        # try to move the image to the new group
        self.args += ['%s' % group.id.val, '/Image:%s' % new_image.id.val]
        with pytest.raises(NonZeroReturnCode):
            self.cli.invoke(self.args, strict=True)
