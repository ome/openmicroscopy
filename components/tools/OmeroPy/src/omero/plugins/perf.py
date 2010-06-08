#!/usr/bin/env python
"""
   Plugin for measuring the performance of an OMERO
   installation.

   Plugin read by omero.cli.Cli during initialization. The method(s)
   defined here will be added to the Cli class for later use.

   Copyright 2008 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import sys
from omero.cli import BaseControl, CLI

PROG_NAME = "%s perf" % sys.argv[0]
HELP = "Run perf_test files"

class PerfControl(BaseControl):

    def _configure(self, parser):
        parser.add_argument("--longhelp", action="store_true", help = "Prints help from perf_test.main")
        parser.add_argument("arg", nargs="*", help = "Arguments to be passed to perf_test.main")
        parser.set_defaults(func=self.__call__)

    def __call__(self, args):
	import omero.install.perf_test as perf_test
        if args.longhelp:
            self.ctx.out(perf_test.usage(prog = PROG_NAME))
            return

        perf_test.main(args.arg, prog = PROG_NAME)

try:
    register("perf", PerfControl, HELP)
except NameError:
    if __name__ == "__main__":
        cli = CLI()
        cli.register("perf", PerfControl, HELP)
        cli.invoke(sys.argv[1:])
