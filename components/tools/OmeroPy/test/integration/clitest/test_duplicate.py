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
from omero.testlib.cli import CLITest
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

    def get_dataset(self, iid):
        """Retrieve all the parent datasets linked to the image"""

        params = omero.sys.ParametersI()
        params.addId(iid)
        query = ("select d from Dataset as d where exists"
                 "(select l from DatasetImageLink as l where "
                 "l.child.id=:id and l.parent=d.id)")
        return self.query.findAllByQuery(query, params)

    def get_project(self, did):
        """Retrieve all the parent projects linked to the dataaset"""

        params = omero.sys.ParametersI()
        params.addId(did)
        query = ("select p from Project as p where exists"
                 "(select l from ProjectDatasetLink as l where "
                 "l.child.id=:id and l.parent=p.id)")
        return self.query.findAllByQuery(query, params)

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

    def testDuplicateSingleObjectDryRun(self, capfd):
        name = self.uuid()
        object_type = "Dataset"
        oid = self.create_object(object_type, name=name)

        # Duplicate the object
        obj_arg = '%s:%s' % (object_type, oid)
        self.args += [obj_arg, "--dry-run"]
        out = self.duplicate(capfd)

        # Check output string
        assert obj_arg in out
        p = omero.sys.ParametersI()
        p.addString("name", name)
        query = "select obj from %s obj where obj.name=:name" % object_type
        objs = self.query.findAllByQuery(query, p)

        # Check original object is present and no duplication happened
        assert len(objs) == 1
        assert objs[0].id.val == oid

    def testDuplicateSingleObjectReport(self, capfd):
        name = self.uuid()
        object_type = "Dataset"
        oid = self.create_object(object_type, name=name)

        # Duplicate the object
        obj_arg = '%s:%s' % (object_type, oid)
        self.args += [obj_arg, "--report"]
        out = self.duplicate(capfd)
        lines_out = out.splitlines()

        p = omero.sys.ParametersI()
        p.addString("name", name)
        query = "select obj from %s obj where obj.name=:name" % object_type
        objs = self.query.findAllByQuery(query, p)

        # Check object has been duplicated
        assert len(objs) == 2
        assert objs[0].id.val != objs[1].id.val
        assert len(lines_out) == 6
        assert '%s:%s' % (object_type, objs[0].id.val) in out
        assert '%s:%s' % (object_type, objs[1].id.val) in out

    def testDuplicateSingleObjectDryRunReport(self, capfd):
        name = self.uuid()
        object_type = "Dataset"
        oid = self.create_object(object_type, name=name)

        # Duplicate the object
        obj_arg = '%s:%s' % (object_type, oid)
        self.args += [obj_arg, "--dry-run", "--report"]
        out = self.duplicate(capfd)
        lines_out = out.splitlines()

        p = omero.sys.ParametersI()
        p.addString("name", name)
        query = "select obj from %s obj where obj.name=:name" % object_type
        objs = self.query.findAllByQuery(query, p)

        # Check object has been duplicated
        assert len(objs) == 1
        assert objs[0].id.val == oid
        assert len(lines_out) == 7
        assert '%s:%s' % (object_type, objs[0].id.val) in out
        # Check object has been found in two different places of the output
        assert out.find(obj_arg) != out.rfind(obj_arg)

    def testBasicHierarchyDuplication(self, capfd):
        namep = self.uuid()
        named = self.uuid()
        namei = self.uuid()
        proj = self.make_project(namep)
        dset = self.make_dataset(named)
        img = self.make_image(namei)

        self.link(proj, dset)
        self.link(dset, img)

        self.args += ['Project:%s' % proj.id.val]
        self.args += ['--report']
        out = self.duplicate(capfd)

        pp = omero.sys.ParametersI()
        pp.addString("name", namep)
        query = "select obj from Project obj where obj.name=:name"
        objsp = self.query.findAllByQuery(query, pp)

        # Check the Project has been duplicated
        assert len(objsp) == 2
        assert objsp[0].id.val != objsp[1].id.val

        pid = max(objsp[0].id.val, objsp[1].id.val)

        # Check the duplicated Project is in the --report output
        assert 'Project:%s' % pid in out

        pd = omero.sys.ParametersI()
        pd.addString("name", named)
        query = "select obj from Dataset obj where obj.name=:name"
        objsd = self.query.findAllByQuery(query, pd)

        # Check the Dataset has been duplicated
        assert len(objsd) == 2
        assert objsd[0].id.val != objsd[1].id.val

        # Find the Projects linked to the duplicated dataset
        did = max(objsd[0].id.val, objsd[1].id.val)
        proj_linked = self.get_project(did)

        # Check the duplicated Dataset is in the --report output
        assert 'Dataset:%s' % did in out

        # Check the duplicated Dataset is linked to just the duplicated Project
        assert len(proj_linked) == 1
        assert proj_linked[0].id.val == pid

        pi = omero.sys.ParametersI()
        pi.addString("name", namei)
        query = "select obj from Image obj where obj.name=:name"
        objsi = self.query.findAllByQuery(query, pi)

        # Check the image has been duplicated
        assert len(objsi) == 2
        assert objsi[0].id.val != objsi[1].id.val

        # Find the Datasets linked to the duplicated image
        iid = max(objsi[0].id.val, objsi[1].id.val)
        dat_linked = self.get_dataset(iid)

        # Check the duplicated Image is in the --report output
        assert 'Image:%s' % iid in out

        # Check the duplicated image is linked to just the duplicated Dataset
        assert len(dat_linked) == 1
        assert dat_linked[0].id.val == did

    def testSkipheadDuplication(self, capfd):
        namep = self.uuid()
        named = self.uuid()

        proj = self.make_project(namep)
        dset = self.make_dataset(named)

        self.link(proj, dset)

        self.args += ['Project/Dataset:%s' % proj.id.val]
        self.args += ['--report']
        out = self.duplicate(capfd)

        pd = omero.sys.ParametersI()
        pd.addString("name", named)
        query = "select obj from Dataset obj where obj.name=:name"
        objsd = self.query.findAllByQuery(query, pd)

        # Check the Dataset has been duplicated
        assert len(objsd) == 2
        assert objsd[0].id.val != objsd[1].id.val

        did = max(objsd[0].id.val, objsd[1].id.val)

        # Check the duplicated Dataset is in the --report output
        assert 'Dataset:%s' % did in out

        # Find the Projects linked to the duplicated dataset
        proj_linked = self.get_project(did)

        # Check the Dataset is not linked to any Project
        assert len(proj_linked) == 0

        pp = omero.sys.ParametersI()
        pp.addString("name", namep)
        query = "select obj from Project obj where obj.name=:name"
        objsp = self.query.findAllByQuery(query, pp)

        # Check the Project has not been duplicated
        assert len(objsp) == 1
