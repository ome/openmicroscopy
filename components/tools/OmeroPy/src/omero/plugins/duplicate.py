#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
   Startup plugin for command-line duplicates

   Copyright 2009-2015 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import sys

from omero.cli import CLI, GraphControl

HELP = """Duplicate OMERO data.

Duplicate entire graphs of data based on the ID of the top-node.

Examples:

    # Duplicate a dataset
    omero duplicate Datset:50
    # Do the same reporting all the new duplicate objects
    omero duplicate Dataset:50 --report

    # Do a dry run of a duplicate reporting the outcome
    # if the duplicate had been run
    omero duplicate Dataset:53 --dry-run
    # Do a dry run of a duplicate, reporting all the objects
    # that would have been duplicated
    omero duplicate Dataset:53 --dry-run --report

"""


class DuplicateControl(GraphControl):

    def cmd_type(self):
        import omero
        import omero.all
        return omero.cmd.Duplicate

    def print_detailed_report(self, req, rsp, status):
        import omero
        if isinstance(rsp, omero.cmd.DoAllRsp):
            for response in rsp.responses:
                if isinstance(response, omero.cmd.DuplicateResponse):
                    self.print_duplicate_response(response)
        elif isinstance(rsp, omero.cmd.DuplicateResponse):
            self.print_duplicate_response(rsp)

    def print_duplicate_response(self, rsp):
        if rsp.duplicates:
            self.ctx.out("Duplicates")
            objIds = self._get_object_ids(rsp.duplicates)
            for k in objIds:
                self.ctx.out("  %s:%s" % (k, objIds[k]))

try:
    register("duplicate", DuplicateControl, HELP)
except NameError:
    if __name__ == "__main__":
        cli = CLI()
        cli.register("duplicate", DuplicateControl, HELP)
        cli.invoke(sys.argv[1:])
