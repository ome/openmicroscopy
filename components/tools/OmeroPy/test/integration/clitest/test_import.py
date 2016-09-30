# -*- coding: utf-8 -*-

#
# Copyright (C) 2014-2016 University of Dundee & Open Microscopy Environment.
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
import yaml
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
        self.keepRootAlive()

    def set_conn_args(self):
        host = self.root.getProperty("omero.host")
        port = self.root.getProperty("omero.port")
        self.args = ["import", "-s", host, "-p",  port]
        self.add_client_dir()

    def do_import(self, capfd, strip_logs=True):
        try:
            self.cli.invoke(self.args, strict=True)
            o, e = capfd.readouterr()
            if strip_logs:
                clean_o = ""
                for line in o.splitlines(True):
                    if not (re.search(r'^\d\d:\d\d:\d\d.*', line)
                            or re.search(r'.*\w\.\w.*', line)):
                        clean_o += line
                o = clean_o
        except NonZeroReturnCode:
            o, e = capfd.readouterr()
            print "O" * 40
            print o
            print "E" * 40
            print e
            raise
        return o, e

    def add_client_dir(self):
        dist_dir = self.OmeroPy / ".." / ".." / ".." / "dist"
        client_dir = dist_dir / "lib" / "client"
        self.args += ["--clientdir", client_dir]

    def check_other_output(self, out, import_type='default'):
        """Check the output of the import except objects
           (Images, Plates, Filesets) and Summary"""

        assert "==> Summary" in out
        if import_type == 'default' or import_type == 'legacy':
            assert "Other imported objects:" in out
        elif import_type == 'yaml':
            assert "Imported objects:" in out
        elif import_type == 'legacy':
            assert 'Imported Pixels:' in out

    def get_object(self, err, obj_type, query=None):
        """Retrieve the created object by parsing the stderr output"""
        pattern = re.compile('^%s:(?P<id>\d+)$' % obj_type)
        for line in reversed(err.split('\n')):
            match = re.match(pattern, line)
            if match:
                break
        obj_id = int(match.group('id'))
        return self.assert_object(obj_type, obj_id, query=query)

    def assert_object(self, obj_type, obj_id, query=None):
        if not query:
            query = self.query
        obj = query.get(obj_type, obj_id,
                        {"omero.group": "-1"})
        assert obj
        assert obj.id.val == obj_id
        return obj

    def get_objects(self, err, obj_type, query=None):
        """Retrieve the created objects by parsing the stderr output"""
        pattern = re.compile('^%s:(?P<idstring>\d+)$' % obj_type)
        objs = []
        for line in reversed(err.split('\n')):
            match = re.match(pattern, line)
            if match:
                ids = match.group('idstring').split(',')
                for obj_id in ids:
                    obj = self.assert_object(obj_type,
                                             int(obj_id), query=query)
                    objs.append(obj)
        return objs

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

    def get_screen(self, pid):
        """Retrieve the single screen linked to the plate"""

        params = omero.sys.ParametersI()
        params.addId(pid)
        query = "select d from Screen as d"
        query += " where exists ("
        query += " select l from ScreenPlateLink as l"
        query += " where l.child.id=:id and l.parent=d.id) "
        return self.query.findByQuery(query, params)

    def get_container(self, pid, spw=False):
        """Retrieve the single container linked to an image or plate"""

        if spw:
            return self.get_screen(pid)
        else:
            return self.get_dataset(pid)

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

    def parse_imported_objects(self, out):
        """Parse the output from stderr or stdout
           regarding Imported objects"""

        return out.strip('\n').split('\n')

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
        self.do_import(capfd)

        # Check that there are no servants leftover
        stateful = []
        for x in range(1000):
            stateful = self.client.getStatefulServices()
            if stateful:
                import time
                time.sleep(0.1)  # Give the backend some time to close
            else:
                break

        assert len(stateful) == 0

    CA_TESTS = [
        (False, 'Adler-32'),  # one underscore only
        (True, 'Adler-32'),
        (True, 'CRC-32'),
        (True, 'File-Size-64'),
        (True, 'MD5-128'),
        (True, 'Murmur3-32'),
        (True, 'Murmur3-128'),
        (True, 'SHA1-160')]
    CA_NAMES = ["%s-%s" % (x[1], x[0] and "underscore" or "legacy")
                for x in CA_TESTS]

    @pytest.mark.parametrize("args", CA_TESTS, ids=CA_NAMES)
    def testChecksumAlgorithm(self, tmpdir, capfd, args):
        """Test checksum algorithm argument"""

        dash, algorithm = args
        arg = dash and '--checksum-algorithm' or '--checksum_algorithm'

        fakefile = tmpdir.join("test.fake")
        fakefile.write('')

        self.args += [str(fakefile)]
        self.args += ['--', arg, algorithm]

        # Invoke CLI import command
        self.do_import(capfd)

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
        o, e = self.do_import(capfd)
        obj = self.get_object(o, 'Image')
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
        o, e = self.do_import(capfd)
        obj = self.get_object(o, 'Image')
        annotations = self.get_linked_annotations(obj.id.val)

        assert len(annotations) == fixture.n
        assert set([x.id.val for x in annotations]) == set(comment_ids)

    class TargetSource(object):

        def get_prefixes(self):
            return ()

        def get_arg(self, client, spw=False):
            raise NotImplemented()

        def verify_containers(self, found1, found2):
            raise NotImplemented()

    class ClassTargetSource(TargetSource):

        def get_arg(self, client, spw=False):
            pass

    class AbstractIdTargetSource(TargetSource):

        def create_container(self, client, spw=False):
            update = client.sf.getUpdateService()
            if spw:
                self.kls = "Screen"
                self.obj = omero.model.ScreenI()
            else:
                self.kls = "Dataset"
                self.obj = omero.model.DatasetI()
            self.obj.name = rstring(self.__class__.__name__+"-Test")
            self.obj = update.saveAndReturnObject(self.obj)
            self.oid = self.obj.id.val

        def verify_containers(self, found1, found2):
            assert self.oid == found1
            assert self.oid == found2

    class ImplicitIdModelTargetSource(AbstractIdTargetSource):

        def get_arg(self, client, spw=False):
            self.create_container(client, spw=spw)
            return ("-T", "%s:%s" % (self.kls, self.oid))

    class IdModelTargetSource(AbstractIdTargetSource):

        def get_arg(self, client, spw=False):
            self.create_container(client, spw=spw)
            return ("-T", "%s:id:%s" % (self.kls, self.oid))

    class LegacyIdModelTargetSource(AbstractIdTargetSource):

        def get_arg(self, client, spw=False):
            self.create_container(client, spw=spw)
            if spw:
                flag = "-r"
            else:
                flag = "-d"
            return (flag, "%s:%s" % (self.kls, self.oid))

    class LegacyIdOnlyTargetSource(AbstractIdTargetSource):

        def get_arg(self, client, spw=False):
            self.create_container(client, spw=spw)
            if spw:
                flag = "-r"
            else:
                flag = "-d"
            return (flag, "%s" % self.oid)

    class NameModelTargetSource(TargetSource):

        def get_arg(self, client, qualifier, spw=False):
            # For later
            self.query = client.sf.getQueryService()
            self.qualifier = qualifier
            if spw:
                self.kls = "Screen"
            else:
                self.kls = "Dataset"
            self.name = "NameModelTargetSource-Test"
            return ("-T", "%s:%sname:%s" % (self.kls, qualifier, self.name))

        def verify_containers(self, found1, found2):
            for attempt in (found1, found2):
                assert self.name == self.query.get(self.kls, attempt).name.val
            if self.qualifier == "@":
                assert found1 != found2
            else:
                assert found1 == found2

    class NameTemplateTargetSource(TargetSource):

        def get_arg(self, client, qualifier, spw=False):
            # For later
            self.query = client.sf.getQueryService()
            self.qualifier = qualifier
            if spw:
                self.kls = "Screen"
            else:
                self.kls = "Dataset"
            return ("-T", "regex:%sname:(?<Container1>.*)"
                    % (qualifier))

        def verify_containers(self, found1, found2):
            assert found1
            assert found2
            if self.qualifier == "@":
                assert found1 != found2
            else:
                assert found1 == found2

    class TemplateTargetSource(TargetSource):

        def __init__(self, template):
            self.template = template

        def get_prefixes(self):
            return ("a", "b")

        def get_arg(self, client, spw=False):
            self.spw = spw
            return ("-T", self.template)

        def verify_containers(self, found1, found2):
            assert found1
            assert found2
            assert found1 == found2

    SOURCES = (
        LegacyIdOnlyTargetSource(),
        LegacyIdModelTargetSource(),
        ImplicitIdModelTargetSource(),
        IdModelTargetSource(),
        TemplateTargetSource("regex:(?<Container1>.*)"),
        TemplateTargetSource(":(?<Container1>.*)"),
        # ClassTargetSource(),
    )

    def parse_container(self, spw, capfd):
        o, e = self.do_import(capfd)
        if spw:
            obj = self.get_object(o, 'Plate')
            container = self.get_screen(obj.id.val)
        else:
            obj = self.get_object(o, 'Image')
            container = self.get_dataset(obj.id.val)

        assert container
        found = container.id.val
        return found

    @pytest.mark.parametrize("spw", (True, False))
    @pytest.mark.parametrize("source", SOURCES)
    def testTargetArgument(self, spw, source, tmpdir, capfd):

        subdir = tmpdir
        for x in source.get_prefixes():
            subdir = subdir.join(x)
            subdir.mkdir()

        if spw:
            fakefile = subdir.join(
                "SPW&screens=0&plates=1&plateRows=1&plateCols=1&"
                "fields=1&plateAcqs=1.fake")
        else:
            fakefile = subdir.join("test.fake")
        fakefile.write('')

        self.args += source.get_arg(self.client, spw)
        self.args += [str(tmpdir)]

        # Now, run the import twice and check that the
        # pre and post container IDs match the sources'
        # assumptions
        found1 = self.parse_container(spw, capfd)
        found2 = self.parse_container(spw, capfd)
        source.verify_containers(found1, found2)

    @pytest.mark.parametrize("spw", (True, False))
    @pytest.mark.parametrize("qualifier", ("", "+", "-", "%", "@"))
    def testQualifiedNameModelTargetArgument(
            self, spw, qualifier, tmpdir, capfd):

        source = self.NameModelTargetSource()

        subdir = tmpdir
        if spw:
            fakefile = subdir.join(
                "SPW&screens=0&plates=1&plateRows=1&plateCols=1&"
                "fields=1&plateAcqs=1.fake")
        else:
            fakefile = subdir.join("test.fake")
        fakefile.write('')

        self.args += source.get_arg(self.client, qualifier, spw)
        self.args += [str(tmpdir)]

        # Now, run the import twice and check that the
        # pre and post container IDs match the sources'
        # assumptions
        found1 = self.parse_container(spw, capfd)
        found2 = self.parse_container(spw, capfd)
        source.verify_containers(found1, found2)

    @pytest.mark.parametrize("spw", (True, False))
    @pytest.mark.parametrize("qualifier", ("", "+", "-", "%", "@"))
    def testQualifiedNameTemplateTargetArgument(
            self, spw, qualifier, tmpdir, capfd):

        source = self.NameTemplateTargetSource()

        subdir = tmpdir
        if spw:
            fakefile = subdir.join(
                "SPW&screens=0&plates=1&plateRows=1&plateCols=1&"
                "fields=1&plateAcqs=1.fake")
        else:
            fakefile = subdir.join("test.fake")
        fakefile.write('')

        self.args += source.get_arg(self.client, qualifier, spw)
        self.args += [str(tmpdir)]

        # Now, run the import twice and check that the
        # pre and post container IDs match the sources'
        # assumptions
        found1 = self.parse_container(spw, capfd)
        found2 = self.parse_container(spw, capfd)
        source.verify_containers(found1, found2)

    @pytest.mark.parametrize("spw", (True, False))
    @pytest.mark.parametrize("qualifier", ("", "+", "-"))
    def testMultipleNameModelTargets(self, spw, qualifier, tmpdir, capfd):
        """ Test importing into a named target when Multiple targets exist """

        name = "MultipleNameModelTargetSource-Test-" + self.uuid()
        oids = []
        for i in range(2):
            if spw:
                kls = "Screen"
            else:
                kls = "Dataset"
            oid = self.create_object(kls, name=name)
            oids.append(oid)

        subdir = tmpdir
        if spw:
            fakefile = subdir.join((
                "SPW&screens=0&plates=1&plateRows=1&plateCols=1&"
                "fields=1&plateAcqs=1.fake"))
        else:
            fakefile = subdir.join("test.fake")
        fakefile.write('')

        target = "%s:%sname:%s" % (kls, qualifier, name)
        self.args += ['-T', target]
        self.args += [str(tmpdir)]

        # Run the import and get the container id
        found = self.parse_container(spw, capfd)
        if qualifier == "-":
            assert found == min(oids)
        else:
            assert found == max(oids)

    @pytest.mark.parametrize("spw", (True, False))
    def testUniqueMultipleNameModelTargets(self, spw, tmpdir, capfd):
        """ Test importing into a named target when Multiple targets exist """

        name = "UniqueMultipleNameModelTargetSource-Test-" + self.uuid()
        for i in range(2):
            if spw:
                kls = "Screen"
            else:
                kls = "Dataset"
            self.create_object(kls, name=name)

        subdir = tmpdir
        if spw:
            fakefile = subdir.join((
                "SPW&screens=0&plates=1&plateRows=1&plateCols=1&"
                "fields=1&plateAcqs=1.fake"))
        else:
            fakefile = subdir.join("test.fake")
        fakefile.write('')

        target = "%s:%%name:%s" % (kls, name)
        self.args += ['-T', target]
        self.args += [str(tmpdir)]

        with pytest.raises(NonZeroReturnCode):
            self.do_import(capfd)

    @pytest.mark.parametrize("spw", (True, False))
    def testNestedNameTemplateTargetArgument(
            self, spw, tmpdir, capfd):

        outer = "NestedNameTemplateTargetArgument-Test-" + self.uuid()
        inner1 = "NestedNameTemplateTargetArgument-Test-" + self.uuid()
        inner2 = "NestedNameTemplateTargetArgument-Test-" + self.uuid()
        subdir = tmpdir.mkdir(outer)
        if spw:
            importType = "Plate"
            fake = ("SPW&screens=0&plates=1&plateRows=1&plateCols=1&"
                    "fields=1&plateAcqs=1.fake")
        else:
            importType = "Image"
            fake = "test.fake"
        subdir.mkdir(inner1).join(fake).write('')
        subdir.mkdir(inner2).join(fake).write('')

        self.args += ("-T", "regex:name:^.*%s/(?<Container1>.*)" % outer)
        self.args += [str(tmpdir)]

        # Now, run the import and check that two distinct
        # containers are created and used.
        o, e = self.do_import(capfd)

        objs = self.get_objects(o, importType)
        assert len(objs) == 2
        container1 = self.get_container(objs[0].id.val, spw=spw)
        container2 = self.get_container(objs[1].id.val, spw=spw)
        assert container1.id.val != container2.id.val
        if container1.name.val == inner1:
            assert container2.name.val == inner2
        else:
            assert container1.name.val == inner2
            assert container2.name.val == inner1

    @pytest.mark.parametrize("spw", (True, False))
    @pytest.mark.parametrize("qualifier", ("", "+", "-", "@"))
    def testMultipleNameTemplateTargetArgument(
            self, spw, qualifier, tmpdir, capfd):

        outer = "MultipleNameTemplateTargetArgument-Test-" + self.uuid()
        inner = "MultipleNameTemplateTargetArgument-Test-" + self.uuid()
        oids = []
        for i in range(2):
            if spw:
                kls = "Screen"
            else:
                kls = "Dataset"
            oid = self.create_object(kls, name=inner)
            oids.append(oid)

        subdir = tmpdir.mkdir(outer)
        if spw:
            fake = ("SPW&screens=0&plates=1&plateRows=1&plateCols=1&"
                    "fields=1&plateAcqs=1.fake")
        else:
            fake = "test.fake"
        subdir.mkdir(inner).join(fake).write('')

        self.args += ("-T",
                      ("regex:%sname:^.*%s/(?<Container1>.*)"
                       % (qualifier, outer)))
        self.args += [str(tmpdir)]

        # Now, run the import and check that the correct
        # container is used or created and used.
        found = self.parse_container(spw, capfd)
        if qualifier == "-":
            assert found == min(oids)
        elif qualifier == "@":
            assert found not in oids
        else:
            assert found == max(oids)

    @pytest.mark.parametrize("spw", (True, False))
    def testUniqueMultipleNameTemplateTargetArgument(
            self, spw, tmpdir, capfd):

        outer = "UniqueMultipleNameTemplateTargetArgument-Test-" + self.uuid()
        inner = "UniqueMultipleNameTemplateTargetArgument-Test-" + self.uuid()
        oids = []
        for i in range(2):
            if spw:
                kls = "Screen"
            else:
                kls = "Dataset"
            oid = self.create_object(kls, name=inner)
            oids.append(oid)

        subdir = tmpdir.mkdir(outer)
        if spw:
            importType = "Plate"
            fake = ("SPW&screens=0&plates=1&plateRows=1&plateCols=1&"
                    "fields=1&plateAcqs=1.fake")
        else:
            importType = "Image"
            fake = "test.fake"
        subdir.mkdir(inner).join(fake).write('')

        self.args += ("-T", "regex:%%name:^.*%s/(?<Container1>.*)" % outer)
        self.args += [str(tmpdir)]

        # Now, run the import and check that the imported object
        # is not in a container.
        o, e = self.do_import(capfd)
        obj = self.get_object(o, importType)
        container = self.get_container(obj.id.val, spw=spw)
        assert container is None

    @pytest.mark.parametrize("kls", ("Project", "Plate", "Image"))
    def testBadTargetArgument(self, kls, tmpdir, capfd):

        subdir = tmpdir
        fakefile = subdir.join("test.fake")
        fakefile.write('')

        name = "BadNameModelTargetSource-Test"
        target = "%s:name:%s" % (kls, name)

        self.args += ['-T', target]
        self.args += [str(tmpdir)]

        with pytest.raises(NonZeroReturnCode):
            self.do_import(capfd)

    @pytest.mark.parametrize("kls", ("Dataset", "Screen"))
    def testBadModelTargetDiscriminator(self, kls, tmpdir, capfd):

        subdir = tmpdir
        fakefile = subdir.join("test.fake")
        fakefile.write('')

        name = "BadNameModelTargetSource-Test"
        target = "%s:notaname:%s" % (kls, name)

        self.args += ['-T', target]
        self.args += [str(tmpdir)]

        with pytest.raises(NonZeroReturnCode):
            self.do_import(capfd)

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
        out, err = self.do_import(capfd, strip_logs=False)
        levels, loggers = self.parse_debug_levels(out)
        expected_levels = debug_levels[debug_levels.index(level):]
        assert set(levels) <= set(expected_levels), out

    def testImportOutputDefault(self, tmpdir, capfd):
        """Test import output"""
        fakefile = tmpdir.join("test.fake")
        fakefile.write('')

        self.args += [str(fakefile)]
        o, e = self.do_import(capfd)

        # Check the contents of "o",
        # and the existence of the newly created image
        assert len(self.parse_imported_objects(o)) == 1
        self.get_object(o, 'Image')

        # Check the contents of "e"
        # and the existence of the newly created Fileset
        self.get_object(e, 'Fileset')
        self.check_other_output(e)

        # Parse and check the summary of the import output
        summary = self.parse_summary(e)
        assert summary
        assert len(summary) == 5

    def testImportOutputYaml(self, tmpdir, capfd):
        """Test import output in yaml case"""
        # Make sure you get yaml output
        self.args += ["--output", "yaml"]
        fakefile = tmpdir.join("test.fake")
        fakefile.write('')

        self.args += [str(fakefile)]
        o, e = self.do_import(capfd)
        yo = yaml.load(o)
        # Check the contents of "yo",
        # and the existence of the newly created fileset and image
        self.assert_object("Fileset", int(yo[0]['Fileset']))
        assert len(yo[0]['Image']) == 1
        self.assert_object("Image", int(yo[0]['Image'][0]))
        # Check the contents of "e"
        self.check_other_output(e, import_type='yaml')

        # Parse and check the summary of the import output
        summary = self.parse_summary(e)
        assert summary
        assert len(summary) == 5

    def testImportOutputLegacy(self, tmpdir, capfd):
        """Test import output in legacy case"""
        # Make sure you get legacy output
        self.args += ["--output", "legacy"]
        fakefile = tmpdir.join("test.fake")
        fakefile.write('')

        self.args += [str(fakefile)]
        o, e = self.do_import(capfd)

        # Check the contents of "o",
        # and the existence of the newly created image
        assert len(self.parse_imported_objects(o)) == 1
        pid = int(self.parse_imported_objects(o)[0])
        self.assert_object('Pixels', pid)

        # Check the contents of "e"
        # and the existence of the newly created Fileset and Image
        self.get_object(e, 'Fileset')
        self.get_object(e, 'Image')
        self.check_other_output(e, import_type='legacy')

        # Parse and check the summary of the import output
        summary = self.parse_summary(e)
        assert summary
        assert len(summary) == 5

    @pytest.mark.parametrize("plate", [1, 2, 3])
    def testImportOutputDefaultWithScreen(self, tmpdir, capfd, plate):
        """Test import summary argument with a screen"""
        fakefile = tmpdir.join("SPW&plates=%d&plateRows=1&plateCols=1&"
                               "fields=1&plateAcqs=1.fake" % plate)
        fakefile.write('')

        self.args += [str(fakefile)]
        o, e = self.do_import(capfd)

        # Check the contents of "o",
        # and the existence of the newly created plates
        assert len(self.parse_imported_objects(o)) == 1
        self.get_objects(o, 'Plate')

        # Check the contents of "e"
        # and the existence of the newly created Fileset
        self.get_object(e, 'Fileset')
        self.check_other_output(e)

        # Parse and check the summary of the import output
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
        o, e = self.do_import(capfd)
        obj = self.get_object(o, 'Image', query=client.sf.getQueryService())
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
        o, e = self.do_import(capfd)
        obj = self.get_object(o, 'Image', query=client.sf.getQueryService())
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
        o, e = self.do_import(capfd)
        obj = self.get_object(o, 'Image', query=client.sf.getQueryService())
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
        o, e = self.do_import(capfd)
        obj = self.get_object(o, fixture.obj_type)

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
        out, err = self.do_import(capfd)
        image = self.get_object(out, 'Image')

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
        out, err = self.do_import(capfd)
        image = self.get_object(out, 'Image')

        # Check min/max calculation
        query = ("select p from Pixels p left outer "
                 "join fetch p.channels as c "
                 "join fetch c.logicalChannel as lc "
                 "join fetch p.image as i where i.id = %s" % image.id.val)
        pixels = self.query.findByQuery(query, None)
        if 'minmax' in skipargs or 'all' in skipargs:
            assert pixels.getChannel(0).getStatsInfo() is None
            assert pixels.getSha1().val == "Pending..."
        else:
            assert pixels.getChannel(0).getStatsInfo()
            assert pixels.getSha1() != "Pending..."

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
        o, e = self.do_import(capfd)
        obj = self.get_object(o, 'Image')

        assert obj

    target_fixtures = [
        ("Dataset", "test.fake", '-d'),
        ("Screen",
         "SPW&plates=1&plateRows=1&plateCols=1&fields=1&plateAcqs=1.fake",
         "-r")]

    @pytest.mark.broken(reason="needs omero.group setting")
    @pytest.mark.parametrize("container,filename,arg", target_fixtures)
    def testTargetInDifferentGroup(self, container, filename, arg,
                                   tmpdir, capfd):
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
        o, e = self.do_import(capfd)
        obj = self.get_object(o, 'Image')
        assert obj.details.group.id.val == new_group.id.val

    @pytest.mark.parametrize("container,filename,arg", target_fixtures)
    def testUnknownTarget(self, container, filename, arg, tmpdir, capfd):
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
            self.do_import(capfd)
