#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
   Plugin for measuring the performance of an OMERO
   installation.

   Plugin read by omero.cli.Cli during initialization. The method(s)
   defined here will be added to the Cli class for later use.

   Copyright 2008, 2016 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import sys
from omero.cli import BaseControl, CLI
from omero_ext.argparse import FileType
import omero.install.perf_test as perf_test

HELP = """Run perf_test files

%s

""" % perf_test.FILE_FORMAT


class PerfControl(BaseControl):

    def _configure(self, parser):
        parser.add_argument(
            "-l", "--list", action="store_true",
            help="List available commands")
        parser.add_argument(
            "file", nargs="*", type=FileType('r'), default=None,
            help="Read from files or standard in")
        parser.set_defaults(func=self.__call__)
        parser.add_login_arguments()

    def __call__(self, args):
        if args.list:
            ops = [x[4:] for x in dir(perf_test.Item) if x.startswith("_op_")]
            ops.sort()
            for op in ops:
                print op
        else:
            if not args.file:
                self.ctx.die(167, "No files given. Use '-' for stdin.")
            client = self.ctx.conn(args)
            ctx = perf_test.Context(None, client=client)
            self.ctx.out("Saving performance results to %s" % ctx.dir)
            ctx.add_reporter(perf_test.CsvReporter(ctx.dir))
            # ctx.add_reporter(perf_test.HdfReporter(ctx.dir))
            # ctx.add_reporter(perf_test.PlotReporter())
            handler = perf_test.PerfHandler(ctx)
            perf_test.handle(handler, args.file)

try:
    register("perf", PerfControl, HELP)
except NameError:
    if __name__ == "__main__":
        cli = CLI()
        cli.register("perf", PerfControl, HELP)
        cli.invoke(sys.argv[1:])
