#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
   download plugin

   Plugin read by omero.cli.Cli during initialization. The method(s)
   defined here will be added to the Cli class for later use.

   Copyright 2007 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import sys
from omero.cli import BaseControl, CLI

HELP = """Download the given file id to the given filename"""


class DownloadControl(BaseControl):

    def _configure(self, parser):
        parser.add_argument("id", help="OriginalFile id")
        parser.add_argument(
            "filename", help="Local filename to be saved to. '-' for stdout")
        parser.set_defaults(func=self.__call__)
        parser.add_login_arguments()

    def __call__(self, args):
        from omero_model_OriginalFileI import OriginalFileI as OFile

        orig_file = OFile(long(args.id))
        target_file = str(args.filename)
        client = self.ctx.conn(args)
        if target_file == "-":
            client.download(orig_file, filehandle=sys.stdout)
            sys.stdout.flush()
        else:
            client.download(orig_file, target_file)

try:
    register("download", DownloadControl, HELP)
except NameError:
    if __name__ == "__main__":
        cli = CLI()
        cli.register("download", DownloadControl, HELP)
        cli.invoke(sys.argv[1:])
