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

For additional information, see:
http://www.openmicroscopy.org/site/support/omero5/users/\
command-line-import.html
Report bugs to <ome-users@lists.openmicroscopy.org.uk>
"""
TESTHELP = """Run the Importer TestEngine suite (devs-only)"""


class ImportControl(BaseControl):

    COMMAND = [START_CLASS]

    def _configure(self, parser):
        parser.add_login_arguments()

        parser.add_argument(
            "--javahelp", action="store_true", help="Show the Java help text")
        parser.add_argument(
            "---file", nargs="?",
            help="File for storing the standard out of the Java process")
        parser.add_argument(
            "---errs", nargs="?",
            help="File for storing the standard err of the Java process")
        # The following arguments are strictly passed to Java
        name_group = parser.add_argument_group(
            'Naming arguments', 'Optional arguments passed strictly to Java. ')
        name_group.add_argument(
            "-n", dest="java_n",
            help="Image or plate name to use (**)",
            metavar="IMAGE_NAME")
        name_group.add_argument(
            "-x", dest="java_x",
            help="Image or plate description to use (**)",
            metavar="IMAGE_DESCRIPTION")

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
            "--report", action="store_true", dest="java_report",
            help="Report errors to the OME team (**)")
        java_group.add_argument(
            "--upload", action="store_true", dest="java_upload",
            help="Upload broken files with report (**)")
        java_group.add_argument(
            "--logs", action="store_true", dest="java_logs",
            help="Upload log file with report (**)")
        java_group.add_argument(
            "--email", dest="java_email",
            help="Email for reported errors (**)", metavar="EMAIL")
        java_group.add_argument(
            "--debug", dest="java_debug",
            help="Turn debug logging on (**)",
            choices=["ALL", "DEBUG", "ERROR", "FATAL", "INFO", "TRACE",
                     "WARN"],
            metavar="LEVEL")
        java_group.add_argument(
            "--annotation_ns", dest="java_ns", metavar="ANNOTATION_NS",
            help="Namespace to use for subsequent annotation (**)")
        java_group.add_argument(
            "--annotation_text", dest="java_text", metavar="ANNOTATION_TEXT",
            help="Content for a text annotation (requires namespace) (**)")
        java_group.add_argument(
            "--annotation_link", dest="java_link", metavar="ANNOTATION_LINK",
            help="Comment annotation ID to link all images to (**)")

        parser.add_argument(
            "path", nargs="*",
            help="Path to be passed to the Java process")
        parser.set_defaults(func=self.importer)

    def importer(self, args):

        client_dir = self.ctx.dir / "lib" / "client"
        etc_dir = self.ctx.dir / "etc"
        xml_file = etc_dir / "logback-cli.xml"
        logback = "-Dlogback.configurationFile=%s" % xml_file
        classpath = [file.abspath() for file in client_dir.files("*.jar")]
        xargs = [logback, "-Xmx1024M", "-cp", os.pathsep.join(classpath)]

        # Here we permit passing ---file=some_output_file in order to
        # facilitate the omero.util.import_candidates.as_dictionary
        # call. This may not always be necessary.
        out = args.file
        err = args.errs

        if out:
            out = open(out, "w")
        if err:
            err = open(err, "w")

        login_args = []
        if args.javahelp:
                login_args.append("-h")

        if "-h" not in login_args and "-f" not in login_args \
                and not args.java_f:
            client = self.ctx.conn(args)
            srv = client.getProperty("omero.host")
            prt = client.getProperty("omero.port")
            login_args.extend(["-s", srv])
            login_args.extend(["-p", prt])
            login_args.extend(["-k", client.getSessionId()])

        # Due to the use of "--" some of these like debug
        # will never be filled out. But for completeness
        # sake, we include them here.
        java_args = {
            "java_f": "-f",
            "java_c": "-c",
            "java_l": "-l",
            "java_d": "-d",
            "java_r": "-r",
            "java_r": "-r",
            "java_n": "-n",
            "java_x": "-x",
            "java_report": "--report",
            "java_upload": "--upload",
            "java_logs": "--logs",
            "java_email": "--email",
            "java_debug": "--debug",
            "java_ns": "--annotation_ns",
            "java_text": "--annotation_text",
            "java_link": "--annotation_link",
            }

        for attr_name, arg_name in java_args.items():
            arg_value = getattr(args, attr_name)
            if arg_value:
                login_args.append(arg_name)
                if isinstance(arg_value, (str, unicode)):
                    login_args.append(arg_value)

        a = self.COMMAND + login_args + args.path
        p = omero.java.popen(
            a, debug=False, xargs=xargs, stdout=out, stderr=err)
        self.ctx.rv = p.wait()


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
