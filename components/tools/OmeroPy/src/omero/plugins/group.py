#!/usr/bin/env python
"""
   Group administration plugin

   Copyright 2009 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import os
import sys

from omero.cli import BaseControl, CLI, ExceptionHandler

HELP="""Group administration methods"""

class GroupControl(BaseControl):

    def _configure(self, parser):

        self.exc = ExceptionHandler()

        sub = parser.sub()
        add = parser.add(sub, self.add, "Add a new group with given permissions")
        add.add_argument("--ignore-existing", action="store_true", default=False, help="Do not fail if user already exists")
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

        copy = parser.add(sub, self.copy, "Copy the members of one group to another group")
        copy.add_argument("from_group", help = "Source group ID or NAME whose members will be copied")
        copy.add_argument("to_group", help = "Target group ID or NAME which will have new members added")

        insert = parser.add(sub, self.insert, "Insert one or more users into a group")
        insert.add_argument("GROUP", metavar="group", help = "ID or NAME of the group which is to have users added")
        insert.add_argument("USER", metavar="user", nargs="+", help = "ID or NAME of user to be inserted")

        remove = parser.add(sub, self.remove, "remove one or more users from a group")
        remove.add_argument("GROUP", metavar="group", help = "ID or NAME of the group which is to have users removed")
        remove.add_argument("USER", metavar="user", nargs="+", help = "ID or NAME of user to be removed")

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
        try:
            grp = admin.lookupGroup(args.name)
            if grp:
                if args.ignore_existing:
                    self.ctx.out("Group exists: %s (id=%s)" % (args.name, grp.id.val))
                    return
                else:
                    self.ctx.die(3, "Group exists: %s (id=%s)" % (args.name, grp.id.val))
        except omero.ApiUsageException, aue:
            pass # Apparently no such group exists

        try:
            id = admin.createGroup(g)
            self.ctx.out("Added group %s (id=%s) with permissions %s" % (id, args.name, perms))
        except omero.ValidationException, ve:
            # Possible, though unlikely after previous check
            if self.exc.is_constraint_violation(ve):
                self.ctx.die(66, "Group already exists: %s" % args.name)
            else:
                self.ctx.die(67, "Unknown ValidationException: %s" % ve.message)
        except omero.SecurityViolation, se:
            self.ctx.die(68, "Security violation: %s" % se.message)
        except omero.ServerError, se:
            self.ctx.die(4, "%s: %s" % (type(se), se.message))

    def find_group(self, admin, id_or_name):
        import omero
        try:
            try:
                gid = long(id_or_name)
                g = admin.getGroup(gid)
            except ValueError:
                g = admin.lookupGroup(id_or_name)
                gid = g.id.val
            return gid, g
        except omero.ApiUsageException:
            self.ctx.die(503, "Unknown group: %s" % gid)

    def find_user(self, admin, id_or_name):
        import omero
        try:
            try:
                uid = long(id_or_name)
                u = admin.getExperimenter(uid)
            except ValueError:
                u = admin.lookupExperimenter(id_or_name)
                uid = u.id.val
            return uid, u
        except omero.ApiUsageException:
            self.ctx.die(503, "Unknown experimenter: %s" % uid)

    def perms(self, args):

        import omero
        from omero_model_ExperimenterGroupI import ExperimenterGroupI as Grp
        from omero_model_PermissionsI import PermissionsI as Perms

        perms = self.parse_perms(args)
        c = self.ctx.conn(args)
        a = c.sf.getAdminService()

        gid, g = self.find_group(admin, args.id_or_name)

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
                row.append(",".join(sorted([str(x.child.id.val) for x in group.copyGroupExperimenterMap() if x.owner.val])))
                row.append(",".join(sorted([str(x.child.id.val) for x in group.copyGroupExperimenterMap() if not x.owner.val])))
            else:
                row.append(len([x for x in group.copyGroupExperimenterMap() if x.owner.val]))
                row.append(len([x for x in group.copyGroupExperimenterMap() if not x.owner.val]))
            tb.row(*tuple(row))
        self.ctx.out(str(tb.build()))

    def copy(self, args):
        import omero
        c = self.ctx.conn(args)
        a = c.sf.getAdminService()
        f_gid, f_grp = self.find_group(a, args.from_group)
        t_gid, t_grp = self.find_group(a, args.to_group)

        to_add = [(x.child.id.val, x.child.omeName.val) for x in f_grp.copyGroupExperimenterMap()]
        already = [x.child.id.val for x in t_grp.copyGroupExperimenterMap()]
        for add in list(to_add):
            if add[0] in already:
                self.ctx.out("%s already in group %s" % (add[1], args.to_group))
                to_add.remove(add)
        self.addusersbyid(c, t_grp, [x[0] for x in to_add])
        self.ctx.out("%s copied to %s" % (args.from_group, args.to_group))

    def insert(self, args):
        import omero
        c = self.ctx.conn(args)
        a = c.sf.getAdminService()
        gid, grp = self.find_group(a, args.GROUP)
        uids = [self.find_user(a, x)[0] for x in args.USER]
        self.addusersbyid(c, grp, uids)

    def remove(self, args):
        import omero
        c = self.ctx.conn(args)
        a = c.sf.getAdminService()
        gid, grp = self.find_group(a, args.GROUP)
        uids = [self.find_user(a, x)[0] for x in args.USER]
        self.removeusersbyid(c, grp, uids)

    def addusersbyid(self, c, group, users):
        import omero
        a = c.sf.getAdminService()
        for add in list(users):
            a.addGroups(omero.model.ExperimenterI(add, False), [group])
            self.ctx.out("Added %s to group %s" % (add, group.id.val))

    def removeusersbyid(self, c, group, users):
        import omero
        a = c.sf.getAdminService()
        for add in list(users):
            a.removeGroups(omero.model.ExperimenterI(add, False), [group])
            self.ctx.out("Removed %s from group %s" % (add, group.id.val))

try:
    register("group", GroupControl, HELP)
except NameError:
    if __name__ == "__main__":
        cli = CLI()
        cli.register("group", GroupControl, HELP)
        cli.invoke(sys.argv[1:])
