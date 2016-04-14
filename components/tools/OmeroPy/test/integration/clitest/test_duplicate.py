#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2016 University of Dundee & Open Microscopy Environment.
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
from omero.plugins.duplicate import DuplicateControl
from test.integration.clitest.cli import CLITest
import pytest

object_types = ["Image", "Dataset", "Project", "Plate", "Screen"]
model = ["", "I"]


class TestDuplicate(CLITest):

    def setup_method(self, method):
        super(TestDuplicate, self).setup_method(method)
        self.cli.register("duplicate", DuplicateControl, "TEST")
        self.args += ["duplicate"]

    def duplicate(self, capfd):
        self.cli.invoke(self.args, strict=True)
        return capfd.readouterr()[0]

    @pytest.mark.parametrize("model", model)
    @pytest.mark.parametrize("object_type", object_types)
    def testDuplicateSingleObject(self, object_type, model, capfd):
        name = self.uuid()
        oid = self.create_object(object_type, name=name)

        # Duplicate the object
        obj_arg = '%s%s:%s' % (object_type, model, oid)
        self.args += [obj_arg]
        out = self.duplicate(capfd)

        # Check output string
        assert obj_arg in out
        p = omero.sys.ParametersI()
        p.addString("name", name)
        query = "select obj from %s obj where obj.name=:name" % object_type
        objs = self.query.findAllByQuery(query, p)

        # Check object has been duplicated
        assert len(objs) == 2
        assert objs[0].id.val != objs[1].id.val

    @pytest.mark.parametrize("model", model)
    @pytest.mark.parametrize("object_type", object_types)
    def testDuplicateSingleObjectDryRun(self, object_type, model, capfd):
        name = self.uuid()
        oid = self.create_object(object_type, name=name)

        # Duplicate the object
        obj_arg = '%s%s:%s' % (object_type, model, oid)
        self.args += [obj_arg, "--dry-run"]
        out = self.duplicate(capfd)

        # Check output string
        assert obj_arg in out
        p = omero.sys.ParametersI()
        p.addString("name", name)
        query = "select obj from %s obj where obj.name=:name" % object_type
        objs = self.query.findAllByQuery(query, p)

        # Check object has been duplicated
        assert len(objs) == 1
        assert objs[0].id.val == oid
