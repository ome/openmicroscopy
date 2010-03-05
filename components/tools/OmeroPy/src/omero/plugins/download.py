#!/usr/bin/env python
"""
   download plugin

   Plugin read by omero.cli.Cli during initialization. The method(s)
   defined here will be added to the Cli class for later use.

   Copyright 2007 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

from omero.cli import Arguments, BaseControl

class DownloadControl(BaseControl):

    def help(self, args = None):
        self.ctx.out(
        """
Syntax: %(program_name)s download <id> <filename>
        Download the given file id to the given file name
        """ )

    def __call__(self, *args):
        args = Arguments(args)
        from omero_model_OriginalFileI import OriginalFileI as OFile
        if len(args) != 2:
            self.help()
            self.ctx.die(2, "")

        orig_file = OFile(long(args.args[0]))
        target_file = str(args.args[1])

        client = self.ctx.conn(args)
        client.download(orig_file, target_file)

try:
    register("download", DownloadControl)
except NameError:
    DownloadControl()._main()
