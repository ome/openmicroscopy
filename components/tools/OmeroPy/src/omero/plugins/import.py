#!/usr/bin/env python
"""
   Startup plugin for command-line importer.

   Copyright 2009 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import subprocess, optparse, os, sys, signal, time
from omero.cli import Arguments, BaseControl, VERSION
import omero.java

START_CLASS="ome.formats.importer.cli.CommandLineImporter"

class ImportControl(BaseControl):

    def _run(self, args = []):
        args = Arguments(args)
        client_dir = self.ctx.dir / "lib" / "client"
        log4j = "-Dlog4j.configuration=%s" % ( client_dir / "log4j.properties" )
        classpath = [ file.abspath() for file in client_dir.files("*.jar") ]
        xargs = [ log4j, "-Xmx256M", "-cp", os.pathsep.join(classpath) ]
        command = [ START_CLASS ] + args.args
        omero.java.run(command, debug=False, xargs = xargs, use_exec = False)

    def help(self, args = None):
        self._run() # Prints help by default

    def __call__(self, *args):
        args = Arguments(*args)
        self._run(args)

try:
    register("import", ImportControl)
except NameError:
    ServerControl()._main()
