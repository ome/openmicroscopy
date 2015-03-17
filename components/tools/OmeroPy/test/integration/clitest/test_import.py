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
import stat
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
xstr = lambda s: s or ""
NFS_names = ['%s%s%s' % (x.obj_type, xstr(x.name_arg),
             xstr(x.description_arg)) for x in NFS]
debug_levels = ['ALL', 'TRACE',  'DEBUG', 'INFO', 'WARN', 'ERROR']


class TestImport(CLITest):

    def setup_method(self, method):
        super(TestImport, self).setup_method(method)
        self.cli.register("import", plugin.ImportControl, "TEST")
        self.args += ["import"]
        self.add_client_dir()

    def set_conn_args(self):
        host = self.root.getProperty("omero.host")
        port = self.root.getProperty("omero.port")
        self.args = ["import", "-s", host, "-p",  port]
        self.add_client_dir()

    def add_client_dir(self):
        dist_dir = self.OmeroPy / ".." / ".." / ".." / "dist"
        client_dir = dist_dir / "lib" / "client"
        self.args += ["--clientdir", client_dir]

    def get_object(self, err, obj_type, query=None):
        if not query:
            query = self.query
        """Retrieve the created object by parsing the stderr output"""
        pattern = re.compile('^%s:(?P<id>\d+)$' % obj_type)
        for line in reversed(err.split('\n')):
            match = re.match(pattern, line)
            if match:
                break
        return query.get(obj_type, int(match.group('id')),
                         {"omero.group": "-1"})

    def get_linked_annotation(self, oid):
        """Retrieve the comment annotation linked to the image"""

        params = omero.sys.ParametersI()
        params.addId(oid)
        query = "select t from TextAnnotation as t"
        query += " where exists ("
        query += " select aal from ImageAnnotationLink as aal"
        query += " where aal.child=t.id and aal.parent.id=:id) "
        return self.query.findByQuery(query, params)

    def get_dataset(self, iid):
        """Retrieve the parent dataset linked to the image"""

        params = omero.sys.ParametersI()
        params.addId(iid)
        query = "select d from Dataset as d"
        query += " where exists ("
        query += " select l from DatasetImageLink as l"
        query += " where l.child.id=:id and l.parent=d.id) "
        return self.query.findByQuery(query, params)

    def get_screens(self, pid):
        """Retrieve the screens linked to the plate"""

        params = omero.sys.ParametersI()
        params.addId(pid)
        query = "select d from Screen as d"
        query += " where exists ("
        query += " select l from ScreenPlateLink as l"
        query += " where l.child.id=:id and l.parent=d.id) "
        return self.query.findAllByQuery(query, params)

    def parse_debug_levels(self, out):
        """Parse the debug levels from the stdout"""

        levels = []
        # First two lines are logging of ome.formats.importer.ImportConfig
        # INFO level and are always output
        for line in out.split('\n')[2:]:
            splitline = line.split()
            # For some reason the ome.system.UpgradeCheck logging is always
            # output independently of the debug level
            if len(splitline) > 3 and splitline[2] in debug_levels:
                levels.append(splitline[2])
        return levels

    def parse_summary(self, err):
        """Parse the summary output from stderr"""

        return re.findall('\d:[\d]{2}:[\d]{2}\.[\d]{3}|\d',
                          err.split('\n')[-2])

    @pytest.mark.parametrize("fixture", NFS, ids=NFS_names)
    def testNamingArguments(self, fixture, tmpdir, capfd):
        """Test naming arguments for the imported image/plate"""

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
        """Test argument creating a comment annotation linked to the import"""

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
        """Test argument linking imported image to a comment annotation"""

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

    def testDatasetArgument(self, tmpdir, capfd):
        """Test argument linking imported image to a dataset"""

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
        d = self.get_dataset(obj.id.val)

        assert d
        assert d.id.val == dataset.id.val

    def testScreenArgument(self, tmpdir, capfd):
        """Test argument linking imported plate to a screen"""

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
        screens = self.get_screens(obj.id.val)

        assert screens
        assert screen.id.val in [s.id.val for s in screens]

    @pytest.mark.parametrize("level", debug_levels)
    @pytest.mark.parametrize("prefix", [None, '--'])
    def testDebugArgument(self, tmpdir, capfd, level, prefix):
        """Test debug argument"""

        fakefile = tmpdir.join("test.fake")
        fakefile.write('')

        self.args += [str(fakefile)]
        if prefix:
            self.args += [prefix]
        self.args += ['--debug=%s' % level]
        # Invoke CLI import command and retrieve stdout/stderr
        self.cli.invoke(self.args, strict=True)
        o, e = capfd.readouterr()
        levels = self.parse_debug_levels(o)
        assert set(levels) <= set(debug_levels[debug_levels.index(level):]), o

    def testImportSummary(self, tmpdir, capfd):
        """Test import summary output"""
        fakefile = tmpdir.join("test.fake")
        fakefile.write('')

        self.args += [str(fakefile)]
        self.cli.invoke(self.args, strict=True)
        o, e = capfd.readouterr()
        summary = self.parse_summary(e)
        assert summary
        assert len(summary) == 5

    @pytest.mark.parametrize("plate", [1, 2, 3])
    def testImportSummaryWithScreen(self, tmpdir, capfd, plate):
        """Test import summary argument with a screen"""
        fakefile = tmpdir.join("SPW&plates=%d&plateRows=1&plateCols=1&"
                               "fields=1&plateAcqs=1.fake" % plate)
        fakefile.write('')

        self.args += [str(fakefile)]
        self.cli.invoke(self.args, strict=True)
        o, e = capfd.readouterr()
        summary = self.parse_summary(e)
        assert summary
        assert len(summary) == 6
        assert int(summary[3]) == plate

    def testImportAsRoot(self, tmpdir, capfd):
        """Test import using sudo argument"""

        # Create new client/user and fake file
        client, user = self.new_client_and_user()
        fakefile = tmpdir.join("test.fake")
        fakefile.write('')

        # Create argument list using sudo
        self.set_conn_args()
        self.args += ['--sudo', 'root']
        self.args += ["-w", self.root.getProperty("omero.rootpass")]
        self.args += ["-u", user.omeName.val]
        self.args += [str(fakefile)]

        # Invoke CLI import command and retrieve stdout/stderr
        self.cli.invoke(self.args, strict=True)
        o, e = capfd.readouterr()
        obj = self.get_object(e, 'Image', query=client.sf.getQueryService())
        assert obj.details.owner.id.val == user.id.val

    def testImportMultiGroup(self, tmpdir, capfd):
        """Test import using sudo argument"""

        # Create new client/user belonging in 2 groups and fake file
        group1 = self.new_group()
        client, user = self.new_client_and_user(group=group1)
        group2 = self.new_group([user])
        fakefile = tmpdir.join("test.fake")
        fakefile.write('')

        # Create argument list
        self.set_conn_args()
        self.args += ["-u", user.omeName.val]
        self.args += ["-w", user.omeName.val]
        self.args += ["-g", group2.name.val]
        self.args += [str(fakefile)]

        # Invoke CLI import command and retrieve stdout/stderr
        self.cli.invoke(self.args, strict=True)
        o, e = capfd.readouterr()
        obj = self.get_object(e, 'Image', query=client.sf.getQueryService())
        assert obj.details.owner.id.val == user.id.val
        assert obj.details.group.id.val == group2.id.val

    def testImportAsRootMultiGroup(self, tmpdir, capfd):
        """Test import using sudo argument"""

        # Create new client/user belonging in 2 groups and fake file
        group1 = self.new_group()
        client, user = self.new_client_and_user(group=group1)
        group2 = self.new_group([user])
        fakefile = tmpdir.join("test.fake")
        fakefile.write('')

        # Create argument list using sudo
        self.set_conn_args()
        self.args += ['--sudo', 'root']
        self.args += ["-w", self.root.getProperty("omero.rootpass")]
        self.args += ["-u", user.omeName.val, "-g", group2.name.val]
        self.args += [str(fakefile)]

        # Invoke CLI import command and retrieve stdout/stderr
        self.cli.invoke(self.args, strict=True)
        o, e = capfd.readouterr()
        obj = self.get_object(e, 'Image', query=client.sf.getQueryService())
        assert obj.details.owner.id.val == user.id.val
        assert obj.details.group.id.val == group2.id.val

    def testSymlinkImport(self, tmpdir, capfd):
        """Test symlink import"""

        fakefile = tmpdir.join("ln_s.fake")
        fakefile.write('')
        fakefile.chmod(stat.S_IREAD)

        self.args += [str(fakefile)]
        self.args += ['--', '--transfer', 'ln_s']

        # Invoke CLI import command and retrieve stdout/stderr
        self.cli.invoke(self.args, strict=True)
        o, e = capfd.readouterr()
        obj = self.get_object(e, 'Image')

        assert obj
