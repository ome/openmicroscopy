#!/usr/bin/env python
"""
   Group administration plugin

   Copyright 2009 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import os
import sys

from omero.cli import BaseControl, CLI

HELP="""Group administration methods"""

class GroupControl(BaseControl):

    def _configure(self, parser):
        sub = parser.sub()
        add = parser.add(sub, self.add, "Add a new group with given permissions")
        add.add_argument("name", help="ExperimenterGroup.name value")

        group = add.add_mutually_exclusive_group()
        group.add_argument("--perms", default="rw----", help="Group permissions set as string, e.g. 'rw----' ")
        group.add_argument("--type", default="private", help="Group permission set symbollically",
            choices=("private", "read-only", "collaborative"))

        list = parser.add(sub, self.list, "List current groups")

    def add(self, args):

        perms = args.perms
        if not perms:
            if args.type == "private":
                perms = "rw----"
            elif args.type == "read-only":
                perms = "rwr---"
            elif args.type == "collaborative":
                perms = "rwrw--"

        import omero, Ice
        from omero.rtypes import rstring
        from omero_model_PermissionsI import PermissionsI as Perms
        from omero_model_ExperimenterGroupI import ExperimenterGroupI as Grp

        c = self.ctx.conn(args)
        p = c.ic.getProperties()
        g = Grp()
        g.name = rstring(args.name)
        g.details.permissions = Perms(perms)
        admin = c.getSession().getAdminService()
        id = admin.createGroup(g)
        self.ctx.out("Added group %s with permissions %s" % (id, perms))

    def list(self, args):
        c = self.ctx.conn(args)
        groups = c.sf.getAdminService().lookupGroups()
        from omero.util.text import TableBuilder
        tb = TableBuilder("id", "name", "perms", "owners", "members")
        for group in groups:
            row = [group.id.val, group.name.val, str(group.details.permissions)]
            row.append(len([x for x in group.copyGroupExperimenterMap() if x.owner.val]))
            row.append(len([x for x in group.copyGroupExperimenterMap() if not x.owner.val]))
            tb.row(*tuple(row))
        self.ctx.out(str(tb.build()))

try:
    register("group", GroupControl, HELP)
except NameError:
    if __name__ == "__main__":
        cli = CLI()
        cli.register("group", GroupControl, HELP)
        cli.invoke(sys.argv[1:])
