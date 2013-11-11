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
from omero.plugins.chgrp import ChgrpControl
from test.integration.clitest.cli import CLITest
from omero.rtypes import rstring
import pytest

object_types = ["Dataset", "Project", "Plate", "Screen"]
permissions = ["rw----", "rwr---", "rwra--", "rwrw--"]


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
        new_object.name = omero.rtypes.rstring(self.name)
        self.update.saveObject(new_object)

    def get_object_by_name(self, object_type):
        # Query
        params = omero.sys.Parameters()
        params.map = {}
        query = "select o from %s as o" % object_type
        params.map["val"] = rstring(self.name)
        query += " where o.name = :val"
        return self.query.findByQuery(query, params)

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
        self.name = self.uuid()
        self.create_object(object_type)

        # check object has been created
        new_object = self.get_object_by_name(object_type)
        assert new_object.name.val == self.name

        # create a new group and change the object group
        group = self.add_new_group()
        self.args += ['%s' % group.id.val,
                      '/%s:%s' % (object_type, new_object.id.val)]
        self.cli.invoke(self.args, strict=True)

        # check the object cannot be queried in the current session
        new_object = self.get_object_by_name(object_type)
        assert new_object is None

        # change the session context and check the object has been moved
        self.set_context(self.client, group.id.val)
        new_object = self.get_object_by_name(object_type)
        assert new_object.name.val == self.name

    @pytest.mark.parametrize("perms", permissions)
    def testPermission(self, perms):
        object_type = "Dataset"
        self.name = self.uuid()
        self.create_object(object_type)

        # check object has been created
        new_object = self.get_object_by_name(object_type)
        assert new_object.name.val == self.name

        # create a new group and change the object group
        group = self.add_new_group(perms=perms)
        self.args += ['%s' % group.id.val,
                      '/%s:%s' % (object_type, new_object.id.val)]
        self.cli.invoke(self.args, strict=True)

        # check the object cannot be queried in the current session
        new_object = self.get_object_by_name(object_type)
        assert new_object is None

        # change the session context and check the object has been moved
        self.set_context(self.client, group.id.val)
        new_object = self.get_object_by_name(object_type)
        assert new_object.name.val == self.name
