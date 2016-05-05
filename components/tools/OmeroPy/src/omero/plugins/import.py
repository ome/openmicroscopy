#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2009-2014 Glencoe Software, Inc. All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
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

"""
   Startup plugin for command-line importer.

"""

import os
import csv
import sys
import shlex
import fileinput

from omero.cli import BaseControl, CLI
import omero.java
from omero_ext.argparse import SUPPRESS
from path import path

START_CLASS = "ome.formats.importer.cli.CommandLineImporter"
TEST_CLASS = "ome.formats.test.util.TestEngine"

HELP = """Run the Java-based command-line importer

This is a Python wrapper around the Java importer. Login is handled by Python
OMERO.cli. To see more options, use "--javahelp".

Options marked with "**" are passed strictly to Java. If they interfere with
any of the Python arguments, you may need to end precede your arguments with a
"--".
"""
EXAMPLES = """
Examples:

  # Display help
  $ bin/omero import -h
  # Import foo.tiff using current login
  $ bin/omero import ~/Data/my_file.dv
  # Import foo.tiff using input credentials
  $ bin/omero import -s localhost -u user -w password foo.tiff
  # Set Java debugging level to ALL
  $ bin/omero import foo.tiff -- --debug=ALL
  # Display used files for importing foo.tiff
  $ bin/omero import foo.tiff -f
  # Limit debugging output
  $ bin/omero import -- --debug=ERROR foo.tiff

For additional information, see:
http://www.openmicroscopy.org/site/support/omero5.2/users/cli/import.html
Report bugs to <ome-users@lists.openmicroscopy.org.uk>
"""
TESTHELP = """Run the Importer TestEngine suite (devs-only)"""
DEBUG_CHOICES = ["ALL", "DEBUG", "ERROR", "FATAL", "INFO", "TRACE", "WARN"]
OUTPUT_CHOICES = ["legacy", "yaml"]
SKIP_CHOICES = ['all', 'checksum', 'minmax', 'thumbnails', 'upgrade']


class CommandArguments(object):

    def __init__(self, ctx, args):
        self.__ctx = ctx
        self.__args = args
        self.__accepts = set()
        self.__java_initial = list()
        self.__java_additional = list()
        self.__py_initial = list()
        self.__py_additional = list()
        # Python arguments
        self.__py_keys = (
            "javahelp", "skip", "file", "errs", "logback",
            "port", "password", "group", "create", "func",
            "bulk", "prog", "user", "key", "path", "logprefix",
            "JAVA_DEBUG", "quiet", "server", "depth", "clientdir")
        self.set_login_arguments(ctx, args)
        self.set_skip_arguments(args)

        for key in vars(args):
            self.__accepts.add(key)
            val = getattr(args, key)
            if key in self.__py_keys:
                # Place the Python elements on the CommandArguments
                # instance so that it behaves like `args`
                setattr(self, key, val)
                self.append_arg(self.__py_initial, key, val)

            elif not val:
                # If there's no value, do nothing
                pass

            else:
                self.append_arg(self.__java_initial, key, val)

    def append_arg(self, cmd_list, key, val):
        if len(key) == 1:
            cmd_list.append("-"+key)
            if isinstance(val, (str, unicode)):
                cmd_list.append(val)
        else:
            cmd_list.append(
                "--%s=%s" % (key, val))

    def set_path(self, path):
        if not isinstance(path, list):
            self.__ctx.die(202, "Path is not a list")
        else:
            self.path = path

    def java_args(self):
        rv = list()
        rv.extend(self.__java_initial)
        rv.extend(self.__java_additional)
        rv.extend(self.path)
        return rv

    def initial_args(self):
        rv = list()
        rv.extend(self.__py_initial)
        rv.extend(self.__java_initial)
        return rv

    def added_args(self):
        rv = list()
        rv.extend(self.__py_additional)
        rv.extend(self.__java_additional)
        rv.extend(self.path)
        return rv

    def accepts(self, key):
        return key in self.__accepts

    def add(self, key, val):

        if key in self.__py_keys:
            # First we check if this is a Python argument, in which
            # case it's set directly on the instance itself. This
            # may need to be later set elsewhere if multiple bulk
            # files are supported.
            setattr(self, key, val)
            self.append_arg(self.__py_additional, key, val)
        elif not self.accepts(key):
            self.__ctx.die(200, "Unknown argument: %s" % key)
        else:
            self.append_arg(self.__java_additional, key, val)

    def set_login_arguments(self, ctx, args):
        """Set the connection arguments"""

        if args.javahelp:
            self.__java_initial.append("-h")

        # Connection is required unless help arguments or -f is passed
        connection_required = ("-h" not in self.__java_initial and
                               not args.f and
                               not args.advanced_help)
        if connection_required:
            client = ctx.conn(args)
            self.__java_initial.extend(["-s", client.getProperty("omero.host")])
            self.__java_initial.extend(["-p", client.getProperty("omero.port")])
            self.__java_initial.extend(["-k", client.getSessionId()])

    def set_skip_arguments(self, args):
        """Set the arguments to skip steps during import"""
        if not args.skip:
            return

        if ('all' in args.skip or 'checksum' in args.skip):
            self.__java_initial.append("--checksum-algorithm=File-Size-64")
        if ('all' in args.skip or 'thumbnails' in args.skip):
            self.__java_initial.append("--no-thumbnails")
        if ('all' in args.skip or 'minmax' in args.skip):
            self.__java_initial.append("--no-stats-info")
        if ('all' in args.skip or 'upgrade' in args.skip):
            self.__java_initial.append("--no-upgrade-check")

    def open_files(self):
        # Open file handles for stdout/stderr if applicable
        out = self.open_log(self.__args.file, self.__args.logprefix)
        err = self.open_log(self.__args.errs, self.__args.logprefix)
        return out, err

    def open_log(self, file, prefix=None):
        if not file:
            return None
        if prefix:
            file = os.path.sep.join([prefix, file])
        dir = os.path.dirname(file)
        if not os.path.exists(dir):
            os.makedirs(dir)
        return open(file, "w")


