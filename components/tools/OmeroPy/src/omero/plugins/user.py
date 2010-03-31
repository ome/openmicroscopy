#!/usr/bin/env python
"""
   User administration plugin

   Copyright 2009 Glencoe Software, Inc. All rights reserved.
   Use is subject to license terms supplied in LICENSE.txt

"""

import os
import sys

from omero.cli import Arguments, BaseControl, VERSION, OMERODIR

class UserControl(BaseControl):

    def password(self, *args):
        args = Arguments(args)
        client = self.ctx.conn(args)
        pw = self._ask_for_password()
        from omero.rtypes import rstring
        client.sf.getAdminService().changePassword(rstring(pw))
        self.ctx.out("Password changed")

    def add(self, *args):
        args = Arguments(args, shortopts="e:i:g:p:", longopts=["email=","institute=","group=","password="])
        email = args.get_arg("email", "e")
        inst = args.get_arg("institute", "i")
        pasw = args.get_arg("password", "p")
        group = args.get_arg("group", "g")
        if not group:
            group = "CHANGE_ME"

	l = len(args)
        if l not in (3, 4):
            self.ctx.die(2, "Usage: omename firstname [middlename] lastname")

	if l == 3 :
		on, fn, ln = args.args
		mn = None
	elif l == 4 :
		on, fn, mn, ln = args.args

	import omero, Ice
        from omero.rtypes import rstring
	from omero_model_ExperimenterI import ExperimenterI as Exp
	from omero_model_ExperimenterGroupI import ExperimenterGroupI as Grp
        c = self.ctx.conn(args)
	p = c.ic.getProperties()
	e = Exp()
	e.omeName = rstring(on)
	e.firstName = rstring(fn)
	e.lastName = rstring(ln)
	e.middleName = rstring(mn)
	e.email = email and rstring(email) or None
	e.institution = inst and rstring(inst) or None
	admin = c.getSession().getAdminService()

        group = admin.lookupGroup(group)
        if pasw is None:
            id = admin.createUser(e, group.name.val)
            self.ctx.out("Added user %s" % id)
        else:
            user_group = Grp(admin.getSecurityRoles().userGroupId, False)
            id = admin.createExperimenterWithPassword(e, rstring(pasw), group, [user_group])
            self.ctx.out("Added user %s with password" % id)

register("user", UserControl)

