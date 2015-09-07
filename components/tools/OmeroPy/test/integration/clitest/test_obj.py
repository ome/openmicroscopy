#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2014 University of Dundee & Open Microscopy Environment.
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


import pytest

from test.integration.clitest.cli import CLITest
from omero.plugins.obj import ObjControl
from omero.util.temp_files import create_path
from omero.cli import NonZeroReturnCode


class TestObj(CLITest):

    def setup_method(self, method):
        super(TestObj, self).setup_method(method)
        self.cli.register("obj", ObjControl, "TEST")
        self.args += ["obj"]
        self.setup_mock()

    def teardown_method(self, method):
        self.teardown_mock()

    def go(self):
        self.cli.invoke(self.args, strict=True)
        return self.cli.get("tx.state")

    def create_script(self):
        path = create_path()
        for x in ("Screen", "Plate", "Project", "Dataset"):
            path.write_text("new %s name=test\n" % x, append=True)
            path.write_text("new %s name=test description=foo\n" % x,
                            append=True)
        return path

    def test_create_from_file(self):
        path = self.create_script()
        self.args.append("--file=%s" % path)
        self.cli.invoke(self.args, strict=True)
        state = self.cli.get("tx.state")
        assert 8 == len(state)
        path.remove()

    def test_create_from_args(self):
        self.args.append("new")
        self.args.append("Dataset")
        self.args.append("name=foo")
        state = self.go()
        assert 1 == len(state)
        assert state.get_row(0).startswith("Dataset")

    def test_linkage(self):
        path = create_path()
        path.write_text(
            """
            new Project name=foo
            new Dataset name=bar
            new ProjectDatasetLink parent@=0 child@=1
            """)
        self.args.append("--file=%s" % path)
        state = self.go()
        assert 3 == len(state)
        assert state.get_row(0).startswith("Project")
        assert state.get_row(1).startswith("Dataset")
        assert state.get_row(2).startswith("ProjectDatasetLink")
        path.remove()

    def test_linkage_via_variables(self):
        path = create_path()
        path.write_text(
            """
            foo = new Project name=foo
            bar = new Dataset name=bar
            new ProjectDatasetLink parent@=foo child@=bar
            """)
        self.args.append("--file=%s" % path)
        state = self.go()
        assert 3 == len(state)
        assert state.get_row(0).startswith("Project")
        assert state.get_row(1).startswith("Dataset")
        assert state.get_row(2).startswith("ProjectDatasetLink")
        path.remove()

    @pytest.mark.parametrize(
        "input", (
            ("new", "Image"),
            ("new", "Image", "name=foo"),
            ("new", "ProjectDatasetLink", "parent=Project:1"),
        ))
    def test_required(self, input):
        self.args.extend(list(input))
        with pytest.raises(NonZeroReturnCode):
            self.go()

    def test_link_annotation(self):
        path = create_path()
        path.write_text(
            """
            new Dataset name=bar
            new CommentAnnotation ns=test textValue=foo
            new DatasetAnnotationLink parent@=0 child@=1
            """)
        self.args.append("--file=%s" % path)
        state = self.go()
        assert 3 == len(state)
        assert state.get_row(0).startswith("Dataset")
        assert state.get_row(1).startswith("CommentAnnotation")
        assert state.get_row(2).startswith("DatasetAnnotationLink")
        path.remove()

    def test_new_get_and_update(self):
        name = "foo"
        desc = "bar"
        self.args = self.login_args() + [
            "obj", "new", "Project", "name=%s" % name]
        state = self.go()
        project = state.get_row(0)
        self.args = self.login_args() + [
            "obj", "get", project, "name"]
        state = self.go()
        assert state.get_row(0) == name
        self.args = self.login_args() + [
            "obj", "update", project, "description=%s" % desc]
        self.go()
        self.args = self.login_args() + [
            "obj", "get", project, "description"]
        state = self.go()
        assert state.get_row(0) == desc

    def test_map_mods(self):
        self.args = self.login_args() + [
            "obj", "new", "MapAnnotation", "ns=test"]
        state = self.go()
        ann = state.get_row(0)

        self.args = self.login_args() + [
            "obj", "map-set", ann, "mapValue", "foo", "bar"]
        state = self.go()
        ann2 = state.get_row(0)
        assert ann == ann2

        self.args = self.login_args() + [
            "obj", "map-get", ann, "mapValue", "foo"]
        state = self.go()
        val = state.get_row(0)
        assert val == "bar"

    def test_nulling(self):
        self.args = self.login_args() + [
            "obj", "new", "MapAnnotation", "ns=test"]
        state = self.go()
        ann = state.get_row(0)

        self.args = self.login_args() + [
            "obj", "null", ann, "ns"]
        state = self.go()
        ann2 = state.get_row(0)
        assert ann == ann2
        type, id = ann.split(":")
        ann3 = self.client.sf.getQueryService().get(type, int(id))
        assert ann3.ns is None
