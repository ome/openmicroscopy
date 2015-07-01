#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2015 Glencoe Software, Inc. All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
#

"""
   chown plugin

   Plugin read by omero.cli.Cli during initialization. The method(s)
   defined here will be added to the Cli class for later use.
"""

from omero.cli import CLI, GraphControl, ExperimenterArg
import sys

HELP = """Transfer ownership of data between users. Entire graphs of data,
based on the ID of the top-node, are transferred.

Examples:

    # In each case transfer the ownership of an image to user 101
    omero chown 101 Image:1
    omero chown User:101 Image:2
    omero chown Experimenter:101 Image:3
    # Transfer three images to the user named jane
    omero chown jane Image:51,52,53

    # Transfer a plate but leave all images with the original owner
    omero chown 201 Plate:1 --exclude Image

    # Transfer all images contained under a project
    omero chown 101 Project/Dataset/Image:53
    # Transfer all images contained under two projects
    omero chown 101 Project/Image:201,202

    # Do a dry run of a transfer reporting the outcome
    # if the transfer had been run
    omero chown 101 Dataset:53 --dry-run
    # Do a dry run of a transfer, reporting all the objects
    # that would have been transferred
    omero chown 101 Dataset:53 --dry-run --report

"""


class ChownControl(GraphControl):

    def cmd_type(self):
        import omero
        import omero.all
        return omero.cmd.Chown2

    def _pre_objects(self, parser):
        parser.add_argument(
            "usr", nargs="?", type=ExperimenterArg,
            help="""user to transfer ownership of objects to""")

    def _process_request(self, req, args, client):
        # Retrieve user id
        uid = args.usr.lookup(client)
        if uid is None:
            self.ctx.die(196, "Failed to find user: %s" % args.usr.orig)

        # Set requests user
        import omero
        if isinstance(req, omero.cmd.DoAll):
            for request in req.requests:
                if isinstance(request, omero.cmd.SkipHead):
                    request.request.userId = uid
                else:
                    request.userId = uid
        else:
            if isinstance(req, omero.cmd.SkipHead):
                req.request.userId = uid
            else:
                req.userId = uid

        super(ChownControl, self)._process_request(req, args, client)

    def print_detailed_report(self, req, rsp, status):
        import omero
        if isinstance(rsp, omero.cmd.DoAllRsp):
            for response in rsp.responses:
                if isinstance(response, omero.cmd.Chown2Response):
                    self.print_chown_response(response)
        elif isinstance(rsp, omero.cmd.Chown2Response):
            self.print_chown_response(rsp)

    def print_chown_response(self, rsp):
        if rsp.includedObjects:
            self.ctx.out("Included objects")
            objIds = self._get_object_ids(rsp.includedObjects)
            for k in objIds:
                self.ctx.out("  %s:%s" % (k, objIds[k]))
        if rsp.deletedObjects:
            self.ctx.out("Deleted objects")
            objIds = self._get_object_ids(rsp.deletedObjects)
            for k in objIds:
                self.ctx.out("  %s:%s" % (k, objIds[k]))

try:
    register("chown", ChownControl, HELP)
except NameError:
    if __name__ == "__main__":
        cli = CLI()
        cli.register("chown", ChownControl, HELP)
        cli.invoke(sys.argv[1:])
