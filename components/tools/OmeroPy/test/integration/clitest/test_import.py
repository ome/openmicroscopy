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

plugin = __import__('omero.plugins.import', globals(), locals(),
                    ['ImportControl'], -1)
ImportControl = plugin.ImportControl
from test.integration.clitest.cli import CLITest
import pytest
import re
import omero
from omero.rtypes import rstring


class NamingFixture(object):
    """
    Fixture to test naming arguments of bin/omero import
    """

    def __init__(self, obj_type, name_arg, description_arg):
        self.obj_type = obj_type
        self.name_arg = name_arg
        self.description_arg = description_arg

NF = NamingFixture
NFS = (
    NF("Image", None, None),
    NF("Image", None, "-x"),
    NF("Image", None, "--description"),
    NF("Image", "-n", None),
    NF("Image", "-n", "-x"),
    NF("Image", "-n", "--description"),
    NF("Image", "--name", None),
    NF("Image", "--name", "-x"),
    NF("Image", "--name", "--description"),
    NF("Plate", None, None),
    NF("Plate", None, "-x"),
    NF("Plate", None, "--description"),
    NF("Plate", None, "--plate_description"),
    NF("Plate", "-n", None),
    NF("Plate", "-n", "-x"),
    NF("Plate", "-n", "--description"),
    NF("Plate", "-n", "--plate_description"),
    NF("Plate", "--name", None),
    NF("Plate", "--name", "-x"),
    NF("Plate", "--name", "--description"),
    NF("Plate", "--name", "--plate_description"),
    NF("Plate", "--plate_name", None),
    NF("Plate", "--plate_name", "-x"),
    NF("Plate", "--plate_name", "--description"),
    NF("Plate", "--plate_name", "--plate_description"),
)


class TestImport(CLITest):

    def setup_method(self, method):
        super(TestImport, self).setup_method(method)
        self.cli.register("import", plugin.ImportControl, "TEST")
        self.args += ["import"]
        dist_dir = self.OmeroPy / ".." / ".." / ".." / "dist"
        client_dir = dist_dir / "lib" / "client"
        self.args += ["--clientdir", client_dir]

    def get_object(self, err, obj_type):
        """Retrieve the created object by parsing the stderr output"""
        pattern = re.compile('^%s:(?P<id>\d+)$' % obj_type)
        for line in reversed(err.split('\n')):
            match = re.match(pattern, line)
            if match:
                break
        return self.query.get(obj_type, int(match.group('id')))

    def get_linked_annotation(self, oid):
        """Retrieve the comment annotation linked to the image"""

        params = omero.sys.ParametersI()
        params.addId(oid)
        query = "select t from TextAnnotation as t"
        query += " where exists ("
        query += " select aal from ImageAnnotationLink as aal"
        query += " where aal.child=t.id and aal.parent.id=:id) "
        return self.query.findByQuery(query, params)

    def get_parent(self, obj_type, oid, parent_type):
        """Retrieve the parent linked to the object"""

        params = omero.sys.ParametersI()
        params.addId(oid)
        query = "select d from %s as d" % parent_type
        query += " where exists ("
        query += " select l from %s%sLink as l" % (parent_type, obj_type)
        query += " where l.child.id=:id and l.parent=d.id) "
        return self.query.findByQuery(query, params)

    def testHelp(self):
        self.args += ["-h"]
        self.cli.invoke(self.args, strict=True)

    @pytest.mark.parametrize("fixture", NFS)
    def testNamingArguments(self, fixture, tmpdir, capfd):

        if fixture.obj_type == 'Image':
            fakefile = tmpdir.join("test.fake")
        else:
            fakefile = tmpdir.join("SPW&plates=1&plateRows=1&plateCols=1&"
                                   "fields=1&plateAcqs=1.fake")
        fakefile.write('')
        self.args += [str(fakefile)]
        if fixture.name_arg:
            self.args += [fixture.name_arg, 'name']
        if fixture.description_arg:
            self.args += [fixture.description_arg, 'description']

        # Invoke CLI import command and retrieve stdout/stderr
        self.cli.invoke(self.args, strict=True)
        o, e = capfd.readouterr()
        obj = self.get_object(e, fixture.obj_type)

        if fixture.name_arg:
            assert obj.getName().val == 'name'
        if fixture.description_arg:
            assert obj.getDescription().val == 'description'

    def testAnnotationText(self, tmpdir, capfd):

        fakefile = tmpdir.join("test.fake")
        fakefile.write('')
        self.args += [str(fakefile)]
        self.args += ['--annotation_ns', 'annotation_ns']
        self.args += ['--annotation_text', 'annotation_text']

        # Invoke CLI import command and retrieve stdout/stderr
        self.cli.invoke(self.args, strict=True)
        o, e = capfd.readouterr()
        obj = self.get_object(e, 'Image')
        annotation = self.get_linked_annotation(obj.id.val)

        assert annotation
        assert annotation.textValue.val == 'annotation_text'
        assert annotation.ns.val == 'annotation_ns'

    def testAnnotationLink(self, tmpdir, capfd):

        fakefile = tmpdir.join("test.fake")
        fakefile.write('')

        comment = omero.model.CommentAnnotationI()
        comment.textValue = rstring('test')
        comment = self.update.saveAndReturnObject(comment)

        self.args += [str(fakefile)]
        self.args += ['--annotation_link', '%s' % comment.id.val]

        # Invoke CLI import command and retrieve stdout/stderr
        self.cli.invoke(self.args, strict=True)
        o, e = capfd.readouterr()
        obj = self.get_object(e, 'Image')
        annotation = self.get_linked_annotation(obj.id.val)

        assert annotation
        assert annotation.id.val == comment.id.val

    def testDataset(self, tmpdir, capfd):

        fakefile = tmpdir.join("test.fake")
        fakefile.write('')

        dataset = omero.model.DatasetI()
        dataset.name = rstring('dataset')
        dataset = self.update.saveAndReturnObject(dataset)

        self.args += [str(fakefile)]
        self.args += ['-d', '%s' % dataset.id.val]

        # Invoke CLI import command and retrieve stdout/stderr
        self.cli.invoke(self.args, strict=True)
        o, e = capfd.readouterr()
        obj = self.get_object(e, 'Image')
        d = self.get_parent('Image', obj.id.val, 'Dataset')

        assert d
        assert d.id.val == dataset.id.val

    def testScreen(self, tmpdir, capfd):

        fakefile = tmpdir.join("SPW&plates=1&plateRows=1&plateCols=1&"
                               "fields=1&plateAcqs=1.fake")
        fakefile.write('')

        screen = omero.model.ScreenI()
        screen.name = rstring('screen')
        screen = self.update.saveAndReturnObject(screen)

        self.args += [str(fakefile)]
        self.args += ['-r', '%s' % screen.id.val]

        # Invoke CLI import command and retrieve stdout/stderr
        self.cli.invoke(self.args, strict=True)
        o, e = capfd.readouterr()
        obj = self.get_object(e, 'Plate')
        s = self.get_parent('Plate', obj.id.val, 'Screen')

        assert s
        assert s[0].id.val == screen.id.val            