#!/usr/bin/env python
"""
   Startup plugin for command-line importer.

   Copyright 2009 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import subprocess, optparse, os, sys, signal, time
from omero.cli import BaseControl, CLI, OMERODIR
import omero.java

START_CLASS="ome.formats.importer.cli.CommandLineImporter"
TEST_CLASS="ome.formats.test.util.TestEngine"

HELP = """Run the Java-based command-line importer

This is a Python wrapper around the Java importer. Login is handled
by Python OmeroCli. To see more options, use "--longhelp".

Examples:

  bin/omero login ~/Data/my_file.dv                    # Use current login
  bin/omero login -- --debug=ALL ~/Data/my_file2.png   # Set Java debug

"""
TESTHELP = """Run the Importer TestEngine suite (devs-only)"""

class ImportControl(BaseControl):

    COMMAND = [START_CLASS]

    def _configure(self, parser):
        parser.add_argument("--longhelp", action="store_true", help="Show the Java help text")
        parser.add_argument("---file", nargs="?", help="File for storing the standard out of the Java process")
        parser.add_argument("---errs", nargs="?", help="File for storing the standard err of the Java process")
        parser.add_argument("arg", nargs="*", help="Arguments to be passed to the Java process")
        parser.set_defaults(func=self.importer)

    def importer(self, args):

        client_dir = self.ctx.dir / "lib" / "client"
        log4j = "-Dlog4j.configuration=log4j-cli.properties"
        classpath = [ file.abspath() for file in client_dir.files("*.jar") ]
        xargs = [ log4j, "-Xmx1024M", "-cp", os.pathsep.join(classpath) ]

        # Here we permit passing ---file=some_output_file in order to
        # facilitate the omero.util.import_candidates.as_dictionary
        # call. This may not always be necessary.
        out = args.file
	err = args.errs

        if out:
            args.args.remove(out)
            out = open(out, "w")
        if err:
            args.args.remove(err)
            err = open(err, "w")

        need_login = not args.longhelp
        if "-f" in args.arg:
            need_login = False

        login_args = []
        if need_login:
            client = self.ctx.conn(args)
            srv = client.getProperty("omero.host")
            login_args.extend(["-s", srv])
            login_args.extend(["-k", client.getSessionId()])

        a = self.COMMAND + login_args + args.arg
        p = omero.java.popen(a, debug=False, xargs = xargs, stdout=out, stderr=err)
        self.ctx.rv = p.wait()

class TestEngine(ImportControl):
    COMMAND = [ TEST_CLASS ]

try:
    register("import", ImportControl, HELP)
    register("testengine", TestEngine, TESTHELP)
except NameError:
    if __name__ == "__main__":
        cli = CLI()
        cli.register("import", ImportControl, HELP)
        cli.register("testengine", TestEngine, TESTHELP)
        cli.invoke(sys.argv[1:])
