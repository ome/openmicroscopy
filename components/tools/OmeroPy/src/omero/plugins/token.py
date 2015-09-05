#!/usr/bin/env python
# -*- coding: utf-8 -*-

#
# Copyright (C) 2015 University of Dundee. All rights reserved.
# Use is subject to license terms supplied in LICENSE.txt
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License along
# with this program; if not, write to the Free Software Foundation, Inc.,
# 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.

"""
tokeb plugin for creating long-lived sessions
"""

import sys

import omero.sys
from omero.cli import CLI, BaseControl


HELP = """Token utilities"""


class TokenControl(BaseControl):

    def _configure(self, parser):

        parser.add_login_arguments()
        sub = parser.sub()

        create = parser.add(
            sub, self.create, help="create a new token")
        create.add_argument(
            "--expiration", type=long, default=3600*24*60,
            help="Expiration time for this token. After this many inactive"
            " seconds, the token will be destroyed. Default: 30 days")

        parser.add(
            sub, self.list, help="list all available tokens")

    def create(self, args):
        """Create a new token"""

        client = self.ctx.conn(args)

        ec = client.sf.getAdminService().getEventContext()
        principal = omero.sys.Principal()
        principal.name = ec.userName
        principal.group = ec.groupName
        principal.eventType = "User"

        # A token does not idle
        timeToIdle = 0
        timeToLive = args.expiration * 1000

        sess = client.sf.getSessionService().createSessionWithTimeouts(
            principal, timeToLive, timeToIdle)

        self.ctx.out(sess.uuid.val)

    def list(self, args):

        self.ctx.out('list current tokens')

try:
    register("token", TokenControl, HELP)
except NameError:
    if __name__ == "__main__":
        cli = CLI()
        cli.register("token", TokenControl, HELP)
        cli.invoke(sys.argv[1:])
