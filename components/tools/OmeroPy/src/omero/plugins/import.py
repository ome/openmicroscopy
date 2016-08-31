#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
   Startup plugin for command-line importer.

   Copyright 2009 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import os
import sys
from omero.cli import BaseControl, CLI
import omero.java

START_CLASS = "ome.formats.importer.cli.CommandLineImporter"
TEST_CLASS = "ome.formats.test.util.TestEngine"

HELP = """Run the Java-based command-line importer

This is a Python wrapper around the Java importer. Login is handled by Python
OmeroCli. To see more options, use "--javahelp".

Options marked with "**" are passed strictly to Java. If they interfere with
any of the Python arguments, you may need to end precede your arguments with a
"--".

Examples:

  bin/omero import ~/Data/my_file.dv                    # Use current login
  bin/omero import -- --debug=ALL ~/Data/my_file2.png   # Set Java debug

"""
TESTHELP = """Run the Importer TestEngine suite (devs-only)"""


class ImportControl(BaseControl):

    COMMAND = [START_CLASS]

    def _configure(self, parser):
        parser.add_argument(
            "--javahelp", action="store_true", help="Show the Java help text")
        parser.add_argument(
            "---file", nargs="?",
            help="File for storing the standard out of the Java process")
        parser.add_argument(
            "---errs", nargs="?",
            help="File for storing the standard err of the Java process")
        # The following arguments are strictly passed to Java
        parser.add_argument(
            "-a", dest="java_a", action="store_true",
            help="Archive original files (**)")
        parser.add_argument(
            "-f", dest="java_f", action="store_true",
            help="Display used files (**)")
        parser.add_argument(
            "-c", dest="java_c", action="store_true",
            help="Continue importing after errors (**)")
        parser.add_argument(
            "-l", dest="java_l",
            help="Use the list of readers rather than the default (**)",
            metavar="READER_FILE")
        parser.add_argument(
            "-d", dest="java_d",
            help="OMERO dataset Id to import image into (**)",
            metavar="DATASET_ID")
        parser.add_argument(
            "-r", dest="java_r",
            help="OMERO screen Id to import plate into (**)",
            metavar="SCREEN_ID")
        parser.add_argument(
            "-n", dest="java_n",
            help="Image name to use (**)",
            metavar="NAME")
        parser.add_argument(
            "-x", dest="java_x",
            help="Image description to use (**)",
            metavar="DESCRIPTION")
        parser.add_argument(
            "--report", action="store_true", dest="java_report",
            help="Report errors to the OME team (**)")
        parser.add_argument(
            "--upload", action="store_true", dest="java_upload",
            help="Upload broken files with report (**)")
        parser.add_argument(
            "--logs", action="store_true", dest="java_logs",
            help="Upload log file with report (**)")
        parser.add_argument(
            "--email", dest="java_email",
            help="Email for reported errors (**)", metavar="EMAIL")
        parser.add_argument(
            "--debug", dest="java_debug",
            help="Turn debug logging on (**; must be preceded by '--')",
            choices=["ALL", "DEBUG", "ERROR", "FATAL", "INFO", "TRACE",
                     "WARN"],
            metavar="LEVEL")

        parser.add_argument(
            "--annotation_ns", dest="java_ns",
            help="Namespace to use for subsequent annotation")
        parser.add_argument(
            "--annotation_text", dest="java_text",
            help="Content for a text annotation (requires namespace)")
        parser.add_argument(
            "--annotation_link", dest="java_link",
            help="Comment annotation ID to link all images to")

        parser.add_argument(
            "arg", nargs="*",
            help="Arguments to be passed to the Java process")
        parser.set_defaults(func=self.importer)
        parser.add_login_arguments()

    def importer(self, args):

        client_dir = self.ctx.dir / "lib" / "client"
        log4j = "-Dlog4j.configuration=log4j-cli.properties"
        classpath = [file.abspath() for file in client_dir.files("*.jar")]
        xargs = [log4j, "-Xmx1024M", "-cp", os.pathsep.join(classpath)]

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
            "java_a": "-a",
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
            "java_link": "--annotation_link"
            }

        for attr_name, arg_name in java_args.items():
            arg_value = getattr(args, attr_name)
            if arg_value:
                login_args.append(arg_name)
                if isinstance(arg_value, (str, unicode)):
                    if arg_name[:2] == "--":
                        login_args[-1] += "=" + arg_value
                    else:
                        login_args.append(arg_value)

        a = self.COMMAND + login_args + args.arg
        p = omero.java.popen(
            a, debug=False, xargs=xargs, stdout=out, stderr=err)
        self.ctx.rv = p.wait()


class TestEngine(ImportControl):
    COMMAND = [TEST_CLASS]

try:
    register("import", ImportControl, HELP)
    register("testengine", TestEngine, TESTHELP)
except NameError:
    if __name__ == "__main__":
        cli = CLI()
        cli.register("import", ImportControl, HELP)
        cli.register("testengine", TestEngine, TESTHELP)
        cli.invoke(sys.argv[1:])
