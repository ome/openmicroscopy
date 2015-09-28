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

import re
import pytest
import omero
from omero.plugins.tag import TagControl
from omero.cli import NonZeroReturnCode
from library import PFS
from test.integration.clitest.cli import CLITest
from omero.rtypes import rstring, rlong
from omero.util.temp_files import create_path
import __builtin__
NSINSIGHTTAGSET = omero.constants.metadata.NSINSIGHTTAGSET


class AbstractTagTest(CLITest):

    def setup_method(self, method):
        super(AbstractTagTest, self).setup_method(method)
        self.cli.register("tag", TagControl, "TEST")
        self.args += ["tag"]
        self.setup_mock()

    def teardown_method(self, method):
        self.teardown_mock()

    @classmethod
    def new_tag(self, name=""):
        tag = omero.model.TagAnnotationI()
        tag.textValue = omero.rtypes.rstring("%s" % name)
        return tag

    @classmethod
    def create_tags(self, ntags, name):
        tag_ids = []
        for i in list(xrange(ntags)):
            tag = self.new_tag("%s - %s" % (name, i))
            tag = self.update.saveAndReturnObject(tag)
            if ntags > 1:
                tag_ids.append(tag.id.val)
            else:
                tag_ids = tag.id.val
        return tag_ids

    @classmethod
    def create_tagset(self, tag_ids, name):
        tagset = omero.model.TagAnnotationI()
        tagset.textValue = omero.rtypes.rstring(name)
        tagset.ns = rstring(NSINSIGHTTAGSET)
        tagset = self.update.saveAndReturnObject(tagset)
        if tag_ids:
            links = []
            for tag_id in tag_ids:
                link = omero.model.AnnotationAnnotationLinkI()
                link.parent = tagset
                link.child = omero.model.TagAnnotationI(tag_id, False)
                links.append(link)
            self.update.saveArray(links)

        return tagset.id.val

    def get_link(self, classname, object_id, client=None):
        # Check link
        params = omero.sys.Parameters()
        params.map = {}
        params.map["id"] = rlong(object_id)
        query = "select l from %sAnnotationLink as l" % classname
        query += " join fetch l.child as a where l.parent.id=:id"
        if not client:
            link = self.query.findByQuery(query, params)
        else:
            link = client.sf.getQueryService().findByQuery(query, params)
        return link

    def get_object(self, out, query=None):
        """Retrieve the created object by parsing the stderr output"""
        if not query:
            query = self.query

        pattern = re.compile('^(?P<obj_type>.*):(?P<id>\d+)$')
        for line in reversed(out.split('\n')):
            print line
            match = re.match(pattern, line)
            if match:
                break
        return query.get(match.group('obj_type'), int(match.group('id')),
                         {"omero.group": "-1"})


class TestTag(AbstractTagTest):

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

    # Tag creation commands
    # ========================================================================
    @pytest.mark.parametrize('name_arg', [None, '--name'])
    @pytest.mark.parametrize('desc_arg', [None, '--description'])
    def testCreateTag(self, name_arg, desc_arg, capfd):
        tag_name = self.uuid()
        tag_desc = self.uuid()
        self.args += ["create"]
        self.mox.StubOutWithMock(__builtin__, "raw_input")
        if name_arg:
            self.args += [name_arg, tag_name]
        else:
            name_input = 'Please enter a name for this tag: '
            raw_input(name_input).AndReturn(tag_name)

        if desc_arg:
            self.args += [desc_arg, tag_desc]
        self.mox.ReplayAll()

        self.cli.invoke(self.args, strict=True)
        o, e = capfd.readouterr()

        # Check tag is created
        tag = self.get_object(o)
        assert tag.textValue.val == tag_name
        if desc_arg:
            assert tag.description.val == tag_desc
        else:
            assert tag.description is None

    @pytest.mark.parametrize('name_arg', [None, '--name'])
    @pytest.mark.parametrize('desc_arg', [None, '--desc'])
    def testCreateTagset(self, name_arg, desc_arg, capfd):
        tag_name = self.uuid()
        tag_desc = self.uuid()
        tag_ids = self.create_tags(2, tag_name)
        self.args += ["createset", "--tag"]
        self.args += ["%s" % tag_id for tag_id in tag_ids]

        self.mox.StubOutWithMock(__builtin__, "raw_input")
        if name_arg:
            self.args += [name_arg, tag_name]
        else:
            name_input = 'Please enter a name for this tag set: '
            raw_input(name_input).AndReturn(tag_name)
        if desc_arg:
            self.args += [desc_arg, tag_desc]
        self.mox.ReplayAll()

        self.cli.invoke(self.args, strict=True)
        o, e = capfd.readouterr()

        # Check tagset is created
        tagset = self.get_object(o)
        assert tagset.textValue.val == tag_name
        assert tagset.ns.val == NSINSIGHTTAGSET
        if desc_arg:
            assert tagset.description.val == tag_desc
        else:
            assert tagset.description is None

        # Check all tags are linked to the tagset
        tags = self.get_tags_in_tagset(tagset.id.val)
        assert sorted([x.id.val for x in tags]) == sorted(tag_ids)

    def testLoadTag(self, capfd):
        tag_name = self.uuid()
        tag_desc = self.uuid()
        p = create_path(suffix=".json")
        p.write_text("""
[{
    "name" : "%s",
    "desc" : "%s"}]""" % (tag_name, tag_desc))
        self.args += ["load", str(p)]
        self.cli.invoke(self.args, strict=True)
        o, e = capfd.readouterr()

        # Check tag is created
        tag = self.get_object(o)
        assert tag.textValue.val == tag_name
        assert tag.description.val == tag_desc

    def testLoadTagset(self, capfd):
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
        self.args += ["load", str(p)]
        self.cli.invoke(self.args, strict=True)
        o, e = capfd.readouterr()

        # Check tagset is created
        tagset = self.get_object(o)
        assert tagset.textValue.val == ts_name
        assert tagset.ns.val == NSINSIGHTTAGSET
        assert tagset.description.val == ts_desc

        # Check all tags are linked to the tagset
        tags = self.get_tags_in_tagset(tagset.id.val)
        assert sorted([x.textValue.val for x in tags]) == sorted(tag_names)

    # Tag linking commands
    # ========================================================================
    @pytest.mark.parametrize(
        'object_type', ['Image', 'Dataset', 'Project', 'Screen', 'Plate'])
    def testLink(self, object_type):
        # Create a tag and an image
        tid = self.create_tags(1, "%s" % self.uuid())
        if object_type == 'Image':
            new_object = self.new_image()
        else:
            new_object = getattr(omero.model, object_type + "I")()
        new_object.name = rstring("%s" % self.uuid())
        new_object = self.update.saveAndReturnObject(new_object)
        oid = new_object.getId().getValue()

        # Call tag link subcommand
        self.args += ["link", "%s:%s" % (object_type, oid), "%s" % tid]
        self.cli.invoke(self.args, strict=True)

        # Check link
        link = self.get_link(object_type, oid)
        assert link.child.id.val == tid

    def testLinkInvalidObject(self):
        # Create a tag and an image
        tid = self.create_tags(1, "%s" % self.uuid())

        # Call tag link subcommand
        self.args += ["link", "Project:0", "%s" % tid]
        with pytest.raises(NonZeroReturnCode):
            self.cli.invoke(self.args, strict=True)

    def testLinkInvalidTag(self):
        # Create a tag and an image
        img = self.new_image()
        img.name = rstring("%s" % self.uuid())
        img = self.update.saveAndReturnObject(img)

        # Call tag link subcommand
        self.args += ["link", "Image:%s" % img.id.val, "-1"]
        with pytest.raises(NonZeroReturnCode):
            self.cli.invoke(self.args, strict=True)


