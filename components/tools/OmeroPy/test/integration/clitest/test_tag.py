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
from omero.plugins.tag import TagControl
from test.integration.clitest.cli import CLITest
from omero.rtypes import rstring, rlong
from omero.util.temp_files import create_path
from omero_ext.mox import IgnoreArg
import __builtin__


class TestTag(CLITest):

    def setup_method(self, method):
        super(TestTag, self).setup_method(method)
        self.cli.register("tag", TagControl, "TEST")

    def create_tags(self, ntags, name):
        tag_ids = []
        for i in list(xrange(ntags)):
            tag = omero.model.TagAnnotationI()
            tag.textValue = omero.rtypes.rstring("%s - %s" % (name, i))
            tag = self.update.saveAndReturnObject(tag)
            if ntags > 1:
                tag_ids.append(tag.id.val)
            else:
                tag_ids = tag.id.val
        return tag_ids

    def create_tag(self, tag_name, tag_desc):
        args = self.login_args()
        args += ["tag", "create"]
        if tag_name:
            args += ["--name", tag_name]
        if tag_desc:
            args += ["--desc", tag_desc]
        self.cli.invoke(args, strict=True)

    def create_tagset(self, tag_ids, tag_name, tag_desc):
        args = self.login_args()
        args += ["tag", "createset"]
        args += ["--tag"]
        args += ["%s" % tag_id for tag_id in tag_ids]
        if tag_name:
            args += ["--name", tag_name]
        if tag_desc:
            args += ["--desc", tag_desc]

        self.cli.invoke(args, strict=True)

    def get_tag_by_name(self, tag_name):
        # Query
        params = omero.sys.Parameters()
        params.map = {}
        query = "select t from TagAnnotation as t"
        params.map["val"] = rstring(tag_name)
        query += " where t.textValue=:val"
        tags = self.query.findByQuery(query, params)
        return tags

    def get_tags_in_tagset(self, tagset_id):
        params = omero.sys.Parameters()
        params.map = {}
        query = "select t from TagAnnotation as t"
        params.map["tid"] = rlong(tagset_id)
        query += " where exists ("
        query += " select aal from AnnotationAnnotationLink as aal"
        query += " where aal.child=t.id and aal.parent.id=:tid) "
        tags = self.query.findAllByQuery(query, params)
        return tags

    # Help subcommands
    # ========================================================================
    def testHelp(self):
        args = self.login_args() + ["tag", "-h"]
        self.cli.invoke(args, strict=True)

    def testCreateHelp(self):
        args = self.login_args() + ["tag", "create", "-h"]
        self.cli.invoke(args, strict=True)

    def testCreateSetHelp(self):
        args = self.login_args() + ["tag", "createset", "-h"]
        self.cli.invoke(args, strict=True)

    def testListHelp(self):
        args = self.login_args() + ["tag", "list", "-h"]
        self.cli.invoke(args, strict=True)

    def testListSetsHelp(self):
        args = self.login_args() + ["tag", "listsets", "-h"]
        self.cli.invoke(args, strict=True)

    def testLinkHelp(self):
        args = self.login_args() + ["tag", "link", "-h"]
        self.cli.invoke(args, strict=True)

    def testLoadHelp(self):
        args = self.login_args() + ["tag", "load", "-h"]
        self.cli.invoke(args, strict=True)

    # Tag creation commands
    # ========================================================================
    def testCreateTag(self):
        tag_name = self.uuid()
        tag_desc = self.uuid()
        self.create_tag(tag_name, tag_desc)

        # Check tag is created
        tag = self.get_tag_by_name(tag_name)
        assert tag.description.val == tag_desc

    def testCreateTagNoDesc(self):
        tag_name = self.uuid()
        tag_desc = self.uuid()

        self.setup_mock()
        self.mox.StubOutWithMock(__builtin__, "raw_input")
        raw_input(IgnoreArg()).AndReturn(tag_desc)
        self.mox.ReplayAll()

        self.create_tag(tag_name, None)
        self.teardown_mock()

        # Check tag is created
        tag = self.get_tag_by_name(tag_name)
        assert tag.description.val == tag_desc

    def testCreateTagNoName(self):
        tag_name = self.uuid()
        tag_desc = self.uuid()

        self.setup_mock()
        self.mox.StubOutWithMock(__builtin__, "raw_input")
        raw_input(IgnoreArg()).AndReturn(tag_name)
        self.mox.ReplayAll()

        self.create_tag(None, tag_desc)
        self.teardown_mock()

        # Check tag is created
        tag = self.get_tag_by_name(tag_name)
        assert tag.description.val == tag_desc

    def testCreateTagNoNameNoDesc(self):
        tag_name = self.uuid()
        tag_desc = self.uuid()

        self.setup_mock()
        self.mox.StubOutWithMock(__builtin__, "raw_input")
        raw_input(IgnoreArg()).AndReturn(tag_name)
        raw_input(IgnoreArg()).AndReturn(tag_desc)
        self.mox.ReplayAll()

        self.create_tag(None, None)
        self.teardown_mock()

        # Check tag is created
        tag = self.get_tag_by_name(tag_name)
        assert tag.description.val == tag_desc

    def testLoadTag(self):
        tag_name = self.uuid()
        tag_desc = self.uuid()
        p = create_path(suffix=".json")
        p.write_text("""
[{
    "name" : "%s",
    "desc" : "%s"}]""" % (tag_name, tag_desc))
        args = self.login_args()
        args += ["tag", "load", str(p)]
        self.cli.invoke(args, strict=True)

        # Check tag is created
        tag = self.get_tag_by_name(tag_name)
        assert tag.description.val == tag_desc

    def testCreateTagset(self):
        ts_name = self.uuid()
        ts_desc = self.uuid()
        tag_ids = self.create_tags(2, ts_name)

        self.create_tagset(tag_ids, ts_name, ts_desc)

        # Check tagset is created
        tagset = self.get_tag_by_name(ts_name)
        assert tagset.description.val == ts_desc

        # Check all tags are linked to the tagset
        tags = self.get_tags_in_tagset(tagset.id.val)
        assert sorted([x.id.val for x in tags]) == sorted(tag_ids)

    def testCreateTagsetNoDesc(self):
        ts_name = self.uuid()
        ts_desc = self.uuid()
        tag_ids = self.create_tags(2, ts_name)

        self.setup_mock()
        self.mox.StubOutWithMock(__builtin__, "raw_input")
        raw_input(IgnoreArg()).AndReturn(ts_desc)
        self.mox.ReplayAll()
        self.create_tagset(tag_ids, ts_name, None)
        self.teardown_mock()

        # Check tagset is created
        tagset = self.get_tag_by_name(ts_name)
        assert tagset.description.val == ts_desc

        # Check all tags are linked to the tagset
        tags = self.get_tags_in_tagset(tagset.id.val)
        assert sorted([x.id.val for x in tags]) == sorted(tag_ids)

    def testCreateTagsetNoName(self):
        ts_name = self.uuid()
        ts_desc = self.uuid()
        tag_ids = self.create_tags(2, ts_name)

        self.setup_mock()
        self.mox.StubOutWithMock(__builtin__, "raw_input")
        raw_input(IgnoreArg()).AndReturn(ts_name)
        self.mox.ReplayAll()
        self.create_tagset(tag_ids, None, ts_desc)
        self.teardown_mock()

        # Check tagset is created
        tagset = self.get_tag_by_name(ts_name)
        assert tagset.description.val == ts_desc

        # Check all tags are linked to the tagset
        tags = self.get_tags_in_tagset(tagset.id.val)
        assert sorted([x.id.val for x in tags]) == sorted(tag_ids)

    def testCreateTagsetNoNameNoDesc(self):
        ts_name = self.uuid()
        ts_desc = self.uuid()
        tag_ids = self.create_tags(2, ts_name)

        self.setup_mock()
        self.mox.StubOutWithMock(__builtin__, "raw_input")
        raw_input(IgnoreArg()).AndReturn(ts_name)
        raw_input(IgnoreArg()).AndReturn(ts_desc)
        self.mox.ReplayAll()
        self.create_tagset(tag_ids, None, None)
        self.teardown_mock()

        # Check tagset is created
        tagset = self.get_tag_by_name(ts_name)
        assert tagset.description.val == ts_desc

        # Check all tags are linked to the tagset
        tags = self.get_tags_in_tagset(tagset.id.val)
        assert sorted([x.id.val for x in tags]) == sorted(tag_ids)

    def testLoadTagset(self):
        ts_name = self.uuid()
        ts_desc = self.uuid()
        tag_names = ["tagset %s - %s" % (ts_name, x) for x in [1, 2]]
        p = create_path(suffix=".json")
        p.write_text("""
[{
    "name" : "%s",
    "desc" : "%s",
    "set" : [{
             "name" : "%s",
             "desc" : ""},
             {
             "name" : "%s",
             "desc" : ""}]
}]""" % (ts_name, ts_desc, tag_names[0], tag_names[1]))
        args = self.login_args()
        args += ["tag", "load", str(p)]
        self.cli.invoke(args, strict=True)

        # Check tagset is created
        tagset = self.get_tag_by_name(ts_name)
        assert tagset.description.val == ts_desc

        # Check all tags are linked to the tagset
        tags = self.get_tags_in_tagset(tagset.id.val)
        assert sorted([x.textValue.val for x in tags]) == sorted(tag_names)

    # Tag linking commands
    # ========================================================================
    def get_link(self, classname, object_id):
        # Check link
        params = omero.sys.Parameters()
        params.map = {}
        params.map["id"] = rlong(object_id)
        query = "select l from %sAnnotationLink as l" % classname
        query += " join fetch l.child as a where l.parent.id=:id"
        link = self.query.findByQuery(query, params)
        return link

    def testLinkImage(self):
        # Create a tag and an image
        tid = self.create_tags(1, "%s" % self.uuid())
        img = self.new_image()
        img = self.update.saveAndReturnObject(img)
        iid = img.getId().getValue()

        # Call tag link subcommand
        args = self.login_args()
        args += ["tag", "link", "Image:%s" % iid, "%s" % tid]
        self.cli.invoke(args, strict=True)

        # Check link
        link = self.get_link("Image", iid)
        assert link.child.id.val == tid

    def testLinkDataset(self):
        # Create a tag and a dataset
        tid = self.create_tags(1, "%s" % self.uuid())
        ds = omero.model.DatasetI()
        ds.name = rstring("%s" % self.uuid())
        ds = self.update.saveAndReturnObject(ds)
        did = ds.getId().getValue()

        # Call tag link subcommand
        args = self.login_args()
        args += ["tag", "link", "Dataset:%s" % did, "%s" % tid]
        self.cli.invoke(args, strict=True)

        # Check link
        link = self.get_link("Dataset", did)
        assert link.child.id.val == tid

    def testLinkProject(self):
        # Create a tag and a project
        tid = self.create_tags(1, "%s" % self.uuid())
        p = omero.model.ProjectI()
        p.name = rstring("%s" % self.uuid())
        p = self.update.saveAndReturnObject(p)
        pid = p.getId().getValue()

        # Call tag link subcommand
        args = self.login_args()
        args += ["tag", "link", "Project:%s" % pid, "%s" % tid]
        self.cli.invoke(args, strict=True)

        # Check link
        link = self.get_link("Project", pid)
        assert link.child.id.val == tid

    def testLinkScreen(self):
        # Create a tag and a project
        tid = self.create_tags(1, "%s" % self.uuid())
        s = omero.model.ScreenI()
        s.name = rstring("%s" % self.uuid())
        s = self.update.saveAndReturnObject(s)
        sid = s.getId().getValue()

        # Call tag link subcommand
        args = self.login_args()
        args += ["tag", "link", "Screen:%s" % sid, "%s" % tid]
        self.cli.invoke(args, strict=True)

        # Check link
        link = self.get_link("Screen", sid)
        assert link.child.id.val == tid

    def testLinkPlate(self):
        # Create a tag and a project
        tid = self.create_tags(1, "%s" % self.uuid())
        p = omero.model.PlateI()
        p.name = rstring("%s" % self.uuid())
        p = self.update.saveAndReturnObject(p)
        pid = p.getId().getValue()

        # Call tag link subcommand
        args = self.login_args()
        args += ["tag", "link", "Plate:%s" % pid, "%s" % tid]
        self.cli.invoke(args, strict=True)

        # Check link
        link = self.get_link("Plate", pid)
        assert link.child.id.val == tid
