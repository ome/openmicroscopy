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
from omero.cli import NonZeroReturnCode
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


class AnnotationFixture(object):
    """
    Fixture to test annotation arguments of bin/omero import
    """

    def __init__(self, arg_type, n, is_deprecated):
        self.arg_type = arg_type
        self.n = n
        self.is_deprecated = is_deprecated
        if self.is_deprecated:
            self.annotation_ns_arg = "--annotation_ns"
            self.annotation_text_arg = "--annotation_text"
            self.annotation_link_arg = "--annotation_link"
        else:
            self.annotation_ns_arg = "--annotation-ns"
            self.annotation_text_arg = "--annotation-text"
            self.annotation_link_arg = "--annotation-link"

    def get_name(self):
        if self.is_deprecated:
            return '%s-%s-Deprecated' % (self.arg_type, self.n)
        else:
            return '%s-%s-Official' % (self.arg_type, self.n)

AF = AnnotationFixture
AFS = (
    AF("Python", 1, False),
    AF("Python", 1, True),
    AF("Java", 1, False),
    AF("Java", 1, True),
    # Multiple annotation input are supported at the Java level only for now
    AF("Java", 2, False),
    AF("Java", 2, True))
AFS_names = [x.get_name() for x in AFS]

skip_fixtures = (
    [], ['all'], ['checksum'], ['minmax'], ['thumbnails'], ['upgrade'])


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

    def get_linked_annotations(self, oid):
        """Retrieve the comment annotation linked to the image"""

        params = omero.sys.ParametersI()
        params.addId(oid)
        query = "select t from TextAnnotation as t"
        query += " where exists ("
        query += " select aal from ImageAnnotationLink as aal"
        query += " where aal.child=t.id and aal.parent.id=:id) "
        return self.query.findAllByQuery(query, params, None)

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
        loggers = []
        # First two lines are logging of ome.formats.importer.ImportConfig
        # INFO level and are always output
        for line in out.split('\n')[2:]:
            splitline = line.split()
            # For some reason the ome.system.UpgradeCheck logging is always
            # output independently of the debug level
            if len(splitline) > 3 and splitline[2] in debug_levels:
                levels.append(splitline[2])
                loggers.append(splitline[3])
        return levels, loggers

    def parse_summary(self, err):
        """Parse the summary output from stderr"""

        return re.findall('\d:[\d]{2}:[\d]{2}\.[\d]{3}|\d',
                          err.split('\n')[-2])

    def get_thumbnail(self, iid):
        query = ("select t from Thumbnail t "
                 "join fetch t.pixels p "
                 "join fetch p.image as i where i.id = %s" % iid)
        t = self.query.findByQuery(query, None)
        return t

    def testAutoClose(self, tmpdir, capfd,):
        """Test auto-close argument"""

        fakefile = tmpdir.join("test.fake")
        fakefile.write('')

        self.args += [str(fakefile)]
        self.args += ['--', '--auto_close']
        self.cli.invoke(self.args, strict=True)

        # Check that there are no servants leftover
        stateful = []
        for x in range(10):
            stateful = self.client.getStatefulServices()
            if stateful:
                import time
                time.sleep(0.5)  # Give the backend some time to close
            else:
                break

        assert len(stateful) == 0

    @pytest.mark.parametrize("arg", [
        '--checksum-algorithm', '--checksum_algorithm'])
    @pytest.mark.parametrize("algorithm", [
        'Adler-32', 'CRC-32', 'File-Size-64', 'MD5-128', 'Murmur3-32',
        'Murmur3-128', 'SHA1-160'])
    def testChecksumAlgorithm(self, tmpdir, capfd, arg, algorithm):
        """Test checksum algorithm argument"""

        fakefile = tmpdir.join("test.fake")
        fakefile.write('')

        self.args += [str(fakefile)]
        self.args += ['--', arg, algorithm]

        # Invoke CLI import command and retrieve stdout/stderr
        self.cli.invoke(self.args, strict=True)

    @pytest.mark.parametrize("fixture", AFS, ids=AFS_names)
    def testAnnotationText(self, tmpdir, capfd, fixture):
        """Test argument creating a comment annotation linked to the import"""

        fakefile = tmpdir.join("test.fake")
        fakefile.write('')
        self.args += [str(fakefile)]
        if fixture.arg_type == 'Java':
            self.args += ['--']
        ns = ['ns%s' % i for i in range(fixture.n)]
        text = ['text%s' % i for i in range(fixture.n)]
        for i in range(fixture.n):
            self.args += [fixture.annotation_ns_arg, ns[i]]
            self.args += [fixture.annotation_text_arg, text[i]]

        # Invoke CLI import command and retrieve stdout/stderr
        self.cli.invoke(self.args, strict=True)
        o, e = capfd.readouterr()
        obj = self.get_object(e, 'Image')
        annotations = self.get_linked_annotations(obj.id.val)

        assert len(annotations) == fixture.n
        assert set([x.ns.val for x in annotations]) == set(ns)
        assert set([x.textValue.val for x in annotations]) == set(text)

    @pytest.mark.parametrize("fixture", AFS, ids=AFS_names)
    def testAnnotationLink(self, tmpdir, capfd, fixture):
        """Test argument linking imported image to a comment annotation"""

        fakefile = tmpdir.join("test.fake")
        fakefile.write('')

        comment_ids = []
        for i in range(fixture.n):
            comment = omero.model.CommentAnnotationI()
            comment.textValue = rstring('comment%s' % i)
            comment = self.update.saveAndReturnObject(comment)
            comment_ids.append(comment.id.val)

        self.args += [str(fakefile)]
        if fixture.arg_type == 'Java':
            self.args += ['--']
        for i in range(fixture.n):
            self.args += [fixture.annotation_link_arg, '%s' % comment_ids[i]]
        print self.args
        # Invoke CLI import command and retrieve stdout/stderr
        self.cli.invoke(self.args, strict=True)
        o, e = capfd.readouterr()
        obj = self.get_object(e, 'Image')
        annotations = self.get_linked_annotations(obj.id.val)

        assert len(annotations) == fixture.n
        assert set([x.id.val for x in annotations]) == set(comment_ids)

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
        out, err = capfd.readouterr()
        levels, loggers = self.parse_debug_levels(out)
        expected_levels = debug_levels[debug_levels.index(level):]
        assert set(levels) <= set(expected_levels), out

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

    @pytest.mark.parametrize("arg", ['--no-thumbnails', '--no_thumbnails'])
    def testNoThumbnails(self, tmpdir, capfd, arg):
        """Test symlink import"""

        fakefile = tmpdir.join("test.fake")
        fakefile.write('')

        self.args += [str(fakefile)]
        self.args += ['--', arg]

        # Invoke CLI import command and retrieve stdout/stderr
        self.cli.invoke(self.args, strict=True)
        out, err = capfd.readouterr()
        image = self.get_object(err, 'Image')

        # Check no thumbnails
        assert self.get_thumbnail(image.id.val) is None

    @pytest.mark.parametrize(
        "skipargs", skip_fixtures, ids=["_".join(x) for x in skip_fixtures])
    def testSkipArguments(self, tmpdir, capfd, skipargs):
        """Test symlink import"""

        fakefile = tmpdir.join("test.fake")
        fakefile.write('')

        self.args += [str(fakefile)]
        for skiparg in skipargs:
            self.args += ['--skip', skiparg]

        # Invoke CLI import command and retrieve stdout/stderr
        self.cli.invoke(self.args, strict=True)
        out, err = capfd.readouterr()
        image = self.get_object(err, 'Image')

        # Check min/max calculation
        query = ("select p from Pixels p left outer "
                 "join fetch p.channels as c "
                 "join fetch c.logicalChannel as lc "
                 "join fetch p.image as i where i.id = %s" % image.id.val)
        pixels = self.query.findByQuery(query, None)
        if 'minmax' in skipargs or 'all' in skipargs:
            assert pixels.getChannel(0).getStatsInfo() is None
            assert pixels.getSha1().val == "Foo"
        else:
            assert pixels.getChannel(0).getStatsInfo()
            assert pixels.getSha1() != "Foo"

        # Check no thumbnails
        if 'thumbnails' in skipargs or 'all' in skipargs:
            assert self.get_thumbnail(image.id.val) is None
        else:
            assert self.get_thumbnail(image.id.val)

        # Check UpgradeCheck skip
        levels, loggers = self.parse_debug_levels(out)
        if 'upgrade' in skipargs or 'all' in skipargs:
            assert 'ome.system.UpgradeCheck' not in loggers, out

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

    target_fixtures = [
        ("Dataset", "Image", "test.fake", '-d'),
        ("Screen", "Plate",
         "SPW&plates=1&plateRows=1&plateCols=1&fields=1&plateAcqs=1.fake",
         "-r")]

    @pytest.mark.parametrize("container,klass,filename,arg", target_fixtures)
    def testTargetInDifferentGroup(self, container, klass, filename, arg,
                                   tmpdir, capfd):
        """
        This now correctly tests the behaviour of the omero import
        command when a valid target from a different group is supplied.
        """
        new_group = self.new_group(experimenters=[self.user])
        self.sf.getAdminService().getEventContext()  # Refresh
        target = eval("omero.model."+container+"I")()
        target.name = rstring('testTargetInDifferentGroup')
        target = self.update.saveAndReturnObject(
            target, {"omero.group": str(new_group.id.val)})
        assert target.details.group.id.val == new_group.id.val

        fakefile = tmpdir.join(filename)
        fakefile.write('')
        self.args += [str(fakefile)]
        self.args += [arg, '%s' % target.id.val]

        # Invoke CLI import command and retrieve stdout/stderr
        self.cli.invoke(self.args, strict=True)

        o, e = capfd.readouterr()
        obj = self.get_object(e, klass)

        assert obj.details.group.id.val == new_group.id.val

    @pytest.mark.parametrize("container,klass,filename,arg", target_fixtures)
    def testUnknownTarget(self, container, klass, filename, arg, tmpdir):
        target = eval("omero.model."+container+"I")()
        target.name = rstring('testUnknownTarget')
        target = self.update.saveAndReturnObject(target)

        params = omero.sys.ParametersI()
        query = "select c from " + container + " as c"
        targets = self.query.findAllByQuery(
            query, params, {"omero.group": "-1"})
        tids = [t.id.val for t in targets]
        assert target.id.val in tids
        unknown = max(tids) + 1
        assert unknown not in tids

        fakefile = tmpdir.join(filename)
        fakefile.write('')
        self.args += [str(fakefile)]
        self.args += [arg, '%s' % unknown]

        with pytest.raises(NonZeroReturnCode):
            self.cli.invoke(self.args, strict=True)
