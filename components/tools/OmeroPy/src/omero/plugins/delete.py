#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
   Startup plugin for command-line deletes

   Copyright 2009-2015 Glencoe Software, Inc. All rights reserved.
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

EXCLUDED_PACKAGES = ["ome.model.display"]


class DeleteControl(GraphControl):

    def cmd_type(self):
        import omero
        import omero.all
        return omero.cmd.Delete2

    def print_detailed_report(self, req, rsp, status):
        import omero
        if isinstance(rsp, omero.cmd.DoAllRsp):
            for response in rsp.responses:
                if isinstance(response, omero.cmd.Delete2Response):
                    self.print_delete_response(response)
        elif isinstance(rsp, omero.cmd.Delete2Response):
            self.print_delete_response(rsp)

    def print_delete_response(self, rsp):
        self.ctx.out("Deleted objects")
        objIds = self._get_object_ids(rsp)
        for k in objIds:
            self.ctx.out("%s:%s" % (k, objIds[k]))

    def _get_object_ids(self, rsp):
        import collections
        objIds = {}
        for k in rsp.deletedObjects.keys():
            if rsp.deletedObjects[k]:
                for excl in EXCLUDED_PACKAGES:
                    if not k.startswith(excl):
                        ids = ','.join(map(str, rsp.deletedObjects[k]))
                        objIds[k] = ids
        newIds = collections.OrderedDict(sorted(objIds.items()))
        objIds = collections.OrderedDict()
        for k in newIds:
            key = k[k.rfind('.')+1:]
            objIds[key] = newIds[k]
        return objIds

try:
    register("delete", DeleteControl, HELP)
except NameError:
    if __name__ == "__main__":
        cli = CLI()
        cli.register("delete", DeleteControl, HELP)
        cli.invoke(sys.argv[1:])
