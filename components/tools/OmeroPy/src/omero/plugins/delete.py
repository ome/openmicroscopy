#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
   Startup plugin for command-line deletes

   Copyright 2009 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import sys

from omero.cli import CLI, GraphControl

HELP = """Delete OMERO data.

Remove entire graphs of data based on the ID of the top-node.

Examples:

    bin/omero delete --list   # Print all of the graphs

    bin/omero delete Image:50
    bin/omero delete Plate:1
    bin/omero delete Image:51,52 OriginalFile:101
    bin/omero delete Project:101 --exclude Dataset,Image

"""


class DeleteControl(GraphControl):

    def cmd_type(self):
        import omero
        import omero.all
        return omero.cmd.Delete2

    def print_detailed_report(self, req, rsp, status):
        import omero
        if isinstance(rsp, omero.cmd.DeleteRsp):
            for k, v in rsp.undeletedFiles.items():
                if v:
                    self.ctx.out("Undeleted %s objects" % k)
                    for i in v:
                        self.ctx.out("%s:%s" % (k, i))

            self.ctx.out("Scheduled deletes: %s" % rsp.scheduledDeletes)
            self.ctx.out("Actual deletes: %s" % rsp.actualDeletes)
            if rsp.warning:
                self.ctx.out("Warning message: %s" % rsp.warning)
            self.ctx.out(" ")

try:
    register("delete", DeleteControl, HELP)
except NameError:
    if __name__ == "__main__":
        cli = CLI()
        cli.register("delete", DeleteControl, HELP)
        cli.invoke(sys.argv[1:])
