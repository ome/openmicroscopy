#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2013 University of Dundee & Open Microscopy Environment.
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


class TestChgrp(CLITest):

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

    def add_new_group(self, perms=None):
        admin = self.sf.getAdminService()
        exp = admin.lookupExperimenter(admin.getEventContext().userName)
        group = self.new_group([exp], perms)
        return group

    def testHelp(self):
        self.args += ["-h"]
        self.cli.invoke(self.args, strict=True)

    @pytest.mark.parametrize("object_type", object_types)
    def testObject(self, object_type):
        oid = self.create_object(object_type)

        # create a new group and move the object to the new group
        group = self.add_new_group()
        self.args += ['%s' % group.id.val, '/%s:%s' % (object_type, oid)]
        self.cli.invoke(self.args, strict=True)

        # change the session context and check the object has been moved
        self.set_context(self.client, group.id.val)
        new_object = self.query.get(object_type, oid)
        assert new_object.id.val == oid

    @pytest.mark.parametrize("perms", permissions)
    def testPermission(self, perms):
        iid = self.create_object("Image")

        # create a new group and move the image to the new group
        group = self.add_new_group(perms=perms)
        self.args += ['%s' % group.id.val, '/Image:%s' % iid]
        self.cli.invoke(self.args, strict=True)

        # change the session context and check the image has been moved
        self.set_context(self.client, group.id.val)
        new_img = self.query.get("Image", iid)
        assert new_img.id.val == iid

    def testNonMember(self):
        iid = self.create_object("Image")

        # create a new group which the current user is not member of
        group = self.new_group()

        # try to move the image to the new group
        self.args += ['%s' % group.id.val, '/Image:%s' % iid]
        self.cli.invoke(self.args, strict=True)

        # check the image has not been moved
        img = self.query.get("Image", iid)
        assert img.id.val == iid

    def testGroupName(self):
        iid = self.create_object("Image")

        # create a new group which the current user is not member of
        group = self.add_new_group()

        # try to move the image to the new group
        self.args += ['%s' % group.name.val, '/Image:%s' % iid]
        self.cli.invoke(self.args, strict=True)

        # change the session context and check the image has been moved
        self.set_context(self.client, group.id.val)
        img = self.query.get("Image", iid)
        assert img.id.val == iid

    @pytest.mark.parametrize("group_prefix", group_prefixes)
    def testGroupPrefix(self, group_prefix):
        iid = self.create_object("Image")

        # create a new group which the current user is not member of
        group = self.add_new_group()

        # try to move the image to the new group
        self.args += ['%s%s' % (group_prefix, group.id.val),
                      '/Image:%s' % iid]
        self.cli.invoke(self.args, strict=True)

        # change the session context and check the image has been moved
        self.set_context(self.client, group.id.val)
        img = self.query.get("Image", iid)
        assert img.id.val == iid

    def testFileset(self):
        # 2 images sharing a fileset
        images = self.importMIF(2)
        img = self.query.get('Image', images[0].id.val)
        filesetId = img.fileset.id.val
        fileset = self.query.get('Fileset', filesetId)
        assert fileset is not None

        # create a new group and move the fileset to the new group
        group = self.add_new_group()
        self.args += ['%s' % group.id.val, '/Fileset:%s' % filesetId]
        self.cli.invoke(self.args, strict=True)

        # # check the image cannot be queried in the current session
        # img = self.query.get('Image', images[0].id.val)
        # assert img is None

        # change the session context and check the image has been moved
        self.set_context(self.client, group.id.val)
        img = self.query.get('Image', images[0].id.val)
        assert img.id.val == images[0].id.val

    def testFilesetOneImg(self):
        # 2 images sharing a fileset
        images = self.importMIF(2)
        img = self.query.get('Image', images[0].id.val)
        filesetId = img.fileset.id.val
        fileset = self.query.get('Fileset', filesetId)
        assert fileset is not None

        # create a new group and try to move only one image to the new group
        group = self.add_new_group()
        self.args += ['%s' % group.id.val, '/Image:%s' % images[0].id.val]
        self.cli.invoke(self.args, strict=True)

        # check the image is still in the current group
        img = self.query.get('Image', images[0].id.val)
        assert img.id.val == images[0].id.val

    @pytest.mark.parametrize("group_prefix", group_prefixes)
    def testNonExistingGroupId(self, group_prefix):
        with pytest.raises(NonZeroReturnCode):
            self.args += ['%s-1' % group_prefix, '/Image:1']
            self.cli.invoke(self.args, strict=True)

    def testNonExistingGroupName(self):
        with pytest.raises(NonZeroReturnCode):
            self.args += [self.uuid(), '/Image:1']
            self.cli.invoke(self.args, strict=True)


class TestChgrpRoot(RootCLITest):

    def setup_method(self, method):
        super(TestChgrpRoot, self).setup_method(method)
        self.cli.register("chgrp", ChgrpControl, "TEST")
        self.args += ["chgrp"]

    def testHelp(self):
        self.args += ["-h"]
        self.cli.invoke(self.args, strict=True)

    def testNonMember(self):
        new_image = self.new_image()
        new_image = self.update.saveAndReturnObject(new_image)

        # create a new group which the current root user is not member of
        group = self.new_group()

        # try to move the image to the new group
        self.args += ['%s' % group.id.val, '/Image:%s' % new_image.id.val]
        self.cli.invoke(self.args, strict=True)

        # check the image has not been moved
        img = self.query.get('Image', new_image.id.val)
        assert img.id.val == new_image.id.val
