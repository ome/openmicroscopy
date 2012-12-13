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
defaultperms = {'private':'rw----',
'read-only': 'rwr---',
'read-annotate': 'rwra--'}

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

        parser.add_login_arguments()
        sub = parser.sub()
        add = parser.add(sub, self.add, "Add a new group with given permissions " + PERM_TXT)
        add.add_argument("--ignore-existing", action="store_true", default=False, help="Do not fail if user already exists")
        add.add_argument("name", help="ExperimenterGroup.name value")

        perms = parser.add(sub, self.perms, "Modify a group's permissions " + PERM_TXT)
        perms.add_argument("id_or_name", help="ExperimenterGroup's id or name")

        for x in (add, perms):
            group = x.add_mutually_exclusive_group()
            group.add_argument("--perms", help="Group permissions set as string, e.g. 'rw----' ")
            group.add_argument("--type", help="Group permission set symbollically", default="private", choices = defaultperms.keys())

        list = parser.add(sub, self.list, "List current groups")
        printgroup = list.add_mutually_exclusive_group()
        printgroup.add_argument("--count", action = "store_true", help = "Print count of all users and owners (default)", default = True)
        printgroup.add_argument("--long", action = "store_true", help = "Print comma-separated list of all users and owners", default = False)
        sortgroup = list.add_mutually_exclusive_group()
        sortgroup.add_argument("--sort-by-id", action = "store_true", default = True, help = "Sort groups by ID (default)")
        sortgroup.add_argument("--sort-by-name", action = "store_true", default = False, help = "Sort groups by name")

        copyusers = parser.add(sub, self.copyusers, "Copy the users of one group to another group")
        copyusers.add_argument("from_group", help = "ID or name of the source group whose users will be copied")
        copyusers.add_argument("to_group", help = "ID or name of the target group which will have new users added")
        copyusers.add_argument("--as-owner", action="store_true", default=False, help="Copy the group owners only")

        adduser = parser.add(sub, self.adduser, "Add one or more users to a group")
        adduser.add_argument("USER", metavar="user", nargs="+", help = "ID or name of the user to add to the group")
        adduser.add_argument("--as-owner", action="store_true", default=False, help="Add the users as owners of the group")
        
        removeuser = parser.add(sub, self.removeuser, "Remove one or more users from a group")
        removeuser.add_argument("USER", metavar="user", nargs="+", help = "ID or name of the user to remove")
        removeuser.add_argument("--as-owner", action="store_true", default=False, help="Remove the users from the group owner list")
        
        for x in (adduser, removeuser):
            group = x.add_mutually_exclusive_group()
            group.add_argument("--id", metavar="group", help="ID of the group")
            group.add_argument("--name", metavar="group", help="Name of the group")
        
        for x in (add, perms, list, copyusers, adduser, removeuser):
            x.add_login_arguments()

    def parse_perms(self, args):
        from omero_model_PermissionsI import PermissionsI as Perms
        perms = getattr(args, "perms", None)
        if not perms:
            perms = defaultperms[args.type]
        try:
            return Perms(perms)
        except ValueError, ve:
            self.ctx.die(505, str(ve))

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
        g.details.permissions = perms
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
        if old_perms == str(perms):
            self.ctx.out("Permissions for group %s (id=%s) already %s" % (g.name.val, gid, perms))
        else:
            try:
                a.changePermissions(Grp(gid, False), perms)
                self.ctx.out("Changed permissions for group %s (id=%s) to %s" % (g.name.val, gid, perms))
            except omero.GroupSecurityViolation:
                import traceback
                self.ctx.dbg(traceback.format_exc())
                self.ctx.die(504, "Cannot change permissions for group %s (id=%s) to %s" % (g.name.val, gid, perms))

    def list(self, args):
        c = self.ctx.conn(args)
        groups = c.sf.getAdminService().lookupGroups()
        from omero.util.text import TableBuilder

        # Sort groups
        if args.sort_by_name:
            groups.sort(key=lambda x: x.name.val)
        elif args.sort_by_id:
            groups.sort(key=lambda x: x.id.val)

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
            return self.find_group_by_id(a, group, fatal = True)
        elif args.name:
            group = getattr(args, "name", None)
            return self.find_group_by_name(a, group, fatal = True)
        else:
            self.error_no_input_group(fatal = True)

    def list_users(self, a, users):

        uid_list = []
        for user in users:
            [uid, u] = self.find_user(a, user, fatal = False)
            if uid:
                uid_list.append(uid)
        if not uid_list:
            self.error_no_user_found(fatal = True)

        return uid_list

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
        f_gid, f_grp = self.find_group(a, args.from_group, fatal = True)
        t_gid, t_grp = self.find_group(a, args.to_group, fatal = True)

        if args.as_owner:
            uids = self.getownerids(f_grp)
        else:
            uids = self.getuserids(f_grp)
        uids = self.filter_users(uids, t_grp, args.as_owner, True)
        
        if args.as_owner:
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
        uids = self.list_users(a, args.USER)
        uids = self.filter_users(uids, group, args.as_owner, True)
        
        if args.as_owner:
            self.addownersbyid(a, group, uids)
        else:
            self.addusersbyid(a, group, uids)

    def removeuser(self, args):
        import omero
        c = self.ctx.conn(args)
        a = c.sf.getAdminService()
        group = self.parse_groupid(a, args)[1]
        uids = self.list_users(a, args.USER)
        uids = self.filter_users(uids, group, args.as_owner, False)
        
        if args.as_owner:
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
