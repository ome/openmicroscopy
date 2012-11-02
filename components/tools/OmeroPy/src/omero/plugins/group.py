#!/usr/bin/env python
"""
   Group administration plugin

   Copyright 2009 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import os
import sys

from omero.cli import UserGroupControl, CLI, ExceptionHandler

HELP="""Group administration methods"""

class GroupControl(UserGroupControl):

    def _configure(self, parser):

        self.exc = ExceptionHandler()

        PERM_TXT = """

Group permissions come in several styles:

    * private       (rw----)   [DEFAULT]
    * read-only     (rwr---)
    * read-annotate (rwra--)   [Previously known as 'collaborative']

In private groups, only group and system administrators will be able
to view someone else's data. In read-only groups, other group members
can see data but not annotate or modify it. In read-annotate groups,
annotation is permitted by group members.

More information is available at:

    https://www.openmicroscopy.org/site/support/omero4/sysadmins/server-permissions.html
        """

        OWNER_TXT = """

Your group may have one or more owners. The group owner has some additional
rights within each group than a standard group member, including the ability
to add other members to the group.

More information is available at:

    https://www.openmicroscopy.org/site/support/omero4/sysadmins/server-permissions.html
        """

        sub = parser.sub()
        add = parser.add(sub, self.add, "Add a new group with given permissions " + PERM_TXT)
        add.add_argument("--ignore-existing", action="store_true", default=False, help="Do not fail if user already exists")
        add.add_argument("name", help="ExperimenterGroup.name value")

        perms = parser.add(sub, self.perms, "Modify a group's permissions " + PERM_TXT)
        perms.add_argument("id_or_name", help="ExperimenterGroup's id or name")

        for x in (add, perms):
            group = x.add_mutually_exclusive_group()
            group.add_argument("--perms", help="Group permissions set as string, e.g. 'rw----' ")
            group.add_argument("--type", help="Group permission set symbollically", default="private",
                choices=("private", "read-only", "read-annotate", "collaborative"))

        list = parser.add(sub, self.list, "List current groups")
        list.add_argument("--long", action="store_true", help = "Print comma-separated list of all groups, not just counts")

        copyusers = parser.add(sub, self.copyusers, "Copy the users of one group to another group")
        copyusers.add_argument("from_group", help = "ID or name of the source group whose users will be copied")
        copyusers.add_argument("to_group", help = "ID or name of the target group which will have new users added")
        copyusers.add_argument("--owner", action="store_true", default=False, help="Copy the group owners only")

        adduser = parser.add(sub, self.adduser, "Add one or more users to a group")
        adduser.add_argument("USER", metavar="user", nargs="+", help = "ID or name of the user to add to the group")
        adduser.add_argument("--owner", action="store_true", default=False, help="Add the users as owners of the group")
        
        removeuser = parser.add(sub, self.removeuser, "Remove one or more users from a group")
        removeuser.add_argument("USER", metavar="user", nargs="+", help = "ID or name of the user to remove")
        removeuser.add_argument("--owner", action="store_true", default=False, help="Remove the users from the group owner list")
        
        for x in (adduser, removeuser):
            group = x.add_mutually_exclusive_group()
            group.add_argument("--id", metavar="group", help="ID of the group")
            group.add_argument("--name", metavar="group", help="Name of the group")
        

    def parse_perms(self, args):
        perms = getattr(args, "perms", None)
        if not perms:
            if args.type == "private":
                perms = "rw----"
            elif args.type == "read-only":
                perms = "rwr---"
            elif args.type in ("read-annotate", "collaborative"):
                perms = "rwra--"
            elif args.type == "read-write":
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

    def perms(self, args):

        import omero
        from omero_model_ExperimenterGroupI import ExperimenterGroupI as Grp
        from omero_model_PermissionsI import PermissionsI as Perms

        perms = self.parse_perms(args)
        c = self.ctx.conn(args)
        a = c.sf.getAdminService()

        gid, g = self.find_group(a, args.id_or_name)

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
            ownerids = self.getownerids(group)
            memberids = self.getmemberids(group)
            if args.long:
                row.append(",".join(sorted([str(x) for x in ownerids])))
                row.append(",".join(sorted([str(x) for x in memberids])))
            else:
                row.append(len(ownerids))
                row.append(len(memberids))
            tb.row(*tuple(row))
        self.ctx.out(str(tb.build()))


    def parse_groupid(self, a, args):
        if args.id:
            group = getattr(args, "id", None)
        elif args.name == "name":
            group = getattr(args, "name", None)
        else:
            self.ctx.die(503, "No group specified")
        return self.find_group(a, group)

    def filter_users(self, uids, group, owner = False, join = True):

        if owner:
            uid_list = self.getownerids(group)
            relation = "owner of"
        else:
            uid_list = self.getuserids(group)
            relation = "in"
            
        for uid in list(uids):            
            if join:
                if uid in uid_list:
                    self.ctx.out("%s is already %s group %s" % (uid, relation, group.id.val))
                    uids.remove(uid)
            else:
                if uid not in uid_list:
                    self.ctx.out("%s is not %s group %s" % (uid, relation, group.id.val))
                    uids.remove(uid)
        return uids

    def copyusers(self, args):
        import omero
        c = self.ctx.conn(args)
        a = c.sf.getAdminService()
        f_gid, f_grp = self.find_group(a, args.from_group)
        t_gid, t_grp = self.find_group(a, args.to_group)

        if args.owner:
            uids = self.getownerids(f_grp)
        else:
            uids = self.getuserids(f_grp)
        uids = self.filter_users(uids, t_grp, args.owner, True)
        
        if args.owner:
            self.addownersbyid(a, t_grp, uids)
            self.ctx.out("Owners of %s copied to %s" % (args.from_group, args.to_group))
        else:
            self.addusersbyid(a, t_grp, uids)
            self.ctx.out("Users of %s copied to %s" % (args.from_group, args.to_group))

    def adduser(self, args):
        import omero
        c = self.ctx.conn(args)
        a = c.sf.getAdminService()
        group = self.parse_groupid(a, args)[1]
        uids = [self.find_user(a, x)[0] for x in args.USER]
        uids = self.filter_users(uids, group, args.owner, True)
        
        if args.owner:
            self.addownersbyid(a, group, uids)
        else:
            self.addusersbyid(a, group, uids)

    def removeuser(self, args):
        import omero
        c = self.ctx.conn(args)
        a = c.sf.getAdminService()
        group = self.parse_groupid(a, args)[1]
        uids = [self.find_user(a, x)[0] for x in args.USER]
        uids = self.filter_users(uids, group, args.owner, False)
        
        if args.owner:
            self.removeownersbyid(a, group, uids)
        else:
            self.removeusersbyid(a, group, uids)

try:
    register("group", GroupControl, HELP)
except NameError:
    if __name__ == "__main__":
        cli = CLI()
        cli.register("group", GroupControl, HELP)
        cli.invoke(sys.argv[1:])
