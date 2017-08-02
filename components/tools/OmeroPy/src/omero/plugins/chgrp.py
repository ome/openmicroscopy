#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2011-2015 Glencoe Software, Inc. All Rights Reserved.
# Use is subject to license terms supplied in LICENSE.txt
#

"""
   chgrp plugin

   Plugin read by omero.cli.Cli during initialization. The method(s)
   defined here will be added to the Cli class for later use.
"""

from omero.cli import CLI, GraphControl, ExperimenterGroupArg
import sys

HELP = """Move data between groups

Move entire graphs of data based on the ID of the top-node.

Examples:

    # In each case move an image to group 101
    omero chgrp 101 Image:1
    omero chgrp Group:101 Image:2
    omero chgrp ExperimenterGroup:101 Image:3
    # In both cases move five images to the group named "My Lab"
    omero chgrp "My Lab" Image:51,52,53,54,56
    omero chgrp "My Lab" Image:51-54,56

    # Move a plate but leave all images in the original group
    omero chgrp 201 Plate:1 --exclude Image

    # Move all images contained under a project
    omero chgrp 101 Project/Dataset/Image:53
    # Move all images contained under two projects
    omero chgrp 101 Project/Image:201,202

    # Do a dry run of a move reporting the outcome if the move had been run
    omero chgrp 101 Dataset:53 --dry-run
    # Do a dry run of a move, reporting all the objects
    # that would have been moved
    omero chgrp 101 Dataset:53 --dry-run --report

"""


class ChgrpControl(GraphControl):

    def cmd_type(self):
        import omero
        import omero.all
        return omero.cmd.Chgrp2

    def _pre_objects(self, parser):
        parser.add_argument(
            "grp", nargs="?", type=ExperimenterGroupArg,
            help="""Group to move objects to""")

    def is_admin(self, client):
        # check if the user currently logged is an admin
        from omero.model.enums import AdminPrivilegeChgrp
        ec = self.ctx.get_event_context()
        return AdminPrivilegeChgrp in ec.adminPrivileges

    def _process_request(self, req, args, client):
        # Retrieve group id
        gid = args.grp.lookup(client)
        if gid is None:
            self.ctx.die(196, "Failed to find group: %s" % args.grp.orig)

        # Retrieve group
        import omero
        try:
            group = client.sf.getAdminService().getGroup(gid)
        except omero.ApiUsageException:
            self.ctx.die(196, "Failed to find group: %s" % args.grp.orig)

        # Check session owner is member of the target group
        uid = self.ctx.get_event_context().userId
        admin = self.is_admin(client)
        ids = [x.child.id.val for x in group.copyGroupExperimenterMap()]
        # check if the user is an admin
        if uid not in ids and not admin:
            self.ctx.die(197, "Current user is not member of group: %s" %
                         group.id.val)

        # Set requests group
        if isinstance(req, omero.cmd.DoAll):
            for request in req.requests:
                if isinstance(request, omero.cmd.SkipHead):
                    request.request.groupId = gid
                else:
                    request.groupId = gid
        else:
            if isinstance(req, omero.cmd.SkipHead):
                req.request.groupId = gid
            else:
                req.groupId = gid

        super(ChgrpControl, self)._process_request(req, args, client)

    def print_detailed_report(self, req, rsp, status):
        import omero
        if isinstance(rsp, omero.cmd.DoAllRsp):
            for response in rsp.responses:
                if isinstance(response, omero.cmd.Chgrp2Response):
                    self.print_chgrp_response(response)
        elif isinstance(rsp, omero.cmd.Chgrp2Response):
            self.print_chgrp_response(rsp)

    def print_chgrp_response(self, rsp):
        if rsp.includedObjects:
            self.ctx.out("Included objects")
            obj_ids = self._get_object_ids(rsp.includedObjects)
            for k in obj_ids:
                self.ctx.out("  %s:%s" % (k, obj_ids[k]))
        if rsp.deletedObjects:
            self.ctx.out("Deleted objects")
            obj_ids = self._get_object_ids(rsp.deletedObjects)
            for k in obj_ids:
                self.ctx.out("  %s:%s" % (k, obj_ids[k]))

try:
    register("chgrp", ChgrpControl, HELP)
except NameError:
    if __name__ == "__main__":
        cli = CLI()
        cli.register("chgrp", ChgrpControl, HELP)
        cli.invoke(sys.argv[1:])
