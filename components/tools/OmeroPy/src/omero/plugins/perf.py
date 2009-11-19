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
from omero.cli import Arguments, BaseControl, VERSION

PROG_NAME = "%s perf" % sys.argv[0]

class PerfControl(BaseControl):

    def help(self, args = None):
        self.ctx.out(perf_test.usage(prog = PROG_NAME))

    def __call__(self, *args):
        import omero.install.perf_test as perf_test
        args = Arguments(*args)

        first,other = args.firstOther()
        if first == "help":
            self.help()
            return

	import omero.install.perf_test as perf_test
        perf_test.main(args.args, prog = PROG_NAME)

try:
    register("perf", PerfControl)
except NameError:
    PerfControl()._main()