class ImportControl(BaseControl):

    COMMAND = [START_CLASS]

    def _configure(self, parser):

        parser.add_login_arguments()

        parser.add_argument(
            "--javahelp", "--java-help",
            action="store_true", help="Show the Java help text")

        parser.add_argument(  # Special?
            "--advanced-help", action="store_true",
            help="Show the advanced help text")

        # The following arguments are strictly used by Python
        # The "---" form is kept for backwards compatibility.
        py_group = parser.add_argument_group(
            'Python arguments',
            'Optional arguments which are used to configure import.')

        def add_python_argument(*args, **kwargs):
            py_group.add_argument(*args, **kwargs)

        for name, help in (
            ("bulk", "Bulk YAML file for driving multiple imports"),
            ("logprefix", "Directory or file prefix for --file and --errs"),
            ("file", "File for storing the standard out of the Java process"),
            ("errs", "File for storing the standard err of the Java process")
        ):
            add_python_argument("--%s" % name, nargs="?", help=help)
            add_python_argument("---%s" % name, nargs="?", help=SUPPRESS)

        add_python_argument(
            "--clientdir", type=str,
            help="Path to the directory containing the client JARs. "
            " Default: lib/client")
        add_python_argument(
            "--logback", type=str,
            help="Path to a logback xml file. "
            " Default: etc/logback-cli.xml")

        # The following arguments are strictly passed to Java
        name_group = parser.add_argument_group(
            'Naming arguments', 'Optional arguments passed strictly to Java.')

        def add_java_name_argument(*args, **kwargs):
            name_group.add_argument(*args, **kwargs)

        add_java_name_argument(
            "-n", "--name",
            help="Image or plate name to use (**)",
            metavar="NAME")
        add_java_name_argument(
            "-x", "--description",
            help="Image or plate description to use (**)",
            metavar="DESCRIPTION")
        # Deprecated naming arguments
        add_java_name_argument(
            "--plate_name",
            help=SUPPRESS)
        add_java_name_argument(
            "--plate_description",
            help=SUPPRESS)

        # Feedback options
        feedback_group = parser.add_argument_group(
            'Feedback arguments',
            'Optional arguments passed strictly to Java allowing to report'
            ' errors to the OME team.')

        def add_feedback_argument(*args, **kwargs):
            feedback_group.add_argument(*args, **kwargs)

        add_feedback_argument(
            "--report", action="store_true",
            help="Report errors to the OME team (**)")
        add_feedback_argument(
            "--upload", action="store_true",
            help=("Upload broken files and log file (if any) with report."
                  " Required --report (**)"))
        add_feedback_argument(
            "--logs", action="store_true",
            help=("Upload log file (if any) with report."
                  " Required --report (**)"))
        add_feedback_argument(
            "--email",
            help="Email for reported errors. Required --report (**)",
            metavar="EMAIL")
        add_feedback_argument(
            "--qa-baseurl",
            help=SUPPRESS)

        # Annotation options
        annotation_group = parser.add_argument_group(
            'Annotation arguments',
            'Optional arguments passed strictly to Java allowing to annotate'
            ' imports.')

        def add_annotation_argument(*args, **kwargs):
            annotation_group.add_argument(*args, **kwargs)

        add_annotation_argument(
            "--annotation-ns", metavar="ANNOTATION_NS",
            help="Namespace to use for subsequent annotation (**)")
        add_annotation_argument(
            "--annotation-text", metavar="ANNOTATION_TEXT",
            help="Content for a text annotation (requires namespace) (**)")
        add_annotation_argument(
            "--annotation-link",
            metavar="ANNOTATION_LINK",
            help="Comment annotation ID to link all images to (**)")
        add_annotation_argument(
            "--annotation_ns", metavar="ANNOTATION_NS",
            help=SUPPRESS)
        add_annotation_argument(
            "--annotation_text", metavar="ANNOTATION_TEXT",
            help=SUPPRESS)
        add_annotation_argument(
            "--annotation_link", metavar="ANNOTATION_LINK",
            help=SUPPRESS)

        java_group = parser.add_argument_group(
            'Java arguments', 'Optional arguments passed strictly to Java')

        def add_java_argument(*args, **kwargs):
            java_group.add_argument(*args, **kwargs)

        add_java_argument(
            "-f", action="store_true",
            help="Display the used files and exit (**)")
        add_java_argument(
            "-c", action="store_true",
            help="Continue importing after errors (**)")
        add_java_argument(
            "-l",
            help="Use the list of readers rather than the default (**)",
            metavar="READER_FILE")
        add_java_argument(
            "-d",
            help="OMERO dataset ID to import image into (**)",
            metavar="DATASET_ID")
        add_java_argument(
            "-r",
            help="OMERO screen ID to import plate into (**)",
            metavar="SCREEN_ID")
        add_java_argument(
            "-T", "--target",
            help="OMERO target specification (**)",
            metavar="TARGET")
        add_java_argument(
            "--debug", choices=DEBUG_CHOICES,
            help="Turn debug logging on (**)",
            metavar="LEVEL", dest="JAVA_DEBUG")
        add_java_argument(
            "--output", choices=OUTPUT_CHOICES,
            help="Set an alternative output style",
            metavar="TYPE")

        # Unsure on these.
        add_python_argument(
            "--depth", default=4, type=int,
            help="Number of directories to scan down for files")
        add_python_argument(
            "--skip", type=str, choices=SKIP_CHOICES, action='append',
            help="Optional step to skip during import")
        add_python_argument(
            "path", nargs="*",
            help="Path to be passed to the Java process")

        parser.set_defaults(func=self.importer)

    def importer(self, args):

        if args.clientdir:
            client_dir = path(args.clientdir)
        else:
            client_dir = self.ctx.dir / "lib" / "client"
        etc_dir = self.ctx.dir / "etc"
        if args.logback:
            xml_file = path(args.logback)
        else:
            xml_file = etc_dir / "logback-cli.xml"
        logback = "-Dlogback.configurationFile=%s" % xml_file

        try:
            classpath = [file.abspath() for file in client_dir.files("*.jar")]
        except OSError as e:
            self.ctx.die(102, "Cannot get JAR files from '%s' (%s)"
                         % (client_dir, e.strerror))
        if not classpath:
            self.ctx.die(103, "No JAR files found under '%s'" % client_dir)

        command_args = CommandArguments(self.ctx, args)
        xargs = [logback, "-Xmx1024M", "-cp", os.pathsep.join(classpath)]
        xargs.append("-Domero.import.depth=%s" % args.depth)

        if args.bulk and args.path:
            self.ctx.die(104, "When using bulk import, omit paths")
        elif args.bulk:
            self.bulk_import(command_args, xargs)
        else:
            self.do_import(command_args, xargs)

    def do_import(self, command_args, xargs):
        out = err = None
        try:

            import_command = self.COMMAND + command_args.java_args()
            out, err = command_args.open_files()

            p = omero.java.popen(
                import_command, debug=False, xargs=xargs,
                stdout=out, stderr=err)

            self.ctx.rv = p.wait()

        finally:
            # Make sure file handles are closed
            if out:
                out.close()
            if err:
                err.close()

    def bulk_import(self, command_args, xargs):

        try:
            from yaml import safe_load
        except ImportError:
            self.ctx.die(105, "yaml is unsupported")

        old_pwd = os.getcwd()
        try:

            # Walk the .yml graph looking for includes
            # and load them all so that the top parent
            # values can be overwritten.
            contents = list()
            bulkfile = command_args.bulk
            while bulkfile:
                bulkfile = os.path.abspath(bulkfile)
                parent = os.path.dirname(bulkfile)
                with open(bulkfile, "r") as f:
                    data = safe_load(f)
                    contents.append((bulkfile, parent, data))
                    bulkfile = data.get("include")
                    os.chdir(parent)
                    # TODO: include file are updated based on the including file
                    # but other file paths aren't!

            bulk = dict()
            for bulkfile, parent, data in reversed(contents):
                bulk.update(data)
                os.chdir(parent)

            failed = 0
            total = 0
            for cont in self.parse_bulk(bulk, command_args):
                if command_args.dry_run:
                    self.ctx.out(" ".join(['"%s"' % x for x in command_args.added_args()]))
                else:
                    self.do_import(command_args, xargs)
                if self.ctx.rv:
                    failed += 1
                    total += self.ctx.rv
                    if cont:
                        msg = "Import failed with error code: %s. Continuing"
                        self.ctx.err(msg % self.ctx.rv)
                    else:
                        msg = "Import failed. Use -c to continue after errors"
                        self.ctx.die(106, msg)
                # Fail if any import failed
                self.ctx.rv = total
                if failed:
                    self.ctx.err("%x failed imports" % failed)
        finally:
            os.chdir(old_pwd)

    def parse_bulk(self, bulk, command_args):
        # Known keys with special handling
        cont = False

        command_args.dry_run = False
        if "dry_run" in bulk:
            dry_run = bulk.pop("dry_run")
            command_args.dry_run = dry_run

        if "continue" in bulk:
            cont = True
            c = bulk.pop("continue")
            command_args.add("c", c)

        if "path" not in bulk:
            # Required until @file format is implemented
            self.ctx.die(107, "No path specified")
        path = bulk.pop("path")

        cols = None
        if "columns" in bulk:
            cols = bulk.pop("columns")

        if "include" in bulk:
            bulk.pop("include")

        # Now parse all other keys
        for key in bulk:
            command_args.add(key, bulk[key])

        # All properties are set, yield for each path
        # to be imported in turn. The value for `cont`
        # is yielded so that the caller knows whether
        # or not an error should be fatal.

        if not cols:
            # No parsing necessary
            function = self.parse_text
        else:
            function = self.parse_shlex
            if path.endswith(".tsv"):
                function = self.parse_tsv
            elif path.endswith(".csv"):
                function = self.parse_csv

        for parts in function(path):
            if not cols:
                command_args.set_path(parts)
            else:
                for idx, col in enumerate(cols):
                    if col == "path":
                        command_args.set_path([parts[idx]])
                    else:
                        command_args.add(col, parts[idx])
            yield cont

    def parse_text(self, path, parse=False):
        with open(path, "r") as o:
            for line in o:
                line = line.strip()
                if parse:
                    line = shlex.split(line)
                yield [line]

    def parse_shlex(self, path):
        for line in self.parse_text(path, parse=True):
            yield line

    def parse_tsv(self, path, delimiter="\t"):
        for line in self.parse_csv(path, delimiter):
            yield line

    def parse_csv(self, path, delimiter=","):
        with open(path, "r") as data:
            for line in csv.reader(data, delimiter=delimiter):
                yield line


class TestEngine(ImportControl):
    COMMAND = [TEST_CLASS]

try:
    register("import", ImportControl, HELP, epilog=EXAMPLES)
    register("testengine", TestEngine, TESTHELP)
except NameError:
    if __name__ == "__main__":
        cli = CLI()
        cli.register("import", ImportControl, HELP, epilog=EXAMPLES)
        cli.register("testengine", TestEngine, TESTHELP)
        cli.invoke(sys.argv[1:])
