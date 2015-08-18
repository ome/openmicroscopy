#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
   Group administration plugin

   Copyright 2009-2015 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import sys

from omero.cli import UserGroupControl, CLI, ExceptionHandler, admin_only

HELP = """Group administration methods"""
defaultperms = {
    'private': 'rw----',
    'read-only': 'rwr---',
    'read-annotate': 'rwra--',
    'read-write': 'rwrw--'}


class GroupControl(UserGroupControl):

    def _configure(self, parser):

        self.exc = ExceptionHandler()

        PERM_TXT = """

Group permissions come in several styles:

    * private       (rw----)   [DEFAULT]
    * read-only     (rwr---)
    * read-annotate (rwra--)   [Previously known as 'collaborative']
    * read-write    (rwrw--)

In private groups, only group owners and system administrators will be
able to view someone else's data. In read-only groups, other group members
can see data but not annotate or modify it. In read-annotate groups,
annotation is permitted by group members. In read-write groups, all
group members can behave as if they co-own all the data.

Changing a group to private unlinks data from other users' containers and
unlinks other users' annotations from data. The change to private will
fail if different users' data is too closely related to be separated.

More information is available at:
http://www.openmicroscopy.org/site/support/omero5.1/sysadmins/\
server-permissions.html
        """

        parser.add_login_arguments()
        sub = parser.sub()
        add = parser.add(sub, self.add,
                         "Add a new group with given permissions " + PERM_TXT)
        add.add_argument(
            "--ignore-existing", action="store_true", default=False,
            help="Do not fail if user already exists")
        add.add_argument("name", help="Name of the group")
        self.add_permissions_arguments(add)

        perms = parser.add(sub, self.perms,
                           "Modify a group's permissions " + PERM_TXT)
        self.add_id_name_arguments(perms, "group")
        self.add_permissions_arguments(perms)

        list = parser.add(
            sub, self.list, help="List information about all groups")

        info = parser.add(
            sub, self.info,
            "List information about the group(s). Default to the context"
            " group")
        self.add_group_arguments(info)

        for x in (list, info):
            x.add_style_argument()
            x.add_user_print_arguments()
            x.add_group_sorting_arguments()

        listusers = parser.add(
            sub, self.listusers, "List users of the current group")
        self.add_group_arguments(listusers)
        listusers.add_style_argument()
        listusers.add_group_print_arguments()
        listusers.add_user_sorting_arguments()

        copyusers = parser.add(sub, self.copyusers, "Copy the users of one"
                               " group to another group")
        copyusers.add_argument("from_group", help="ID or name of the source"
                               " group whose users will be copied")
        copyusers.add_argument("to_group", help="ID or name of the target"
                               " group which will have new users added")
        copyusers.add_argument(
            "--as-owner", action="store_true",
            default=False, help="Copy the group owners only")

        adduser = parser.add(sub, self.adduser,
                             "Add one or more users to a group")
        self.add_id_name_arguments(adduser, "group")
        group = self.add_user_arguments(
            adduser, action=" to add to the group")
        group.add_argument("--as-owner", action="store_true", default=False,
                           help="Add the users as owners of the group")

        removeuser = parser.add(sub, self.removeuser,
                                "Remove one or more users from a group")
        self.add_id_name_arguments(removeuser, "group")
        group = self.add_user_arguments(
            removeuser, action=" to remove from the group")
        group.add_argument("--as-owner", action="store_true", default=False,
                           help="Remove the users from the group owner list")

        for x in (add, perms, list, copyusers, adduser, removeuser):
            x.add_login_arguments()

    def add_permissions_arguments(self, parser):
        group = parser.add_mutually_exclusive_group()
        group.add_argument(
            "--perms", help="Group permissions set as string, e.g. 'rw----' ")
        group.add_argument(
            "--type", help="Group permissions set symbolically",
            default="private", choices=defaultperms.keys())

    def parse_perms(self, args):
        from omero_model_PermissionsI import PermissionsI as Perms
        perms = getattr(args, "perms", None)
        if not perms:
            perms = defaultperms[args.type]
        try:
            return Perms(perms)
        except ValueError, ve:
            self.ctx.die(505, str(ve))

    @admin_only
    def add(self, args):

        import omero
        from omero.rtypes import rbool, rstring
        from omero_model_ExperimenterGroupI import ExperimenterGroupI as Grp

        perms = self.parse_perms(args)
        c = self.ctx.conn(args)
        g = Grp()
        g.name = rstring(args.name)
        g.ldap = rbool(False)
        g.details.permissions = perms
        admin = c.getSession().getAdminService()
        try:
            grp = admin.lookupGroup(args.name)
            if grp:
                if args.ignore_existing:
                    self.ctx.out("Group exists: %s (id=%s)"
                                 % (args.name, grp.id.val))
                    return
                else:
                    self.ctx.die(3, "Group exists: %s (id=%s)"
                                 % (args.name, grp.id.val))
        except omero.ApiUsageException:
            pass  # Apparently no such group exists

        try:
            id = admin.createGroup(g)
            self.ctx.out("Added group %s (id=%s) with permissions %s"
                         % (args.name, id, perms))
        except omero.ValidationException, ve:
            # Possible, though unlikely after previous check
            if self.exc.is_constraint_violation(ve):
                self.ctx.die(66, "Group already exists: %s" % args.name)
            else:
                self.ctx.die(67, "Unknown ValidationException: %s"
                             % ve.message)
        except omero.SecurityViolation, se:
            self.ctx.die(68, "Security violation: %s" % se.message)
        except omero.ServerError, se:
            self.ctx.die(4, "%s: %s" % (type(se), se.message))

    def perms(self, args):

        import omero

        perms = self.parse_perms(args)
        c = self.ctx.conn(args)
        a = c.sf.getAdminService()

        gid, g = self.parse_groupid(a, args)

        old_perms = str(g.details.permissions)
        if old_perms == str(perms):
            self.ctx.out("Permissions for group %s (id=%s) already %s"
                         % (g.name.val, gid, perms))
        else:
            try:
                chmod = omero.cmd.Chmod(
                    type="/ExperimenterGroup", id=gid, permissions=str(perms))
                c.submit(chmod, loops=120)
                self.ctx.out("Changed permissions for group %s (id=%s) to %s"
                             % (g.name.val, gid, perms))
            except omero.GroupSecurityViolation:
                import traceback
                self.ctx.dbg(traceback.format_exc())
                self.ctx.die(504, "Cannot change permissions for group %s"
                             " (id=%s) to %s" % (g.name.val, gid, perms))

    def list(self, args):
        c = self.ctx.conn(args)
        groups = c.sf.getAdminService().lookupGroups()
        self.output_groups_list(groups, args)

    def info(self, args):
        c = self.ctx.conn(args)
        a = c.sf.getAdminService()
        [gid, groups] = self.list_groups(a, args, use_context=True)
        self.output_groups_list(groups, args)

    def listusers(self, args):
        c = self.ctx.conn(args)
        admin = c.sf.getAdminService()
        [gids, groups] = self.list_groups(admin, args, use_context=True)
        if len(gids) != 1:
            self.ctx.die(516, 'Too many group arguments')
        users = admin.containedExperimenters(gids[0])
        self.output_users_list(admin, users, args)

    def parse_groupid(self, a, args):
        if args.id:
            group = getattr(args, "id", None)
            return self.find_group_by_id(a, group, fatal=True)
        elif args.name:
            group = getattr(args, "name", None)
            return self.find_group_by_name(a, group, fatal=True)
        else:
            self.error_no_input_group(fatal=True)

    def filter_users(self, uids, group, owner=False, join=True):

        if owner:
            uid_list = self.getownerids(group)
            relation = "owner of"
        else:
            uid_list = self.getuserids(group)
            relation = "in"

        for uid in list(uids):
            if join:
                if uid in uid_list:
                    self.ctx.out("%s is already %s group %s"
                                 % (uid, relation, group.id.val))
                    uids.remove(uid)
            else:
                if uid not in uid_list:
                    self.ctx.out("%s is not %s group %s"
                                 % (uid, relation, group.id.val))
                    uids.remove(uid)
        return uids

    def copyusers(self, args):
        c = self.ctx.conn(args)
        a = c.sf.getAdminService()
        f_gid, f_grp = self.find_group(a, args.from_group, fatal=True)
        t_gid, t_grp = self.find_group(a, args.to_group, fatal=True)

        if args.as_owner:
            uids = self.getownerids(f_grp)
        else:
            uids = self.getuserids(f_grp)
        uids = self.filter_users(uids, t_grp, args.as_owner, True)

        if args.as_owner:
            self.addownersbyid(a, t_grp, uids)
            self.ctx.out("Owners of %s copied to %s"
                         % (args.from_group, args.to_group))
        else:
            self.addusersbyid(a, t_grp, uids)
            self.ctx.out("Users of %s copied to %s"
                         % (args.from_group, args.to_group))

    def adduser(self, args):
        c = self.ctx.conn(args)
        a = c.sf.getAdminService()
        group = self.parse_groupid(a, args)[1]
        [uids, users] = self.list_users(a, args, use_context=False)
        uids = self.filter_users(uids, group, args.as_owner, True)

        if args.as_owner:
            self.addownersbyid(a, group, uids)
        else:
            self.addusersbyid(a, group, uids)

    def removeuser(self, args):
        c = self.ctx.conn(args)
        a = c.sf.getAdminService()
        group = self.parse_groupid(a, args)[1]
        [uids, users] = self.list_users(a, args, use_context=False)
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
