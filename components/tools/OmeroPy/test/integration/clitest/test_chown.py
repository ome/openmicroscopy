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
from test.integration.clitest.cli import CLITest
import pytest

object_types = ["Image", "Dataset", "Project", "Plate", "Screen"]
user_prefixes = ["", "User:", "Experimenter:"]
all_grps = {'omero.group': '-1'}


class TestChown(CLITest):

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

        # check the object has been transfered
        obj = client.sf.getQueryService().get(object_type, oid, all_grps)
        assert obj.id.val == oid
        assert obj.details.owner.id.val == user.id.val
        with pytest.raises(omero.SecurityViolation):
            self.query.get(object_type, oid, all_grps)

    def testChownBasicUsageWithName(self):
        oid = self.create_object("Image")

        # create a user in the same group and transfer the object to the user
        client, user = self.new_client_and_user()
        self.add_experimenters(self.group, [user])
        self.args += ['%s' % (user.omeName.val),
                      '%s:%s' % ("Image", oid)]
        self.cli.invoke(self.args, strict=True)

        # check the object has been transfered
        obj = client.sf.getQueryService().get("Image", oid, all_grps)
        assert obj.id.val == oid
        assert obj.details.owner.id.val == user.id.val
        with pytest.raises(omero.SecurityViolation):
            self.query.get("Image", oid, all_grps)

    def testChownDifferentGroup(self):
        oid = self.create_object("Image")

        # create user and try to transfer the object to the user
        client, user = self.new_client_and_user()
        self.args += ['%s' % user.id.val,
                      '%s:%s' % ("Image", oid)]
        self.cli.invoke(self.args, strict=True)

        # check the object has not been transfered
        obj = self.query.get("Image", oid, all_grps)
        assert obj.id.val == oid
        assert obj.details.owner.id.val == self.user.id.val
        with pytest.raises(omero.SecurityViolation):
            client.sf.getQueryService().get("Image", oid, all_grps)
