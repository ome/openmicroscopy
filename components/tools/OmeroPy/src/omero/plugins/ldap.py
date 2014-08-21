#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
   User administration plugin (LDAP extension)

   Copyright 2011 - 2014 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import sys

from omero.cli import BaseControl, CLI, ExceptionHandler

HELP = """Administrative support for managing users' LDAP settings

Most of these commands should be run as an OMERO administrator such as root.

Examples:

  bin/omero login root
  bin/omero ldap active
  bin/omero ldap active     || echo "Not active!"
  bin/omero ldap list
  bin/omero ldap getdn jack
  bin/omero ldap getdn beth || echo "No DN"
  bin/omero ldap setdn jack uid=me,ou=example,o=com
  bin/omero ldap setdn jack ""                  # Disables LDAP login.
  bin/omero ldap discover --commands            # Requires "ldap" module
  bin/omero ldap create jack                    # User jack must exist in LDAP

"""


class LdapControl(BaseControl):

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
            help="""Set DN for user (admins only)

Once the DN is set for a user, the password set via OMERO is
ignored, and any attempt to change it will result in an error. When
you remove the DN, the previous password will be in effect, but if the
user never had a password, one will need to be set!""")

        for x in (getdn, setdn):
            x.add_argument("username", help="User's OMERO login name")
        setdn.add_argument(
            "dn", help="User's LDAP distinguished name. If empty, LDAP will"
            " be disabled for the user")

        discover = parser.add(
            sub, self.discover,
            help="Discover distinguished names for existing OMERO users")
        discover.add_argument(
            "--commands", action="store_true", default=False,
            help="Print setdn commands on standard out")
        discover.add_argument(
            "--urls", help="Override OMERO omero.ldap.urls setting")
        discover.add_argument(
            "--base", help="Override OMERO omero.ldap.base setting")

        create = parser.add(
            sub, self.create,
            help="Create a local user based on LDAP username (admins only)"
            )
        create.add_argument(
            "username", help="LDAP username of user to be created")

        for x in (active, list, getdn, setdn, discover, create):
            x.add_login_arguments()

    def active(self, args):
        c = self.ctx.conn(args)
        ildap = c.sf.getLdapService()

        import omero
        try:
            if ildap.getSetting():
                self.ctx.out("Yes")
            else:
                self.ctx.die(1, "No")
        except omero.SecurityViolation:
            self.error_admin_only(fatal=True)

    def list(self, args):
        c = self.ctx.conn(args)
        iadmin = c.sf.getAdminService()

        import omero
        from omero.rtypes import unwrap
        from omero.util.text import TableBuilder
        try:

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

        except omero.SecurityViolation:
            self.error_admin_only(fatal=True)

    def getdn(self, args):
        c = self.ctx.conn(args)
        iadmin = c.sf.getAdminService()

        try:
            exp = iadmin.lookupExperimenter(args.username)
        except:
            self.ctx.die(134, "Unknown user: %s" % args.username)

        dn = iadmin.lookupLdapAuthExperimenter(exp.id.val)
        if dn is not None and dn.strip():
            self.ctx.out(dn)
        else:
            self.ctx.die(136, "DN Not found: %s" % dn)

    def setdn(self, args):
        c = self.ctx.conn(args)
        ildap = c.sf.getLdapService()
        iadmin = c.sf.getAdminService()

        try:
            exp = iadmin.lookupExperimenter(args.username)
        except:
            self.ctx.die(134, "Unknown user: %s" % args.username)

        import omero
        try:
            ildap.setDN(exp.id, args.dn)
        except omero.SecurityViolation:
            self.error_admin_only(fatal=True)

    def discover(self, args):
        c = self.ctx.conn(args)
        ildap = c.sf.getLdapService()
        experimenters = {}

        import omero
        try:
            experimenters = ildap.discover()
        except omero.SecurityViolation:
            self.ctx.die(131, "SecurityViolation: Admins only!")

        if len(experimenters) > 0:
            self.ctx.out("Found mismatching DNs for the following users:")
            for dn, exp in experimenters.iteritems():
                if args.commands:
                    self.ctx.out("%s ldap setdn %s %s"
                                 % (sys.argv[0], exp.getOmeName().getValue(),
                                    dn))
                else:
                    self.ctx.out("Experimenter:%s\tomeName=%s\t%s"
                                 % (exp.getId().getValue(),
                                    exp.getOmeName().getValue(), dn))

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
        except omero.SecurityViolation:
            self.ctx.die(131, "SecurityViolation: Admins only!")
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
