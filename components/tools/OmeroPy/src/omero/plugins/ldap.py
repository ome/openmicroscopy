#!/usr/bin/env python
"""
   User administration plugin (LDAP extension)

   Copyright 2011 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import os
import sys

from omero.cli import BaseControl, CLI, ExceptionHandler
from omero.rtypes import unwrap as _

HELP = """Administrative support for managing users' LDAP settings.

Most of these commands should be run as an OMERO administrator such
as root.

Examples:

  bin/omero login root
  bin/omero ldap active
  bin/omero ldap active     || echo "Not active!"
  bin/omero ldap list
  bin/omero ldap getdn jack
  bin/omero ldap getdn beth || echo "No DN"
  bin/omero ldap setdn jack true                      # Enables LDAP login
  bin/omero ldap setdn jack false                     # Disables LDAP login
  bin/omero ldap getgroupdn mylab
  bin/omero ldap getgroupdn mylab || echo "No DN"
  bin/omero ldap setgroupdn mylab true                # Enables LDAP login
  bin/omero ldap setgroupdn mylab false               # Disables LDAP login
  bin/omero ldap discover --commands                  # Requires "ldap" module

"""


class LdapControl(BaseControl):

    def _configure(self, parser):

        self.exc = ExceptionHandler()

        sub = parser.sub()

        active = parser.add(sub, self.active, \
                help = "Return code shows if LDAP is configured (admins-only)")

        list = parser.add(sub, self.list, help = "List all OMERO users with DNs")

        usertxt = """
Once LDAP is enabled for a user, the password set via OMERO is
ignored, and any attempt to change it will result in an error. When
you disable LDAP, the previous password will be in effect, but if the
user never had a password, one will need to be set!
"""

        for which, getter, setter, txt in (
                ("user", self.getdn, self.setdn, usertxt),
                ("group", self.getgroupdn, self.setgroupdn, "")):

            getdn = parser.add(sub, getter, help = "Get DN for %s on stdout" % which)
            setdn = parser.add(sub, setter, help = """Enable/disable DN-lookup for %s (admins only)
