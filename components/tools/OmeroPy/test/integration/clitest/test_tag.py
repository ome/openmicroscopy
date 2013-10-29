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


class TestTag(CLITest):

    def setup_method(self, method):
        super(TestTag, self).setup_method(method)
        self.cli.register("tag", TagControl, "TEST")

    def create_tag(self, tag_name, tag_desc):
        args = self.login_args()
        args += ["tag", "create", "--name", tag_name, "--desc", tag_desc]
        self.cli.invoke(args, strict=True)

    def create_tagset(self, tag_ids, tag_name, tag_desc):
        args = self.login_args()
        args += ["tag", "createset", "--name", tag_name, "--desc", tag_desc]
        args += ["--tag"]
        args += ["%s" % tag_id for tag_id in tag_ids]
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

        tag1 = omero.model.TagAnnotationI()
        tag1.textValue = omero.rtypes.rstring("tagset %s - 1" % ts_name)
        tag1 = self.update.saveAndReturnObject(tag1)

        tag2 = omero.model.TagAnnotationI()
        tag2.textValue = omero.rtypes.rstring("tagset %s - 1" % ts_name)
        tag2 = self.update.saveAndReturnObject(tag2)

        self.create_tagset([tag1.id.val, tag2.id.val], ts_name, ts_desc)

        # Check tagset is created
        tagset = self.get_tag_by_name(ts_name)
        assert tagset.description.val == ts_desc

        # Check all tags are linked to the tagset
        tags = self.get_tags_in_tagset(tagset.id.val)
        tag_ids = [x.id.val for x in tags]
        assert sorted(tag_ids) == sorted([tag1.id.val, tag2.id.val])

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
    def testLinkImage(self):
        # Create a tag and an image
        tag = omero.model.TagAnnotationI()
        tag.textValue = omero.rtypes.rstring("%s" % self.uuid())
        tag = self.update.saveAndReturnObject(tag)
        img = self.new_image()
        img = self.update.saveAndReturnObject(img)
        iid = img.getId().getValue()

        #  Call tag link subcommand
        args = self.login_args()
        args += ["tag", "link", "Image:%s" % iid, "%s" % tag.id.val]
        self.cli.invoke(args, strict=True)

        # Check link
        params = omero.sys.Parameters()
        params.map = {}
        params.map["iid"] = rlong(iid)
        query = "select l from ImageAnnotationLink as l"
        query += " join fetch l.child as a where l.parent.id=:iid"
        link = self.query.findByQuery(query, params)

        assert link.child.id.val == tag.id.val
