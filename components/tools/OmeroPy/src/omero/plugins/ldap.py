#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
   User administration plugin (LDAP extension)

   Copyright 2011 - 2014 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import sys

from omero.cli import CLI, ExceptionHandler, admin_only, UserGroupControl
from omero.rtypes import rbool

HELP = """Administrative support for managing users' LDAP settings

Most of these commands should be run as an OMERO administrator such as root.

Examples:

  bin/omero login root
  bin/omero ldap active
  bin/omero ldap list
  bin/omero ldap getdn --user-name jack         # Get DN of user.
  bin/omero ldap setdn --user-name jack true    # Enable LDAP login.
  bin/omero ldap setdn --user-name jack false   # Disable LDAP login.
  bin/omero ldap getdn --group-name mylab       # Get DN of user group.
  bin/omero ldap setdn --group-name mylab true  # Mark group as LDAP group.
  bin/omero ldap setdn --group-name mylab false # Mark group as local.
  bin/omero ldap discover
  bin/omero ldap discover --groups
  bin/omero ldap create bob                     # User bob must exist in LDAP

"""


class LdapControl(UserGroupControl):

    def _configure(self, parser):

        self.exc = ExceptionHandler()

        sub = parser.sub()

        active = parser.add(
            sub, self.active,
            help="Return code shows if LDAP is configured (admins-only)")

        list = parser.add(
            sub, self.list,
            help="List all OMERO users with DNs")
        list.add_style_argument()

        getdn = parser.add(sub, self.getdn, help="Get DN for user on stdout")
        setdn = parser.add(
            sub, self.setdn,
            help="""Enable LDAP login for user (admins only)

Once LDAP login is enabled for a user, the password set via OMERO is
ignored, and any attempt to change it will result in an error. When
you disable LDAP login, the previous password will be in effect, but if the
user never had a password, one will need to be set!""")

        for x in (getdn, setdn):
            self.add_user_and_group_arguments(x)
        setdn.add_argument("choice", action="store",
                           help="Enable/disable LDAP login (true/false)")

        discover = parser.add(
            sub, self.discover,
            help="""Discover DNs for existing OMERO users or groups

This command works in the context of users or groups. Specifying
--groups will only discover groups, that is check which group exists in
the LDAP server and OMERO and has the "ldap" flag disabled - such groups
will be presented to the user. Omitting --groups will apply the same logic
to users.""")
        discover.add_argument(
            "--commands", action="store_true", default=False,
            help="Print setdn commands on standard out")
        discover.add_argument("--groups", action="store_true", default=False,
                              help="Discover LDAP groups, not users.")

        create = parser.add(
            sub, self.create,
            help="Create a local user based on LDAP username (admins only)"
        )
        create.add_argument(
            "username", help="LDAP username of user to be created")

        for x in (active, list, getdn, setdn, discover, create):
            x.add_login_arguments()

    @admin_only
    def active(self, args):
        c = self.ctx.conn(args)
        ildap = c.sf.getLdapService()

        if ildap.getSetting():
            self.ctx.out("Yes")
        else:
            self.ctx.die(1, "No")

    @admin_only
    def list(self, args):
        c = self.ctx.conn(args)
        iadmin = c.sf.getAdminService()

        from omero.rtypes import unwrap
        from omero.util.text import TableBuilder
        list_of_dn_user_maps = unwrap(iadmin.lookupLdapAuthExperimenters())
        if list_of_dn_user_maps is None:
            return

        count = 0
        tb = TableBuilder("#")
        if args.style:
            tb.set_style(args.style)
        tb.cols(["Id", "OmeName", "DN"])
        for map in list_of_dn_user_maps:
            for dn, id in map.items():
                try:
                    exp = iadmin.getExperimenter(id)
                except:
                    self.ctx.err("Bad experimenter: %s" % id)

                tb.row(count, *(id, exp.omeName.val, dn))
                count += 1
        self.ctx.out(str(tb.build()))

    @admin_only
    def getdn(self, args):
        c = self.ctx.conn(args)
        iadmin = c.sf.getAdminService()
        ildap = c.sf.getLdapService()

        dn = None

        if args.user_name:
            [uid, u] = self.find_user_by_name(iadmin, args.user_name,
                                              fatal=True)
            if u and u.getLdap().val:
                name = u.getOmeName().val
                dn = name + ": " + ildap.findDN(name)
        elif args.user_id:
            [uid, u] = self.find_user_by_id(iadmin, args.user_id, fatal=True)
            if u and u.getLdap().val:
                name = u.getOmeName().val
                dn = name + ": " + ildap.findDN(name)
        elif args.group_name:
            [gid, g] = self.find_group_by_name(iadmin, args.group_name,
                                               fatal=True)
            if g and g.getLdap().val:
                name = g.getName().val
                dn = name + ": " + ildap.findGroupDN(name)
        elif args.group_id:
            [gid, g] = self.find_group_by_id(iadmin, args.group_id, fatal=True)
            if g and g.getLdap().val:
                name = g.getName().val
                dn = name + ": " + ildap.findGroupDN(name)

        if dn is not None and dn.strip():
            self.ctx.out(dn)

    @admin_only
    def setdn(self, args):
        c = self.ctx.conn(args)
        iupdate = c.sf.getUpdateService()
        iadmin = c.sf.getAdminService()

        obj = None

        if args.user_name:
            [uid, obj] = self.find_user_by_name(iadmin, args.user_name,
                                                fatal=True)
        elif args.user_id:
            [uid, obj] = self.find_user_by_id(iadmin, args.user_id, fatal=True)
        elif args.group_name:
            [gid, obj] = self.find_group_by_name(iadmin, args.group_name,
                                                 fatal=True)
        elif args.group_id:
            [gid, obj] = self.find_group_by_id(iadmin, args.group_id,
                                               fatal=True)

        if obj is not None:
            obj.setLdap(rbool(args.choice.lower()
                              in ("yes", "true", "t", "1")))
            iupdate.saveObject(obj)

    @admin_only
    def discover(self, args):
        c = self.ctx.conn(args)
        ildap = c.sf.getLdapService()
        elements = {}
        element_name = "users"

        if args.groups:
            element_name = "groups"
            elements = ildap.discoverGroups()
        else:
            elements = ildap.discover()

        if len(elements) > 0:
            self.ctx.out("Following LDAP %s are disabled in OMERO:"
                         % element_name)
            for e in elements:
                if args.groups:
                    if args.commands:
                        self.ctx.out("%s ldap setdn --group-name %s true"
                                     % (sys.argv[0], e.getName().getValue()))
                    else:
                        self.ctx.out("Group=%s\tname=%s"
                                     % (e.getId().getValue(),
                                        e.getName().getValue()))
                else:
                    if args.commands:
                        self.ctx.out("%s ldap setdn --user-name %s true"
                                     % (sys.argv[0],
                                        e.getOmeName().getValue()))
                    else:
                        self.ctx.out("Experimenter=%s\tomeName=%s"
                                     % (e.getId().getValue(),
                                        e.getOmeName().getValue()))

    @admin_only
    def create(self, args):
        c = self.ctx.conn(args)
        ildap = c.sf.getLdapService()
        iadmin = c.sf.getAdminService()

        import omero
        import Ice
        try:
            exp = ildap.createUser(args.username)
            dn = iadmin.lookupLdapAuthExperimenter(exp.id.val)
            self.ctx.out("Added user %s (id=%s) with DN=%s" %
                         (exp.omeName.val, exp.id.val, dn))
        except omero.ValidationException as ve:
            self.ctx.die(132, ve.message)
        except Ice.RequestFailedException as rfe:
            self.ctx.die(133, self.exc.handle_failed_request(rfe))

try:
    register("ldap", LdapControl, HELP)
except NameError:
    if __name__ == "__main__":
        cli = CLI()
        cli.register("ldap", LdapControl, HELP)
        cli.invoke(sys.argv[1:])