%s
Only valid values: true, false """ % (which, txt))

            for x in (getdn, setdn):
                x.add_argument("%sname" % which, \
                        help = "%s's OMERO login name" % which.capitalize())
            setdn.add_argument("dn", choices=("true","false"), \
                    help = "true to enable, false to disable LDAP-lookup")

        discover = parser.add(sub, self.discover, help = "Discover distinguished names for existing OMERO users")
        discover.add_argument("--commands", action="store_true", default=False, help = "Print setdn commands on standard out")
        discover.add_argument("--urls", help = "Override OMERO omero.ldap.urls setting")
        discover.add_argument("--base", help = "Override OMERO omero.ldap.base setting")

    def __import_ldap__(self):
        try:
            import ldap
        except:
            self.ctx.die(155, """Python "ldap"  module is not installed""")
        return ldap

    def active(self, args):
        c = self.ctx.conn(args)
        ildap= c.sf.getLdapService()

        import omero
        try:
            if ildap.getSetting():
                self.ctx.out("Yes")
            else:
                self.ctx.die(1, "No")
        except omero.SecurityViolation, sv:
            self.ctx.die(111, "SecurityViolation: Admins only!")

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
            tb.cols(["Id", "OmeName", "DN"])
            for map in list_of_dn_user_maps:
                for dn, id in map.items():
                    try:
                        exp = iadmin.getExperimenter(id)
                    except omero.ValidationException, ve:
                        self.ctx.err("Bad experimenter: %s" % id)
                        continue
                    tb.row(count, *(id, exp.omeName.val, dn))
                    count += 1
            self.ctx.out(str(tb.build()))

        except omero.SecurityViolation, sv:
            self.ctx.die(131, "SecurityViolation: Must be an admin to lists DNs")

    def _checkexists(self, iadmin, name, group=False):
        if group:
            try:
                return iadmin.lookupGroup(name)
            except:
                self.ctx.die(134, "Unknown group: %s" % name)
        else:
            try:
                return iadmin.lookupExperimenter(name)
            except:
                self.ctx.die(134, "Unknown user: %s" % name)

    def getdn(self, args):
        return self._getdn(args, args.username, group=False)

    def getgroupdn(self, args):
        return self._getdn(args, args.groupname, group=True)

    def _getdn(self, args, name, group=False):
        c = self.ctx.conn(args)
        iadmin = c.sf.getAdminService()
        ildap = c.sf.getLdapService()

        self._checkexists(iadmin, name, group)
        type = group and "group" or "user"

        import omero
        try:
            dn = ildap.findDN("%s:%s" % (type, name))
        except omero.ApiUsageException:
            dn = ""

        if dn is not None and dn.strip():
            self.ctx.out(dn)
            return dn
        else:
            self.ctx.die(1, dn, newline=False)

    def setdn(self, args):
        return self._setdn(args, args.username, group=False)

    def setgroupdn(self, args):
        return self._setdn(args, args.groupname, group=True)

    def _setdn(self, args, name, group=False):
        c = self.ctx.conn(args)
        ildap = c.sf.getLdapService()
        iadmin = c.sf.getAdminService()

        obj = self._checkexists(iadmin, name, group)
        type = group and "group" or "user"

        import omero
        dn = "%s:%s" % (type, args.dn)
        try:
            ildap.setDN(obj.id, dn)
        except omero.SecurityViolation, sv:
            self.ctx.die(135, "SecurityViolation: Admins only!")

    def discover(self, args):

        import omero
        ldap = self.__import_ldap__()

        c = self.ctx.conn(args)
        iconfig = c.sf.getConfigService()
        iadmin = c.sf.getAdminService()
        ildap = c.sf.getLdapService()

        LDAP_PROPERTIES = """
        omero.ldap.urls
        omero.ldap.username
        omero.ldap.password
        omero.ldap.base
        omero.ldap.user_filter
        omero.ldap.user_mapping
        omero.ldap.group_filter
        omero.ldap.group_mapping
        omero.ldap.new_user_group
        """.split()

        cfg = dict()
        for key in LDAP_PROPERTIES:
            cfg[key.split(".")[-1]] = iconfig.getConfigValue(key)


        urls = args.urls and args.urls or cfg["urls"]
        basedn = args.base and args.base or cfg["base"]

        for url in urls.split(","):

            self.ctx.err("Connecting to %s..." % url)

            ld = ldap.initialize(url)
            ld.simple_bind_s()

            user_filter = cfg["user_filter"]
            user_mapping = cfg["user_mapping"]
            user_mapping = user_mapping.split(",")
            omeName_mapping = None
            for um in user_mapping:
                parts = um.split("=")
                if parts[0] == "omeName":
                    omeName_mapping = parts[1]
            results = ld.search_s(basedn, ldap.SCOPE_SUBTREE, user_filter)
            for dn, entry in results:
                omeName = entry[omeName_mapping]
                if isinstance(omeName, (list, tuple)):
                    if len(omeName) == 1:
                        omeName = omeName[0]
                    else:
                        self.ctx.err("Failed to unwrap omeName: %s" % omeName)
                        continue
                try:
                    exp = iadmin.lookupExperimenter(omeName)
                    olddn = iadmin.lookupLdapAuthExperimenter(exp.id.val)
                except omero.ApiUsageException:
                    continue # Unknown user

                if olddn:
                    if olddn != dn:
                        self.ctx.err("Found different DN for %s: %s" % (omeName, olddn))
                    else:
                        self.ctx.dbg("DN already set for %s: %s" % (omeName, olddn))
                else:
                    if args.commands:
                        self.ctx.out("%s ldap setdn %s %s" % (sys.argv[0], omeName, dn))
                    else:
                        self.ctx.out("Experimenter:%s\tomeName=%s\t%s" % (exp.id.val, omeName, dn))


try:
    register("ldap", LdapControl, HELP)
except NameError:
    if __name__ == "__main__":
        cli = CLI()
        cli.register("ldap", LdapControl, HELP)
        cli.invoke(sys.argv[1:])
