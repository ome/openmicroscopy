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
        perms_to_groups = {"other":[]}
        for perms in ("rw----", "rwr---", "rwrw--"):
            perms_to_groups[perms] = []
        for g in groups:
            if g.name.val in ("user"):
                continue
            try:
                perms_to_groups[str(g.details.permissions)].append(g.name.val)
            except KeyError:
                perms_to_groups["other"].append("%s (%s)" % (g.name.val, str(g.details.permissions)))
        for x in ("rw----", "rwr---", "rwrw--", "other"):
            if perms_to_groups[x]:
                self.ctx.out("# %s" % x)
                for g in perms_to_groups[x]:
                    self.ctx.out(g)

try:
    register("group", GroupControl, HELP)
except NameError:
    if __name__ == "__main__":
        cli = CLI()
        cli.register("group", GroupControl, HELP)
        cli.invoke(sys.argv[1:])
