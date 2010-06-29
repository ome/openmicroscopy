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

        perms = parser.add(sub, self.perms, "Modify a group's permissions")
        perms.add_argument("id_or_name", help="ExperimenterGroup's id or name")

        for x in (add, perms):
            group = x.add_mutually_exclusive_group()
            group.add_argument("--perms", help="Group permissions set as string, e.g. 'rw----' ")
            group.add_argument("--type", help="Group permission set symbollically",
                choices=("private", "read-only", "collaborative"))

        list = parser.add(sub, self.list, "List current groups")
        list.add_argument("--long", action="store_true", help = "Print comma-separated list of all groups, not just counts")

    def parse_perms(self, args):
        perms = getattr(args, "perms", None)
        if not perms:
            if args.type == "private":
                perms = "rw----"
            elif args.type == "read-only":
                perms = "rwr---"
            elif args.type == "collaborative":
                perms = "rwrw--"
        if not perms:
            perms = "rw----"
        return perms

    def add(self, args):

        import omero, Ice
        from omero.rtypes import rstring
        from omero_model_PermissionsI import PermissionsI as Perms
        from omero_model_ExperimenterGroupI import ExperimenterGroupI as Grp

        perms = self.parse_perms(args)
        c = self.ctx.conn(args)
        p = c.ic.getProperties()
        g = Grp()
        g.name = rstring(args.name)
        g.details.permissions = Perms(perms)
        admin = c.getSession().getAdminService()
        id = admin.createGroup(g)
        self.ctx.out("Added group %s (id=%s) with permissions %s" % (id, args.name, perms))

    def perms(self, args):

        import omero
        from omero_model_ExperimenterGroupI import ExperimenterGroupI as Grp
        from omero_model_PermissionsI import PermissionsI as Perms

        perms = self.parse_perms(args)
        c = self.ctx.conn(args)
        a = c.sf.getAdminService()

        try:
            try:
                gid = long(args.id_or_name)
                g = a.getGroup(gid)
            except ValueError:
                g = a.lookupGroup(args.id_or_name)
                gid = g.id.val
        except omero.ApiUsageException:
            self.ctx.die(503, "Unknown group: %s" % gid)


        old_perms = str(g.details.permissions)
        if old_perms == perms:
            self.ctx.out("Permissions for group %s (id=%s) already %s" % (g.name.val, gid, perms))
        else:
            try:
                a.changePermissions(Grp(gid, False), Perms(perms))
                self.ctx.out("Changed permissions for group %s (id=%s) to %s" % (g.name.val, gid, perms))
            except omero.GroupSecurityViolation:
                import traceback
                self.ctx.dbg(traceback.format_exc())
                self.ctx.die(504, "Cannot change permissions for group %s (id=%s) to %s" % (g.name.val, gid, perms))


    def list(self, args):
        c = self.ctx.conn(args)
        groups = c.sf.getAdminService().lookupGroups()
        from omero.util.text import TableBuilder
        if args.long:
            tb = TableBuilder("id", "name", "perms", "owner ids", "member ids")
        else:
            tb = TableBuilder("id", "name", "perms", "# of owners", "# of members")
        for group in groups:
            row = [group.id.val, group.name.val, str(group.details.permissions)]
            if args.long:
                row.append(",".join([str(x.child.id.val) for x in group.copyGroupExperimenterMap() if x.owner.val]))
                row.append(",".join([str(x.child.id.val) for x in group.copyGroupExperimenterMap() if not x.owner.val]))
            else:
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
