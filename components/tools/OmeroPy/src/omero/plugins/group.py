#!/usr/bin/env python
"""
   Group administration plugin

   Copyright 2009 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import os
import sys

from omero.cli import Arguments, BaseControl, VERSION, OMERODIR

class GroupControl(BaseControl):

    def add(self, *args):
        args = Arguments(args, longopts=["perms="])
        perms = args.get_arg("perms", None)
        if not perms:
            perms = "rw----"

        if len(args) != 1:
            self.ctx.die(2, "Usage: --perms=[rwrwrw] <group name>")

        group_name = args.args[0]

	import omero, Ice
        from omero.rtypes import rstring
	from omero_model_PermissionsI import PermissionsI as Perms
	from omero_model_ExperimenterGroupI import ExperimenterGroupI as Grp

        c = self.ctx.conn(args)
	p = c.ic.getProperties()
	g = Grp()
        g.name = rstring(group_name)
        g.details.permissions = Perms(perms)
	admin = c.getSession().getAdminService()
	id = admin.createGroup(g)
	self.ctx.out("Added group %s with permissions %s" % (id, perms))

register("group", GroupControl)

