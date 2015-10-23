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
import sys
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
SKIP_CHOICES = ['all', 'checksum', 'minmax', 'thumbnails', 'upgrade']


class ImportControl(BaseControl):

    COMMAND = [START_CLASS]

    def _configure(self, parser):
        parser.add_login_arguments()

        parser.add_argument(
            "--javahelp", "--java-help",
            action="store_true", help="Show the Java help text")
        parser.add_argument(
            "--advanced-help", action="store_true", dest="java_advanced_help",
            help="Show the advanced help text")
        parser.add_argument(
            "---file", nargs="?",
            help="File for storing the standard out of the Java process")
        parser.add_argument(
            "---errs", nargs="?",
            help="File for storing the standard err of the Java process")
        parser.add_argument(
            "--clientdir", type=str,
            help="Path to the directory containing the client JARs. "
            " Default: lib/client")

        # The following arguments are strictly passed to Java
        name_group = parser.add_argument_group(
            'Naming arguments', 'Optional arguments passed strictly to Java.')
        name_group.add_argument(
            "-n", "--name", dest="java_name",
            help="Image or plate name to use (**)",
            metavar="NAME")
        name_group.add_argument(
            "-x", "--description", dest="java_description",
            help="Image or plate description to use (**)",
            metavar="DESCRIPTION")
        # Deprecated naming arguments
        name_group.add_argument(
            "--plate_name", dest="java_plate_name",
            help=SUPPRESS)
        name_group.add_argument(
            "--plate_description", dest="java_plate_description",
            help=SUPPRESS)

        # Feedback options
        feedback_group = parser.add_argument_group(
            'Feedback arguments',
            'Optional arguments passed strictly to Java allowing to report'
            ' errors to the OME team.')
        feedback_group.add_argument(
            "--report", action="store_true", dest="java_report",
            help="Report errors to the OME team (**)")
        feedback_group.add_argument(
            "--upload", action="store_true", dest="java_upload",
            help=("Upload broken files and log file (if any) with report."
                  " Required --report (**)"))
        feedback_group.add_argument(
            "--logs", action="store_true", dest="java_logs",
            help=("Upload log file (if any) with report."
                  " Required --report (**)"))
        feedback_group.add_argument(
            "--email", dest="java_email",
            help="Email for reported errors. Required --report (**)",
            metavar="EMAIL")
        feedback_group.add_argument(
            "--qa-baseurl", dest="java_qa_baseurl",
            help=SUPPRESS)

        # Annotation options
        annotation_group = parser.add_argument_group(
            'Annotation arguments',
            'Optional arguments passed strictly to Java allowing to annotate'
            ' imports.')
        annotation_group.add_argument(
            "--annotation-ns", dest="java_ns", metavar="ANNOTATION_NS",
            help="Namespace to use for subsequent annotation (**)")
        annotation_group.add_argument(
            "--annotation-text", dest="java_text", metavar="ANNOTATION_TEXT",
            help="Content for a text annotation (requires namespace) (**)")
        annotation_group.add_argument(
            "--annotation-link", dest="java_link",
            metavar="ANNOTATION_LINK",
            help="Comment annotation ID to link all images to (**)")
        annotation_group.add_argument(
            "--annotation_ns", dest="java_ns", metavar="ANNOTATION_NS",
            help=SUPPRESS)
        annotation_group.add_argument(
            "--annotation_text", dest="java_text", metavar="ANNOTATION_TEXT",
            help=SUPPRESS)
        annotation_group.add_argument(
            "--annotation_link", dest="java_link", metavar="ANNOTATION_LINK",
            help=SUPPRESS)

        java_group = parser.add_argument_group(
            'Java arguments', 'Optional arguments passed strictly to Java')
        java_group.add_argument(
            "-f", dest="java_f", action="store_true",
            help="Display the used files and exit (**)")
        java_group.add_argument(
            "-c", dest="java_c", action="store_true",
            help="Continue importing after errors (**)")
        java_group.add_argument(
            "-l", dest="java_l",
            help="Use the list of readers rather than the default (**)",
            metavar="READER_FILE")
        java_group.add_argument(
            "-d", dest="java_d",
            help="OMERO dataset ID to import image into (**)",
            metavar="DATASET_ID")
        java_group.add_argument(
            "-r", dest="java_r",
            help="OMERO screen ID to import plate into (**)",
            metavar="SCREEN_ID")
        java_group.add_argument(
            "--debug", choices=DEBUG_CHOICES, dest="java_debug",
            help="Turn debug logging on (**)",
            metavar="LEVEL")

        parser.add_argument(
            "--depth", default=4, type=int,
            help="Number of directories to scan down for files")
        parser.add_argument(
            "--skip", type=str, choices=SKIP_CHOICES, action='append',
            help="Optional step to skip during import")
        parser.add_argument(
            "path", nargs="*",
            help="Path to be passed to the Java process")

        parser.set_defaults(func=self.importer)

    def set_login_arguments(self, args):
        """Set the connection arguments"""

        # Connection is required unless help arguments or -f is passed
        connection_required = ("-h" not in self.command_args and
                               not args.java_f and
                               not args.java_advanced_help)
        if connection_required:
            client = self.ctx.conn(args)
            self.command_args.extend(["-s", client.getProperty("omero.host")])
            self.command_args.extend(["-p", client.getProperty("omero.port")])
            self.command_args.extend(["-k", client.getSessionId()])

    def set_skip_arguments(self, args):
        """Set the arguments to skip steps during import"""
        if not args.skip:
            return

        if ('all' in args.skip or 'checksum' in args.skip):
            self.command_args.append("--checksum-algorithm=File-Size-64")
        if ('all' in args.skip or 'thumbnails' in args.skip):
            self.command_args.append("--no-thumbnails")
        if ('all' in args.skip or 'minmax' in args.skip):
            self.command_args.append("--no-stats-info")
        if ('all' in args.skip or 'upgrade' in args.skip):
            self.command_args.append("--no-upgrade-check")

    def set_java_arguments(self, args):
        """Set the arguments passed to Java"""
        # Due to the use of "--" some of these like debug
        # will never be filled out. But for completeness
        # sake, we include them here.
        java_args = {
            "java_f": "-f",
            "java_c": "-c",
            "java_l": "-l",
            "java_d": "-d",
            "java_r": "-r",
            "java_name": ("--name",),
            "java_description": ("--description",),
            "java_plate_name": ("--plate_name",),
            "java_plate_description": ("--plate_description",),
            "java_report": ("--report"),
            "java_upload": ("--upload"),
            "java_logs": ("--logs"),
            "java_email": ("--email"),
            "java_debug": ("--debug",),
            "java_qa_baseurl": ("--qa-baseurl",),
            "java_ns": "--annotation-ns",
            "java_text": "--annotation-text",
            "java_link": "--annotation-link",
            "java_advanced_help": "--advanced-help",
            }

        for attr_name, arg_name in java_args.items():
            arg_value = getattr(args, attr_name)
            if arg_value:
                if isinstance(arg_name, tuple):
                    arg_name = arg_name[0]
                    self.command_args.append(
                        "%s=%s" % (arg_name, arg_value))
                else:
                    self.command_args.append(arg_name)
                    if isinstance(arg_value, (str, unicode)):
                        self.command_args.append(arg_value)

    def importer(self, args):

        if args.clientdir:
            client_dir = path(args.clientdir)
        else:
            client_dir = self.ctx.dir / "lib" / "client"
        etc_dir = self.ctx.dir / "etc"
        xml_file = etc_dir / "logback-cli.xml"
        logback = "-Dlogback.configurationFile=%s" % xml_file

        try:
            classpath = [file.abspath() for file in client_dir.files("*.jar")]
        except OSError as e:
            self.ctx.die(102, "Cannot get JAR files from '%s' (%s)"
                         % (client_dir, e.strerror))
        if not classpath:
            self.ctx.die(103, "No JAR files found under '%s'" % client_dir)

        xargs = [logback, "-Xmx1024M", "-cp", os.pathsep.join(classpath)]

        # Create import command to be passed to Java
        self.command_args = []
        if args.javahelp:
            self.command_args.append("-h")
        self.set_login_arguments(args)
        self.set_skip_arguments(args)
        self.set_java_arguments(args)
        xargs.append("-Domero.import.depth=%s" % args.depth)
        import_command = self.COMMAND + self.command_args + args.path

        try:
            # Open file handles for stdout/stderr if applicable
            out = args.file
            err = args.errs

            if out:
                out = open(out, "w")
            if err:
                err = open(err, "w")

            p = omero.java.popen(
                import_command, debug=False, xargs=xargs, stdout=out,
                stderr=err)
            self.ctx.rv = p.wait()

        finally:
            # Make sure file handles are closed
            if out:
                out.close()
            if err:
                err.close()


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