class TestPermissions(AbstractTagTest):

    @classmethod
    def setup_class(cls):
        super(TestPermissions, cls).setup_class()
        cls.groups = {}
        for perms in set([x.perms for x in PFS]):
            cls.groups[perms] = cls.new_group(perms=perms)

    def setup_method(self, method):
        super(TestPermissions, self).setup_method(method)
        self.args = ["tag"]

    @pytest.mark.parametrize('fixture', PFS, ids=[x.get_name() for x in PFS])
    def testLink(self, fixture):
        # Create a tag and an image
        tag = self.new_tag()
        img = self.new_image()
        writer = self.new_client(
            group=self.groups[fixture.perms],
            system=(fixture.writer == "system-admin"),
            owner=(fixture.writer == "group-owner"),
            )
        tag = writer.sf.getUpdateService().saveAndReturnObject(tag)
        img = writer.sf.getUpdateService().saveAndReturnObject(img)

        # Call tag link subcommand
        reader = self.new_client(
            group=self.groups[fixture.perms],
            system=(fixture.reader == "system-admin"),
            owner=(fixture.reader == "group-owner"),
            )
        self.args += ["link"]
        self.args += self.login_args(reader)
        self.args += ["Image:%s" % img.id.val, "%s" % tag.id.val]
        if fixture.canAnnotate:
            self.cli.invoke(self.args, strict=True)

            # Check link
            link = self.get_link('Image', img.id.val, client=reader)
            assert link.child.id.val == tag.id.val
        else:
            with pytest.raises(NonZeroReturnCode):
                self.cli.invoke(self.args, strict=True)


class TestTagList(AbstractTagTest):

    @classmethod
    def setup_class(cls):
        super(TestTagList, cls).setup_class()
        cls.tag_ids = cls.create_tags(2, 'list_tag')
        cls.tagset_id = cls.create_tagset(cls.tag_ids, 'list_set')
        cls.orphan_ids = cls.create_tags(2, 'orphan_tag')
        cls.empty_id = cls.create_tagset([], 'empty_set')

    # Tag list commands
    # ========================================================================
    @pytest.mark.parametrize('page_arg', ['', '--nopage'])
    def testList(self, capsys, page_arg):

        self.args += ["list"]
        if page_arg:
            self.args += [page_arg]
        self.cli.invoke(self.args, strict=True)

        out, err = capsys.readouterr()
        assert str(self.tagset_id) in out
        assert str(self.empty_id) in out
        for tag_id in self.tag_ids:
            assert str(tag_id) in out
        for tag_id in self.orphan_ids:
            assert str(tag_id) in out

    # Tag listsets commands
    # ========================================================================
    @pytest.mark.parametrize('page_arg', ['', '--nopage'])
    def testListSets(self, capsys, page_arg):

        self.args += ["listsets"]
        if page_arg:
            self.args += [page_arg]
        self.cli.invoke(self.args, strict=True)

        out, err = capsys.readouterr()
        assert str(self.tagset_id) in out
        assert str(self.empty_id) in out
        for tag_id in self.tag_ids:
            assert str(tag_id) not in out
        for tag_id in self.orphan_ids:
            assert str(tag_id) not in out
